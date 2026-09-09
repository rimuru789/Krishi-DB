package com.krishidb.model;

public class Expense {

    private int id;
    private String category;
    private String description;
    private double amount;
    private String paymentMethod;
    private String expenseDate;
    private String syncStatus;

    public Expense() {
        this.paymentMethod = "CASH";
        this.syncStatus = "PENDING";
    }

    // Constructor for creating a new expense
    public Expense(String category, String description, double amount, String paymentMethod) {
        this.category = category;
        this.description = description;
        this.amount = Math.round(amount * 100.0) / 100.0;
        this.paymentMethod = paymentMethod != null ? paymentMethod : "CASH";
        this.syncStatus = "PENDING";
    }

    // Constructor for database records
    public Expense(int id, String category, String description, double amount,
                   String paymentMethod, String expenseDate, String syncStatus) {
        this.id = id;
        this.category = category;
        this.description = description;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.expenseDate = expenseDate;
        this.syncStatus = syncStatus;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    @Override
    public String toString() {
        return "Expense{" +
                "id=" + id +
                ", category='" + category + '\'' +
                ", description='" + description + '\'' +
                ", amount=" + amount +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", expenseDate='" + expenseDate + '\'' +
                ", syncStatus='" + syncStatus + '\'' +
                '}';
    }
}
