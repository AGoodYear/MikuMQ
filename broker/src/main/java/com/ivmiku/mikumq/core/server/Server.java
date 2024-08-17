package com.ivmiku.mikumq.core.server;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReUtil;
import com.ivmiku.mikumq.core.*;
import com.ivmiku.mikumq.dao.DatabaseInitializr;
import com.ivmiku.mikumq.dao.MessageDao;
import com.ivmiku.mikumq.dao.QueueDao;
import com.ivmiku.mikumq.entity.ExchangeType;
import com.ivmiku.mikumq.entity.Message;
import com.ivmiku.mikumq.entity.Request;
import com.ivmiku.mikumq.entity.Response;
import com.ivmiku.mikumq.core.manager.ClusterManager;
import com.ivmiku.mikumq.core.manager.ConsumerManager;
import com.ivmiku.mikumq.core.manager.ItemManager;
import com.ivmiku.mikumq.request.*;
import com.ivmiku.mikumq.response.Confirm;
import com.ivmiku.mikumq.response.MessageBody;
import com.ivmiku.mikumq.tracing.ApiController;
import com.ivmiku.mikumq.utils.ConfigUtil;
import com.ivmiku.mikumq.utils.DurableUtil;
import com.ivmiku.mikumq.utils.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.smartboot.socket.StateMachineEnum;
import org.smartboot.socket.extension.plugins.HeartPlugin;
import org.smartboot.socket.extension.processor.AbstractMessageProcessor;
import org.smartboot.socket.transport.AioQuickServer;
import org.smartboot.socket.transport.AioSession;
import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Broker服务器
 * @author Aurora
 */
@Slf4j
public class Server {
    /**
     * 存储session会话
     */
    public static ConcurrentHashMap<String, AioSession> sessionMap = new ConcurrentHashMap<>();
    /**
     * 存储来自集群其他实例的session
     */
    public static ConcurrentHashMap<String, AioSession> clusterSessionMap = new ConcurrentHashMap<>();
    public static ItemManager itemManager = new ItemManager();
    private static ClusterManager clusterManager = null;

    public static ConsumerManager consumerManager;

    public static HashMap<String, String> params;

    public static ApiController apiController;

    public boolean loginRequired;

    public void start() throws IOException {
        //初始化服务器
        init();
        //定义消息处理器
        AbstractMessageProcessor<Request> processor = new AbstractMessageProcessor<>() {
            @Override
            public void process0(AioSession aioSession, Request request) {
                //向服务器注册则将session存到内存中
                if (request.getType() == 1) {
                    Register register = ObjectUtil.deserialize(request.getPayload());
                    boolean logged;
                    //是否开启鉴权
                    if (loginRequired) {
                        logged = PasswordUtil.login(register.getUsername(), register.getPassword());
                    } else {
                        logged = true;
                    }
                    //判断session是否来自集群
                    if (logged && !register.isCluster()) {
                        sessionMap.put(register.getTag(), aioSession);
                    } else {
                        clusterSessionMap.put(register.getTag(), aioSession);
                    }
                }
                Response response = response(request);
                if (response != null && !clusterSessionMap.contains(aioSession)) {
                    sendResponse(aioSession, response);
                }
                //如果是新增队列、消息等操作，则转发集群其他实例
                List<Integer> sendingList = Arrays.asList(1, 8, 9, 10, 11);
                if (clusterManager != null && !sendingList.contains(request.getType()) && !clusterSessionMap.contains(aioSession)) {
                    clusterManager.sendToInstances(request);
                }
            }

            @Override
            public void stateEvent0(AioSession session, StateMachineEnum stateMachineEnum, Throwable throwable) {
                if (stateMachineEnum == StateMachineEnum.DECODE_EXCEPTION || stateMachineEnum == StateMachineEnum.PROCESS_EXCEPTION) {
                    log.error("解码时出现了异常");
                    throwable.printStackTrace();
                }
                if (stateMachineEnum == StateMachineEnum.SESSION_CLOSED) {
                    //会话关闭，移除session
                    for (Iterator<Map.Entry<String, AioSession>> it = sessionMap.entrySet().iterator(); it.hasNext(); ) {
                        Map.Entry<String, AioSession> item = it.next();
                        if (item.getValue() == session) {
                            it.remove();
                            break;
                        }
                    }
                }
            }
        };
        //添加心跳插件
        processor.addPlugin(new HeartPlugin<>(Integer.parseInt(params.get("heart.rate")), Integer.parseInt(params.get("heart.timeout")), TimeUnit.SECONDS) {
            @Override
            public void sendHeartRequest(AioSession aioSession) throws IOException {
                WriteBuffer buffer = aioSession.writeBuffer();
                byte[] data = ObjectUtil.serialize(Response.getResponse(4, null));
                buffer.writeInt(data.length);
                buffer.write(data);
                buffer.flush();
            }

            @Override
            public boolean isHeartMessage(AioSession aioSession, Request request) {
                return request.getType() == 12;
            }

        });
        AioQuickServer server = new AioQuickServer(params.get("host") ,Integer.parseInt(params.get("port")), new RequestProtocol(), processor);
        server.setLowMemory(true);
        //设置线程数量
        server.setThreadNum(Integer.parseInt(params.get("threadNum")));
        server.setReadBufferSize(Integer.parseInt(params.get("readBufferSize")));
        server.setWriteBuffer(Integer.parseInt(params.get("writeBufferSize")), Integer.parseInt(params.get("writeBufferCapacity")));
        server.start();
        log.info("MikuMQ Broker Started");
    }

