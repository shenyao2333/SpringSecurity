package com.sy.springsecurity.surictiy;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.sy.springsecurity.utils.JwtTokenUtil;



import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
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
public class JwtAuthenticationTokenFilter extends UsernamePasswordAuthenticationFilter  {


    private AuthenticationManager authenticationManager;



    public JwtAuthenticationTokenFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        super.setFilterProcessesUrl("/auth/login");
    }



    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        // 从输入流中获取到登录的信息
        try {
            SelfUserDetails loginUser = new ObjectMapper().readValue(request.getInputStream(), SelfUserDetails.class);
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginUser.getUsername(), loginUser.getPassword(),loginUser.getAuthorities()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    // 成功验证后调用的方法
    // 如果验证成功，就生成token并返回
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {

        SelfUserDetails jwtUser = (SelfUserDetails) authResult.getPrincipal();
        String token = JwtTokenUtil.createToken(jwtUser.getUsername(),jwtUser.getRoles());
        // 返回创建成功的token
        // 但是这里创建的token只是单纯的token
        // 按照jwt的规定，最后请求的格式应该是 `Bearer token`
        response.setHeader("Authorization","Bearer "+token);
    }

    // 这是验证失败时候调用的方法
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.getWriter().write("authentication failed, reason: " + failed.getMessage());
    }



}
