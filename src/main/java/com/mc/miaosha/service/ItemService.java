package com.mc.miaosha.service;

import com.mc.miaosha.error.BusinessException;
import com.mc.miaosha.service.model.ItemModel;

import java.util.List;

public interface ItemService {
    //创建商品
    ItemModel createItem(ItemModel itemModel) throws BusinessException;
    //商品列表浏览
    List<ItemModel> listItem();
    //商品详情查询
    ItemModel getItemById(Integer id) throws BusinessException;

    boolean decreaseStock(Integer itemId,Integer amount) throws BusinessException;

    //回滚库存
    boolean increaseStock(Integer itemId,Integer amount) throws BusinessException;
    //异步更新库存
    boolean asyncDecreaseStock(Integer itemId,Integer amount);

    boolean increaseSales(Integer itemId,Integer amount) throws BusinessException;

    ItemModel getItemByIdInCache(Integer id) throws BusinessException;

    String initStockLod(Integer itemId,Integer amount);
}
