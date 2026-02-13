package com.fulfilment.application.monolith.products;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsNot.not;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ProductEndpointTest {

  @Test
  public void testDeleteProduct() {
    final String path = "product";

    // List all, should have all 3 products the database has initially:
    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(containsString("TONSTAD"), containsString("KALLAX"), containsString("BESTÅ"));

    // Delete the TONSTAD:
    given().when().delete(path + "/1").then().statusCode(204);

    // List all, TONSTAD should be missing now:
    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(not(containsString("TONSTAD")), containsString("KALLAX"), containsString("BESTÅ"));
  }

  @Test
  public void testAddProduct() {
    final String path = "product";

    // List all, should have all 3 products the database has initially:
    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(containsString("TONSTAD"), containsString("KALLAX"), containsString("BESTÅ"));

    String newProductCreationRequestMessage = """
        {
            "name": "BARLAST",
            "description": null,
            "price": null,
            "stock": 15
        }
        """;

    // Add the BARLAST:
    given()
        .contentType(ContentType.JSON)
            .body(newProductCreationRequestMessage)
            .when()
        .post(path)
        .then()
        .statusCode(201);

    // List all, TONSTAD should be missing now:
    given()
            .when()
            .get(path)
            .then()
            .statusCode(200)
            .body((containsString("TONSTAD")), containsString("KALLAX"),
                    containsString("BESTÅ"), containsString("BARLAST"));
  }
}
