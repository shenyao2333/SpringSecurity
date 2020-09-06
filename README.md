## SpringBoot+SpringSecurity+JWT实现权限控制

### 一、简介

        SpringSecurity的核心功能主要是认证、授权和攻击防护。这里主要是以SpringBoot整合SpringSecurity的一个练习，练习前就尽量过一遍它的知识嘛，这样更加有利于理解为什么这么做。

具体其他的介绍可以参考[SpringSecuity中文文档](https://www.springcloud.cc/spring-security-zhcn.html#what-is-acegi-security) 。

以及JWT的一些介绍[SpringSecuity中文文档](https://jwt.io/introduction) 。

这次demo主要实现的功能

- 用户拦截
- 基于JWTtoken的单点登录
- 统一响应信息
- 注解操作权限

### 二、整合计划

​     SpringSecurity主要使用了AOP思想，利用拦截器实现对接口的保护，与业务耦合度相对较低。先从搭建项目工程开始，先整理一下步骤，再具体完成每一个步骤的任务。

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

#### 1、maven的核心依赖

```xml

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

```java
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

```java
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

```java
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

JWT你应该了解过，它不是一个普通的token，主要使用在前端端分离的项目中。利用HMAC或者使用RSA或ECDSA的公钥/私钥对进行签名，提供了各方之间的保密，当使用公钥/私钥对签署令牌时，签名还证明只有持有私钥的一方是签署私钥的一方（这句话是我抄来的，有点绕口，但是不是错句哦）。所以我们可以将用户或者业务信息声明在字符串中。

使用时主要的是创建、解密和判断是否失效。

- 创建

  ```
  再创建token的时候，就把需要的用户名和角色写在token信息里。角色在做权限判断的时候需要用到。
  ```

- 解密

  ```
  这里只是我突方便，再解密用户名和角色后直接用一个对象来处理。因为这个对象可以交给SpringSercuity去管理。所以我直接这样用，具体看个人喜欢怎么改。
  ```

- token是否有效

  ```
  在创建token的时候就把有效期写入token里
  ```

  

```java
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


	/**
     * 从token中解密数据后直接返回对象
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

```



### 四、SpringSecurity配置使用

#### 前面是准备工作，从这里开始是SpringSecurity的具体配置了，是重点！

#### 1、实现security用户对象

这里有个重点：**getAuthoritiesByRoles() 方法。** 返回对象是List<GrantedAuthority>，这个是SpringSecurity角色对象。我们要把登录人的角色信息告诉它，它才能判断哪些接口是要什么角色才能请求，怎么告诉它呢，就是用这个对象了。

> 这里有个需要注意的是角色名，我们数据里存的角色名一般不带 ' ROLE_ '前缀，但 SpringSecurity里的角色名需要一个这样的前缀，而我在这里处理这里的角色名了。

```java
package com.sy.springsecurity.surictiy;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @Author: sy
 * @DateTime: 2020.3.15 16:20
 * @Description: 实现security用户对象
 */
@Data
public class SelfUserDetails  implements UserDetails, Serializable {

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
     * 角色列表
     */
    private List roles;

    private Set<? extends GrantedAuthority> authorities;

    /**
     * 这里加上ROLE_，因为交给SpringSecurity的角色用需要ROLE_前缀，
     * 但我们数据库存的role信息往往不带ROLE_ 前缀。
     * @return
     */
    public Collection<? extends GrantedAuthority> getAuthoritiesByRoles() {
        List<GrantedAuthority> auths = new ArrayList<>();
        List roles = getRoles();
        for(Object role : roles) {
            auths.add(new SimpleGrantedAuthority("ROLE_"+role.toString()));
        }
        return auths;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    /**
     * 密码
     * @return
     */
    @Override
    public String getPassword() {
        return this.password;
    }

    /**
     * 重点
     * @return
     */
    @Override
    public String getUsername() {
        return this.userName;
    }

    /**
     * 账号是否未过期，这个是告诉SpringSecuirty是否过期的，但我们用JWT的失效时间，所以这里写死true
     * @return
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 用户是否锁定
     * @return
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }


    /**
     * 用户凭证是否未过期。未过期
     * @return
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

```





#### 2、登录拦截器。

UsernamePasswordAuthenticationFilter是AbstractAuthenticationProcessingFilter针对使用**用户名和密码进行身份验证而定制化的一个过滤器**。重写attemptAuthentication身份验证入口方法。**从POST的HttpRequest中获取对应的参数字段，并传递给AuthenticationManager进行身份验证。**

- 另外在这里也就可以设置登录的路径，默认是 /login
- 定义登录成功后返回的数据
- 定义登录失败返回的数据

```java
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
                                            Authentication authResult) throws                IOException,  ServletException {
        //从流获取参数                                  
        SelfUserDetails jwtUser = (SelfUserDetails) authResult.getPrincipal();
        String token =        JwtTokenUtil.createToken(jwtUser.getUsername(),jwtUser.getRoles());
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

#### 3、登录

上面是登录拦截器，那么拦截后到哪呢？就是这里

```java
package com.sy.springsecurity.surictiy;

import com.sy.springsecurity.service.SecurityUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author: sy
 * @DateTime: 2020.3.15 16:18
 * @Description: 用户认证、角色分配
 */
@Component
@Slf4j
public class SelfUserDetailsService implements UserDetailsService  {


    @Resource
    private SecurityUserService securityUserService;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SelfUserDetails user = securityUserService.selectByUserName(username);
        log.info("查询出用户---》"+user);
        return user;
    }

}

```

**这里的sql语句只是将用户和角色查询出来。** 是一个一对多关系，具体是表结构，来决定查询语句。

````xml
    <resultMap id="userMap"  type="com.sy.springsecurity.surictiy.SelfUserDetails">
       <id column="id" jdbcType="INTEGER" property="id" />
       <result column="user_name" jdbcType="VARCHAR" property="userName" />
       <result column="password" jdbcType="VARCHAR" property="password" />
       <collection   property="roles" ofType="java.lang.String" javaType="java.util.List">
         <result column="enname" />
    </collection>
  </resultMap>

  
  
  <select id="selectByUserName" resultMap="userMap">
    SELECT  u.id, user_name, `password` , enname  FROM user_info u
    left join auth_user_role ur on ur.user_id = u.id
    left join auth_role r on ur.role_id = r.id
      where u.user_name = #{username}
  </select>
````





#### 4、鉴权拦截

```java
package com.sy.springsecurity.surictiy;

import com.alibaba.fastjson.JSON;
import com.sy.springsecurity.utils.GrabException;
import com.sy.springsecurity.utils.JwtTokenUtil;
import com.sy.springsecurity.utils.RespBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

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
            response.getWriter().write(JSON.toJSONString(RespBean.fail(40001,"token已失效")));
            response.getWriter().flush();
            return;
        }
        super.doFilterInternal(request, response, chain);

    }

    /**
     * 这里就使用到jwt解析token了。GrabException是一个自定义异常对象
     * @param token
     * @return
     */
    private UsernamePasswordAuthenticationToken getAuthentication(String token) {
        boolean b = JwtTokenUtil.validateToken(token);
        if (b){
            throw new GrabException(40001,"token超时了");
        }
        SelfUserDetails userInfo = JwtTokenUtil.getUserInfo(token);
        
        //解析后用户名和角色信息
        if (userInfo.getUsername() != null){
            return new UsernamePasswordAuthenticationToken(userInfo.getUsername(), null, userInfo.getAuthoritiesByRoles());
        }
        return null;
    }

}

