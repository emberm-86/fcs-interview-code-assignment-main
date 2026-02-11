package com.fulfilment.application.monolith.stores;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class StoreRepository implements PanacheRepository<Store> {

    public List<Store> findAllSorted() {
        return listAll(io.quarkus.panache.common.Sort.by("name"));
    }
}
