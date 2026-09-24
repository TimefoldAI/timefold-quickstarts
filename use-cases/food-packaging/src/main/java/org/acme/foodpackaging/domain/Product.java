package org.acme.foodpackaging.domain;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.common.PlanningId;

/**
 * A packaged product. Switching a line from one product to another costs a changeover cleaning,
 * whose duration depends on both products: sharing ingredients means a shorter cleaning.
 */
public class Product {

    @PlanningId
    private final String id;
    private final String name;
    /** The map key is the product produced before this one on the same line. */
    private final Map<Product, Duration> cleaningDurations;

    public Product(String id, String name, Map<Product, Duration> cleaningDurations) {
        this.id = id;
        this.name = name;
        this.cleaningDurations = cleaningDurations;
    }

    /**
     * @return the cleaning needed to switch this line from {@code previousProduct} to this product
     * @throws IllegalArgumentException if no cleaning duration was supplied for that pair; the input
     *         is validated for exactly this before solving starts, so this is a last-resort guard
     */
    public Duration getCleanupDuration(Product previousProduct) {
        Duration cleanupDuration = cleaningDurations.get(previousProduct);
        if (cleanupDuration == null) {
            throw new IllegalArgumentException("Cleanup duration from previousProduct (%s) to product (%s) is missing."
                    .formatted(previousProduct, this));
        }
        return cleanupDuration;
    }

    // ************************************************************************
    // Getters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Map<Product, Duration> getCleaningDurations() {
        return cleaningDurations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Product product)) {
            return false;
        }
        return Objects.equals(id, product.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return name;
    }
}
