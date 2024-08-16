package com.ivmiku.mikumq.dao;

import com.ivmiku.mikumq.core.User;
import com.ivmiku.mikumq.utils.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * User相关表操作
 * @author Aurora
 */
public class UserDao {
    public static void insertUser(User user) {
        Connection connection = null;
        PreparedStatement st = null;
        try {
            connection = JDBCUtils.getConnection();
            st = connection.prepareStatement("insert into user values (?, ?, ?, ?, ?)");
            st.setString(1, user.getId());
            st.setString(2, user.getUsername());
            st.setString(3, user.getSalt());
            st.setString(4, user.getPassword());
            st.setString(5, user.getRole());
            st.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JDBCUtils.release(connection, st, null);
        }
    }

    public static User getUserByName(String username) {
        Connection connection = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        User user = null;
        try {
            connection = JDBCUtils.getConnection();
            st = connection.prepareStatement("select id, username, salt, password, role, created_at from user where username = ?");
            st.setString(1, username);
            rs = st.executeQuery();
            if (rs.next()) {
                user = new User();
                user.setId(rs.getString("id"));
                user.setUsername(rs.getString("username"));
                user.setSalt(rs.getString("salt"));
                user.setPassword(rs.getString("password"));
                user.setCreatedAt(rs.getString("created_at"));
                user.setRole(rs.getString("role"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JDBCUtils.release(connection, st, rs);
        }
        return user;
    }

    public static void deleteUser(String username) {
        Connection connection = null;
        PreparedStatement st = null;
        try {
            connection = JDBCUtils.getConnection();
            st = connection.prepareStatement("delete from user where username = ?");
            st.setString(1, username);
            st.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JDBCUtils.release(connection, st, null);
        }
    }

    public static void changePassword(String username, String password, String salt) {
        Connection connection = null;
        PreparedStatement st = null;
        try {
            connection = JDBCUtils.getConnection();
            st = connection.prepareStatement("update user set salt = ? and password = ? where username = ?");
            st.setString(1, salt);
            st.setString(2, password);
            st.setString(3, username);
            st.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JDBCUtils.release(connection, st, null);
        }
    }
}
