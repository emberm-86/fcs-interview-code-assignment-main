package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.exception.ResourceNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore {

  private static final Logger LOGGER = LoggerFactory.getLogger(WarehouseRepository.class);

  @Inject
  WarehouseDao dao;

  @Override
  public List<Warehouse> getAll() {
    return dao.findAllNonArchived()
            .stream()
            .map(DbWarehouse::toWarehouse)
            .toList();
  }

  @Override
  @Transactional
  public void create(Warehouse warehouse) {
    DbWarehouse db = new DbWarehouse();

    db.businessUnitCode = warehouse.businessUnitCode;
    db.location = warehouse.location;
    db.capacity = warehouse.capacity;
    db.stock = warehouse.stock;

    dao.save(db);

    LOGGER.info("Warehouse created BU={}", db.businessUnitCode);
  }

  @Override
  @Transactional
  public void update(Warehouse warehouse) {
    DbWarehouse db = findActiveByBusinessUnitCode(warehouse.businessUnitCode);

    db.location = warehouse.location;
    db.capacity = warehouse.capacity;
    db.stock = warehouse.stock;
    db.archivedAt = warehouse.archivedAt;

    LOGGER.info("Warehouse updated BU={}", db.businessUnitCode);
  }

  @Override
  @Transactional
  public void removeById(long id) {
    DbWarehouse db = dao.findByIdNonArchived(id);

    if (db == null) {
      throw new ResourceNotFoundException("Warehouse not found id=" + id);
    }

    db.archivedAt = LocalDateTime.now();

    LOGGER.info("Warehouse archived id={}", id);
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse db = dao.findByBusinessCode(buCode);

    if (db == null) {
      return null;
    }

    return db.toWarehouse();
  }

  public long countByLocation(String location) {
    return dao.countByLocation(location);
  }

  public Warehouse findByIdW(Long id) {
    DbWarehouse db = dao.findByIdW(id);

    if (db == null) {
      return null;
    }

    return db.toWarehouse();
  }

  @Transactional
  public void cleanUp() {
    dao.cleanUp();
  }

  private DbWarehouse findActiveByBusinessUnitCode(String buCode) {
    DbWarehouse db = dao.findByBusinessCodeNonArchived(buCode);

    if (db == null) {
      throw new ResourceNotFoundException(
              "Warehouse with businessUnitCode " + buCode + " not found");
    }

    return db;
  }
}