package com.fulfilment.application.monolith.stores;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Path("store")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class StoreResource {

  @Inject
  Event<StoreChangedEvent> storeChangedEvent;

  private static final Logger LOGGER =
          LoggerFactory.getLogger(StoreResource.class);

  @GET
  public List<Store> get() {
    return Store.listAll(Sort.by("name"));
  }

  @GET
  @Path("{id}")
  public Store getSingle(@PathParam("id") Long id) {
    Store store = Store.findById(id);
    if (store == null) {
      throw new WebApplicationException(
              "Store with id of " + id + " does not exist.", 404);
    }
    return store;
  }

  @POST
  @Transactional
  public Response create(Store store) {

    if (store.id != null) {
      throw new WebApplicationException(
              "Id was invalidly set on request.", 422);
    }

    store.persist();

    // 🔥 Fire event inside transaction
    storeChangedEvent.fire(
            new StoreChangedEvent(store, StoreChangedEvent.Operation.CREATED)
    );

    return Response.status(Response.Status.CREATED)
            .entity(store)
            .build();
  }

  @PUT
  @Path("{id}")
  @Transactional
  public Store update(@PathParam("id") Long id, Store updatedStore) {

    if (updatedStore.name == null) {
      throw new WebApplicationException(
              "Store Name was not set on request.", 422);
    }

    Store store = Store.findById(id);

    if (store == null) {
      throw new WebApplicationException(
              "Store with id of " + id + " does not exist.", 404);
    }

    store.name = updatedStore.name;
    store.quantityProductsInStock =
            updatedStore.quantityProductsInStock;

    // 🔥 Fire event inside transaction
    storeChangedEvent.fire(
            new StoreChangedEvent(store, StoreChangedEvent.Operation.UPDATED)
    );

    return store;
  }

  @PATCH
  @Path("{id}")
  @Transactional
  public Store patch(@PathParam("id") Long id, Store updatedStore) {

    Store store = Store.findById(id);

    if (store == null) {
      throw new WebApplicationException(
              "Store with id of " + id + " does not exist.", 404);
    }

    if (updatedStore.name != null) {
      store.name = updatedStore.name;
    }

    if (updatedStore.quantityProductsInStock != null) {
      store.quantityProductsInStock =
              updatedStore.quantityProductsInStock;
    }

    storeChangedEvent.fire(
            new StoreChangedEvent(store, StoreChangedEvent.Operation.UPDATED)
    );

    return store;
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(@PathParam("id") Long id) {

    Store store = Store.findById(id);

    if (store == null) {
      throw new WebApplicationException(
              "Store with id of " + id + " does not exist.", 404);
    }

    store.delete();

    storeChangedEvent.fire(
            new StoreChangedEvent(store, StoreChangedEvent.Operation.DELETED)
    );

    return Response.noContent().build();
  }
}