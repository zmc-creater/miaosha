package com.mc.miaosha.dao;

import com.mc.miaosha.dataobject.UserDO;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDOMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(UserDO record);

    int insertSelective(UserDO record);

    UserDO selectByPrimaryKey(Integer id);

    UserDO selectByPhone(String phone);

    int updateByPrimaryKeySelective(UserDO record);

    int updateByPrimaryKey(UserDO record);
}