import cn.hutool.core.util.ObjectUtil;
import com.ivmiku.mikumq.connection.ConnectionFactory;
import com.ivmiku.mikumq.consumer.Consumer;
import com.ivmiku.mikumq.consumer.MessageProcessor;
import com.ivmiku.mikumq.entity.ExchangeType;
import com.ivmiku.mikumq.entity.Message;
import com.ivmiku.mikumq.producer.Producer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class SendAndReceive {
    public static void main(String[] args) throws IOException {
        Consumer consumer = new Consumer();
        consumer.setTag("consumer");
        Producer producer = new Producer("producer");
        ConnectionFactory factory = new ConnectionFactory();
        factory.setPort(8888);
        factory.setHost("localhost");
        producer.setConnection(factory.getConnection());
        producer.startSession();
        List<String> list = new LinkedList<>();
        factory.setMessageProcessor(message -> {
            String msg = new String(message.getMessage());
            System.out.println(msg);
            list.add(msg);
        });
        consumer.setConnection(factory.getConnection());
        consumer.startSession();
        Message message = Message.initMessage("queue1", "hello world".getBytes(StandardCharsets.UTF_8));
        producer.declareExchange("test", ExchangeType.DIRECT, false);
        producer.declareQueue("queue1", true, false);
        producer.declareBinding("queue1", "test", "1231312");
        consumer.subscribe("queue1");
        producer.sendMessage("test", message);
    }
}