```

#### 5、配置无权限和未登录的统一返回信息，这里可以忽略，但实际使用是必须的

```
package com.sy.springsecurity.surictiy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sy.springsecurity.utils.RespBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author: sy
 * @DateTime: 2020.3.15 17:14
 * @Description: 登录异常处理
 */


public class SelfAuthenticationEntryPoint implements AuthenticationEntryPoint  , AccessDeniedHandler {
    


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
        httpServletResponse.getWriter().print(JSONObject.toJSONString(RespBean.fail(40001,"请先登录")));
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
        httpServletResponse.getWriter().write(JSON.toJSONString(RespBean.fail(4001,"暂无权限！")));
    }
}

```



#### 6、SpringSecurity配置

```
package com.sy.springsecurity.surictiy;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.annotation.Resource;

/**
 * @author ：sy
 * @date ：Created in 2020.3.12 21:31
 * @description:SpringSecurity配置类
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter  {


    @Resource
    private SelfUserDetailsService userDetailsService;


    /**
     * 拦截策略
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //关闭跨域保护
        http.cors().and().csrf().disable()
                // 无状态模式，不需要session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                /**
                 * 设置指定一个url需要ADMIN权限，只要是测试用
                 * 这里指定的ADMIN，但交给SpringSecurity的时候需要ROLE_ADMIN
                 */
                .authorizeRequests()
                .antMatchers( "/security/register").hasRole("ADMIN")

                /**
                 * 其他的都要登录后才能访问
                 */
                .anyRequest().authenticated()
                .and()

                /**
                 * 添加一个拦截器
                 */
                .addFilter(new AuthenticationFilter(authenticationManager()))
                .addFilter(new AuthorizationFilter(authenticationManager()))

                //添加无权限和未登录的处理时的处理
                .exceptionHandling().authenticationEntryPoint(new SelfAuthenticationEntryPoint())
                .accessDeniedHandler(new SelfAuthenticationEntryPoint());



    }
	
	/**
	 *密码匹配方式
	 */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }


    /**
     * 配置忽略的URL
     * @param web
     * @throws Exception
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("security/add");
    }


    /**
     * 拦截后的操作
     * @param auth
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(new BCryptPasswordEncoder());
    }


}

```



#### 7、到这里就实际就差不多了，现在需要写几个接口测试一下

```java
package com.sy.springsecurity.controller;

import com.sy.springsecurity.domain.SecurityUser;
import com.sy.springsecurity.service.SecurityUserService;
import com.sy.springsecurity.utils.RespBean;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @Author: sy
 * @DateTime: 2020.3.15 16:04
 * @Description: 认证测试
 */
