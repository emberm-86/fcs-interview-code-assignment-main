package com.fulfilment.application.monolith.warehouses.adapters.database.integrationtest;

import com.fulfilment.application.monolith.db.FreshDbProfile;
import com.fulfilment.application.monolith.exception.ResourceNotFoundException;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestProfile(FreshDbProfile.class)
class WarehouseRepositoryTest {

    @Inject
    WarehouseRepository repository;

    @BeforeEach
    @Transactional
    void setUp() {
        repository.cleanUp();

        repository.create(
                Warehouse.builder()
                        .businessUnitCode("MWH.001")
                        .location("ZWOLLE-001")
                        .capacity(100)
                        .stock(10)
                        .build()
        );

        repository.create(
                Warehouse.builder()
                        .businessUnitCode("MWH.012")
                        .location("AMSTERDAM-001")
                        .capacity(50)
                        .stock(5)
                        .build()
        );

        repository.create(
                Warehouse.builder()
                        .businessUnitCode("MWH.023")
                        .location("TILBURG-001")
                        .capacity(30)
                        .stock(27)
                        .build()
        );
    }

    @Test
    void shouldReturnAllNonArchivedWarehouses() {
        List<Warehouse> warehouses = repository.getAll();

        assertEquals(3, warehouses.size());
        assertTrue(
                warehouses.stream()
                        .anyMatch(w -> w.businessUnitCode.equals("MWH.001"))
        );
    }

    @Test
    void shouldFindWarehouseByBusinessUnitCode() {
        Warehouse warehouse = repository.findByBusinessUnitCode("MWH.012");

        assertNotNull(warehouse);
        assertEquals("AMSTERDAM-001", warehouse.location);
        assertEquals(50, warehouse.capacity);
        assertEquals(5, warehouse.stock);
    }

    @Test
    void shouldReturnNullIfWarehouseDoesNotExist() {
        Warehouse warehouse = repository.findByBusinessUnitCode("UNKNOWN");

        assertNull(warehouse);
    }

    @Test
    @TestTransaction
    void shouldCreateWarehouse() {
        Warehouse newWarehouse = Warehouse.builder()
                .businessUnitCode("MWH.999")
                .location("UTRECHT-001")
                .capacity(200)
                .stock(0)
                .build();

        repository.create(newWarehouse);

        Warehouse stored = repository.findByBusinessUnitCode("MWH.999");

        assertNotNull(stored);
        assertEquals("UTRECHT-001", stored.location);
        assertEquals(200, stored.capacity);
    }

    @Test
    @TestTransaction
    void shouldUpdateWarehouse() {
        Warehouse updated = Warehouse.builder()
                .businessUnitCode("MWH.001")
                .location("ZWOLLE-UPDATED")
                .capacity(150)
                .stock(20)
                .build();

        repository.update(updated);

        Warehouse stored = repository.findByBusinessUnitCode("MWH.001");

        assertEquals("ZWOLLE-UPDATED", stored.location);
        assertEquals(150, stored.capacity);
        assertEquals(20, stored.stock);
    }

    @Test
    @TestTransaction
    void shouldThrowIfUpdatingNonExistingWarehouse() {
        Warehouse nonExisting = Warehouse.builder()
                .businessUnitCode("INVALID")
                .location("NOWHERE")
                .capacity(10)
                .stock(1)
                .build();

        assertThrows(ResourceNotFoundException.class,
                () -> repository.update(nonExisting));
    }

    @Test
    @TestTransaction
    void shouldArchiveWarehouseInsteadOfDeleting() {
        Warehouse tobeArchived = repository.findByBusinessUnitCode("MWH.001");
        assertNotNull(tobeArchived);

        repository.removeById(tobeArchived.id);

        Warehouse archived = repository.findByBusinessUnitCode("MWH.001");

        // Should not be found because archivedAt is set
        assertNotNull(archived);
        assertNotNull(archived.archivedAt);

        List<Warehouse> all = repository.getAll();
        assertEquals(2, all.size());
    }

    @Test
    void shouldCountWarehousesByLocation() {
        long count = repository.countByLocation("ZWOLLE-001");

        assertEquals(1, count);
    }
}