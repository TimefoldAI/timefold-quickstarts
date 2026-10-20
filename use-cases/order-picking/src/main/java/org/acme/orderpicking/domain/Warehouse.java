package org.acme.orderpicking.domain;

import static java.lang.Math.abs;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Models the warehouse where the order picking problem is formulated and is a static structure composed of shelvings
 * organized in columns and rows. A shelving can be identified by the warehouse column and row number where it's placed.
 * The store products are located on the shelvings, each shelving can contain products on the LEFT and RIGHT sides, and
 * on a row on the particular shelving side.
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
 * @see Shelving
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
     * The number of rows on each side of a shelving, numbered from 0 at the top. A row is one meter high.
     */
    public static final int SHELVING_ROWS = 10;

    /**
     * The shelving width in meters.
     */
    private static final int SHELVING_WIDTH = 2;
    /**
     * The shelving height in meters.
     */
    private static final int SHELVING_HEIGHT = SHELVING_ROWS;
    /**
     * Spacing between the shelving rows and columns in meters.
     */
    private static final int SHELVING_PADDING = 3;

    private static final Map<String, Shelving> SHELVING_MAP = new LinkedHashMap<>();

    static {
        var shelvingX = 0;
        for (String column : COLUMNS) {
            var shelvingY = 0;
            for (int row = 1; row <= ROW_COUNT; row++) {
                var shelving = new Shelving(shelvingId(column, row), shelvingX, shelvingY);
                SHELVING_MAP.put(shelving.id(), shelving);
                shelvingY = shelvingY + SHELVING_HEIGHT + SHELVING_PADDING;
            }
            shelvingX = shelvingX + SHELVING_WIDTH + SHELVING_PADDING;
        }
    }

    private Warehouse() {
    }

    public static String shelvingId(String column, int row) {
        return column + row;
    }

    public static boolean hasShelving(String shelvingId) {
        return SHELVING_MAP.containsKey(shelvingId);
    }

    /**
     * Calculates the distance in meters between two locations considering the warehouse structure.
     */
    public static int calculateDistance(WarehouseLocation start, WarehouseLocation end) {
        final var startShelving = getShelving(start.shelvingId());
        final var endShelving = getShelving(end.shelvingId());
        var deltaX = 0;
        var deltaY = 0;

        final var startX = getAbsoluteX(startShelving, start);
        final var startY = getAbsoluteY(startShelving, start);
        final var endX = getAbsoluteX(endShelving, end);
        final var endY = getAbsoluteY(endShelving, end);

        if (startShelving.equals(endShelving)) {
            //same shelving
            if (start.side() == end.side()) {
                //same side
                deltaY = abs(startY - endY);
            } else {
                //different side, calculate shortest walk.
                deltaX = SHELVING_WIDTH;
                deltaY = calculateBestYDistanceInShelvingRow(start.row(), end.row());
            }
        } else if (startShelving.y() == endShelving.y()) {
            //distinct shelvings but on the same warehouse row
            if (abs(startX - endX) == SHELVING_PADDING) {
                //neighbor shelvings, but also contiguous side
                deltaX = SHELVING_PADDING;
                deltaY = abs(startY - endY);
            } else {
                //any other combination of shelvings but in the same warehouse row
                deltaX = abs(startX - endX);
                deltaY = calculateBestYDistanceInShelvingRow(start.row(), end.row());
            }
        } else {
            //shelvings on different warehouse rows
            deltaX = abs(startX - endX);
            deltaY = abs(startY - endY);
        }
        return deltaX + deltaY;
    }

    /**
     * Calculates the distance in meters a trolley travels: from its location past every pick task in its list, and
     * back to its location again. Only reads the list variable, so it is correct even before the shadow variables
     * of the pick tasks have been computed.
     */
    public static int calculateDistanceToTravel(Trolley trolley) {
        var distance = 0;
        var previousLocation = trolley.getLocation();
        for (var pickTask : trolley.getPickTasks()) {
            distance += calculateDistance(previousLocation, pickTask.getLocation());
            previousLocation = pickTask.getLocation();
        }
        distance += calculateDistance(previousLocation, trolley.getLocation());
        return distance;
    }

    private static Shelving getShelving(String shelvingId) {
        var shelving = SHELVING_MAP.get(shelvingId);
        if (shelving == null) {
            throw new IllegalArgumentException(
                    "Shelving (%s) was not found in the warehouse structure.".formatted(shelvingId));
        }
        return shelving;
    }

    private static int calculateBestYDistanceInShelvingRow(int startY, int endY) {
        final var northDirectionDistance = startY + endY;
        final var southDirectionDistance = (SHELVING_HEIGHT - startY) + (SHELVING_HEIGHT - endY);
        return Math.min(northDirectionDistance, southDirectionDistance);
    }

    /**
     * Calculates the absolute X position of a location considering the warehouse structure and the shelving where it's
     * contained.
     */
    private static int getAbsoluteX(Shelving shelving, WarehouseLocation location) {
        if (location.side() == ShelvingSide.LEFT) {
            return shelving.x();
        } else {
            return shelving.x() + SHELVING_WIDTH;
        }
    }

    /**
     * Calculates the absolute Y position of a location considering the warehouse structure and the shelving where it's
     * contained.
     */
    private static int getAbsoluteY(Shelving shelving, WarehouseLocation location) {
        return shelving.y() + location.row();
    }
}
