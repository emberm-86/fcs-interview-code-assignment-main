package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(WarehouseResourceImpl.class);

  @Inject private WarehouseRepository warehouseRepository;
  @Inject private LocationResolver locationResolver;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseRepository.getAll().stream().map(this::toWarehouseResponse).toList();
  }

  @Override
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    String businessUnitCode = data.getBusinessUnitCode();

    if (StringUtils.isBlank(businessUnitCode)) {
      throw new WebApplicationException("Business Unit Code cannot be empty for a warehouse!", 422);
    }

    Location location = locationResolver.resolveByIdentifier(data.getLocation());

    if (Objects.isNull(location)) {
      throw new WebApplicationException("Valid location should be defined for a warehouse!", 422);
    }

    if (location.maxNumberOfWarehouses == warehouseRepository.countWareHousesByLocationId(location.identification)) {
      throw new WebApplicationException("Maximum number of warehouses reached, cannot create a new one!", 422);
    }

    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse
            existingWarehouse = warehouseRepository.findByBusinessUnitCode(businessUnitCode);

    if (Objects.nonNull(existingWarehouse)) {
      throw new WebApplicationException("Warehouse with Business Unit Code: " + businessUnitCode + " already exists.", 409);
    }

    warehouseRepository.create(fromWarehouseRequestToModel(data));
    return data;
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    DbWarehouse byId = warehouseRepository.findById(Long.valueOf(id));
    return toWarehouseResponse(byId.toWarehouse());
  }

  @Override
  public void archiveAWarehouseUnitByID(String id) {
    if (StringUtils.isBlank(id)) {
      throw new WebApplicationException("Id cannot be empty", 422);
    }
    warehouseRepository.removeById(id);
  }

  @Override
  public Warehouse replaceTheCurrentActiveWarehouse(
      String businessUnitCode, @NotNull Warehouse data) {

    if (StringUtils.isBlank(businessUnitCode)) {
      throw new WebApplicationException("Business Unit Code cannot be empty for warehouse", 422);
    }

    Location location = locationResolver.resolveByIdentifier(data.getLocation());

    if (Objects.nonNull(location)) {
      throw new WebApplicationException("Valid location should be defined for warehouse", 422);
    }

    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse existingWarehouse = warehouseRepository.findByBusinessUnitCode(businessUnitCode);

    if (Objects.isNull(existingWarehouse)) {
      throw new WebApplicationException("Warehouse with businessUnitCode of " + businessUnitCode + " does not exist.", 404);
    }

    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse newWarehouse = fromWarehouseRequestToModel(data);

    existingWarehouse.businessUnitCode = newWarehouse.businessUnitCode;
    existingWarehouse.location = newWarehouse.location;
    existingWarehouse.capacity = newWarehouse.capacity;
    existingWarehouse.stock = newWarehouse.stock;

    warehouseRepository.update(existingWarehouse);

    return toWarehouseResponse(existingWarehouse);
  }

  private Warehouse toWarehouseResponse(
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
    var response = new Warehouse();

    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);

    return response;
  }

  private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse fromWarehouseRequestToModel(Warehouse request) {
    var warehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();

    warehouse.businessUnitCode = request.getBusinessUnitCode();
    warehouse.location = request.getLocation();
    warehouse.capacity = request.getCapacity();
    warehouse.stock = request.getStock();

    return warehouse;
  }

  @Provider
  public static class ErrorMapper implements ExceptionMapper<Exception> {

    @Inject
    ObjectMapper objectMapper;

    @Override
    public Response toResponse(Exception exception) {
      LOGGER.error("Failed to handle request", exception);

      int code = 500;
      if (exception instanceof WebApplicationException) {
        code = ((WebApplicationException) exception).getResponse().getStatus();
      }

      ObjectNode exceptionJson = objectMapper.createObjectNode();
      exceptionJson.put("exceptionType", exception.getClass().getName());
      exceptionJson.put("code", code);

      if (exception.getMessage() != null) {
        exceptionJson.put("error", exception.getMessage());
      }

      return Response.status(code).entity(exceptionJson).build();
    }
  }
}
