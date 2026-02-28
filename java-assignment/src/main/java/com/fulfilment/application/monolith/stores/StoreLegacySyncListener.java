package com.fulfilment.application.monolith.stores;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class StoreLegacySyncListener {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(StoreLegacySyncListener.class);

    @Inject
    LegacyStoreManagerGateway legacyStoreManagerGateway;

    public void onStoreChanged(
            @Observes(during = TransactionPhase.AFTER_SUCCESS)
            StoreChangedEvent event) {

        Store store = event.store();

        LOGGER.info("Transaction committed. Syncing store id={} to legacy system",
                store.id);

        switch (event.operation()) {
            case CREATED -> legacyStoreManagerGateway.createStoreOnLegacySystem(store);
            case UPDATED -> legacyStoreManagerGateway.updateStoreOnLegacySystem(store);
            case DELETED -> legacyStoreManagerGateway.deleteStoreOnLegacySystem(store.id);
        }

        LOGGER.info("Store id={} successfully synced to legacy system", store.id);
    }
}
