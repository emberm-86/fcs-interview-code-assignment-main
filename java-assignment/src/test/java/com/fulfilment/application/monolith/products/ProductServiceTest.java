package com.fulfilment.application.monolith.products;

import com.fulfilment.application.monolith.exception.ResourceNotFoundException;
import com.fulfilment.application.monolith.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    ProductRepository productRepository;

    @InjectMocks
    ProductService productService;

    @Test
    void shouldReturnAllProducts() {
        Product p1 = new Product();
        p1.name = "A";

        Product p2 = new Product();
        p2.name = "B";

        when(productRepository.listAll(any())).thenReturn(List.of(p1, p2));

        List<Product> result = productService.getAll();

        assertEquals(2, result.size());
        verify(productRepository).listAll(any());
    }

    @Test
    void shouldReturnProductById() {
        Product product = new Product();
        product.id = 1L;

        when(productRepository.findById(1L)).thenReturn(product);

        Product result = productService.getById(1L);

        assertEquals(1L, result.id);
    }

    @Test
    void shouldThrowWhenProductNotFound() {
        when(productRepository.findById(1L)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> productService.getById(1L));
    }

    @Test
    void shouldCreateProduct() {
        Product product = new Product();
        product.name = "Test";

        Product result = productService.create(product);

        assertEquals("Test", result.name);
        verify(productRepository).persist(product);
    }

    @Test
    void shouldFailCreateWhenIdIsSet() {
        Product product = new Product();
        product.id = 1L;

        assertThrows(ValidationException.class,
                () -> productService.create(product));
    }

    @Test
    void shouldFailCreateWhenNameMissing() {
        Product product = new Product();

        assertThrows(ValidationException.class,
                () -> productService.create(product));
    }

    @Test
    void shouldUpdateProduct() {
        Product existing = new Product();
        existing.id = 1L;

        when(productRepository.findById(1L)).thenReturn(existing);

        Product update = new Product();
        update.name = "Updated";
        update.description = "Desc";
        update.price = new BigDecimal("100");
        update.stock = 10;

        Product result = productService.update(1L, update);

        assertEquals("Updated", result.name);
        assertEquals("Desc", result.description);
        assertEquals(0, result.price.compareTo(new BigDecimal("100.00")));
        assertEquals(10, result.stock);
    }

    @Test
    void shouldFailUpdateWhenNameMissing() {
        Product update = new Product();

        assertThrows(ValidationException.class,
                () -> productService.update(1L, update));
    }

    @Test
    void shouldFailUpdateWhenProductNotFound() {
        when(productRepository.findById(1L)).thenReturn(null);

        Product update = new Product();
        update.name = "Valid";

        assertThrows(ResourceNotFoundException.class,
                () -> productService.update(1L, update));
    }

    @Test
    void shouldDeleteProduct() {
        Product product = new Product();
        product.id = 1L;

        when(productRepository.findById(1L)).thenReturn(product);

        productService.delete(1L);

        verify(productRepository).delete(product);
    }

    @Test
    void shouldFailDeleteWhenProductNotFound() {
        when(productRepository.findById(1L)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> productService.delete(1L));
    }
}
