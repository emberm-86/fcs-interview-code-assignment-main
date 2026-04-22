package com.fulfilment.application.monolith.stores;

public record StoreChangedEvent(Store store, StoreOperation operation) {
}
