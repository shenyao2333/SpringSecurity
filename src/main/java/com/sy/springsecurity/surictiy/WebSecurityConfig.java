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
 * @description:
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
        http.cors().and().csrf().disable()
                // 无状态模式，不需要session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                /**
                 * 设置指定一个url需要ADMIN权限
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
                 * 添加一个挂炉
                 */
                .addFilter(new AuthenticationFilter(authenticationManager()))
                .addFilter(new AuthorizationFilter(authenticationManager()))

                //添加无权限和未登录的处理时的处理
                .exceptionHandling().authenticationEntryPoint(new SelfAuthenticationEntryPoint())
                .accessDeniedHandler(new SelfAuthenticationEntryPoint());



    }

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
