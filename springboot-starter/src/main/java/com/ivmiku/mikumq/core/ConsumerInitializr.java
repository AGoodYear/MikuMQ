package com.ivmiku.mikumq.core;

import com.ivmiku.mikumq.connection.ConnectionFactory;
import com.ivmiku.mikumq.consumer.Consumer;
import jakarta.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConsumerInitializr implements CommandLineRunner {
    @Resource
    private MikuProperties mikuProperties;

    @Override
    public void run(String... args) {
        List<ConsumerProperties> propertiesList = ConsumerManager.propertiesList;
        if (propertiesList != null) {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(mikuProperties.getHost());
            factory.setPort(Integer.parseInt(mikuProperties.getPort()));
            factory.setReadBufferSize(mikuProperties.getReadBufferSize());
            factory.setWriteBufferCapacity(mikuProperties.getWriteBufferCapacity());
            factory.setWriteBufferSize(mikuProperties.getWriteBufferSize());
            for (ConsumerProperties properties : propertiesList) {
                Consumer consumer = new Consumer();
                factory.setConsumer(consumer);
                factory.setMessageProcessor(properties.getProcessor());
                consumer.setTag(properties.getTag());
                consumer.setConnection(factory.getConnection());
                consumer.startSession();
                for (String queue : properties.getQueueName()) {
                    consumer.subscribe(queue);
                }
                if (properties.getQueryTime() != null) {
                    consumer.setQueryDelay(properties.getQueryTime());
                } else {
                    consumer.setQueryDelay(mikuProperties.getQuerytime());
                }
                consumer.queryMessage();
                ConsumerManager.consumerMap.put(properties.getTag(), consumer);
            }
        }
    }
}
