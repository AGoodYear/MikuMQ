package com.ivmiku.mikumq.tracing;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Api返回的结果封装类
 * @author Aurora
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result implements Serializable {
    @JSONField(ordinal = 1)
    private int code;
    @JSONField(ordinal = 2)
    private String message;
    @JSONField(ordinal = 3)
    private Object data = null;

    public Result(ResultCode resultCode) {
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    public Result(ResultCode resultCode, Object data) {
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
        this.data = data;
    }

    public static Result ok(){
        return new Result(200, "success", null);
    }

    public static Result error(){
        return new Result(-1, "error", null);
    }

    public static Result ok(Object data){
        return new Result(200, "success", data);
    }

    public static Result ok(String message){
        return new Result(200, message, null);
    }

    public static Result error(String message){
        return new Result(-1, message, null);
    }


}
