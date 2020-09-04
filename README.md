## SpringBoot+SpringSecurity+JWT实现权限控制

### 一、简介

​        SpringSecurity的核心功能主要是认证、授权和攻击防护。这里主要是以SpringBoot整合SpringSecurity的一个练习，练习尽量过一遍它的知识，这样更有利于整合过程遇到的问题。

具体其他的介绍可以参考其[SpringSecuity中文文档](https://www.springcloud.cc/spring-security-zhcn.html#what-is-acegi-security) 。

具体说明可以看 **这里**

### 二、计划

SpringSecurity的功能主要使用了AOP思想，用拦截器实现对我们接口的保护，耦合度相对较低。先从搭建项目工程开始，先整理一下步骤，再具体完成每一个步骤的任务。

- 1、maven环境
- 2、创建实体，并且和一些增删改查。
- 3、用户登录信息拦截器操作
- 4、用户权限拦截器
- 5、实现登录逻辑，创建token
- 6、SpringSecurity配置（为什么放在后面，因为他要依赖3-5步骤）
- 7、统一处理认证和鉴权的异常
- 8、测试
- 9、回头理一遍思路



### 三、准备工作

#### 1、pom核心依赖

```

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <dependency>
			<groupId>org.mybatis.spring.boot</groupId>
			<artifactId>mybatis-spring-boot-starter</artifactId>
			<version>2.1.2</version>
		</dependency>

		<dependency>
			<groupId>mysql</groupId>
			<artifactId>mysql-connector-java</artifactId>
			<scope>runtime</scope>
		</dependency>
		
		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<optional>true</optional>
		</dependency>
		
		<dependency>
			<groupId>io.jsonwebtoken</groupId>
			<artifactId>jjwt</artifactId>
			<version>0.9.1</version>
		</dependency>
```

#### 2、用户实体类

```
package com.sy.springsecurity.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.List;

import lombok.Data;


@ApiModel(value="SecurityUser")
@Data
public class SecurityUser implements Serializable  {
    /**
     * 主键id
     */
    @ApiModelProperty(value="主键id")
    private Integer id;

    /**
     * 用户名
     */
    @ApiModelProperty(value="用户名")
    private String userName;

    /**
     * 密码
     */
    @ApiModelProperty(value="密码")
    private String password;

    /**
     * 年龄
     */
    @ApiModelProperty(value="年龄")
    private Integer age;

    /**
     * 性别
     */
    @ApiModelProperty(value="性别")
    private String sex;


    private List<SecurityRole> roles;


    private static final long serialVersionUID = 1L;

}

```

#### 3、角色实体类

```
package com.sy.springsecurity.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.Data;

@ApiModel(value="SecurityRole")
@Data
public class SecurityRole implements Serializable {
    /**
    * 主键id
    */
    @ApiModelProperty(value="主键id")
    private Integer id;

    /**
    * 角色名
    */
    @ApiModelProperty(value="角色名")
    private String roleName;

    /**
    * 用户id
    */
    @ApiModelProperty(value="用户id")
    private String userId;

    private static final long serialVersionUID = 1L;
}
```





#### 4、创建统一的返回对象。（可忽略）

```
package com.sy.springsecurity.utils;

import lombok.Data;

import java.io.Serializable;

/**
 * @author sy
 * Date: 2019/11/30 16:18
 * @Description 返回对象
 */
@Data
public class RespBean<T> implements Serializable {
    private static final long serialVersionUID = 3468352004150968551L;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 状态
     */
    private boolean status;

    /**
     * 消息
     */
    private String message;

    /**
     * 返回对象
     */
    private T data;
    
}

```

#### 5、JWT生成Token工具类

```
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

```





#### 4、前面是准备工作，从这里开始是SpringSecurity的具体配置了，是重点！登录拦截器。

UsernamePasswordAuthenticationFilter是AbstractAuthenticationProcessingFilter针对使用**用户名和密码进行身份验证而定制化的一个过滤器**。重写attemptAuthentication身份验证入口方法。**从POST的HttpRequest中获取对应的参数字段，并传递给AuthenticationManager进行身份验证。**

```


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
                                            Authentication authResult) throws      	  		IOException,  ServletException {
        SelfUserDetails jwtUser = (SelfUserDetails) authResult.getPrincipal();
        String token = 		JwtTokenUtil.createToken(jwtUser.getUsername(),jwtUser.getRoles());
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

```

