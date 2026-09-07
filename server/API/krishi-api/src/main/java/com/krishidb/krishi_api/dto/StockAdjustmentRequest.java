package com.krishidb.krishi_api.dto;

public class StockAdjustmentRequest {
    private Double delta;
    private String reason;

    public StockAdjustmentRequest() {
    }

    public StockAdjustmentRequest(Double delta, String reason) {
        this.delta = delta;
        this.reason = reason;
    }

    public Double getDelta() {
        return delta;
    }

    public void setDelta(Double delta) {
        this.delta = delta;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
