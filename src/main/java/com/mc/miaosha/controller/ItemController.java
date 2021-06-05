package com.mc.miaosha.controller;

import com.mc.miaosha.controller.viewobject.ItemVO;
import com.mc.miaosha.error.BusinessException;
import com.mc.miaosha.error.EmBusinessError;
import com.mc.miaosha.response.CommonReturnType;
import com.mc.miaosha.service.ItemService;
import com.mc.miaosha.service.model.ItemModel;
import com.sun.xml.internal.messaging.saaj.packaging.mime.util.BEncoderStream;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller("item")
@RequestMapping(value = "/item")
@CrossOrigin
public class ItemController extends BaseController{

    @Autowired
    private ItemService itemService;

    @RequestMapping(value = "/list",method = RequestMethod.GET)
    @ResponseBody
    public CommonReturnType listItem() throws BusinessException {

        List<ItemModel> itemModelList = itemService.listItem();
        if (itemModelList == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"没有商品");
        }
        return CommonReturnType.create(itemModelList);
    }

    @RequestMapping("/getItem")
    @ResponseBody
    public CommonReturnType getItem(@RequestParam(name = "id")Integer id) throws BusinessException {

        if (id == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        ItemModel itemModel = itemService.getItemById(id);
        ItemVO itemVO = this.coverVoFromModel(itemModel);

        return CommonReturnType.create(itemVO);
    }

    @RequestMapping(value = "/createItem",method = {RequestMethod.POST},consumes = {BaseController.CONTENT_TYPE_FORMED})
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
        ItemVO itemVO = this.coverVoFromModel(item);

        return CommonReturnType.create(itemVO);
    }

    private ItemVO coverVoFromModel(ItemModel itemModel) throws BusinessException {
        if (itemModel == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(itemModel,itemVO);
        return itemVO;
    }
}
