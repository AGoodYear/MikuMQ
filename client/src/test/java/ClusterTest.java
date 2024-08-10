import com.ivmiku.mikumq.connection.ConnectionFactory;
import com.ivmiku.mikumq.consumer.Consumer;
import com.ivmiku.mikumq.consumer.MessageProcessor;
import com.ivmiku.mikumq.entity.Message;
import com.ivmiku.mikumq.producer.Producer;

import java.nio.charset.StandardCharsets;

public class ClusterTest {
    public static void main(String[] args) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        factory.setPort(8889);
        factory.setMessageProcessor(new MessageProcessor() {
            @Override
            public void process(Message message) {
                String msg = new String(message.getMessage());
                System.out.println(msg);
            }
        });
        Producer producer = new Producer("producer");
        producer.setConnection(factory.getConnection());
        producer.setUsername("guest");
        producer.setPassword("guest");
        producer.startSession();
        producer.declareQueue("pulltest", true, true);
        Message message = Message.initMessage("pulltest", "hello world".getBytes(StandardCharsets.UTF_8));
        producer.declareBinding("pulltest", "test", "12313123s");
        Consumer consumer = new Consumer();
        consumer.setTag("consumer");
        factory.setPort(8887);
        consumer.setConnection(factory.getConnection());
        consumer.setUsername("guest");
        consumer.setPassword("guest");
        consumer.startSession();
        consumer.subscribe("pulltest");
        producer.sendMessage("test", Message.initMessage("pulltest", "ouneideshou".getBytes(StandardCharsets.UTF_8)));
        consumer.queryOne();
        consumer.queryOne();
        consumer.queryOne();
        consumer.queryOne();
        consumer.queryOne();
        consumer.queryOne();
        consumer.queryOne();
        consumer.queryOne();
        consumer.queryOne();
    }
}
