package com.storemanagement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "users") // Names the table 'users' in PostgreSQL
public class User {

    @Id // Marks this as the Primary Key
    @Column(name = "user_id")
    private String id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @Column(name = "role", nullable = false)
    private String role; // "ADMIN" or "EMPLOYEE"

    // Constructor
    public User() {
        // Hibernate requires a no-argument constructor
    }

    public User(String username, String password, String role) {
        this.id = "usr_" + UUID.randomUUID().toString().substring(0, 8); // Generates a clean ID
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Getters and Setters (Required for Hibernate to read/write data)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}