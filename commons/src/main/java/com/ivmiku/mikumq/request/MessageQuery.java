package com.ivmiku.mikumq.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 客户端向服务器的问询
 * @author Aurora
 */
@Data
public class MessageQuery implements Serializable {
    private String tag;
}
