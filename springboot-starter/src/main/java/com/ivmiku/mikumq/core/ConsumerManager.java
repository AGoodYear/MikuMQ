package com.ivmiku.mikumq.core;

import com.ivmiku.mikumq.consumer.Consumer;
import com.ivmiku.mikumq.entity.Message;
import jakarta.annotation.Resource;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消费者管理中心
 * @author Aurora
 */
public class ConsumerManager {
    public static Map<String, Consumer> consumerMap = new ConcurrentHashMap<>();
    public static Map<String, List<Message>> consumerList = new ConcurrentHashMap<>();
    public static List<ConsumerProperties> propertiesList;

    public static void setPropertiesList(List<ConsumerProperties> propertiesList) {
        ConsumerManager.propertiesList = propertiesList;
    }

    @Resource
    private MikuProperties properties;

}
