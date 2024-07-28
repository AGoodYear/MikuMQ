package com.ivmiku.mikumq.tracing.entity;

import lombok.Data;

@Data
public class ExchangeInfo {
    private String name;
    private String type;
    private Integer bindingNum;
    private boolean durable;
}
