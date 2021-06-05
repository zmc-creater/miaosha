package com.mc.miaosha.dao;

import com.mc.miaosha.dataobject.UserPasswordDO;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPasswordDOMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(UserPasswordDO record);

    int insertSelective(UserPasswordDO record);

    UserPasswordDO selectByPrimaryKey(Integer id);

    //按照id查询
    UserPasswordDO selectByUserId(Integer id);

    int updateByPrimaryKeySelective(UserPasswordDO record);

    int updateByPrimaryKey(UserPasswordDO record);
}