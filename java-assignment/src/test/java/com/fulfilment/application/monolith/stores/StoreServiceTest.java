package com.fulfilment.application.monolith.stores;

import com.fulfilment.application.monolith.exception.ResourceNotFoundException;
import com.fulfilment.application.monolith.exception.ValidationException;
import jakarta.enterprise.event.Event;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    StoreRepository storeRepository;

    @Mock
    Event<StoreChangedEvent> storeChangedEvent;

    @InjectMocks
    StoreService storeService;

    @Test
    void shouldReturnAllStores() {
        when(storeRepository.findAllSorted())
                .thenReturn(List.of(new Store(), new Store()));

        List<Store> result = storeService.getAll();

        assertEquals(2, result.size());
        verify(storeRepository).findAllSorted();
    }

    @Test
    void shouldReturnStoreById() {
        Store store = new Store();
        store.id = 1L;

        when(storeRepository.findById(1L)).thenReturn(store);

        Store result = storeService.getById(1L);

        assertEquals(1L, result.id);
    }

    @Test
    void shouldThrowWhenStoreNotFound() {
        when(storeRepository.findById(1L)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> storeService.getById(1L));
    }

    @Test
    void shouldCreateStoreAndFireEvent() {
        Store store = new Store();
        store.name = "Test";

        Store result = storeService.create(store);

        assertEquals("Test", result.name);

        verify(storeRepository).persist(store);
        verify(storeChangedEvent).fire(any(StoreChangedEvent.class));
    }

    @Test
    void shouldFailCreateWhenIdSet() {
        Store store = new Store();
        store.id = 1L;

        assertThrows(ValidationException.class,
                () -> storeService.create(store));
    }

    @Test
    void shouldUpdateStoreAndFireEvent() {
        Store existing = new Store();
        existing.id = 1L;

        when(storeRepository.findById(1L)).thenReturn(existing);

        Store update = new Store();
        update.name = "Updated";
        update.quantityProductsInStock = 10;

        Store result = storeService.update(1L, update);

        assertEquals("Updated", result.name);
        assertEquals(10, result.quantityProductsInStock);

        verify(storeChangedEvent).fire(any(StoreChangedEvent.class));
    }

    @Test
    void shouldDeleteStoreAndFireEvent() {
        Store store = new Store();
        store.id = 1L;

        when(storeRepository.findById(1L)).thenReturn(store);

        storeService.delete(1L);

        verify(storeRepository).delete(store);
        verify(storeChangedEvent).fire(any(StoreChangedEvent.class));
    }
}