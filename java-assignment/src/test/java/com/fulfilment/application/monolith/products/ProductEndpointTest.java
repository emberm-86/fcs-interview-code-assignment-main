package com.fulfilment.application.monolith.products;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsNot.not;

import com.fulfilment.application.monolith.db.FreshDbProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(FreshDbProfile.class)
public class ProductEndpointTest {

  private static final String ENDPOINT = "product";

  @Inject
  ProductRepository productRepository;

  private Long tonstadId;
  private Long kallaxId;
  private Long bestaId;

  // ============================
  // Initialize test data
  // ============================
  @BeforeEach
  @Transactional
  void setUp() {
    productRepository.deleteAll();

    tonstadId = persist("TONSTAD", 20);
    kallaxId = persist("KALLAX", 10);
    bestaId = persist("BESTÅ", 15);
  }

  // ============================
  // Tests
  // ============================

  @Test
  public void testDeleteProduct() {
    given()
            .when().get(ENDPOINT)
            .then()
            .statusCode(200)
            .body(containsString("TONSTAD"))
            .body(containsString("KALLAX"))
            .body(containsString("BESTÅ"));

    // Delete TONSTAD using captured ID
    given().when().delete(ENDPOINT + "/" + tonstadId)
            .then().statusCode(204);

    // Verify TONSTAD is removed
    given()
            .when().get(ENDPOINT)
            .then()
            .statusCode(200)
            .body(not(containsString("TONSTAD")))
            .body(containsString("KALLAX"))
            .body(containsString("BESTÅ"));
  }

  @Test
  public void testAddProduct() {
    String newProductJson = """
                {
                    "name": "BARLAST",
                    "description": "Storage system",
                    "price": null,
                    "stock": 15
                }
                """;

    Long barlastId = given()
            .contentType(ContentType.JSON)
            .body(newProductJson)
            .when()
            .post(ENDPOINT)
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id"); // safely extract as Long

    // Verify all products exist including new one
    given()
            .when().get(ENDPOINT)
            .then()
            .statusCode(200)
            .body(containsString("TONSTAD"))
            .body(containsString("KALLAX"))
            .body(containsString("BESTÅ"))
            .body(containsString("BARLAST"));
  }

  @Test
  public void testUpdateProduct() {
    String updateJson = """
                {
                    "id": %d,
                    "name": "BARLAST",
                    "description": "Storage system",
                    "price": null,
                    "stock": 15
                }
                """.formatted(tonstadId);

    given()
            .contentType(ContentType.JSON)
            .body(updateJson)
            .when()
            .put(ENDPOINT + "/" + tonstadId)
            .then()
            .statusCode(200);

    // Verify update reflected
    given()
            .when().get(ENDPOINT)
            .then()
            .statusCode(200)
            .body(containsString("BARLAST"))
            .body(containsString("Storage system"));
  }

  // ============================
  // Helper: Create product and return ID
  // ============================
  private Long persist(String name, int stock) {
    Product p = new Product();
    p.name = name;
    p.description = null;
    p.price = null;
    p.stock = stock;
    productRepository.persist(p);
    return p.id;
  }
}