package com.cinema.models;

public class PaymentInfo {
    private String bankName;
    private String accountHolder;
    private String accountNumber;
    private String transferContent;
    private String qrCodeUrl; // URL hoặc path đến QR code image
    private double amount;

    public PaymentInfo() {
    }

    public PaymentInfo(String bankName, String accountHolder, String accountNumber, 
                       String transferContent, String qrCodeUrl, double amount) {
        this.bankName = bankName;
        this.accountHolder = accountHolder;
        this.accountNumber = accountNumber;
        this.transferContent = transferContent;
        this.qrCodeUrl = qrCodeUrl;
        this.amount = amount;
    }

    // Getters and Setters
    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    public void setAccountHolder(String accountHolder) {
        this.accountHolder = accountHolder;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getTransferContent() {
        return transferContent;
    }

    public void setTransferContent(String transferContent) {
        this.transferContent = transferContent;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getFormattedAmount() {
        return String.format("%,.0f đ", amount);
    }

    public String getFormattedAccountNumber() {
        // Format: 9999 8888 7777
        return accountNumber.replaceAll("(\\d{4})(?=\\d)", "$1 ");
    }
}