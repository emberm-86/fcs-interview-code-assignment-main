package com.fulfilment.application.monolith.warehouses.adapters.database.unittest;

import com.fulfilment.application.monolith.exception.ValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class WarehouseTest {

    private Warehouse createValidWarehouse() {
        return Warehouse.builder()
                .id(1L)
                .businessUnitCode("MWH.001")
                .location("AMSTERDAM-001")
                .capacity(100)
                .stock(50)
                .createdAt(LocalDateTime.of(2024, 1, 1, 0, 0))
                .build();
    }

    // ----------------------------
    // Builder validation
    // ----------------------------

    @Test
    void shouldBuildValidWarehouse() {
        Warehouse w = createValidWarehouse();

        assertNotNull(w);
        assertEquals("MWH.001", w.businessUnitCode);
        assertEquals(100, w.capacity);
        assertEquals(50, w.stock);
    }

    @Test
    void shouldThrowWhenBusinessUnitCodeIsMissing() {
        assertThrows(ValidationException.class, () ->
                Warehouse.builder()
                        .location("AMSTERDAM-001")
                        .capacity(10)
                        .stock(5)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }

    @Test
    void shouldThrowWhenStockExceedsCapacity() {
        assertThrows(ValidationException.class, () ->
                Warehouse.builder()
                        .businessUnitCode("MWH.001")
                        .location("AMSTERDAM-001")
                        .capacity(10)
                        .stock(20)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }

    @Test
    void shouldThrowWhenArchivedBeforeCreated() {
        LocalDateTime created = LocalDateTime.of(2024, 1, 2, 0, 0);
        LocalDateTime archived = LocalDateTime.of(2024, 1, 1, 0, 0);

        assertThrows(ValidationException.class, () ->
                Warehouse.builder()
                        .businessUnitCode("MWH.001")
                        .location("AMSTERDAM-001")
                        .capacity(100)
                        .stock(50)
                        .createdAt(created)
                        .archivedAt(archived)
                        .build()
        );
    }

    // ----------------------------
    // Archive behavior
    // ----------------------------

    @Test
    void shouldArchiveWarehouse() {
        Warehouse w = createValidWarehouse();

        LocalDateTime now = LocalDateTime.of(2024, 2, 1, 0, 0);
        Warehouse archived = w.archive(now);

        assertTrue(archived.isArchived());
        assertEquals(now, archived.archivedAt);
    }

    @Test
    void shouldNotArchiveAlreadyArchivedWarehouse() {
        LocalDateTime now = LocalDateTime.of(2024, 2, 1, 0, 0);

        Warehouse w = createValidWarehouse().archive(now);
        Warehouse archivedAgain = w.archive(now.plusDays(1));

        assertSame(w, archivedAgain); // important: same instance
    }

    // ----------------------------
    // Replacement logic
    // ----------------------------

    @Test
    void shouldReplaceWarehouseFields() {
        Warehouse original = createValidWarehouse();

        Warehouse replacement = Warehouse.builder()
                .businessUnitCode("MWH.001")
                .location("Vienna")
                .capacity(200)
                .stock(50)
                .createdAt(original.createdAt)
                .build();

        original.replaceWith(replacement);

        assertEquals("Vienna", original.location);
        assertEquals(200, original.capacity);
        assertEquals(50, original.stock);
    }

    @Test
    void shouldThrowWhenReplacementCapacityTooSmall() {
        Warehouse original = createValidWarehouse(); // stock = 50

        Warehouse replacement = Warehouse.builder()
                .businessUnitCode("MWH.001")
                .location("Vienna")
                .capacity(49) // < original.stock (50)
                .stock(49)    // valid: stock <= capacity
                .createdAt(original.createdAt)
                .build();

        assertThrows(ValidationException.class,
                () -> original.replaceWith(replacement));
    }

    @Test
    void shouldThrowWhenReplacementStockMismatch() {
        Warehouse original = createValidWarehouse();

        Warehouse replacement = Warehouse.builder()
                .businessUnitCode("MWH.001")
                .location("Vienna")
                .capacity(100)
                .stock(10) // mismatch
                .createdAt(original.createdAt)
                .build();

        assertThrows(ValidationException.class,
                () -> original.replaceWith(replacement));
    }

    // ----------------------------
    // Equality
    // ----------------------------

    @Test
    void shouldBeEqualWhenBusinessUnitCodeMatches() {
        Warehouse w1 = createValidWarehouse();

        Warehouse w2 = Warehouse.builder()
                .businessUnitCode("MWH.001")
                .location("Other")
                .capacity(999)
                .stock(1)
                .createdAt(LocalDateTime.now())
                .build();

        assertEquals(w1, w2);
        assertEquals(w1.hashCode(), w2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenBusinessUnitCodeDiffers() {
        Warehouse w1 = createValidWarehouse();

        Warehouse w2 = Warehouse.builder()
                .businessUnitCode("WH-2")
                .location("AMSTERDAM-001")
                .capacity(100)
                .stock(50)
                .createdAt(LocalDateTime.now())
                .build();

        assertNotEquals(w1, w2);
    }
}