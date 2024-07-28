import cn.hutool.core.util.ObjectUtil;
import com.ivmiku.mikumq.connection.ConnectionFactory;
import com.ivmiku.mikumq.connection.ResponseProtocol;
import com.ivmiku.mikumq.entity.Message;
import com.ivmiku.mikumq.entity.Request;
import com.ivmiku.mikumq.entity.Response;
import com.ivmiku.mikumq.request.AddMessage;
import com.ivmiku.mikumq.request.Subscribe;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.transport.AioQuickClient;
import org.smartboot.socket.transport.AioSession;
import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;
import java.net.SocketOption;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectionTest {

    @Test
    public void connect() throws IOException, InterruptedException {
        MessageProcessor<Response> processor = (aioSession, response) -> {

        };
        AioQuickClient client = new AioQuickClient("127.0.0.1", 8888, new ResponseProtocol(), processor);
        client.start();
        AioSession session = client.getSession();
        System.out.println("连接成功！");
        WriteBuffer writeBuffer = session.writeBuffer();
        Request request = Request.setRequest(1, "null");
        writeBuffer.writeAndFlush(ObjectUtil.serialize(request));
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        ConnectionTest connectionTest = new ConnectionTest();
        connectionTest.connect();
    }
}

