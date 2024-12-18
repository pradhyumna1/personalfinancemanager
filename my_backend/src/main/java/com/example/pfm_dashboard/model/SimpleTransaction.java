package com.example.pfm_dashboard.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
public class SimpleTransaction {

    @Id
    @Column(name = "transaction_id", nullable = false, unique = true)
    private String transactionId;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "original_amount", nullable = false)
    private BigDecimal originalAmount; // Retains the original value

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    @JsonIgnore 
    private Category category; // New normalized category relationship

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "original_category_id", referencedColumnName = "category_id")
    @JsonIgnore 
    private Category originalCategory; // To store the original category

    @Column(name = "transaction_date", nullable = false)
    private LocalDate date;

    @Column(name = "merchant_name")
    private String merchantName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", referencedColumnName = "item_id")
    @JsonBackReference // Handle bidirectional relationship with Item
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonBackReference
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", referencedColumnName = "account_id")
    @JsonBackReference
    private Account account;

    // Default constructor
    public SimpleTransaction() {}

    // Parameterized constructor
    public SimpleTransaction(String transactionId, BigDecimal amount, BigDecimal originalAmount, Category category, Category originalCategory, LocalDate date, String merchantName, Item item, User user, Account account) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.originalAmount = originalAmount;
        this.category = category;
        this.originalCategory = originalCategory;
        this.date = date;
        this.merchantName = merchantName;
        this.item = item;
        this.user = user;
        this.account = account;
    }

    // Getters and Setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Category getOriginalCategory() {
        return originalCategory;
    }

    public void setOriginalCategory(Category originalCategory) {
        this.originalCategory = originalCategory;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
