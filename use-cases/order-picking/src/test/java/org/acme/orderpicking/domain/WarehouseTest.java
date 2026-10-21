package org.acme.orderpicking.domain;

import static org.acme.orderpicking.domain.RackSide.LEFT;
import static org.acme.orderpicking.domain.RackSide.RIGHT;
import static org.acme.orderpicking.support.TestHelper.location;
import static org.acme.orderpicking.support.TestHelper.pickTask;
import static org.acme.orderpicking.support.TestHelper.pickingCart;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
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

    @Test
    void calculateDistanceToTravel() {
        // From the start location past both pick tasks and back again.
        PickingCart pickingCart = pickingCart("c1", 1, 1,
                pickTask("1", location("C3", RIGHT, 1)),
                pickTask("2", location("E1", RIGHT, 3)));

        assertThat(Warehouse.calculateDistanceToTravel(pickingCart)).isEqualTo(
                Warehouse.calculateDistance(location("A1", LEFT, 0), location("C3", RIGHT, 1))
                        + Warehouse.calculateDistance(location("C3", RIGHT, 1), location("E1", RIGHT, 3))
                        + Warehouse.calculateDistance(location("E1", RIGHT, 3), location("A1", LEFT, 0)));
        assertThat(Warehouse.calculateDistanceToTravel(pickingCart("c2", 1, 1))).isZero();
    }

    @Test
    void unknownRack() {
        assertThat(Warehouse.hasRack("E3")).isTrue();
        assertThat(Warehouse.hasRack("F1")).isFalse();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> Warehouse.calculateDistance(location("F1", LEFT, 1), location("A1", LEFT, 1)))
                .withMessageContaining("F1");
    }

    private static Stream<Arguments> calculateDistanceParams() {
        return Stream.of(
                //distances between locations on the same rack
                Arguments.of(location("B3", LEFT, 1), location("B3", LEFT, 1), 0),
                Arguments.of(location("B3", LEFT, 1), location("B3", RIGHT, 1), 4),
                Arguments.of(location("B3", LEFT, 5), location("B3", LEFT, 5), 0),
                Arguments.of(location("B3", LEFT, 1), location("B3", LEFT, 10), 9),
                Arguments.of(location("B3", LEFT, 1), location("B3", LEFT, 5), 4),
                Arguments.of(location("B3", RIGHT, 10), location("B3", RIGHT, 10), 0),
                Arguments.of(location("B3", RIGHT, 2), location("B3", RIGHT, 2), 0),
                Arguments.of(location("B3", RIGHT, 2), location("B3", RIGHT, 8), 6),
                Arguments.of(location("B3", RIGHT, 10), location("B3", RIGHT, 1), 9),
                Arguments.of(location("B3", RIGHT, 9), location("B3", RIGHT, 1), 8),
                Arguments.of(location("B3", LEFT, 2), location("B3", RIGHT, 4), 8),
                Arguments.of(location("B3", LEFT, 8), location("B3", RIGHT, 7), 7),

                //distances between locations on racks that are neighbours on the same warehouse row.
                Arguments.of(location("C2", LEFT, 1), location("D2", RIGHT, 3), 11),
                Arguments.of(location("C2", LEFT, 1), location("D2", LEFT, 3), 9),
                Arguments.of(location("C2", RIGHT, 1), location("D2", LEFT, 3), 5),
                Arguments.of(location("C2", RIGHT, 1), location("D2", RIGHT, 3), 9),
                Arguments.of(location("C2", LEFT, 8), location("D2", RIGHT, 9), 10),
                Arguments.of(location("C2", LEFT, 8), location("D2", LEFT, 9), 8),
                Arguments.of(location("C2", RIGHT, 8), location("D2", LEFT, 9), 4),
                Arguments.of(location("C2", RIGHT, 8), location("D2", RIGHT, 9), 8),

                //distances between locations on racks that are non neighbor but on the same warehouse row.
                Arguments.of(location("C3", LEFT, 1), location("E3", RIGHT, 3), 16),
                Arguments.of(location("C3", LEFT, 1), location("E3", LEFT, 3), 14),
                Arguments.of(location("C3", RIGHT, 1), location("E3", LEFT, 3), 12),
                Arguments.of(location("C3", RIGHT, 1), location("E3", RIGHT, 3), 14),
                Arguments.of(location("C3", LEFT, 9), location("E3", RIGHT, 8), 15),
                Arguments.of(location("C3", LEFT, 9), location("E3", LEFT, 8), 13),
                Arguments.of(location("C3", RIGHT, 9), location("E3", LEFT, 8), 11),
                Arguments.of(location("C3", RIGHT, 9), location("E3", RIGHT, 8), 13),

                //distances between locations on racks that are on different warehouse rows.
                Arguments.of(location("C1", LEFT, 1), location("E3", RIGHT, 3), 40),
                Arguments.of(location("C1", LEFT, 1), location("E3", LEFT, 3), 38),
                Arguments.of(location("C1", RIGHT, 1), location("E3", LEFT, 3), 36),
                Arguments.of(location("C1", RIGHT, 1), location("E3", RIGHT, 3), 38),
                Arguments.of(location("B2", LEFT, 9), location("B3", RIGHT, 8), 14),
                Arguments.of(location("B2", LEFT, 9), location("B3", LEFT, 8), 12),
                Arguments.of(location("B2", RIGHT, 9), location("B3", LEFT, 8), 14),
                Arguments.of(location("B2", RIGHT, 12), location("B3", RIGHT, 11), 12));
    }
}
