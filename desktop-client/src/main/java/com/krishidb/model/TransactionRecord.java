package com.krishidb.model;

public class TransactionRecord {

    private int id;
    private String transactionType; // SALE, PURCHASE, EXPENSE, CUSTOMER_PAYMENT, SUPPLIER_PAYMENT
    private Integer referenceId;
    private double amount;
    private String paymentMethod;
    private String description;
    private String transactionDate;
    private String syncStatus;

    public TransactionRecord() {
        this.paymentMethod = "CASH";
        this.syncStatus = "PENDING";
    }

    public TransactionRecord(String transactionType, Integer referenceId, double amount,
                             String paymentMethod, String description) {
        this.transactionType = transactionType;
        this.referenceId = referenceId;
        this.amount = Math.round(amount * 100.0) / 100.0;
        this.paymentMethod = paymentMethod != null ? paymentMethod : "CASH";
        this.description = description;
        this.syncStatus = "PENDING";
    }

    public TransactionRecord(int id, String transactionType, Integer referenceId, double amount,
                             String paymentMethod, String description, String transactionDate,
                             String syncStatus) {
        this.id = id;
        this.transactionType = transactionType;
        this.referenceId = referenceId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.description = description;
        this.transactionDate = transactionDate;
        this.syncStatus = syncStatus;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public Integer getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Integer referenceId) {
        this.referenceId = referenceId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = Math.round(amount * 100.0) / 100.0;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }

    public boolean isInflow() {
        return "SALE".equalsIgnoreCase(transactionType) || "CUSTOMER_PAYMENT".equalsIgnoreCase(transactionType);
    }

    @Override
    public String toString() {
        return "TransactionRecord{" +
                "id=" + id +
                ", transactionType='" + transactionType + '\'' +
                ", referenceId=" + referenceId +
                ", amount=" + amount +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", description='" + description + '\'' +
                ", transactionDate='" + transactionDate + '\'' +
                ", syncStatus='" + syncStatus + '\'' +
                '}';
    }
}
