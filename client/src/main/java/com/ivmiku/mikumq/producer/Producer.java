package com.ivmiku.mikumq.producer;

import cn.hutool.core.util.ObjectUtil;
import com.ivmiku.mikumq.entity.ExchangeType;
import com.ivmiku.mikumq.entity.Message;
import com.ivmiku.mikumq.entity.Request;
import com.ivmiku.mikumq.exception.ConnectionException;
import com.ivmiku.mikumq.request.*;
import lombok.Getter;
import lombok.Setter;
import org.smartboot.socket.transport.AioQuickClient;
import org.smartboot.socket.transport.AioSession;
import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;

/**
 * 生产者客户端
 * @author Aurora
 */
public class Producer {
    private AioQuickClient client;
    @Setter
    private String tag;
    @Getter
    private AioSession aliveSession;
    @Setter
    private String username;
    @Setter
    private String password;
    public Producer(String tag) {
        this.tag = tag;
    }

    /**
     * 设置连接
     * @param client AioQuickClient
     */
    public void setConnection(AioQuickClient client) {
        this.client = client;
    }

    /**
     * 关闭连接
     */
    public void shutdown() {
        client.shutdown();
    }

    /**
     * 向服务器注册自己
     */
    public void startSession() {
        aliveSession = client.getSession();
        Register register = new Register();
        register.setTag(tag);
        if (username != null && password != null) {
            register.setPassword(password);
            register.setUsername(username);
        }
        sendRequest(Request.setRequest(1, register));
    }

    public void closeSession() {
        if (aliveSession != null) {
            aliveSession.close();
        }
    }

    /**
     * 通用发送请求函数
     * @param request 请求
     */
    public void sendRequest(Request request) {
        if (aliveSession != null) {
            try  {
                byte[] data = ObjectUtil.serialize(request);
                WriteBuffer writeBuffer = aliveSession.writeBuffer();
                writeBuffer.writeInt(data.length);
                writeBuffer.write(data);
                writeBuffer.flush();
            } catch (IOException e) {
                aliveSession.close();
                startSession();
                throw new RuntimeException(e);
            }
        } else {
            throw new ConnectionException("请先创建Session再与服务器进行通信。");
        }
    }

    /**
     * 创建交换机
     * @param exchangeName 交换机名字
     * @param type 类型
     * @param durable 是否持久化
     */
    public void declareExchange(String exchangeName, ExchangeType type, boolean durable) {
        DeclareExchange declareExchange = new DeclareExchange(exchangeName, type, durable);
        sendRequest(Request.setRequest(4, declareExchange));
    }

    /**
     * 创建绑定
     * @param queueName 队列名称
     * @param exchangeName 交换机名称
     * @param bindingKey key
     */
    public void declareBinding(String queueName, String exchangeName, String bindingKey) {
        DeclareBinding declareBinding = new DeclareBinding(exchangeName, queueName, bindingKey);
        sendRequest(Request.setRequest(6, declareBinding));
    }

    /**
     * 创建 队列
     * @param queueName 队列名称
     * @param autoAck 是否自动ack
     * @param durable 是否持久化
     */
    public void declareQueue(String queueName, boolean autoAck, boolean durable) {
        DeclareQueue declareQueue = new DeclareQueue(queueName, autoAck, durable);
        sendRequest(Request.setRequest(7, declareQueue));
    }

    /**
     * 发送消息
     * @param exchangeName 交换机名称
     * @param message 消息
     */
    public void sendMessage(String exchangeName, Message message) {
        AddMessage addMessage = new AddMessage();
        addMessage.setMessage(message);
        addMessage.setExchangeName(exchangeName);
        sendRequest(Request.setRequest(3, addMessage));
    }
}
