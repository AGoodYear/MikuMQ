package com.ivmiku.mikumq.connection;

import cn.hutool.core.util.ObjectUtil;
import com.ivmiku.mikumq.entity.Request;
import com.ivmiku.mikumq.entity.Response;
import org.smartboot.socket.Protocol;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;

/**
 * 定义通信协议
 * @author Aurora
 */
public class ResponseProtocol implements Protocol<Response> {

    @Override
    public Response decode(ByteBuffer readBuffer, AioSession session) {
        int remaining = readBuffer.remaining();
        if (remaining < Integer.BYTES) {
            return null;
        }
        readBuffer.mark();
        int length = readBuffer.getInt();
        if (readBuffer.remaining() < length) {
            readBuffer.reset();
            return null;
        }
        byte[] b = new byte[length];
        readBuffer.get(b);
        readBuffer.mark();
        return ObjectUtil.deserialize(b);
    }
}
