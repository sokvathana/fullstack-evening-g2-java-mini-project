package main.java.com.thegoldenbread.dto;

public class Product {
    private int id;
    private String name;
    private double price;
    private int qty;
    private boolean isDeleted;
    private String uuid;
    private String category;

    public Product() {}
    public Product(String name, double price, int qty, String category) {
        this.name = name;
        this.price = price;
        this.qty = qty;
        this.category = category;
        this.isDeleted = false;
        this.uuid = java.util.UUID.randomUUID().toString();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { this.isDeleted = deleted; }
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}