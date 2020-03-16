package com.sy.springsecurity.surictiy;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.annotation.Resource;

/**
 * @author ：sy
 * @date ：Created in 2020.3.12 21:31
 * @description:
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {


    @Resource
    private SelfUserDetailsService userDetailsService;

    @Resource
    private SelfAuthenticationEntryPoint selfAuthenticationEntryPoint;

    @Resource
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    /**
     * 拦截策略
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //去掉csrf(跨域请求)
        http.csrf().disable()
                //toke、session,使用swt
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .httpBasic().authenticationEntryPoint(selfAuthenticationEntryPoint)
                .and()
                .authorizeRequests()//定义哪些URL需要被保护、哪些不需要被保护
                .antMatchers("/security/register").hasAnyRole("ADMIN")
                .and()
                .authorizeRequests()
                //任何请求,登录后可以访问
                .anyRequest()
                // RBAC 动态 url 认证
                .access("@rbacauthorityservice.hasPermission(request,authentication)")
                .and()



                //开启登录, 定义当需要用户登录时候，转到的登录页面
                .formLogin()
        //        .loginPage("/test/login.html")
        //        .loginProcessingUrl("/login")
                .successHandler(selfAuthenticationEntryPoint)//登录成功
                .failureHandler(selfAuthenticationEntryPoint)//登录失败
                .permitAll()
                .and()
                .logout()//默认注销行为为logout
                .logoutUrl("/logout")
                .logoutSuccessHandler(selfAuthenticationEntryPoint)
                .permitAll();
        http.rememberMe().rememberMeParameter("remember-me")
                .userDetailsService(userDetailsService).tokenValiditySeconds(1000);

        http.exceptionHandling().accessDeniedHandler(selfAuthenticationEntryPoint);// 无权访问 JSON 格式的数据
        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        http.authorizeRequests()
                .antMatchers( "/resources/**", "/swagger-ui.html", "/swagger-resources/**", "/v2/api-docs")
                .permitAll().anyRequest().authenticated();
    }


    /**
     * 配置忽略的URL
     * @param web
     * @throws Exception
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/userLogin");
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
