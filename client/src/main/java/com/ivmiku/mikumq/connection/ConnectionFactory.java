package com.ivmiku.mikumq.connection;

import cn.hutool.core.util.ObjectUtil;
import com.ivmiku.mikumq.entity.Message;
import com.ivmiku.mikumq.entity.Request;
import com.ivmiku.mikumq.entity.Response;
import com.ivmiku.mikumq.exception.ConnectionException;
import com.ivmiku.mikumq.exception.ErrorResponseException;
import com.ivmiku.mikumq.request.Acknowledgement;
import com.ivmiku.mikumq.response.Confirm;
import com.ivmiku.mikumq.response.MessageBody;
import lombok.Data;
import lombok.Setter;
import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.transport.AioQuickClient;
import org.smartboot.socket.transport.AioSession;
import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 连接工厂类
 * @author Aurora
 */
@Data
public class ConnectionFactory {
    private String host;
    private int port;
    @Setter
    private com.ivmiku.mikumq.consumer.MessageProcessor messageProcessor;

    public AioQuickClient getConnection() throws IOException {
        MessageProcessor<Response> processor = (aioSession, response) -> readResponse(response, aioSession);
        AioQuickClient client = new AioQuickClient(host, port, new ResponseProtocol(), processor);
        ExecutorService executors = Executors.newFixedThreadPool(10);
        AsynchronousChannelGroup group = AsynchronousChannelGroup.withThreadPool(executors);
        try {
            client.start(group);
        } catch (IOException e) {
            throw new ConnectionException("与MikuMQ的服务器连接失败，请检查网络连接。");
        }
        return client;
    }

    public void readResponse(Response response, AioSession session) {
        //响应确认
        if (response.getType() == 1) {
            Confirm confirm = ObjectUtil.deserialize(response.getPayload());
            String msg = confirm.getMessage();
            if (!"success".equals(msg)) {
                throw new ErrorResponseException(msg);
            }
        } else if (response.getType() == 2) {
            MessageBody messageBody = ObjectUtil.deserialize(response.getPayload());
            try {
                messageProcessor.process(messageBody.getMessage());
                if (messageBody.isRequireAck()) {
                    WriteBuffer writeBuffer = session.writeBuffer();
                    Acknowledgement acknowledgement = new Acknowledgement(messageBody.getMessage().getId(), messageBody.getQueueName(), true);
                    byte[] data = ObjectUtil.serialize(Request.setRequest(5, acknowledgement));
                    writeBuffer.writeInt(data.length);
                    writeBuffer.write(data);
                    writeBuffer.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (messageBody.isRequireAck()) {
                    WriteBuffer writeBuffer = session.writeBuffer();
                    Acknowledgement acknowledgement = new Acknowledgement(messageBody.getMessage().getId(), messageBody.getQueueName(), false);
                    byte[] data = ObjectUtil.serialize(Request.setRequest(5, acknowledgement));
                    try {
                        writeBuffer.writeInt(data.length);
                        writeBuffer.write(data);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    writeBuffer.flush();
                }
            }
        }
    }
}
