package com.fulfilment.application.monolith.warehouses.adapters.restapi.integrationtest;

import com.fulfilment.application.monolith.db.FreshDbProfile;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseProductAssignmentRepository;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.models.WarehouseProductAssignment;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@TestProfile(FreshDbProfile.class)
class FulfillmentEndpointIT {

    private static final Long STORE_ID = 1L;

    @Inject
    WarehouseRepository warehouseRepository;

    @Inject
    WarehouseProductAssignmentRepository warehouseProductAssignmentRepository;

    @Inject
    EntityManager entityManager;

    @BeforeEach
    @Transactional
    void setup() {
        entityManager.createQuery("delete from DbWarehouseAssignmentProduct").executeUpdate();
        entityManager.createQuery("delete from DbWarehouseAssignment").executeUpdate();

        warehouseRepository.create(
                Warehouse.builder()
                        .businessUnitCode("MWH.001")
                        .location("ZWOLLE-001")
                        .capacity(100)
                        .stock(10)
                        .build()
        );

        // --- Warehouse MWH.001 has 5 products (rule #1)
        WarehouseProductAssignment w1 = new WarehouseProductAssignment("MWH.001", STORE_ID);
        w1.addProduct(1L);
        w1.addProduct(2L);
        w1.addProduct(3L);
        w1.addProduct(4L);
        w1.addProduct(5L);
        warehouseProductAssignmentRepository.save(w1);

        // --- MWH.002 (store 1)
        WarehouseProductAssignment w2 = new WarehouseProductAssignment("MWH.002", STORE_ID);
        w2.addProduct(1L); // for product limit
        w2.addProduct(2L); // for store limit
        warehouseProductAssignmentRepository.save(w2);

        // --- MWH.003 (store 1)
        WarehouseProductAssignment w3 = new WarehouseProductAssignment("MWH.003", STORE_ID);
        w3.addProduct(1L); // for product limit
        w3.addProduct(3L); // for store limit
        warehouseProductAssignmentRepository.save(w3);
    }

    // ✅ SUCCESS
    @Test
    @TestTransaction
    void shouldReturn201_whenAssignmentIsValid() {

        given()
                .queryParam("storeId", "2")
                .queryParam("warehouseBuCode", "MWH.002")
                .queryParam("productId", "2")
                .when()
                .post("/fulfillment/assign")
                .then()
                .statusCode(201);
    }

    // ❌ Warehouse limit
    @Test
    @TestTransaction
    void shouldReturn422_whenWarehouseProductCapacityExceeded() {

        given()
                .queryParam("storeId", "1")
                .queryParam("warehouseBuCode", "MWH.001")
                .queryParam("productId", "6")
                .when()
                .post("/fulfillment/assign")
                .then()
                .statusCode(422)
                .body(containsString("maximum of 5 product types"));
    }

    @Test
    @TestTransaction
    void shouldNotDuplicateProduct_whenSameRequestIsSentTwice() {

        long storeId = 2L;

        given()
                .queryParam("storeId", storeId)
                .queryParam("warehouseBuCode", "MWH.002")
                .queryParam("productId", "2")
                .post("/fulfillment/assign")
                .then()
                .statusCode(201);


        given()
                .queryParam("storeId", storeId)
                .queryParam("warehouseBuCode", "MWH.002")
                .queryParam("productId", "2")
                .post("/fulfillment/assign")
                .then()
                .statusCode(201);


        WarehouseProductAssignment result =
                warehouseProductAssignmentRepository.findByStoreAndWarehouse(storeId, "MWH.002");

        assertEquals(1, result.getProductIds().size()); // no duplicates
    }

    // ❌ Product limit
    @Test
    @TestTransaction
    void shouldReturn422_whenProductWarehouseLimitExceeded() {
        given()
                .queryParam("storeId", "1")
                .queryParam("warehouseBuCode", "MWH.004")
                .queryParam("productId", "1")
                .when()
                .post("/fulfillment/assign")
                .then()
                .statusCode(422)
                .body(containsString("A product can be fulfilled by maximum of 2 warehouses per store"));
    }

    // ❌ Store limit
    @Test
    @TestTransaction
    void shouldReturn422_whenStoreWarehouseLimitExceeded() {
        // --- MWH.004 (store 1)
        WarehouseProductAssignment w4 = new WarehouseProductAssignment("MWH.004", STORE_ID);
        w4.addProduct(4L); // for store limit
        warehouseProductAssignmentRepository.save(w4);

        given()
                .queryParam("storeId", "1")
                .queryParam("warehouseBuCode", "MWH.004") // NEW warehouse
                .queryParam("productId", "6")
                .when()
                .post("/fulfillment/assign")
                .then()
                .statusCode(422)
                .body(containsString("maximum of 3 warehouses"));
    }
}