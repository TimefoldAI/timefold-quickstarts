package org.acme.vehiclerouting.solver;

import java.time.Duration;
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

    @Inject
    ConstraintVerifier<VehicleRoutingConstraintProvider, VehicleRoutePlan> constraintVerifier;
    private static final Location location1 = new Location( 0.0, 0.0);
    private static final Location location2 = new Location(0.0, 4.0);
    private static final Location location3 = new Location( 3.0, 0.0);

    @BeforeAll
    static void initDistanceMaps() {
        new EuclideanDistanceCalculator().initDistanceMaps(Arrays.asList(location1, location2, location3));
    }

    @Test
    void totalDistance() {
        Vehicle vehicleA = new Vehicle(1L, new Depot(1L, location1), LocalTime.of(7, 0));
        Customer customer1 = new Customer(2L, location2, LocalTime.of(8, 0), LocalTime.of(10, 0), Duration.ofMinutes(30L));
        vehicleA.getCustomers().add(customer1);
        Customer customer2 = new Customer(3L, location3, LocalTime.of(8, 0), LocalTime.of(10, 0), Duration.ofMinutes(30L));
        vehicleA.getCustomers().add(customer2);

        constraintVerifier.verifyThat(VehicleRoutingConstraintProvider::totalDistance)
                .given(vehicleA, customer1, customer2)
                .penalizesBy((4 + 5 + 3) * EuclideanDistanceCalculator.METERS_PER_DEGREE);
    }

    @Test
    void serviceFinishedAfterDueTime() {
        Customer customer1 = new Customer(2L, location2, LocalTime.of(8, 0), LocalTime.of(18, 0), Duration.ofHours(1L));
        customer1.setArrivalTime(LocalTime.of(8, 40));
        Customer customer2 = new Customer(3L, location3, LocalTime.of(8, 0), LocalTime.of(9, 0), Duration.ofHours(1L));
        customer2.setArrivalTime(LocalTime.of(8 + 1 + 1, 30));
        Vehicle vehicleA = new Vehicle(1L, new Depot(1L, location1), LocalTime.of(7, 0));

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
