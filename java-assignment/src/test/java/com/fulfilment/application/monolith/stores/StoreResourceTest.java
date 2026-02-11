package com.fulfilment.application.monolith.stores;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreResourceTest {

    @Mock
    StoreService storeService;

    @InjectMocks
    StoreResource storeResource;

    @Test
    void shouldReturnAllStores() {
        List<Store> stores = List.of(new Store(), new Store());
        when(storeService.getAll()).thenReturn(stores);

        List<Store> result = storeResource.get();

        assertEquals(stores, result);
        verify(storeService).getAll();
        verifyNoMoreInteractions(storeService);
    }

    @Test
    void shouldReturnSingleStore() {
        Store store = new Store();
        store.id = 1L;

        when(storeService.getById(1L)).thenReturn(store);

        Store result = storeResource.getSingle(1L);

        assertEquals(store, result);
        verify(storeService).getById(1L);
    }

    @Test
    void shouldCreateStore() {
        Store input = new Store();
        Store created = new Store();
        created.id = 1L;

        when(storeService.create(input)).thenReturn(created);

        try (Response response = storeResource.create(input)) {
            assertEquals(201, response.getStatus());
            assertEquals(created, response.getEntity());
        }
        verify(storeService).create(input);
    }

    @Test
    void shouldUpdateStore() {
        Store input = new Store();
        Store updated = new Store();

        when(storeService.update(1L, input)).thenReturn(updated);

        Store result = storeResource.update(1L, input);

        assertSame(updated, result);
        verify(storeService).update(1L, input);
    }

    @Test
    void shouldPatchStore() {
        Store input = new Store();
        Store updated = new Store();

        when(storeService.patch(1L, input)).thenReturn(updated);

        Store result = storeResource.patch(1L, input);

        assertSame(updated, result);
        verify(storeService).patch(1L, input);
    }

    @Test
    void shouldDeleteStore() {
        try (Response response = storeResource.delete(1L)) {
            assertEquals(204, response.getStatus());
        }
        verify(storeService).delete(1L);
    }
}