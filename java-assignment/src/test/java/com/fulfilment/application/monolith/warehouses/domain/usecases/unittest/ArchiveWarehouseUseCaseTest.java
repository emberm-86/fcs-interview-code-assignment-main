package com.fulfilment.application.monolith.warehouses.domain.usecases.unittest;

import com.fulfilment.application.monolith.exception.ResourceNotFoundException;
import com.fulfilment.application.monolith.exception.ValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ArchiveWarehouseUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArchiveWarehouseUseCaseTest {

    @InjectMocks
    ArchiveWarehouseUseCase useCase;

    @Mock
    WarehouseStore warehouseStore;

    @Test
    void shouldArchiveWarehouseSuccessfully() {
        String businessUnitCode = "MWH.001";

        Warehouse storedWarehouse = Warehouse.builder()
                .businessUnitCode(businessUnitCode)
                .location("Zurich")
                .capacity(1000)
                .stock(200)
                .createdAt(LocalDateTime.now())
                .build();

        when(warehouseStore.findByBusinessUnitCode(businessUnitCode))
                .thenReturn(storedWarehouse);

        // when
        useCase.archive(storedWarehouse);

        // then
        ArgumentCaptor<Warehouse> captor = ArgumentCaptor.forClass(Warehouse.class);

        verify(warehouseStore).update(captor.capture());

        Warehouse updatedWarehouse = captor.getValue();

        assertNotNull(updatedWarehouse.archivedAt);
    }

    @Test
    void shouldThrowValidationException_whenWarehouseIsNull() {
        // when
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> useCase.archive(null)
        );

        // then
        assertEquals("Warehouse cannot be null", exception.getMessage());
        verifyNoInteractions(warehouseStore);
    }

    @Test
    void shouldThrowExceptionWhenWarehouseDoesNotExist() {
        // given
        Warehouse warehouse = Warehouse.builder()
                .businessUnitCode("BU-404")
                .location("Zurich")
                .capacity(1000)
                .stock(200)
                .build();

        when(warehouseStore.findByBusinessUnitCode("BU-404"))
                .thenThrow(new ResourceNotFoundException("Warehouse with businessUnitCode BU-404 not found"));

        // when
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> useCase.archive(warehouse)
        );

        // then
        assertEquals(
                "Warehouse with businessUnitCode BU-404 not found",
                exception.getMessage()
        );

        verify(warehouseStore).findByBusinessUnitCode("BU-404");
        verify(warehouseStore, never()).update(any());
    }
}