package com.mc.miaosha.controller;

import com.mc.miaosha.error.BusinessException;
import com.mc.miaosha.error.EmBusinessError;
import com.mc.miaosha.mq.MqProducer;
import com.mc.miaosha.response.CommonReturnType;
import com.mc.miaosha.service.ItemService;
import com.mc.miaosha.service.OrderService;
import com.mc.miaosha.service.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController("order")
@RequestMapping("/order")
public class OrderController extends BaseController{

    @Autowired
    private MqProducer mqProducer;

    @Autowired
    private OrderService orderService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ItemService itemService;

    @RequestMapping(value = "/createorder",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    public CommonReturnType createOrder(@RequestParam(name = "itemId")Integer itemId,
        @RequestParam(name = "amount")Integer amount,
        @RequestParam(name = "promoId")String netPromoId) throws BusinessException {

        //验证用户登录状态

        //Boolean isLogin = (Boolean)httpServletRequest.getSession().getAttribute("IS_LOGIN");
        String token = httpServletRequest.getParameterMap().get("token")[0];

        //拿到用户登录信息
        UserModel userModel = (UserModel)redisTemplate.opsForValue().get(token);
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN);
        }

        //UserVO userVO = (UserVO) httpServletRequest.getSession().getAttribute("LOGIN_USER");

        //下单
        Integer promoId;
        if("".equals(netPromoId)){
            promoId = null;
        }else{
            promoId = Integer.valueOf(netPromoId);
        }
        //OrderModel orderModel = orderService.createOrder(userModel.getId(), itemId, promoId, amount);

        if(!mqProducer.transactionAsyncReduceStock(itemId,amount,userModel.getId(),promoId)){
            throw new BusinessException(EmBusinessError.MQ_SEND_FAIL);
        }

        return CommonReturnType.create(null);
    }

}
