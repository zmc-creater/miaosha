package com.mc.miaosha.service;

import com.mc.miaosha.service.model.PromoModel;

public interface PromoService {
    /**
     * 根据itemId确定当前时间是否有秒杀活动
     * @param itemId
     * @return
     */
    PromoModel getPromoByItemId(Integer itemId);
}
