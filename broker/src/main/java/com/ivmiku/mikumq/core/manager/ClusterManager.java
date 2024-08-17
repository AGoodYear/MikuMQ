package com.ivmiku.mikumq.core.manager;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.ivmiku.mikumq.entity.Request;
import com.ivmiku.mikumq.request.Register;
import org.smartboot.socket.transport.AioQuickClient;
import org.smartboot.socket.transport.AioSession;
import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * 集群功能管理
 * @author Aurora
 */
public class ClusterManager {
    private HashMap<String, String> params;
    private static List<Instance> instances = new ArrayList<>();
    private static final Map<String, AioSession> SESSION_MAP = new ConcurrentHashMap<>();
    private static final Map<String, AioQuickClient> CLIENT_MAP = new ConcurrentHashMap<>();

    public void init(HashMap<String, String> params) {
        this.params = params;
    }

    public void start() {
        String address = params.get("address");
        try {
            //向nacos注册自身并订阅集群
            NamingService namingService = NacosFactory.createNamingService(address);
            namingService.registerInstance("MikuMQ", params.get("ip"), Integer.parseInt(params.get("port")), params.get("clusterName"));
            EventListener listener = event -> {
                if (event instanceof NamingEvent) {
                    instances = ((NamingEvent) event).getInstances();
                    for (Instance instance : instances) {
                        if (instance.getIp().equals(params.get("ip")) && instance.getPort()==Integer.parseInt(params.get("port"))){
                            instances.remove(instance);
                            break;
                        }
                    }
                    for (Instance instance : instances) {
                        String key = instance.getIp()+instance.getPort();
                        if (!CLIENT_MAP.containsKey(key)) {
                            AioQuickClient client = new AioQuickClient(instance.getIp(), instance.getPort(), new ResponseProtocol(), (aioSession, response) -> {
                                if (response.getType() == 4) {
                                    WriteBuffer buffer = aioSession.writeBuffer();
                                    byte[] data = ObjectUtil.serialize(Request.setRequest(12, null));
                                    try {
                                        buffer.writeInt(data.length);
                                        buffer.write(data);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                    buffer.flush();
                                }
                            });
                            try {
                                client.start();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            AioSession session = client.getSession();
                            SESSION_MAP.put(key, session);
                            CLIENT_MAP.put(key, client);
                            WriteBuffer buffer = session.writeBuffer();
                            Register register = new Register();
                            register.setTag(instance.getIp()+instance.getPort());
                            register.setCluster(true);
                            byte[] data = ObjectUtil.serialize(Request.setRequest(1, register));
                            try {
                                buffer.writeInt(data.length);
                                buffer.write(data);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            buffer.flush();
                        }
                    }
                }
            };
            namingService.subscribe("MikuMQ", Collections.singletonList(params.get("clusterName")), listener);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
        garbageCollect();
    }

    /**
     * 向集群其他实例发送请求
     * @param request 请求
     */
    public void sendToInstances(Request request) {
        for (Instance instance : instances) {
            String key = instance.getIp()+instance.getPort();
            AioSession session = CLIENT_MAP.get(key).getSession();
            WriteBuffer writeBuffer = session.writeBuffer();
            byte[] data = ObjectUtil.serialize(request);
            try {
                writeBuffer.writeInt(data.length);
                writeBuffer.write(data);
                writeBuffer.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 定期删除与离线的实例的链接释放资源
     */
    public void garbageCollect() {
        ScheduledExecutorService service = new ScheduledThreadPoolExecutor(1);
        ScheduledFuture<?> runnableFuture = service.scheduleWithFixedDelay(() -> {
            List<String> keyList = new ArrayList<>(CLIENT_MAP.keySet());
            List<String> currentKeyList = new ArrayList<>();
            for (Instance instance : instances) {
                currentKeyList.add("#"+instance.getIp() + instance.getPort());
            }
            for (String key : keyList) {
                if (!currentKeyList.contains(key)) {
                    SESSION_MAP.remove(key).close();
                    CLIENT_MAP.remove(key).shutdown();
                }
            }
        }, 1,1, TimeUnit.HOURS);
    }
}
