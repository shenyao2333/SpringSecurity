package com.sy.springsecurity.surictiy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sy.springsecurity.utils.JwtTokenUtil;
import com.sy.springsecurity.utils.RedisUtil;
import com.sy.springsecurity.utils.RespBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author: sy
 * @DateTime: 2020.3.15 20:58
 * @Description: 拦截器
 */
@Component
@Slf4j
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private SelfUserDetailsService userDetailsService;


    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        String token = JwtTokenUtil.resolveToken(httpServletRequest);
        log.info("传进来的token---->"+token);
        if (token!=null){
            Map<Object, Object> hget = redisUtil.hget(token);
            if (hget!=null){
                String username = JwtTokenUtil.getUsername(token);
                log.info("解析后的用户名-->"+username);
                SelfUserDetails userDetails = new SelfUserDetails();
                userDetails.setUserName(username);
                Object roles = hget.get("roles");
                Set objects =(Set) JSON.parseArray(roles.toString());
                System.out.println(objects);
                userDetails.setAuthorities(objects);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails,null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}
