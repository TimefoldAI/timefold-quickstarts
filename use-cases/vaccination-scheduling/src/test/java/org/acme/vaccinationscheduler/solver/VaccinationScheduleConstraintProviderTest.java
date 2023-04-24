package org.acme.vaccinationscheduler.solver;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

import jakarta.inject.Inject;

import org.acme.vaccinationscheduler.domain.Location;
import org.acme.vaccinationscheduler.domain.VaccinationCenter;
import org.acme.vaccinationscheduler.domain.VaccineType;
import org.acme.vaccinationscheduler.domain.solver.PersonAssignment;
import org.acme.vaccinationscheduler.domain.solver.VaccinationSlot;
import org.acme.vaccinationscheduler.domain.solver.VaccinationSolution;
import org.junit.jupiter.api.Test;
import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class VaccinationScheduleConstraintProviderTest {

    private static final VaccineType PFIZER = new VaccineType("Pfizer");
    private static final VaccineType MODERNA = new VaccineType("Moderna");
    private static final VaccineType ASTRAZENECA = new VaccineType("AstraZeneca");
    private static final VaccineType AGE_40_TO_55_VACCINE = new VaccineType("40 to 55", 40, 55);

    private static final VaccinationCenter VACCINATION_CENTER_1 = new VaccinationCenter("1", "Downtown", new Location(0, 0));
    private static final VaccinationCenter VACCINATION_CENTER_2 = new VaccinationCenter("2", "Uptown", new Location(10, 10));
    private static final LocalDate MONDAY = LocalDate.of(2021, 2, 1);
    private static final LocalDate TUESDAY = LocalDate.of(2021, 2, 2);
    private static final LocalDate WEDNESDAY = LocalDate.of(2021, 2, 3);
    private static final LocalDate THURSDAY = LocalDate.of(2021, 2, 4);
    private static final LocalDate FRIDAY = LocalDate.of(2021, 2, 5);
    private static final LocalDateTime MONDAY_0900 = LocalDateTime.of(MONDAY, LocalTime.of(9, 0));
    private static final LocalDateTime TUESDAY_0900 = LocalDateTime.of(TUESDAY, LocalTime.of(9, 0));
    private static final LocalDateTime WEDNESDAY_0900 = LocalDateTime.of(WEDNESDAY, LocalTime.of(9, 0));
    private static final LocalDateTime THURSDAY_0900 = LocalDateTime.of(THURSDAY, LocalTime.of(9, 0));
    private static final LocalDateTime FRIDAY_0900 = LocalDateTime.of(FRIDAY, LocalTime.of(9, 0));
    private static final VaccinationSlot PFIZER_MONDAY_SLOT =
            new VaccinationSlot(1L, VACCINATION_CENTER_1, MONDAY_0900, PFIZER, 12);
    private static final VaccinationSlot PFIZER_TUESDAY_SLOT =
            new VaccinationSlot(2L, VACCINATION_CENTER_1, TUESDAY_0900, PFIZER, 12);
    private static final VaccinationSlot PFIZER_WEDNESDAY_SLOT =
            new VaccinationSlot(3L, VACCINATION_CENTER_1, WEDNESDAY_0900, PFIZER, 12);
    private static final VaccinationSlot PFIZER_THURSDAY_SLOT =
            new VaccinationSlot(4L, VACCINATION_CENTER_1, THURSDAY_0900, PFIZER, 12);
    private static final VaccinationSlot PFIZER_FRIDAY_SLOT =
            new VaccinationSlot(5L, VACCINATION_CENTER_1, FRIDAY_0900, PFIZER, 12);
    private static final VaccinationSlot MODERNA_SLOT = new VaccinationSlot(11L, VACCINATION_CENTER_1, MONDAY_0900, MODERNA, 12);
    private static final VaccinationSlot AZ_SLOT = new VaccinationSlot(12L, VACCINATION_CENTER_1, MONDAY_0900, ASTRAZENECA, 12);
    private static final VaccinationSlot AGE_40_TO_55_VACCINE_SLOT = new VaccinationSlot(12L, VACCINATION_CENTER_1, MONDAY_0900, AGE_40_TO_55_VACCINE, 12);
    private static final VaccinationSlot VACCINATION_CENTER_2_SLOT = new VaccinationSlot(13L, VACCINATION_CENTER_2, MONDAY_0900, PFIZER, 12);

    @Inject
    ConstraintVerifier<VaccinationScheduleConstraintProvider, VaccinationSolution> constraintVerifier;

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    @Test
    void vaccinationSlotCapacity() {
        VaccinationCenter vaccinationCenter = new VaccinationCenter("1", "Uptown", new Location(0, 0));
        VaccinationSlot vaccinationSlot = new VaccinationSlot(null, vaccinationCenter, null, null, 3);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::vaccinationSlotCapacity)
                .given(
                        vaccinationSlot, PFIZER_MONDAY_SLOT,
                        new PersonAssignment("1", "Ann", null, null, null, 0, vaccinationSlot),
                        new PersonAssignment("2", "Beth", null, null, null, 0, vaccinationSlot),
                        new PersonAssignment("3", "Carl", null, null, null, 0, vaccinationSlot),
                        new PersonAssignment("4", "Dan", null, null, null, 0, vaccinationSlot),
                        new PersonAssignment("5", "Ed", null, null, null, 0, vaccinationSlot),
                        new PersonAssignment("6", "Flo", null, null, null, 0, PFIZER_MONDAY_SLOT))
                .penalizesBy(2);
    }

    @Test
    void requiredVaccineType() {
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::requiredVaccineType)
                .given(
                        new PersonAssignment("1", "Ann", null, null, null, 0, PFIZER_MONDAY_SLOT))
                .penalizesBy(0);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::requiredVaccineType)
                .given(
                        new PersonAssignment("3", "Carl", null, null, null, 0,
                                2, PFIZER, null, null, null, null, null, null, MODERNA_SLOT))
                .penalizesBy(1);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::requiredVaccineType)
                .given(
                        new PersonAssignment("3", "Carl", null, null, null, 0,
                                2, PFIZER, null, null, null, null, null, null, PFIZER_MONDAY_SLOT))
                .penalizesBy(0);
    }

    @Test
    void requiredVaccinationCenter() {
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::requiredVaccinationCenter)
                .given(
                        new PersonAssignment("1", "Ann", null, null, null, 0, PFIZER_MONDAY_SLOT))
                .penalizesBy(0);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::requiredVaccinationCenter)
                .given(
                        new PersonAssignment("3", "Carl", null, null, null, 0,
                                2, null, null, VACCINATION_CENTER_2, null, null, null, null, MODERNA_SLOT))
                .penalizesBy(1);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::requiredVaccinationCenter)
                .given(
                        new PersonAssignment("3", "Carl", null, null, null, 0,
                                2, null, null, VACCINATION_CENTER_1, null, null, null, null, PFIZER_MONDAY_SLOT))
                .penalizesBy(0);
    }

    @Test
    void minimumAgeVaccineType() {
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::minimumAgeVaccineType)
                .given(
                        new PersonAssignment("1", "Ann", null, null, LocalDate.of(1990, 1, 1), 0L, PFIZER_MONDAY_SLOT))
                .penalizesBy(0);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::minimumAgeVaccineType)
                .given(
                        new PersonAssignment("1", "Ann", null, null, LocalDate.of(1990, 1, 1), 0L, AGE_40_TO_55_VACCINE_SLOT))
                .penalizesBy(40 - 31);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::minimumAgeVaccineType)
                .given(
                        new PersonAssignment("2", "Beth", null, null, LocalDate.of(1980, 1, 1), 0L, AGE_40_TO_55_VACCINE_SLOT))
                .penalizesBy(0);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::minimumAgeVaccineType)
                .given(
                        new PersonAssignment("3", "Carl", null, null, LocalDate.of(1980, 1, 1), 0L, 2, AGE_40_TO_55_VACCINE, null, null, null, null, null, null, AGE_40_TO_55_VACCINE_SLOT))
                .penalizesBy(0);
    }

    @Test
    void maximumAgeVaccineType() {
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::maximumAgeVaccineType)
                .given(
                        new PersonAssignment("1", "Ann", null, null, LocalDate.of(1950, 1, 1), 0L, PFIZER_MONDAY_SLOT))
                .penalizesBy(0);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::maximumAgeVaccineType)
                .given(
                        new PersonAssignment("1", "Ann", null, null, LocalDate.of(1950, 1, 1), 0L, AGE_40_TO_55_VACCINE_SLOT))
                .penalizesBy(71 - 55);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::maximumAgeVaccineType)
                .given(
                        new PersonAssignment("2", "Beth", null, null, LocalDate.of(1980, 1, 1), 0L, AGE_40_TO_55_VACCINE_SLOT))
                .penalizesBy(0);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::maximumAgeVaccineType)
                .given(
                        new PersonAssignment("3", "Carl", null, null, LocalDate.of(1950, 1, 1), 0L, 2, AGE_40_TO_55_VACCINE, null, null, null, null, null, null, AGE_40_TO_55_VACCINE_SLOT))
                .penalizesBy(0);
    }

    @Test
    void readyDate() {
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::readyDate)
                .given(
                        new PersonAssignment("1", "Ann", null, null, null, 0, PFIZER_MONDAY_SLOT))
                .penalizesBy(0);

        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::readyDate)
                .given(
                        new PersonAssignment("3", "Carl", null, null, null, 0, 2, PFIZER, null, null, null, WEDNESDAY, null, null, PFIZER_MONDAY_SLOT))
                .penalizesBy(2);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::readyDate)
                .given(
                        new PersonAssignment("3", "Carl", null, null, null, 0, 2, PFIZER, null, null, null, WEDNESDAY, null, null, PFIZER_TUESDAY_SLOT))
                .penalizesBy(1);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::readyDate)
                .given(
                        new PersonAssignment("3", "Carl", null, null, null, 0, 2, PFIZER, null, null, null, WEDNESDAY, null, null, PFIZER_WEDNESDAY_SLOT))
                .penalizesBy(0);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::readyDate)
                .given(
                        new PersonAssignment("3", "Carl", null, null, null, 0, 2, PFIZER, null, null, null, WEDNESDAY, null, null, PFIZER_THURSDAY_SLOT))
                .penalizesBy(0);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::readyDate)
                .given(
                        new PersonAssignment("3", "Carl", null, null, null, 0, 2, PFIZER, null, null, null, WEDNESDAY, null, null, PFIZER_FRIDAY_SLOT))
                .penalizesBy(0);
    }

    @Test
    void dueDate() {
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::dueDate)
                .given(
                        new PersonAssignment("1", "Ann", null, null, null, 0, PFIZER_FRIDAY_SLOT))
                .penalizesBy(0);

        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::dueDate)
                .given(
                        new PersonAssignment("3", "Carl", null, null, null, 0, 2, PFIZER, null, null, null, null, null, WEDNESDAY, PFIZER_MONDAY_SLOT))
                .penalizesBy(0);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::dueDate)
                .given(
                        new PersonAssignment("3", "Carl", null, null, null, 0, 2, PFIZER, null, null, null, null, null, WEDNESDAY, PFIZER_TUESDAY_SLOT))
                .penalizesBy(0);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::dueDate)
                .given(
                        new PersonAssignment("3", "Carl", null, null, null, 0, 2, PFIZER, null, null, null, null, null, WEDNESDAY, PFIZER_WEDNESDAY_SLOT))
                .penalizesBy(0);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::dueDate)
                .given(
                        new PersonAssignment("3", "Carl", null, null, null, 0, 2, PFIZER, null, null, null, null, null, WEDNESDAY, PFIZER_THURSDAY_SLOT))
                .penalizesBy(1);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::dueDate)
                .given(
                        new PersonAssignment("3", "Carl", null, null, null, 0, 2, PFIZER, null, null, null, null, null, WEDNESDAY, PFIZER_FRIDAY_SLOT))
                .penalizesBy(2);
    }

    // ************************************************************************
    // Medium constraints
    // ************************************************************************

    @Test
    void scheduleSecondOrLaterDosePeople() {
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::scheduleSecondOrLaterDosePeople)
                .given(
                        new PersonAssignment("1", "Ann", null, null, null, 0, null),
                        new PersonAssignment("3", "Carl", null, null, null, 0, 2, PFIZER, null, null, null, null, null, null, PFIZER_MONDAY_SLOT))
                .penalizesBy(0);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::scheduleSecondOrLaterDosePeople)
                .given(
                        new PersonAssignment("1", "Ann", null, null, null, 0, PFIZER_MONDAY_SLOT),
                        new PersonAssignment("3", "Carl", null, null, null, 0, 2, PFIZER, null, null, null, null, null, null, null))
                .penalizesBy(1);
    }

    @Test
    void scheduleHigherPriorityRatingPeople() {
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::scheduleHigherPriorityRatingPeople)
                .given(
                        new PersonAssignment("1", "Ann", null, null, LocalDate.of(1950, 1, 1), 71, AZ_SLOT),
                        new PersonAssignment("2", "Beth", null, null, LocalDate.of(1980, 1, 1), 41, null),
                        new PersonAssignment("3", "Carl", null, null, LocalDate.of(1970, 1, 1), 51, null))
                .penalizesBy(51 + 41);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::scheduleHigherPriorityRatingPeople)
                .given(
                        new PersonAssignment("1", "Ann", null, null, LocalDate.of(1950, 1, 1), 71, AZ_SLOT),
                        new PersonAssignment("2", "Beth", null, null, LocalDate.of(1980, 1, 1), 41, null),
                        new PersonAssignment("3", "Carl", null, null, LocalDate.of(1970, 1, 1), 51, AZ_SLOT))
                .penalizesBy(41);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::scheduleHigherPriorityRatingPeople)
                .given(
                        new PersonAssignment("1", "Ann", null, null, LocalDate.of(1950, 1, 1), 71, null),
                        new PersonAssignment("2", "Dr. Beth", null, null, LocalDate.of(1980, 1, 1), 1041, null),
                        new PersonAssignment("3", "Dr. Carl", null, null, LocalDate.of(1970, 1, 1), 1051, AZ_SLOT))
                .penalizesBy(71 + 1041);
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    @Test
    void preferredVaccineType() {
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::preferredVaccineType)
                .given(
                        new PersonAssignment("1", "Ann", null, null, null, 0, PFIZER_MONDAY_SLOT))
                .penalizesBy(0);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::preferredVaccineType)
                .given(
                        new PersonAssignment("3", "Carl", null, null, null, 0,
                                2, null, PFIZER, null, null, null, null, null, MODERNA_SLOT))
                .penalizesBy(1);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::preferredVaccineType)
                .given(
                        new PersonAssignment("3", "Carl", null, null, null, 0,
                                2, null, PFIZER, null, null, null, null, null, PFIZER_MONDAY_SLOT))
                .penalizesBy(0);
    }

    @Test
    void preferredVaccinationCenter() {
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::preferredVaccinationCenter)
                .given(
                        new PersonAssignment("1", "Ann", null, null, null, 0, PFIZER_MONDAY_SLOT))
                .penalizesBy(0);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::preferredVaccinationCenter)
                .given(
                        new PersonAssignment("3", "Carl", null, null, null, 0,
                                2, null, null, null, VACCINATION_CENTER_2, null, null, null, MODERNA_SLOT))
                .penalizesBy(1);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::preferredVaccinationCenter)
                .given(
                        new PersonAssignment("3", "Carl", null, null, null, 0,
                                2, null, null, null, VACCINATION_CENTER_1, null, null, null, PFIZER_MONDAY_SLOT))
                .penalizesBy(0);
    }

    @Test
    void regretDistance() {
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::regretDistance)
                .given(
                        VACCINATION_CENTER_1, VACCINATION_CENTER_2,
                        new PersonAssignment("1", "Ann", new Location(1, 0), Map.of(
                                VACCINATION_CENTER_1, 100L, VACCINATION_CENTER_2, 120L
                        ), null, 0, AZ_SLOT))
                .penalizesBy(0L);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::regretDistance)
                .given(
                        VACCINATION_CENTER_1, VACCINATION_CENTER_2,
                        new PersonAssignment("1", "Ann", new Location(1, 0), Map.of(
                                VACCINATION_CENTER_1, 100L, VACCINATION_CENTER_2, 120L
                        ), null, 0, VACCINATION_CENTER_2_SLOT))
                .penalizesBy(20L * 20L);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::regretDistance)
                .given(
                        VACCINATION_CENTER_1, VACCINATION_CENTER_2,
                        new PersonAssignment("1", "Ann", new Location(1, 0), Map.of(
                                VACCINATION_CENTER_1, 200L, VACCINATION_CENTER_2, 120L
                        ), null, 0, VACCINATION_CENTER_2_SLOT))
                .penalizesBy(0L);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::regretDistance)
                .given(
                        VACCINATION_CENTER_1, VACCINATION_CENTER_2,
                        new PersonAssignment("1", "Ann", new Location(1, 0), Map.of(
                                VACCINATION_CENTER_1, 120L, VACCINATION_CENTER_2, 100L
                        ), null, 0, AZ_SLOT),
                        new PersonAssignment("2", "Beth", new Location(2, 0), Map.of(
                                VACCINATION_CENTER_1, 100L, VACCINATION_CENTER_2, 103L
                        ), null, 0, VACCINATION_CENTER_2_SLOT))
                .penalizesBy((20L * 20L) + (3L * 3L));

        // If the requiredVaccinationCenter is non-null, that has 0 regret distance and others have non-regret distance
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::regretDistance)
                .given(
                        VACCINATION_CENTER_1, VACCINATION_CENTER_2,
                        new PersonAssignment("1", "Ann", new Location(1, 0), Map.of(
                                VACCINATION_CENTER_1, 100L, VACCINATION_CENTER_2, 120L
                        ), null, 0, 1, null, null, VACCINATION_CENTER_2, null, null, null, null, VACCINATION_CENTER_2_SLOT))
                .penalizesBy(0L);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::regretDistance)
                .given(
                        VACCINATION_CENTER_1, VACCINATION_CENTER_2,
                        new PersonAssignment("1", "Ann", new Location(1, 0), Map.of(
                                VACCINATION_CENTER_1, 100L, VACCINATION_CENTER_2, 120L
                        ), null, 0, 1, null, null, VACCINATION_CENTER_2, null, null, null, null, AZ_SLOT))
                .penalizesBy(0L);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::regretDistance)
                .given(
                        VACCINATION_CENTER_1, VACCINATION_CENTER_2,
                        new PersonAssignment("1", "Ann", new Location(1, 0), Map.of(
                                VACCINATION_CENTER_1, 130L, VACCINATION_CENTER_2, 100L
                        ), null, 0, 1, null, null, VACCINATION_CENTER_2, null, null, null, null, AZ_SLOT))
                .penalizesBy(30L * 30L);
        // If the preferredVaccinationCenter is non-null, that has 0 regret distance and others have non-regret distance
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::regretDistance)
                .given(
                        VACCINATION_CENTER_1, VACCINATION_CENTER_2,
                        new PersonAssignment("1", "Ann", new Location(1, 0), Map.of(
                                VACCINATION_CENTER_1, 100L, VACCINATION_CENTER_2, 120L
                        ), null, 0, 1, null, null, null, VACCINATION_CENTER_2, null, null, null, VACCINATION_CENTER_2_SLOT))
                .penalizesBy(0L);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::regretDistance)
                .given(
                        VACCINATION_CENTER_1, VACCINATION_CENTER_2,
                        new PersonAssignment("1", "Ann", new Location(1, 0), Map.of(
                                VACCINATION_CENTER_1, 100L, VACCINATION_CENTER_2, 120L
                        ), null, 0, 1, null, null, null, VACCINATION_CENTER_2, null, null, null, AZ_SLOT))
                .penalizesBy(0L);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::regretDistance)
                .given(
                        VACCINATION_CENTER_1, VACCINATION_CENTER_2,
                        new PersonAssignment("1", "Ann", new Location(1, 0), Map.of(
                                VACCINATION_CENTER_1, 130L, VACCINATION_CENTER_2, 100L
                        ), null, 0, 1, null, null, null, VACCINATION_CENTER_2, null, null, null, AZ_SLOT))
                .penalizesBy(30L * 30L);
    }

    @Test
    void idealDate() {
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::idealDate)
                .given(
                        new PersonAssignment("1", "Ann", null, null, null, 0, PFIZER_MONDAY_SLOT))
                .penalizesBy(0);

        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::idealDate)
                .given(
                        new PersonAssignment("3", "Carl", null, null, null, 0, 2, PFIZER, null, null, null, null, THURSDAY, null, PFIZER_MONDAY_SLOT))
                .penalizesBy(3 * 3);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::idealDate)
                .given(
                        new PersonAssignment("3", "Carl", null, null, null, 0, 2, PFIZER, null, null, null, null, THURSDAY, null, PFIZER_TUESDAY_SLOT))
                .penalizesBy(2 * 2);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::idealDate)
                .given(
                        new PersonAssignment("3", "Carl", null, null, null, 0, 2, PFIZER, null, null, null, null, THURSDAY, null, PFIZER_WEDNESDAY_SLOT))
                .penalizesBy(1);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::idealDate)
                .given(
                        new PersonAssignment("3", "Carl", null, null, null, 0, 2, PFIZER, null, null, null, null, THURSDAY, null, PFIZER_THURSDAY_SLOT))
                .penalizesBy(0);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::idealDate)
                .given(
                        new PersonAssignment("3", "Carl", null, null, null, 0, 2, PFIZER, null, null, null, null, THURSDAY, null, PFIZER_FRIDAY_SLOT))
                .penalizesBy(1);
    }

    // TODO implement once it penalizes based on planning window start
