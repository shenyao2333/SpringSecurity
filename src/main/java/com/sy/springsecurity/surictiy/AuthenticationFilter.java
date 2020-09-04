package com.sy.springsecurity.surictiy;


import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sy.springsecurity.utils.JwtTokenUtil;

import com.sy.springsecurity.utils.RespBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author: sy
 * @DateTime: 2020.3.15 20:58
 * @Description: 用户拦截器
 */
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter  {


    private AuthenticationManager authenticationManager;


    /**
     *  设置登录路径
     * @param authenticationManager
     */
    public AuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        super.setFilterProcessesUrl("/auth/login");
    }



    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        // 从输入流中获取到登录的信息

        SelfUserDetails loginUser = null;
        try {
            loginUser = new ObjectMapper().readValue(request.getInputStream(), SelfUserDetails.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginUser.getUsername(), loginUser.getPassword(),loginUser.getAuthorities()));

    }

    /**
     * 登录成功
     * @param request
     * @param response
     * @param chain
     * @param authResult
     * @throws IOException
     * @throws ServletException
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        SelfUserDetails jwtUser = (SelfUserDetails) authResult.getPrincipal();
        String token = JwtTokenUtil.createToken(jwtUser.getUsername(),jwtUser.getRoles());
        // 返回创建成功的token
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().print(JSONObject.toJSONString(RespBean.success(token)));
    }

    /**
     *  这是验证失败时候调用的方法
     * @param request
     * @param response
     * @param failed
     * @throws IOException
     * @throws ServletException
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().print(JSONObject.toJSONString(RespBean.fail(20001,"帐号或密码错误！")));
    }



}
