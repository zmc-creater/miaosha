package com.mc.miaosha.service.impl;

import com.mc.miaosha.dao.ItemDOMapper;
import com.mc.miaosha.dao.ItemStockDOMapper;
import com.mc.miaosha.dao.StockLogDOMapper;
import com.mc.miaosha.dataobject.ItemDO;
import com.mc.miaosha.dataobject.ItemStockDO;
import com.mc.miaosha.dataobject.StockLogDO;
import com.mc.miaosha.error.BusinessException;
import com.mc.miaosha.error.EmBusinessError;
import com.mc.miaosha.mq.MqProducer;
import com.mc.miaosha.service.ItemService;
import com.mc.miaosha.service.PromoService;
import com.mc.miaosha.service.model.ItemModel;
import com.mc.miaosha.service.model.PromoModel;
import com.mc.miaosha.validator.ValidationResult;
import com.mc.miaosha.validator.ValidatorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    
    private static final Logger logger = LoggerFactory.getLogger(ItemServiceImpl.class);

    @Autowired
    private MqProducer mqProducer;

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

    @Autowired
    private StockLogDOMapper stockLogDOMapper;

    @Override
    @Transactional
    public ItemModel createItem(ItemModel itemModel) {
        //入参校验
        ValidationResult result = validator.validate(itemModel);
        if (result.isHasErrors()) {
            return null;
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
    public ItemModel getItemById(Integer id) {
        logger.info("getItemById::id = [{}]", id);
        ItemDO itemDO = itemDOMapper.selectByPrimaryKey(id);
        if (itemDO == null) {
            return null;
        }

        ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
        ItemModel itemModel = this.covertModelFromDataObject(itemDO, itemStockDO);

        //获取商品活动信息
        PromoModel promoModel = promoService.getPromoByItemId(itemModel.getId());
        if (promoModel != null && promoModel.getStatus() != 3) {
            itemModel.setPromoModel(promoModel);
        }

        return itemModel;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public boolean decreaseStock(Integer itemId, Integer amount) {
        //redis中扣减商品库存，redis中是单线程，不存在并发问题，不用加分布式锁
        long result = redisTemplate.opsForValue().increment("promo_item_stock_"+itemId,amount * (-1));
        if (result > 0){
            //扣减库存成功
            return true;
        }else if(result == 0){
            //售完最后一件，设置标志
            redisTemplate.opsForValue().set("promo_item_stock_invalid"+itemId,"true");
            return true;
        }else {
            //回滚库存
            increaseStock(itemId,amount);
            return false;
        }

    }

    @Override
    public boolean increaseStock(Integer itemId, Integer amount) {
        redisTemplate.opsForValue().increment("promo_item_stock_" + itemId, amount);
        return true;
    }

    @Override
    public boolean asyncDecreaseStock(Integer itemId, Integer amount) {
        boolean mqResult = mqProducer.asyncReduceStock(itemId, amount);
        return mqResult;
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
    public ItemModel getItemByIdInCache(Integer id) {
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

    /**
     * 初始化对应的库存流水
     * @param itemId
     * @param amount
     * @return 库存流水id
     */
    @Override
    @Transactional
    public String initStockLod(Integer itemId, Integer amount) {
        StockLogDO stockLogDO = new StockLogDO();
        stockLogDO.setItemId(itemId);
        stockLogDO.setAmount(amount);

        //库存流水为初始状态
        stockLogDO.setStatus(1);
        stockLogDO.setStockLogId(UUID.randomUUID().toString().replace("-",""));

        stockLogDOMapper.insertSelective(stockLogDO);

        return stockLogDO.getStockLogId();
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
