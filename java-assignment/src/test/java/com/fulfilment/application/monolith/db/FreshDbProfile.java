package com.fulfilment.application.monolith.db;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;
import java.util.UUID;

public class FreshDbProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
                "quarkus.datasource.jdbc.url",
                "jdbc:h2:mem:test-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1",
                "quarkus.hibernate-orm.database.generation",
                "drop-and-create",
                "quarkus.hibernate-orm.sql-load-script",
                "no-file"
        );
    }
}