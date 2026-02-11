package com.fulfilment.application.monolith.stores;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreLegacySyncListenerTest {

    @Mock
    LegacyStoreManagerGateway legacyStoreManagerGateway;

    @InjectMocks
    StoreLegacySyncListener listener;

    private Store createStore() {
        Store store = new Store();
        store.id = 100L;
        return store;
    }

    @Test
    void shouldCallCreateWhenEventIsCreated() {
        Store store = createStore();
        StoreChangedEvent event = new StoreChangedEvent(store, StoreChangedEvent.Operation.CREATED);

        listener.onStoreChanged(event);

        verify(legacyStoreManagerGateway).createStoreOnLegacySystem(store);
        verifyNoMoreInteractions(legacyStoreManagerGateway);
    }

    @Test
    void shouldCallUpdateWhenEventIsUpdated() {
        Store store = createStore();
        StoreChangedEvent event = new StoreChangedEvent(store, StoreChangedEvent.Operation.UPDATED);

        listener.onStoreChanged(event);

        verify(legacyStoreManagerGateway).updateStoreOnLegacySystem(store);
        verifyNoMoreInteractions(legacyStoreManagerGateway);
    }

    @Test
    void shouldCallDeleteWhenEventIsDeleted() {
        Store store = createStore();
        StoreChangedEvent event = new StoreChangedEvent(store, StoreChangedEvent.Operation.DELETED);

        listener.onStoreChanged(event);

        verify(legacyStoreManagerGateway).deleteStoreOnLegacySystem(store.id);
        verifyNoMoreInteractions(legacyStoreManagerGateway);
    }
}