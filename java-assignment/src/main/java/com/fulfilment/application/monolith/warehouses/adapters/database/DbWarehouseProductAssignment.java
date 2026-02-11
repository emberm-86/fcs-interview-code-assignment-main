package com.fulfilment.application.monolith.warehouses.adapters.database;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "warehouse_product_assignment",
        uniqueConstraints = @UniqueConstraint(columnNames = {"store_id", "warehouse_bu_code"}))
public class DbWarehouseProductAssignment extends PanacheEntity {

    @Column(name = "warehouse_bu_code", nullable = false)
    public String warehouseBuCode;

    @Column(name = "store_id", nullable = false)
    public Long storeId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "warehouse_products",
            joinColumns = @JoinColumn(name = "assignment_id")
    )
    @Column(name = "product_id")
    public Set<Long> productIds = new HashSet<>();
}