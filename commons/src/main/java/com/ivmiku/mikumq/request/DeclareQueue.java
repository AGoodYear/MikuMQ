package com.ivmiku.mikumq.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeclareQueue implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String name;
    private boolean autoAck;
    private boolean durable;
}
