package com.ivmiku.mikumq.dao;

import com.ivmiku.mikumq.core.DurableMessage;
import com.ivmiku.mikumq.utils.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MessageDao {
    public static void insertMessage(DurableMessage message) {
        Connection connection = null;
        PreparedStatement st = null;
        try {
            connection = JDBCUtils.getConnection();
            st = connection.prepareStatement("insert into message values (?, ?, ?)");
            st.setString(1, message.getId());
            st.setString(2, message.getStart());
            st.setString(3, message.getQueue());
            st.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JDBCUtils.release(connection, st, null);
        }
    }

    public static List<DurableMessage> getAll() {
        Connection connection = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        List<DurableMessage> list = new ArrayList<>();
        try {
            connection = JDBCUtils.getConnection();
            st = connection.prepareStatement("select id, start, queue from message");
            rs = st.executeQuery();
            while (rs.next()) {
                DurableMessage message = new DurableMessage();
                message.setId(rs.getString("id"));
                message.setStart(rs.getString("start"));
                message.setQueue(rs.getString("queue"));
                list.add(message);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JDBCUtils.release(connection, st, null);
        }
        return list;
    }

    public static void deleteMessage(String id) {
        Connection connection = null;
        PreparedStatement st = null;
        try {
            connection = JDBCUtils.getConnection();
            st = connection.prepareStatement("delete from message where id = ?");
            st.setString(1, id);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JDBCUtils.release(connection, st, null);
        }
    }

    public static DurableMessage selectMessageById(String id) {
        Connection connection = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            connection = JDBCUtils.getConnection();
            st = connection.prepareStatement("select id, start, queue from message where id = ?");
            st.setString(1, id);
            rs = st.executeQuery();
            if (rs.next()) {
                DurableMessage message = new DurableMessage();
                message.setId(rs.getString("id"));
                message.setStart(rs.getString("start"));
                message.setQueue(rs.getString("queue"));
                return message;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JDBCUtils.release(connection, st, rs);
        }
        return null;
    }
}
