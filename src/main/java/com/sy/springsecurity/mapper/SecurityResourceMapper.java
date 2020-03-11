package com.sy.springsecurity.mapper;

import com.sy.springsecurity.domain.SecurityResource;

public interface SecurityResourceMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(SecurityResource record);

    int insertSelective(SecurityResource record);

    SecurityResource selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(SecurityResource record);

    int updateByPrimaryKey(SecurityResource record);
}