package com.ivmiku.mikumq.request;

import com.ivmiku.mikumq.entity.Message;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class AddMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String exchangeName;
    private Message message;
}