@RestController
@RequestMapping("/security")
public class SecurityController {

    @Resource
    private SecurityUserService securityUserService;


    @Resource
    private BCryptPasswordEncoder bCryptPasswordEncoder;


    /**
     * 注册信息 ，这个接口需要登录，并且需要ADMIN权限
     * @return
     */
    @ApiOperation(value = "注册用户信息")
    @PostMapping("/register")
    public RespBean register(@RequestBody SecurityUser user){
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        securityUserService.insert(user);
        return  RespBean.success("注册成功");
    }


    /**
     *
     * @param name
     * @return
     */
    @GetMapping("test")
    public RespBean test(String name){
        return  RespBean.success(name);
    }


    /**
     * 这个接口开放
     * @param user
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "添加用户")
    public RespBean add(@RequestBody SecurityUser user){
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        securityUserService.insert(user);
        return  RespBean.success("添加成功后");
    }
    
    /**
     * 测试使用注解做权限
     * @return
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/testPoser")
    public RespBean testPoser(){
        return  RespBean.success("ADMIN角色");
    }

}

```



### 四、最终完了，实际做了什么回顾一下

首先是登录拦截，实现登录，把用户信息查询出来加密到token中。然后是鉴权，把拦截下来的路径解密token后，把用户信息转化成SpringSecurity里的对象，最后交给它来做权限判断。最后配置一下统一返回对象以及一些拦截策略。

最后祝各位兄弟早日成就自己的架构师之梦！如果有缘再相见的话，希望你我都是架。。都牛逼！
