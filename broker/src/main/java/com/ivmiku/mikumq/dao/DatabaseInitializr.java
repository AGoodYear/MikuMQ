package com.ivmiku.mikumq.dao;

import com.ivmiku.mikumq.utils.JDBCUtils;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 初始化数据库
 * @author Aurora
 */
public class DatabaseInitializr {
    public static void initDatabase() {
        Connection connection = null;
        try {
            connection = JDBCUtils.getConnection();
            PreparedStatement statement1 = connection.prepareStatement("CREATE TABLE IF NOT EXISTS \"exchange\" (\n" +
                    "  \"name\" text(100) NOT NULL,\n" +
                    "  \"type\" text(20),\n" +
                    "  \"durable\" integer(1),\n" +
                    "  PRIMARY KEY (\"name\")\n" +
                    ");");
            PreparedStatement statement2 = connection.prepareStatement("CREATE TABLE IF NOT EXISTS \"queue\" (\n" +
                    "  \"name\" text(100) NOT NULL,\n" +
                    "  PRIMARY KEY (\"name\")\n" +
                    ");");
            PreparedStatement statement3 = connection.prepareStatement("CREATE TABLE IF NOT EXISTS \"binding\" (\n" +
                    "  \"bkey\" text(100) NOT NULL,\n" +
                    "  \"exchange\" text(100),\n" +
                    "  \"queue\" text(100),\n" +
                    "  PRIMARY KEY (\"bkey\"),\n" +
                    "  CONSTRAINT \"binding_ibfk_1\" FOREIGN KEY (\"exchange\") REFERENCES \"exchange\" (\"name\") ON DELETE CASCADE ON UPDATE CASCADE,\n" +
                    "  CONSTRAINT \"binding_ibfk_2\" FOREIGN KEY (\"queue\") REFERENCES \"queue\" (\"name\") ON DELETE CASCADE ON UPDATE CASCADE\n" +
                    ")");
            PreparedStatement statement4 = connection.prepareStatement("CREATE TABLE IF NOT EXISTS \"listener\" (\n" +
                    "  \"queue\" TEXT,\n" +
                    "  \"tag\" TEXT,\n" +
                    "  FOREIGN KEY (\"queue\") REFERENCES \"queue\" (\"name\") ON DELETE CASCADE ON UPDATE CASCADE\n" +
                    ");");
            PreparedStatement statement5 = connection.prepareStatement("CREATE TABLE IF NOT EXISTS \"user\" (\n" +
                    "  \"id\" integer NOT NULL,\n" +
                    "  \"username\" text(50) NOT NULL,\n" +
                    "  \"salt\" text(20),\n" +
                    "  \"password\" text(100) NOT NULL,\n" +
                    "  \"role\" text(20),\n" +
                    "  \"created_at\" text,\n" +
                    "  PRIMARY KEY (\"id\")\n" +
                    ");");
            statement1.execute();
            statement2.execute();
            statement3.execute();
            statement4.execute();
            statement5.execute();
            statement1.close();
            statement2.close();
            statement3.close();
            statement4.close();
            statement5.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JDBCUtils.release(connection, null, null);
        }
        try {
            connection = JDBCUtils.getConnection();
            PreparedStatement statement6 = connection.prepareStatement("CREATE UNIQUE INDEX IF NOT EXISTS \"username\"\n" +
                    "ON \"user\" (\n" +
                    "  \"username\" ASC\n" +
                    ");");
            statement6.execute();
            statement6.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JDBCUtils.release(connection, null, null);
        }
    }

    public static void initMysql() {
        Connection connection = null;
        try {
            connection = JDBCUtils.getConnection();
            PreparedStatement statement1 = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `exchange` (\n" +
                    "  `name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,\n" +
                    "  `type` varchar(20) COLLATE utf8mb4_general_ci DEFAULT 'direct',\n" +
                    "  `durable` tinyint(1) DEFAULT '0',\n" +
                    "  PRIMARY KEY (`name`)\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;");
            PreparedStatement statement2 = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `queue` (\n" +
                    "  `name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,\n" +
                    "  PRIMARY KEY (`name`)\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;");
            PreparedStatement statement3 = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `binding` (\n" +
                    "  `bkey` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,\n" +
                    "  `exchange` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,\n" +
                    "  `queue` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,\n" +
                    "  PRIMARY KEY (`bkey`),\n" +
                    "  KEY `exchange` (`exchange`,`queue`),\n" +
                    "  KEY `queue` (`queue`),\n" +
                    "  CONSTRAINT `binding_ibfk_1` FOREIGN KEY (`exchange`) REFERENCES `exchange` (`name`) ON DELETE CASCADE ON UPDATE CASCADE,\n" +
                    "  CONSTRAINT `binding_ibfk_2` FOREIGN KEY (`queue`) REFERENCES `queue` (`name`) ON DELETE CASCADE ON UPDATE CASCADE\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;");
            PreparedStatement statement4 = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `listener` (\n" +
                    "  `queue` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,\n" +
                    "  `tag` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,\n" +
                    "  KEY `queue` (`queue`),\n" +
                    "  CONSTRAINT `listener_ibfk_1` FOREIGN KEY (`queue`) REFERENCES `queue` (`name`) ON DELETE CASCADE ON UPDATE CASCADE\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;");
            PreparedStatement statement5 = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `user` (\n" +
                    "  `id` bigint NOT NULL,\n" +
                    "  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,\n" +
                    "  `salt` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,\n" +
                    "  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,\n" +
                    "  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'user',\n" +
                    "  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,\n" +
                    "  PRIMARY KEY (`id`),\n" +
                    "  UNIQUE KEY `username` (`username`)\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;");
            statement1.execute();
            statement2.execute();
            statement3.execute();
            statement4.execute();
            statement5.execute();
            statement1.close();
            statement2.close();
            statement3.close();
            statement4.close();
            statement5.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JDBCUtils.release(connection, null, null);
        }
    }
    public static void createFile() {
        String path = "./data";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(path + "/meta.db");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
