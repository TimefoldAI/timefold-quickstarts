package org.acme.maintenancescheduling.solver;

import java.time.LocalDate;
import java.util.Set;
import jakarta.inject.Inject;

import org.acme.maintenancescheduling.domain.Crew;
import org.acme.maintenancescheduling.domain.Job;
import org.acme.maintenancescheduling.domain.MaintenanceSchedule;
import org.junit.jupiter.api.Test;
import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
 class MaintenanceSchedulingConstraintProviderTest {

    private static final Crew ALPHA_CREW = new Crew("1", "Alpha crew");
    private static final Crew BETA_CREW = new Crew("2", "Beta crew");
    private static final LocalDate DAY_1 = LocalDate.of(2021, 2, 1);
    private static final LocalDate DAY_2 = LocalDate.of(2021, 2, 2);
    private static final LocalDate DAY_3 = LocalDate.of(2021, 2, 3);

    @Inject
    ConstraintVerifier<MaintenanceScheduleConstraintProvider, MaintenanceSchedule> constraintVerifier;

    @Test
     void crewConflict() {
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::crewConflict)
                .given(ALPHA_CREW,
                        new Job("1", "Downtown tunnel", 1, null, null, null, null, ALPHA_CREW, DAY_1),
                        new Job("2", "Uptown bridge", 1, null, null, null, null, ALPHA_CREW, DAY_1))
                .penalizesBy(1);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::crewConflict)
                .given(ALPHA_CREW,
                        new Job("1", "Downtown tunnel", 1, null, null, null, null, ALPHA_CREW, DAY_1),
                        new Job("2", "Uptown bridge", 1, null, null, null, null, ALPHA_CREW, DAY_2))
                .penalizesBy(0);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::crewConflict)
                .given(ALPHA_CREW,
                        new Job("1", "Downtown tunnel", 3, null, null, null, null, ALPHA_CREW, DAY_1),
                        new Job("2", "Uptown bridge", 3, null, null, null, null, ALPHA_CREW, DAY_2))
                .penalizesBy(2);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::crewConflict)
                .given(ALPHA_CREW, BETA_CREW,
                        new Job("1", "Downtown tunnel", 1, null, null, null, null, ALPHA_CREW, DAY_1),
                        new Job("2", "Uptown bridge", 1, null, null, null, null, BETA_CREW, DAY_1))
                .penalizesBy(0);
    }

    @Test
     void readyDate() {
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::readyDate)
                .given(new Job("1", "Downtown tunnel", 1, DAY_2, null, null, null, ALPHA_CREW, DAY_2))
                .penalizesBy(0);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::readyDate)
                .given(new Job("1", "Downtown tunnel", 1, DAY_1, null, null, null, ALPHA_CREW, DAY_3))
                .penalizesBy(0);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::readyDate)
                .given(new Job("1", "Downtown tunnel", 1, DAY_3, null, null, null, ALPHA_CREW, DAY_1))
                .penalizesBy(2);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::readyDate)
                .given(new Job("1", "Downtown tunnel", 4, DAY_3, null, null, null, ALPHA_CREW, DAY_1))
                .penalizesBy(2);
    }

    @Test
     void dueDate() {
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::dueDate)
                .given(new Job("1", "Downtown tunnel", 1, null, DAY_2, null, null, ALPHA_CREW, DAY_2))
                .penalizesBy(1);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::dueDate)
                .given(new Job("1", "Downtown tunnel", 1, null, DAY_1, null, null, ALPHA_CREW, DAY_3))
                .penalizesBy(3);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::dueDate)
                .given(new Job("1", "Downtown tunnel", 1, null, DAY_3, null, null, ALPHA_CREW, DAY_1))
                .penalizesBy(0);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::dueDate)
                .given(new Job("1", "Downtown tunnel", 4, null, DAY_3, null, null, ALPHA_CREW, DAY_1))
                .penalizesBy(2);
    }

    @Test
     void beforeIdealEndDate() {
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::beforeIdealEndDate)
                .given(new Job("1", "Downtown tunnel", 0, null, null, DAY_2, null, ALPHA_CREW, DAY_2))
                .penalizesBy(0);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::beforeIdealEndDate)
                .given(new Job("1", "Downtown tunnel", 0, null, null, DAY_1, null, ALPHA_CREW, DAY_3))
                .penalizesBy(0);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::beforeIdealEndDate)
                .given(new Job("1", "Downtown tunnel", 0, null, null, DAY_3, null, ALPHA_CREW, DAY_1))
                .penalizesBy(2);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::beforeIdealEndDate)
                .given(new Job("1", "Downtown tunnel", 0, null, null, DAY_3, null, ALPHA_CREW, DAY_1))
                .penalizesBy(2);
    }

    @Test
     void afterIdealEndDate() {
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::afterIdealEndDate)
                .given(new Job("1", "Downtown tunnel", 1, null, null, DAY_2, null, ALPHA_CREW, DAY_2))
                .penalizesBy(1);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::afterIdealEndDate)
                .given(new Job("1", "Downtown tunnel", 1, null, null, DAY_1, null, ALPHA_CREW, DAY_3))
                .penalizesBy(3);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::afterIdealEndDate)
                .given(new Job("1", "Downtown tunnel", 1, null, null, DAY_3, null, ALPHA_CREW, DAY_1))
                .penalizesBy(0);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::afterIdealEndDate)
                .given(new Job("1", "Downtown tunnel", 4, null, null, DAY_3, null, ALPHA_CREW, DAY_1))
                .penalizesBy(2);
    }

    @Test
     void tagConflict() {
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::tagConflict)
                .given(
                        new Job("1", "Downtown tunnel", 1, null, null, null, Set.of("Downtown"), ALPHA_CREW, DAY_1),
                        new Job("2", "Downtown bridge", 1, null, null, null, Set.of("Downtown", "Crane"), ALPHA_CREW, DAY_3))
                .penalizesBy(0);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::tagConflict)
                .given(
                        new Job("1", "Downtown tunnel", 1, null, null, null, Set.of("Downtown"), ALPHA_CREW, DAY_1),
                        new Job("2", "Downtown bridge", 1, null, null, null, Set.of("Downtown", "Crane"), ALPHA_CREW, DAY_1))
                .penalizesBy(1);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::tagConflict)
                .given(
                        new Job("1", "Downtown tunnel", 1, null, null, null, Set.of("Downtown"), ALPHA_CREW, DAY_1),
                        new Job("2", "Uptown bridge", 1, null, null, null, Set.of("Uptown", "Crane"), ALPHA_CREW, DAY_1))
                .penalizesBy(0);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::tagConflict)
                .given(
                        new Job("1", "Downtown tunnel", 1, null, null, null, Set.of("Downtown", "Crane"), ALPHA_CREW, DAY_2),
                        new Job("2", "Downtown bridge", 1, null, null, null, Set.of("Downtown", "Crane"), ALPHA_CREW, DAY_2))
                .penalizesBy(2);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::tagConflict)
                .given(
                        new Job("1", "Downtown tunnel", 5, null, null, null, Set.of("Downtown", "Crane"), ALPHA_CREW, DAY_1),
                        new Job("2", "Downtown bridge", 3, null, null, null, Set.of("Downtown", "Crane"), ALPHA_CREW, DAY_2))
                .penalizesBy(2 * 3);
    }

}
