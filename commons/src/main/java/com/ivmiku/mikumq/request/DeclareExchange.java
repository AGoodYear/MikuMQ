package com.ivmiku.mikumq.request;

import com.ivmiku.mikumq.entity.ExchangeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeclareExchange implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String name;
    private ExchangeType type;
    private boolean durable;
}
