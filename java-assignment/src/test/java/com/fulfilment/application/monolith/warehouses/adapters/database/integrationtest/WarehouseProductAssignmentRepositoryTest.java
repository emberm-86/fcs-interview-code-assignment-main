package com.fulfilment.application.monolith.warehouses.adapters.database.integrationtest;

import com.fulfilment.application.monolith.db.FreshDbProfile;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseAssignmentDao;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseProductAssignmentRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.DbWarehouseAssignment;
import com.fulfilment.application.monolith.warehouses.domain.models.WarehouseProductAssignment;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestProfile(FreshDbProfile.class)
class WarehouseProductAssignmentRepositoryTest {

    @Inject
    WarehouseProductAssignmentRepository repository;

    @Inject
    WarehouseAssignmentDao warehouseAssignmentDao;

    PanacheRepository<DbWarehouseAssignment> panacheRepository;

    EntityManager em;

    private String warehouse1;
    private String warehouse2;
    private Long storeId;

    @BeforeEach
    @Transactional
    void clean() {
        panacheRepository = (PanacheRepository<DbWarehouseAssignment>) warehouseAssignmentDao;
        em = panacheRepository.getEntityManager();

        em.createQuery("DELETE FROM DbWarehouseAssignmentProduct")
                .executeUpdate();

        em.createQuery("DELETE FROM DbWarehouseAssignment")
                .executeUpdate();

        em.flush();
        em.clear();

        storeId = 1L;
        warehouse1 = "MWH.001";
        warehouse2 = "MWH.002";
    }

    // -------------------------------------------------------
    // 1. CREATE + READ
    // -------------------------------------------------------
    @Test
    @Transactional
    void shouldSaveAndFindByStoreAndWarehouse() {

        WarehouseProductAssignment assignment =
                new WarehouseProductAssignment(warehouse1, storeId);

        assignment.addProduct(1L);
        assignment.addProduct(2L);

        repository.save(assignment);

        em.flush();
        em.clear();

        WarehouseProductAssignment result =
                repository.findByStoreAndWarehouse(storeId, warehouse1);

        assertNotNull(result);
        assertEquals(2, result.getProductIds().size());
        assertTrue(result.getProductIds().contains(1L));
        assertTrue(result.getProductIds().contains(2L));
    }

    // -------------------------------------------------------
    // 2. OVERWRITE (FULL SYNC BEHAVIOR)
    // -------------------------------------------------------
    @Test
    @Transactional
    void shouldOverwriteProducts_whenSavingSameWarehouseAgain() {

        WarehouseProductAssignment initial =
                new WarehouseProductAssignment(warehouse1, storeId);

        initial.addProduct(1L);
        initial.addProduct(2L);

        repository.save(initial);

        em.flush();
        em.clear();

        WarehouseProductAssignment updated =
                new WarehouseProductAssignment(warehouse1, storeId);

        updated.addProduct(2L);
        updated.addProduct(3L);

        repository.save(updated);

        em.flush();
        em.clear();

        WarehouseProductAssignment result =
                repository.findByStoreAndWarehouse(storeId, warehouse1);

        assertNotNull(result);

        assertEquals(2, result.getProductIds().size());
        assertTrue(result.getProductIds().contains(2L));
        assertTrue(result.getProductIds().contains(3L));
        assertFalse(result.getProductIds().contains(1L));
    }

    // -------------------------------------------------------
    // 3. QUERY BY STORE + PRODUCT
    // -------------------------------------------------------
    @Test
    @Transactional
    void shouldFindByStoreAndProduct() {

        WarehouseProductAssignment w1 =
                new WarehouseProductAssignment(warehouse1, storeId);
        w1.addProduct(100L);
        repository.save(w1);

        WarehouseProductAssignment w2 =
                new WarehouseProductAssignment(warehouse2, storeId);
        w2.addProduct(100L);
        repository.save(w2);

        em.flush();
        em.clear();

        List<WarehouseProductAssignment> results =
                repository.findByStoreAndProduct(storeId, 100L);

        assertEquals(2, results.size());
    }

    // -------------------------------------------------------
    // 4. QUERY BY STORE ID
    // -------------------------------------------------------
    @Test
    @Transactional
    void shouldFindByStoreId() {

        long store = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;

        WarehouseProductAssignment w1 =
                new WarehouseProductAssignment(warehouse1, store);
        w1.addProduct(1L);
        repository.save(w1);

        WarehouseProductAssignment w2 =
                new WarehouseProductAssignment(warehouse2, store);
        w2.addProduct(2L);
        repository.save(w2);

        em.flush();
        em.clear();

        List<WarehouseProductAssignment> results =
                repository.findByStoreId(store);

        assertEquals(2, results.size());

        assertTrue(contains(results, warehouse1, 1L));
        assertTrue(contains(results, warehouse2, 2L));

        assertTrue(results.stream().allMatch(r -> r.getStoreId().equals(store)));
    }

    // -------------------------------------------------------
    // 5. NOT FOUND CASE
    // -------------------------------------------------------
    @Test
    @Transactional
    void shouldReturnNull_whenAssignmentNotFound() {

        WarehouseProductAssignment result =
                repository.findByStoreAndWarehouse(999L, "UNKNOWN");

        assertNull(result);
    }

    // -------------------------------------------------------
    // 6. IDENTITY BEHAVIOR (IMPORTANT EDGE CASE)
    // -------------------------------------------------------
    @Test
    @Transactional
    void shouldBeIdempotent_whenSavingSameStateTwice() {

        WarehouseProductAssignment assignment =
                new WarehouseProductAssignment(warehouse1, storeId);

        assignment.addProduct(1L);
        assignment.addProduct(2L);

        repository.save(assignment);
        repository.save(assignment); // second save same state

        em.flush();
        em.clear();

        WarehouseProductAssignment result =
                repository.findByStoreAndWarehouse(storeId, warehouse1);

        assertEquals(2, result.getProductIds().size());
    }

    // -------------------------------------------------------
    // helper
    // -------------------------------------------------------
    private boolean contains(List<WarehouseProductAssignment> list,
                             String warehouse,
                             Long productId) {
        return list.stream().anyMatch(a ->
                a.getWarehouseBuCode().equals(warehouse)
                        && a.getProductIds().contains(productId));
    }
}