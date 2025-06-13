package main.java.com.thegoldenbread.controller;

import main.java.com.thegoldenbread.dao.OrderDAO;
import main.java.com.thegoldenbread.dao.ProductDAO;
import main.java.com.thegoldenbread.dao.UserDAO;
import main.java.com.thegoldenbread.dao.CartDAO;
import main.java.com.thegoldenbread.dto.Order;
import main.java.com.thegoldenbread.dto.OrderProduct;
import main.java.com.thegoldenbread.dto.Product;
import main.java.com.thegoldenbread.dto.User;
import main.java.com.thegoldenbread.dto.Cart;
import main.java.com.thegoldenbread.util.DBConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class GoldenBreadController {
    private final UserDAO userDAO;
    private final ProductDAO productDAO;
    private final OrderDAO orderDAO;
    private final CartDAO cartDAO;
    private final Connection conn;
    private User currentUser = null;

    public GoldenBreadController() throws SQLException {
        conn        = DBConnection.getConnection();
        userDAO     = new UserDAO(conn);
        productDAO  = new ProductDAO(conn);
        orderDAO    = new OrderDAO(conn);
        cartDAO     = new CartDAO(conn);
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            if (currentUser == null) {
                System.out.println("\u001B[32mWelcome to The Golden Bread - Please Login or Register\u001B[0m");
                System.out.println("\u001B[32m1. Login\u001B[0m");
                System.out.println("\u001B[32m2. Register\u001B[0m");
                System.out.println("\u001B[32m3. Insert 10M Products\u001B[0m");
                System.out.println("\u001B[32m4. Read 10M Products\u001B[0m");
                System.out.println("\u001B[31m5. Exit\u001B[0m");
                int choice = getValidIntInput(scanner, "Enter choice: ");
                scanner.nextLine();
                if (choice == 1) login(scanner);
                else if (choice == 2) register(scanner);
                else if (choice == 3) insertTenMillionProducts(scanner);
                else if (choice == 4) readTenMillionProducts(scanner);
                else if (choice == 5) break;
            } else {
                System.out.println("\u001B[34mWelcome to The Golden Bread, " + currentUser.getUsername() + "\u001B[0m");
                System.out.println("\u001B[32m1. View All Products\u001B[0m");
                System.out.println("\u001B[32m2. Search Products by Name\u001B[0m");
                System.out.println("\u001B[32m3. Search by Category and Name Prefix\u001B[0m");
                System.out.println("\u001B[32m4. Add Product to Cart\u001B[0m");
                System.out.println("\u001B[32m5. View Cart\u001B[0m");
                System.out.println("\u001B[32m6. Place Order\u001B[0m");
                System.out.println("\u001B[31m7. Logout\u001B[0m");
                int choice = getValidIntInput(scanner, "Enter choice: ");
                scanner.nextLine();
                if (choice == 1) viewProducts();
                else if (choice == 2) searchByName(scanner);
                else if (choice == 3) searchByCategoryAndName(scanner);
                else if (choice == 4) addToCart(scanner);
                else if (choice == 5) viewCart();
                else if (choice == 6) placeOrder(scanner);
                else if (choice == 7) {
                    currentUser = null;
                    System.out.println("\u001B[33mLogged out successfully\u001B[0m");
                }
            }
        }
        scanner.close();
        try {
            if (conn != null && !conn.isClosed()) conn.close();
        } catch (SQLException e) {
            System.out.println("\u001B[31mError closing connection: " + e.getMessage() + "\u001B[0m");
        }
    }

    private void login(Scanner scanner) {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("\u001B[31mUsername and password cannot be empty\u001B[0m");
            return;
        }
        try {
            User user = userDAO.login(username, password);
            if (user != null) {
                currentUser = user;
                System.out.println("\u001B[32mLogin successful - Enjoy The Golden Bread!\u001B[0m");
            } else {
                System.out.println("\u001B[31mInvalid username or password\u001B[0m");
            }
        } catch (SQLException e) {
            System.out.println("\u001B[31mDatabase error: " + e.getMessage() + "\u001B[0m");
        }
    }

