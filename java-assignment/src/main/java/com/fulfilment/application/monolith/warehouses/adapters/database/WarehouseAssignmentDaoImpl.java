package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.DbWarehouseAssignment;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class WarehouseAssignmentDaoImpl
        implements WarehouseAssignmentDao, PanacheRepository<DbWarehouseAssignment> {

    @Override
    public DbWarehouseAssignment findOneByStoreAndWarehouse(Long storeId, String warehouseCode) {
        return find("storeId = ?1 and warehouseCode = ?2", storeId, warehouseCode)
                .firstResult();
    }

    @Override
    public List<DbWarehouseAssignment> findByStoreId(Long storeId) {
        return list("storeId", storeId);
    }

    @Override
    public List<DbWarehouseAssignment> findByStoreAndProduct(Long storeId, Long productId) {
        return find("""
            select distinct a
            from DbWarehouseAssignment a
            join a.products p
            where a.storeId = ?1 and p.productId = ?2
        """, storeId, productId).list();
    }

    @Override
    public void save(DbWarehouseAssignment entity) {
        persist(entity);
    }
}