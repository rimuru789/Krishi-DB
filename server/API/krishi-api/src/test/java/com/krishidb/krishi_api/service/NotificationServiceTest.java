package com.krishidb.krishi_api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.krishidb.krishi_api.model.Customer;
import com.krishidb.krishi_api.model.Product;
import com.krishidb.krishi_api.model.Supplier;
import com.krishidb.krishi_api.repository.CustomerRepository;
import com.krishidb.krishi_api.repository.ProductRepository;
import com.krishidb.krishi_api.repository.SupplierRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private SupplierRepository supplierRepository;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(productRepository, customerRepository, supplierRepository);
    }

    @Test
    void testGetNotifications_GeneratesLowStockAndBalancesWithRupeeSymbol() {
        Product p = new Product();
        p.setId(10L);
        p.setName("DAP Fertilizer");
        p.setUnit("Bag");
        p.setStockQuantity(2.0);
        p.setLowStockLevel(5.0);

        Customer c = new Customer("Ramesh Patil", "9822011223", "Nashik");
        c.setId(20L);
        c.setOutstandingBalance(1500.0);

        Supplier s = new Supplier("IFFCO Ltd", "02226541200", "Nashik");
        s.setId(30L);
        s.setOutstandingBalance(5000.0);

        when(productRepository.findAll()).thenReturn(List.of(p));
        when(customerRepository.findAll()).thenReturn(List.of(c));
        when(supplierRepository.findAll()).thenReturn(List.of(s));

        List<Map<String, Object>> notifs = notificationService.getNotifications();
        assertNotNull(notifs);
        assertEquals(3, notifs.size(), "Should have 1 stock, 1 customer, and 1 supplier notification");

        Map<String, Object> stockNotif = notifs.get(0);
        assertEquals("LOW_STOCK", stockNotif.get("type"));
        assertEquals("warning", stockNotif.get("severity"));
        assertTrue(((String) stockNotif.get("title")).contains("DAP Fertilizer"));

        Map<String, Object> custNotif = notifs.get(1);
        assertEquals("CUSTOMER_OUTSTANDING", custNotif.get("type"));
        String custMsg = (String) custNotif.get("message");
        assertTrue(custMsg.contains("\u20B91500.0"), "Customer message must include correct Rupee symbol: " + custMsg);

        Map<String, Object> suppNotif = notifs.get(2);
        assertEquals("SUPPLIER_PAYABLE", suppNotif.get("type"));
        String suppMsg = (String) suppNotif.get("message");
        assertTrue(suppMsg.contains("\u20B95000.0"), "Supplier message must include correct Rupee symbol: " + suppMsg);
    }
}
