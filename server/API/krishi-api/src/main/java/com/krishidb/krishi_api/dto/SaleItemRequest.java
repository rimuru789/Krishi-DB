package com.krishidb.krishi_api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class SaleItemRequest {
    private Long productId;
    private double quantity;
    @JsonAlias({"unitPrice", "price", "unit_price"})
    private Double pricePerUnit;

    public SaleItemRequest() {
    }

    public SaleItemRequest(Long productId, double quantity, Double pricePerUnit) {
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

    public Double getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(Double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public Double getUnitPrice() {
        return pricePerUnit;
    }

    public void setUnitPrice(Double unitPrice) {
        this.pricePerUnit = unitPrice;
    }
}
