package com.ivmiku.mikumq.tracing.entity;

import lombok.Data;

/**
 * 用户输入
 * @author Aurora
 */
@Data
public class UserInput {
    private String username;
    private String password;
    private String role;
}
