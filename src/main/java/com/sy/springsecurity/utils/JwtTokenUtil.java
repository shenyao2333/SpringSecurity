package com.sy.springsecurity.utils;


import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import java.util.*;

import com.alibaba.fastjson.JSONObject;
import com.sy.springsecurity.domain.SecurityUser;
import com.sy.springsecurity.surictiy.SelfUserDetails;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;

/**
 * @Author: sy
 * @DateTime: 2020.3.15 20:08
 * @Description: JWTtoken生成工具
 */
public class JwtTokenUtil {

    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";


    private static String secretKey = "123123";


    private static long validityInMilliseconds = 3600000L*3;


    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }


    public  static String createToken( String userName ,List roles) {

        HashMap<String,Object> map = new HashMap<>();
        map.put("roles",roles);
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);
        return Jwts.builder()
                .setClaims(map)
                .setIssuedAt(now)
                .setExpiration(validity)
                .setSubject(userName)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }



    public static SelfUserDetails getUserInfo(String token) {
        Claims body = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
        String subject = body.getSubject();
        List roles = (List)body.get("roles");
        SelfUserDetails selfUserDetails = new SelfUserDetails();
        selfUserDetails.setUserName(subject);
        selfUserDetails.setRoles(roles);
        return selfUserDetails;
    }


    public static String resolveToken(HttpServletRequest req) {
        String bearerToken = req.getHeader(TOKEN_HEADER);
        if (bearerToken != null && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private  static Claims getTokenBody(String token){
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    }


    /**
     * 是否超时
     * @param token
     * @return
     */
    public static boolean validateToken(String token) {
        try {
            return getTokenBody(token).getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }


}
