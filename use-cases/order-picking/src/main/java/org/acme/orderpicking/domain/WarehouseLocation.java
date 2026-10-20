package org.acme.orderpicking.domain;

/**
 * Represents a location in the warehouse where a product can be stored. In the context of the order picking problem
 * the warehouse is modeled as set of shelvings. For picking a particular product the employee walks to the indicated
 * shelving side and row.
 *
 * @param shelvingId the id of the {@link Shelving}, for example {@code A1}
 * @param side the side of the shelving
 * @param row the row on that side of the shelving, from 0 (top) to {@link Warehouse#SHELVING_ROWS} (bottom)
 * @see Warehouse
 */
public record WarehouseLocation(String shelvingId, ShelvingSide side, int row) {

    @Override
    public String toString() {
        return shelvingId + " " + side + " " + row;
    }
}
