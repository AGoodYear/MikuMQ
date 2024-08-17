package com.ivmiku.mikumq.tracing;

import lombok.Getter;

/**
 * ResultCode枚举类
 * @author Aurora
 */

@Getter
public enum ResultCode {
    /**
     * 预设的返回状态
     */
    OK(200, "success"),
    ERROR(-1, "error");

    /**
     * 返回的code
     */
    private int code;
    /**
     * 返回的信息
     */
    private  String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
