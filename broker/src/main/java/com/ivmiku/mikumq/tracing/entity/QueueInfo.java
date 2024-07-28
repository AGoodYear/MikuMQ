package com.ivmiku.mikumq.tracing.entity;

import lombok.Data;

@Data
public class QueueInfo {
    private String name;
    private Integer messageNum;
    private Integer listenerNum;
    private boolean durable;
    private boolean autoAck;
}
