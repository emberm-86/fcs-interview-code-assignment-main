package com.fulfilment.application.monolith.warehouses.domain.models;

import jakarta.persistence.*;

@Entity
@Table(
        name = "warehouse_assignment_product",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"assignment_id", "product_id"}
        )
)
public class DbWarehouseAssignmentProduct {

    @Id
    @GeneratedValue
    public Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "assignment_id")
    public DbWarehouseAssignment assignment;

    @Column(name = "product_id", nullable = false)
    public Long productId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DbWarehouseAssignmentProduct)) return false;

        DbWarehouseAssignmentProduct that = (DbWarehouseAssignmentProduct) o;

        if (productId == null || that.productId == null) return false;

        return productId.equals(that.productId) &&
                assignment != null &&
                that.assignment != null &&
                assignment.equals(that.assignment);
    }

    @Override
    public int hashCode() {
        int result = productId != null ? productId.hashCode() : 0;
        result = 31 * result + (assignment != null ? assignment.hashCode() : 0);
        return result;
    }
}
