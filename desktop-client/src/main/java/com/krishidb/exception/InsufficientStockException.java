package com.krishidb.exception;

/**
 * Exception thrown when a sale operation requests more quantity of a product
 * than is currently available in the inventory.
 */
public class InsufficientStockException extends Exception {

    private final String productName;
    private final double requestedQuantity;
    private final double availableQuantity;

    public InsufficientStockException(String productName, double requestedQuantity, double availableQuantity) {
        super(String.format("Insufficient stock for product '%s'. Requested: %.2f, Available: %.2f",
                productName, requestedQuantity, availableQuantity));
        this.productName = productName;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    public String getProductName() {
        return productName;
    }

    public double getRequestedQuantity() {
        return requestedQuantity;
    }

    public double getAvailableQuantity() {
        return availableQuantity;
    }
}
