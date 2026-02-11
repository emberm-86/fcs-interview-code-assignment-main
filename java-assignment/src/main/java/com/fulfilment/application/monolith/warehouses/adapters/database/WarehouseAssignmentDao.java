package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.DbWarehouseAssignment;

import java.util.List;

public interface WarehouseAssignmentDao {

    DbWarehouseAssignment findOneByStoreAndWarehouse(Long storeId, String warehouseCode);

    List<DbWarehouseAssignment> findByStoreId(Long storeId);

    List<DbWarehouseAssignment> findByStoreAndProduct(Long storeId, Long productId);

    void save(DbWarehouseAssignment entity);
}