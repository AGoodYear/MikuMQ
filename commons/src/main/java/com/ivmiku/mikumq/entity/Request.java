package com.ivmiku.mikumq.entity;

import cn.hutool.core.util.ObjectUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Request implements Serializable {
    @Serial
    private static final long serialVersionUID = 2L;
    private int type;
    private byte[] payload;

    public static Request setRequest(int type, Object payload) {
        Request request = new Request();
        request.setType(type);
        request.setPayload(ObjectUtil.serialize(payload));
        return request;
    }
}
