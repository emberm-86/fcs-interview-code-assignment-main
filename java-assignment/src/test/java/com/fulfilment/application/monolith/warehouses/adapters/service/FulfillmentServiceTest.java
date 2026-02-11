package com.fulfilment.application.monolith.warehouses.adapters.service;

import com.fulfilment.application.monolith.exception.ValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.WarehouseProductAssignment;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseProductAssignmentStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FulfillmentServiceTest {

    @InjectMocks
    FulfillmentService service;

    @Mock
    WarehouseProductAssignmentStore assignmentStore;

    private static final long STORE_ID = 1L;
    private static final long PRODUCT_ID = 1L;
    private static final String WAREHOUSE_ZWOLLE_BU_CODE = "MWH.001";
    private static final String WAREHOUSE_AMSTERDAM_BU_CODE = "MWH.012";
    private static final String WAREHOUSE_TILBURG_BU_CODE = "MWH.023";

    @Test
    void shouldAssignProduct_toExistingWarehouse_likeSeedData() {
        WarehouseProductAssignment assignment =
                new WarehouseProductAssignment(WAREHOUSE_ZWOLLE_BU_CODE, STORE_ID);

        when(assignmentStore.findByStoreAndWarehouse(STORE_ID, WAREHOUSE_ZWOLLE_BU_CODE))
                .thenReturn(assignment);
        when(assignmentStore.findByStoreAndProduct(STORE_ID, PRODUCT_ID))
                .thenReturn(List.of());

        service.validateAndSaveAssignment(STORE_ID, WAREHOUSE_ZWOLLE_BU_CODE, PRODUCT_ID);

        assertTrue(assignment.getProductIds().contains(PRODUCT_ID));

        verify(assignmentStore).save(argThat(saved ->
                saved.getProductIds().contains(PRODUCT_ID)
        ));
    }

    @Test
    void shouldCreateNewAssignment_likeWarehouseInsert() {
        when(assignmentStore.findByStoreAndWarehouse(STORE_ID, WAREHOUSE_ZWOLLE_BU_CODE))
                .thenReturn(null);
        when(assignmentStore.findByStoreAndProduct(STORE_ID, PRODUCT_ID))
                .thenReturn(List.of());
        when(assignmentStore.findByStoreId(STORE_ID))
                .thenReturn(List.of());

        WarehouseProductAssignment result =
                service.validateAndSaveAssignment(STORE_ID, WAREHOUSE_ZWOLLE_BU_CODE, PRODUCT_ID);

        assertNotNull(result);
        assertTrue(result.getProductIds().contains(PRODUCT_ID));
        assertEquals(1, result.getProductIds().size());
        verify(assignmentStore).save(result);
    }

    @Test
    void shouldPropagateException_whenWarehouseAtMaxProductCapacity() {
        WarehouseProductAssignment assignment =
                new WarehouseProductAssignment(WAREHOUSE_ZWOLLE_BU_CODE, STORE_ID);

        assignment.addProduct(1L);
        assignment.addProduct(2L);
        assignment.addProduct(3L);
        assignment.addProduct(4L);
        assignment.addProduct(5L);

        when(assignmentStore.findByStoreAndWarehouse(STORE_ID, WAREHOUSE_ZWOLLE_BU_CODE))
                .thenReturn(assignment);
        when(assignmentStore.findByStoreAndProduct(STORE_ID, 6L))
                .thenReturn(List.of());

        assertThrows(ValidationException.class,
                () -> service.validateAndSaveAssignment(
                        STORE_ID,
                        WAREHOUSE_ZWOLLE_BU_CODE,
                        6L
                ));
    }

    @Test
    void shouldIgnore_whenProductAlreadyExistsInWarehouse() {
        WarehouseProductAssignment assignment =
                new WarehouseProductAssignment(WAREHOUSE_ZWOLLE_BU_CODE, STORE_ID);

        assignment.addProduct(PRODUCT_ID);

        when(assignmentStore.findByStoreAndWarehouse(STORE_ID, WAREHOUSE_ZWOLLE_BU_CODE))
                .thenReturn(assignment);
        when(assignmentStore.findByStoreAndProduct(STORE_ID, PRODUCT_ID))
                .thenReturn(List.of(assignment));

        service.validateAndSaveAssignment(
                STORE_ID,
                WAREHOUSE_ZWOLLE_BU_CODE,
                PRODUCT_ID
        );

        assertTrue(assignment.getProductIds().contains(PRODUCT_ID));
        assertEquals(1, assignment.getProductIds().size());
        verify(assignmentStore).save(assignment);
    }

    @Test
    void shouldThrow_whenProductUsedInTooManyWarehouses_likeConstraint() {
        WarehouseProductAssignment w1 =
                new WarehouseProductAssignment("MWH.001", STORE_ID);
        WarehouseProductAssignment w2 =
                new WarehouseProductAssignment("WH2", STORE_ID);

        when(assignmentStore.findByStoreAndWarehouse(STORE_ID, WAREHOUSE_AMSTERDAM_BU_CODE))
                .thenReturn(null);
        when(assignmentStore.findByStoreAndProduct(STORE_ID, PRODUCT_ID))
                .thenReturn(List.of(w1, w2));

        assertThrows(ValidationException.class,
                () -> service.validateAndSaveAssignment(
                        STORE_ID,
                        WAREHOUSE_AMSTERDAM_BU_CODE,
                        PRODUCT_ID
                ));
        verify(assignmentStore, never()).save(any());
    }

    @Test
    void shouldAllow_whenAddingProductToExistingWarehouse_evenIfStoreHas3Warehouses() {
        WarehouseProductAssignment existing =
                new WarehouseProductAssignment(WAREHOUSE_ZWOLLE_BU_CODE, STORE_ID);

        WarehouseProductAssignment w2 =
                new WarehouseProductAssignment(WAREHOUSE_AMSTERDAM_BU_CODE, STORE_ID);

        WarehouseProductAssignment w3 =
                new WarehouseProductAssignment(WAREHOUSE_TILBURG_BU_CODE, STORE_ID);

        when(assignmentStore.findByStoreAndWarehouse(STORE_ID, WAREHOUSE_ZWOLLE_BU_CODE))
                .thenReturn(existing);

        when(assignmentStore.findByStoreAndProduct(STORE_ID, PRODUCT_ID))
                .thenReturn(List.of(existing, w2));

        when(assignmentStore.findByStoreId(STORE_ID))
                .thenReturn(List.of(existing, w2, w3));

        service.validateAndSaveAssignment(
                STORE_ID,
                WAREHOUSE_ZWOLLE_BU_CODE,
                PRODUCT_ID
        );
        assertTrue(existing.getProductIds().contains(PRODUCT_ID));
        verify(assignmentStore).save(existing);
    }

    @Test
    void shouldThrow_whenCreatingFourthWarehouseForStore() {
        WarehouseProductAssignment w1 =
                new WarehouseProductAssignment(WAREHOUSE_ZWOLLE_BU_CODE, STORE_ID);

        WarehouseProductAssignment w2 =
                new WarehouseProductAssignment(WAREHOUSE_AMSTERDAM_BU_CODE, STORE_ID);

        WarehouseProductAssignment w3 =
                new WarehouseProductAssignment(WAREHOUSE_TILBURG_BU_CODE, STORE_ID);

        when(assignmentStore.findByStoreAndWarehouse(STORE_ID, "WH4"))
                .thenReturn(null);
        when(assignmentStore.findByStoreId(STORE_ID))
                .thenReturn(List.of(w1, w2, w3));

        assertThrows(ValidationException.class,
                () -> service.validateAndSaveAssignment(
                        STORE_ID,
                        "WH4",
                        PRODUCT_ID
                ));

        verify(assignmentStore, never()).save(any());
    }

    @Test
    void shouldAllow_whenProductAlreadyAssignedToSameWarehouse_evenIfLimitReached() {
        WarehouseProductAssignment existing =
                new WarehouseProductAssignment(WAREHOUSE_ZWOLLE_BU_CODE, STORE_ID);

        WarehouseProductAssignment other =
                new WarehouseProductAssignment("WH2", STORE_ID);

        when(assignmentStore.findByStoreAndWarehouse(STORE_ID, WAREHOUSE_ZWOLLE_BU_CODE))
                .thenReturn(existing);
        when(assignmentStore.findByStoreAndProduct(STORE_ID, PRODUCT_ID))
                .thenReturn(List.of(existing, other));

        service.validateAndSaveAssignment(
                STORE_ID,
                WAREHOUSE_ZWOLLE_BU_CODE,
                PRODUCT_ID
        );

        verify(assignmentStore).save(existing);
    }
}