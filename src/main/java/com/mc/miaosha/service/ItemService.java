package com.mc.miaosha.service;

import com.mc.miaosha.service.model.ItemModel;

import java.util.List;

public interface ItemService {
    //创建商品
    ItemModel createItem(ItemModel itemModel);
    //商品列表浏览
    List<ItemModel> listItem();
    //商品详情查询
    ItemModel getItemById(Integer id);

    /**
     * 扣减库存
     * @param itemId
     * @param amount
     * @return
     */
    boolean decreaseStock(Integer itemId,Integer amount);

    /**
     * 回滚库存
     * @param itemId
     * @param amount
     * @return
     */
    boolean increaseStock(Integer itemId,Integer amount);

    //异步更新库存
    boolean asyncDecreaseStock(Integer itemId,Integer amount);

    boolean increaseSales(Integer itemId,Integer amount);

    ItemModel getItemByIdInCache(Integer id);

    /**
     * 初始化库存流水状态
     * @param itemId
     * @param amount
     * @return
     */
    String initStockLod(Integer itemId,Integer amount);
}
