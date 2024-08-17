package com.ivmiku.mikumq.tracing.entity;

import lombok.Data;

/**
 * 队列相关信息
 * @author Aurora
 */
@Data
public class QueueInfo {
    private String name;
    private Integer messageNum;
    private Integer listenerNum;
    private boolean durable;
    private boolean autoAck;
}
