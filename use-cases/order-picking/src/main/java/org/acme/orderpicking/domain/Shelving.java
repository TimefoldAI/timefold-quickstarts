package org.acme.orderpicking.domain;

import java.util.Objects;

/**
 * Represents the products container. In the order picking problem the warehouse is represented as a set of shelvings
 * that are organized into columns and rows. Each shelving has two sides where the products can be stored, and
 * {@link Warehouse#SHELVING_ROWS a number of rows}.
 *
 * @param id the column letter followed by the row number, for example {@code A1}
 * @param x absolute x position of the shelving's left bottom corner within the warehouse
 * @param y absolute y position of the shelving's left bottom corner within the warehouse
 * @see Warehouse
 */
public record Shelving(String id, int x, int y) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Shelving shelving)) {
            return false;
        }
        return Objects.equals(id, shelving.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id;
    }
}
