package com.fulfilment.application.monolith.stores;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class LegacyStoreManagerGatewayTest {

    private LegacyStoreManagerGateway gateway;

    @BeforeEach
    void setUp() {
        gateway = new LegacyStoreManagerGateway();
    }

    @Test
    void createStoreOnLegacySystem_shouldNotThrowException() {
        Store store = new Store();
        store.id = 1L;
        store.name = "Test Store";
        store.quantityProductsInStock = 100;

        assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
    }

    @Test
    void updateStoreOnLegacySystem_shouldNotThrowException() {
        Store store = new Store();
        store.id = 2L;
        store.name = "Updated Store";
        store.quantityProductsInStock = 50;

        assertDoesNotThrow(() -> gateway.updateStoreOnLegacySystem(store));
    }

    @Test
    void deleteStoreOnLegacySystem_shouldNotThrowException() {
        Long storeId = 3L;

        assertDoesNotThrow(() -> gateway.deleteStoreOnLegacySystem(storeId));
    }
}