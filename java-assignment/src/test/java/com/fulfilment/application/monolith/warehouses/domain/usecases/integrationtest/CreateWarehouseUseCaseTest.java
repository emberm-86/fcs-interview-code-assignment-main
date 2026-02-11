package com.fulfilment.application.monolith.warehouses.domain.usecases.integrationtest;

import com.fulfilment.application.monolith.db.FreshDbProfile;
import com.fulfilment.application.monolith.exception.ResourceConflictException;
import com.fulfilment.application.monolith.exception.ValidationException;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCase;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestProfile(FreshDbProfile.class)
class CreateWarehouseUseCaseTest {

    @Inject
    CreateWarehouseUseCase useCase;

    @Inject
    WarehouseStore warehouseStore;

    @Test
    @TestTransaction
    void shouldCreateWarehouseSuccessfully() {
        ((WarehouseRepository) warehouseStore).cleanUp();
        String businessUnitCode = "MWH.001";

        Warehouse warehouse = Warehouse.builder()
                .businessUnitCode(businessUnitCode)
                .location("Zurich")
                .capacity(1000)
                .stock(200)
                .build();

        useCase.create(warehouse);

        Warehouse persistedWarehouse =
                warehouseStore.findByBusinessUnitCode(businessUnitCode);

        assertNotNull(persistedWarehouse);
        assertEquals(businessUnitCode, persistedWarehouse.businessUnitCode);
    }

    @Test
    void shouldThrowValidationException_whenWarehouseIsNull() {
        assertThrows(ValidationException.class, () -> useCase.create(null));
    }

    @Test
    @TestTransaction
    void shouldThrowExceptionWhenWarehouseAlreadyExists() {
        Warehouse warehouse = Warehouse.builder()
                .businessUnitCode("MWH.001")
                .location("Zurich")
                .capacity(1000)
                .stock(200)
                .build();

        warehouseStore.create(warehouse);

        ResourceConflictException exception = assertThrows(
                ResourceConflictException.class,
                () -> useCase.create(warehouse)
        );

        assertEquals(
                "Warehouse with businessUnitCode MWH.001 already exists",
                exception.getMessage()
        );
    }
}
