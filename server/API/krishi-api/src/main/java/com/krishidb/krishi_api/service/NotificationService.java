package com.krishidb.krishi_api.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.krishidb.krishi_api.model.Customer;
import com.krishidb.krishi_api.model.Product;
import com.krishidb.krishi_api.model.Supplier;
import com.krishidb.krishi_api.repository.CustomerRepository;
import com.krishidb.krishi_api.repository.ProductRepository;
import com.krishidb.krishi_api.repository.SupplierRepository;

@Service
@Transactional(readOnly = true)
public class NotificationService {

    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;

    public NotificationService(ProductRepository productRepository,
            CustomerRepository customerRepository,
            SupplierRepository supplierRepository) {
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.supplierRepository = supplierRepository;
    }

    public List<Map<String, Object>> getNotifications() {
        List<Map<String, Object>> notifications = new ArrayList<>();
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Low stock alerts
        List<Product> products = productRepository.findAll();
        for (Product p : products) {
            if (p.getStockQuantity() <= p.getLowStockLevel()) {
                Map<String, Object> alert = new HashMap<>();
                alert.put("id", "stock-" + p.getId());
                alert.put("type", "LOW_STOCK");
                alert.put("severity", p.getStockQuantity() <= 0 ? "critical" : "warning");
                alert.put("title", "Low Stock Alert: " + p.getName());
                alert.put("message", "Product '" + p.getName() + "' is low on stock (" + p.getStockQuantity() + " " + p.getUnit() + " remaining, minimum is " + p.getLowStockLevel() + " " + p.getUnit() + ")");
                alert.put("timestamp", now);
                alert.put("referenceId", p.getId());
                notifications.add(alert);
            }
        }

        // Customer outstanding alerts
        List<Customer> customers = customerRepository.findAll();
        for (Customer c : customers) {
            if (c.getOutstandingBalance() > 0) {
                Map<String, Object> alert = new HashMap<>();
                alert.put("id", "cust-" + c.getId());
                alert.put("type", "CUSTOMER_OUTSTANDING");
                alert.put("severity", "info");
                alert.put("title", "Customer Due: " + c.getName());
                alert.put("message", "Customer '" + c.getName() + "' has an outstanding balance of ₹" + c.getOutstandingBalance());
                alert.put("timestamp", now);
                alert.put("referenceId", c.getId());
                notifications.add(alert);
            }
        }

        // Supplier payable alerts
        List<Supplier> suppliers = supplierRepository.findAll();
        for (Supplier s : suppliers) {
            if (s.getOutstandingBalance() > 0) {
                Map<String, Object> alert = new HashMap<>();
                alert.put("id", "supp-" + s.getId());
                alert.put("type", "SUPPLIER_PAYABLE");
                alert.put("severity", "info");
                alert.put("title", "Supplier Payable: " + s.getName());
                alert.put("message", "Supplier '" + s.getName() + "' has pending payables of ₹" + s.getOutstandingBalance());
                alert.put("timestamp", now);
                alert.put("referenceId", s.getId());
                notifications.add(alert);
            }
        }

        return notifications;
    }
}
