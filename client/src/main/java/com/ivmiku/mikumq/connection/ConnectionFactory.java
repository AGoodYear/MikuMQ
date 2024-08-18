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

/**
 * 连接工厂类
 * @author Aurora
 */
@Data
public class ConnectionFactory {
    private String host;
    private int readBufferSize = 1024;
    private int writeBufferSize = 1024;
    private int writeBufferCapacity = 2;
    private int port;
    /**
     * 定义消息处理方法
     */
    @Setter
    private com.ivmiku.mikumq.consumer.MessageProcessor messageProcessor;
    /**
     * 记录当前连接将被赋予哪个消费者（生产者不用填写）
     */
    @Setter
    private Consumer consumer = null;

    /**
     * 创建链接
     * @return 创建的连接（AioQuickClient）
     */
    public AioQuickClient getConnection()  {
        MessageProcessor<Response> processor = (aioSession, response) -> readResponse(response, aioSession);
        AioQuickClient client = new AioQuickClient(host, port, new ResponseProtocol(), processor);
        client.setReadBufferSize(readBufferSize);
        client.setWriteBuffer(writeBufferSize, writeBufferCapacity);
        try {
            client.start();
        } catch (IOException e) {
            throw new ConnectionException("与MikuMQ的服务器连接失败，请检查网络连接。");
        }
        return client;
    }

    /**
     * 读取服务器的响应
     * @param response 服务器发回的响应
     * @param session 当前会话
     */
    public void readResponse(Response response, AioSession session) {
        //响应确认
        switch (response.getType()) {
            case 1 -> {
                Confirm confirm = ObjectUtil.deserialize(response.getPayload());
                String msg = confirm.getMessage();
                if (!"success".equals(msg)) {
                    throw new ErrorResponseException(msg);
                }
                //读取信息
            }
            case 2 -> {
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
                //挂起请求
            }
            case 3 -> consumer.setOnHold(true);

            //心跳信息
            case 4 -> {
                WriteBuffer buffer = session.writeBuffer();
                byte[] data = ObjectUtil.serialize(Request.setRequest(12, null));
                try {
                    buffer.writeInt(data.length);
                    buffer.write(data);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                buffer.flush();
            }
            default -> {

            }
        }
    }
}
