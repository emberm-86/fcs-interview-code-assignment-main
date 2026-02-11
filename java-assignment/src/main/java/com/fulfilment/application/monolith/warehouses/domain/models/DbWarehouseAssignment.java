package com.fulfilment.application.monolith.warehouses.domain.models;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "warehouse_assignment",
        uniqueConstraints = @UniqueConstraint(columnNames = {"store_id", "warehouse_code"})
)
public class DbWarehouseAssignment {

    @Id
    @GeneratedValue
    public Long id;

    @Column(name = "store_id", nullable = false)
    public Long storeId;

    @Column(name = "warehouse_code", nullable = false)
    public String warehouseCode;

    @OneToMany(
            mappedBy = "assignment",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    public Set<DbWarehouseAssignmentProduct> products = new HashSet<>();
}