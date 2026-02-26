package com.fulfilment.application.monolith.stores;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Path("store")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class StoreResource {

  @Inject LegacyStoreManagerGateway legacyStoreManagerGateway;

  private static final Logger LOGGER = LoggerFactory.getLogger(StoreResource.class);

  @GET
  public List<Store> get() {
    return Store.listAll(Sort.by("name"));
  }

  @GET
  @Path("{id}")
  public Store getSingle(Long id) {
    Store store = Store.findById(id);
    if (store == null) {
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }
    return store;
  }

  @POST
  @Transactional
  public Response create(Store store) {
    if (store.id != null) {
      throw new WebApplicationException("Id was invalidly set on request.", 422);
    }
    store.persistAndFlush();

    legacyStoreManagerGateway.createStoreOnLegacySystem(store);

    return Response.ok(store).status(201).build();
  }

  @PUT
  @Path("{id}")
  @Transactional
  public Store update(Long id, Store updatedStore) {
    if (updatedStore.name == null) {
      throw new WebApplicationException("Store Name was not set on request.", 422);
    }

    Store store = Store.findById(id);

    if (store == null) {
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }

    store.name = updatedStore.name;
    store.quantityProductsInStock = updatedStore.quantityProductsInStock;
    store.persistAndFlush();

    legacyStoreManagerGateway.updateStoreOnLegacySystem(updatedStore);

    return store;
  }

  @PATCH
  @Path("{id}")
  @Transactional
  public Store patch(Long id, Store updatedStore) {
    if (updatedStore.name == null) {
      throw new WebApplicationException("Store Name was not set on request.", 422);
    }

    Store store = Store.findById(id);

    if (store == null) {
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }

    if (store.name != null) {
      store.name = updatedStore.name;
    }

    if (store.quantityProductsInStock != 0) {
      store.quantityProductsInStock = updatedStore.quantityProductsInStock;
    }
    store.persistAndFlush();

    legacyStoreManagerGateway.updateStoreOnLegacySystem(updatedStore);

    return store;
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(Long id) {
    Store store = Store.findById(id);
    if (store == null) {
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }
    store.delete();
    return Response.status(204).build();
  }
}
