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

/**
 * 生产者Template
 * @author Aurora
 */
@Component
public class MikuTemplate {
    private Map<String, String> params;
    private Producer producer;

    /**
     * 发送字节信息
     * @param exchangeName 交换机名字
     * @param routingKey key
     * @param payload 负载
     */
    public void send(String exchangeName, String routingKey, byte[] payload) {
        Message message = Message.initMessage(routingKey, payload);
        producer.sendMessage(exchangeName, message);
    }

    /**
     * 将对象转换为字节数组后发送
     * @param exchangeName 交换机名字
     * @param routingKey key
     * @param payload 负载
     */
    public void convertAndSend(String exchangeName, String routingKey, Object payload) {
        Message message = Message.initMessage(routingKey, ObjectUtil.serialize(payload));
        producer.sendMessage(exchangeName, message);
    }

    public void init(Map<String, String> params) {
        this.params = params;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(params.get("host"));
        factory.setPort(Integer.parseInt(params.get("port")));
        factory.setWriteBufferSize(Integer.parseInt(params.get("writeBufferSize")));
        factory.setWriteBufferCapacity(Integer.parseInt(params.get("writebufferCapacity")));
        factory.setReadBufferSize(Integer.parseInt(params.get("readBufferSize")));
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

    /**
     * 创建 队列
     * @param queueName 队列名称
     * @param autoAck 是否自动ack
     * @param isDurable 是否持久化
     */
    public void declareQueue(String queueName, boolean autoAck, boolean isDurable) {
        producer.declareQueue(queueName, autoAck, isDurable);
    }

    /**
     * 创建绑定
     * @param queueName 队列名称
     * @param exchangeName 交换机名称
     * @param bindingKey key
     */
    public void declareBinding(String bindingKey, String queueName, String exchangeName) {
        producer.declareBinding(queueName, exchangeName, bindingKey);
    }

    /**
     * 创建交换机
     * @param exchangeName 交换机名字
     * @param type 类型
     * @param isDurable 是否持久化
     */
    public void declareExchange(String exchangeName, ExchangeType type, boolean isDurable) {
        producer.declareExchange(exchangeName, type, isDurable);
    }
}
