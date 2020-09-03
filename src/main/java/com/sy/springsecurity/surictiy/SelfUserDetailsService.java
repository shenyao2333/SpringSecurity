package com.sy.springsecurity.surictiy;

import com.sy.springsecurity.service.SecurityUserService;
import com.sy.springsecurity.utils.GrabException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author: sy
 * @DateTime: 2020.3.15 16:18
 * @Description: 用户认证、角色分配
 */
@Component
@Slf4j
public class SelfUserDetailsService implements UserDetailsService  {


    @Resource
    private SecurityUserService securityUserService;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SelfUserDetails user = securityUserService.selectByUserName(username);
        log.info("查询出用户---》"+user);
        return user;
    }




}
