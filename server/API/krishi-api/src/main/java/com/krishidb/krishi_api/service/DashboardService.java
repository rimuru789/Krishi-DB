package com.krishidb.krishi_api.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.krishidb.krishi_api.dto.DashboardSummaryDTO;
import com.krishidb.krishi_api.model.Product;
import com.krishidb.krishi_api.repository.*;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final PurchaseRepository purchaseRepository;
    private final ExpenseRepository expenseRepository;
    private final TransactionRepository transactionRepository;

    public DashboardService(ProductRepository productRepository,
            CustomerRepository customerRepository,
            SupplierRepository supplierRepository,
            SaleRepository saleRepository,
            SaleItemRepository saleItemRepository,
            PurchaseRepository purchaseRepository,
            ExpenseRepository expenseRepository,
            TransactionRepository transactionRepository) {
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.supplierRepository = supplierRepository;
        this.saleRepository = saleRepository;
        this.saleItemRepository = saleItemRepository;
        this.purchaseRepository = purchaseRepository;
        this.expenseRepository = expenseRepository;
        this.transactionRepository = transactionRepository;
    }

    public DashboardSummaryDTO getDashboardStats() {
        DashboardSummaryDTO dto = new DashboardSummaryDTO();

        // Products & inventory metrics
        List<Product> products = productRepository.findAll();
        dto.setTotalProductsCount(products.size());

        double inventoryValue = 0.0;
        long lowStockCount = 0;
        for (Product p : products) {
            inventoryValue += (p.getSellingPrice() * p.getStockQuantity());
            if (p.getStockQuantity() <= p.getLowStockLevel()) {
                lowStockCount++;
            }
        }
        dto.setInventoryValue(Math.round(inventoryValue * 100.0) / 100.0);
        dto.setLowStockCount(lowStockCount);

        // Today's sales
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        double todaySales = saleRepository.sumSalesAmountForDate(today);
        dto.setTodaySales(Math.round(todaySales * 100.0) / 100.0);

        // Sales totals
        dto.setTotalSalesCount(saleRepository.count());
        dto.setTotalRevenue(Math.round(saleRepository.sumTotalSalesAmount() * 100.0) / 100.0);

        // Purchases & Expenses totals
        dto.setTotalPurchases(Math.round(purchaseRepository.sumTotalPurchaseAmount() * 100.0) / 100.0);
        dto.setTotalExpenses(Math.round(expenseRepository.sumTotalExpenseAmount() * 100.0) / 100.0);

        // Customers & Suppliers
        dto.setCustomerCount(customerRepository.count());
        dto.setSupplierCount(supplierRepository.count());
        dto.setTotalReceivables(Math.round(customerRepository.sumTotalOutstandingBalance() * 100.0) / 100.0);
        dto.setTotalPayables(Math.round(supplierRepository.sumTotalOutstandingPayable() * 100.0) / 100.0);

        // Recent activity
        dto.setRecentSales(saleRepository.findTop10ByOrderByIdDesc());
        dto.setRecentTransactions(transactionRepository.findTop10ByOrderByIdDesc());

        // Top products
        List<Object[]> topProductRows = saleItemRepository.findTopSellingProducts();
        List<Map<String, Object>> topList = new ArrayList<>();
        int count = 0;
        for (Object[] row : topProductRows) {
            if (count++ >= 5) break;
            Map<String, Object> map = new HashMap<>();
            map.put("productId", row[0]);
            map.put("productName", row[1]);
            map.put("quantitySold", row[2]);
            map.put("totalRevenue", row[3]);
            topList.add(map);
        }
        dto.setTopProducts(topList);

        return dto;
    }
}
