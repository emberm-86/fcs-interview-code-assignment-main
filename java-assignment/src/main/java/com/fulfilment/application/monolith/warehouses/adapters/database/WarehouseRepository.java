package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.mappers.WareHouseMapper;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

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
    this.persistAndFlush(warehouseMapper.fromModelToEntity(warehouse));
  }

  @Override
  @Transactional
  public void update(Warehouse warehouse) {
    DbWarehouse dbWarehouse = getDbWarehouseByBusinessUnitCode(warehouse.businessUnitCode);
    warehouseMapper.updateEntity(warehouse, dbWarehouse);
    this.persistAndFlush(dbWarehouse);
  }

  @Override
  @Transactional
  public void remove(Warehouse warehouse) {
    DbWarehouse existingDbWarehouse = getDbWarehouseByBusinessUnitCode(warehouse.businessUnitCode);
    existingDbWarehouse.archivedAt = LocalDateTime.now();
    this.persistAndFlush(existingDbWarehouse);
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse dbWarehouse = getDbWarehouseByBusinessUnitCode(buCode);
    return warehouseMapper.fromEntityToModel(dbWarehouse);
  }

  private DbWarehouse getDbWarehouseByBusinessUnitCode(String warehouse) {
    return this.listAll().stream()
            .filter(it -> it.businessUnitCode.equals(warehouse))
            .findFirst().orElse(null);
  }
}
