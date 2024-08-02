package com.ivmiku.mikumq.core;

import lombok.Data;

/**
 * 记录当前信息消费情况
 * @author Aurora
 */
@Data
public class MessageStatus {
    private String messageId;
    private Integer waitingNum;
}
