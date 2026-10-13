package com.krishidb.krishi_api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class PurchaseItemRequest {
    private Long productId;
    private double quantity;
    @JsonAlias({"unitPrice", "price", "unit_price"})
    private double pricePerUnit;

    public PurchaseItemRequest() {
    }

    public PurchaseItemRequest(Long productId, double quantity, double pricePerUnit) {
        this.productId = productId;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public double getUnitPrice() {
        return pricePerUnit;
    }

    public void setUnitPrice(double unitPrice) {
        this.pricePerUnit = unitPrice;
    }
}
