package com.ivmiku.mikumq.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 连接异常
 * @author Aurora
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ConnectionException extends RuntimeException{
    private String message;

    public ConnectionException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
