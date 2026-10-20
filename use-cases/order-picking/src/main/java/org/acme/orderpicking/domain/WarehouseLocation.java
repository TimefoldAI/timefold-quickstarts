package org.acme.orderpicking.domain;

/**
 * Represents a location in the warehouse where a product can be stored. In the context of the order picking problem
 * the warehouse is modeled as set of racks. For picking a particular product the employee walks to the indicated
 * rack side and bay.
 *
 * @param rackId the id of the {@link Rack}, for example {@code A1}
 * @param side the side of the rack
 * @param bay the bay on that side of the rack, from 0 (top) to {@link Warehouse#RACK_BAYS} (bottom)
 * @see Warehouse
 */
public record WarehouseLocation(String rackId, RackSide side, int bay) {

    @Override
    public String toString() {
        return rackId + " " + side + " " + bay;
    }
}
