package com.sy.springsecurity.service;

import com.sy.springsecurity.surictiy.SelfUserDetails;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import com.sy.springsecurity.mapper.SecurityUserMapper;
import com.sy.springsecurity.domain.SecurityUser;
@Service
public class SecurityUserService{

    @Resource
    private SecurityUserMapper securityUserMapper;

    
    public int deleteByPrimaryKey(Integer id) {
        return securityUserMapper.deleteByPrimaryKey(id);
    }

    
    public int insert(SecurityUser record) {
        return securityUserMapper.insert(record);
    }

    
    public int insertSelective(SecurityUser record) {
        return securityUserMapper.insertSelective(record);
    }

    
    public SecurityUser selectByPrimaryKey(Integer id) {
        return securityUserMapper.selectByPrimaryKey(id);
    }

    
    public int updateByPrimaryKeySelective(SecurityUser record) {
        return securityUserMapper.updateByPrimaryKeySelective(record);
    }

    
    public int updateByPrimaryKey(SecurityUser record) {
        return securityUserMapper.updateByPrimaryKey(record);
    }

    public SelfUserDetails selectByUserName(String username) {
        return securityUserMapper.selectByUserName(username);
    }
}
