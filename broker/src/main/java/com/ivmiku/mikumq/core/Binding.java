package com.ivmiku.mikumq.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Exchange和Queue的绑定关系
 * @author Aurora
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Binding {
    private String exchangeName;
    private String queueName;
    private String bindingKey;
}
