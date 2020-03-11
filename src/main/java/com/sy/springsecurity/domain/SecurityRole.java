package com.sy.springsecurity.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.Data;

@ApiModel(value="com-sy-springsecurity-domain-SecurityRole")
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