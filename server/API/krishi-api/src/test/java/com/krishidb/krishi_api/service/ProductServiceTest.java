package com.krishidb.krishi_api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.krishidb.krishi_api.exception.ResourceNotFoundException;
import com.krishidb.krishi_api.model.Product;
import com.krishidb.krishi_api.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    private ProductService service;

    @BeforeEach
    void setUp() {
        service = new ProductService(repository);
    }

    @Test
    void testCreateProduct_Success() {
        Product product = new Product();
        product.setName("Organic Wheat");
        product.setCategory("Grains");
        product.setUnit("kg");
        product.setSellingPrice(45.0);
        product.setStockQuantity(100.0);
        product.setLowStockLevel(10.0);

        when(repository.save(any(Product.class))).thenAnswer(invocation -> {
            Product saved = invocation.getArgument(0);
            saved.setId(101L);
            return saved;
        });

        Product created = service.createProduct(product);
        assertNotNull(created);
        assertEquals(101L, created.getId());
        assertEquals("Organic Wheat", created.getName());
    }

    @Test
    void testCreateProduct_BlankName_ThrowsException() {
        Product product = new Product();
        product.setName("   ");
        assertThrows(IllegalArgumentException.class, () -> service.createProduct(product));
    }

    @Test
    void testCreateProduct_NegativeStock_ThrowsException() {
        Product product = new Product();
        product.setName("Wheat");
        product.setStockQuantity(-5.0);
        assertThrows(IllegalArgumentException.class, () -> service.createProduct(product));
    }

    @Test
    void testUpdateProduct_NotFound_ThrowsException() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        Product product = new Product();
        product.setName("Wheat");
        product.setSellingPrice(50.0);

        assertThrows(ResourceNotFoundException.class, () -> service.updateProduct(999L, product));
    }

    @Test
    void testAdjustStock_PositiveDelta_Success() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Wheat");
        product.setStockQuantity(50.0);

        when(repository.findById(1L)).thenReturn(Optional.of(product));
        when(repository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product updated = service.adjustStock(1L, 20.0, "Restock");
        assertEquals(70.0, updated.getStockQuantity());
    }

    @Test
    void testAdjustStock_NegativeDelta_Success() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Wheat");
        product.setStockQuantity(50.0);

        when(repository.findById(1L)).thenReturn(Optional.of(product));
        when(repository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product updated = service.adjustStock(1L, -30.0, "Sale");
        assertEquals(20.0, updated.getStockQuantity());
    }

    @Test
    void testAdjustStock_ExceedsCurrentStock_ThrowsException() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Wheat");
        product.setStockQuantity(10.0);

        when(repository.findById(1L)).thenReturn(Optional.of(product));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.adjustStock(1L, -15.0, "Too much deducted"));
        assertTrue(ex.getMessage().contains("Insufficient stock"));
    }

    @Test
    void testDeleteProduct_NotFound_ThrowsException() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.deleteProduct(999L));
    }
}
