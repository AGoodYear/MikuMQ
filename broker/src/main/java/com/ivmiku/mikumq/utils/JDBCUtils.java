package com.ivmiku.mikumq.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.HashMap;

/**
 * JDBCUtils用于处理数据库的连接与释放
 *
 * @author AGoodYear
 * @date 2023/12/19
 */
public class JDBCUtils {
    /**
     * HikariCP数据源
     */
    private static final HikariDataSource DATA_SOURCE;
    private static final HashMap<String, String> PARAMS;

    /**
     * 读取配置文件
     */
    static{
        PARAMS = ConfigUtil.getDbConfig();
        HikariConfig config = new HikariConfig();
        if (PARAMS.get("database").equals("embedded")) {
            config.setJdbcUrl("jdbc:sqlite:./data/meta.db");
        } else {
            config.setJdbcUrl(PARAMS.get("database.url"));
            config.setUsername(PARAMS.get("database.username"));
            config.setPassword(PARAMS.get("database.password"));
        }
        config.setMaximumPoolSize(60);
        config.setMinimumIdle(5);
        config.setMaxLifetime(6000);
        DATA_SOURCE = new HikariDataSource(config);
    }

    /**
     * 获取数据库连接
     *
     * @return Connection对象
     * @throws SQLException 如果连接过程出现错误
     */
    public static Connection getConnection() throws SQLException{
        try {
            return DATA_SOURCE.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 释放数据库连接
     *
     * @param con 要释放的Connection对象
     * @param st 要释放的PreparedStatement对象
     * @param rs 要释放的ResultSet对象
     */
    public static void release(Connection con, PreparedStatement st, ResultSet rs){
        if (rs != null){
            try {
                rs.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        if (st != null){
            try {
                st.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        if (con != null){
            try {
                con.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

}


