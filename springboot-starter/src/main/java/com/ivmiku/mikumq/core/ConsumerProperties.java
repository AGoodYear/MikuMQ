package com.ivmiku.mikumq.core;

import com.ivmiku.mikumq.consumer.MessageProcessor;
import lombok.Data;

@Data
public class ConsumerProperties {
    private String tag;
    private String[] queueName;
    private MessageProcessor processor;
    private Long queryTime;
    private int readBufferSize = 1024;
    private int writeBufferSize = 1024;
    private int writeBufferCapacity = 2;
}
