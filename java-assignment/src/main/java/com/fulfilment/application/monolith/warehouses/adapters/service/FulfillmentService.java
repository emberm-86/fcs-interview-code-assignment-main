package com.fulfilment.application.monolith.warehouses.adapters.service;

import com.fulfilment.application.monolith.exception.BusinessException;
import com.fulfilment.application.monolith.exception.ValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.WarehouseProductAssignment;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseProductAssignmentStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class FulfillmentService {

    private static final int MAX_WAREHOUSES_PER_PRODUCT = 2;
    private static final int MAX_WAREHOUSES_PER_STORE = 3;

    @Inject
    WarehouseProductAssignmentStore assignmentStore;

    public WarehouseProductAssignment validateAndSaveAssignment(Long storeId,
                                                                String warehouseBuCode,
                                                                Long productId) throws BusinessException {
        WarehouseProductAssignment assignment =
                assignmentStore.findByStoreAndWarehouse(storeId, warehouseBuCode);

        boolean isNewAssignment = assignment == null;

        if (isNewAssignment) {
            assignment = new WarehouseProductAssignment(warehouseBuCode, storeId);
        }

        List<WarehouseProductAssignment> productAssignments =
                assignmentStore.findByStoreAndProduct(storeId, productId);

        List<WarehouseProductAssignment> storeAssignments =
                assignmentStore.findByStoreId(storeId);

        boolean warehouseAlreadyInProduct =
                productAssignments.stream()
                        .anyMatch(a -> a.getWarehouseBuCode().equals(warehouseBuCode));

        boolean warehouseAlreadyInStore =
                storeAssignments.stream()
                        .anyMatch(a -> a.getWarehouseBuCode().equals(warehouseBuCode));

        // Product-level validation
        if (!warehouseAlreadyInProduct && productAssignments.size() >= MAX_WAREHOUSES_PER_PRODUCT) {
            throw new ValidationException(
                    "A product can be fulfilled by maximum of "
                            + MAX_WAREHOUSES_PER_PRODUCT + " warehouses per store");
        }

        // Store-level validation
        if (!warehouseAlreadyInStore && storeAssignments.size() >= MAX_WAREHOUSES_PER_STORE) {
            throw new ValidationException(
                    "A store can be fulfilled by maximum of "
                            + MAX_WAREHOUSES_PER_STORE + " warehouses");
        }

        assignment.addProduct(productId);
        assignmentStore.save(assignment);

        return assignment;
    }
}
