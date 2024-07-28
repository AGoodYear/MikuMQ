package com.ivmiku.mikumq.entity;

public enum ExchangeType {
    DIRECT(0),
    FANOUT(1);

    private int code;

    ExchangeType(int i) {
        code = i;
    }
}
