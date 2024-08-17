package com.ivmiku.mikumq.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 记录队列消费者轮询消费次序
 * @author Aurora
 */
public class BalancerUtil {
    private static final Map<String, Integer> BALANCE_MAP = new ConcurrentHashMap<>();

    /**
     * 获取当前要询问第几个订阅者
     * @param queueName 队列名字
     * @return 询问的次序
     */
    public static Integer getRobin(String queueName) {
        Integer robin = BALANCE_MAP.computeIfAbsent(queueName, k -> 0);
        BALANCE_MAP.put(queueName, robin+1);
        return robin;
    }

    /**
     * 溢出（超过订阅者数量）重置轮询次序
     * @param queueName 队列名称
     */
    public static void resetQueue(String queueName) {
        BALANCE_MAP.put(queueName, 1);
    }
}
