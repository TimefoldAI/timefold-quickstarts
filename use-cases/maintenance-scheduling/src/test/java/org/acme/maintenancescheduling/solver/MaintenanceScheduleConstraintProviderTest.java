package org.acme.maintenancescheduling.solver;

import static org.acme.maintenancescheduling.support.TestHelper.aCrew;
import static org.acme.maintenancescheduling.support.TestHelper.aJob;
import static org.acme.maintenancescheduling.support.TestHelper.sequencedSet;

import java.time.LocalDate;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.score.stream.test.ConstraintVerifier;

import org.acme.maintenancescheduling.domain.MaintenanceSchedule;
import org.acme.maintenancescheduling.support.TestHelper.CrewBuilder;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class MaintenanceScheduleConstraintProviderTest {

    private static final CrewBuilder ALPHA_CREW = aCrew("1").name("Alpha crew");
    private static final CrewBuilder BETA_CREW = aCrew("2").name("Beta crew");

    // A Monday, a Tuesday and a Wednesday, so that no weekend is skipped within these tests.
    private static final LocalDate DAY_1 = LocalDate.of(2021, 2, 1);
    private static final LocalDate DAY_2 = LocalDate.of(2021, 2, 2);
    private static final LocalDate DAY_3 = LocalDate.of(2021, 2, 3);

    @Inject
    ConstraintVerifier<MaintenanceScheduleConstraintProvider, MaintenanceSchedule> constraintVerifier;

    @Test
    void crewConflict() {
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::crewConflict)
                .given(ALPHA_CREW.build(),
                        aJob("1").name("Downtown tunnel").crew(ALPHA_CREW).startDate(DAY_1).build(),
                        aJob("2").name("Uptown bridge").crew(ALPHA_CREW).startDate(DAY_1).build())
                .penalizesBy(1);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::crewConflict)
                .given(ALPHA_CREW.build(),
                        aJob("1").name("Downtown tunnel").crew(ALPHA_CREW).startDate(DAY_1).build(),
                        aJob("2").name("Uptown bridge").crew(ALPHA_CREW).startDate(DAY_2).build())
                .penalizesBy(0);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::crewConflict)
                .given(ALPHA_CREW.build(),
                        aJob("1").name("Downtown tunnel").durationInDays(3).crew(ALPHA_CREW).startDate(DAY_1).build(),
                        aJob("2").name("Uptown bridge").durationInDays(3).crew(ALPHA_CREW).startDate(DAY_2).build())
                .penalizesBy(2);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::crewConflict)
                .given(ALPHA_CREW.build(), BETA_CREW.build(),
                        aJob("1").name("Downtown tunnel").crew(ALPHA_CREW).startDate(DAY_1).build(),
                        aJob("2").name("Uptown bridge").crew(BETA_CREW).startDate(DAY_1).build())
                .penalizesBy(0);
    }

    @Test
    void minStartDate() {
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::minStartDate)
                .given(aJob("1").minStartDate(DAY_2).crew(ALPHA_CREW).startDate(DAY_2).build())
                .penalizesBy(0);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::minStartDate)
                .given(aJob("1").minStartDate(DAY_1).crew(ALPHA_CREW).startDate(DAY_3).build())
                .penalizesBy(0);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::minStartDate)
                .given(aJob("1").minStartDate(DAY_3).crew(ALPHA_CREW).startDate(DAY_1).build())
                .penalizesBy(2);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::minStartDate)
                .given(aJob("1").durationInDays(4).minStartDate(DAY_3).crew(ALPHA_CREW).startDate(DAY_1).build())
                .penalizesBy(2);
    }

    @Test
    void maxEndDate() {
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::maxEndDate)
                .given(aJob("1").maxEndDate(DAY_2).crew(ALPHA_CREW).startDate(DAY_2).build())
                .penalizesBy(1);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::maxEndDate)
                .given(aJob("1").maxEndDate(DAY_1).crew(ALPHA_CREW).startDate(DAY_3).build())
                .penalizesBy(3);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::maxEndDate)
                .given(aJob("1").maxEndDate(DAY_3).crew(ALPHA_CREW).startDate(DAY_1).build())
                .penalizesBy(0);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::maxEndDate)
                .given(aJob("1").durationInDays(4).maxEndDate(DAY_3).crew(ALPHA_CREW).startDate(DAY_1).build())
                .penalizesBy(2);
    }

    @Test
    void beforeIdealEndDate() {
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::beforeIdealEndDate)
                .given(aJob("1").durationInDays(0).idealEndDate(DAY_2).crew(ALPHA_CREW).startDate(DAY_2).build())
                .penalizesBy(0);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::beforeIdealEndDate)
                .given(aJob("1").durationInDays(0).idealEndDate(DAY_1).crew(ALPHA_CREW).startDate(DAY_3).build())
                .penalizesBy(0);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::beforeIdealEndDate)
                .given(aJob("1").durationInDays(0).idealEndDate(DAY_3).crew(ALPHA_CREW).startDate(DAY_1).build())
                .penalizesBy(2);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::beforeIdealEndDate)
                .given(aJob("1").durationInDays(1).idealEndDate(DAY_3).crew(ALPHA_CREW).startDate(DAY_1).build())
                .penalizesBy(1);
    }

    @Test
    void afterIdealEndDate() {
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::afterIdealEndDate)
                .given(aJob("1").idealEndDate(DAY_2).crew(ALPHA_CREW).startDate(DAY_2).build())
                .penalizesBy(1);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::afterIdealEndDate)
                .given(aJob("1").idealEndDate(DAY_1).crew(ALPHA_CREW).startDate(DAY_3).build())
                .penalizesBy(3);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::afterIdealEndDate)
                .given(aJob("1").idealEndDate(DAY_3).crew(ALPHA_CREW).startDate(DAY_1).build())
                .penalizesBy(0);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::afterIdealEndDate)
                .given(aJob("1").durationInDays(4).idealEndDate(DAY_3).crew(ALPHA_CREW).startDate(DAY_1).build())
                .penalizesBy(2);
    }

    @Test
    void tagConflict() {
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::tagConflict)
                .given(
                        aJob("1").name("Downtown tunnel").tags(sequencedSet("Downtown"))
                                .crew(ALPHA_CREW).startDate(DAY_1).build(),
                        aJob("2").name("Downtown bridge").tags(sequencedSet("Downtown", "Crane"))
                                .crew(ALPHA_CREW).startDate(DAY_3).build())
                .penalizesBy(0);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::tagConflict)
                .given(
                        aJob("1").name("Downtown tunnel").tags(sequencedSet("Downtown"))
                                .crew(ALPHA_CREW).startDate(DAY_1).build(),
                        aJob("2").name("Downtown bridge").tags(sequencedSet("Downtown", "Crane"))
                                .crew(ALPHA_CREW).startDate(DAY_1).build())
                .penalizesBy(1);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::tagConflict)
                .given(
                        aJob("1").name("Downtown tunnel").tags(sequencedSet("Downtown"))
                                .crew(ALPHA_CREW).startDate(DAY_1).build(),
                        aJob("2").name("Uptown bridge").tags(sequencedSet("Uptown", "Crane"))
                                .crew(ALPHA_CREW).startDate(DAY_1).build())
                .penalizesBy(0);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::tagConflict)
                .given(
                        aJob("1").name("Downtown tunnel").tags(sequencedSet("Downtown", "Crane"))
                                .crew(ALPHA_CREW).startDate(DAY_2).build(),
                        aJob("2").name("Downtown bridge").tags(sequencedSet("Downtown", "Crane"))
                                .crew(ALPHA_CREW).startDate(DAY_2).build())
                .penalizesBy(2);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::tagConflict)
                .given(
                        aJob("1").name("Downtown tunnel").durationInDays(5).tags(sequencedSet("Downtown", "Crane"))
                                .crew(ALPHA_CREW).startDate(DAY_1).build(),
                        aJob("2").name("Downtown bridge").durationInDays(3).tags(sequencedSet("Downtown", "Crane"))
                                .crew(ALPHA_CREW).startDate(DAY_2).build())
                .penalizesBy(2 * 3);
    }
}
