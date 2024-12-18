package com.example.pfm_dashboard.model;

import jakarta.persistence.*;
import java.time.YearMonth;
import com.example.pfm_dashboard.converter.YearMonthConverter;

@Entity
@Table(name = "budgets")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category; // Relationship to the Category model

    @Column(nullable = false)
    private Double amount;


    @Column(nullable = false)
    private Double spent = 0.0; // Default to 0.0

    @Column(nullable = false)
    private Double originalSpent = 0.0;

    @Column(nullable = false)
    @Convert(converter = YearMonthConverter.class)
    private YearMonth month; // Month of the budget

    public boolean isExceeded() {
        return spent > amount;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getSpent() {
        return spent;
    }

    public void setSpent(Double spent) {
        this.spent = spent;
    }

    public Double getOriginalSpent() {
        return originalSpent;
    }

    public void setOriginalSpent(Double originalSpent) {
        this.originalSpent = originalSpent;
    }

    public YearMonth getMonth() {
        return month;
    }

    public void setMonth(YearMonth month) {
        this.month = month;
    }
}
