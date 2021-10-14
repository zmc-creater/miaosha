package com.mc.miaosha.controller;

import com.google.common.util.concurrent.RateLimiter;
import com.mc.miaosha.error.BusinessException;
import com.mc.miaosha.error.EmBusinessError;
import com.mc.miaosha.mq.MqProducer;
import com.mc.miaosha.response.CommonReturnType;
import com.mc.miaosha.service.ItemService;
import com.mc.miaosha.service.PromoService;
import com.mc.miaosha.service.model.UserModel;
import com.mc.miaosha.util.CodeUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

@RestController("order")
@RequestMapping("/order")
public class OrderController extends BaseController{
    final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private MqProducer mqProducer;

    @Autowired
    private PromoService promoService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ItemService itemService;

    private ExecutorService executorService;

    /**
     * google guava提供的限流工具
     */
    private RateLimiter orderCreateRateLimiter;

    @PostConstruct
    public void init(){
        executorService = Executors.newFixedThreadPool(20);
        //单机限制每秒的tps为200
        orderCreateRateLimiter = RateLimiter.create(200);
    }

    /**
     * 生成验证码
     * @param httpServletResponse
     * @return
     * @throws BusinessException
     */
    @RequestMapping(value = "/generateverifycode",method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public void generateverifycode(HttpServletResponse httpServletResponse) throws BusinessException {
        //根据token获取用户信息
        String token = httpServletRequest.getParameterMap().get("token")[0];
        if(StringUtils.isEmpty(token)){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登陆，不能下单");
        }
        //获取用户的登陆信息
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if(userModel == null){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登陆，不能下单");
        }

        Map<String,Object> map = CodeUtil.generateCodeAndPic();
        redisTemplate.opsForValue().set("verify_code_"+userModel.getId(),map.get("code"));
        redisTemplate.expire("verify_code_"+userModel.getId(),10,TimeUnit.MINUTES);

        try {
            ImageIO.write((RenderedImage) map.get("codePic"), "jpeg", httpServletResponse.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            throw new BusinessException(EmBusinessError.UNKNOWN_ERROR,"生成验证码失败");
        }
    }
    /**
     * 生成秒杀令牌
     */
    @RequestMapping(value = "/generatetoken",method = {RequestMethod.POST},consumes={CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType generateToken(@RequestParam(name="itemId")Integer itemId,
                                          @RequestParam(name="promoId")Integer promoId,
                                          @RequestParam(name="verifyCode")String verifyCode) throws BusinessException {
        //根据token获取用户信息
        String token = httpServletRequest.getParameterMap().get("token")[0];
        if(StringUtils.isEmpty(token)){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登陆，不能下单");
        }
        //获取用户的登陆信息
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if(userModel == null){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登陆，不能下单");
        }


        //通过verifyCode验证验证码的有效性
        String redisVerifyCode = (String) redisTemplate.opsForValue().get("verify_code_"+userModel.getId());
        if(StringUtils.isEmpty(redisVerifyCode)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"请求非法");
        }
        if(!redisVerifyCode.equalsIgnoreCase(verifyCode)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"请求非法，验证码错误");
        }

        //获取秒杀访问令牌
        String promoToken = promoService.generateSecondKillToken(promoId,itemId,userModel.getId());
        if(promoToken == null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"生成令牌失败");
        }
        //返回对应的结果
        return CommonReturnType.create(promoToken);
    }

    @RequestMapping(value = "/createorder",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    public CommonReturnType createOrder(@RequestParam(name = "itemId")Integer itemId,
        @RequestParam(name = "amount")Integer amount,
        @RequestParam(name = "promoId",required = false)Integer promoId,
        @RequestParam(name="promoToken",required = false)String promoToken) throws BusinessException {

        if(!orderCreateRateLimiter.tryAcquire()){
            throw new BusinessException(EmBusinessError.RATELIMTE);
        }
        //验证用户登录状态
        String token = httpServletRequest.getParameterMap().get("token")[0];
        //拿到用户登录信息
        UserModel userModel = (UserModel)redisTemplate.opsForValue().get(token);
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN);
        }

        //校验秒杀令牌是否正确
        if(promoId != null){
            String inRedisPromoToken = (String) redisTemplate.opsForValue().get("promo_token_"+promoId+"_userid_"+userModel.getId()+"_itemid_"+itemId);
            if(inRedisPromoToken == null){
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"秒杀令牌校验失败");
            }
            if(!org.apache.commons.lang3.StringUtils.equals(promoToken,inRedisPromoToken)){
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"秒杀令牌校验失败");
            }
        }

        //同步调用线程池的submit方法
        //拥塞窗口为20的等待队列，用来队列化泄洪
        Future<Object> future = executorService.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                //加入库存流水init状态
                String stockLogId = itemService.initStockLod(itemId,amount);
                //再去完成对应的下单事务型消息机制
                if(!mqProducer.transactionAsyncReduceStock(userModel.getId(),itemId,promoId,amount,stockLogId)){
                    throw new BusinessException(EmBusinessError.UNKNOWN_ERROR,"下单失败");
                }
                return null;
            }
        });

        try {
            future.get();
        } catch (InterruptedException e) {
            throw new BusinessException(EmBusinessError.UNKNOWN_ERROR);
        } catch (ExecutionException e) {
            throw new BusinessException(EmBusinessError.UNKNOWN_ERROR);
        }

        return CommonReturnType.create(null);
    }

}
