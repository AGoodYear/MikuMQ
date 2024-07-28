package com.ivmiku.mikumq.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 声明Binding
 * @author Aurora
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeclareBinding implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String exchangeName;
    private String queueName;
    private String bindingKey;
}
