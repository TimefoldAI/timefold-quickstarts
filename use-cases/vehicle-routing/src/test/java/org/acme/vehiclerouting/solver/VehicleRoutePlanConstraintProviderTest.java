package org.acme.vehiclerouting.solver;

import static org.acme.vehiclerouting.support.TestHelper.aRoutePlan;
import static org.acme.vehiclerouting.support.TestHelper.aVehicle;
import static org.acme.vehiclerouting.support.TestHelper.aVisit;
import static org.acme.vehiclerouting.support.TestHelper.at;
import static org.acme.vehiclerouting.support.TestHelper.drivingTimeSeconds;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.score.stream.test.ConstraintVerifier;

import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class VehicleRoutePlanConstraintProviderTest {

    // A home location and two visit locations in the same city, so the driving times are minutes apart.
    private static final double HOME_LATITUDE = 51.00;
    private static final double HOME_LONGITUDE = 3.65;
    private static final double FIRST_LATITUDE = 51.01;
    private static final double FIRST_LONGITUDE = 3.66;
    private static final double SECOND_LATITUDE = 51.02;
    private static final double SECOND_LONGITUDE = 3.68;

    @Inject
    ConstraintVerifier<VehicleRoutePlanConstraintProvider, VehicleRoutePlan> constraintVerifier;

    @Test
    void vehicleCapacity() {
        // Within capacity: 5 + 4 of 10.
        constraintVerifier.verifyThat(VehicleRoutePlanConstraintProvider::vehicleCapacity)
                .givenSolution(aRoutePlan()
                        .route(aVehicle("1").capacity(10),
                                aVisit("1").demand(5),
                                aVisit("2").demand(4))
                        .build())
                .penalizesBy(0);
        // Exactly at capacity: 5 + 5 of 10.
        constraintVerifier.verifyThat(VehicleRoutePlanConstraintProvider::vehicleCapacity)
                .givenSolution(aRoutePlan()
                        .route(aVehicle("1").capacity(10),
                                aVisit("1").demand(5),
                                aVisit("2").demand(5))
                        .build())
                .penalizesBy(0);
        // Three over capacity: 5 + 8 of 10.
        constraintVerifier.verifyThat(VehicleRoutePlanConstraintProvider::vehicleCapacity)
                .givenSolution(aRoutePlan()
                        .route(aVehicle("1").capacity(10),
                                aVisit("1").demand(5),
                                aVisit("2").demand(8))
                        .build())
                .penalizesBy(3);
        // An unassigned visit takes up no capacity, however big its demand.
        constraintVerifier.verifyThat(VehicleRoutePlanConstraintProvider::vehicleCapacity)
                .givenSolution(aRoutePlan()
                        .route(aVehicle("1").capacity(10), aVisit("1").demand(5))
                        .unassigned(aVisit("2").demand(100))
                        .build())
                .penalizesBy(0);
    }

    @Test
    void serviceFinishedAfterMaxEndTime() {
        // The visit sits on the vehicle's home location, so it is serviced from the departure time
        // onwards and the driving time does not blur the expected delay.
        constraintVerifier.verifyThat(VehicleRoutePlanConstraintProvider::serviceFinishedAfterMaxEndTime)
                .givenSolution(aRoutePlan()
                        .route(aVehicle("1").departureTime(at(8, 0)),
                                aVisit("1").minStartTime(at(8, 0)).maxEndTime(at(9, 0)).serviceDurationMinutes(30))
                        .build())
                .penalizesBy(0);
        // Servicing finishes exactly at the maximum end time.
        constraintVerifier.verifyThat(VehicleRoutePlanConstraintProvider::serviceFinishedAfterMaxEndTime)
                .givenSolution(aRoutePlan()
                        .route(aVehicle("1").departureTime(at(8, 0)),
                                aVisit("1").minStartTime(at(8, 0)).maxEndTime(at(9, 0)).serviceDurationMinutes(60))
                        .build())
                .penalizesBy(0);
        // Servicing runs half an hour past the maximum end time.
        constraintVerifier.verifyThat(VehicleRoutePlanConstraintProvider::serviceFinishedAfterMaxEndTime)
                .givenSolution(aRoutePlan()
                        .route(aVehicle("1").departureTime(at(8, 0)),
                                aVisit("1").minStartTime(at(8, 0)).maxEndTime(at(9, 0)).serviceDurationMinutes(90))
                        .build())
                .penalizesBy(30);
        // A visit nobody services is never late.
        constraintVerifier.verifyThat(VehicleRoutePlanConstraintProvider::serviceFinishedAfterMaxEndTime)
                .givenSolution(aRoutePlan()
                        .unassigned(aVisit("1").minStartTime(at(8, 0)).maxEndTime(at(9, 0)).serviceDurationMinutes(90))
                        .build())
                .penalizesBy(0);
    }

    @Test
    void maximizeVisitsAssigned() {
        constraintVerifier.verifyThat(VehicleRoutePlanConstraintProvider::maximizeVisitsAssigned)
                .givenSolution(aRoutePlan()
                        .route(aVehicle("1"), aVisit("1").serviceDurationMinutes(30))
                        .build())
                .penalizesBy(0);
        // The penalty is the servicing the unassigned visit does not get.
        constraintVerifier.verifyThat(VehicleRoutePlanConstraintProvider::maximizeVisitsAssigned)
                .givenSolution(aRoutePlan()
                        .route(aVehicle("1"), aVisit("1").serviceDurationMinutes(30))
                        .unassigned(aVisit("2").serviceDurationMinutes(40))
                        .build())
                .penalizesBy(40);
        constraintVerifier.verifyThat(VehicleRoutePlanConstraintProvider::maximizeVisitsAssigned)
                .givenSolution(aRoutePlan()
                        .unassigned(aVisit("1").serviceDurationMinutes(10), aVisit("2").serviceDurationMinutes(20))
                        .build())
                .penalizesBy(30);
    }

    @Test
    void minimizeTravelTime() {
        // A vehicle that stays home drives nothing.
        constraintVerifier.verifyThat(VehicleRoutePlanConstraintProvider::minimizeTravelTime)
                .givenSolution(aRoutePlan()
                        .route(aVehicle("1").location(HOME_LATITUDE, HOME_LONGITUDE))
                        .unassigned(aVisit("1").location(FIRST_LATITUDE, FIRST_LONGITUDE))
                        .build())
                .penalizesBy(0);
        // One visit: out and back again.
        long homeToFirst = drivingTimeSeconds(HOME_LATITUDE, HOME_LONGITUDE, FIRST_LATITUDE, FIRST_LONGITUDE);
        constraintVerifier.verifyThat(VehicleRoutePlanConstraintProvider::minimizeTravelTime)
                .givenSolution(aRoutePlan()
                        .route(aVehicle("1").location(HOME_LATITUDE, HOME_LONGITUDE),
                                aVisit("1").location(FIRST_LATITUDE, FIRST_LONGITUDE))
                        .build())
                .penalizesBy(2 * homeToFirst);
        // Two visits: home to the first, on to the second, and back home.
        long firstToSecond = drivingTimeSeconds(FIRST_LATITUDE, FIRST_LONGITUDE, SECOND_LATITUDE, SECOND_LONGITUDE);
        long secondToHome = drivingTimeSeconds(SECOND_LATITUDE, SECOND_LONGITUDE, HOME_LATITUDE, HOME_LONGITUDE);
        constraintVerifier.verifyThat(VehicleRoutePlanConstraintProvider::minimizeTravelTime)
                .givenSolution(aRoutePlan()
                        .route(aVehicle("1").location(HOME_LATITUDE, HOME_LONGITUDE),
                                aVisit("1").location(FIRST_LATITUDE, FIRST_LONGITUDE),
                                aVisit("2").location(SECOND_LATITUDE, SECOND_LONGITUDE))
                        .build())
                .penalizesBy(homeToFirst + firstToSecond + secondToHome);
    }
}
