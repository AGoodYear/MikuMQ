package com.ivmiku.mikumq.core;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 记录待发送的消息信息
 * @author Aurora
 */
@AllArgsConstructor
@Data
public class MessageRecorder {
    private String messageId;
    private String queueName;
}
