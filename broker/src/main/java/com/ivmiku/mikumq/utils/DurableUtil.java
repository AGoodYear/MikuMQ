package com.ivmiku.mikumq.utils;

import cn.hutool.core.util.ObjectUtil;
import com.ivmiku.mikumq.core.DurableMessage;
import com.ivmiku.mikumq.entity.Message;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 消息持久化相关工具
 * @author Aurora
 */
public class DurableUtil {
    private static final RandomAccessFile MESSAGE_FILE;

    static{
        createFile();
        try {
            MESSAGE_FILE = new RandomAccessFile("./data/message", "rw");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createFile() {
        String path = "./data";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(path + "/" + "message");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static DurableMessage writeMessage(Message message) {
        byte[] data = ObjectUtil.serialize(message);
        int start;
        synchronized (MESSAGE_FILE) {
            try {
                start = (int) MESSAGE_FILE.length();
                MESSAGE_FILE.seek(MESSAGE_FILE.length());
                MESSAGE_FILE.writeInt(1);
                MESSAGE_FILE.writeInt(data.length);
                MESSAGE_FILE.write(data);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        DurableMessage durableMessage = new DurableMessage();
        durableMessage.setId(message.getId());
        durableMessage.setStart(String.valueOf(start));
        return durableMessage;
    }

    public static List<Message> readAllMessage() {
        List<Message> list = new ArrayList<>();
        synchronized (MESSAGE_FILE) {
            try{
                MESSAGE_FILE.seek(0);
                while (true) {
                    int valid = MESSAGE_FILE.readInt();
                    int length = MESSAGE_FILE.readInt();
                    if (valid == 0) {
                        MESSAGE_FILE.skipBytes(length);
                    } else {
                        byte[] data = new byte[length];
                        MESSAGE_FILE.readFully(data);
                        Message message = ObjectUtil.deserialize(data);
                        list.add(message);
                    }
                }
            } catch (EOFException ignored) {

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return list;
    }

    public static void invalidateMessage(int offset) {
        synchronized (MESSAGE_FILE) {
            try {
                MESSAGE_FILE.seek(offset);
                MESSAGE_FILE.writeInt(0);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
