package com.ivmiku.mikumq.tracing.entity;

import lombok.Data;

@Data
public class UserInput {
    private String username;
    private String password;
    private String role;
}
