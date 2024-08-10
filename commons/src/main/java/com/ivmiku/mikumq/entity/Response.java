package com.ivmiku.mikumq.entity;

import cn.hutool.core.util.ObjectUtil;
import com.ivmiku.mikumq.response.Confirm;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private int type;
    private byte[] payload;

    public static Response success() {
        Response response = new Response();
        response.setType(1);
        response.setPayload(ObjectUtil.serialize(new Confirm("success")));
        return response;
    }

    public static Response error(String message) {
        Response response = new Response();
        response.setType(1);
        response.setPayload(ObjectUtil.serialize(new Confirm(message)));
        return response;
    }

    public static Response getResponse(int type, Object payload) {
        Response response = new Response();
        response.setType(type);
        if (payload != null) {
            response.setPayload(ObjectUtil.serialize(payload));
        }
        return response;
    }
}
