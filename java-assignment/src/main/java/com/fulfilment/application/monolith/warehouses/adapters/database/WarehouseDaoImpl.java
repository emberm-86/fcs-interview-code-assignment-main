package com.fulfilment.application.monolith.warehouses.adapters.database;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class WarehouseDaoImpl implements WarehouseDao, PanacheRepository<DbWarehouse> {

    @Override
    public List<DbWarehouse> findAllNonArchived() {
        return list("archivedAt is null");
    }

    @Override
    public DbWarehouse findByIdNonArchived(Long id) {
        return find("id = ?1 and archivedAt is null", id).firstResult();
    }

    @Override
    public DbWarehouse findByIdW(Long id) {
        return findById(id);
    }

    @Override
    public DbWarehouse findByBusinessCodeNonArchived(String buCode) {
        return find("businessUnitCode = ?1 and archivedAt is null", buCode)
                .firstResult();
    }

    @Override
    public DbWarehouse findByBusinessCode(String buCode) {
        return find("businessUnitCode = ?1", buCode)
                .firstResult();
    }

    @Override
    public long countByLocation(String location) {
        return count("location = ?1", location);
    }

    @Override
    public void save(DbWarehouse entity) {
        persist(entity);
    }

    @Override
    public void cleanUp() {
        deleteAll();
    }
}
