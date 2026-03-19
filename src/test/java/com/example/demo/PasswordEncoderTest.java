package com.example.demo;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Generate BCrypt hash for admin123
        String rawPassword = "admin123";
        String encodedPassword = encoder.encode(rawPassword);
        
        System.out.println("Raw password: " + rawPassword);
        System.out.println("BCrypt hash: " + encodedPassword);
        System.out.println("Password matches: " + encoder.matches(rawPassword, encodedPassword));
        
        // Test database password
        String dbPassword = "$2a$10$rKOxq9Z8g7Y6h5J4K3L2M.Nzmdr9k7uOCQb376NoUnuTJ8iDJfYR5";
        System.out.println("\nDatabase password: " + dbPassword);
        System.out.println("Database password matches: " + encoder.matches(rawPassword, dbPassword));
        
        // Generate new password hash for SQL update
        System.out.println("\n=== New Password Hash (for SQL update) ===");
        System.out.println(encodedPassword);
    }
}
