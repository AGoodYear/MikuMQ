import com.ivmiku.mikumq.connection.ConnectionFactory;
import com.ivmiku.mikumq.consumer.MessageProcessor;
import com.ivmiku.mikumq.entity.Message;
import com.ivmiku.mikumq.producer.Producer;

import java.io.IOException;

public class LoginTest {
    public static void main(String[] args) throws IOException {
        Producer producer = new Producer("logintest");
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        factory.setPort(8888);
        factory.setMessageProcessor(new MessageProcessor() {
            @Override
            public void process(Message message) {
                System.out.println(new String(message.getMessage()));
            }
        });
        producer.setConnection(factory.getConnection());
        producer.setUsername("guest");
        producer.setPassword("guest");
        producer.startSession();
    }
}
