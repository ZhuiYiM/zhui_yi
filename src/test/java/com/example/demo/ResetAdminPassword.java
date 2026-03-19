package com.example.demo;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class ResetAdminPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String rawPassword = "admin123";
        String encodedPassword = encoder.encode(rawPassword);
        
        System.out.println("Raw password: " + rawPassword);
        System.out.println("Generated hash: " + encodedPassword);
        System.out.println("Test match: " + encoder.matches(rawPassword, encodedPassword));
        
        // Test with database hash
        String dbHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iDJfYR5sYpF5qE9qQhN7sP8xGK2C";
        System.out.println("\nDatabase hash: " + dbHash);
        System.out.println("DB hash match: " + encoder.matches(rawPassword, dbHash));
        
        // Output SQL command
        System.out.println("\n=== SQL Command ===");
        System.out.println("UPDATE admin_user SET password = '" + encodedPassword + "' WHERE username = 'admin';");
    }
}
