package main.java.com.thegoldenbread.dao;

import main.java.com.thegoldenbread.dto.Cart;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartDAO {
    private final Connection conn;

    public CartDAO(Connection conn) {
        this.conn = conn;
    }

    public boolean addToCart(Cart cart) throws SQLException {
        String sql = "INSERT INTO carts (user_id, product_id, quantity) VALUES (?, ?, ?) ON CONFLICT (user_id, product_id) DO UPDATE SET quantity = carts.quantity + EXCLUDED.quantity";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, cart.getUserId());
            stmt.setInt(2, cart.getProductId());
            stmt.setInt(3, cart.getQuantity());
            int rows = stmt.executeUpdate();
            System.out.println("\u001B[34mRows affected by cart insert/update: " + rows + "\u001B[0m"); // Debug
            if (rows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    cart.setId(rs.getInt(1));
                }
                conn.commit(); // Explicitly commit the transaction
                return true;
            } else {
                conn.rollback(); // Rollback on failure
                return false;
            }
        } catch (SQLException e) {
            System.out.println("\u001B[31mSQL Error during addToCart: " + e.getMessage() + "\u001B[0m");
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.out.println("\u001B[31mRollback failed: " + ex.getMessage() + "\u001B[0m");
            }
            throw e;
        }
    }

    public List<Cart> getCartItems(int userId) throws SQLException {
        List<Cart> cartItems = new ArrayList<>();
        String sql = "SELECT c.id, c.user_id, c.product_id, c.quantity, p.p_name, p.price FROM carts c JOIN products p ON c.product_id = p.id WHERE c.user_id = ? AND p.is_deleted = false";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Cart cart = new Cart();
                    cart.setId(rs.getInt("id"));
                    cart.setUserId(rs.getInt("user_id"));
                    cart.setProductId(rs.getInt("product_id"));
                    cart.setQuantity(rs.getInt("quantity"));
                    System.out.println("\u001B[34mCart Item: " + rs.getString("p_name") + ", Quantity: " + rs.getInt("quantity") + ", Price: $" + rs.getDouble("price") + "\u001B[0m"); // Debug
                    cartItems.add(cart);
                }
            }
        }
        return cartItems;
    }

    public boolean removeFromCart(int cartId) throws SQLException {
        String sql = "DELETE FROM carts WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cartId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                conn.commit(); // Commit the deletion
                return true;
            }
            return false;
        }
    }
}