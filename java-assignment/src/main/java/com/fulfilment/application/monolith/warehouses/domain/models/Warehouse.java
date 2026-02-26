package com.fulfilment.application.monolith.warehouses.domain.models;

import java.time.LocalDateTime;

public class Warehouse {

  // unique identifier
  public String businessUnitCode;

  public String location;

  public int capacity;

  public int stock;

  public LocalDateTime createdAt;

  public LocalDateTime archivedAt;
}
