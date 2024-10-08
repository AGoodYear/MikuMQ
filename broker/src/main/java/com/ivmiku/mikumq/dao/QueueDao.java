package com.ivmiku.mikumq.dao;

import com.ivmiku.mikumq.core.MessageQueue;
import com.ivmiku.mikumq.utils.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 消息队列数据库操作
 * @author Aurora
 */
public class QueueDao {
    public static void insertQueue(MessageQueue queue) {
        Connection connection = null;
        PreparedStatement st = null;
        try {
            connection = JDBCUtils.getConnection();
            st = connection.prepareStatement("insert into queue values (?, ?, ?)");
            st.setString(1, queue.getName());
            st.setInt(2, queue.isAutoAck()?1:0);
            st.setInt(3, queue.isDurable()?1:0);
            st.execute();
            st = connection.prepareStatement("insert into listener values (?, ?)");
            st.setString(1, queue.getName());
            List<String> list = queue.getListener();
            for (String listener : list) {
                st.setString(2, listener);
                st.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JDBCUtils.release(connection, st, null);
        }
    }

    public static void deleteQueue(String queueName) {
        Connection connection = null;
        PreparedStatement st = null;
        try {
            connection = JDBCUtils.getConnection();
            st = connection.prepareStatement("delete from queue where name = ?");
            st.setString(1, queueName);
            st.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JDBCUtils.release(connection, st, null);
        }
    }

    public static List<MessageQueue> getQueue() {
        Connection connection = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        List<MessageQueue> list = new ArrayList<>();
        try {
            connection = JDBCUtils.getConnection();
            st = connection.prepareStatement("select name, auto_ack, durable from queue");
            rs = st.executeQuery();
            st = connection.prepareStatement("select tag from listener where queue = ?");
            while (rs.next()) {
                MessageQueue queue = new MessageQueue();
                queue.setName(rs.getString("name"));
                queue.setAutoAck(rs.getInt("auto_ack") != 0);
                queue.setDurable(rs.getInt("durable") != 0);
                List<String> listener = new LinkedList<>();
                st.setString(1, queue.getName());
                ResultSet rs2 = st.executeQuery();
                while (rs2.next()) {
                    listener.add(rs2.getString("tag"));
                }
                queue.setListener(listener);
                list.add(queue);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JDBCUtils.release(connection, st, rs);
        }
        return list;
    }

    public static boolean ifExist(String queueName) {
        Connection connection = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            connection = JDBCUtils.getConnection();
            st = connection.prepareStatement("select name from queue where name = ?");
            st.setString(1, queueName);
            rs = st.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JDBCUtils.release(connection, st, rs);
        }
    }

    public static void insertListener(String queueName, String tag) {
        Connection connection = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            connection = JDBCUtils.getConnection();
            st = connection.prepareStatement("select tag from listener where queue = ? and tag = ?");
            st.setString(1, queueName);
            st.setString(2, tag);
            rs = st.executeQuery();
            if (rs.next()) {
                st = connection.prepareStatement("insert into listener values (?, ?)");
                st.setString(1, queueName);
                st.setString(2, tag);
                st.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JDBCUtils.release(connection, st, rs);
        }
    }
}
