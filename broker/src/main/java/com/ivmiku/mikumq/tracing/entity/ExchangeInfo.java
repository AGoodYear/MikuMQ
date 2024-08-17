package com.ivmiku.mikumq.tracing.entity;

import lombok.Data;

/**
 * 交换机的相关信息
 * @author Aurora
 */
@Data
public class ExchangeInfo {
    private String name;
    private String type;
    private Integer bindingNum;
    private boolean durable;
}
