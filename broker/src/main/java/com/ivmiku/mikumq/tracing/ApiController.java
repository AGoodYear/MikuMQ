package com.ivmiku.mikumq.tracing;

import cn.hutool.http.ContentType;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ivmiku.mikumq.core.Binding;
import com.ivmiku.mikumq.core.Exchange;
import com.ivmiku.mikumq.core.MessageQueue;
import com.ivmiku.mikumq.server.Server;
import com.ivmiku.mikumq.tracing.entity.ExchangeInfo;
import com.ivmiku.mikumq.tracing.entity.QueueInfo;
import com.ivmiku.mikumq.utils.AuthUtil;
import com.ivmiku.mikumq.utils.PasswordUtil;

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
                .addAction("/login", ((httpServerRequest, httpServerResponse) -> {
                    JSONObject body = JSONObject.parseObject(httpServerRequest.getBody());
                    if (PasswordUtil.login(body.getString("username"), body.getString("password"))) {
                        Map<String, String> map = new HashMap<>(1);
                        map.put("token", AuthUtil.getToken(body.getString("username")));
                        httpServerResponse.write(JSON.toJSONString(Result.ok(map)));
                    } else {
                        httpServerResponse.write(JSON.toJSONString(Result.error("用户名或密码错误")), ContentType.JSON.toString());
                    }
                }))
                .addAction("/queue/list", ((httpServerRequest, httpServerResponse) -> {
                    if (AuthUtil.validate(httpServerRequest.getHeader("token"))) {
                        List<String> list = server.getItemManager().getQueueList();
                        httpServerResponse.write(JSON.toJSONString(Result.ok(list)), ContentType.JSON.toString());
                    } else {
                        httpServerResponse.write(JSON.toJSONString(Result.error("token无效！")), ContentType.JSON.toString());
                    }

                }))
                .addAction("/queue", ((httpServerRequest, httpServerResponse) -> {
                    if (AuthUtil.validate(httpServerRequest.getHeader("token"))) {
                        String queueName = httpServerRequest.getParam("name");
                        MessageQueue queue = server.getItemManager().getQueue(queueName);
                        QueueInfo info = new QueueInfo();
                        info.setName(queueName);
                        info.setMessageNum(server.getItemManager().getMessageList(queueName).size());
                        info.setListenerNum(queue.getListener().size());
                        info.setDurable(queue.isDurable());
                        info.setAutoAck(queue.isAutoAck());
                        httpServerResponse.write(JSON.toJSONString(Result.ok(info)), ContentType.JSON.toString());
                    } else {
                        httpServerResponse.write(JSON.toJSONString(Result.error("token无效！")), ContentType.JSON.toString());
                    }
                }))
                .addAction("/exchange/list", ((httpServerRequest, httpServerResponse) -> {
                    if (AuthUtil.validate(httpServerRequest.getHeader("token"))) {
                        httpServerResponse.write(JSON.toJSONString(Result.ok(server.getItemManager().getExchangeList())));
                    } else {
                        httpServerResponse.write(JSON.toJSONString(Result.error("token无效！")), ContentType.JSON.toString());
                    }
                }))
                .addAction("/exchange", ((httpServerRequest, httpServerResponse) -> {
                    if (AuthUtil.validate(httpServerRequest.getHeader("token"))) {
                        String exchangeName = httpServerRequest.getParam("name");
                        Exchange exchange = server.getItemManager().getExchange(exchangeName);
                        ExchangeInfo info = new ExchangeInfo();
                        info.setName(exchangeName);
                        info.setType(String.valueOf(exchange.getType()));
                        info.setDurable(exchange.isDurable());
                        info.setBindingNum(server.getItemManager().getBinding(exchangeName).size());
                        httpServerResponse.write(JSON.toJSONString(Result.ok(info)), ContentType.JSON.toString());
                    } else {
                        httpServerResponse.write(JSON.toJSONString(Result.error("token无效！")), ContentType.JSON.toString());
                    }
                }))
                .addAction("/exchange/binding", ((httpServerRequest, httpServerResponse) -> {
                    if (AuthUtil.validate(httpServerRequest.getHeader("token"))) {
                        String exchangeName = httpServerRequest.getParam("name");
                        List<Binding> list = server.getItemManager().getBinding(exchangeName).values().stream().toList();
                        httpServerResponse.write(JSON.toJSONString(Result.ok(list)), ContentType.JSON.toString());
                    } else {
                        httpServerResponse.write(JSON.toJSONString(Result.error("token无效！")), ContentType.JSON.toString());
                    }
                }))
                .addAction("/connection/list", ((httpServerRequest, httpServerResponse) -> {
                    if (AuthUtil.validate(httpServerRequest.getHeader("token"))) {
                        List<String> list = server.getConnections();
                        httpServerResponse.write(JSON.toJSONString(Result.ok(list)), ContentType.JSON.toString());
                    } else {
                        httpServerResponse.write(JSON.toJSONString(Result.error("token无效！")), ContentType.JSON.toString());
                    }
                }))
                .addAction("/", ((httpServerRequest, httpServerResponse) -> {
                    if (AuthUtil.validate(httpServerRequest.getHeader("token"))) {
                        httpServerResponse.write("hello world");
                    } else {
                        httpServerResponse.write(JSON.toJSONString(Result.error("token无效！")), ContentType.JSON.toString());
                    }
                }))
                .start();
    }
}
