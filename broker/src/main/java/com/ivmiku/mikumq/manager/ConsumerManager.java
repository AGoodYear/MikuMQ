package com.ivmiku.mikumq.manager;

import cn.hutool.core.util.ObjectUtil;
import com.ivmiku.mikumq.entity.Message;
import com.ivmiku.mikumq.entity.Response;
import com.ivmiku.mikumq.response.MessageBody;
import com.ivmiku.mikumq.server.Server;
import lombok.Setter;
import org.smartboot.socket.transport.AioSession;
import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class ConsumerManager {
    private final Server server;
    private final BlockingQueue<String> waitingQueue = new LinkedBlockingQueue<>();
    private Thread thread;
    private ExecutorService scanThread;
    private ExecutorService processThread;
    @Setter
    private HashMap<String, String> params;

    public ConsumerManager(Server server, HashMap<String, String> consumerConfig) {
        this.server = server;
        params = consumerConfig;
        scanThread = Executors.newFixedThreadPool(Integer.parseInt(params.get("scanThread")));
        processThread = Executors.newCachedThreadPool();
        for (int i=0; i<Integer.parseInt(params.get("scanThread")); i++) {
            scanThread.execute(() -> {
                while (true) {
                    try {
                        String queueName = waitingQueue.take();
                        processThread.execute(() -> {
                            LinkedList<Message> list = server.getItemManager().getMessageList(queueName);
                            List<String> listener = server.getItemManager().getQueue(queueName).getListener();
                            Message message = list.pollFirst();
                            boolean requireAck = server.getItemManager().getQueue(queueName).isAutoAck();
                            requireAck = !requireAck;
                            for (String tag : listener) {
                                try {
                                    sendToConsumer(message, tag, queueName, requireAck);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            if (!requireAck) {
                                server.getItemManager().deleteMessage(message.getId());
                            } else {
                                server.getItemManager().waitingForAck(message.getId(), queueName);
                            }
                        });

                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
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

    public void addQueue(String queueName){
        try {
            waitingQueue.put(queueName);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
