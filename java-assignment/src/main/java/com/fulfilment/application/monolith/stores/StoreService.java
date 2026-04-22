package com.fulfilment.application.monolith.stores;

import com.fulfilment.application.monolith.exception.ResourceNotFoundException;
import com.fulfilment.application.monolith.exception.ValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class StoreService {

    @Inject
    StoreRepository storeRepository;

    @Inject
    Event<StoreChangedEvent> storeChangedEvent;

    public List<Store> getAll() {
        return storeRepository.findAllSorted();
    }

    public Store getById(Long id) {
        return ensureExists(id);
    }

    @Transactional
    public Store create(Store store) {
        validateCreate(store);

        storeRepository.persist(store);

        storeChangedEvent.fire(
                new StoreChangedEvent(store, StoreOperation.CREATED)
        );

        return store;
    }

    @Transactional
    public Store update(Long id, Store updatedStore) {
        validateUpdate(updatedStore);

        Store store = ensureExists(id);

        store.name = updatedStore.name;
        store.quantityProductsInStock = updatedStore.quantityProductsInStock;

        storeChangedEvent.fire(
                new StoreChangedEvent(store, StoreOperation.UPDATED)
        );

        return store;
    }

    @Transactional
    public Store patch(Long id, Store updatedStore) {
        Store store = ensureExists(id);

        if (updatedStore.name != null) {
            store.name = updatedStore.name;
        }

        if (updatedStore.quantityProductsInStock != null) {
            store.quantityProductsInStock = updatedStore.quantityProductsInStock;
        }

        storeChangedEvent.fire(
                new StoreChangedEvent(store, StoreOperation.UPDATED)
        );

        return store;
    }

    @Transactional
    public void delete(Long id) {
        Store store = ensureExists(id);

        storeRepository.delete(store);

        storeChangedEvent.fire(
                new StoreChangedEvent(store, StoreOperation.DELETED)
        );
    }

    private Store ensureExists(Long id) {
        Store store = storeRepository.findById(id);
        if (store == null) {
            throw new ResourceNotFoundException("Store with id of " + id + " does not exist.");
        }
        return store;
    }

    private void validateCreate(Store store) {
        if (store.id != null) {
            throw new ValidationException("Id was invalidly set on request.");
        }
        if (store.name == null) {
            throw new ValidationException("Store Name was not set on request.");
        }
    }

    private void validateUpdate(Store store) {
        if (store.name == null) {
            throw new ValidationException("Store Name was not set on request.");
        }
    }
}