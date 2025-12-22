// src/main/java/com/cinema/models/dto/BankInfoDTO.java
package com.cinema.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BankInfoDTO {
    @JsonProperty("bank_name")
    private String bankName;
    
    @JsonProperty("bank_account_holder")
    private String bankAccountHolder;
    
    @JsonProperty("bank_account_number")
    private String bankAccountNumber;
    
    @JsonProperty("bank_branch")
    private String bankBranch;
    
    @JsonProperty("bank_qr_template")
    private String bankQrTemplate;

    // Constructors
    public BankInfoDTO() {}

    // Getters & Setters
    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankAccountHolder() {
        return bankAccountHolder;
    }

    public void setBankAccountHolder(String bankAccountHolder) {
        this.bankAccountHolder = bankAccountHolder;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public String getBankBranch() {
        return bankBranch;
    }

    public void setBankBranch(String bankBranch) {
        this.bankBranch = bankBranch;
    }

    public String getBankQrTemplate() {
        return bankQrTemplate;
    }

    public void setBankQrTemplate(String bankQrTemplate) {
        this.bankQrTemplate = bankQrTemplate;
    }
}