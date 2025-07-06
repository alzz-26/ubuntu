package com.devops.inventory.controller;

import com.devops.inventory.model.Product;
import com.devops.inventory.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private MeterRegistry meterRegistry;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getAllProducts_ShouldReturnProductsList() throws Exception {
        // Given
        List<Product> products = Arrays.asList(
            createSampleProduct(1L, "Laptop", "High-performance laptop", new BigDecimal("1299.99"), 10),
            createSampleProduct(2L, "Phone", "Smartphone", new BigDecimal("999.99"), 20)
        );
        when(productService.getAllProducts()).thenReturn(products);

        // When & Then
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Laptop"))
                .andExpect(jsonPath("$[1].name").value("Phone"));

        verify(productService, times(1)).getAllProducts();
    }

    @Test
    void getProductById_ShouldReturnProduct() throws Exception {
        // Given
        Product product = createSampleProduct(1L, "Laptop", "High-performance laptop", new BigDecimal("1299.99"), 10);
        when(productService.getProductById(1L)).thenReturn(product);

        // When & Then
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"));

        verify(productService, times(1)).getProductById(1L);
    }

    @Test
    void getProductById_ShouldReturn404_WhenProductNotFound() throws Exception {
        // Given
        when(productService.getProductById(999L)).thenThrow(new EntityNotFoundException("Product not found"));

        // When & Then
        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).getProductById(999L);
    }

    @Test
    void createProduct_ShouldReturnCreatedProduct() throws Exception {
        // Given
        Product productToCreate = createSampleProduct(null, "New Laptop", "New laptop description", new BigDecimal("1499.99"), 5);
        Product createdProduct = createSampleProduct(1L, "New Laptop", "New laptop description", new BigDecimal("1499.99"), 5);
        
        when(productService.createProduct(any(Product.class))).thenReturn(createdProduct);

        // When & Then
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productToCreate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("New Laptop"));

        verify(productService, times(1)).createProduct(any(Product.class));
    }

    @Test
    void updateProduct_ShouldReturnUpdatedProduct() throws Exception {
        // Given
        Product productToUpdate = createSampleProduct(1L, "Updated Laptop", "Updated description", new BigDecimal("1599.99"), 15);
        when(productService.updateProduct(eq(1L), any(Product.class))).thenReturn(productToUpdate);

        // When & Then
        mockMvc.perform(put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productToUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Laptop"));

        verify(productService, times(1)).updateProduct(eq(1L), any(Product.class));
    }

    @Test
    void deleteProduct_ShouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(productService).deleteProduct(1L);

        // When & Then
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).deleteProduct(1L);
    }

    @Test
    void getProductsByCategory_ShouldReturnProducts() throws Exception {
        // Given
        List<Product> electronics = Arrays.asList(
            createSampleProduct(1L, "Laptop", "Laptop description", new BigDecimal("1299.99"), 10),
            createSampleProduct(2L, "Phone", "Phone description", new BigDecimal("999.99"), 20)
        );
        when(productService.getProductsByCategory("Electronics")).thenReturn(electronics);

        // When & Then
        mockMvc.perform(get("/api/products/category/Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Laptop"))
                .andExpect(jsonPath("$[1].name").value("Phone"));

        verify(productService, times(1)).getProductsByCategory("Electronics");
    }

    @Test
    void getLowStockProducts_ShouldReturnProducts() throws Exception {
        // Given
        List<Product> lowStockProducts = Arrays.asList(
            createSampleProduct(1L, "Laptop", "Laptop description", new BigDecimal("1299.99"), 5)
        );
        when(productService.getLowStockProducts(10)).thenReturn(lowStockProducts);

        // When & Then
        mockMvc.perform(get("/api/products/low-stock/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Laptop"));

        verify(productService, times(1)).getLowStockProducts(10);
    }

    @Test
    void getOutOfStockProducts_ShouldReturnProducts() throws Exception {
        // Given
        List<Product> outOfStockProducts = Arrays.asList(
            createSampleProduct(1L, "Laptop", "Laptop description", new BigDecimal("1299.99"), 0)
        );
        when(productService.getOutOfStockProducts()).thenReturn(outOfStockProducts);

        // When & Then
        mockMvc.perform(get("/api/products/out-of-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Laptop"));

        verify(productService, times(1)).getOutOfStockProducts();
    }

    @Test
    void getInStockProductCount_ShouldReturnCount() throws Exception {
        // Given
        when(productService.getInStockProductCount()).thenReturn(5L);

        // When & Then
        mockMvc.perform(get("/api/products/stats/in-stock-count"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        verify(productService, times(1)).getInStockProductCount();
    }

    @Test
    void getTotalInventoryCount_ShouldReturnCount() throws Exception {
        // Given
        when(productService.getTotalInventoryCount()).thenReturn(100L);

        // When & Then
        mockMvc.perform(get("/api/products/stats/total-inventory"))
                .andExpect(status().isOk())
                .andExpect(content().string("100"));

        verify(productService, times(1)).getTotalInventoryCount();
    }

    private Product createSampleProduct(Long id, String name, String description, BigDecimal price, Integer quantity) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setQuantity(quantity);
        product.setCategory("Electronics");
        product.setSku("SKU-" + name.toUpperCase().replace(" ", "-"));
        return product;
    }
} 