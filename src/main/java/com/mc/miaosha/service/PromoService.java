package com.mc.miaosha.service;

import com.mc.miaosha.error.BusinessException;
import com.mc.miaosha.service.model.PromoModel;

public interface PromoService {
    /**
     * 根据itemId确定当前时间是否有秒杀活动
     * @param itemId
     * @return
     */
    PromoModel getPromoByItemId(Integer itemId);

    /**
     * 发布活动
     * @param promoId
     */
    void publishPromo(Integer promoId) throws BusinessException;
}
