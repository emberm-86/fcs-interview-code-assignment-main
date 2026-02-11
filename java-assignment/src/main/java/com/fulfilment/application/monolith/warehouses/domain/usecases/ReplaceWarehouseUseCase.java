package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.exception.ResourceNotFoundException;
import com.fulfilment.application.monolith.exception.ValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    if (newWarehouse == null) {
      throw new ValidationException("Warehouse cannot be null");
    }

    Warehouse existing =
            warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);

    if (existing == null) {
      throw new ResourceNotFoundException("Warehouse not found!");
    }

    existing.replaceWith(newWarehouse);

    warehouseStore.update(existing);
  }
}
