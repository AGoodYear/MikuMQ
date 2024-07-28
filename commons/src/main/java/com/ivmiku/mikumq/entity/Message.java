package com.ivmiku.mikumq.entity;

import cn.hutool.core.lang.generator.SnowflakeGenerator;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 消息
 * @author Aurora
 */
@Data
public class Message implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private byte[] message;
    private String id;
    private String routingKey;
    private int retryTime = 0;

    /**
     * 创建新的消息
     * @param routingKey RoutingKey
     * @param body 消息体
     * @return 创建好的消息
     */
    public static Message initMessage(String routingKey, byte[] body) {
        SnowflakeGenerator generator = new SnowflakeGenerator();
        Message message = new Message();
        message.setId(String.valueOf(generator.next()));
        message.setRoutingKey(routingKey);
        message.setMessage(body);
        return message;
    }
}
