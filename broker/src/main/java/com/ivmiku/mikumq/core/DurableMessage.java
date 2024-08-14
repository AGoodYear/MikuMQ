package com.ivmiku.mikumq.core;

import lombok.Data;

@Data
public class DurableMessage {
    private String id;
    private String start;
    private String queue;
}
