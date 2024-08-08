package com.ivmiku.mikumq.server;

import cn.hutool.core.util.ObjectUtil;
import com.ivmiku.mikumq.core.*;
import com.ivmiku.mikumq.dao.DatabaseInitializr;
import com.ivmiku.mikumq.entity.ExchangeType;
import com.ivmiku.mikumq.entity.Request;
import com.ivmiku.mikumq.entity.Response;
import com.ivmiku.mikumq.manager.ClusterManager;
import com.ivmiku.mikumq.manager.ConsumerManager;
import com.ivmiku.mikumq.manager.ItemManager;
import com.ivmiku.mikumq.request.*;
import com.ivmiku.mikumq.tracing.ApiController;
import com.ivmiku.mikumq.utils.ConfigUtil;
import com.ivmiku.mikumq.utils.PasswordUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.StateMachineEnum;
import org.smartboot.socket.transport.AioQuickServer;
import org.smartboot.socket.transport.AioSession;
import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Broker服务器
 * @author Aurora
 */
@Slf4j
public class Server {
    public static ConcurrentHashMap<String, AioSession> sessionMap = new ConcurrentHashMap<>();

    public static ItemManager itemManager = new ItemManager();
    @Getter
    private static ClusterManager clusterManager = null;

    public static ConsumerManager consumerManager;

    public static HashMap<String, String> params;

    public static ApiController apiController;

    public boolean loginRequired;

    public void start() throws IOException {
        init();
        MessageProcessor<Request> processor = new MessageProcessor<>() {
            @Override
            public void process(AioSession aioSession, Request request) {
                if (request.getType() == 1) {
                    Register register = ObjectUtil.deserialize(request.getPayload());
                    boolean logged = false;
                    if (loginRequired) {
                        logged = PasswordUtil.login(register.getUsername(), register.getPassword());
                    }
                    if (logged) {
                        sessionMap.put(register.getTag(), aioSession);
                    }
                }
                sendResponse(aioSession, response(request));
                if (clusterManager != null && request.getType() != 1) {
                    clusterManager.sendToInstances(request);
                }
            }

            @Override
            public void stateEvent(AioSession session, StateMachineEnum stateMachineEnum, Throwable throwable) {
                if (stateMachineEnum == StateMachineEnum.DECODE_EXCEPTION || stateMachineEnum == StateMachineEnum.PROCESS_EXCEPTION) {
                    System.out.println("解码时出现了异常");
                    throwable.printStackTrace();
                }
                if (stateMachineEnum == StateMachineEnum.SESSION_CLOSED) {
                    for(Iterator<Map.Entry<String, AioSession>> it = sessionMap.entrySet().iterator(); it.hasNext();) {
                        Map.Entry<String, AioSession> item = it.next();
                        if (item.getValue() == session) {
                            it.remove();
                            break;
                        }
                    }
                }
            }
        };
        AioQuickServer server = new AioQuickServer(params.get("host") ,Integer.parseInt(params.get("port")), new RequestProtocol(), processor);
        server.setLowMemory(true);
        server.setThreadNum(Integer.parseInt(params.get("threadNum")));
        server.start();
        log.info("MikuMQ Broker Started");
    }

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

    public Response response(Request request) {
        if (request.getType() == 1) {
            Register register = ObjectUtil.deserialize(request.getPayload());
            if (sessionMap.containsKey(register.getTag())) {
                return Response.success();
            } else {
                return Response.error("登陆失败！请检查相关配置");
            }
        } else if (request.getType() == 2) {
            Subscribe subscribe = ObjectUtil.deserialize(request.getPayload());
            if (itemManager.getQueue(subscribe.getQueueName()) == null) {
                return Response.error("要订阅的队列不存在！");
            }
            List<String> listener = itemManager.getQueue(subscribe.getQueueName()).getListener();
            if (!listener.contains(subscribe.getTag())) {
                listener.add(subscribe.getTag());
            }
            return Response.success();
        } else if (request.getType() == 3) {
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
            }
           return Response.success();
        } else if (request.getType() == 4) {
            DeclareExchange declareExchange = ObjectUtil.deserialize(request.getPayload());
            Exchange exchange = new Exchange();
            exchange.setName(declareExchange.getName());
            exchange.setType(declareExchange.getType());
            exchange.setDurable(declareExchange.isDurable());
            itemManager.insertExchange(exchange);
            return Response.success();
        } else if (request.getType() == 5) {
            Acknowledgement ack = ObjectUtil.deserialize(request.getPayload());
            if (ack.isSuccess()) {
                itemManager.ackMessage(ack.getMessageId(), ack.getQueueName(), ack.getConsumerTag());
                System.out.println("AckOk");
            } else {
                if (itemManager.getMessage(ack.getMessageId()).getRetryTime() < Integer.parseInt(params.get("retryTime"))) {
                    itemManager.getMessage(ack.getMessageId()).setRetryTime(itemManager.getMessage(ack.getMessageId()).getRetryTime()+1);
                    return Response.getResponse(2, itemManager.getMessage(ack.getMessageId()));
                } else {
                    itemManager.enterDeadQueue(ack.getMessageId(), ack.getQueueName(), ack.getConsumerTag());
                }

            }
            return Response.success();
        } else if (request.getType() == 6) {
            DeclareBinding declareBinding = ObjectUtil.deserialize(request.getPayload());
            Binding binding = new Binding(declareBinding.getExchangeName(), declareBinding.getQueueName(), declareBinding.getBindingKey());
            itemManager.insertBinding(binding);
            return Response.success();
        } else if (request.getType() == 7) {
            DeclareQueue declareQueue = ObjectUtil.deserialize(request.getPayload());
            MessageQueue queue = new MessageQueue();
            queue.setName(declareQueue.getName());
            queue.setDurable(declareQueue.isDurable());
            queue.setAutoAck(declareQueue.isAutoAck());
            itemManager.insertQueue(queue);
            return Response.success();
        } else if (request.getType() == 8) {
            MessageQuery query = ObjectUtil.deserialize(request.getPayload());
            String consumerTag = query.getTag();
            if (itemManager.getUnreadMessage(consumerTag) != null) {
                if (!itemManager.getUnreadMessage(consumerTag).isEmpty()) {
                    consumerManager.addQueue(consumerTag);
                    return Response.success();
                }
            }
            return Response.success();
        } else if (request.getType() == 9) {
            DeleteMessage deleteMessage = ObjectUtil.deserialize(request.getPayload());
            itemManager.deleteMessage(deleteMessage.getMessageId());
        } else if (request.getType() == 10) {
            WaitingAck waitingAck = ObjectUtil.deserialize(request.getPayload());

        }
        return null;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    public AioSession getSession(String sessionId) {
        return sessionMap.get(sessionId);
    }

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
    }

    public List<String> getConnections() {
        return Collections.list(sessionMap.keys());
    }

    public boolean isCluster() {
        return !"standalone".equals(params.get("cluster.mode"));
    }
}
