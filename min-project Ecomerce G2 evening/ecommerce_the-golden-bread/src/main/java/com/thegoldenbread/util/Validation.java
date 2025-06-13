package main.java.com.thegoldenbread.util;

public class Validation {
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) return false;
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) return false;
        return true; // Expand with more rules if needed
    }
}