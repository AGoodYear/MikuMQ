package com.ivmiku.mikumq.core;

import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * MQ核心-消息队列
 * @author Aurora
 */
@Data
public class MessageQueue {
    private String name;
    private List<String> listener = new ArrayList<>();
    private boolean autoAck;
    private boolean durable;
}
