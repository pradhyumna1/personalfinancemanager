package com.example.pfm_dashboard.dto;

public class AccountBankDTO {
    private String accountName;
    private String bankName;

    public AccountBankDTO(String accountName, String bankName) {
        this.accountName = accountName;
        this.bankName = bankName;
    }

    // Getters and Setters
    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
}
