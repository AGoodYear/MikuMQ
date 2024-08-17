package com.ivmiku.mikumq;

import com.ivmiku.mikumq.core.server.Server;

import java.io.IOException;

/**
 * MikuMQ启动服务器的主类
 * @author Aurora
 */
public class Main {
    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.start();
    }
}
