package com.fulfilment.application.monolith.warehouses.domain.models;

import com.fulfilment.application.monolith.exception.ValidationException;

import java.util.HashSet;
import java.util.Set;

public class WarehouseProductAssignment {

    private static final int MAX_PRODUCTS = 5;
    private final String warehouseBuCode;
    private final Long storeId;
    private final Set<Long> productIds = new HashSet<>(); // max 5 products

    public WarehouseProductAssignment(String warehouseBuCode, long storeId) {
        this.warehouseBuCode = warehouseBuCode;
        this.storeId = storeId;
    }

    public String getWarehouseBuCode() { return warehouseBuCode; }
    public Long getStoreId() { return storeId; }
    public Set<Long> getProductIds() { return Set.copyOf(productIds); }

    public void addProduct(Long productId) {
        if (productIds.contains(productId)) return;

        if (productIds.size() >= MAX_PRODUCTS) {
            throw new ValidationException("A warehouse can store a maximum of " + MAX_PRODUCTS + " product types");
        }
        productIds.add(productId);
    }
}