package com.krishidb.krishi_api.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.krishidb.krishi_api.model.Expense;
import com.krishidb.krishi_api.model.Product;
import com.krishidb.krishi_api.model.Purchase;
import com.krishidb.krishi_api.model.Sale;
import com.krishidb.krishi_api.repository.ExpenseRepository;
import com.krishidb.krishi_api.repository.ProductRepository;
import com.krishidb.krishi_api.repository.PurchaseRepository;
import com.krishidb.krishi_api.repository.SaleRepository;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final SaleRepository saleRepository;
    private final PurchaseRepository purchaseRepository;
    private final ExpenseRepository expenseRepository;
    private final ProductRepository productRepository;

    public ReportService(SaleRepository saleRepository,
            PurchaseRepository purchaseRepository,
            ExpenseRepository expenseRepository,
            ProductRepository productRepository) {
        this.saleRepository = saleRepository;
        this.purchaseRepository = purchaseRepository;
        this.expenseRepository = expenseRepository;
        this.productRepository = productRepository;
    }

    public Map<String, Object> getSalesReport(String startDate, String endDate) {
        List<Sale> sales;
        if (startDate != null && endDate != null) {
            sales = saleRepository.findBySaleDateRange(startDate, endDate + " 23:59:59");
        } else {
            sales = saleRepository.findAll();
        }

        double totalRevenue = 0.0;
        for (Sale s : sales) {
            totalRevenue += s.getTotalAmount();
        }

        Map<String, Object> report = new HashMap<>();
        report.put("totalCount", sales.size());
        report.put("totalRevenue", Math.round(totalRevenue * 100.0) / 100.0);
        report.put("sales", sales);
        return report;
    }

    public Map<String, Object> getPurchasesReport(String startDate, String endDate) {
        List<Purchase> purchases;
        if (startDate != null && endDate != null) {
            purchases = purchaseRepository.findByPurchaseDateRange(startDate, endDate + " 23:59:59");
        } else {
            purchases = purchaseRepository.findAll();
        }

        double totalAmount = 0.0;
        for (Purchase p : purchases) {
            totalAmount += p.getTotalAmount();
        }

        Map<String, Object> report = new HashMap<>();
        report.put("totalCount", purchases.size());
        report.put("totalExpenditure", Math.round(totalAmount * 100.0) / 100.0);
        report.put("purchases", purchases);
        return report;
    }

    public Map<String, Object> getExpensesReport(String startDate, String endDate) {
        List<Expense> expenses;
        if (startDate != null && endDate != null) {
            expenses = expenseRepository.findByExpenseDateRange(startDate, endDate + " 23:59:59");
        } else {
            expenses = expenseRepository.findAll();
        }

        double totalAmount = 0.0;
        for (Expense e : expenses) {
            totalAmount += e.getAmount();
        }

        Map<String, Object> report = new HashMap<>();
        report.put("totalCount", expenses.size());
        report.put("totalExpense", Math.round(totalAmount * 100.0) / 100.0);
        report.put("expenses", expenses);
        return report;
    }

    public Map<String, Object> getInventoryReport() {
        List<Product> products = productRepository.findAll();
        double totalValuation = 0.0;
        double totalUnits = 0.0;
        List<Product> lowStockItems = new ArrayList<>();

        for (Product p : products) {
            totalValuation += (p.getSellingPrice() * p.getStockQuantity());
            totalUnits += p.getStockQuantity();
            if (p.getStockQuantity() <= p.getLowStockLevel()) {
                lowStockItems.add(p);
            }
        }

        Map<String, Object> report = new HashMap<>();
        report.put("totalProducts", products.size());
        report.put("totalUnits", Math.round(totalUnits * 100.0) / 100.0);
        report.put("totalValuation", Math.round(totalValuation * 100.0) / 100.0);
        report.put("lowStockCount", lowStockItems.size());
        report.put("lowStockItems", lowStockItems);
        return report;
    }
}
