package com.ivmiku.mikumq.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 发送给集群其他服务器的删除消息指令
 * @author Aurora
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteMessage implements Serializable {
    private String messageId;
    private String queueName;
    private String consumerTag;
}
