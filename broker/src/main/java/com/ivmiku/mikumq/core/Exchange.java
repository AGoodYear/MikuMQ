package com.ivmiku.mikumq.core;

import com.ivmiku.mikumq.entity.ExchangeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MQ核心-交换机
 * @author Aurora
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Exchange {
    private String name;
    private ExchangeType type = ExchangeType.DIRECT;
    private boolean durable = false;
}
