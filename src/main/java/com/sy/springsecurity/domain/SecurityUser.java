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
