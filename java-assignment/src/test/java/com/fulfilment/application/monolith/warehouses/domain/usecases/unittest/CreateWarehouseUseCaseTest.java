package com.fulfilment.application.monolith.warehouses.domain.usecases.unittest;

import com.fulfilment.application.monolith.exception.ResourceConflictException;
import com.fulfilment.application.monolith.exception.ValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateWarehouseUseCaseTest {

    @InjectMocks
    CreateWarehouseUseCase useCase;

    @Mock
    WarehouseStore warehouseStore;

    @Test
    void shouldCreateWarehouseSuccessfully() {
        // given
        String businessUnitCode = "MWH.001";
        Warehouse warehouse = Warehouse.builder()
                .businessUnitCode(businessUnitCode)
                .location("Zurich")
                .capacity(1000)
                .stock(200)
                .build();

        when(warehouseStore.findByBusinessUnitCode(businessUnitCode))
                .thenReturn(null);

        // when
        useCase.create(warehouse);

        // then
        verify(warehouseStore).create(warehouse);
    }

    @Test
    void shouldThrowValidationException_whenWarehouseIsNull() {
        assertThrows(ValidationException.class,
                () -> useCase.create(null));

        verifyNoInteractions(warehouseStore);
    }

    @Test
    void shouldThrowExceptionWhenWarehouseAlreadyExists() {
        // given
        String businessUnitCode = "MWH.001";

        Warehouse existing = Warehouse.builder()
                .businessUnitCode(businessUnitCode)
                .location("Zurich")
                .capacity(1000)
                .stock(200)
                .build();

        when(warehouseStore.findByBusinessUnitCode(businessUnitCode))
                .thenReturn(existing);

        Warehouse newWarehouse = Warehouse.builder()
                .businessUnitCode(businessUnitCode)
                .location("Zurich")
                .capacity(1000)
                .stock(200)
                .build();

        // when + then
        ResourceConflictException ex = assertThrows(
                ResourceConflictException.class,
                () -> useCase.create(newWarehouse)
        );

        assertEquals(
                "Warehouse with businessUnitCode " + businessUnitCode + " already exists",
                ex.getMessage()
        );

        verify(warehouseStore).findByBusinessUnitCode(businessUnitCode);
        verify(warehouseStore, never()).create(any());
    }
}
