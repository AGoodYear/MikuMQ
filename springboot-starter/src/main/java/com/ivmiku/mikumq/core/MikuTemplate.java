package com.ivmiku.mikumq.core;

import cn.hutool.core.lang.generator.SnowflakeGenerator;
import cn.hutool.core.util.ObjectUtil;
import com.ivmiku.mikumq.connection.ConnectionFactory;
import com.ivmiku.mikumq.consumer.MessageProcessor;
import com.ivmiku.mikumq.entity.ExchangeType;
import com.ivmiku.mikumq.entity.Message;
import com.ivmiku.mikumq.producer.Producer;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MikuTemplate {
    private Map<String, String> params;
    private Producer producer;

    public void send(String exchangeName, String routingKey, byte[] payload) {
        Message message = Message.initMessage(routingKey, payload);
        producer.sendMessage(exchangeName, message);
    }

    public void convertAndSend(String exchangeName, String routingKey, Object payload) {
        Message message = Message.initMessage(routingKey, ObjectUtil.serialize(payload));
        producer.sendMessage(exchangeName, message);
    }

    public void init(Map<String, String> params) {
        this.params = params;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(params.get("host"));
        factory.setPort(Integer.parseInt(params.get("port")));
        factory.setMessageProcessor(new MessageProcessor() {
            @Override
            public void process(Message message) {

            }
        });
        SnowflakeGenerator generator = new SnowflakeGenerator();
        producer = new Producer("spring@" + generator.next());
        producer.setConnection(factory.getConnection());
        producer.setUsername(params.get("username"));
        producer.setPassword(params.get("password"));
        producer.startSession();
    }

    public void declareQueue(String queueName, boolean autoAck, boolean isDurable) {
        producer.declareQueue(queueName, autoAck, isDurable);
    }

    public void declareBinding(String bindingKey, String queueName, String exchangeName) {
        producer.declareBinding(queueName, exchangeName, bindingKey);
    }

    public void declareExchange(String exchangeName, ExchangeType type, boolean isDurable) {
        producer.declareExchange(exchangeName, type, isDurable);
    }
}
