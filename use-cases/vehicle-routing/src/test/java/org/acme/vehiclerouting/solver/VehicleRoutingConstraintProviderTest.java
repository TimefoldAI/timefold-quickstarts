package org.acme.vehiclerouting.solver;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;

import jakarta.inject.Inject;

import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;

import org.acme.vehiclerouting.domain.Location;
import org.acme.vehiclerouting.domain.Vehicle;
import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.acme.vehiclerouting.domain.Visit;
import org.acme.vehiclerouting.domain.geo.HaversineDrivingTimeCalculator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class VehicleRoutingConstraintProviderTest {

    /*
     * LOCATION_1 to LOCATION_2 is approx. 11713 m ~843 seconds of driving time
     * LOCATION_2 to LOCATION_3 is approx. 8880 m ~639 seconds of driving time
     * LOCATION_1 to LOCATION_3 is approx. 13075 m ~941 seconds of driving time
     */
    private static final Location LOCATION_1 = new Location(49.288087, 16.562172);
    private static final Location LOCATION_2 = new Location(49.190922, 16.624466);
    private static final Location LOCATION_3 = new Location(49.1767533245638, 16.50422914190477);

    private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);
    @Inject
    ConstraintVerifier<VehicleRoutingConstraintProvider, VehicleRoutePlan> constraintVerifier;

    @BeforeAll
    static void initDrivingTimeMaps() {
        HaversineDrivingTimeCalculator.getInstance().initDrivingTimeMaps(Arrays.asList(LOCATION_1, LOCATION_2, LOCATION_3));
    }

    @Test
    void vehicleCapacityUnpenalized() {
        LocalDateTime tomorrow_07_00 = LocalDateTime.of(TOMORROW, LocalTime.of(7, 0));
        LocalDateTime tomorrow_08_00 = LocalDateTime.of(TOMORROW, LocalTime.of(8, 0));
        LocalDateTime tomorrow_10_00 = LocalDateTime.of(TOMORROW, LocalTime.of(10, 0));
        Vehicle vehicleA = new Vehicle("1", 100, LOCATION_1, tomorrow_07_00);
        Visit visit1 = new Visit("2", "John", LOCATION_2, 80, tomorrow_08_00, tomorrow_10_00, Duration.ofMinutes(30L));
        vehicleA.getVisits().add(visit1);

        constraintVerifier.verifyThat(VehicleRoutingConstraintProvider::vehicleCapacity)
                .given(vehicleA, visit1)
                .penalizesBy(0);
    }

    @Test
    void vehicleCapacityPenalized() {
        LocalDateTime tomorrow_07_00 = LocalDateTime.of(TOMORROW, LocalTime.of(7, 0));
        LocalDateTime tomorrow_08_00 = LocalDateTime.of(TOMORROW, LocalTime.of(8, 0));
        LocalDateTime tomorrow_10_00 = LocalDateTime.of(TOMORROW, LocalTime.of(10, 0));
        Vehicle vehicleA = new Vehicle("1", 100, LOCATION_1, tomorrow_07_00);
        Visit visit1 = new Visit("2", "John", LOCATION_2, 80, tomorrow_08_00, tomorrow_10_00, Duration.ofMinutes(30L));
        vehicleA.getVisits().add(visit1);
        Visit visit2 = new Visit("3", "Paul", LOCATION_3, 40, tomorrow_08_00, tomorrow_10_00, Duration.ofMinutes(30L));
        vehicleA.getVisits().add(visit2);

        constraintVerifier.verifyThat(VehicleRoutingConstraintProvider::vehicleCapacity)
                .given(vehicleA, visit1, visit2)
                .penalizesBy(20);
    }

    @Test
    void totalDrivingTime() {
        LocalDateTime tomorrow_07_00 = LocalDateTime.of(TOMORROW, LocalTime.of(7, 0));
        LocalDateTime tomorrow_08_00 = LocalDateTime.of(TOMORROW, LocalTime.of(8, 0));
        LocalDateTime tomorrow_10_00 = LocalDateTime.of(TOMORROW, LocalTime.of(10, 0));
        Vehicle vehicleA = new Vehicle("1", 100, LOCATION_1, tomorrow_07_00);
        Visit visit1 = new Visit("2", "John", LOCATION_2, 80, tomorrow_08_00, tomorrow_10_00, Duration.ofMinutes(30L));
        vehicleA.getVisits().add(visit1);
        Visit visit2 = new Visit("3", "Paul", LOCATION_3, 40, tomorrow_08_00, tomorrow_10_00, Duration.ofMinutes(30L));
        vehicleA.getVisits().add(visit2);

        constraintVerifier.verifyThat(VehicleRoutingConstraintProvider::minimizeTravelTime)
                .given(vehicleA, visit1, visit2)
                .penalizesBy(2423L); // The sum of the approximate driving time between all three locations.
    }

    @Test
    void serviceFinishedAfterMaxEndTime() {
        LocalDateTime tomorrow_07_00 = LocalDateTime.of(TOMORROW, LocalTime.of(7, 0));
        LocalDateTime tomorrow_08_00 = LocalDateTime.of(TOMORROW, LocalTime.of(8, 0));
        LocalDateTime tomorrow_08_40 = LocalDateTime.of(TOMORROW, LocalTime.of(8, 40));
        LocalDateTime tomorrow_09_00 = LocalDateTime.of(TOMORROW, LocalTime.of(9, 0));
        LocalDateTime tomorrow_10_30 = LocalDateTime.of(TOMORROW, LocalTime.of(10, 30));
        LocalDateTime tomorrow_18_00 = LocalDateTime.of(TOMORROW, LocalTime.of(18, 0));

        Visit visit1 = new Visit("2", "John", LOCATION_2, 80, tomorrow_08_00, tomorrow_18_00, Duration.ofHours(1L));
        visit1.setArrivalTime(tomorrow_08_40);
        Visit visit2 = new Visit("3", "Paul", LOCATION_3, 40, tomorrow_08_00, tomorrow_09_00, Duration.ofHours(1L));
        visit2.setArrivalTime(tomorrow_10_30);
        Vehicle vehicleA = new Vehicle("1", 100, LOCATION_1, tomorrow_07_00);

        connect(vehicleA, visit1, visit2);

        constraintVerifier.verifyThat(VehicleRoutingConstraintProvider::serviceFinishedAfterMaxEndTime)
                .given(vehicleA, visit1, visit2)
                .penalizesBy(90 + visit2.getServiceDuration().toMinutes());
    }

    static void connect(Vehicle vehicle, Visit... visits) {
        vehicle.setVisits(Arrays.asList(visits));
        for (int i = 0; i < visits.length; i++) {
            Visit visit = visits[i];
            visit.setVehicle(vehicle);
            if (i > 0) {
                visit.setPreviousVisit(visits[i - 1]);
            }
            if (i < visits.length - 1) {
                visit.setNextVisit(visits[i + 1]);
            }
        }
    }
}
