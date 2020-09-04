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

    public RespBean() {
        super();
    }

    public RespBean(Integer code) {
        super();
        this.code = code;
    }

    public RespBean(Integer code, String message) {
        super();
        this.code = code;
        this.message = message;
    }

    public RespBean(Integer code, Throwable throwable) {
        super();
        this.code = code;
        this.message = throwable.getMessage();
    }

    public RespBean(Integer code, T data) {
        super();
        this.code = code;
        this.data = data;
    }

    public RespBean(Integer code, String message, T data) {
        super();
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public RespBean(Integer code, String message, T data, boolean status) {
        super();
        this.code = code;
        this.message = message;
        this.data = data;
        this.status=status;
    }



    public RespBean(Integer code, boolean status, String message, T data) {
        super();
        this.code = code;
        this.message = message;
        this.data = data;
        this.status=status;
    }


    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((data == null) ? 0 : data.hashCode());
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((code == null) ? 0 : code.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RespBean<?> other = (RespBean<?>) obj;
        if (data == null) {
            if (other.data != null) {
                return false;
            }
        } else if (!data.equals(other.data)) {
            return false;
        }
        if (message == null) {
            if (other.message != null) {
                return false;
            }
        } else if (!message.equals(other.message)) {
            return false;
        }
        if (code == null) {
            if (other.code != null) {
                return false;
            }
        } else if (!code.equals(other.code)) {
            return false;
        }
        return true;
    }

    public static RespBean<Object> success() {
        return new RespBean<>(200,true, "处理成功",null);
    }

    public static <T>  RespBean success(T data) {
        return new RespBean(200,true, "处理成功",data);
    }

    public static <T>  RespBean success(T data, String msg) {
        return new RespBean<Object>(200,true, msg,data);
    }

    public static RespBean<Object> fail(){
        return new RespBean<>(500,false,"处理失败",null);
    }


    public static  RespBean<Object> fail(String message){
        return new RespBean<>(500,false,message,null);
    }


    public static RespBean<Object> fail(Integer code, String message){
        return new RespBean<>(code,false,message,null);
    }



}
