package org.acme.orderpicking.domain;

import static java.lang.Math.abs;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Models the warehouse where the order picking problem is formulated and is a static structure composed of racks
 * organized in columns and rows. A rack can be identified by the warehouse column and row number where it's placed.
 * The store products are located on the racks, each rack can contain products on the LEFT and RIGHT sides, and
 * in a bay on the particular rack side.
 * <p>
 * Note: This warehouse structure is completely static and aligned with the map drawn by the order picking web UI
 * (visualize.js), changes on this structure require UI adjustments.
 *
 * <pre>
 *  -----------------------------------------------------------------------------------&gt; x
 *  |    |--------|   |--------|   |--------|   |--------|   |--------|
 *  |    |        |   |        |   |        |   |        |   |        |
 *  |    |   A1   |   |   B1   |   |   C1   |   |   D1   |   |   E1   |
 *  |    |        |   |        |   |        |   |        |   |        |
 *  |    |--------|   |--------|   |--------|   |--------|   |--------|
 *  |
 *  |    |--------|   |--------|   |--------|   |--------|   |--------|
 *  |    |        |   |        |   |        |   |        |   |        |
 *  |    |   A2   |   |   B2   |   |   C2   |   |   D2   |   |   E2   |
 *  |    |        |   |        |   |        |   |        |   |        |
 *  |    |--------|   |--------|   |--------|   |--------|   |--------|
 *  |
 *  |    |--------|   |--------|   |--------|   |--------|   |--------|
 *  |    |        |   |        |   |        |   |        |   |        |
 *  |    |   A3   |   |   B3   |   |   C3   |   |   D3   |   |   E3   |
 *  |    |        |   |        |   |        |   |        |   |        |
 *  |    |--------|   |--------|   |--------|   |--------|   |--------|
 *  |
 *  y
 * </pre>
 *
 * @see Rack
 */
public final class Warehouse {

    /**
     * The warehouse columns, from left to right.
     */
    public static final List<String> COLUMNS = List.of("A", "B", "C", "D", "E");
    /**
     * The number of warehouse rows, numbered from 1 at the top.
     */
    public static final int ROW_COUNT = 3;
    /**
     * The number of bays on each side of a rack, numbered from 0 at the top. A bay is one meter long.
     */
    public static final int RACK_BAYS = 10;

    /**
     * The rack width in meters.
     */
    private static final int RACK_WIDTH = 2;
    /**
     * The rack length in meters, along the y axis.
     */
    private static final int RACK_LENGTH = RACK_BAYS;
    /**
     * Spacing between the warehouse rows and columns in meters.
     */
    private static final int RACK_PADDING = 3;

    private static final Map<String, Rack> RACK_MAP = new LinkedHashMap<>();

    static {
        var rackX = 0;
        for (String column : COLUMNS) {
            var rackY = 0;
            for (int row = 1; row <= ROW_COUNT; row++) {
                var rack = new Rack(rackId(column, row), rackX, rackY);
                RACK_MAP.put(rack.id(), rack);
                rackY = rackY + RACK_LENGTH + RACK_PADDING;
            }
            rackX = rackX + RACK_WIDTH + RACK_PADDING;
        }
    }

    private Warehouse() {
    }

    public static String rackId(String column, int row) {
        return column + row;
    }

    public static boolean hasRack(String rackId) {
        return RACK_MAP.containsKey(rackId);
    }

    /**
     * Calculates the distance in meters between two locations considering the warehouse structure.
     */
    public static int calculateDistance(WarehouseLocation start, WarehouseLocation end) {
        final var startRack = getRack(start.rackId());
        final var endRack = getRack(end.rackId());
        var deltaX = 0;
        var deltaY = 0;

        final var startX = getAbsoluteX(startRack, start);
        final var startY = getAbsoluteY(startRack, start);
        final var endX = getAbsoluteX(endRack, end);
        final var endY = getAbsoluteY(endRack, end);

        if (startRack.equals(endRack)) {
            //same rack
            if (start.side() == end.side()) {
                //same side
                deltaY = abs(startY - endY);
            } else {
                //different side, calculate shortest walk.
                deltaX = RACK_WIDTH;
                deltaY = calculateBestYDistanceInWarehouseRow(start.bay(), end.bay());
            }
        } else if (startRack.y() == endRack.y()) {
            //distinct racks but on the same warehouse row
            if (abs(startX - endX) == RACK_PADDING) {
                //neighbor racks, but also contiguous side
                deltaX = RACK_PADDING;
                deltaY = abs(startY - endY);
            } else {
                //any other combination of racks but in the same warehouse row
                deltaX = abs(startX - endX);
                deltaY = calculateBestYDistanceInWarehouseRow(start.bay(), end.bay());
            }
        } else {
            //racks on different warehouse rows
            deltaX = abs(startX - endX);
            deltaY = abs(startY - endY);
        }
        return deltaX + deltaY;
    }

    /**
     * Calculates the distance in meters a picking cart travels: from its location past every pick task in its list, and
     * back to its location again. Only reads the list variable, so it is correct even before the shadow variables
     * of the pick tasks have been computed.
     */
    public static int calculateDistanceToTravel(PickingCart pickingCart) {
        var distance = 0;
        var previousLocation = pickingCart.getLocation();
        for (var pickTask : pickingCart.getPickTasks()) {
            distance += calculateDistance(previousLocation, pickTask.getLocation());
            previousLocation = pickTask.getLocation();
        }
        distance += calculateDistance(previousLocation, pickingCart.getLocation());
        return distance;
    }

    private static Rack getRack(String rackId) {
        var rack = RACK_MAP.get(rackId);
        if (rack == null) {
            throw new IllegalArgumentException(
                    "Rack (%s) was not found in the warehouse structure.".formatted(rackId));
        }
        return rack;
    }

    private static int calculateBestYDistanceInWarehouseRow(int startY, int endY) {
        final var northDirectionDistance = startY + endY;
        final var southDirectionDistance = (RACK_LENGTH - startY) + (RACK_LENGTH - endY);
        return Math.min(northDirectionDistance, southDirectionDistance);
    }

    /**
     * Calculates the absolute X position of a location considering the warehouse structure and the rack where it's
     * contained.
     */
    private static int getAbsoluteX(Rack rack, WarehouseLocation location) {
        if (location.side() == RackSide.LEFT) {
            return rack.x();
        } else {
            return rack.x() + RACK_WIDTH;
        }
    }

    /**
     * Calculates the absolute Y position of a location considering the warehouse structure and the rack where it's
     * contained.
     */
    private static int getAbsoluteY(Rack rack, WarehouseLocation location) {
        return rack.y() + location.bay();
    }
}
