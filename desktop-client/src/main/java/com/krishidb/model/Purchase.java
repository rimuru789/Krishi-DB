package com.krishidb.model;

import java.util.ArrayList;
import java.util.List;

public class Purchase {

    private int id;
    private Integer supplierId;
    private String supplierName;
    private String invoiceNumber;
    private double totalAmount;
    private String paymentMethod;
    private String purchaseDate;
    private String notes;
    private String syncStatus;
    private List<PurchaseItem> items;

    // Default constructor
    public Purchase() {
        this.items = new ArrayList<>();
        this.syncStatus = "PENDING";
        this.paymentMethod = "CASH";
    }

    // Constructor for creating new purchase
    public Purchase(Integer supplierId, String invoiceNumber, double totalAmount,
                    String paymentMethod, String notes) {
        this.supplierId = supplierId;
        this.invoiceNumber = invoiceNumber;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod != null ? paymentMethod : "CASH";
        this.notes = notes;
        this.syncStatus = "PENDING";
        this.items = new ArrayList<>();
    }

    // Constructor for reading from database
    public Purchase(int id, Integer supplierId, String supplierName, String invoiceNumber,
                    double totalAmount, String paymentMethod, String purchaseDate,
                    String notes, String syncStatus) {
        this.id = id;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.invoiceNumber = invoiceNumber;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.purchaseDate = purchaseDate;
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

    public Integer getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
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

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
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

    public List<PurchaseItem> getItems() {
        return items;
    }

    public void setItems(List<PurchaseItem> items) {
        this.items = items;
    }

    public void addItem(PurchaseItem item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
    }

    @Override
    public String toString() {
        return "Purchase{" +
                "id=" + id +
                ", supplierId=" + supplierId +
                ", supplierName='" + supplierName + '\'' +
                ", invoiceNumber='" + invoiceNumber + '\'' +
                ", totalAmount=" + totalAmount +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", purchaseDate='" + purchaseDate + '\'' +
                ", syncStatus='" + syncStatus + '\'' +
                ", itemsCount=" + (items != null ? items.size() : 0) +
                '}';
    }
}
