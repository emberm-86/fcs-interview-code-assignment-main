package com.fulfilment.application.monolith.warehouses.adapters.service;

import com.fulfilment.application.monolith.exception.BusinessException;
import com.fulfilment.application.monolith.exception.ResourceConflictException;
import com.fulfilment.application.monolith.exception.ResourceNotFoundException;
import com.fulfilment.application.monolith.exception.ValidationException;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang.StringUtils;

import java.util.List;

@ApplicationScoped
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final LocationResolver locationResolver;

    @Inject
    public WarehouseService(WarehouseRepository warehouseRepository,
                            LocationResolver locationResolver) {
        this.warehouseRepository = warehouseRepository;
        this.locationResolver = locationResolver;
    }

    public List<Warehouse> listAllWarehousesUnits() {
        return warehouseRepository.getAll()
                .stream()
                .map(this::toWarehouseResponse)
                .toList();
    }

    public Warehouse createANewWarehouseUnit(Warehouse data) {
        validateWarehouseRequest(data);

        String businessUnitCode = data.getBusinessUnitCode();
        Location location = resolveAndValidateLocation(data.getLocation());

        validateLocationCapacity(location);

        if (warehouseRepository.findByBusinessUnitCode(businessUnitCode) != null) {
            throw new ResourceConflictException(
                    "Warehouse with Business Unit Code: " + businessUnitCode + " already exists."
            );
        }

        var model = fromWarehouseRequestToModel(data);
        warehouseRepository.create(model);

        return toWarehouseResponse(model);
    }

    public Warehouse getAWarehouseUnitByID(Long id) {
        com.fulfilment.application.monolith.warehouses.domain.models.Warehouse existing = warehouseRepository.findByIdW(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Warehouse with id " + id + " not found.");
        }

        return toWarehouseResponse(existing);
    }

    public void archiveAWarehouseUnitByID(long id) throws BusinessException {
        com.fulfilment.application.monolith.warehouses.domain.models.Warehouse existing = warehouseRepository.findByIdW(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Warehouse with id " + id + " not found.");
        }

        warehouseRepository.removeById(id);
    }

    public Warehouse replaceTheCurrentActiveWarehouse(String businessUnitCode, Warehouse data) throws BusinessException {
        if (StringUtils.isBlank(businessUnitCode)) {
            throw new ValidationException("Business Unit Code cannot be empty for warehouse");
        }

        validateWarehouseRequest(data);

        Location newLocation = resolveAndValidateLocation(data.getLocation());

        var existingWarehouse = warehouseRepository.findByBusinessUnitCode(businessUnitCode);
        if (existingWarehouse == null) {
            throw new ResourceNotFoundException(
                    "Warehouse with businessUnitCode of " + businessUnitCode + " does not exist."
            );
        }

        // Check capacity ONLY if location changes
        if (!existingWarehouse.location.equals(newLocation.identification)) {
            validateLocationCapacity(newLocation);
        }

        // Explicit field updates (no hidden side effects)
        existingWarehouse.location = data.getLocation();
        existingWarehouse.capacity = data.getCapacity();
        existingWarehouse.stock = data.getStock();

        warehouseRepository.update(existingWarehouse);

        return toWarehouseResponse(existingWarehouse);
    }

    // =========================
    // Helpers
    // =========================

    private void validateWarehouseRequest(Warehouse data) {
        if (data == null) {
            throw new ValidationException("Warehouse data cannot be null!");
        }

        if (StringUtils.isBlank(data.getBusinessUnitCode())) {
            throw new ValidationException("Business Unit Code cannot be empty!");
        }

        if (StringUtils.isBlank(data.getLocation())) {
            throw new ValidationException("Location cannot be empty!");
        }
    }

    private Location resolveAndValidateLocation(String locationId) {
        Location location = locationResolver.resolveByIdentifier(locationId);

        if (location == null) {
            throw new ValidationException("Valid location should be defined for warehouse!");
        }

        return location;
    }

    private void validateLocationCapacity(Location location) {
        long current = warehouseRepository.countByLocation(location.identification);

        if (current >= location.maxNumberOfWarehouses) {
            throw new ValidationException(
                    "Maximum number of warehouses reached for location: " + location.identification
            );
        }
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

    private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse
    fromWarehouseRequestToModel(Warehouse request) {

        return com.fulfilment.application.monolith.warehouses.domain.models.Warehouse
                .builder()
                .businessUnitCode(request.getBusinessUnitCode())
                .location(request.getLocation())
                .capacity(request.getCapacity())
                .stock(request.getStock())
                .build();
    }
}