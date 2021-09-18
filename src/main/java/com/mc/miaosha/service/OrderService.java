package com.mc.miaosha.service;

import com.mc.miaosha.error.BusinessException;
import com.mc.miaosha.service.model.OrderModel;

public interface OrderService {
    OrderModel createOrder(Integer uerId, Integer itemId,Integer promoId, Integer amount,String stockLogId) throws BusinessException;

}
