package com.ivmiku.mikumq.consumer;

import cn.hutool.core.util.ObjectUtil;
import com.ivmiku.mikumq.entity.Message;
import com.ivmiku.mikumq.entity.Request;
import com.ivmiku.mikumq.exception.ConnectionException;
import com.ivmiku.mikumq.request.Acknowledgement;
import com.ivmiku.mikumq.request.MessageQuery;
import com.ivmiku.mikumq.request.Register;
import com.ivmiku.mikumq.request.Subscribe;
import lombok.Getter;
import lombok.Setter;
import org.smartboot.socket.transport.AioQuickClient;
import org.smartboot.socket.transport.AioSession;
import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 消费者客户端
 * @author Aurora
 */
public class Consumer {
    private AioQuickClient client;
    @Setter
    @Getter
    private String tag;
    private AioSession aliveSession;
    @Setter
    private String username;
    @Setter
    private String password;
    @Setter
    private Integer maxMsgSize = 10;
    @Setter
    private Long queryDelay = 500L;
    private List<Message> messageList = new LinkedList<>();
    private ScheduledFuture<?> runnableFuture;
    @Setter
    @Getter
    private boolean onHold;

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
            WriteBuffer writeBuffer = aliveSession.writeBuffer();
            try {
                byte[] data = ObjectUtil.serialize(request);
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

    public void ackMessage(String messageId, String queueName) {
        Acknowledgement acknowledgement = new Acknowledgement();
        acknowledgement.setMessageId(messageId);
        acknowledgement.setQueueName(queueName);
        Request request = Request.setRequest(5, acknowledgement);
        sendRequest(request);
    }

    /**
     * 订阅队列
     * @param queueName 队列名字
     */
    public void subscribe(String queueName) {
        Subscribe subscribe = new Subscribe();
        subscribe.setTag(tag);
        subscribe.setQueueName(queueName);
        sendRequest(Request.setRequest(2, subscribe));
    }

    /**
     * 开始接受信息（PUSH）
     */
    public void queryMessage() {
        ScheduledExecutorService service = new ScheduledThreadPoolExecutor(1);
        runnableFuture = service.scheduleWithFixedDelay(() -> {
                if (messageList.size()<=maxMsgSize) {
                    if (!isOnHold()) {
                        MessageQuery query = new MessageQuery();
                        query.setTag(tag);
                        sendRequest(Request.setRequest(8, query));
                    }
                }
        }, 0L, queryDelay, TimeUnit.MILLISECONDS);
    }

    /**
     * 停止接收信息
     */
    public void stopQuery() {
        runnableFuture.cancel(true);
    }

    /**
     * 请求一个信息
     */
    public void queryOne() {
        MessageQuery query = new MessageQuery();
        query.setTag(tag);
        sendRequest(Request.setRequest(8, query));
    }
}
