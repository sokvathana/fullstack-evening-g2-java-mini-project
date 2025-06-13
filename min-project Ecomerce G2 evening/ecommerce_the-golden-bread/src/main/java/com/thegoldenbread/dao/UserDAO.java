package main.java.com.thegoldenbread.dao;

import main.java.com.thegoldenbread.dto.User;
import java.sql.*;

public class UserDAO {
    private final Connection conn;

    public UserDAO(Connection conn) {
        this.conn = conn;
    }

    public User login(String username, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_name = ? AND password = ? AND is_deleted = false";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("user_name"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setDeleted(rs.getBoolean("is_deleted"));
                user.setUuid(rs.getString("u_uuid"));
                return user;
            }
            return null;
        }
    }

    public boolean register(User user) throws SQLException {
        String sql = "INSERT INTO users (user_name, email, password, is_deleted, u_uuid) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());
            stmt.setBoolean(4, user.isDeleted());
            stmt.setString(5, user.getUuid());
            int rows = stmt.executeUpdate();
            System.out.println("\u001B[34mRows affected by insert: " + rows + "\u001B[0m"); // Debug
            if (rows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                }
                conn.commit(); // Explicitly commit the transaction
                return true;
            } else {
                conn.rollback(); // Rollback on failure
                return false;
            }
        } catch (SQLException e) {
            System.out.println("\u001B[31mSQL Error during registration: " + e.getMessage() + "\u001B[0m");
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.out.println("\u001B[31mRollback failed: " + ex.getMessage() + "\u001B[0m");
            }
            throw e;
        }
    }
}