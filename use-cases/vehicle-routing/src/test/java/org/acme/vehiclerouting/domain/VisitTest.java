package org.acme.vehiclerouting.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.acme.vehiclerouting.domain.geo.HaversineDrivingTimeCalculator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class VisitTest {

    private static final Location LOCATION_1 = new Location(49.288087, 16.562172);
    private static final Location LOCATION_2 = new Location(49.190922, 16.624466);
    private static final Location LOCATION_3 = new Location(49.1767533245638, 16.50422914190477);

    private static final LocalDateTime DEPARTURE = LocalDateTime.of(2027, 2, 1, 7, 30);
    private static final LocalDateTime WINDOW_START = LocalDateTime.of(2027, 2, 1, 8, 0);
    private static final LocalDateTime WINDOW_END = LocalDateTime.of(2027, 2, 1, 18, 0);

    @BeforeAll
    static void initDrivingTimeMaps() {
        HaversineDrivingTimeCalculator.getInstance().initDrivingTimeMaps(Arrays.asList(LOCATION_1, LOCATION_2, LOCATION_3));
    }

    private static Visit visit(String id, Location location) {
        return new Visit(id, "visit-" + id, location, 10, WINDOW_START, WINDOW_END, Duration.ofMinutes(30L));
    }

    @Test
    void arrivalTimeSupplierReturnsNullWhenUnassigned() {
        Visit visit = visit("1", LOCATION_2);
        assertThat(visit.arrivalTimeSupplier()).isNull();
        assertThat(visit.getDepartureTime()).isNull();
        assertThat(visit.getStartServiceTime()).isNull();
        assertThat(visit.getServiceFinishedDelayInMinutes()).isZero();
        assertThat(visit.getDrivingTimeSecondsFromPreviousStandstillOrNull()).isNull();
    }

    @Test
    void arrivalTimeSupplierFromVehicle() {
        Vehicle vehicle = new Vehicle("1", 100, LOCATION_1, DEPARTURE);
        Visit visit = visit("2", LOCATION_2);
        vehicle.getVisits().add(visit);
        visit.setVehicle(vehicle);

        LocalDateTime arrival = visit.arrivalTimeSupplier();
        assertThat(arrival).isNotNull();
        assertThat(visit.getDrivingTimeSecondsFromPreviousStandstill()).isPositive();
        assertThat(visit.getDrivingTimeSecondsFromPreviousStandstillOrNull()).isNotNull();
    }

    @Test
    void arrivalTimeSupplierFromPreviousVisit() {
        Vehicle vehicle = new Vehicle("1", 100, LOCATION_1, DEPARTURE);
        Visit visit1 = visit("2", LOCATION_2);
        Visit visit2 = visit("3", LOCATION_3);
        vehicle.setVisits(Arrays.asList(visit1, visit2));
        visit1.setVehicle(vehicle);
        visit2.setVehicle(vehicle);
        visit2.setPreviousVisit(visit1);

        visit1.setArrivalTime(visit1.arrivalTimeSupplier());
        assertThat(visit2.arrivalTimeSupplier()).isNotNull();
    }

    @Test
    void arrivalTimeSupplierNullWhenPreviousDepartureUnknown() {
        Visit visit1 = visit("2", LOCATION_2);
        Visit visit2 = visit("3", LOCATION_3);
        visit2.setPreviousVisit(visit1);
        // visit1 has no arrival time, so its departure time is null.
        assertThat(visit2.arrivalTimeSupplier()).isNull();
    }

    @Test
    void serviceFinishedAfterMaxEndTime() {
        Vehicle vehicle = new Vehicle("1", 100, LOCATION_1, DEPARTURE);
        Visit visit = new Visit("2", "late", LOCATION_2, 10, WINDOW_START,
                LocalDateTime.of(2027, 2, 1, 8, 10), Duration.ofHours(1L));
        vehicle.getVisits().add(visit);
        visit.setVehicle(vehicle);
        visit.setArrivalTime(LocalDateTime.of(2027, 2, 1, 8, 0));

        assertThat(visit.isServiceFinishedAfterMaxEndTime()).isTrue();
        assertThat(visit.getServiceFinishedDelayInMinutes()).isPositive();
        assertThat(visit.getDepartureTime()).isNotNull();
        assertThat(visit.getStartServiceTime()).isNotNull();
        assertThat(List.of(visit)).hasSize(1);
    }

    @Test
    void drivingTimeThrowsWhenVehicleMissing() {
        Visit visit = visit("2", LOCATION_2);
        assertThatThrownBy(visit::getDrivingTimeSecondsFromPreviousStandstill)
                .isInstanceOf(IllegalStateException.class);
    }
}
