package com.ivmiku.mikumq.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 发送确认消息
 * @author Aurora
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Acknowledgement implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String messageId;
    private String queueName;
    private String consumerTag;
    private boolean success;
}
