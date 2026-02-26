package com.fulfilment.application.monolith.products;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsNot.not;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ProductEndpointTest {

  private static final String ENDPOINT = "product";

  @Inject
  ProductRepository productRepository;

  @BeforeEach
  public void initTestData() {
    createProduct("TONSTAD", 20);
    createProduct("KALLAX", 10);
    createProduct("BESTÅ", 15);
  }

  @AfterEach
  @Transactional
  public void cleanUpTestData() {
    productRepository.deleteAll();
    productRepository.getEntityManager()
            .createNativeQuery("ALTER SEQUENCE product_seq RESTART WITH 1")
            .executeUpdate();
    productRepository.flush();
  }

  @Test
  public void testDeleteProduct() {
    // Verify initial products exist
    given()
            .when().get(ENDPOINT)
            .then()
            .statusCode(200)
            .body(containsString("TONSTAD"), containsString("KALLAX"), containsString("BESTÅ"));

    // Delete TONSTAD
    given().when().delete(ENDPOINT + "/1").then().statusCode(204);

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
                    "description": null,
                    "price": null,
                    "stock": 15
                }
                """;

    // Add new product
    given()
            .contentType(ContentType.JSON)
            .body(newProductJson)
            .when()
            .post(ENDPOINT)
            .then()
            .statusCode(201);

    // Verify new product exists along with initial products
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
                    "id": 1,
                    "name": "BARLAST",
                    "description": "Storage system",
                    "price": null,
                    "stock": 15
                }
                """;

    // Update product
    given()
            .contentType(ContentType.JSON)
            .body(updateJson)
            .when()
            .put(ENDPOINT + "/1")
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

  /** Helper to create a product and assert it was created */
  private void createProduct(String name, int stock) {
    String productJson = """
                {
                    "name": "%s",
                    "description": null,
                    "price": null,
                    "stock": %d
                }
                """.formatted(name, stock);

    given()
            .contentType(ContentType.JSON)
            .body(productJson)
            .when()
            .post(ENDPOINT)
            .then()
            .statusCode(201)
            .extract().path("id");
  }
}