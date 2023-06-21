package org.acme.vehiclerouting.solver;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;

import jakarta.inject.Inject;

import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;

import org.acme.vehiclerouting.domain.Customer;
import org.acme.vehiclerouting.domain.Depot;
import org.acme.vehiclerouting.domain.Location;
import org.acme.vehiclerouting.domain.Vehicle;
import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.acme.vehiclerouting.domain.geo.EuclideanDistanceCalculator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class VehicleRoutingConstraintProviderTest {
    private static final Location location1 = new Location( 0.0, 0.0);
    private static final Location location2 = new Location(0.0, 4.0);
    private static final Location location3 = new Location( 3.0, 0.0);

    private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);
    @Inject
    ConstraintVerifier<VehicleRoutingConstraintProvider, VehicleRoutePlan> constraintVerifier;

    @BeforeAll
    static void initDistanceMaps() {
        new EuclideanDistanceCalculator().initDistanceMaps(Arrays.asList(location1, location2, location3));
    }

    @Test
    void totalDistance() {
        LocalDateTime tomorrow_07_00 = LocalDateTime.of(TOMORROW, LocalTime.of(7, 0));
        LocalDateTime tomorrow_08_00 = LocalDateTime.of(TOMORROW, LocalTime.of(8, 0));
        LocalDateTime tomorrow_10_00 = LocalDateTime.of(TOMORROW, LocalTime.of(10, 0));
        Vehicle vehicleA = new Vehicle(1L, new Depot(1L, location1), tomorrow_07_00);
        Customer customer1 = new Customer(2L, location2, tomorrow_08_00, tomorrow_10_00, Duration.ofMinutes(30L));
        vehicleA.getCustomers().add(customer1);
        Customer customer2 = new Customer(3L, location3, tomorrow_08_00, tomorrow_10_00, Duration.ofMinutes(30L));
        vehicleA.getCustomers().add(customer2);

        constraintVerifier.verifyThat(VehicleRoutingConstraintProvider::totalDistance)
                .given(vehicleA, customer1, customer2)
                .penalizesBy((4 + 5 + 3) * EuclideanDistanceCalculator.METERS_PER_DEGREE);
    }

    @Test
    void serviceFinishedAfterDueTime() {
        LocalDateTime tomorrow_07_00 = LocalDateTime.of(TOMORROW, LocalTime.of(7, 0));
        LocalDateTime tomorrow_08_00 = LocalDateTime.of(TOMORROW, LocalTime.of(8, 0));
        LocalDateTime tomorrow_08_40 = LocalDateTime.of(TOMORROW, LocalTime.of(8, 40));
        LocalDateTime tomorrow_09_00 = LocalDateTime.of(TOMORROW, LocalTime.of(9, 0));
        LocalDateTime tomorrow_10_30 = LocalDateTime.of(TOMORROW, LocalTime.of(10, 30));
        LocalDateTime tomorrow_18_00 = LocalDateTime.of(TOMORROW, LocalTime.of(18, 0));

        Customer customer1 = new Customer(2L, location2, tomorrow_08_00, tomorrow_18_00, Duration.ofHours(1L));
        customer1.setArrivalTime(tomorrow_08_40);
        Customer customer2 = new Customer(3L, location3, tomorrow_08_00, tomorrow_09_00, Duration.ofHours(1L));
        customer2.setArrivalTime(tomorrow_10_30);
        Vehicle vehicleA = new Vehicle(1L, new Depot(1L, location1), tomorrow_07_00);

        connect(vehicleA, customer1, customer2);

        constraintVerifier.verifyThat(VehicleRoutingConstraintProvider::serviceFinishedAfterDueTime)
                .given(vehicleA, customer1, customer2)
                .penalizesBy(90 + customer2.getServiceDuration().toMinutes());
    }

    static void connect(Vehicle vehicle, Customer... customers) {
        vehicle.setCustomers(Arrays.asList(customers));
        for (int i = 0; i < customers.length; i++) {
            Customer customer = customers[i];
            customer.setVehicle(vehicle);
            if (i > 0) {
                customer.setPreviousCustomer(customers[i - 1]);
            }
            if (i < customers.length - 1) {
                customer.setNextCustomer(customers[i + 1]);
            }
        }
    }
}
