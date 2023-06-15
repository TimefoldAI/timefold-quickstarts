package org.acme.vehiclerouting.solver;

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
    void vehicleCapacityUnpenalized() {
        Vehicle vehicleA = new Vehicle(1L, 100, new Depot(1L, location1));
        Customer customer1 = new Customer(2L, location2, 80);
        vehicleA.getCustomers().add(customer1);

        constraintVerifier.verifyThat(VehicleRoutingConstraintProvider::vehicleCapacity)
                .given(vehicleA, customer1)
                .penalizesBy(0);
    }

    @Test
    void vehicleCapacityPenalized() {
        Vehicle vehicleA = new Vehicle(1L, 100, new Depot(1L, location1));
        Customer customer1 = new Customer(2L, location2, 80);
        vehicleA.getCustomers().add(customer1);

        Customer customer2 = new Customer(3L, location3, 40);
        vehicleA.getCustomers().add(customer2);

        constraintVerifier.verifyThat(VehicleRoutingConstraintProvider::vehicleCapacity)
                .given(vehicleA, customer1, customer2)
                .penalizesBy(20);
    }

    @Test
    void totalDistance() {
        Vehicle vehicleA = new Vehicle(1L, 100, new Depot(1L, location1));
        Customer customer1 = new Customer(2L, location2, 80);
        vehicleA.getCustomers().add(customer1);
        Customer customer2 = new Customer(3L, location3, 40);
        vehicleA.getCustomers().add(customer2);

        constraintVerifier.verifyThat(VehicleRoutingConstraintProvider::totalDistance)
                .given(vehicleA, customer1, customer2)
                .penalizesBy((4 + 5 + 3) * EuclideanDistanceCalculator.METERS_PER_DEGREE);
    }
}
