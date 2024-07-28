package com.ivmiku.mikumq.dao;

import com.ivmiku.mikumq.core.Exchange;
import com.ivmiku.mikumq.entity.ExchangeType;
import com.ivmiku.mikumq.utils.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 交换机数据库操作
 * @author Aurora
 */
public class ExchangeDao {
    public static void insertExchange(Exchange exchange) {
        Connection connection = null;
        PreparedStatement st = null;
        try {
            connection = JDBCUtils.getConnection();
            st = connection.prepareStatement("insert into exchange values (?, ?, ?)");
            st.setString(1, exchange.getName());
            st.setString(2, String.valueOf(exchange.getType()));
            st.setInt(3, exchange.isDurable() ? 1 : 0);
            st.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JDBCUtils.release(connection, st, null);
        }
    }

    public static void deleteExchange(String exchangeName) {
        Connection connection = null;
        PreparedStatement st = null;
        try {
            connection = JDBCUtils.getConnection();
            st = connection.prepareStatement("delete from exchange where name = ?");
            st.setString(1, exchangeName);
            st.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JDBCUtils.release(connection, st, null);
        }
    }

    public static List<Exchange> getExchange() {
        Connection connection = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        List<Exchange> list = new ArrayList<>();
        try {
            connection = JDBCUtils.getConnection();
            st = connection.prepareStatement("select name, type, durable from exchange");
            rs = st.executeQuery();

            while (rs.next()) {
                Exchange exchange = new Exchange();
                exchange.setName(rs.getString("name"));
                exchange.setType(ExchangeType.valueOf(rs.getString("type")));
                exchange.setDurable(rs.getInt("durable")!=0);
                list.add(exchange);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JDBCUtils.release(connection, st, rs);
        }
        return list;
    }
}
