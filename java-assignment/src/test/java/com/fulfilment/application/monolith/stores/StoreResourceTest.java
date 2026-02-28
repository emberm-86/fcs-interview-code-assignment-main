package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class StoreResourceTest {

    private Long tonstadId;
    private Long kallaxId;
    private Long bestaId;

    // ============================
    // Initialize test data
    // ============================
    @BeforeEach
    @Transactional
    void initTestData() {
        // clean DB
        Store.deleteAll();

        Store s1 = new Store();
        s1.name = "TONSTAD";
        s1.quantityProductsInStock = 10;
        s1.persist();
        tonstadId = s1.id;

        Store s2 = new Store();
        s2.name = "KALLAX";
        s2.quantityProductsInStock = 5;
        s2.persist();
        kallaxId = s2.id;

        Store s3 = new Store();
        s3.name = "BESTÅ";
        s3.quantityProductsInStock = 3;
        s3.persist();
        bestaId = s3.id;
    }

    // ============================
    // GET TESTS
    // ============================
    @Test
    void testGetAllStores() {
        given()
                .when()
                .get("/store")
                .then()
                .statusCode(200)
                .body("size()", is(3))
                .body("name", hasItems("TONSTAD", "KALLAX", "BESTÅ"));
    }

    @Test
    void testGetSingleStore() {
        given()
                .pathParam("id", tonstadId)
                .when()
                .get("/store/{id}")
                .then()
                .statusCode(200)
                .body("name", is("TONSTAD"))
                .body("quantityProductsInStock", is(10));
    }

    @Test
    void testGetNonExistingStore() {
        given()
                .pathParam("id", 999999L)
                .when()
                .get("/store/{id}")
                .then()
                .statusCode(404);
    }

    // ============================
    // CREATE TEST
    // ============================
    @Test
    void testCreateStore() {
        String payload = """
            {
                "name": "NEWSTORE",
                "quantityProductsInStock": 7
            }
        """;

        Integer createdId =
                given()
                        .contentType(ContentType.JSON)
                        .body(payload)
                        .when()
                        .post("/store")
                        .then()
                        .statusCode(201)
                        .body("name", is("NEWSTORE"))
                        .body("quantityProductsInStock", is(7))
                        .body("id", notNullValue())
                        .extract()
                        .path("id");

        // verify persisted data
        Store store = Store.findById(createdId);
        assert store != null;
        assert store.name.equals("NEWSTORE");
        assert store.quantityProductsInStock == 7;
    }

    // ============================
    // UPDATE TEST
    // ============================
    @Test
    void testUpdateStore() {
        String payload = """
            {
                "name": "TONSTAD_UPDATED",
                "quantityProductsInStock": 20
            }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .pathParam("id", tonstadId)
                .when()
                .put("/store/{id}")
                .then()
                .statusCode(200)
                .body("name", is("TONSTAD_UPDATED"))
                .body("quantityProductsInStock", is(20));
    }

    // ============================
    // PATCH TESTS
    // ============================
    @Test
    void testPatchStoreWithMissingFields() {
        String payload = """
            {
                "name": "KALLAX_PATCHED"
            }
        """;

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", kallaxId)
                .body(payload)
                .when()
                .patch("/store/{id}")
                .then()
                .statusCode(200)
                .body("name", is("KALLAX_PATCHED"))
                .body("quantityProductsInStock", is(5));
    }

    @Test
    void testPatchStoreWithZeroStock() {
        String payload = """
            {
                "quantityProductsInStock": 0
            }
        """;

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", kallaxId)
                .body(payload)
                .when()
                .patch("/store/{id}")
                .then()
                .statusCode(200)
                .body("name", is("KALLAX"))
                .body("quantityProductsInStock", is(0));
    }

    // ============================
    // DELETE TEST
    // ============================
    @Test
    void testDeleteStore() {
        given()
                .pathParam("id", bestaId)
                .when()
                .delete("/store/{id}")
                .then()
                .statusCode(204);

        assert Store.findById(bestaId) == null;
    }
}