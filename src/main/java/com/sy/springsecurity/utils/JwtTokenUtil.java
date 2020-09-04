package com.sy.springsecurity.utils;


import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import java.util.*;

import com.sy.springsecurity.surictiy.SelfUserDetails;
import io.jsonwebtoken.*;


/**
 * @Author: sy
 * @DateTime: 2020.3.15 20:08
 * @Description: JWTtoken生成工具
 */
public class JwtTokenUtil {

    /**
     * 报文头定义
     */
    public static final String TOKEN_HEADER = "Authorization";

    /**
     * 根据JWT定义规范，代表着请求头定义的schema。文档：https://jwt.io/introduction/
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * JWT密钥
     */
    private static String secretKey = "123123";

    /**
     * 有效期定义
     */
    private static long validityInMilliseconds = 3600000L*3;


    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }


    /**
     * 使用用户名和角色列表生成的一个token。具体可以更改
     * @param userName
     * @param roles
     * @return
     */
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


    /**
     * 从token中解密数据
     * @param token
     * @return
     */
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
