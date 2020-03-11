package com.sy.springsecurity.mapper;

import com.sy.springsecurity.domain.SecurityRole;

public interface SecurityRoleMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(SecurityRole record);

    int insertSelective(SecurityRole record);

    SecurityRole selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(SecurityRole record);

    int updateByPrimaryKey(SecurityRole record);
}