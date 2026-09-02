package org.acme.vehiclerouting.domain.geo;

import static org.acme.vehiclerouting.support.TestHelper.drivingTimeSeconds;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HaversineDrivingTimeCalculatorTest {

    // Results have been verified with the help of https://latlongdata.com/.
    @Test
    void calculateDrivingTime() {
        // Gent to Brno.
        assertThat(drivingTimeSeconds(51.0441461, 3.7336349, 49.1913945, 16.6122723))
                .isEqualTo(HaversineDrivingTimeCalculator.metersToDrivingSeconds(939748));

        // Svolvaer to Lulea, close to the North Pole.
        assertThat(drivingTimeSeconds(68.2359953, 14.5644379, 65.5887708, 22.1518707))
                .isEqualTo(HaversineDrivingTimeCalculator.metersToDrivingSeconds(442297));
    }
}
