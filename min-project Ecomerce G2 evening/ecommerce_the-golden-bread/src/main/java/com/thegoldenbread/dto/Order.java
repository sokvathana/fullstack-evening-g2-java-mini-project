package main.java.com.thegoldenbread.dto;
import java.util.List;
import java.sql.Timestamp;

//public class Order {
//    private int id;
//    private int userId;
//    private String orderDate;
//    private String orderCode;
//    private double totalPrice;
//    private String deliveryAddress;
//    private List<OrderProduct> items;
//
//    public Order() {}
//    public Order(int userId, String deliveryAddress) {
//        this.userId = userId;
//        this.deliveryAddress = deliveryAddress;
//        this.orderDate = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
//        this.orderCode = java.util.UUID.randomUUID().toString().substring(0, 8);
//        this.totalPrice = 0.0;
//    }
//
//    public int getId() { return id; }
//    public void setId(int id) { this.id = id; }
//    public int getUserId() { return userId; }
//    public void setUserId(int userId) { this.userId = userId; }
//    public String getOrderDate() { return orderDate; }
//    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }
//    public String getOrderCode() { return orderCode; }
//    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }
//    public double getTotalPrice() { return totalPrice; }
//    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
//    public String getDeliveryAddress() { return deliveryAddress; }
//    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
//    public List<OrderProduct> getItems() { return items; }
//    public void setItems(List<OrderProduct> items) { this.items = items; }
//}

public class Order {
    private int id;
    private int userId;
    private Timestamp orderDate;
    private String orderCode;
    private double totalPrice;
    private String deliveryAddress;
    private List<OrderProduct> items;

    public Order() {}
    public Order(int userId, String deliveryAddress) {
        this.userId = userId;
        this.deliveryAddress = deliveryAddress;
        this.orderDate = new Timestamp(System.currentTimeMillis());
        this.orderCode = java.util.UUID.randomUUID().toString().substring(0, 8);
        this.totalPrice = 0.0;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public Timestamp getOrderDate() { return orderDate; }
    public void setOrderDate(Timestamp orderDate) { this.orderDate = orderDate; }
    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public List<OrderProduct> getItems() { return items; }
    public void setItems(List<OrderProduct> items) { this.items = items; }
}