package com.mc.miaosha.service;

import com.mc.miaosha.error.BusinessException;
import com.mc.miaosha.service.model.UserModel;

public interface UserService {
    UserModel getUserById(Integer id);
    void register(UserModel userModel) throws BusinessException;
    UserModel validLogin(String telphone,String encrpPassword) throws BusinessException;

    UserModel getUserByIdInCache(Integer id);
}
