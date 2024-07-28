package com.ivmiku.mikumq.consumer;

import com.ivmiku.mikumq.entity.Message;

/**
 * 消费者自定义的消息处理方法
 * @author Aurora
 */
public interface MessageProcessor {
    /**
     * 用户自定义的消息消费方法
     * @param message 从服务端收到的消息
     */
    void process(Message message);
}
