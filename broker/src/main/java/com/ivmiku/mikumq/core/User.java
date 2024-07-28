package com.ivmiku.mikumq.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户实体
 * @author Aurora
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String id;
    private String username;
    private String salt;
    private String password;
    private String role;
    private String createdAt;
}
