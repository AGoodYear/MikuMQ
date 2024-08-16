package com.ivmiku.mikumq.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("mikumq")
public class MikuProperties {
    private String host;
    private String port;
    private String username;
    private String password;
    private Long querytime = 500L;
}
