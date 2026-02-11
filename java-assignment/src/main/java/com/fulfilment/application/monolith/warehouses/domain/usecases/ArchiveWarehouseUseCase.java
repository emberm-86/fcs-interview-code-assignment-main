package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.exception.ResourceNotFoundException;
import com.fulfilment.application.monolith.exception.ValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;

@ApplicationScoped
public class ArchiveWarehouseUseCase implements ArchiveWarehouseOperation {

  private final WarehouseStore warehouseStore;

  public ArchiveWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void archive(Warehouse warehouse) {
    if (warehouse == null) {
      throw new ValidationException("Warehouse cannot be null");
    }

    Warehouse storedWarehouse =
            warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode);

    if (storedWarehouse == null) {
      throw new ResourceNotFoundException(
              "Warehouse with businessUnitCode " + warehouse.businessUnitCode + " not found");
    }
    warehouseStore.update(storedWarehouse.archive(LocalDateTime.now()));
  }
}
