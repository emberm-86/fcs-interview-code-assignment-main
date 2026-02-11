package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.DbWarehouseAssignment;
import com.fulfilment.application.monolith.warehouses.domain.models.DbWarehouseAssignmentProduct;
import com.fulfilment.application.monolith.warehouses.domain.models.WarehouseProductAssignment;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseProductAssignmentStore;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class WarehouseProductAssignmentRepository implements WarehouseProductAssignmentStore {

    @Inject
    WarehouseAssignmentDao dao;

    @Transactional
    public void save(WarehouseProductAssignment assignment) {

        DbWarehouseAssignment db =
                dao.findOneByStoreAndWarehouse(
                        assignment.getStoreId(),
                        assignment.getWarehouseBuCode()
                );

        if (db == null) {
            db = new DbWarehouseAssignment();
            db.storeId = assignment.getStoreId();
            db.warehouseCode = assignment.getWarehouseBuCode();
            db.products = new HashSet<>();
            dao.save(db);
        }

        Map<Long, DbWarehouseAssignmentProduct> existing =
                db.products.stream()
                        .collect(Collectors.toMap(p -> p.productId, p -> p));

        Set<Long> desired = new HashSet<>(assignment.getProductIds());

        db.products.removeIf(p -> !desired.contains(p.productId));

        for (Long productId : desired) {
            if (!existing.containsKey(productId)) {
                DbWarehouseAssignmentProduct p = new DbWarehouseAssignmentProduct();
                p.assignment = db;
                p.productId = productId;
                db.products.add(p);
            }
        }
    }

    @Override
    public List<WarehouseProductAssignment> findByStoreId(Long storeId) {
        return dao.findByStoreId(storeId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<WarehouseProductAssignment> findByStoreAndProduct(Long storeId, Long productId) {
        return dao.findByStoreAndProduct(storeId, productId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public WarehouseProductAssignment findByStoreAndWarehouse(Long storeId, String code) {
        DbWarehouseAssignment db = dao.findOneByStoreAndWarehouse(storeId, code);
        return db != null ? toDomain(db) : null;
    }

    public WarehouseProductAssignment toDomain(DbWarehouseAssignment db) {
        WarehouseProductAssignment domain =
                new WarehouseProductAssignment(db.warehouseCode, db.storeId);

        db.products.forEach(p -> domain.addProduct(p.productId));

        return domain;
    }
}