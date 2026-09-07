package com.krishidb.krishi_api.dto;

public class CustomerPaymentRequest {
    private Double amount;
    private String paymentMethod;
    private String notes;

    public CustomerPaymentRequest() {
    }

    public CustomerPaymentRequest(Double amount, String paymentMethod, String notes) {
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.notes = notes;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
