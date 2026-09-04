package com.krishidb.model;

import java.util.ArrayList;
import java.util.List;

public class Sale {

    private int id;
    private Integer customerId; // Nullable for walk-in cash customer
    private String customerName; // Joined/transient for display
    private double totalAmount;
    private String paymentMethod; // CASH, UPI, CREDIT
    private String saleDate;
    private String notes;
    private String syncStatus;
    private List<SaleItem> items;

    public Sale() {
        this.items = new ArrayList<>();
        this.syncStatus = "PENDING";
    }

    public Sale(Integer customerId, double totalAmount, String paymentMethod, String notes) {
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.notes = notes;
        this.syncStatus = "PENDING";
        this.items = new ArrayList<>();
    }

    public Sale(int id, Integer customerId, String customerName, double totalAmount,
                String paymentMethod, String saleDate, String notes, String syncStatus) {
        this.id = id;
        this.customerId = customerId;
        this.customerName = customerName;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.saleDate = saleDate;
        this.notes = notes;
        this.syncStatus = syncStatus;
        this.items = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(String saleDate) {
        this.saleDate = saleDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }

    public List<SaleItem> getItems() {
        return items;
    }

    public void setItems(List<SaleItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public void addItem(SaleItem item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
    }

    @Override
    public String toString() {
        return "Sale{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", customerName='" + customerName + '\'' +
                ", totalAmount=" + totalAmount +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", saleDate='" + saleDate + '\'' +
                ", syncStatus='" + syncStatus + '\'' +
                ", itemsCount=" + (items != null ? items.size() : 0) +
                '}';
    }
}
