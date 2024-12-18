package com.example.pfm_dashboard.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "categories", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name", "user_id"})
})
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "color", nullable = false) // New color column
    private String color;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "category", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = false)
    @JsonIgnore // Break infinite recursion for this relationship
    private Set<SimpleTransaction> transactions = new HashSet<>();

    @OneToMany(mappedBy = "originalCategory", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = false)
    @JsonIgnore // Break infinite recursion for this relationship
    private Set<SimpleTransaction> originalTransactions = new HashSet<>();

    // Default Constructor
    public Category() {}

    // Parameterized Constructor
    public Category(String name, String color, User user) {
        this.name = name;
        this.color = color;
        this.user = user;
    }

    // Getters and Setters
    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.toLowerCase().trim();
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<SimpleTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(Set<SimpleTransaction> transactions) {
        this.transactions = transactions;
    }

    public Set<SimpleTransaction> getOriginalTransactions() {
        return originalTransactions;
    }

    public void setOriginalTransactions(Set<SimpleTransaction> originalTransactions) {
        this.originalTransactions = originalTransactions;
    }
}
