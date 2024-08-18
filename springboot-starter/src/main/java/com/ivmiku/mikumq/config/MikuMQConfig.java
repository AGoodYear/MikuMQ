package com.ivmiku.mikumq.config;

import com.ivmiku.mikumq.core.MikuProperties;
import com.ivmiku.mikumq.core.MikuTemplate;
import jakarta.annotation.Resource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ComponentScan("com.ivmiku.mikumq")
@EnableConfigurationProperties(MikuProperties.class)
public class MikuMQConfig {
    @Resource
    private MikuProperties properties;

    @Bean
    public MikuTemplate mikuTemplate() {
        Map<String, String> map = new HashMap<>();
        map.put("host", properties.getHost());
        map.put("port", properties.getPort());
        map.put("username", properties.getUsername());
        map.put("password", properties.getPassword());
        map.put("writeBufferSize", String.valueOf(properties.getWriteBufferSize()));
        map.put("readBufferSize", String.valueOf(properties.getReadBufferSize()));
        map.put("writeBufferCapacity", String.valueOf(properties.getWriteBufferCapacity()));
        MikuTemplate template = new MikuTemplate();
        template.init(map);
        return template;
    }

}
