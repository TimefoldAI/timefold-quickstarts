package org.acme.facilitylocation.solver;

import static org.acme.facilitylocation.support.TestHelper.BASE_LATITUDE;
import static org.acme.facilitylocation.support.TestHelper.BASE_LONGITUDE;
import static org.acme.facilitylocation.support.TestHelper.aConsumer;
import static org.acme.facilitylocation.support.TestHelper.aFacility;
import static org.acme.facilitylocation.support.TestHelper.distanceMeters;
import static org.acme.facilitylocation.support.TestHelper.initDistanceMap;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.score.stream.test.ConstraintVerifier;

import org.acme.facilitylocation.domain.Consumer;
import org.acme.facilitylocation.domain.FacilityPlan;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class FacilityLocationConstraintProviderTest {

    @Inject
    ConstraintVerifier<FacilityLocationConstraintProvider, FacilityPlan> constraintVerifier;

    @Test
    void facilityCapacityWithinCapacity() {
        var facility = aFacility("f1").capacity(100);

        Consumer first = aConsumer("c1", facility).demand(40).build();
        Consumer second = aConsumer("c2", facility).demand(60).build();

        constraintVerifier.verifyThat(FacilityLocationConstraintProvider::facilityCapacity)
                .given(first, second)
                .penalizesBy(0);
    }

    @Test
    void facilityCapacityExceeded() {
        var facility = aFacility("f1").capacity(100);

        Consumer first = aConsumer("c1", facility).demand(60).build();
        Consumer second = aConsumer("c2", facility).demand(60).build();

        // 60 + 60 demand on a facility that can only serve 100.
        constraintVerifier.verifyThat(FacilityLocationConstraintProvider::facilityCapacity)
                .given(first, second)
                .penalizesBy(20);
    }

    @Test
    void setupCostIsPaidOncePerUsedFacility() {
        var usedFacility = aFacility("f1").setupCost(1_000);
        var otherUsedFacility = aFacility("f2").setupCost(300);

        Consumer first = aConsumer("c1", usedFacility).build();
        Consumer second = aConsumer("c2", usedFacility).build();
        Consumer third = aConsumer("c3", otherUsedFacility).build();

        // Both consumers of f1 only pay its setup cost once: 1000 + 300.
        constraintVerifier.verifyThat(FacilityLocationConstraintProvider::setupCost)
                .given(first, second, third)
                .penalizesBy(1_300);
    }

    @Test
    void distanceFromFacility() {
        var facility = aFacility("f1").longitude(BASE_LONGITUDE);

        Consumer nearby = aConsumer("c1", facility).longitude(BASE_LONGITUDE).build();
        Consumer oneDegreeAway = aConsumer("c2", facility).longitude(BASE_LONGITUDE + 1).build();
        initDistanceMap(nearby, oneDegreeAway);

        constraintVerifier.verifyThat(FacilityLocationConstraintProvider::distanceFromFacility)
                .given(nearby, oneDegreeAway)
                .penalizesBy(distanceMeters(BASE_LATITUDE, BASE_LONGITUDE, BASE_LATITUDE, BASE_LONGITUDE + 1));
    }
}
