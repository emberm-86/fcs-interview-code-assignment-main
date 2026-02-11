package com.fulfilment.application.monolith.warehouses.adapters.restapi.unittest;

import com.fulfilment.application.monolith.warehouses.adapters.restapi.WarehouseResourceImpl;
import com.fulfilment.application.monolith.warehouses.adapters.service.WarehouseService;
import com.warehouse.api.beans.Warehouse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseResourceImplTest {

    private static final long WAREHOUSE_ID = 1L;

    @Mock
    WarehouseService warehouseService;

    @InjectMocks
    WarehouseResourceImpl resource;

    @Test
    void shouldReturnAllWarehouses() {

        List<Warehouse> expected = List.of(
                new Warehouse(),
                new Warehouse()
        );

        when(warehouseService.listAllWarehousesUnits())
                .thenReturn(expected);

        List<Warehouse> result = resource.listAllWarehousesUnits();

        assertEquals(2, result.size());
        assertSame(expected, result);

        verify(warehouseService).listAllWarehousesUnits();
    }

    @Test
    void shouldCreateWarehouse() {

        Warehouse input = new Warehouse();
        Warehouse created = new Warehouse();

        when(warehouseService.createANewWarehouseUnit(input))
                .thenReturn(created);

        Warehouse result = resource.createANewWarehouseUnit(input);

        assertNotNull(result);
        assertSame(created, result);

        verify(warehouseService).createANewWarehouseUnit(input);
    }

    @Test
    void shouldReturnWarehouseById() {
        Warehouse warehouse = new Warehouse();

        when(warehouseService.getAWarehouseUnitByID(WAREHOUSE_ID))
                .thenReturn(warehouse);

        Warehouse result = resource.getAWarehouseUnitByID(WAREHOUSE_ID);

        assertSame(warehouse, result);

        verify(warehouseService).getAWarehouseUnitByID(WAREHOUSE_ID);
    }

    @Test
    void shouldArchiveWarehouse() {

        doNothing().when(warehouseService)
                .archiveAWarehouseUnitByID(WAREHOUSE_ID);

        resource.archiveAWarehouseUnitByID(WAREHOUSE_ID);

        verify(warehouseService).archiveAWarehouseUnitByID(WAREHOUSE_ID);
    }

    @Test
    void shouldReplaceWarehouse() {

        Warehouse input = new Warehouse();
        Warehouse updated = new Warehouse();

        when(warehouseService.replaceTheCurrentActiveWarehouse("MWH.001", input))
                .thenReturn(updated);

        Warehouse result =
                resource.replaceTheCurrentActiveWarehouse("MWH.001", input);

        assertSame(updated, result);

        verify(warehouseService)
                .replaceTheCurrentActiveWarehouse("MWH.001", input);
    }
}
