package com.sy.springsecurity.surictiy;

import com.alibaba.fastjson.JSON;
import com.sy.springsecurity.domain.SecurityUser;
import com.sy.springsecurity.utils.GrabException;
import com.sy.springsecurity.utils.JwtTokenUtil;
import com.sy.springsecurity.utils.RespBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author sy
 * @date Created in 2020.9.2 21:55
 * @description 鉴权拦截
 */
public class AuthorizationFilter extends BasicAuthenticationFilter {



    public AuthorizationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }





    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {
        String token = JwtTokenUtil.resolveToken(request);
        // 如果请求头中没有Authorization信息则直接放行，然后交给SpringSecurity去处理
        if (token == null) {
            chain.doFilter(request, response);
            return;
        }
        // 如果请求头中有token，则进行解析，告诉SpringSecurity有哪些角色信息。然后交给它去处理
        try {
            SecurityContextHolder.getContext().setAuthentication(getAuthentication(token));
        }catch (GrabException e){
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JSON.toJSONString(RespBean.fail(40002,"token已失效")));
            response.getWriter().flush();
            return;
        }
        super.doFilterInternal(request, response, chain);

    }

    /**
     * 使用jwt解析token
     * @param token
     * @return
     */
    private UsernamePasswordAuthenticationToken getAuthentication(String token) {
        boolean b = JwtTokenUtil.validateToken(token);
        if (b){
            throw new GrabException(40001,"token超时了");
        }
        SelfUserDetails userInfo = JwtTokenUtil.getUserInfo(token);
        if (userInfo.getUsername() != null){
            return new UsernamePasswordAuthenticationToken(userInfo.getUsername(), null, userInfo.getAuthoritiesByRoles());
        }
        return null;
    }


}
