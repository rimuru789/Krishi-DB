package com.krishidb.krishi_api.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonAlias;

public class PurchaseRequest {
    private Long supplierId;
    private String supplierName;
    private String invoiceNumber;
    @JsonAlias({"paymentStatus", "payment_status", "payment_mode"})
    private String paymentMethod; // CASH, UPI, CREDIT
    private String notes;
    private List<PurchaseItemRequest> items;

    public PurchaseRequest() {
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
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

    public List<PurchaseItemRequest> getItems() {
        return items;
    }

    public void setItems(List<PurchaseItemRequest> items) {
        this.items = items;
    }
}
