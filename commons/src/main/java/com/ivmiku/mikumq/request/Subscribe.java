package com.ivmiku.mikumq.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class Subscribe implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String tag;
    private String queueName;
}
