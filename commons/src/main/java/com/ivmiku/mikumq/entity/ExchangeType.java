package com.ivmiku.mikumq.entity;

/**
 * 交换机模式枚举类
 * @author Aurora
 */

public enum ExchangeType {
    /**
     * 交换机的信息投递模式
     */
    DIRECT(0),
    FANOUT(1),
    TOPIC(2);


    private int code;

    ExchangeType(int i) {
        code = i;
    }
}
