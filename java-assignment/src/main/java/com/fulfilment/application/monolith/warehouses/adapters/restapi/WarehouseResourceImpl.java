package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.mappers.WareHouseMapper;
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
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Objects;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  private static final Logger LOGGER = Logger.getLogger(WarehouseResource.class.getName());

  @Inject private WarehouseRepository warehouseRepository;
  @Inject private WareHouseMapper warehouseMapper;
  @Inject private LocationResolver locationResolver;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
     // return warehouseRepository.getAll().stream().map(this::toWarehouseResponse).toList();
    return warehouseRepository.getAll().stream()
            .map(warehouseMapper::fromModelToResponse)
            .toList();
  }

  @Override
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    String businessUnitCode = data.getBusinessUnitCode();

    if (StringUtils.isBlank(businessUnitCode)) {
      throw new WebApplicationException("Business Unit Code cannot be empty for warehouse", 422);
    }

    Location location = locationResolver.resolveByIdentifier(data.getLocation());

    if (Objects.isNull(location)) {
      throw new WebApplicationException("Valid location should be defined for warehouse", 422);
    }

    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse
            existingWarehouse = warehouseRepository.findByBusinessUnitCode(businessUnitCode);

    if (Objects.nonNull(existingWarehouse)) {
      throw new WebApplicationException("Warehouse with Business Unit Code: " + businessUnitCode + " already exists.", 409);
    }

    warehouseRepository.create(warehouseMapper.fromRequestToModel(data));
    return data;
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    DbWarehouse byId = warehouseRepository.findById(Long.valueOf(id));
    return warehouseMapper.fromModelToResponse(warehouseMapper.fromEntityToModel(byId));
  }

  @Override
  public void archiveAWarehouseUnitByID(String id) {
    if (StringUtils.isBlank(id)) {
      throw new WebApplicationException("Id cannot be empty", 422);
    }

    DbWarehouse byId = warehouseRepository.findById(Long.valueOf(id));

    if (Objects.isNull(byId)) {
      return;
    }
    warehouseRepository.remove(warehouseMapper.fromEntityToModel(byId));
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

    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse newWarehouse = warehouseMapper.fromRequestToModel(data);
    warehouseMapper.updateModel(newWarehouse, existingWarehouse);
    warehouseRepository.update(existingWarehouse);

    return warehouseMapper.fromModelToResponse(existingWarehouse);
  }

  /*
  private Warehouse toWarehouseResponse(
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
    var response = new Warehouse();

    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);

    return response;
  }*/

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
