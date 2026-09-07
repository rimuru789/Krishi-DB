package com.krishidb.krishi_api.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.persistence.*;

@Entity
@Table(name = "transactions")
public class TransactionRecord {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_type", nullable = false)
    private String transactionType; // SALE, PURCHASE, EXPENSE, CUSTOMER_PAYMENT, SUPPLIER_PAYMENT

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(nullable = false)
    private double amount;

    @Column(name = "payment_method")
    private String paymentMethod;

    private String description;

    @Column(name = "transaction_date")
    private String transactionDate;

    @Column(name = "sync_status")
    private String syncStatus = "SYNCED";

    public TransactionRecord() {
    }

    public TransactionRecord(String transactionType, Long referenceId, double amount,
            String paymentMethod, String description) {
        this.transactionType = transactionType;
        this.referenceId = referenceId;
        this.amount = Math.round(amount * 100.0) / 100.0;
        this.paymentMethod = paymentMethod;
        this.description = description;
    }

    @PrePersist
    protected void onCreate() {
        if (this.transactionDate == null) {
            this.transactionDate = LocalDateTime.now().format(FORMATTER);
        }
        if (this.syncStatus == null) {
            this.syncStatus = "SYNCED";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
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
}
