package com.fulfilment.application.monolith.products;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductResourceTest {

    @Mock
    ProductService productService;

    @InjectMocks
    ProductResource productResource;

    @Test
    void shouldReturnAllProducts() {
        // given
        List<Product> products = List.of(new Product(), new Product());
        when(productService.getAll()).thenReturn(products);

        // when
        List<Product> result = productResource.get();

        // then
        assertEquals(products, result);
        verify(productService).getAll();
        verifyNoMoreInteractions(productService);
    }

    @Test
    void shouldReturnSingleProduct() {
        // given
        Product product = new Product();
        product.id = 1L;
        when(productService.getById(1L)).thenReturn(product);

        // when
        Product result = productResource.getSingle(1L);

        // then
        assertEquals(product, result);
        verify(productService).getById(1L);
        verifyNoMoreInteractions(productService);
    }

    @Test
    void shouldCreateProduct() {
        // given
        Product input = new Product();
        Product created = new Product();
        created.id = 1L;

        when(productService.create(input)).thenReturn(created);

        // when
        try (Response response = productResource.create(input)) {
            // then
            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
            assertEquals(created, response.getEntity());
        }

        verify(productService).create(input);
        verifyNoMoreInteractions(productService);
    }

    @Test
    void shouldUpdateProduct() {
        // given
        Product input = new Product();
        Product updated = new Product();

        when(productService.update(1L, input)).thenReturn(updated);

        // when
        Product result = productResource.update(1L, input);

        // then
        assertSame(updated, result); // stronger than equals here
        verify(productService).update(1L, input);
        verifyNoMoreInteractions(productService);
    }

    @Test
    void shouldDeleteProduct() {
        // when
        try (Response response = productResource.delete(1L)) {

            // then
            assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        }
        verify(productService).delete(1L);
        verifyNoMoreInteractions(productService);
    }
}