//    private void register(Scanner scanner) {
//        System.out.print("Enter username: ");
//        String username = scanner.nextLine();
//        System.out.print("Enter email: ");
//        String email = scanner.nextLine();
//        System.out.print("Enter password: ");
//        String password = scanner.nextLine();
//        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
//            System.out.println("\u001B[31mAll fields are required\u001B[0m");
//            return;
//        }
//        User user = new User(username, email, password);
//        System.out.println("\u001B[34mRegistering: Username=" + user.getUsername() + ", Email=" + user.getEmail() + ", UUID=" + user.getUuid() + "\u001B[0m");
//        try {
//            if (userDAO.register(user)) {
//                System.out.println("\u001B[32mRegistration successful - Welcome to The Golden Bread!\u001B[0m");
//            } else {
//                System.out.println("\u001B[31mRegistration failed\u001B[0m");
//            }
//        } catch (SQLException e) {
//            System.out.println("\u001B[31mDatabase error during registration: " + e.getMessage() + "\u001B[0m");
//        }
//    }

    private void register(Scanner scanner) {
        String username, email, password;
        while (true) {
            System.out.print("Enter username: ");
            username = scanner.nextLine().trim();
            System.out.print("Enter email: ");
            email = scanner.nextLine().trim();
            System.out.print("Enter password: ");
            password = scanner.nextLine().trim();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                System.out.println("\u001B[31mAll fields are required\u001B[0m");
                continue;
            }

            String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
            if (!email.matches(emailRegex)) {
                System.out.println("\u001B[31mInvalid email format. Please use a valid email address (e.g., user@example.com)\u001B[0m");
                continue;
            }
            break;
        }

        User user = new User(username, email, password);
        System.out.println("\u001B[34mRegistering: Username=" + user.getUsername() + ", Email=" + user.getEmail() + ", UUID=" + user.getUuid() + "\u001B[0m");
        try {
            if (userDAO.register(user)) {
                System.out.println("\u001B[32mRegistration successful - Welcome to The Golden Bread!\u001B[0m");
            } else {
                System.out.println("\u001B[31mRegistration failed\u001B[0m");
            }
        } catch (SQLException e) {
            System.out.println("\u001B[31mDatabase error during registration: " + e.getMessage() + "\u001B[0m");
        }
    }

    private int getTotalProductCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM products WHERE is_deleted = false";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    private void viewProducts() {
        try {
            int pageSize = 10000; // Default page size
            int totalRecords = getTotalProductCount();
            int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
            long startTime = System.currentTimeMillis();
            Scanner scanner = new Scanner(System.in);

            System.out.println("\u001B[34mAutomatically viewing all products. Press 'q' at any time to quit. Total Records: " + totalRecords + "\u001B[0m");
            long[] pageTimes = new long[Math.min(5, totalPages)]; // Store times for first 5 pages or all if fewer
            final int[] timeIndex = {0};
            final AtomicLong[] lastId = {new AtomicLong()}; // Cursor for keyset pagination

            ExecutorService executor = Executors.newFixedThreadPool(4); // 4 threads for parallel processing
            List<Future<?>> futures = new ArrayList<>();

            for (int page = 0; page < totalPages; page++) {
                int finalPage = page;
                long finalLastId = lastId[0].get();
                Future<?> future = executor.submit(() -> {
                    try {
                        long pageStartTime = System.currentTimeMillis();
                        List<Product> products = productDAO.getProductsPaginated(finalLastId, pageSize);
                        synchronized (System.out) {
                            System.out.println("\u001B[34mPage " + (finalPage + 1) + " of " + totalPages + ":\u001B[0m");
                            displayProducts(products);
                            if (!products.isEmpty()) {
                                lastId[0].set(products.get(products.size() - 1).getId()); // Update cursor
                            }

                            // Calculate percentage and time remaining
                            double percentage = ((double) (finalPage + 1) / totalPages) * 100;
                            long currentTime = System.currentTimeMillis();
                            long elapsedTime = currentTime - startTime;
                            long pageTime = currentTime - pageStartTime;
                            if (finalPage >= 1) {
                                pageTimes[timeIndex[0]] = pageTime;
                                timeIndex[0] = (timeIndex[0] + 1) % pageTimes.length;
                                long totalPageTime = 0;
                                for (long time : pageTimes) {
                                    totalPageTime += time;
                                }
                                long averagePageTime = totalPageTime / Math.min(finalPage + 1, pageTimes.length);
                                long remainingTime = (averagePageTime > 0) ? (long) (averagePageTime * (totalPages - finalPage - 1) / 1000) : 0;
                                System.out.printf("\u001B[34mProgress: %.2f%%, Estimated Time Remaining: %d seconds\u001B[0m%n", percentage, remainingTime);
                            }
                        }
                    } catch (SQLException e) {
                        System.out.println("\u001B[31mDatabase error for page " + (finalPage + 1) + ": " + e.getMessage() + "\u001B[0m");
                    }
                });
                futures.add(future);

                if (page < totalPages - 1) {
                    Thread.sleep(500); // Brief delay to stagger thread execution
                    if (scanner.hasNextLine()) {
                        String input = scanner.nextLine().trim();
                        if ("q".equalsIgnoreCase(input)) {
                            executor.shutdownNow();
                            return;
                        }
                    }
                }
            }

            // Wait for all tasks to complete
            for (Future<?> future : futures) {
                future.get();
            }
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);

            long endTime = System.currentTimeMillis();
            System.out.println("\u001B[32mViewed " + totalRecords + " products in " + (endTime - startTime) / 1000 + " seconds\u001B[0m");
        } catch (SQLException e) {
            System.out.println("\u001B[31mDatabase error: " + e.getMessage() + "\u001B[0m");
        } catch (InterruptedException e) {
            System.out.println("\u001B[31mInterrupted: " + e.getMessage() + "\u001B[0m");
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            System.out.println("\u001B[31mExecution error: " + e.getMessage() + "\u001B[0m");
        }
    }

    private List<Product> searchByName(Scanner scanner) {
        System.out.print("Enter product name to search (required): ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("\u001B[31mProduct name is required. Please try again.\u001B[0m");
            return null;
        }
        try {
            List<Product> products = productDAO.searchProductsByName(name);
            displayProducts(products);
        } catch (SQLException e) {
            System.out.println("\u001B[31mDatabase error: " + e.getMessage() + "\u001B[0m");
        }
        return null;
    }

    private void searchByCategoryAndName(Scanner scanner) {
        System.out.print("Enter category (e.g., 'b' or 'Bread', required): ");
        String category = scanner.nextLine().trim();
        System.out.print("Enter name prefix (e.g., 'P' for products starting with P, required): ");
        String namePrefix = scanner.nextLine().trim();
        if (category.isEmpty() || namePrefix.isEmpty()) {
            System.out.println("\u001B[31mBoth category and name prefix are required. Please try again.\u001B[0m");
            return;
        }
        try {
            List<Product> products = productDAO.searchProductsByCategoryAndName(category, namePrefix);
            displayProducts(products);
        } catch (SQLException e) {
            System.out.println("\u001B[31mDatabase error: " + e.getMessage() + "\u001B[0m");
        }
    }

    private void addToCart(Scanner scanner) {
        try {
            while (true) {
                List<Product> products = searchByName(scanner);
                assert products != null;
                if (products.isEmpty()) {
                    System.out.println("\u001B[33mNo products found or search failed, please try again.\u001B[0m");
                    continue;
                }

                System.out.println("\u001B[34mEnter UUIDs to add to cart (one per line, 'q' or 's' to stop):\u001B[0m");
                List<Cart> cartItems = new ArrayList<>();
                int uuidCount = 0;
                while (true) {
                    String uuid = scanner.nextLine().trim();
                    if (uuid.equalsIgnoreCase("q") || uuid.equalsIgnoreCase("s")) {
                        break;
                    }
                    uuidCount++;
                    if (uuidCount > 1) {
                        System.out.println("\u001B[34mEnter other product UUID:\u001B[0m");
                    }
                    Product product = products.stream()
                            .filter(p -> p.getUuid().equals(uuid))
                            .findFirst()
                            .orElseGet(() -> {
                                try {
                                    return productDAO.getProductByUuid(uuid);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                    if (product != null && product.getQty() > 0) {
                        System.out.print("Enter quantity for " + product.getName() + ": ");
                        int quantity = getValidIntInput(scanner, "Quantity: ");
                        scanner.nextLine(); // Clear the buffer
                        if (quantity > 0 && quantity <= product.getQty()) {
                            cartItems.add(new Cart(currentUser.getId(), product.getId(), quantity));
                            System.out.println("\u001B[32mAdded " + quantity + " x " + product.getName() + " to cart\u001B[0m");
                        } else {
                            System.out.println("\u001B[31mInvalid quantity or insufficient stock for " + product.getName() + "\u001B[0m");
                        }
                    } else {
                        System.out.println("\u001B[31mProduct not found or out of stock for UUID: " + uuid + "\u001B[0m");
                    }
                }

                // Save all cart items to database
                for (Cart cart : cartItems) {
                    if (cartDAO.addToCart(cart)) {
                        Product product = productDAO.getProductById(cart.getProductId()); // Use getProductById instead of getAllProducts
                        if (product != null) {
                            product.setQty(product.getQty() - cart.getQuantity());
                            productDAO.updateProduct(product);
                        }
                    }
                }

                System.out.print("\u001B[34mContinue adding products to cart? (y/ok to continue, any other key to quit): \u001B[0m");
                String continueChoice = scanner.nextLine().trim().toLowerCase();
                if (!continueChoice.equals("y") && !continueChoice.equals("ok")) {
                    break;
                }
            }
            System.out.println("\u001B[32mCart updates saved to database.\u001B[0m");
        } catch (SQLException e) {
            System.out.println("\u001B[31mDatabase error: " + e.getMessage() + "\u001B[0m");
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.out.println("\u001B[31mRollback failed: " + ex.getMessage() + "\u001B[0m");
            }
        }
    }

    private void viewCart() {
        try {
            List<Cart> cartItems = cartDAO.getCartItems(currentUser.getId());
            if (cartItems.isEmpty()) {
                System.out.println("\u001B[33mYour cart is empty\u001B[0m");
            } else {
                System.out.println("\u001B[34mYour Cart at The Golden Bread:\u001B[0m");
                double total = 0;
                for (Cart item : cartItems) {
                    Product product = productDAO.getProductById(item.getProductId()); // Use getProductById
                    if (product != null) {
                        total += product.getPrice() * item.getQuantity();
                        System.out.println("\u001B[36mItem: " + product.getName() + ", Quantity: " + item.getQuantity() + ", Price: $" + product.getPrice() + "\u001B[0m");
                    }
                }
                System.out.println("\u001B[34mTotal: $" + total + "\u001B[0m");
            }
        } catch (SQLException e) {
            System.out.println("\u001B[31mDatabase error: " + e.getMessage() + "\u001B[0m");
        }
    }

    private void placeOrder(Scanner scanner) {
        System.out.print("Enter delivery address: ");
        String deliveryAddress = scanner.nextLine();
        if (deliveryAddress.isEmpty()) {
            System.out.println("\u001B[31mDelivery address is required\u001B[0m");
            return;
        }
        try {
            List<Cart> cartItems = cartDAO.getCartItems(currentUser.getId());
            if (cartItems.isEmpty()) {
                System.out.println("\u001B[31mCart is empty, add items before placing an order\u001B[0m");
                return;
            }
            Order order = new Order(currentUser.getId(), deliveryAddress);
            List<OrderProduct> items = new ArrayList<>();
            double totalPrice = 0;
            for (Cart cartItem : cartItems) {
                Product product = productDAO.getProductById(cartItem.getProductId()); // Use getProductById
                if (product != null && product.getQty() >= cartItem.getQuantity()) {
                    items.add(new OrderProduct(order.getId(), cartItem.getProductId(), cartItem.getQuantity()));
                    totalPrice += product.getPrice() * cartItem.getQuantity();
                    product.setQty(product.getQty() - cartItem.getQuantity());
                    productDAO.updateProduct(product);
                } else {
                    System.out.println("\u001B[31mInsufficient stock for " + product.getName() + "\u001B[0m");
                    return;
                }
            }
            order.setItems(items);
            order.setTotalPrice(totalPrice);
            if (orderDAO.saveOrder(order)) {
                for (Cart item : cartItems) {
                    cartDAO.removeFromCart(item.getId());
                }
                System.out.println("\u001B[32mOrder placed successfully at The Golden Bread!\u001B[0m");
                System.out.println("\u001B[34mOrder Details - Code: " + order.getOrderCode() +
                        ", Date: " + order.getOrderDate() + ", Total: $" + order.getTotalPrice() +
                        ", Address: " + order.getDeliveryAddress() + "\u001B[0m");
            } else {
                System.out.println("\u001B[31mOrder placement failed\u001B[0m");
            }
        } catch (SQLException e) {
            System.out.println("\u001B[31mDatabase error: " + e.getMessage() + "\u001B[0m");
        }
    }

    private void insertTenMillionProducts(Scanner scanner) {
        System.out.print("Enter batch size (e.g., 10000): ");
        int batchSize = getValidIntInput(scanner, "Batch size: ");
        try {
            List<Product> products = new ArrayList<>();
            Random rand = new Random();
            String[] categories = {"Bread", "Pastry", "Cake"};
            String[] names = {"Sourdough", "Croissant", "Baguette", "Muffin", "Cake"};
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < 10_000_000; i++) {
                String name = names[rand.nextInt(names.length)] + " " + rand.nextInt(100);
                double price = 1.99 + (rand.nextDouble() * 10);
                int qty = rand.nextInt(100) + 1;
                String category = categories[rand.nextInt(categories.length)];
                Product p = new Product(name, price, qty, category);
                p.setUuid(java.util.UUID.randomUUID().toString());
                products.add(p);
                if (products.size() >= batchSize) {
                    productDAO.batchInsertProducts(products, batchSize);
                    products.clear();
                    System.out.println("\u001B[32mInserted " + (i + 1) + " products...\u001B[0m");
                }
            }
            if (!products.isEmpty()) {
                productDAO.batchInsertProducts(products, batchSize);
            }
            long endTime = System.currentTimeMillis();
            System.out.println("\u001B[32mInserted 10 million products in " + (endTime - startTime) / 1000 + " seconds\u001B[0m");
            conn.commit();
        } catch (SQLException e) {
            System.out.println("\u001B[31mDatabase error: " + e.getMessage() + "\u001B[0m");
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.out.println("\u001B[31mRollback failed: " + ex.getMessage() + "\u001B[0m");
            }
        }
    }

    private void readTenMillionProducts(Scanner scanner) {
        try {
            int pageSize; // Default page size, adjustable via input
            System.out.print("Enter page size (e.g., 10000, default is 10000): ");
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                pageSize = Integer.parseInt(input);
            } else {
                pageSize = 10000;
            }
            // Consume any remaining newline to prevent blocking
            while (scanner.hasNextLine()) {
                scanner.nextLine();
                break;
            }

            int totalRecords = getTotalProductCount();
            long startTime = System.currentTimeMillis();

            System.out.println("\u001B[34mAutomatically reading " + totalRecords + " products. Press 'q' at any time to quit.\u001B[0m");
            long[] pageTimes = new long[5]; // Sliding window of last 5 page times
            final int[] timeIndex = {0};
            final long[] lastId = {0}; // Cursor for keyset pagination
            boolean isRunning = true;

            ExecutorService executor = Executors.newFixedThreadPool(Math.min(8, Runtime.getRuntime().availableProcessors() * 2)); // Dynamic thread pool
            List<Future<?>> futures = new ArrayList<>();

            while (lastId[0] < totalRecords && isRunning) {
                long finalLastId = lastId[0];
                final int currentTimeIndex = timeIndex[0]; // Final copy for lambda
                Future<?> future = executor.submit(() -> {
                    try {
                        long pageStartTime = System.currentTimeMillis();
                        List<Product> products = productDAO.getProductsPaginated(finalLastId, pageSize);
                        long newLastId = finalLastId;
                        if (!products.isEmpty()) {
                            newLastId = products.get(products.size() - 1).getId();
                        }
                        synchronized (System.out) {
                            // Clear previous line and reprint to simulate auto-refresh
                            System.out.print("\u001B[1A\u001B[2K"); // Move up and clear line
                            System.out.println("\u001B[34mReading products with ID > " + finalLastId + " (Auto-refreshing)\u001B[0m");
                            displayProducts(products);

                            long currentTime = System.currentTimeMillis();
                            long pageTime = currentTime - pageStartTime;
                            long elapsedTime = currentTime - startTime;

                            if (pageTime > 0) {
                                pageTimes[currentTimeIndex] = pageTime;
                                // Update timeIndex and pageTimes outside lambda
                                synchronized (GoldenBreadController.this) {
                                    timeIndex[0] = (timeIndex[0] + 1) % pageTimes.length;
                                }
                                long totalPageTime = 0;
                                for (long time : pageTimes) {
                                    totalPageTime += time;
                                }
                                long averagePageTime = totalPageTime / Math.min(currentTimeIndex + 1, pageTimes.length);
                                double percentage = ((double) newLastId / totalRecords) * 100;
                                long remainingTime = (averagePageTime > 0) ? (long) ((totalRecords - newLastId) * averagePageTime / (pageSize * 1000)) : 0;
                                System.out.printf("\u001B[34mProgress: %.2f%%, Elapsed: %d seconds, Estimated Remaining: %d seconds\u001B[0m%n",
                                        percentage, elapsedTime / 1000, remainingTime);
                            }
                        }
                        synchronized (this) {
                            lastId[0] = newLastId;
                        }
                    } catch (SQLException e) {
                        System.out.println("\u001B[31mDatabase error: " + e.getMessage() + "\u001B[0m");
                    }
                });
                futures.add(future);

                // Non-blocking quit check with a small delay
                if (System.in.available() > 0) {
                    String quitInput = scanner.nextLine().trim();
                    if ("q".equalsIgnoreCase(quitInput)) {
                        isRunning = false;
                        executor.shutdownNow();
                        return;
                    }
                }

                Thread.sleep(100); // Controlled refresh rate (100ms) to avoid flooding
            }

            // Wait for all tasks to complete
            for (Future<?> future : futures) {
                future.get();
            }
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);

            long endTime = System.currentTimeMillis();
            System.out.println("\u001B[32mRead " + totalRecords + " products in " + (endTime - startTime) / 1000 + " seconds\u001B[0m");
        } catch (SQLException e) {
            System.out.println("\u001B[31mDatabase error: " + e.getMessage() + "\u001B[0m");
        } catch (InterruptedException e) {
            System.out.println("\u001B[31mInterrupted: " + e.getMessage() + "\u001B[0m");
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            System.out.println("\u001B[31mExecution error: " + e.getMessage() + "\u001B[0m");
        } catch (java.io.IOException e) {
            System.out.println("\u001B[31mIO error: " + e.getMessage() + "\u001B[0m");
        }
    }

    private void displayProducts(List<Product> products) {
        if (products.isEmpty()) {
            System.out.println("\u001B[33mNo products found\u001B[0m");
        } else {
            System.out.println("\u001B[34mThe Golden Bread Menu:\u001B[0m");
            for (Product p : products) {
                System.out.println("\u001B[36mID: " + p.getId() + ", Name: " + p.getName() +
                        ", Price: $" + p.getPrice() + ", Qty: " + p.getQty() +
                        ", Category: " + p.getCategory() + ", UUID: " + p.getUuid() + "\u001B[0m");
            }
        }
    }

    private int getValidIntInput(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            if (scanner.hasNextInt()) {
                return scanner.nextInt();
            } else {
                System.out.println("\u001B[31mPlease enter a valid number\u001B[0m");
                scanner.next();
            }
        }
    }

    public static void main(String[] args) {
        try {
            new GoldenBreadController().start();
        } catch (SQLException e) {
            System.out.println("\u001B[31mThe Golden Bread application failed to start: " + e.getMessage() + "\u001B[0m");
        }
    }
}


