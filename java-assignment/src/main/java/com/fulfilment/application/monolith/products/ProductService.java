package com.fulfilment.application.monolith.products;

import com.fulfilment.application.monolith.exception.ResourceNotFoundException;
import com.fulfilment.application.monolith.exception.ValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class ProductService {

    @Inject
    ProductRepository productRepository;

    public List<Product> getAll() {
        return productRepository.listAll(io.quarkus.panache.common.Sort.by("name"));
    }

    public Product getById(Long id) {
        return ensureExists(id);
    }

    @Transactional
    public Product create(Product product) {
        validateCreate(product);
        productRepository.persist(product);
        return product;
    }

    @Transactional
    public Product update(Long id, Product update) {
        validateUpdate(update);

        Product entity = ensureExists(id);

        entity.name = update.name;
        entity.description = update.description;
        entity.price = update.price;
        entity.stock = update.stock;

        return entity;
    }

    @Transactional
    public void delete(Long id) {
        Product product = ensureExists(id);
        productRepository.delete(product);
    }

    // =====================
    // Validation & helpers
    // =====================

    private void validateCreate(Product product) {
        if (product.id != null) {
            throw new ValidationException("Id was invalidly set on request.");
        }
        if (product.name == null) {
            throw new ValidationException("Product Name was not set on request.");
        }
    }

    private void validateUpdate(Product product) {
        if (product.name == null) {
            throw new ValidationException("Product Name was not set on request.");
        }
    }

    private Product ensureExists(Long id) {
        Product product = productRepository.findById(id);
        if (product == null) {
            throw new ResourceNotFoundException("Product with id of " + id + " does not exist.");
        }
        return product;
    }
}