package com.example.pfm_dashboard.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_id", nullable = false, unique = true)
    private String itemId; // Unique identifier for the Item from Plaid

    @Column(name = "access_token", nullable = false)
    private String accessToken; // Access token associated with the Item for API access

    @Column(name = "bank_name")
    private String bankName; // Name of the financial institution

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore // Prevent recursive serialization of the user
    private User user;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // Prevent recursive serialization of transactions
    private List<SimpleTransaction> transactions;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // Prevent recursive serialization of accounts
    private List<Account> accounts;

    @Column(name = "transaction_cursor")
    private String transactionCursor; // Cursor for transaction pagination

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<SimpleTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<SimpleTransaction> transactions) {
        this.transactions = transactions;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public String getTransactionCursor() {
        return transactionCursor;
    }

    public void setTransactionCursor(String transactionCursor) {
        this.transactionCursor = transactionCursor;
    }
}
