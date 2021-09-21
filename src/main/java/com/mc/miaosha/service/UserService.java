package com.mc.miaosha.service;

import com.mc.miaosha.error.BusinessException;
import com.mc.miaosha.service.model.UserModel;

public interface UserService {
    UserModel getUserById(Integer id);

    /**
     * user register
     * @param userModel user model
     * @throws BusinessException
     */
    void register(UserModel userModel) throws BusinessException;

    /**
     * 验证登录信息
     * @param telphone user phone number
     * @param encrpPassword encrypted password
     * @return
     * @throws BusinessException
     */
    UserModel validLogin(String telphone,String encrpPassword) throws BusinessException;

    /**
     * 根据用户id在缓存中查找用户
     * @param userId
     * @return
     */
    UserModel getUserByIdInCache(Integer userId);
}
