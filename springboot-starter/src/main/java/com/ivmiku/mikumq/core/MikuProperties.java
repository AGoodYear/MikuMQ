package com.ivmiku.mikumq.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SpringBoot配置项
 * @author Aurora
 */
@Data
@ConfigurationProperties("mikumq")
public class MikuProperties {
    private String host;
    private String port;
    private String username;
    private String password;
    private Long querytime = 500L;
    private int readBufferSize = 1024;
    private int writeBufferSize = 1024;
    private int writeBufferCapacity = 2;
}
