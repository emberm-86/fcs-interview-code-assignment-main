package com.fulfilment.application.monolith.warehouses.adapters.restapi.unittest;

import com.fulfilment.application.monolith.warehouses.adapters.restapi.FulfillmentResourceImpl;
import com.fulfilment.application.monolith.warehouses.adapters.service.FulfillmentService;
import com.fulfilment.application.monolith.warehouses.domain.models.WarehouseProductAssignment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FulfillmentResourceImplTest {

    @Mock
    FulfillmentService fulfillmentService;

    @InjectMocks
    FulfillmentResourceImpl resource;

    @Test
    void shouldReturnAssignment_whenValidRequest() {

        WarehouseProductAssignment expected =
                new WarehouseProductAssignment("MWH.002", 2L);

        when(fulfillmentService.validateAndSaveAssignment(2L, "MWH.002", 2L))
                .thenReturn(expected);

        WarehouseProductAssignment result =
                resource.assignProductToWarehouse(2L, "MWH.002", 2L);

        assertNotNull(result);
        assertEquals("MWH.002", result.getWarehouseBuCode());
        assertEquals(2L, result.getStoreId());

        verify(fulfillmentService)
                .validateAndSaveAssignment(2L, "MWH.002", 2L);
    }

    @Test
    void shouldThrowException_whenServiceFails() {

        when(fulfillmentService.validateAndSaveAssignment(anyLong(), anyString(), anyLong()))
                .thenThrow(new RuntimeException("maximum of 5 product types"));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> resource.assignProductToWarehouse(1L, "MWH.001", 6L)
        );

        assertEquals("maximum of 5 product types", ex.getMessage());
    }

    @Test
    void shouldCallServiceWithCorrectParameters() {

        WarehouseProductAssignment assignment =
                new WarehouseProductAssignment("MWH.003", 1L);

        when(fulfillmentService.validateAndSaveAssignment(anyLong(), anyString(), anyLong()))
                .thenReturn(assignment);

        resource.assignProductToWarehouse(1L, "MWH.003", 3L);

        verify(fulfillmentService)
                .validateAndSaveAssignment(1L, "MWH.003", 3L);
    }
}