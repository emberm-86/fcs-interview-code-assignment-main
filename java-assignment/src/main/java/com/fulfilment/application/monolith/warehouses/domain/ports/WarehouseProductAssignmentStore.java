package com.fulfilment.application.monolith.warehouses.domain.ports;

import com.fulfilment.application.monolith.warehouses.domain.models.WarehouseProductAssignment;

import java.util.List;

public interface WarehouseProductAssignmentStore {

    void save(WarehouseProductAssignment assignment);

    List<WarehouseProductAssignment> findByStoreId(Long storeId);

    List<WarehouseProductAssignment> findByStoreAndProduct(Long storeId, Long productId);

    WarehouseProductAssignment findByStoreAndWarehouse(Long storeId, String warehouseBuCode);
}