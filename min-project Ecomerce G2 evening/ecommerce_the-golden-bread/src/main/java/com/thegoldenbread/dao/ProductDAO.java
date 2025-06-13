package main.java.com.thegoldenbread.dao;

import main.java.com.thegoldenbread.dto.Product;
import main.java.com.thegoldenbread.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    private final Connection conn;

    public ProductDAO(Connection conn) {
        this.conn = conn;
    }

    public List<Product> getProductsPaginated(long lastId, int limit) throws SQLException {
        String sql = "SELECT id, p_name AS name, price, qty, category, p_uuid AS uuid FROM products WHERE id > ? AND is_deleted = false ORDER BY id LIMIT ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, lastId);
            stmt.setInt(2, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Product> products = new ArrayList<>();
                while (rs.next()) {
                    Product p = new Product(rs.getString("name"), rs.getDouble("price"), rs.getInt("qty"), rs.getString("category"));
                    p.setId((int) rs.getLong("id"));
                    p.setUuid(rs.getString("uuid"));
                    products.add(p);
                }
                return products;
            }
        }
    }

    public Product getProductByUuid(String uuid) throws SQLException {
        String sql = "SELECT id, p_name AS name, price, qty, category, p_uuid AS uuid FROM products WHERE p_uuid = ? AND is_deleted = false";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Product p = new Product(rs.getString("name"), rs.getDouble("price"), rs.getInt("qty"), rs.getString("category"));
                    p.setId((int) rs.getLong("id"));
                    p.setUuid(rs.getString("uuid"));
                    return p;
                }
                return null;
            }
        }
    }

    public Product getProductById(long id) throws SQLException {
        String sql = "SELECT id, p_name AS name, price, qty, category, p_uuid AS uuid FROM products WHERE id = ? AND is_deleted = false";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Product p = new Product(rs.getString("name"), rs.getDouble("price"), rs.getInt("qty"), rs.getString("category"));
                    p.setId((int) rs.getLong("id"));
                    p.setUuid(rs.getString("uuid"));
                    return p;
                }
                return null;
            }
        }
    }

    public List<Product> searchProductsByName(String name) throws SQLException {
        String sql = "SELECT id, p_name AS name, price, qty, category, p_uuid AS uuid FROM products WHERE p_name ILIKE ? AND is_deleted = false";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + name + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                List<Product> products = new ArrayList<>();
                while (rs.next()) {
                    Product p = new Product(rs.getString("name"), rs.getDouble("price"), rs.getInt("qty"), rs.getString("category"));
                    p.setId((int) rs.getLong("id"));
                    p.setUuid(rs.getString("uuid"));
                    products.add(p);
                }
                return products;
            }
        }
    }

    public List<Product> searchProductsByCategoryAndName(String category, String namePrefix) throws SQLException {
        String sql = "SELECT id, p_name AS name, price, qty, category, p_uuid AS uuid FROM products WHERE category ILIKE ? AND p_name ILIKE ? AND is_deleted = false";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + category + "%");
            stmt.setString(2, namePrefix + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                List<Product> products = new ArrayList<>();
                while (rs.next()) {
                    Product p = new Product(rs.getString("name"), rs.getDouble("price"), rs.getInt("qty"), rs.getString("category"));
                    p.setId((int) rs.getLong("id"));
                    p.setUuid(rs.getString("uuid"));
                    products.add(p);
                }
                return products;
            }
        }
    }

    public void updateProduct(Product product) throws SQLException {
        String sql = "UPDATE products SET p_name = ?, price = ?, qty = ?, category = ?, p_uuid = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.getName());
            stmt.setDouble(2, product.getPrice());
            stmt.setInt(3, product.getQty());
            stmt.setString(4, product.getCategory());
            stmt.setString(5, product.getUuid());
            stmt.setLong(6, product.getId());
            stmt.executeUpdate();
            // Notify changes
            try (Statement stmtNotify = conn.createStatement()) {
                stmtNotify.execute("NOTIFY product_updates");
            }
        }
    }

    public void batchInsertProducts(List<Product> products, int batchSize) throws SQLException {
        String sql = "INSERT INTO products (p_name, price, qty, category, p_uuid, is_deleted) VALUES (?, ?, ?, ?, ?, false)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < products.size(); i++) {
                Product p = products.get(i);
                stmt.setString(1, p.getName());
                stmt.setDouble(2, p.getPrice());
                stmt.setInt(3, p.getQty());
                stmt.setString(4, p.getCategory());
                stmt.setString(5, p.getUuid());
                stmt.addBatch();
                if ((i + 1) % batchSize == 0 || i == products.size() - 1) {
                    stmt.executeBatch();
                    // Notify changes after each batch
                    try (Statement stmtNotify = conn.createStatement()) {
                        stmtNotify.execute("NOTIFY product_updates");
                    }
                }
            }
        }
    }

    // Method to start listening for notifications (to be called from controller)
    public void startNotificationListener(Runnable onUpdate) throws SQLException {
        new Thread(() -> {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("LISTEN product_updates");
                while (!Thread.currentThread().isInterrupted()) {
                    java.sql.DatabaseMetaData md = conn.getMetaData();
                    if (md.getConnection() == null) break; // Handle connection closure
                    conn.getNetworkTimeout(); // Ensure connection is alive
                    java.sql.SQLWarning warn = conn.getWarnings();
                    if (warn != null) {
                        System.out.println("\u001B[31mWarning: " + warn.getMessage() + "\u001B[0m");
                        conn.clearWarnings();
                    }
                    java.sql.ResultSet rs = stmt.getResultSet();
                    if (rs == null) {
                        try {
                            conn.createStatement().executeQuery("SELECT 1"); // Ping to keep connection alive
                        } catch (SQLException e) {
                            System.out.println("\u001B[31mConnection lost: " + e.getMessage() + "\u001B[0m");
                            break;
                        }
                    }
                    java.sql.SQLWarning warning = conn.getWarnings();
                    if (warning != null) {
                        System.out.println("\u001B[31mWarning: " + warning.getMessage() + "\u001B[0m");
                        conn.clearWarnings();
                    }
                    if (conn.getTransactionIsolation() != Connection.TRANSACTION_READ_COMMITTED) {
                        conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                    }
                    java.sql.DatabaseMetaData meta = conn.getMetaData();
                    if (meta == null) break; // Handle metadata issues
                    try {
                        conn.createStatement().execute("SELECT 1"); // Keep connection alive
                    } catch (SQLException e) {
                        System.out.println("\u001B[31mConnection check failed: " + e.getMessage() + "\u001B[0m");
                        break;
                    }
                    java.sql.ResultSet rs2 = stmt.getResultSet();
                    if (rs2 == null) {
                        try {
                            Thread.sleep(1000); // Wait before next check
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                        continue;
                    }
                    if (stmt.getMoreResults()) {
                        onUpdate.run(); // Trigger update when notification received
                    }
                }
            } catch (SQLException e) {
                System.out.println("\u001B[31mNotification listener error: " + e.getMessage() + "\u001B[0m");
            }
        }).start();
    }
}