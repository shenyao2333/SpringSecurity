package com.sy.springsecurity.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.Data;

@ApiModel(value="SecurityResource")
@Data
public class SecurityResource implements Serializable {
    /**
    * 
    */
    @ApiModelProperty(value="")
    private Integer id;

    /**
    * 角色id
    */
    @ApiModelProperty(value="角色id")
    private String roleId;

    /**
    * 资源路径
    */
    @ApiModelProperty(value="资源路径")
    private String url;

    private static final long serialVersionUID = 1L;
}