    /**
     * 向会话发送响应
     * @param session 当前会话
     * @param response 响应
     */
    public static void sendResponse(AioSession session, Response response) {
        try {
            WriteBuffer outputStream = session.writeBuffer();
            byte[] data = ObjectUtil.serialize(response);
            outputStream.writeInt(data.length);
            outputStream.write(data);
            outputStream.flush();
            if (response.getType()==1 && "登陆失败！请检查相关配置".equals(Arrays.toString(response.getPayload()))) {
                session.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据请求获取响应
     * @param request 客户端的请求
     * @return 响应
     */
    public Response response(Request request) {
        switch (request.getType()) {
            //登录
            case 1 -> {
                Register register = ObjectUtil.deserialize(request.getPayload());
                if (sessionMap.containsKey(register.getTag())) {

                } else {
                    return Response.error("登陆失败！请检查相关配置");
                }
            }
            //消费者订阅
            case 2 -> {
                Subscribe subscribe = ObjectUtil.deserialize(request.getPayload());
                if (itemManager.getQueue(subscribe.getQueueName()) == null) {
                    return Response.error("要订阅的队列不存在！");
                }
                List<String> listener = itemManager.getQueue(subscribe.getQueueName()).getListener();
                if (!listener.contains(subscribe.getTag())) {
                    listener.add(subscribe.getTag());
                    QueueDao.insertListener(subscribe.getQueueName(), subscribe.getTag());
                }
            }
            //生产者投递消息
            case 3 -> {
                AddMessage addMessage = ObjectUtil.deserialize(request.getPayload());
                itemManager.insertMessage(addMessage.getMessage());
                ExchangeType type = itemManager.getExchange(addMessage.getExchangeName()).getType();
                if (type == ExchangeType.DIRECT) {
                    Exchange exchange = itemManager.getExchange(addMessage.getExchangeName());
                    if (itemManager.getBinding(exchange.getName()).containsKey(addMessage.getMessage().getRoutingKey())) {
                        itemManager.sendMessage(addMessage.getMessage().getRoutingKey(), addMessage.getMessage());
                    } else {
                        return Response.error("试图写入的队列不存在！");
                    }
                } else if (type == ExchangeType.FANOUT) {
                    ConcurrentHashMap<String, Binding> bindingMap = itemManager.getBinding(addMessage.getExchangeName());
                    for (Binding binding : bindingMap.values()) {
                        itemManager.sendMessage(binding.getQueueName(), addMessage.getMessage());
                    }
                } else if (type == ExchangeType.TOPIC) {
                    ConcurrentHashMap<String, Binding> bindingMap = itemManager.getBinding(addMessage.getExchangeName());
                    String routingKey = addMessage.getMessage().getRoutingKey();
                    for (Binding binding : bindingMap.values()) {
                        if (ReUtil.isMatch(binding.getBindingKey(), routingKey)) {
                            itemManager.sendMessage(binding.getQueueName(), addMessage.getMessage());
                        }
                    }
                }
                if (addMessage.getMessage().isDurable()) {
                    DurableMessage message = DurableUtil.writeMessage(addMessage.getMessage());
                    message.setQueue(addMessage.getExchangeName());
                    MessageDao.insertMessage(message);
                }
            }
            //创建交换机
            case 4 -> {
                DeclareExchange declareExchange = ObjectUtil.deserialize(request.getPayload());
                Exchange exchange = new Exchange();
                exchange.setName(declareExchange.getName());
                exchange.setType(declareExchange.getType());
                exchange.setDurable(declareExchange.isDurable());
                itemManager.insertExchange(exchange);
            }
            //Ack
            case 5 -> {
                Acknowledgement ack = ObjectUtil.deserialize(request.getPayload());
                if (ack.isSuccess()) {
                    itemManager.ackMessage(ack.getMessageId(), ack.getQueueName(), ack.getConsumerTag());
                    System.out.println("AckOk");
                } else {
                    if (itemManager.getMessage(ack.getMessageId()).getRetryTime() < Integer.parseInt(params.get("retryTime"))) {
                        itemManager.getMessage(ack.getMessageId()).setRetryTime(itemManager.getMessage(ack.getMessageId()).getRetryTime() + 1);
                        return Response.getResponse(2, itemManager.getMessage(ack.getMessageId()));
                    } else {
                        itemManager.enterDeadQueue(ack.getMessageId(), ack.getQueueName(), ack.getConsumerTag());
                    }

                }
                return Response.success();
            }
            //创建绑定
            case 6 -> {
                DeclareBinding declareBinding = ObjectUtil.deserialize(request.getPayload());
                Binding binding = new Binding(declareBinding.getExchangeName(), declareBinding.getQueueName(), declareBinding.getBindingKey());
                itemManager.insertBinding(binding);
            }
            //创建队列
            case 7 -> {
                DeclareQueue declareQueue = ObjectUtil.deserialize(request.getPayload());
                MessageQueue queue = new MessageQueue();
                queue.setName(declareQueue.getName());
                queue.setDurable(declareQueue.isDurable());
                queue.setAutoAck(declareQueue.isAutoAck());
                itemManager.insertQueue(queue);
            }
            //问询消息
            case 8 -> {
                MessageQuery query = ObjectUtil.deserialize(request.getPayload());
                String consumerTag = query.getTag();
                if (itemManager.getUnreadMessage(consumerTag) != null) {
                    if (!itemManager.getUnreadMessage(consumerTag).isEmpty()) {
                        consumerManager.addQueue(consumerTag);
                    }
                }
                if (itemManager.getUnreadMessage(consumerTag) == null || itemManager.getUnreadMessage(consumerTag).isEmpty()) {
                    consumerManager.addListen(consumerTag);
                    return Response.getResponse(3, null);
                }
            }
            //删除消息
            case 9 -> {
                DeleteMessage deleteMessage = ObjectUtil.deserialize(request.getPayload());
                itemManager.deleteMessage(deleteMessage.getMessageId());
                String queueName = deleteMessage.getQueueName();
                String messageId = deleteMessage.getMessageId();
                if (itemManager.ifInQueue(queueName, messageId)) {
                    itemManager.removeFromQueue(queueName, messageId);
                } else if (itemManager.ifWaitingAck(queueName, messageId)) {
                    itemManager.removeFromAck(queueName, messageId);
                } else if (itemManager.ifDead(queueName, messageId)) {
                    itemManager.removeFromDead(queueName, messageId);
                }
                itemManager.removeFromConsumer(deleteMessage.getConsumerTag(), deleteMessage.getMessageId());
            }
            //等待ack
            case 10 -> {
                WaitingAck waitingAck = ObjectUtil.deserialize(request.getPayload());
                itemManager.waitingForAck(waitingAck.getMessageId(), waitingAck.getQueueName());
            }
            //消费死信队列
            case 11 -> {
                DeclareDeadQueue declareDeadQueue = ObjectUtil.deserialize(request.getPayload());
                Message message = itemManager.declareDeadQueue(declareDeadQueue.getQueueName());
                MessageBody body = new MessageBody();
                body.setMessage(message);
                body.setQueueName(declareDeadQueue.getQueueName());
                body.setRequireAck(false);
                return Response.getResponse(2, body);
            }
            default -> {
                return Response.getResponse(1, new Confirm("未经定义的请求"));
            }
        }
        return null;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    public AioSession getSession(String sessionId) {
        return sessionMap.get(sessionId);
    }

    /**
     * 初始化服务器，设置相关参数
     */
    public void init() {
        params = ConfigUtil.getServerConfig();
        DatabaseInitializr.createFile();
        consumerManager = new ConsumerManager(this, ConfigUtil.getConsumerConfig());
        String dbType = params.get("database");
        if ("embedded".equals(dbType)) {
            DatabaseInitializr.initDatabase();
        } else if ("mysql".equals(dbType)) {
            DatabaseInitializr.initMysql();
        }
        itemManager.init();
        if ("true".equals(params.get("tracing.enable"))) {
            apiController = new ApiController(this, ConfigUtil.getTracingConfig());
            apiController.start();
        }
        loginRequired = Boolean.parseBoolean(params.get("login.enable"));
        if (!"standalone".equals(params.get("cluster.mode"))) {
            clusterManager = new ClusterManager();
            clusterManager.init(ConfigUtil.getClusterConfig());
            clusterManager.start();
        }
        //从磁盘读取持久化的信息
        List<Message> durableList = DurableUtil.readAllMessage();
        for (Message message : durableList) {
            DurableMessage durableMessage = MessageDao.selectMessageById(message.getId());
            itemManager.insertMessage(message);
            ExchangeType type = itemManager.getExchange(durableMessage.getQueue()).getType();
            if (type == ExchangeType.DIRECT) {
                Exchange exchange = itemManager.getExchange(durableMessage.getQueue());
                if (itemManager.getBinding(exchange.getName()).containsKey(message.getRoutingKey())) {
                    itemManager.sendMessage(message.getRoutingKey(), message);
                }
            } else if (type == ExchangeType.FANOUT) {
                ConcurrentHashMap<String, Binding> bindingMap = itemManager.getBinding(durableMessage.getQueue());
                for (Binding binding : bindingMap.values()) {
                    itemManager.sendMessage(binding.getQueueName(), message);
                }
            } else if (type == ExchangeType.TOPIC) {
                ConcurrentHashMap<String, Binding> bindingMap = itemManager.getBinding(durableMessage.getQueue());
                String routingKey = message.getRoutingKey();
                for (Binding binding : bindingMap.values()) {
                    if (ReUtil.isMatch(binding.getBindingKey(), routingKey)) {
                        itemManager.sendMessage(binding.getQueueName(), message);
                    }
                }
            }
        }
    }

    public List<String> getConnections() {
        return Collections.list(sessionMap.keys());
    }

    public boolean isCluster() {
        return !"standalone".equals(params.get("cluster.mode"));
    }

    public ClusterManager getClusterManager() {
        return clusterManager;
    }
}
