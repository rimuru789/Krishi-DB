package com.krishidb.model;

public class MarketPrice {

    private int id;
    private String commodity;
    private String market;
    private String district;
    private double price;
    private String unit;
    private String recordedAt;

    public MarketPrice() {
    }

    public MarketPrice(int id, String commodity, String market, String district,
                       double price, String unit, String recordedAt) {
        this.id = id;
        this.commodity = commodity;
        this.market = market;
        this.district = district;
        this.price = price;
        this.unit = unit;
        this.recordedAt = recordedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCommodity() {
        return commodity;
    }

    public void setCommodity(String commodity) {
        this.commodity = commodity;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(String recordedAt) {
        this.recordedAt = recordedAt;
    }

    @Override
    public String toString() {
        return "MarketPrice{" +
                "id=" + id +
                ", commodity='" + commodity + '\'' +
                ", market='" + market + '\'' +
                ", district='" + district + '\'' +
                ", price=" + price +
                ", unit='" + unit + '\'' +
                ", recordedAt='" + recordedAt + '\'' +
                '}';
    }
}
