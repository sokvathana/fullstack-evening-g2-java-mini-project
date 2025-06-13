package main.java.com.thegoldenbread.dao;

import main.java.com.thegoldenbread.dto.Order;
import main.java.com.thegoldenbread.dto.OrderProduct;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {
    private Connection conn;

    public OrderDAO(Connection conn) {
        this.conn = conn;
    }

    public boolean saveOrder(Order order) throws SQLException {
        String sql = "INSERT INTO orders (user_id, order_date, order_code, total_price, delivery_address) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, order.getUserId());
            stmt.setTimestamp(2, order.getOrderDate()); // Changed to setTimestamp
            stmt.setString(3, order.getOrderCode());
            stmt.setDouble(4, order.getTotalPrice());
            stmt.setString(5, order.getDeliveryAddress());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    order.setId(rs.getInt(1));
                    List<OrderProduct> items = order.getItems();
                    if (items != null) {
                        for (OrderProduct item : items) {
                            addOrderProduct(order.getId(), item.getProductId(), item.getQuantity());
                        }
                    }
                }
                return true;
            }
            return false;
        }
    }

    public boolean addOrderProduct(int orderId, int productId, int quantity) throws SQLException {
        String sql = "INSERT INTO order_products (order_id, product_id, quantity) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            stmt.setInt(2, productId);
            stmt.setInt(3, quantity);
            return stmt.executeUpdate() > 0;
        }
    }
}
