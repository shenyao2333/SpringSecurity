package com.sy.springsecurity.controller;

import com.sy.springsecurity.domain.SecurityUser;
import com.sy.springsecurity.service.SecurityUserService;
import com.sy.springsecurity.utils.RespBean;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
     * 注册信息 ，这个接口需要登录，并且需要ADMIN权限
     * @return
     */
    @ApiOperation(value = "注册用户信息")
    @PostMapping("/register")
    public RespBean register(@RequestBody SecurityUser user){
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        securityUserService.insert(user);
        return  RespBean.success("注册成功");
    }


    /**
     *
     * @param name
     * @return
     */
    @GetMapping("test")
    public RespBean test(String name){
        return  RespBean.success(name);
    }


    /**
     * 这个接口开放
     * @param user
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "添加用户")
    public RespBean add(@RequestBody SecurityUser user){
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        securityUserService.insert(user);
        return  RespBean.success("添加成功后");
    }




    /**
     * 测试使用注解做权限
     * @return
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/testPoser")
    public RespBean testPoser(){
        return  RespBean.success("ADMIN角色");
    }






}
