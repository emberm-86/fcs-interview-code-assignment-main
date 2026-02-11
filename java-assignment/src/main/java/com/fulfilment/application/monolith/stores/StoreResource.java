package com.fulfilment.application.monolith.stores;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("store")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class StoreResource {

    @Inject
    StoreService storeService;

    @GET
    public List<Store> get() {
        return storeService.getAll();
    }

    @GET
    @Path("{id}")
    public Store getSingle(@PathParam("id") Long id) {
        return storeService.getById(id);
    }

    @POST
    public Response create(Store store) {
        Store created = storeService.create(store);

        return Response.status(Response.Status.CREATED)
                .entity(created)
                .build();
    }

    @PUT
    @Path("{id}")
    public Store update(@PathParam("id") Long id, Store store) {
        return storeService.update(id, store);
    }

    @PATCH
    @Path("{id}")
    public Store patch(@PathParam("id") Long id, Store store) {
        return storeService.patch(id, store);
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") Long id) {
        storeService.delete(id);
        return Response.noContent().build();
    }
}
