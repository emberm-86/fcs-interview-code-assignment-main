package com.fulfilment.application.monolith.warehouses.adapters.database.unittest;

import com.fulfilment.application.monolith.exception.ValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.WarehouseProductAssignment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WarehouseProductAssignmentTest {

    @Test
    void shouldAddProductsUpToLimit() {

        WarehouseProductAssignment assignment =
                new WarehouseProductAssignment("MWH.001", 1L);

        assignment.addProduct(1L);
        assignment.addProduct(2L);
        assignment.addProduct(3L);
        assignment.addProduct(4L);
        assignment.addProduct(5L);

        assertEquals(5, assignment.getProductIds().size());
    }

    @Test
    void shouldThrowWhenExceedingMaxProducts() {

        WarehouseProductAssignment assignment =
                new WarehouseProductAssignment("MWH.001", 1L);

        for (long i = 1; i <= 5; i++) {
            assignment.addProduct(i);
        }

        assertThrows(ValidationException.class,
                () -> assignment.addProduct(6L));
    }

    @Test
    void shouldNotAddDuplicateProducts() {

        WarehouseProductAssignment assignment =
                new WarehouseProductAssignment("MWH.001", 1L);

        assignment.addProduct(1L);
        assignment.addProduct(1L);

        assertEquals(1, assignment.getProductIds().size());
    }

    @Test
    void shouldReturnImmutableProductSet() {

        WarehouseProductAssignment assignment =
                new WarehouseProductAssignment("MWH.001", 1L);

        assignment.addProduct(1L);

        assertThrows(UnsupportedOperationException.class,
                () -> assignment.getProductIds().add(2L));
    }
}