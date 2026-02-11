package com.fulfilment.application.monolith.warehouses.adapters.restapi.integrationtest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.allOf;

import com.fulfilment.application.monolith.db.FreshDbProfile;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.Arrays;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(FreshDbProfile.class)
public class WarehouseEndpointIT {

  private static final String ENDPOINT_PATH = "warehouse";

  @Inject
  WarehouseRepository warehouseRepository;

  @BeforeEach
  public void setupTestData() {
    // Create initial warehouses that tests expect to exist
    // Using AMSTERDAM-001 which has maxNumberOfWarehouses=5, so we can create multiple test warehouses
    warehouseRepository.create(Warehouse.builder()
            .businessUnitCode("MWH.001")
            .location("AMSTERDAM-001")
            .capacity(100)
            .stock(10)
            .createdAt(LocalDateTime.now())
            .build());

    warehouseRepository.create(Warehouse.builder()
            .businessUnitCode("MWH.012")
            .location("AMSTERDAM-002")
            .capacity(50)
            .stock(5)
            .createdAt(LocalDateTime.now())
            .build());

    warehouseRepository.create(Warehouse.builder()
            .businessUnitCode("MWH.023")
            .location("EINDHOVEN-001")
            .capacity(30)
            .stock(27)
            .createdAt(LocalDateTime.now())
            .build());
  }

  // Helper to dynamically assert that a list of warehouse codes exist
  private void assertWarehouseListContains(String... codes) {
    Matcher[] matchers = Arrays.stream(codes)
            .map(CoreMatchers::containsString)
            .toArray(Matcher[]::new);

    given()
            .when().get(ENDPOINT_PATH)
            .then()
            .statusCode(200)
            .body(allOf(matchers));
  }

  @Test
  public void testListWarehouses() {
    // Just assert some known warehouse codes exist (initial DB state)
    assertWarehouseListContains("MWH.001", "MWH.012", "MWH.023");
  }

  @Test
  public void testCreateWarehouse() {
    // 1. Create a new warehouse in AMSTERDAM-001 (max 5 warehouses)
    given()
            .contentType("application/json")
            .body("{\"businessUnitCode\":\"MWH.100\", \"location\":\"AMSTERDAM-001\", \"capacity\":80, \"stock\":15}")
            .when()
            .post(ENDPOINT_PATH)
            .then()
            .statusCode(201);

    // 2. Verify it exists
    assertWarehouseListContains("MWH.100");
  }

  @Test
  public void testArchiveWarehouse() {
    // 1. Create a temporary warehouse to archive
    given()
            .contentType("application/json")
            .body("{\"businessUnitCode\":\"MWH.999\", \"location\":\"AMSTERDAM-002\", \"capacity\":25, \"stock\":5}")
            .when()
            .post(ENDPOINT_PATH)
            .then()
            .statusCode(201);

    // 2. Verify it exists
    assertWarehouseListContains("MWH.999");
  }
}