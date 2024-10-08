package com.ivmiku.mikumq.core.manager;

import cn.hutool.core.util.ObjectUtil;
import com.ivmiku.mikumq.core.DeleteMessage;
import com.ivmiku.mikumq.core.MessageRecorder;
import com.ivmiku.mikumq.core.WaitingAck;
import com.ivmiku.mikumq.entity.Message;
import com.ivmiku.mikumq.entity.Request;
import com.ivmiku.mikumq.entity.Response;
import com.ivmiku.mikumq.response.MessageBody;
import com.ivmiku.mikumq.core.server.Server;
import lombok.Setter;
import org.smartboot.socket.transport.AioSession;
import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 处理消费者的消息发送工作
 * @author Aurora
 */
public class ConsumerManager {
    private final Server server;
    /**
     * 存在待发送信息的消费者队列
     */
    private final BlockingQueue<String> waitingQueue = new LinkedBlockingQueue<>();
    /**
     * 扫描消费者队列并进行发送的线程
     */
    private final ExecutorService processThread;
    @Setter
    private HashMap<String, String> params;
    /**
     * 阻塞的消费者队列，待有信息时主动推送给消费者
     */
    private final BlockingQueue<String> listenQueue = new LinkedBlockingQueue<>();
    private final ExecutorService listenThread;

    public ConsumerManager(Server server, HashMap<String, String> consumerConfig) {
        this.server = server;
        params = consumerConfig;
        ExecutorService scanThread = Executors.newFixedThreadPool(Integer.parseInt(params.get("scanThread")));
        processThread = Executors.newCachedThreadPool();
        for (int i=0; i<Integer.parseInt(params.get("scanThread")); i++) {
            scanThread.execute(() -> {
                while (true) {
                    try {
                        String consumerTag = waitingQueue.take();
                        processThread.execute(() -> {
                            //信息发送给消费者
                            MessageRecorder recorder = server.getItemManager().getUnreadMessage(consumerTag).removeFirst();
                            Message message = server.getItemManager().getMessage(recorder.getMessageId());
                            boolean requireAck = server.getItemManager().getQueue(recorder.getQueueName()).isAutoAck();
                            try {
                                sendToConsumer(message, consumerTag, recorder.getQueueName(), requireAck);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            if (!requireAck) {
                                server.getItemManager().deleteMessage(message.getId());
                                server.getItemManager().deleteFromQueue(recorder.getQueueName(), recorder.getMessageId());
                                notifyDelete(recorder.getQueueName(), recorder.getMessageId(), consumerTag);
                            } else {
                                server.getItemManager().waitingForAck(message.getId(), recorder.getQueueName());
                                notifyAck(recorder.getQueueName(), recorder.getMessageId(), consumerTag);
                            }
                        });

                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        listenThread = Executors.newFixedThreadPool(5);
        listenThread.execute(() -> {
            while (true) {
                try {
                    String consumerTag = listenQueue.take();
                    //消费者不在线或没有新消息则重新入队
                    if (server.getItemManager().ifHaveMessage(consumerTag) && server.getSession(consumerTag) != null) {
                        try {
                            listenQueue.put(consumerTag);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        listenThread.execute(() -> {
                            //信息发送给消费者
                            MessageRecorder recorder = server.getItemManager().getUnreadMessage(consumerTag).removeFirst();
                            Message message = server.getItemManager().getMessage(recorder.getMessageId());
                            boolean requireAck = server.getItemManager().getQueue(recorder.getQueueName()).isAutoAck();
                            try {
                                sendToConsumer(message, consumerTag, recorder.getQueueName(), requireAck);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            if (!requireAck) {
                                server.getItemManager().deleteMessage(message.getId());
                                server.getItemManager().deleteFromQueue(recorder.getQueueName(), recorder.getMessageId());
                                notifyDelete(recorder.getQueueName(), recorder.getMessageId(), consumerTag);
                            } else {
                                server.getItemManager().waitingForAck(message.getId(), recorder.getQueueName());
                                notifyAck(recorder.getQueueName(), recorder.getMessageId(), consumerTag);
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException();
                }
            }
        });
    }

    public void sendToConsumer(Message message, String tag, String queueName, boolean requireAck) throws IOException {
        AioSession session = server.getSession(tag);
        MessageBody body = new MessageBody(queueName, message, requireAck);
        byte[] data = ObjectUtil.serialize(Response.getResponse(2, body));
        WriteBuffer writeBuffer = session.writeBuffer();
        writeBuffer.writeInt(data.length);
        writeBuffer.write(data);
        writeBuffer.flush();
    }

    public void addQueue(String consumerTag){
        if (!waitingQueue.contains(consumerTag)) {
            try {
                waitingQueue.put(consumerTag);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void addListen(String consumerTag) {
        if (!listenQueue.contains(consumerTag)) {
            try {
                listenQueue.put(consumerTag);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 通知集群其他实例同步操作
     * @param queueName 队列名称
     * @param messageId 消息id
     */
    public void notifyAck(String queueName, String messageId, String consumerTag) {
        if (server.isCluster()) {
            WaitingAck waitingAck = new WaitingAck();
            waitingAck.setMessageId(messageId);
            waitingAck.setQueueName(queueName);
            waitingAck.setConsumerTag(consumerTag);
            server.getClusterManager().sendToInstances(Request.setRequest(10, waitingAck));
        }
    }

    /**
     * 通知集群其他实例同步操作
     * @param queueName 队列名称
     * @param messageId 消息id
     * @param consumerTag 消费者tag
     */
    public void notifyDelete(String queueName, String messageId, String consumerTag) {
        if (server.isCluster()) {
            DeleteMessage deleteMessage = new DeleteMessage();
            deleteMessage.setConsumerTag(consumerTag);
            deleteMessage.setMessageId(messageId);
            deleteMessage.setQueueName(queueName);
            server.getClusterManager().sendToInstances(Request.setRequest(9, deleteMessage));
        }
    }
}
