package com.fulfilment.application.monolith.warehouses.domain.usecases.integrationtest;

import com.fulfilment.application.monolith.db.FreshDbProfile;
import com.fulfilment.application.monolith.exception.ResourceNotFoundException;
import com.fulfilment.application.monolith.exception.ValidationException;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ArchiveWarehouseUseCase;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestProfile(FreshDbProfile.class)
class ArchiveWarehouseUseCaseTest {

    @Inject
    ArchiveWarehouseUseCase useCase;

    @Inject
    WarehouseStore warehouseStore;

    @Test
    @TestTransaction
    void shouldArchiveWarehouseSuccessfully() {
        ((WarehouseRepository) warehouseStore).cleanUp();
        String businessUnitCode = "MWH.001";

        Warehouse warehouse = Warehouse.builder()
                .businessUnitCode(businessUnitCode)
                .location("Zurich")
                .capacity(1000)
                .stock(200)
                .build();

        warehouseStore.create(warehouse);

        useCase.archive(warehouse);

        Warehouse persistedWarehouse =
                warehouseStore.findByBusinessUnitCode(businessUnitCode);

        assertNotNull(persistedWarehouse.archivedAt);
    }

    @Test
    void shouldThrowValidationException_whenWarehouseIsNull() {
        assertThrows(ValidationException.class, () -> useCase.archive(null));
    }

    @Test
    @TestTransaction
    void shouldThrowExceptionWhenWarehouseDoesNotExist() {
        Warehouse warehouse = Warehouse.builder()
                .businessUnitCode("BU-404")
                .location("Zurich")
                .capacity(1000)
                .stock(200)
                .build();

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> useCase.archive(warehouse)
        );

        assertEquals(
                "Warehouse with businessUnitCode BU-404 not found",
                exception.getMessage()
        );
    }
}