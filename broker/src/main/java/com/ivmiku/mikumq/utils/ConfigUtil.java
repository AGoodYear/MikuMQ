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
        return map;
    }
}
