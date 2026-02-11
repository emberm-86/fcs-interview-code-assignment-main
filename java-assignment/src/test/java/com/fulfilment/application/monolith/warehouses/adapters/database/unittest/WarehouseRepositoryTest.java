package com.fulfilment.application.monolith.warehouses.adapters.database.unittest;

import com.fulfilment.application.monolith.exception.ResourceNotFoundException;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseDao;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseRepositoryTest {

    @Mock
    WarehouseDao dao;

    @InjectMocks
    WarehouseRepository repository;

    @Test
    void getAll_shouldReturnMappedWarehouses() {
        DbWarehouse db = new DbWarehouse();
        db.businessUnitCode = "MWH.001";
        db.location = "ZWOLLE-001";
        db.capacity = 100;
        db.stock = 50;

        when(dao.findAllNonArchived()).thenReturn(List.of(db));

        List<Warehouse> result = repository.getAll();

        assertEquals(1, result.size());
        assertEquals("MWH.001", result.get(0).businessUnitCode);
        assertEquals("ZWOLLE-001", result.get(0).location);
    }

    @Test
    void create_shouldSaveWarehouse() {
        Warehouse w = Warehouse.builder()
                .businessUnitCode("MWH.001")
                .location("ZWOLLE-001")
                .capacity(10)
                .stock(5)
                .build();

        repository.create(w);

        ArgumentCaptor<DbWarehouse> captor = ArgumentCaptor.forClass(DbWarehouse.class);
        verify(dao).save(captor.capture());

        DbWarehouse saved = captor.getValue();
        assertEquals("MWH.001", saved.businessUnitCode);
        assertEquals("ZWOLLE-001", saved.location);
        assertEquals(10, saved.capacity);
        assertEquals(5, saved.stock);
    }

    @Test
    void update_shouldModifyExistingEntity() {
        DbWarehouse db = new DbWarehouse();
        db.businessUnitCode = "MWH.001";
        db.location = "ZWOLLE-001";
        db.capacity = 100;
        db.stock = 50;

        when(dao.findByBusinessCodeNonArchived("MWH.001")).thenReturn(db);

        Warehouse updated = Warehouse.builder()
                .businessUnitCode("MWH.001")
                .location("ZWOLLE-001")
                .capacity(200)
                .stock(80)
                .build();

        repository.update(updated);

        assertEquals("ZWOLLE-001", db.location);
        assertEquals(200, db.capacity);
        assertEquals(80, db.stock);
    }

    @Test
    void update_shouldThrowIfNotFound() {
        when(dao.findByBusinessCodeNonArchived("MWH.001")).thenReturn(null);

        Warehouse w = Warehouse.builder()
                .businessUnitCode("MWH.001")
                .location("ZWOLLE-001")
                .capacity(100)
                .stock(50)
                .build();

        assertThrows(ResourceNotFoundException.class,
                () -> repository.update(w));
    }

    @Test
    void removeById_shouldArchiveWarehouse() {
        DbWarehouse db = new DbWarehouse();

        when(dao.findByIdNonArchived(1L)).thenReturn(db);

        repository.removeById(1L);

        assertNotNull(db.archivedAt);
    }

    @Test
    void removeById_shouldThrowIfNotFound() {
        when(dao.findByIdNonArchived(1L)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> repository.removeById(1L));
    }

    @Test
    void findByBusinessUnitCode_shouldReturnWarehouse() {
        DbWarehouse db = new DbWarehouse();
        db.businessUnitCode = "MWH.001";
        db.location = "ZWOLLE-001";
        db.capacity = 100;
        db.stock = 50;

        when(dao.findByBusinessCode("MWH.001")).thenReturn(db);

        Warehouse result = repository.findByBusinessUnitCode("MWH.001");

        assertNotNull(result);
        assertEquals("MWH.001", result.businessUnitCode);
        assertEquals("ZWOLLE-001", result.location);
        assertEquals(100, result.capacity);
        assertEquals(50, result.stock);
    }

    @Test
    void findByBusinessUnitCode_shouldReturnNullIfMissing() {
        when(dao.findByBusinessCode("MWH.001")).thenReturn(null);

        Warehouse result = repository.findByBusinessUnitCode("MWH.001");

        assertNull(result);
    }

    @Test
    void countByLocation_shouldDelegateToDao() {
        when(dao.countByLocation("LOC")).thenReturn(5L);

        long result = repository.countByLocation("LOC");

        assertEquals(5L, result);
    }

    @Test
    void findByIdW_shouldReturnWarehouse() {
        DbWarehouse db = new DbWarehouse();
        db.businessUnitCode = "MWH.001";
        db.location = "ZWOLLE-001";
        db.capacity = 100;
        db.stock = 50;

        when(dao.findByIdW(1L)).thenReturn(db);

        Warehouse result = repository.findByIdW(1L);

        assertNotNull(result);
    }

    @Test
    void cleanUp_shouldCallDao() {
        repository.cleanUp();

        verify(dao).cleanUp();
    }
}