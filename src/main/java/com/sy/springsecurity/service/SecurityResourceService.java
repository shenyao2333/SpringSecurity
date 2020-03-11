package com.sy.springsecurity.service;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import com.sy.springsecurity.mapper.SecurityResourceMapper;
import com.sy.springsecurity.domain.SecurityResource;
@Service
public class SecurityResourceService{

    @Resource
    private SecurityResourceMapper securityResourceMapper;

    
    public int deleteByPrimaryKey(Integer id) {
        return securityResourceMapper.deleteByPrimaryKey(id);
    }

    
    public int insert(SecurityResource record) {
        return securityResourceMapper.insert(record);
    }

    
    public int insertSelective(SecurityResource record) {
        return securityResourceMapper.insertSelective(record);
    }

    
    public SecurityResource selectByPrimaryKey(Integer id) {
        return securityResourceMapper.selectByPrimaryKey(id);
    }

    
    public int updateByPrimaryKeySelective(SecurityResource record) {
        return securityResourceMapper.updateByPrimaryKeySelective(record);
    }

    
    public int updateByPrimaryKey(SecurityResource record) {
        return securityResourceMapper.updateByPrimaryKey(record);
    }

}
