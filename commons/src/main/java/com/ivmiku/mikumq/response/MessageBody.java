package com.ivmiku.mikumq.response;

import com.ivmiku.mikumq.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageBody implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String queueName;
    private Message message;
    private boolean requireAck;
}
