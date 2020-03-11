package com.sy.springsecurity.service;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import com.sy.springsecurity.domain.SecurityRole;
import com.sy.springsecurity.mapper.SecurityRoleMapper;
@Service
public class SecurityRoleService{

    @Resource
    private SecurityRoleMapper securityRoleMapper;

    
    public int deleteByPrimaryKey(Integer id) {
        return securityRoleMapper.deleteByPrimaryKey(id);
    }

    
    public int insert(SecurityRole record) {
        return securityRoleMapper.insert(record);
    }

    
    public int insertSelective(SecurityRole record) {
        return securityRoleMapper.insertSelective(record);
    }

    
    public SecurityRole selectByPrimaryKey(Integer id) {
        return securityRoleMapper.selectByPrimaryKey(id);
    }

    
    public int updateByPrimaryKeySelective(SecurityRole record) {
        return securityRoleMapper.updateByPrimaryKeySelective(record);
    }

    
    public int updateByPrimaryKey(SecurityRole record) {
        return securityRoleMapper.updateByPrimaryKey(record);
    }

}
