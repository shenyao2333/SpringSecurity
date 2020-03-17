package com.sy.springsecurity.surictiy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sy.springsecurity.utils.JwtTokenUtil;
import com.sy.springsecurity.utils.RedisUtil;
import com.sy.springsecurity.utils.RespBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @Author: sy
 * @DateTime: 2020.3.15 17:14
 * @Description: 登录异常处理
 */

@Component
public class SelfAuthenticationEntryPoint implements AuthenticationEntryPoint, AuthenticationSuccessHandler , AuthenticationFailureHandler, LogoutSuccessHandler , AccessDeniedHandler {


    @Resource
    private RedisUtil redisUtil;


    /**
     * 未登录
     * @param httpServletRequest
     * @param httpServletResponse
     * @param e
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
        httpServletResponse.setContentType("application/json;charset=UTF-8");
        httpServletResponse.setContentType("text/javascript;charset=utf-8");
        httpServletResponse.getWriter().print(JSONObject.toJSONString(RespBean.fail(40001,"请先登录")));
    }


    /**
     * 登录后返回对象
     * @param httpServletRequest
     * @param httpServletResponse
     * @param authentication
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
        SelfUserDetails userDetails = (SelfUserDetails) authentication.getPrincipal();

        String token = JwtTokenUtil.createToken(userDetails.getUsername());
        Map<String, Object> map = new HashMap<>();
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        map.put("roles",JSON.toJSONString(authorities));
        redisUtil.hmset(token,map,3600*3);
        Map<String, String> restMap = new HashMap<String, String>();
        restMap.put("token",token);
        restMap.put("tokenHead","Bearer");
        restMap.put("expiresTime",3600*3+"");
        httpServletResponse.setContentType("application/json;charset=UTF-8");
        httpServletResponse.getWriter().write(JSON.toJSONString(RespBean.success(restMap,"登录成功")));

    }

    /**
     * 登录失败
     * @param httpServletRequest
     * @param httpServletResponse
     * @param e
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
        httpServletResponse.setContentType("application/json;charset=UTF-8");
        httpServletResponse.getWriter().write(JSON.toJSONString(RespBean.fail(2000,"帐号或密码错误")));
    }


    /**
     * 注销登录
     * @param httpServletRequest
     * @param httpServletResponse
     * @param authentication
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
        String s = JwtTokenUtil.resolveToken(httpServletRequest);
        redisUtil.del(s);
        httpServletResponse.getWriter().write(JSON.toJSONString(RespBean.success("注销成功")));
    }

    /**
     * 无权访问
     * @param httpServletRequest
     * @param httpServletResponse
     * @param e
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException, ServletException {
        httpServletResponse.setContentType("application/json;charset=UTF-8");
        httpServletResponse.getWriter().write(JSON.toJSONString(RespBean.fail(RespBean.Code.POWER)));
    }
}
