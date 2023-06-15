package org.acme.vehiclerouting.domain.geo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.acme.vehiclerouting.domain.Location;
import org.junit.jupiter.api.Test;

class EuclideanDistanceCalculatorTest {

    @Test
    void calculateDistance() {
        Location a = new Location(0.0, 0.0);
        Location b = new Location(0.0, 4.0);
        EuclideanDistanceCalculator distanceCalculator = new EuclideanDistanceCalculator();
        assertThat(distanceCalculator.calculateDistance(a, a)).isZero();
        assertThat(distanceCalculator.calculateDistance(b, b)).isZero();
        assertThat(distanceCalculator.calculateDistance(a, b)).isEqualTo(distanceCalculator.calculateDistance(b, a));
        assertThat(distanceCalculator.calculateDistance(a, b))
                .isEqualTo(4 * EuclideanDistanceCalculator.METERS_PER_DEGREE);
    }

    @Test
    void distanceMap() {
        Location a = new Location(0.0, 0.0);
        Location b = new Location(0.0, 4.0);
        List<Location> locations = Arrays.asList(a, b);
        EuclideanDistanceCalculator distanceCalculator = new EuclideanDistanceCalculator();
        Map<Location, Map<Location, Long>> distanceMatrix = distanceCalculator.calculateBulkDistance(locations, locations);
        assertThat(distanceMatrix.get(a).get(b)).isEqualTo(distanceCalculator.calculateDistance(a, b));
    }
}
