import com.ivmiku.mikumq.connection.ConnectionFactory;
import com.ivmiku.mikumq.consumer.Consumer;
import com.ivmiku.mikumq.entity.ExchangeType;
import com.ivmiku.mikumq.entity.Message;
import com.ivmiku.mikumq.producer.Producer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AckTest {
    public static void main(String[] args) throws IOException {
        Consumer consumer = new Consumer();
        consumer.setTag("consumer");
        Producer producer = new Producer("producer");
        ConnectionFactory factory = new ConnectionFactory();
        factory.setPort(8888);
        factory.setHost("localhost");
        producer.setConnection(factory.getConnection());
        producer.startSession();
        factory.setMessageProcessor(message -> {
            String msg = new String(message.getMessage());
            System.out.println(msg);
        });
        factory.setConsumer(consumer);
        consumer.setConnection(factory.getConnection());
        consumer.startSession();
        Message message = Message.initMessage("queue2", "hello world".getBytes(StandardCharsets.UTF_8));
        producer.declareExchange("test", ExchangeType.DIRECT, false);
        producer.declareQueue("queue2", false, false);
        producer.declareBinding("queue2", "test", "12313123");
        consumer.subscribe("queue2");
        producer.sendMessage("test", message);
    }
}
