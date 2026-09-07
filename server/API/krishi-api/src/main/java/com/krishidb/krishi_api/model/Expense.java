package com.krishidb.krishi_api.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.persistence.*;

@Entity
@Table(name = "expenses")
public class Expense {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String category;

    private String description;

    @Column(nullable = false)
    private double amount;

    @Column(name = "payment_method")
    private String paymentMethod; // CASH, UPI, etc.

    @Column(name = "expense_date")
    private String expenseDate;

    @Column(name = "sync_status")
    private String syncStatus = "SYNCED";

    public Expense() {
    }

    public Expense(String category, String description, double amount, String paymentMethod) {
        this.category = category;
        this.description = description;
        this.amount = Math.round(amount * 100.0) / 100.0;
        this.paymentMethod = paymentMethod != null ? paymentMethod : "CASH";
    }

    @PrePersist
    protected void onCreate() {
        if (this.expenseDate == null) {
            this.expenseDate = LocalDateTime.now().format(FORMATTER);
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(String expenseDate) {
        this.expenseDate = expenseDate;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }
}
