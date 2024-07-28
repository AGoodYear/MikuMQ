package com.ivmiku.mikumq.tracing;

import cn.hutool.http.ContentType;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.ivmiku.mikumq.core.Binding;
import com.ivmiku.mikumq.core.Exchange;
import com.ivmiku.mikumq.core.MessageQueue;
import com.ivmiku.mikumq.server.Server;
import com.ivmiku.mikumq.tracing.entity.ExchangeInfo;
import com.ivmiku.mikumq.tracing.entity.QueueInfo;

import java.util.*;

/**
 * 对外查询数据的接口
 * @author Aurora
 */
public class ApiController {
    public final Server server;
    private final HashMap<String, String> params;

    public ApiController(Server server, HashMap<String, String> params) {
        this.server = server;
        this.params = params;
    }

    public void start() {
        HttpUtil.createServer(Integer.parseInt(params.get("port")))
                .addAction("/queue/list", ((httpServerRequest, httpServerResponse) -> {
                    List<String> list = server.getItemManager().getQueueList();
                    httpServerResponse.write(JSON.toJSONString(Result.ok(list)), ContentType.JSON.toString());
                }))
                .addAction("/queue", ((httpServerRequest, httpServerResponse) -> {
                    String queueName = httpServerRequest.getParam("name");
                    MessageQueue queue = server.getItemManager().getQueue(queueName);
                    QueueInfo info = new QueueInfo();
                    info.setName(queueName);
                    info.setMessageNum(server.getItemManager().getMessageList(queueName).size());
                    info.setListenerNum(queue.getListener().size());
                    info.setDurable(queue.isDurable());
                    info.setAutoAck(queue.isAutoAck());
                    httpServerResponse.write(JSON.toJSONString(Result.ok(info)), ContentType.JSON.toString());
                }))
                .addAction("/exchange/list", ((httpServerRequest, httpServerResponse) -> {
                    httpServerResponse.write(JSON.toJSONString(Result.ok(server.getItemManager().getExchangeList())));
                }))
                .addAction("/exchange", ((httpServerRequest, httpServerResponse) -> {
                    String exchangeName = httpServerRequest.getParam("name");
                    Exchange exchange = server.getItemManager().getExchange(exchangeName);
                    ExchangeInfo info = new ExchangeInfo();
                    info.setName(exchangeName);
                    info.setType(String.valueOf(exchange.getType()));
                    info.setDurable(exchange.isDurable());
                    info.setBindingNum(server.getItemManager().getBinding(exchangeName).size());
                    httpServerResponse.write(JSON.toJSONString(Result.ok(info)), ContentType.JSON.toString());
                }))
                .addAction("/exchange/binding", ((httpServerRequest, httpServerResponse) -> {
                    String exchangeName = httpServerRequest.getParam("name");
                    List<Binding> list = server.getItemManager().getBinding(exchangeName).values().stream().toList();
                    httpServerResponse.write(JSON.toJSONString(Result.ok(list)), ContentType.JSON.toString());
                }))
                .addAction("/connection/list", ((httpServerRequest, httpServerResponse) -> {
                    List<String> list = server.getConnections();
                    httpServerResponse.write(JSON.toJSONString(Result.ok(list)), ContentType.JSON.toString());
                }))
                .addAction("/", ((httpServerRequest, httpServerResponse) -> {
                    httpServerResponse.write("hello world");
                }))
                .start();
    }
}
