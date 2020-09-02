package com.sy.springsecurity.surictiy;

import com.sy.springsecurity.utils.JwtTokenUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author sy
 * @date Created in 2020.9.2 21:55
 * @description 鉴权拦截
 */

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {



    public JWTAuthorizationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }





    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {

        String token = JwtTokenUtil.resolveToken(request);
        // 如果请求头中没有Authorization信息则直接放行了
        if (token == null) {
            chain.doFilter(request, response);
            return;
        }
        // 如果请求头中有token，则进行解析，并且设置认证信息
        SecurityContextHolder.getContext().setAuthentication(getAuthentication(token));
        super.doFilterInternal(request, response, chain);
    }

    /**
     *  这里从token中获取用户信息并新建一个token
     * @param token
     * @return
     */
    private UsernamePasswordAuthenticationToken getAuthentication(String token) {
        String username = JwtTokenUtil.getUsername(token);
        if (username != null){
            return new UsernamePasswordAuthenticationToken(username, null,JwtTokenUtil.getRoles(token) );
        }
        return null;
    }


}
