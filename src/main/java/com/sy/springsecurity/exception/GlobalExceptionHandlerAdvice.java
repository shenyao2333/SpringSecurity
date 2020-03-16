package com.sy.springsecurity.exception;

import com.sy.springsecurity.utils.GrabException;
import com.sy.springsecurity.utils.RespBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author: sy
 * @DateTime: 2020.3.15 16:42
 * @Description: 处理全局异常返回自定义对象
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandlerAdvice {


    @ExceptionHandler(value = {GrabException.class})
    public RespBean grabException(GrabException ex){
        return RespBean.fail(ex.getCode()==null?-1:ex.getCode(),ex.getMessage());
    }


    @ExceptionHandler
    public RespBean exceptionHandler(Exception e){
        log.error(e.getMessage());
        //开发中先放开，快速定位到错误
        e.printStackTrace();
        return RespBean.fail(-1,"内部错误");
    }






}
