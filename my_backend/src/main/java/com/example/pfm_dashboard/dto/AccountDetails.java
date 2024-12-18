package com.example.pfm_dashboard.dto;

import java.math.BigDecimal;

public class AccountDetails {

    private String institutionName; // Name of the financial institution
    private String accountName;    // Name of the account (e.g., Checking Account)
    private String accountType;    // High-level type (e.g., depository, credit)
    private String accountSubtype; // Specific subtype (e.g., checking, savings)
    private BigDecimal availableBalance; // Available balance
    private BigDecimal currentBalance;   // Current balance
    private String accountId;    // Unique identifier for the account from Plaid

    // Updated Constructor
    public AccountDetails(String institutionName, String accountName, String accountType, String accountSubtype,
                          BigDecimal availableBalance, BigDecimal currentBalance, String accountId) {
        this.institutionName = institutionName;
        this.accountName = accountName;
        this.accountType = accountType;
        this.accountSubtype = accountSubtype;
        this.availableBalance = availableBalance;
        this.currentBalance = currentBalance;
        this.accountId = accountId;
    }

    // Getters and Setters
    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getAccountSubtype() {
        return accountSubtype;
    }

    public void setAccountSubtype(String accountSubtype) {
        this.accountSubtype = accountSubtype;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
}
