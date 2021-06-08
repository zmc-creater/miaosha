package com.mc.miaosha.controller;

import com.mc.miaosha.controller.viewobject.UserVO;
import com.mc.miaosha.error.BusinessException;
import com.mc.miaosha.error.EmBusinessError;
import com.mc.miaosha.response.CommonReturnType;
import com.mc.miaosha.service.OrderService;
import com.mc.miaosha.service.model.OrderModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController("order")
@RequestMapping("/order")
public class OrderController extends BaseController{
    @Autowired
    private OrderService orderService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @RequestMapping(value = "/createorder",method = {RequestMethod.POST},consumes = {BaseController.CONTENT_TYPE_FORMED})
    public CommonReturnType createOrder(@RequestParam(name = "itemId")Integer itemId,
        @RequestParam(name = "amount")Integer amount) throws BusinessException {

        //验证用户登录状态
        Boolean isLogin = (Boolean)httpServletRequest.getSession().getAttribute("IS_LOGIN");
        if (isLogin == null || !isLogin.booleanValue()) {
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN);
        }

        //拿到用户登录信息
        UserVO userVO = (UserVO) httpServletRequest.getSession().getAttribute("LOGIN_USER");

        //下单
        OrderModel orderModel = orderService.createOrder(userVO.getId(), itemId, amount);

        return CommonReturnType.create(orderModel);
    }

}
