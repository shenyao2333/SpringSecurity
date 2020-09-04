package com.sy.springsecurity.mapper;

import com.sy.springsecurity.domain.SecurityUser;
import com.sy.springsecurity.surictiy.SelfUserDetails;

public interface SecurityUserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(SecurityUser record);

    int insertSelective(SecurityUser record);

    SecurityUser selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(SecurityUser record);

    int updateByPrimaryKey(SecurityUser record);


    SelfUserDetails selectByUserName(String username);
}
