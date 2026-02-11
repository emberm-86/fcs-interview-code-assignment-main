package com.fulfilment.application.monolith.warehouses.adapters.database.unittest;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseAssignmentDao;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseProductAssignmentRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WarehouseProductAssignmentRepositoryTest {

    @Mock
    WarehouseAssignmentDao dao;

    @InjectMocks
    WarehouseProductAssignmentRepository repository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ----------------------------
    // 🧪 SAVE TESTS
    // ----------------------------

    @Test
    void shouldCreateNewAssignment_whenNotExists() {
        when(dao.findOneByStoreAndWarehouse(1L, "MWH.001"))
                .thenReturn(null);

        WarehouseProductAssignment input =
                new WarehouseProductAssignment("MWH.001", 1L);
        input.addProduct(10L);
        input.addProduct(20L);

        repository.save(input);

        ArgumentCaptor<DbWarehouseAssignment> captor =
                ArgumentCaptor.forClass(DbWarehouseAssignment.class);

        verify(dao).save(captor.capture());

        DbWarehouseAssignment saved = captor.getValue();

        assertEquals(1L, saved.storeId);
        assertEquals("MWH.001", saved.warehouseCode);
        assertEquals(2, saved.products.size());
    }

    @Test
    void shouldAddNewProducts_toExistingAssignment() {
        DbWarehouseAssignment db = createDb(1L, "MWH.001", Set.of(10L));

        when(dao.findOneByStoreAndWarehouse(1L, "MWH.001"))
                .thenReturn(db);

        WarehouseProductAssignment input =
                new WarehouseProductAssignment("MWH.001", 1L);
        input.addProduct(10L);
        input.addProduct(20L);

        repository.save(input);

        assertEquals(2, db.products.size());
        assertTrue(db.products.stream().anyMatch(p -> p.productId == 20L));
    }

    @Test
    void shouldRemoveMissingProducts() {
        DbWarehouseAssignment db = createDb(1L, "MWH.001", Set.of(10L, 20L));

        when(dao.findOneByStoreAndWarehouse(1L, "MWH.001"))
                .thenReturn(db);

        WarehouseProductAssignment input =
                new WarehouseProductAssignment("MWH.001", 1L);
        input.addProduct(10L);

        repository.save(input);

        assertEquals(1, db.products.size());
        assertTrue(db.products.stream().anyMatch(p -> p.productId == 10L));
    }

    @Test
    void shouldNotDuplicateProducts() {
        DbWarehouseAssignment db = createDb(1L, "MWH.001", Set.of(10L));

        when(dao.findOneByStoreAndWarehouse(1L, "MWH.001"))
                .thenReturn(db);

        WarehouseProductAssignment input =
                new WarehouseProductAssignment("MWH.001", 1L);
        input.addProduct(10L);

        repository.save(input);

        assertEquals(1, db.products.size());
    }

    // ----------------------------
    // 🧪 FIND TESTS
    // ----------------------------

    @Test
    void shouldFindByStoreId() {
        DbWarehouseAssignment db = createDb(1L, "MWH.001", Set.of(10L, 20L));

        when(dao.findByStoreId(1L))
                .thenReturn(List.of(db));

        List<WarehouseProductAssignment> result =
                repository.findByStoreId(1L);

        assertEquals(1, result.size());
        assertEquals("MWH.001", result.get(0).getWarehouseBuCode());
        assertEquals(2, result.get(0).getProductIds().size());
    }

    @Test
    void shouldFindByStoreAndProduct() {
        DbWarehouseAssignment db = createDb(1L, "MWH.001", Set.of(10L));

        when(dao.findByStoreAndProduct(1L, 10L))
                .thenReturn(List.of(db));

        List<WarehouseProductAssignment> result =
                repository.findByStoreAndProduct(1L, 10L);

        assertEquals(1, result.size());
        assertTrue(result.get(0).getProductIds().contains(10L));
    }

    @Test
    void shouldReturnNull_whenFindByStoreAndWarehouseNotFound() {
        when(dao.findOneByStoreAndWarehouse(1L, "MWH.001"))
                .thenReturn(null);

        WarehouseProductAssignment result =
                repository.findByStoreAndWarehouse(1L, "MWH.001");

        assertNull(result);
    }

    // ----------------------------
    // 🔧 TEST HELPERS
    // ----------------------------

    private DbWarehouseAssignment createDb(Long storeId, String code, Set<Long> productIds) {
        DbWarehouseAssignment db = new DbWarehouseAssignment();
        db.storeId = storeId;
        db.warehouseCode = code;
        db.products = new HashSet<>();

        for (Long id : productIds) {
            DbWarehouseAssignmentProduct p = new DbWarehouseAssignmentProduct();
            p.productId = id;
            p.assignment = db;
            db.products.add(p);
        }

        return db;
    }
}