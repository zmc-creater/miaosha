package com.mc.miaosha.service.impl;

import com.mc.miaosha.dao.OrderDOMapper;
import com.mc.miaosha.dao.SequenceDOMapper;
import com.mc.miaosha.dataobject.OrderDO;
import com.mc.miaosha.dataobject.SequenceDO;
import com.mc.miaosha.error.BusinessException;
import com.mc.miaosha.error.EmBusinessError;
import com.mc.miaosha.service.ItemService;
import com.mc.miaosha.service.OrderService;
import com.mc.miaosha.service.UserService;
import com.mc.miaosha.service.model.ItemModel;
import com.mc.miaosha.service.model.OrderModel;
import com.mc.miaosha.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private OrderDOMapper orderDOMapper;

    @Autowired
    private SequenceDOMapper sequenceDOMapper;

    @Override
    public OrderModel createOrder(Integer userId, Integer itemId, Integer amount) throws BusinessException {
        //信息校验
        UserModel userModel = userService.getUserById(userId);
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"用户信息不正确");
        }
        ItemModel itemModel = itemService.getItemById(itemId);
        if (itemModel == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"商品信息异常");
        }
        if(amount <=0 || amount >99){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"购买数量不正确");
        }

        //订单落地扣款（支付落地扣款）
        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setItemId(itemId);
        orderModel.setAmount(amount);
        orderModel.setItemPrice(itemModel.getPrice());
        orderModel.setOrderPrice(itemModel.getPrice().multiply(new BigDecimal(amount)));
        //减商品库存
        boolean succeed = itemService.decreaseStock(itemId, amount);
        if (!succeed){
            throw new BusinessException(EmBusinessError.ITEM_AMOUNT_NOT_ENOUGH,"商品数量不足");
        }
        //增加商品销量
        itemService.increaseSales(itemId, amount);
        //生成订单流水号
        orderModel.setId(this.generateOrderNO());

        //订单入库
        OrderDO orderDO = this.covertFromModel(orderModel);
        orderDOMapper.insertSelective(orderDO);

        return orderModel;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateOrderNO(){
        StringBuilder orderNO = new StringBuilder();
        LocalDateTime localDateTime = LocalDateTime.now();
        String nowDate = localDateTime.format(DateTimeFormatter.ISO_DATE).replace("-","");
        orderNO.append(nowDate);

        //中间6位,自动增加位
        //获取sequence
        int sequence = 0;
        SequenceDO sequenceDO = sequenceDOMapper.getSequenceByName("order_info");
        sequence = sequenceDO.getCurrentValue();
        sequenceDO.setCurrentValue(sequence + sequenceDO.getStep());
        sequenceDOMapper.updateByPrimaryKeySelective(sequenceDO);

        String sequenceStr = String.valueOf(sequence);
        for (int i = 0; i < 6- sequenceStr.length(); i++) {
            orderNO.append(0);
        }
        orderNO.append(sequenceStr);

        //末尾2位分库分表位
        orderNO.append("00");
        return orderNO.toString();
    }

    private OrderDO covertFromModel(OrderModel orderModel){
        if (orderModel == null) {
            return null;
        }
        OrderDO orderDO = new OrderDO();
        BeanUtils.copyProperties(orderModel,orderDO);
        return orderDO;
    }
}
