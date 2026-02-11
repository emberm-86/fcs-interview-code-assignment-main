package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.service.FulfillmentService;
import com.fulfilment.application.monolith.warehouses.domain.models.WarehouseProductAssignment;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import org.jboss.resteasy.reactive.ResponseStatus;

@RequestScoped
@Path("/fulfillment")
@Produces("application/json")
public class FulfillmentResourceImpl {

    @Inject
    FulfillmentService fulfillmentService;

    @POST
    @Produces("application/json")
    @ResponseStatus(201)
    @Path("/assign")
    public WarehouseProductAssignment assignProductToWarehouse(
            @QueryParam("storeId") Long storeId,
            @QueryParam("warehouseBuCode") String warehouseBuCode,
            @QueryParam("productId") Long productId) {

        return fulfillmentService.validateAndSaveAssignment(storeId, warehouseBuCode, productId);
    }
}
