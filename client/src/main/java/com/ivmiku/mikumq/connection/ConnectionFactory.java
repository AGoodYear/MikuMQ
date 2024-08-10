package com.ivmiku.mikumq.connection;

import cn.hutool.core.util.ObjectUtil;
import com.ivmiku.mikumq.consumer.Consumer;
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
    @Setter
    private Consumer consumer = null;

    public AioQuickClient getConnection()  {
        MessageProcessor<Response> processor = (aioSession, response) -> readResponse(response, aioSession);
        AioQuickClient client = new AioQuickClient(host, port, new ResponseProtocol(), processor);
        ExecutorService executors = Executors.newFixedThreadPool(10);
        AsynchronousChannelGroup group;
        try {
            group = AsynchronousChannelGroup.withThreadPool(executors);
        } catch (IOException e) {
            throw new ConnectionException("创建线程池失败，请检查内存剩余");
        }
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
            if (consumer.isOnHold()) {
                consumer.setOnHold(false);
            }
            MessageBody messageBody = ObjectUtil.deserialize(response.getPayload());
            try {
                messageProcessor.process(messageBody.getMessage());
                if (messageBody.isRequireAck()) {
                    WriteBuffer writeBuffer = session.writeBuffer();
                    Acknowledgement acknowledgement = new Acknowledgement(messageBody.getMessage().getId(), messageBody.getQueueName(), consumer.getTag(), true);
                    byte[] data = ObjectUtil.serialize(Request.setRequest(5, acknowledgement));
                    writeBuffer.writeInt(data.length);
                    writeBuffer.write(data);
                    writeBuffer.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (messageBody.isRequireAck()) {
                    WriteBuffer writeBuffer = session.writeBuffer();
                    Acknowledgement acknowledgement = new Acknowledgement(messageBody.getMessage().getId(), messageBody.getQueueName(), consumer.getTag(), false);
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
        } else if (response.getType() == 3) {
            consumer.setOnHold(true);
        }
    }
}
