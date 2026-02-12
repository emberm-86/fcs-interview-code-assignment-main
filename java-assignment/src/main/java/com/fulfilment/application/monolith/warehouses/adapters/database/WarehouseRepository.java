package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.mappers.WareHouseMapper;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WarehouseRepository.class);

  @Inject
  private WareHouseMapper warehouseMapper;

  @Override
  public List<Warehouse> getAll() {
    return this.listAll().stream()
            .filter(warehouse -> Objects.isNull(warehouse.archivedAt))
            .map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  @Transactional
  public void create(Warehouse warehouse) {
    DbWarehouse entity = warehouseMapper.fromModelToEntity(warehouse);
    this.persistAndFlush(entity);
    LOGGER.info("Warehouse has been created successfully with id: {}", entity.id);
  }

  @Override
  @Transactional
  public void update(Warehouse warehouse) {
    DbWarehouse dbWarehouse = getDbWarehouseByBusinessUnitCode(warehouse.businessUnitCode);
    warehouseMapper.updateEntity(warehouse, dbWarehouse);
    this.persistAndFlush(dbWarehouse);
    LOGGER.info("Warehouse has been updated successfully with id: {}", dbWarehouse.id);
  }

  @Override
  @Transactional
  public void remove(Warehouse warehouse) {
    DbWarehouse existingDbWarehouse = getDbWarehouseByBusinessUnitCode(warehouse.businessUnitCode);
    existingDbWarehouse.archivedAt = LocalDateTime.now();
    this.persistAndFlush(existingDbWarehouse);
    LOGGER.info("Warehouse has been archived successfully with id: {}", existingDbWarehouse.id);
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse dbWarehouse = getDbWarehouseByBusinessUnitCode(buCode);
    return warehouseMapper.fromEntityToModel(dbWarehouse);
  }

  public long countWareHousesByLocationId(String locationId) {
    return getAll().stream()
            .filter(warehouse -> warehouse.location.equals(locationId))
            .count();
  }

  private DbWarehouse getDbWarehouseByBusinessUnitCode(String warehouse) {
    return this.listAll().stream()
            .filter(it -> it.businessUnitCode.equals(warehouse))
            .findFirst().orElse(null);
  }
}
