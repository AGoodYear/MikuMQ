package com.ivmiku.mikumq.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 发送给集群其他服务器的等待确认指令
 * @author Aurora
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WaitingAck implements Serializable {
    private String messageId;
    private String queueName;
    private String consumerTag;
}
