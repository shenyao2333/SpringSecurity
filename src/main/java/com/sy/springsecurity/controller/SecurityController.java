package com.sy.springsecurity.controller;

import com.sy.springsecurity.domain.SecurityUser;
import com.sy.springsecurity.service.SecurityUserService;
import com.sy.springsecurity.utils.RespBean;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @Author: sy
 * @DateTime: 2020.3.15 16:04
 * @Description: 认证测试
 */
@RestController
@RequestMapping("/security")
public class SecurityController {


    @Resource
    private SecurityUserService securityUserService;


    @Resource
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    /**
     * 注册信息
     * @return
     */
    @ApiOperation(value = "注册用户信息")
    @PostMapping("/register")
    public RespBean register(@RequestBody SecurityUser user){
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        securityUserService.insert(user);
        return  RespBean.success("注册成功");
    }


    @GetMapping("test")
    public RespBean test(String name){
        System.out.println(name);
        return  RespBean.success(name);
    }











}
