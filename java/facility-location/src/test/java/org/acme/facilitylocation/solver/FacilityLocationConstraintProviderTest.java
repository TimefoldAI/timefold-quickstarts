package org.acme.facilitylocation.solver;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.score.stream.test.ConstraintVerifier;

import org.acme.facilitylocation.domain.Consumer;
import org.acme.facilitylocation.domain.Facility;
import org.acme.facilitylocation.domain.FacilityLocationProblem;
import org.acme.facilitylocation.domain.Location;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class FacilityLocationConstraintProviderTest {

    @Inject
    ConstraintVerifier<FacilityLocationConstraintProvider, FacilityLocationProblem> constraintVerifier;

    @Test
    void penalizesCapacityExceededByASingleConsumer() {
        Location location = new Location(1, 1);
        Facility facility = new Facility("1", location, 0, 20);
        Consumer consumer = new Consumer("1", location, 100);
        consumer.setFacility(facility);

        constraintVerifier.verifyThat(FacilityLocationConstraintProvider::facilityCapacity)
                .given(consumer, facility)
                .penalizesBy(80);
    }

    @Test
    void noPenaltyWhenDemandLessThanCapacity() {
        Location location = new Location(1, 1);
        Facility facility = new Facility("1", location, 0, 100);
        Consumer consumer1 = new Consumer("1", location, 1);
        Consumer consumer2 = new Consumer("2", location, 2);
        Consumer consumer3 = new Consumer("3", location, 3);
        consumer1.setFacility(facility);
        consumer2.setFacility(facility);
        consumer3.setFacility(facility);

        constraintVerifier.verifyThat(FacilityLocationConstraintProvider::facilityCapacity)
                .given(consumer1, consumer2, consumer3, facility)
                .penalizesBy(0);
    }

    @Test
    void noPenaltyWhenConsumerNotAssigned() {
        Location location = new Location(1, 1);
        Facility facility = new Facility("1", location, 0, 1);
        Consumer consumer = new Consumer("1", location, 100);

        constraintVerifier.verifyThat(FacilityLocationConstraintProvider::facilityCapacity)
                .given(consumer, facility)
                .penalizesBy(0);
    }

    @Test
    void shouldPenalizeSetupCost() {
        long setupCost = 123;
        Location location = new Location(1, 1);
        Facility facility = new Facility("1", location, setupCost, 100);
        Consumer consumer = new Consumer("1", location, 1);
        consumer.setFacility(facility);

        constraintVerifier.verifyThat(FacilityLocationConstraintProvider::setupCost)
                .given(facility, consumer)
                .penalizesBy(setupCost);
    }

    @Test
    void shouldPenalizeDistanceToFacility() {
        Location facilityLocation = new Location(0, 0);
        Location consumer1Location = new Location(10, 0);
        Location consumer2Location = new Location(0, 20);

        Facility facility = new Facility("1", facilityLocation, 0, 100);
        Consumer consumer1 = new Consumer("1", consumer1Location, 1);
        Consumer consumer2 = new Consumer("2", consumer2Location, 1);

        consumer1.setFacility(facility);
        consumer2.setFacility(facility);

        constraintVerifier.verifyThat(FacilityLocationConstraintProvider::distanceFromFacility)
                .given(facility, consumer1, consumer2)
                .penalizesBy((int) (30 * Location.METERS_PER_DEGREE));
    }
}
