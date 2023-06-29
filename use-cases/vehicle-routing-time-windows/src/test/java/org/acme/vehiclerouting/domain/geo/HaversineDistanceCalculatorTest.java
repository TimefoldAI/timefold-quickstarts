package org.acme.vehiclerouting.domain.geo;

import org.acme.vehiclerouting.domain.Location;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class HaversineDistanceCalculatorTest {

    private final DistanceCalculator distanceCalculator = new HaversineDistanceCalculator();

    // Results have been verified with the help of https://latlongdata.com/.
    @Test
    void calculateDistance() {
        Location Gent = new Location(51.0441461, 3.7336349);
        Location Brno = new Location(49.1913945, 16.6122723);
        Assertions.assertThat(distanceCalculator.calculateDistance(Gent, Brno))
                .isEqualTo(HaversineDistanceCalculator.kilometersToDrivingSeconds(939.748));

        // Close to the North Pole.
        Location Svolvaer = new Location(68.2359953, 14.5644379);
        Location Lulea = new Location(65.5887708, 22.1518707);
        Assertions.assertThat(distanceCalculator.calculateDistance(Svolvaer, Lulea))
                .isEqualTo(HaversineDistanceCalculator.kilometersToDrivingSeconds(442.297));
    }
}
