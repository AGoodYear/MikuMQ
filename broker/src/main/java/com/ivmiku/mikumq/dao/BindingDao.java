package com.ivmiku.mikumq.dao;

import com.ivmiku.mikumq.core.Binding;
import com.ivmiku.mikumq.utils.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 绑定关系数据库操作
 * @author Aurora
 */
public class BindingDao {
    public static void insertBinding(Binding binding) {
        Connection connection = null;
        PreparedStatement st = null;
        try {
            connection = JDBCUtils.getConnection();
            st = connection.prepareStatement("insert into binding values (?, ?, ?)");
            st.setString(1, binding.getBindingKey());
            st.setString(2, binding.getExchangeName());
            st.setString(3, binding.getQueueName());
            st.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JDBCUtils.release(connection, st, null);
        }
    }

    public static void deleteExchange(String bindingKey) {
        Connection connection = null;
        PreparedStatement st = null;
        try {
            connection = JDBCUtils.getConnection();
            st = connection.prepareStatement("delete from binding where bkey = ?");
            st.setString(1, bindingKey);
            st.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JDBCUtils.release(connection, st, null);
        }
    }

    public static List<Binding> getBinding() {
        Connection connection = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        List<Binding> list = new ArrayList<>();
        try {
            connection = JDBCUtils.getConnection();
            st = connection.prepareStatement("select bkey, exchange, queue from binding");
            rs = st.executeQuery();
            while (rs.next()) {
                Binding binding = new Binding();
                binding.setBindingKey(rs.getString("key"));
                binding.setExchangeName(rs.getString("exchange"));
                binding.setQueueName(rs.getString("queue"));
                list.add(binding);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JDBCUtils.release(connection, st, rs);
        }
        return list;
    }
}
