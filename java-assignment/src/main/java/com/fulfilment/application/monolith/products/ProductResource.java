package com.fulfilment.application.monolith.products;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("product")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class ProductResource {

  @Inject
  ProductService productService;

  @GET
  public List<Product> get() {
    return productService.getAll();
  }

  @GET
  @Path("{id}")
  public Product getSingle(@PathParam("id") Long id) {
    return productService.getById(id);
  }

  @POST
  public Response create(Product product) {
    Product created = productService.create(product);
    return Response.status(Response.Status.CREATED).entity(created).build();
  }

  @PUT
  @Path("{id}")
  public Product update(@PathParam("id") Long id, Product product) {
    return productService.update(id, product);
  }

  @DELETE
  @Path("{id}")
  public Response delete(@PathParam("id") Long id) {
    productService.delete(id);
    return Response.noContent().build();
  }
}