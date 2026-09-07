package com.krishidb.krishi_api.dto;

import java.util.List;
import java.util.Map;

import com.krishidb.krishi_api.model.Sale;
import com.krishidb.krishi_api.model.TransactionRecord;

public class DashboardSummaryDTO {

    private double todaySales;
    private long totalSalesCount;
    private double totalRevenue;
    private double inventoryValue;
    private long totalProductsCount;
    private long lowStockCount;
    private long customerCount;
    private long supplierCount;
    private double totalPurchases;
    private double totalExpenses;
    private double totalReceivables;
    private double totalPayables;
    private List<Sale> recentSales;
    private List<TransactionRecord> recentTransactions;
    private List<Map<String, Object>> topProducts;

    public DashboardSummaryDTO() {
    }

    public double getTodaySales() {
        return todaySales;
    }

    public void setTodaySales(double todaySales) {
        this.todaySales = todaySales;
    }

    public long getTotalSalesCount() {
        return totalSalesCount;
    }

    public void setTotalSalesCount(long totalSalesCount) {
        this.totalSalesCount = totalSalesCount;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public double getInventoryValue() {
        return inventoryValue;
    }

    public void setInventoryValue(double inventoryValue) {
        this.inventoryValue = inventoryValue;
    }

    public long getTotalProductsCount() {
        return totalProductsCount;
    }

    public void setTotalProductsCount(long totalProductsCount) {
        this.totalProductsCount = totalProductsCount;
    }

    public long getLowStockCount() {
        return lowStockCount;
    }

    public void setLowStockCount(long lowStockCount) {
        this.lowStockCount = lowStockCount;
    }

    public long getCustomerCount() {
        return customerCount;
    }

    public void setCustomerCount(long customerCount) {
        this.customerCount = customerCount;
    }

    public long getSupplierCount() {
        return supplierCount;
    }

    public void setSupplierCount(long supplierCount) {
        this.supplierCount = supplierCount;
    }

    public double getTotalPurchases() {
        return totalPurchases;
    }

    public void setTotalPurchases(double totalPurchases) {
        this.totalPurchases = totalPurchases;
    }

    public double getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(double totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public double getTotalReceivables() {
        return totalReceivables;
    }

    public void setTotalReceivables(double totalReceivables) {
        this.totalReceivables = totalReceivables;
    }

    public double getTotalPayables() {
        return totalPayables;
    }

    public void setTotalPayables(double totalPayables) {
        this.totalPayables = totalPayables;
    }

    public List<Sale> getRecentSales() {
        return recentSales;
    }

    public void setRecentSales(List<Sale> recentSales) {
        this.recentSales = recentSales;
    }

    public List<TransactionRecord> getRecentTransactions() {
        return recentTransactions;
    }

    public void setRecentTransactions(List<TransactionRecord> recentTransactions) {
        this.recentTransactions = recentTransactions;
    }

    public List<Map<String, Object>> getTopProducts() {
        return topProducts;
    }

    public void setTopProducts(List<Map<String, Object>> topProducts) {
        this.topProducts = topProducts;
    }
}
