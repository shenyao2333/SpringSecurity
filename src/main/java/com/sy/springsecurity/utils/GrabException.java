package com.sy.springsecurity.utils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: sy
 * @DateTime: 2020.3.15 16:33
 * @Description: 自定义异常类
 */
@Slf4j
@Data
public class GrabException extends RuntimeException {


    private Integer code;
    private String message;

    public GrabException( Integer code,String message) {
        super(message);
        this.code = code;
        this.message=message;
    }








}
