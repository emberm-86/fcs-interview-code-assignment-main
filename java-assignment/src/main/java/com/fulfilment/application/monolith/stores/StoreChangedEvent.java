package com.fulfilment.application.monolith.stores;

public record StoreChangedEvent(Store store, Operation operation) {
    public enum Operation {
        CREATED,
        UPDATED,
        DELETED
    }
}
