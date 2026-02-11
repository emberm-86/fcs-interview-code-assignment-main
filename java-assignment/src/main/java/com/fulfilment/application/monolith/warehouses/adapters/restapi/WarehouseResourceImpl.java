package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.service.WarehouseService;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import org.jboss.resteasy.reactive.ResponseStatus;

import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject
  WarehouseService warehouseService;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseService.listAllWarehousesUnits();
  }

  @Override
  @POST
  @Produces("application/json")
  @Consumes("application/json")
  @ResponseStatus(201)
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    return warehouseService.createANewWarehouseUnit(data);
  }

  @Override
  public Warehouse getAWarehouseUnitByID(long id) {
    return warehouseService.getAWarehouseUnitByID(id);
  }

  @Override
  public void archiveAWarehouseUnitByID(long id) {
    warehouseService.archiveAWarehouseUnitByID(id);
  }

  @Override
  public Warehouse replaceTheCurrentActiveWarehouse(String businessUnitCode, @NotNull Warehouse data) {
    return warehouseService.replaceTheCurrentActiveWarehouse(businessUnitCode, data);
  }
}
