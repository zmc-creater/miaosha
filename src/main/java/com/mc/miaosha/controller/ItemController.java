package com.mc.miaosha.controller;

import com.alibaba.druid.support.json.JSONUtils;
import com.mc.miaosha.controller.viewobject.ItemVO;
import com.mc.miaosha.error.BusinessException;
import com.mc.miaosha.error.EmBusinessError;
import com.mc.miaosha.response.CommonReturnType;
import com.mc.miaosha.service.CacheService;
import com.mc.miaosha.service.ItemService;
import com.mc.miaosha.service.PromoService;
import com.mc.miaosha.service.model.ItemModel;
import com.mc.miaosha.service.model.PromoModel;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Controller("item")
@RequestMapping(value = "/item")
public class ItemController extends BaseController{

    @Autowired
    private ItemService itemService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private PromoService promoService;

    @RequestMapping(value = "/list",method = RequestMethod.GET)
    @ResponseBody
    public CommonReturnType listItem() throws BusinessException {

        List<ItemModel> itemModelList = itemService.listItem();
        if (itemModelList == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"没有商品");
        }
        return CommonReturnType.create(itemModelList);
    }

    @RequestMapping(value = "/get", method = {RequestMethod.GET})
    @ResponseBody
    public CommonReturnType getItem(@RequestParam(name = "id")Integer id) throws BusinessException {

        if (id == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }

        //先查本地缓存
        ItemModel itemModel = (ItemModel)cacheService.getCommonCache("item_" + id);
        if(itemModel == null){
            //查redis缓存
            itemModel = (ItemModel)redisTemplate.opsForValue().get("item_" + id);
            if(itemModel == null){
                itemModel = itemService.getItemById(id);
                redisTemplate.opsForValue().set("item_"+id, itemModel);
                redisTemplate.expire("item_"+id,10, TimeUnit.MINUTES);
            }
            cacheService.setCommonCache("item_"+id,itemModel);
        }

        ItemVO itemVO = this.covertVOFromModel(itemModel);

        return CommonReturnType.create(itemVO);
    }

    @RequestMapping(value = "/createItem",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createItem(@RequestParam(name = "title")String title,
                                       @RequestParam(name = "price")BigDecimal price,
                                       @RequestParam(name = "stock")Integer stock,
                                       @RequestParam(name = "description")String description,
                                       @RequestParam(name = "imgUrl")String imgUrl) throws BusinessException {

        ItemModel itemModel = new ItemModel();
        itemModel.setTitle(title);
        itemModel.setPrice(price);
        itemModel.setStock(stock);
        itemModel.setDescription(description);
        itemModel.setImgUrl(imgUrl);

        ItemModel item = itemService.createItem(itemModel);
        ItemVO itemVO = this.covertVOFromModel(item);

        return CommonReturnType.create(itemVO);
    }

    @RequestMapping(value = "/publishpromo", method = {RequestMethod.GET})
    @ResponseBody
    public CommonReturnType publishPromo(@RequestParam(name = "id")Integer id) throws BusinessException {
        promoService.publishPromo(id);
        return CommonReturnType.create(null);
    }

        private ItemVO covertVOFromModel(ItemModel itemModel) throws BusinessException {
        if (itemModel == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(itemModel,itemVO);

        PromoModel itemPromo = itemModel.getPromoModel();
        if(itemPromo !=null){//有秒杀活动
            itemVO.setPromoStatus(itemPromo.getStatus());
            itemVO.setPromoId(itemPromo.getId());
            itemVO.setPromoPrice(itemPromo.getPromoItemPrice());
            itemVO.setPromoStartTime(itemPromo.getStartTime().toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
        }else{
            itemVO.setPromoStatus(0);
        }
        return itemVO;
    }
}
