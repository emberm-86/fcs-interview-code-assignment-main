package com.fulfilment.application.monolith.warehouses.domain.models;

import com.fulfilment.application.monolith.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.Objects;

public class Warehouse {

  public final Long id;
  public final String businessUnitCode;
  public String location;
  public int capacity;
  public int stock;
  public final LocalDateTime createdAt;
  public final LocalDateTime archivedAt;

  private Warehouse(Builder builder) {
    this.id = builder.id;
    this.businessUnitCode = builder.businessUnitCode;
    this.location = builder.location;
    this.capacity = builder.capacity;
    this.stock = builder.stock;
    this.createdAt = builder.createdAt;
    this.archivedAt = builder.archivedAt;
  }

  // ----------------------------
  // Builder
  // ----------------------------

  public static Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    return new Builder()
            .businessUnitCode(this.businessUnitCode)
            .location(this.location)
            .capacity(this.capacity)
            .stock(this.stock)
            .createdAt(this.createdAt)
            .archivedAt(this.archivedAt);
  }

  public void validateReplacementWith(Warehouse newWarehouse) {
    if (newWarehouse.capacity < this.stock) {
      throw new ValidationException(
              "New warehouse capacity cannot accommodate existing stock"
      );
    }

    if (newWarehouse.stock != this.stock) {
      throw new ValidationException(
              "New warehouse stock must match existing warehouse stock"
      );
    }
  }

  public void replaceWith(Warehouse other) {
    validateReplacementWith(other);

    this.capacity = other.capacity;
    this.stock = other.stock;
    this.location = other.location;
  }

  public static class Builder {
    private Long id;
    private String businessUnitCode;
    private String location;
    private int capacity;
    private int stock;
    private LocalDateTime createdAt;
    private LocalDateTime archivedAt;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder businessUnitCode(String businessUnitCode) {
      this.businessUnitCode = businessUnitCode;
      return this;
    }

    public Builder location(String location) {
      this.location = location;
      return this;
    }

    public Builder capacity(int capacity) {
      this.capacity = capacity;
      return this;
    }

    public Builder stock(int stock) {
      this.stock = stock;
      return this;
    }

    public Builder createdAt(LocalDateTime createdAt) {
      this.createdAt = createdAt;
      return this;
    }

    public Builder archivedAt(LocalDateTime archivedAt) {
      this.archivedAt = archivedAt;
      return this;
    }

    public Warehouse build() {
      validate();
      return new Warehouse(this);
    }

    private void validate() {

      if (businessUnitCode == null || businessUnitCode.isBlank()) {
        throw new ValidationException("BusinessUnitCode cannot be empty");
      }

      if (location == null || location.isBlank()) {
        throw new ValidationException("Location cannot be empty");
      }

      if (capacity < 0) {
        throw new ValidationException("Capacity cannot be negative");
      }

      if (stock < 0) {
        throw new ValidationException("Stock cannot be negative");
      }

      if (stock > capacity) {
        throw new ValidationException("Stock cannot exceed capacity");
      }

      if (archivedAt != null && archivedAt.isBefore(createdAt)) {
        throw new ValidationException("archivedAt cannot be before createdAt");
      }
    }
  }

  // ----------------------------
  // Domain Behavior
  // ----------------------------

  public boolean isArchived() {
    return archivedAt != null;
  }

  public Warehouse archive(LocalDateTime now) {
    if (isArchived()) {
      return this;
    }
    return this.toBuilder()
            .archivedAt(now)
            .build();
  }

  // ----------------------------
  // equals & hashCode
  // ----------------------------

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Warehouse that)) return false;
    return Objects.equals(businessUnitCode, that.businessUnitCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(businessUnitCode);
  }
}
