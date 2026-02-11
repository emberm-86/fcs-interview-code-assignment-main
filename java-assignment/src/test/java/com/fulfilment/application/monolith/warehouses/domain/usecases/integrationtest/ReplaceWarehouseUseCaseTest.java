package com.fulfilment.application.monolith.warehouses.domain.usecases.integrationtest;

import com.fulfilment.application.monolith.db.FreshDbProfile;
import com.fulfilment.application.monolith.exception.ResourceNotFoundException;
import com.fulfilment.application.monolith.exception.ValidationException;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ReplaceWarehouseUseCase;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestProfile(FreshDbProfile.class)
class ReplaceWarehouseUseCaseTest {

    @Inject
    ReplaceWarehouseUseCase useCase;

    @Inject
    WarehouseStore warehouseStore;

    @Test
    @TestTransaction
    void shouldReplaceWarehouseSuccessfully() {
        ((WarehouseRepository) warehouseStore).cleanUp();
        String businessUnitCode = "MWH.001";

        warehouseStore.create(
                Warehouse.builder()
                        .businessUnitCode(businessUnitCode)
                        .location("ZWOLLE-001")
                        .capacity(100)
                        .stock(5)
                        .build()
        );

        Warehouse newWarehouse = Warehouse
                .builder()
                .businessUnitCode(businessUnitCode)
                .capacity(20)
                .stock(5)
                .location("AMSTERDAM-001")
                .build();

        useCase.replace(newWarehouse);

        Warehouse persistedWarehouse =
                warehouseStore.findByBusinessUnitCode(businessUnitCode);

        assertNotNull(persistedWarehouse);

        assertEquals(20, persistedWarehouse.capacity);
        assertEquals(5, persistedWarehouse.stock);
        assertEquals("AMSTERDAM-001", persistedWarehouse.location);
    }

    @Test
    void shouldThrowValidationException_whenWarehouseIsNull() {
        assertThrows(ValidationException.class, () -> useCase.replace(null));
    }

    @Test
    void shouldThrowValidation_whenNewStockNotEqual() {
        ((WarehouseRepository) warehouseStore).cleanUp();
        String businessUnitCode = "MWH.001";

        warehouseStore.create(
                Warehouse.builder()
                        .businessUnitCode(businessUnitCode)
                        .location("ZWOLLE-001")
                        .capacity(100)
                        .stock(5)
                        .build());

        Warehouse newWarehouse = Warehouse.builder()
                .businessUnitCode(businessUnitCode)
                .location("ZWOLLE-001")
                .capacity(100)
                .stock(6)
                .build();

        assertThrows(ValidationException.class,
                () -> useCase.replace(newWarehouse));
    }

    @Test
    void shouldThrowResourceNotFound_whenWarehouseDoesNotExist() {
        String businessUnitCode = "BU-404";

        Warehouse newWarehouse = Warehouse
                .builder()
                .businessUnitCode(businessUnitCode)
                .capacity(20)
                .stock(6)
                .location("AMSTERDAM-001")
                .build();

        assertThrows(ResourceNotFoundException.class,
                () -> useCase.replace(newWarehouse));
    }
}