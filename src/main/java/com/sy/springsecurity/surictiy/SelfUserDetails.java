package com.sy.springsecurity.surictiy;

import com.sy.springsecurity.domain.SecurityRole;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
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
    private List<SecurityRole> roles;

    private Set<? extends GrantedAuthority> authorities;


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
     *是否过期
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
     * 用户凭证是否未过期
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
