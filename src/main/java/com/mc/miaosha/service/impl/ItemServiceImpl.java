package com.mc.miaosha.service.impl;

import com.mc.miaosha.dao.ItemDOMapper;
import com.mc.miaosha.dao.ItemStockDOMapper;
import com.mc.miaosha.dataobject.ItemDO;
import com.mc.miaosha.dataobject.ItemStockDO;
import com.mc.miaosha.error.BusinessException;
import com.mc.miaosha.error.EmBusinessError;
import com.mc.miaosha.service.ItemService;
import com.mc.miaosha.service.PromoService;
import com.mc.miaosha.service.model.ItemModel;
import com.mc.miaosha.service.model.PromoModel;
import com.mc.miaosha.validator.ValidationResult;
import com.mc.miaosha.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private ItemDOMapper itemDOMapper;

    @Autowired
    private ItemStockDOMapper itemStockDOMapper;

    @Autowired
    private PromoService promoService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    @Transactional
    public ItemModel createItem(ItemModel itemModel) throws BusinessException {
        //入参校验
        ValidationResult result = validator.validate(itemModel);
        if (result.isHasErrors()) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,result.getErrMsg());
        }

//        if (itemDOMapper.selectByTitle(itemModel.getTitle())==0) {
//            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"商品已存在");
//        }
        
        //转化model->dataobject
        ItemDO itemDO = this.covertItemDOFromItemModel(itemModel);

        //数据库中插入数据
        itemDOMapper.insertSelective(itemDO);
        itemModel.setId(itemDO.getId());

        ItemStockDO itemStockDO = this.covertItemStockDOFromItemModel(itemModel);
        itemStockDO.setItemId(itemModel.getId());
        itemStockDOMapper.insertSelective(itemStockDO);
        //返回数据
        return getItemById(itemModel.getId());
    }

    @Override
    public List<ItemModel> listItem() {
        List<ItemDO> itemDOS = itemDOMapper.listItem();

        List<ItemModel> itemModelList = itemDOS.stream().map(itemDO -> {
            ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
            ItemModel itemModel = this.covertModelFromDataObject(itemDO, itemStockDO);
            return itemModel;
        }).collect(Collectors.toList());

        return itemModelList;
    }

    @Override
    public ItemModel getItemById(Integer id) throws BusinessException {

        ItemDO itemDO = null;

        itemDO = itemDOMapper.selectByPrimaryKey(id);
        if (itemDO == null) {
            throw new BusinessException(EmBusinessError.ITEM_NOT_EXIST,EmBusinessError.ITEM_NOT_EXIST.getErrMsg());
        }

        ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
        ItemModel itemModel = this.covertModelFromDataObject(itemDO, itemStockDO);

        //获取商品活动信息
        PromoModel promoModel = promoService.getPromoByItemId(itemModel.getId());
        if (promoModel != null && promoModel.getStatus().intValue() != 3) {
            itemModel.setPromoModel(promoModel);
        }

        return itemModel;
    }

    @Override
    @Transactional
    public boolean decreaseStock(Integer itemId, Integer amount) {
        int effect = itemStockDOMapper.decreaseStock(amount, itemId);
        if (effect < 0){
            return false;
        }
        return true;
    }

    @Override
    @Transactional
    public boolean increaseSales(Integer itemId, Integer amount) {
        int increaseSales = itemDOMapper.increaseSales(amount,itemId);
        if (increaseSales <= 0) {
            return false;
        }
        return true;
    }

    @Override
    public ItemModel getItemByIdInCache(Integer id) throws BusinessException {
        ItemModel itemModel = (ItemModel) redisTemplate.opsForValue().get("item_validate_" + id);
        if (itemModel == null) {
            itemModel = getItemById(id);
            if (itemModel != null) {
                redisTemplate.opsForValue().set("item_validate_" + id, itemModel);
                redisTemplate.expire("item_validate_" + id, 10, TimeUnit.MINUTES);
            }
        }
        return itemModel;
    }

    private ItemModel covertModelFromDataObject(ItemDO itemDO,ItemStockDO itemStockDO){
        if (itemDO == null){
            return null;
        }
        ItemModel itemModel = new ItemModel();
        if (itemStockDO != null) {
            itemModel.setStock(itemStockDO.getStock());
        }
        BeanUtils.copyProperties(itemDO,itemModel);
        return itemModel;
    }

    private ItemStockDO covertItemStockDOFromItemModel(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemStockDO itemStockDO = new ItemStockDO();
        itemStockDO.setStock(itemModel.getStock());
        return itemStockDO;
    }

    private ItemDO covertItemDOFromItemModel(ItemModel itemModel){
        if (itemModel == null) {
            return null;
        }
        ItemDO itemDO = new ItemDO();
        BeanUtils.copyProperties(itemModel,itemDO);
        return itemDO;
    }
}
