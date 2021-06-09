package com.mc.miaosha.service.impl;

import com.mc.miaosha.dao.PromoDOMapper;
import com.mc.miaosha.dataobject.PromoDO;
import com.mc.miaosha.service.PromoService;
import com.mc.miaosha.service.model.PromoModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PromoServiceImpl implements PromoService {
    @Autowired
    private PromoDOMapper promoDOMapper;

    @Override
    public PromoModel getPromoByItemId(Integer itemId) {
        PromoDO promoDO = promoDOMapper.selectByItemId(itemId);
        PromoModel promoModel = this.covertFromDataObject(promoDO);
        if (promoModel == null) {
            return null;
        }
        //根据秒杀活动的时间判断秒杀活动状态
        if (promoModel.getStartTime().isAfterNow()) {
            //秒杀活动还未开始
            promoModel.setStatus(1);
        } else if (promoModel.getEndTime().isBeforeNow()) {
            //秒杀活动已经结束
            promoModel.setStatus(3);
        } else {
            //秒杀活动正在进行
            promoModel.setStatus(2);
        }

        return promoModel;
    }

    private PromoModel covertFromDataObject(PromoDO promoDO){
        if (promoDO == null) {
            return null;
        }
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promoDO,promoModel);
        promoModel.setStartTime(new DateTime(promoDO.getStartTime()));
        promoModel.setEndTime(new DateTime(promoDO.getEndTime()));
        return promoModel;
    }
}
