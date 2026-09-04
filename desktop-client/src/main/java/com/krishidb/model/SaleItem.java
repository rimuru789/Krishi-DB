package com.krishidb.model;

public class SaleItem {

    private int id;
    private int saleId;
    private int productId;
    private String productName; // Joined for UI display
    private String unit;        // Joined for UI display
    private double quantity;
    private double pricePerUnit;
    private double subtotal;

    public SaleItem() {
    }

    public SaleItem(int productId, String productName, String unit, double quantity, double pricePerUnit) {
        this.productId = productId;
        this.productName = productName;
        this.unit = unit;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.subtotal = Math.round(quantity * pricePerUnit * 100.0) / 100.0;
    }

    public SaleItem(int id, int saleId, int productId, String productName, String unit,
                    double quantity, double pricePerUnit, double subtotal) {
        this.id = id;
        this.saleId = saleId;
        this.productId = productId;
        this.productName = productName;
        this.unit = unit;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.subtotal = subtotal;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
        this.subtotal = Math.round(this.quantity * this.pricePerUnit * 100.0) / 100.0;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
        this.subtotal = Math.round(this.quantity * this.pricePerUnit * 100.0) / 100.0;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    @Override
    public String toString() {
        return "SaleItem{" +
                "id=" + id +
                ", saleId=" + saleId +
                ", productId=" + productId +
                ", productName='" + productName + '\'' +
                ", unit='" + unit + '\'' +
                ", quantity=" + quantity +
                ", pricePerUnit=" + pricePerUnit +
                ", subtotal=" + subtotal +
                '}';
    }
}
