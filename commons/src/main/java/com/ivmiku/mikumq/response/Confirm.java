package com.ivmiku.mikumq.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 发送给客户端的确认消息
 * @author Aurora
 */
@Data
@AllArgsConstructor
public class Confirm implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String message;
}
