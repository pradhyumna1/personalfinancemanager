package com.example.pfm_dashboard.dto;

import com.example.pfm_dashboard.model.SimpleTransaction;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.example.pfm_dashboard.model.Category;

public class TransactionDTO {
    private String transactionId;
    private BigDecimal amount;
    private BigDecimal originalAmount; 
    @JsonIgnore
    private String accountId;
    private String accountName; // New field for account name
    private Category category;
    private Category originalCategory;
    private LocalDate date;
    private String merchantName;
    private String bankName;

    // Constructor to populate DTO
    public TransactionDTO(SimpleTransaction transaction) {
        this.transactionId = transaction.getTransactionId();
        this.amount = transaction.getAmount();
        this.originalAmount = transaction.getOriginalAmount();
        this.accountName = transaction.getAccount() != null ? transaction.getAccount().getName() : "Unknown Account"; // Extract account name
        this.category = transaction.getCategory();
        this.originalCategory = transaction.getOriginalCategory();
        this.date = transaction.getDate();
        this.merchantName = transaction.getMerchantName();
        this.bankName = transaction.getAccount() != null && transaction.getAccount().getItem() != null 
                ? transaction.getAccount().getItem().getBankName() 
                : "Unknown Bank"; // Extract bank name
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

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
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

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
}