//    @Test
//    void higherPriorityRatingEarlier() {
//        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::higherPriorityRatingEarlier)
//                .given(
//                        new PersonAssignment("1", "Ann", null, null, LocalDate.of(1950, 1, 1), 71, AZ_SLOT),
//                        new PersonAssignment("2", "Beth", null, null, LocalDate.of(1980, 1, 1), 41, null),
//                        new PersonAssignment("3", "Carl", null, null, LocalDate.of(1970, 1, 1), 51, null))
//                .penalizesBy(51 + 41);
//        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::higherPriorityRatingEarlier)
//                .given(
//                        new PersonAssignment("1", "Ann", null, null, LocalDate.of(1950, 1, 1), 71, AZ_SLOT),
//                        new PersonAssignment("2", "Beth", null, null, LocalDate.of(1980, 1, 1), 41, null),
//                        new PersonAssignment("3", "Carl", null, null, LocalDate.of(1970, 1, 1), 51, AZ_SLOT))
//                .penalizesBy(41);
//        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::higherPriorityRatingEarlier)
//                .given(
//                        new PersonAssignment("1", "Ann", null, null, LocalDate.of(1950, 1, 1), 71, null),
//                        new PersonAssignment("2", "Dr. Beth", null, null, LocalDate.of(1980, 1, 1), 1041, null),
//                        new PersonAssignment("3", "Dr. Carl", null, null, LocalDate.of(1970, 1, 1), 1051, AZ_SLOT))
//                .penalizesBy(71 + 1041);
//    }

}
