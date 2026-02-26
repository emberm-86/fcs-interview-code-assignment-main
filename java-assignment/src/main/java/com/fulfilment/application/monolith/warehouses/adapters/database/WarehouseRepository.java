package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WarehouseRepository.class);

  @Override
  public List<Warehouse> getAll() {
    return list("archivedAt is null").stream()
            .map(DbWarehouse::toWarehouse)
            .toList();
  }

  @Override
  @Transactional
  public void create(Warehouse warehouse) {
    DbWarehouse dbWarehouse = new DbWarehouse();

    dbWarehouse.businessUnitCode = warehouse.businessUnitCode;
    dbWarehouse.location = warehouse.location;
    dbWarehouse.capacity = warehouse.capacity;
    dbWarehouse.stock = warehouse.stock;

    persist(dbWarehouse);

    LOGGER.info("Warehouse has been created successfully with id: {}", dbWarehouse.id);
  }

  @Override
  @Transactional
  public void update(Warehouse warehouse) {
    DbWarehouse dbWarehouse = getDbWarehouseByBusinessUnitCode(warehouse.businessUnitCode);

    if (dbWarehouse == null) {
      throw new IllegalArgumentException("Warehouse with BU code " + warehouse.businessUnitCode + " not found");
    }

    dbWarehouse.location = warehouse.location;
    dbWarehouse.capacity = warehouse.capacity;
    dbWarehouse.stock = warehouse.stock;

    LOGGER.info("Warehouse has been updated successfully with id: {}", dbWarehouse.id);
  }

  @Override
  @Transactional
  public void removeById(String id) {
    DbWarehouse byId = findById(Long.valueOf(id));
    byId.archivedAt = LocalDateTime.now();
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse dbWarehouse = getDbWarehouseByBusinessUnitCode(buCode);
    if (dbWarehouse == null) return null;
    return dbWarehouse.toWarehouse();
  }

  public long countWareHousesByLocationId(String locationId) {
    return count("location = ?1 and archivedAt is null", locationId);
  }

  private DbWarehouse getDbWarehouseByBusinessUnitCode(String buCode) {
    return find("businessUnitCode = ?1 and archivedAt is null", buCode).firstResult();
  }
}