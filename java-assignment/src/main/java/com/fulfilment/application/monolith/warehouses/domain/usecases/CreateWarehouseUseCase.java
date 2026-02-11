package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.exception.ResourceConflictException;
import com.fulfilment.application.monolith.exception.ValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang.StringUtils;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void create(Warehouse warehouse) {
    if (warehouse == null) {
      throw new ValidationException("Warehouse cannot be null");
    }

    if (StringUtils.isBlank(warehouse.businessUnitCode)) {
      throw new ValidationException("Business unit code cannot be empty");
    }

    Warehouse existing =
            warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode);

    if (existing != null) {
      throw new ResourceConflictException(
              "Warehouse with businessUnitCode " + warehouse.businessUnitCode + " already exists");
    }

    warehouseStore.create(warehouse);
  }
}
