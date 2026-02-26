package com.fulfilment.application.monolith.products;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Cacheable
public class Product {

  // @Id @GeneratedValue public Long id;

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq")
  @SequenceGenerator(
          name = "product_seq",
          sequenceName = "product_seq",
          allocationSize = 1
  ) public Long id;

  @Column(length = 40, unique = true)
  public String name;

  @Column(nullable = true)
  public String description;

  @Column(precision = 10, scale = 2, nullable = true)
  public BigDecimal price;

  public int stock;

  public Product() {}

  public Product(String name) {
    this.name = name;
  }
}
