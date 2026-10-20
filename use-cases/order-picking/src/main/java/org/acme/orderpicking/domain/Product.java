package org.acme.orderpicking.domain;

import java.util.Objects;

/**
 * Represents an indivisible store article that can be included in an order, e.g. "6 milk bricks pack".
 *
 * @param id the unique identifier of the product
 * @param name the human-readable name of the product
 * @param volume the volume of the product in cm3
 * @param location the place in the warehouse the product is stored
 */
public record Product(String id, String name, int volume, WarehouseLocation location) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Product product)) {
            return false;
        }
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return name + "(" + id + ")";
    }
}
