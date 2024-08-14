package com.ivmiku.mikumq.utils;

import cn.hutool.core.lang.hash.Hash;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

/**
 * 配置项相关
 * @author Aurora
 */
public class ConfigUtil {
    public static Properties properties;

    static {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("./config/broker.properties"));
            properties = new Properties();
            properties.load(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static HashMap<String, String> getServerConfig() {
        HashMap<String, String> map = new HashMap<>(7);
        map.put("threadNum", (String) properties.get("server.threadNum"));
        map.put("port", (String) properties.get("server.port"));
        map.put("host", (String) properties.get("server.host"));
        map.put("retryTime", (String) properties.get("server.retryTime"));
        map.put("tracing.enable", (String) properties.get("tracing.enable"));
        map.put("login.enable", (String) properties.get("server.login.enable"));
        map.put("database", (String) properties.get("server.database"));
        map.put("heart.rate", (String) properties.get("server.heart.rate"));
        map.put("heart.timeout", (String) properties.get("server.heart.timeout"));
        map.put("cluster.mode", (String) properties.get("server.mode"));
        return map;
    }

    public static HashMap<String, String> getConsumerConfig() {
        HashMap<String, String> map = new HashMap<>(1);
        map.put("scanThread", (String) properties.get("server.scanThread"));
        return map;
    }

    public static HashMap<String, String> getTracingConfig() {
        HashMap<String, String> map = new HashMap<>(1);
        map.put("port", (String) properties.get("tracing.port"));
        return map;
    }

    public static HashMap<String, String> getDbConfig() {
        HashMap<String, String> map = new HashMap<>(4);
        map.put("database" , (String) properties.get("server.database"));
        map.put("database.url", (String) properties.get("server.database.url"));
        map.put("database.username", (String) properties.get("server.database.username"));
        map.put("database.password", (String) properties.get("server.database.password"));
        map.put("maxsize", (String) properties.get("server.database.maxpoolsize"));
        map.put("minidle", (String) properties.get("server.database.minidle"));
        map.put("maxlife", (String) properties.get("server.database.maxlifetime"));
        return map;
    }

    public static HashMap<String, String> getClusterConfig() {
        HashMap<String, String> map = new HashMap<>(4);
        map.put("ip", (String) properties.get("server.host"));
        map.put("port", (String) properties.get("server.port"));
        map.put("clusterName", (String) properties.get("cluster.name"));
        map.put("address", (String) properties.get("nacos.address"));
        return map;
    }

    public static HashMap<String, String> getAuthConfig() {
        HashMap<String, String> map = new HashMap<>(4);
        map.put("key", (String) properties.get("tracing.auth.key"));
        map.put("expireTime", (String) properties.get("tracing.auth.expireTime"));
        return map;
    }
}
