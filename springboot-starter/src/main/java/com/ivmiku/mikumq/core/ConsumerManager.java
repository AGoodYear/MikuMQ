package com.ivmiku.mikumq.core;

import com.ivmiku.mikumq.connection.ConnectionFactory;
import com.ivmiku.mikumq.consumer.Consumer;
import com.ivmiku.mikumq.consumer.MessageProcessor;
import com.ivmiku.mikumq.entity.Message;
import jakarta.annotation.Resource;
import org.smartboot.socket.transport.AioQuickClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConsumerManager {
    public static Map<String, Consumer> consumerMap = new ConcurrentHashMap<>();
    public static Map<String, List<Message>> consumerList = new ConcurrentHashMap<>();

    @Resource
    private MikuProperties properties;

    public void initConsumer(List<String> list) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(properties.getHost());
        factory.setPort(Integer.parseInt(properties.getPort()));
        factory.setMessageProcessor(message -> {

        });
        for (String tag : list) {

            Consumer consumer = new Consumer();
        }
    }
}
