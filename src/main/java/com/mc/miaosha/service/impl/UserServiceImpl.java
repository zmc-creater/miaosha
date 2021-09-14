package com.mc.miaosha.service.impl;

import com.mc.miaosha.dao.UserDOMapper;
import com.mc.miaosha.dao.UserPasswordDOMapper;
import com.mc.miaosha.dataobject.UserDO;
import com.mc.miaosha.dataobject.UserPasswordDO;
import com.mc.miaosha.error.BusinessException;
import com.mc.miaosha.error.EmBusinessError;
import com.mc.miaosha.service.UserService;
import com.mc.miaosha.service.model.UserModel;
import com.mc.miaosha.validator.ValidationResult;
import com.mc.miaosha.validator.ValidatorImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDOMapper userDOMapper;

    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;

    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public UserModel getUserById(Integer id) {
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(id);
        return covertFromDataObject(userDO,userPasswordDO);
    }

    @Override
    @Transactional
    public void register(UserModel userModel) throws BusinessException {
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }

        /*
        if(StringUtils.isEmpty(userModel.getName())
                ||userModel.getGender() == null
                ||userModel.getAge() ==null
                || StringUtils.isEmpty(userModel.getPhone())
                ||StringUtils.isEmpty(userModel.getEncrptPaaword())){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        */
        ValidationResult result = this.validator.validate(userModel);
        if (result.isHasErrors()) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,result.getErrMsg());
        }

        UserDO userDO = covertFromModel(userModel);
        
        try {
            userDOMapper.insertSelective(userDO);
        } catch (Exception exception) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"用户已注册");
        }
        UserPasswordDO userPasswordDO = covertFromPasswordModel(userModel);
        userPasswordDO.setUserId(userDO.getId());
        userPasswordDOMapper.insertSelective(userPasswordDO);
        return;
    }

    @Override
    public UserModel validLogin(String telphone,String encrpPassword) throws BusinessException {
        //按照手机号查询用户时候存在
        UserDO userDO = userDOMapper.selectByPhone(telphone);
        if (userDO == null) {
            throw new BusinessException(EmBusinessError.USER_LOGIN_ERROR);
        }
        //比对密码是否一致
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        UserModel userModel = covertFromDataObject(userDO, userPasswordDO);
        if (!StringUtils.equals(encrpPassword,userModel.getEncrptPaaword())) {
            throw new BusinessException(EmBusinessError.USER_LOGIN_ERROR);
        }
        return userModel;
    }

    @Override
    public UserModel getUserByIdInCache(Integer id) {
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get("user_validate_"+id);
        if (userModel == null) {
            userModel = getUserById(id);
            if (userModel == null) {
                redisTemplate.opsForValue().set("user_validate_" + id, userModel);
                redisTemplate.expire("user_validate_" + id, 10, TimeUnit.MINUTES);
            }
        }
        return userModel;
    }

    private UserModel covertFromDataObject(UserDO userDo,UserPasswordDO userPasswordDO){
        if (userDo == null) {
            return null;
        }
        UserModel userModel = new UserModel();
        if (userPasswordDO != null) {
            userModel.setEncrptPaaword(userPasswordDO.getEncrptPaaword());
        }
        BeanUtils.copyProperties(userDo,userModel);
        return userModel;
    }
    private UserPasswordDO covertFromPasswordModel(UserModel userModel){
        if (userModel == null) {
            return null;
        }
        UserPasswordDO userPasswordDO = new UserPasswordDO();
        userPasswordDO.setEncrptPaaword(userModel.getEncrptPaaword());

        return userPasswordDO;
    }
    private UserDO covertFromModel(UserModel userModel){
        if (userModel == null) {
            return null;
        }
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userModel,userDO);
        return userDO;
    }

}
