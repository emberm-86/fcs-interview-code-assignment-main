package com.fulfilment.application.monolith.warehouses.domain.usecases.unittest;

import com.fulfilment.application.monolith.exception.ResourceNotFoundException;
import com.fulfilment.application.monolith.exception.ValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ReplaceWarehouseUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReplaceWarehouseUseCaseTest {

    @InjectMocks
    ReplaceWarehouseUseCase useCase;

    @Mock
    WarehouseStore warehouseStore;

    @Test
    void shouldReplaceWarehouseSuccessfully() {
        String code = "MWH.001";

        Warehouse existing = Warehouse.builder()
                .businessUnitCode(code)
                .capacity(10)
                .stock(5)
                .location("ZWOLLE-001")
                .build();

        Warehouse newWarehouse = Warehouse.builder()
                .businessUnitCode(code)
                .capacity(20)
                .stock(5)
                .location("AMSTERDAM-001")
                .build();

        when(warehouseStore.findByBusinessUnitCode(code))
                .thenReturn(existing);

        // when
        useCase.replace(newWarehouse);

        // then
        ArgumentCaptor<Warehouse> captor = ArgumentCaptor.forClass(Warehouse.class);
        verify(warehouseStore).update(captor.capture());

        Warehouse updated = captor.getValue();

        assertEquals(20, updated.capacity);
        assertEquals(5, updated.stock);
        assertEquals("AMSTERDAM-001", updated.location);
    }

    @Test
    void shouldThrowValidationException_whenWarehouseIsNull() {
        assertThrows(ValidationException.class, () -> useCase.replace(null));
        verifyNoInteractions(warehouseStore);
    }

    @Test
    void shouldThrowValidation_whenStockDoesNotMatch() {
        // given
        String businessUnitCode = "MWH.001";

        Warehouse existing = Warehouse.builder()
                .businessUnitCode(businessUnitCode)
                .location("Zurich")
                .capacity(1000)
                .stock(5)
                .createdAt(LocalDateTime.now())
                .build();

        Warehouse newWarehouse = Warehouse.builder()
                .businessUnitCode(businessUnitCode)
                .location("Zurich")
                .capacity(1000)
                .stock(6)
                .createdAt(LocalDateTime.now())
                .build();

        when(warehouseStore.findByBusinessUnitCode(businessUnitCode))
                .thenReturn(existing);

        // when + then
        assertThrows(ValidationException.class,
                () -> useCase.replace(newWarehouse));

        verify(warehouseStore).findByBusinessUnitCode(businessUnitCode);
        verify(warehouseStore, never()).update(any());
    }

    @Test
    void shouldThrowResourceNotFound_whenWarehouseDoesNotExist() {
        // given
        String businessUnitCode = "BU-404";

        Warehouse newWarehouse = Warehouse.builder()
                .businessUnitCode(businessUnitCode)
                .location("Zurich")
                .capacity(1000)
                .stock(200)
                .createdAt(LocalDateTime.now())
                .build();

        when(warehouseStore.findByBusinessUnitCode(businessUnitCode))
                .thenReturn(null);

        // when + then
        assertThrows(ResourceNotFoundException.class,
                () -> useCase.replace(newWarehouse));

        verify(warehouseStore).findByBusinessUnitCode(businessUnitCode);
        verify(warehouseStore, never()).update(any());
    }
}