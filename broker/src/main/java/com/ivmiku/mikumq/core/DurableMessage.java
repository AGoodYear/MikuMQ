package com.ivmiku.mikumq.core;

import lombok.Data;

/**
 * 持久化消息信息记录类
 * @author Aurora
 */
@Data
public class DurableMessage {
    private String id;
    private String start;
    private String queue;
}
