package com.sy.springsecurity.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ：sy
 * @date ：Created in 2020.3.12 21:44
 * @description:
 */
@Controller
@RestController
public class PageController {


    /**
     * 首页
     */
    @RequestMapping("/")
    public String index (){
        return "index" ;
    }
    /**
     * 登录页
     */
    @RequestMapping("/userLogin")
    public String loginPage (){

        return "pages/login" ;
    }


    /**
     * page1 下页面
     */
    @RequestMapping("/page1/{pageName}")
    public String onePage (@PathVariable("pageName") String pageName){
        return "pages/page1/"+pageName ;
    }
    /**
     * page2 下页面
     */
    @RequestMapping("/page2/{pageName}")
    public String twoPage (@PathVariable("pageName") String pageName){
        return "pages/page2/"+pageName ;
    }


    /**
     * page3 下页面
     */
    @PreAuthorize("hasAuthority('Root')")
    @RequestMapping("/page3/{pageName}")
    public String threePage (@PathVariable("pageName") String pageName){
        return "pages/page3/"+pageName ;
    }



}
