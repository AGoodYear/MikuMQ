package com.ivmiku.mikumq.utils;

import cn.hutool.crypto.SecureUtil;
import com.ivmiku.mikumq.core.User;
import com.ivmiku.mikumq.dao.UserDao;

import java.util.Objects;
import java.util.Random;

/**
 * 加密相关
 * @author Aurora
 */
public class PasswordUtil {
    /**
     * 获取随机盐值
     * @param n 位数
     * @return 盐值
     */
    public static String getSalt(int n) {
        char[] chars = ("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890!@#$%^&*()_+").toCharArray();
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < n; i++){
            //Random().nextInt()返回值为[0,n)
            char aChar = chars[new Random().nextInt(chars.length)];
            sb.append(aChar);
        }
        return sb.toString();
    }

    /**
     * md5加密
     * @param password 原密码
     * @param salt 盐值
     * @return 加密后的密码
     */
    public static String encrypt(String password, String salt) {
        return SecureUtil.md5(password+salt);
    }

    public static boolean login(String username, String password) {
        User user = UserDao.getUserByName(username);
        return user.getPassword().equals(encrypt(password, user.getSalt()));
    }
}
