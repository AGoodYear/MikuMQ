package com.ivmiku.mikumq.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 记录队列消费者轮询消费次序
 * @author Aurora
 */
public class BalancerUtil {
    private static final Map<String, Integer> BALANCE_MAP = new ConcurrentHashMap<>();

    public static Integer getRobin(String queueName) {
        Integer robin = BALANCE_MAP.computeIfAbsent(queueName, k -> 0);
        BALANCE_MAP.put(queueName, robin+1);
        return robin;
    }

    public static void resetQueue(String queueName) {
        BALANCE_MAP.put(queueName, 1);
    }
}
