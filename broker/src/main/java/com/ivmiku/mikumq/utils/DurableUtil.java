package com.ivmiku.mikumq.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.util.ObjectUtil;
import com.ivmiku.mikumq.entity.Message;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class DurableUtil {
    public static void createFile(String queueName) {
        String path = "./message";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(path + "/" + queueName);
        File file2 = new File(path + "/" + queueName + "rec");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            if (file2.exists()) {
                file2.createNewFile();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeMessage(Message message, String queueName) {
        byte[] body = ObjectUtil.serialize(message);
        File file = new File("./message/" + queueName);
        File recFile = new File("./message/" + queueName + "rec");
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.append("1");
        fileWriter.append(String.valueOf(body.length));
        fileWriter.append(body, 0, body.length);
    }

    public static List<Message> getAllMessage(String queueName) {
        File file = new File("./message/" + queueName);
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = fileReader.getReader();
        try {
            int valid = bufferedReader.read();
            int length = bufferedReader.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;//todo

    }
}
