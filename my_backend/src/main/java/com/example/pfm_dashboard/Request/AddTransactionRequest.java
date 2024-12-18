package com.example.pfm_dashboard.Request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;

public class AddTransactionRequest {
    private String username;
    private String merchantName;
    private BigDecimal amount;
    private String accountName;

    @JsonProperty("category") // Map the "category" JSON key to this field
    private String categoryName;

    private LocalDate date;

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    @Override
    public String toString() {
        return "AddTransactionRequest{" +
                "username='" + username + '\'' +
                ", merchantName='" + merchantName + '\'' +
                ", amount=" + amount +
                ", accountName='" + accountName + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", date=" + date +
                '}';
    }
}
