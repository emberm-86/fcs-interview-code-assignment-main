package com.fulfilment.application.monolith.warehouses.adapters.service;

import com.fulfilment.application.monolith.exception.ResourceConflictException;
import com.fulfilment.application.monolith.exception.ResourceNotFoundException;
import com.fulfilment.application.monolith.exception.ValidationException;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.warehouse.api.beans.Warehouse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {

    @Mock
    WarehouseRepository warehouseRepository;

    @Mock
    LocationResolver locationResolver;

    @InjectMocks
    WarehouseService warehouseService;

    private Warehouse validRequest;
    private Location validLocation;

    @BeforeEach
    void setUp() {
        validRequest = new Warehouse();
        validRequest.setBusinessUnitCode("MWH.001");
        validRequest.setLocation("ZWOLLE-001");
        validRequest.setCapacity(100);
        validRequest.setStock(10);

        validLocation = new Location("ZWOLLE-001", 1, 40);
    }

    // =========================
    // CREATE
    // =========================

    @Test
    void shouldCreateWarehouseSuccessfully() {
        when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(validLocation);
        when(warehouseRepository.countByLocation("ZWOLLE-001")).thenReturn(0L);
        when(warehouseRepository.findByBusinessUnitCode("MWH.001")).thenReturn(null);

        Warehouse result = warehouseService.createANewWarehouseUnit(validRequest);

        assertEquals("MWH.001", result.getBusinessUnitCode());
        verify(warehouseRepository).create(any());
    }

    @Test
    void shouldThrowWhenBusinessUnitCodeIsBlank() {
        validRequest.setBusinessUnitCode(" ");

        assertThrows(ValidationException.class,
                () -> warehouseService.createANewWarehouseUnit(validRequest));
    }

    @Test
    void shouldThrowWhenLocationIsInvalid() {
        when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(null);

        assertThrows(ValidationException.class,
                () -> warehouseService.createANewWarehouseUnit(validRequest));
    }

    @Test
    void shouldThrowWhenLocationCapacityExceeded() {
        when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(validLocation);
        when(warehouseRepository.countByLocation("ZWOLLE-001")).thenReturn(2L);

        assertThrows(ValidationException.class,
                () -> warehouseService.createANewWarehouseUnit(validRequest));
    }

    @Test
    void shouldThrowWhenBusinessUnitAlreadyExists() {
        when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(validLocation);
        when(warehouseRepository.countByLocation("ZWOLLE-001")).thenReturn(0L);
        when(warehouseRepository.findByBusinessUnitCode("MWH.001"))
                .thenReturn(mock(com.fulfilment.application.monolith.warehouses.domain.models.Warehouse.class));

        assertThrows(ResourceConflictException.class,
                () -> warehouseService.createANewWarehouseUnit(validRequest));
    }

    // =========================
    // GET
    // =========================

    @Test
    void shouldReturnWarehouseById() {
        var domainWarehouse = com.fulfilment.application.monolith.warehouses.domain.models.Warehouse
                .builder()
                .businessUnitCode("MWH.001")
                .location("ZWOLLE-001")
                .capacity(100)
                .stock(10)
                .build();

        when(warehouseRepository.findByIdW(1L)).thenReturn(domainWarehouse);

        Warehouse result = warehouseService.getAWarehouseUnitByID(1L);

        assertEquals("MWH.001", result.getBusinessUnitCode());
    }

    @Test
    void shouldThrowWhenWarehouseNotFoundById() {
        when(warehouseRepository.findByIdW(1L)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> warehouseService.getAWarehouseUnitByID(1L));
    }

    // =========================
    // ARCHIVE
    // =========================

    @Test
    void shouldArchiveWarehouse() {
        com.fulfilment.application.monolith.warehouses.domain.models.Warehouse
                existing = mock(com.fulfilment.application.monolith.warehouses.domain.models.Warehouse.class);
        when(warehouseRepository.findByIdW(1L)).thenReturn(existing);

        warehouseService.archiveAWarehouseUnitByID(1L);

        verify(warehouseRepository).removeById(1L);
        verify(warehouseRepository).findByIdW(1L);
        verifyNoMoreInteractions(warehouseRepository);
    }

    @Test
    void shouldThrowWhenArchivingNonExistingWarehouse() {
        when(warehouseRepository.findByIdW(1L)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> warehouseService.getAWarehouseUnitByID(1L));
    }

    // =========================
    // REPLACE
    // =========================

    @Test
    void shouldReplaceWarehouseSuccessfully() {
        var existing = com.fulfilment.application.monolith.warehouses.domain.models.Warehouse
                .builder()
                .businessUnitCode("MWH.001")
                .location("ZWOLLE-001")
                .capacity(50)
                .stock(5)
                .build();

        when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(validLocation);
        when(warehouseRepository.findByBusinessUnitCode("MWH.001")).thenReturn(existing);

        Warehouse result = warehouseService.replaceTheCurrentActiveWarehouse("MWH.001", validRequest);

        assertEquals(100, result.getCapacity());
        verify(warehouseRepository).update(existing);
    }

    @Test
    void shouldThrowWhenReplacingNonExistingWarehouse() {
        when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(validLocation);
        when(warehouseRepository.findByBusinessUnitCode("MWH.001")).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> warehouseService.replaceTheCurrentActiveWarehouse("MWH.001", validRequest));
    }

    @Test
    void shouldValidateCapacityWhenLocationChanges() {
        var existing = com.fulfilment.application.monolith.warehouses.domain.models.Warehouse
                .builder()
                .businessUnitCode("MWH.001")
                .location("OLD_LOC")
                .capacity(50)
                .stock(5)
                .build();

        when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(validLocation);
        when(warehouseRepository.findByBusinessUnitCode("MWH.001")).thenReturn(existing);
        when(warehouseRepository.countByLocation("ZWOLLE-001")).thenReturn(2L);

        assertThrows(ValidationException.class,
                () -> warehouseService.replaceTheCurrentActiveWarehouse("MWH.001", validRequest));
    }
}
