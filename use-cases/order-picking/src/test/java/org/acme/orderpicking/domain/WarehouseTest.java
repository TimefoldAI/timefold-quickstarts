package org.acme.orderpicking.domain;

import static org.acme.orderpicking.domain.Shelving.newShelvingId;
import static org.acme.orderpicking.domain.Warehouse.Column.COL_B;
import static org.acme.orderpicking.domain.Warehouse.Column.COL_C;
import static org.acme.orderpicking.domain.Warehouse.Column.COL_D;
import static org.acme.orderpicking.domain.Warehouse.Column.COL_E;
import static org.acme.orderpicking.domain.Warehouse.Row.ROW_1;
import static org.acme.orderpicking.domain.Warehouse.Row.ROW_2;
import static org.acme.orderpicking.domain.Warehouse.Row.ROW_3;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class WarehouseTest {

    @ParameterizedTest
    @MethodSource("calculateDistanceParams")
    void calculateDistance(WarehouseLocation start, WarehouseLocation end, int expectedDistance) {
        assertThat(Warehouse.calculateDistance(start, end))
                .withFailMessage("Distance from %s to %s must be %s", start, end, expectedDistance)
                .isEqualTo(expectedDistance);
        assertThat(Warehouse.calculateDistance(end, start))
                .withFailMessage("Distance from %s to %s must be %s", start, end, expectedDistance)
                .isEqualTo(expectedDistance);
    }

    private static Stream<Arguments> calculateDistanceParams() {
        return Stream.of(
                //distances between locations on the same shelving
                Arguments.of(
                        new WarehouseLocation(newShelvingId(COL_B, ROW_3), Shelving.Side.LEFT, 1),
                        new WarehouseLocation(newShelvingId(COL_B, ROW_3), Shelving.Side.LEFT, 1),
                        0),
                Arguments.of(
                        new WarehouseLocation(newShelvingId(COL_B, ROW_3), Shelving.Side.LEFT, 1),
                        new WarehouseLocation(newShelvingId(COL_B, ROW_3), Shelving.Side.RIGHT, 1),
                        4),
                Arguments.of(
                        new WarehouseLocation(newShelvingId(COL_B, ROW_3), Shelving.Side.LEFT, 5),
                        new WarehouseLocation(newShelvingId(COL_B, ROW_3), Shelving.Side.LEFT, 5),
                        0),
                Arguments.of(
                        new WarehouseLocation(newShelvingId(COL_B, ROW_3), Shelving.Side.LEFT, 1),
                        new WarehouseLocation(newShelvingId(COL_B, ROW_3), Shelving.Side.LEFT, 10),
                        9),
                Arguments.of(
                        new WarehouseLocation(newShelvingId(COL_B, ROW_3), Shelving.Side.LEFT, 1),
                        new WarehouseLocation(newShelvingId(COL_B, ROW_3), Shelving.Side.LEFT, 5),
                        4),
                Arguments.of(
                        new WarehouseLocation(newShelvingId(COL_B, ROW_3), Shelving.Side.RIGHT, 10),
                        new WarehouseLocation(newShelvingId(COL_B, ROW_3), Shelving.Side.RIGHT, 10),
                        0),
                Arguments.of(
                        new WarehouseLocation(newShelvingId(COL_B, ROW_3), Shelving.Side.RIGHT, 2),
                        new WarehouseLocation(newShelvingId(COL_B, ROW_3), Shelving.Side.RIGHT, 2),
                        0),
                Arguments.of(
                        new WarehouseLocation(newShelvingId(COL_B, ROW_3), Shelving.Side.RIGHT, 2),
                        new WarehouseLocation(newShelvingId(COL_B, ROW_3), Shelving.Side.RIGHT, 8),
                        6),
                Arguments.of(
                        new WarehouseLocation(newShelvingId(COL_B, ROW_3), Shelving.Side.RIGHT, 10),
                        new WarehouseLocation(newShelvingId(COL_B, ROW_3), Shelving.Side.RIGHT, 1),
                        9),
                Arguments.of(
                        new WarehouseLocation(newShelvingId(COL_B, ROW_3), Shelving.Side.RIGHT, 9),
                        new WarehouseLocation(newShelvingId(COL_B, ROW_3), Shelving.Side.RIGHT, 1),
                        8),
                Arguments.of(
                        new WarehouseLocation(newShelvingId(COL_B, ROW_3), Shelving.Side.LEFT, 2),
                        new WarehouseLocation(newShelvingId(COL_B, ROW_3), Shelving.Side.RIGHT, 4),
                        8),
                Arguments.of(
                        new WarehouseLocation(newShelvingId(COL_B, ROW_3), Shelving.Side.LEFT, 8),
                        new WarehouseLocation(newShelvingId(COL_B, ROW_3), Shelving.Side.RIGHT, 7),
                        7),

                //distances between locations on shelvings that are neighbours on the same warehouse row.
                Arguments.of(
                        new WarehouseLocation(newShelvingId(COL_C, ROW_2), Shelving.Side.LEFT, 1),
                        new WarehouseLocation(newShelvingId(COL_D, ROW_2), Shelving.Side.RIGHT, 3),
                        11),
                Arguments.of(
                        new WarehouseLocation(newShelvingId(COL_C, ROW_2), Shelving.Side.LEFT, 1),
                        new WarehouseLocation(newShelvingId(COL_D, ROW_2), Shelving.Side.LEFT, 3),
                        9),
                Arguments.of(
                        new WarehouseLocation(newShelvingId(COL_C, ROW_2), Shelving.Side.RIGHT, 1),
                        new WarehouseLocation(newShelvingId(COL_D, ROW_2), Shelving.Side.LEFT, 3),
                        5),
                Arguments.of(
                        new WarehouseLocation(newShelvingId(COL_C, ROW_2), Shelving.Side.RIGHT, 1),
                        new WarehouseLocation(newShelvingId(COL_D, ROW_2), Shelving.Side.RIGHT, 3),
                        9),
                Arguments.of(
                        new WarehouseLocation(newShelvingId(COL_C, ROW_2), Shelving.Side.LEFT, 8),
                        new WarehouseLocation(newShelvingId(COL_D, ROW_2), Shelving.Side.RIGHT, 9),
                        10),
                Arguments.of(
                        new WarehouseLocation(newShelvingId(COL_C, ROW_2), Shelving.Side.LEFT, 8),
                        new WarehouseLocation(newShelvingId(COL_D, ROW_2), Shelving.Side.LEFT, 9),
                        8),
                Arguments.of(
                        new WarehouseLocation(newShelvingId(COL_C, ROW_2), Shelving.Side.RIGHT, 8),
                        new WarehouseLocation(newShelvingId(COL_D, ROW_2), Shelving.Side.LEFT, 9),
                        4),
                Arguments.of(
                        new WarehouseLocation(newShelvingId(COL_C, ROW_2), Shelving.Side.RIGHT, 8),
                        new WarehouseLocation(newShelvingId(COL_D, ROW_2), Shelving.Side.RIGHT, 9),
                        8),

                //distances between locations on shelvings that are non neighbor but on the same warehouse row.
                Arguments.of(
                        new WarehouseLocation(newShelvingId(COL_C, ROW_3), Shelving.Side.LEFT, 1),
                        new WarehouseLocation(newShelvingId(COL_E, ROW_3), Shelving.Side.RIGHT, 3),
                        16),
                Arguments.of(
                        new WarehouseLocation(newShelvingId(COL_C, ROW_3), Shelving.Side.LEFT, 1),
                        new WarehouseLocation(newShelvingId(COL_E, ROW_3), Shelving.Side.LEFT, 3),
                        14),
                Arguments.of(
                        new WarehouseLocation(newShelvingId(COL_C, ROW_3), Shelving.Side.RIGHT, 1),
                        new WarehouseLocation(newShelvingId(COL_E, ROW_3), Shelving.Side.LEFT, 3),
                        12),
                Arguments.of(
                        new WarehouseLocation(newShelvingId(COL_C, ROW_3), Shelving.Side.RIGHT, 1),
                        new WarehouseLocation(newShelvingId(COL_E, ROW_3), Shelving.Side.RIGHT, 3),
                        14),
                Arguments.of(
                        new WarehouseLocation(newShelvingId(COL_C, ROW_3), Shelving.Side.LEFT, 9),
                        new WarehouseLocation(newShelvingId(COL_E, ROW_3), Shelving.Side.RIGHT, 8),
                        15),
                Arguments.of(
                        new WarehouseLocation(newShelvingId(COL_C, ROW_3), Shelving.Side.LEFT, 9),
                        new WarehouseLocation(newShelvingId(COL_E, ROW_3), Shelving.Side.LEFT, 8),
                        13),
                Arguments.of(
                        new WarehouseLocation(newShelvingId(COL_C, ROW_3), Shelving.Side.RIGHT, 9),
                        new WarehouseLocation(newShelvingId(COL_E, ROW_3), Shelving.Side.LEFT, 8),
                        11),
                Arguments.of(
                        new WarehouseLocation(newShelvingId(COL_C, ROW_3), Shelving.Side.RIGHT, 9),
                        new WarehouseLocation(newShelvingId(COL_E, ROW_3), Shelving.Side.RIGHT, 8),
                        13),

                //distances between locations on shelvings that are on different warehouse rows.
                Arguments.of(
                        new WarehouseLocation(newShelvingId(COL_C, ROW_1), Shelving.Side.LEFT, 1),
                        new WarehouseLocation(newShelvingId(COL_E, ROW_3), Shelving.Side.RIGHT, 3),
                        40),
                Arguments.of(
                        new WarehouseLocation(newShelvingId(COL_C, ROW_1), Shelving.Side.LEFT, 1),
                        new WarehouseLocation(newShelvingId(COL_E, ROW_3), Shelving.Side.LEFT, 3),
                        38),
                Arguments.of(
                        new WarehouseLocation(newShelvingId(COL_C, ROW_1), Shelving.Side.RIGHT, 1),
                        new WarehouseLocation(newShelvingId(COL_E, ROW_3), Shelving.Side.LEFT, 3),
                        36),
                Arguments.of(
                        new WarehouseLocation(newShelvingId(COL_C, ROW_1), Shelving.Side.RIGHT, 1),
                        new WarehouseLocation(newShelvingId(COL_E, ROW_3), Shelving.Side.RIGHT, 3),
                        38),
                Arguments.of(
                        new WarehouseLocation(newShelvingId(COL_B, ROW_2), Shelving.Side.LEFT, 9),
                        new WarehouseLocation(newShelvingId(COL_B, ROW_3), Shelving.Side.RIGHT, 8),
                        14),
                Arguments.of(
                        new WarehouseLocation(newShelvingId(COL_B, ROW_2), Shelving.Side.LEFT, 9),
                        new WarehouseLocation(newShelvingId(COL_B, ROW_3), Shelving.Side.LEFT, 8),
                        12),
                Arguments.of(
                        new WarehouseLocation(newShelvingId(COL_B, ROW_2), Shelving.Side.RIGHT, 9),
                        new WarehouseLocation(newShelvingId(COL_B, ROW_3), Shelving.Side.LEFT, 8),
                        14),
                Arguments.of(
                        new WarehouseLocation(newShelvingId(COL_B, ROW_2), Shelving.Side.RIGHT, 12),
                        new WarehouseLocation(newShelvingId(COL_B, ROW_3), Shelving.Side.RIGHT, 11),
                        12));
    }
}