package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "warehouse")
@Cacheable
public class DbWarehouse {

  @Id @GeneratedValue public Long id;

  public String businessUnitCode;

  public String location;

  public Integer capacity;

  @Override
  public String toString() {
    return "DbWarehouse{" +
            "id=" + id +
            ", businessUnitCode='" + businessUnitCode + '\'' +
            ", location='" + location + '\'' +
            ", capacity=" + capacity +
            ", stock=" + stock +
            ", createdAt=" + createdAt +
            ", archivedAt=" + archivedAt +
            '}';
  }

  public Integer stock;

  public LocalDateTime createdAt;

  public LocalDateTime archivedAt;

  public DbWarehouse() {}

  public Warehouse toWarehouse() {
    return Warehouse.builder()
            .id(id)
            .businessUnitCode(businessUnitCode)
            .location(location)
            .capacity(capacity)
            .stock(stock == null ? 0 : stock)
            .createdAt(createdAt)
            .archivedAt(archivedAt)
            .build();
  }

  @PrePersist
  public void prePersist() {
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
  }
}
