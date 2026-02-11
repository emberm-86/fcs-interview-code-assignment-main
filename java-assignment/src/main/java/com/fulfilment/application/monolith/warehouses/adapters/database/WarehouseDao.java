package com.fulfilment.application.monolith.warehouses.adapters.database;

import java.util.List;

public interface WarehouseDao {
    List<DbWarehouse> findAllNonArchived();
    DbWarehouse findByIdNonArchived(Long id);
    DbWarehouse findByIdW(Long id);
    DbWarehouse findByBusinessCodeNonArchived(String buCode);
    DbWarehouse findByBusinessCode(String buCode);
    long countByLocation(String location);
    void save(DbWarehouse entity);
    void cleanUp();
}
