package com.ivmiku.mikumq;

import com.ivmiku.mikumq.server.Server;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.start();
    }
}
