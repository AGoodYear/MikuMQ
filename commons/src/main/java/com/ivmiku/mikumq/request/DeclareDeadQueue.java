package com.ivmiku.mikumq.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class DeclareDeadQueue implements Serializable {
    private String queueName;
}
