package org.acme.foodpackaging.domain;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

import java.time.Duration;
import java.util.Map;

public class Product {

    @PlanningId
    private String id;
    private String name;
    /** The map key is previous product on assembly line. */
    private Map<Product, Duration> cleaningDurations;

    public Product() {
    }

    public Product(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public Duration getCleanupDuration(Product previousProduct) {
        Duration cleanupDuration = cleaningDurations.get(previousProduct);
        if (cleanupDuration == null) {
            throw new IllegalArgumentException("Cleanup duration previousProduct (" + previousProduct
                    + ") to toProduct (" + this + ") is missing.");
        }
        return cleanupDuration;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Product, Duration> getCleaningDurations() {
        return cleaningDurations;
    }

    public void setCleaningDurations(Map<Product, Duration> cleaningDurations) {
        this.cleaningDurations = cleaningDurations;
    }

}
