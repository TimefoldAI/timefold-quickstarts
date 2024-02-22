package org.acme.vaccinationscheduler.solver;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MINUTES;

import java.time.LocalDateTime;

import org.acme.vaccinationscheduler.domain.solver.PersonAssignment;
import ai.timefold.solver.core.api.score.buildin.bendablelong.BendableLongScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

public class VaccinationScheduleConstraintProvider implements ConstraintProvider {

    public static final int HARD_LEVELS_SIZE = 1;
    public static final int SOFT_LEVELS_SIZE = 5;

    private static final LocalDateTime COVID_EPOCH = LocalDateTime.of(2021, 1, 1, 0, 0);

    private BendableLongScore ofHard(long hardScore) {
        return BendableLongScore.ofHard(HARD_LEVELS_SIZE, SOFT_LEVELS_SIZE, 0, hardScore);
    }

    private BendableLongScore ofSoft(int softLevel, long softScore) {
        return BendableLongScore.ofSoft(HARD_LEVELS_SIZE, SOFT_LEVELS_SIZE, softLevel, softScore);
    }

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
                // Hard constraints
                vaccinationSlotCapacity(constraintFactory),
                requiredVaccineType(constraintFactory),
                requiredVaccinationCenter(constraintFactory),
                minimumAgeVaccineType(constraintFactory),
                maximumAgeVaccineType(constraintFactory),
                readyDate(constraintFactory),
                dueDate(constraintFactory),
                // TODO restrict maximum distance
                // Medium constraints
                scheduleSecondOrLaterDosePeople(constraintFactory),
                scheduleHigherPriorityRatingPeople(constraintFactory),
                // Soft constraints
                preferredVaccineType(constraintFactory),
                preferredVaccinationCenter(constraintFactory),
                regretDistance(constraintFactory),
                idealDate(constraintFactory),
                higherPriorityRatingEarlier(constraintFactory)
        };
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    Constraint vaccinationSlotCapacity(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(PersonAssignment.class)
                .groupBy(PersonAssignment::getVaccinationSlot, ConstraintCollectors.count())
                .filter((vaccinationSlot, personCount) -> personCount > vaccinationSlot.getCapacity())
                .penalizeLong(ofHard(1_000),
                        (vaccinationSlot, personCount) -> personCount - vaccinationSlot.getCapacity())
                .asConstraint("Vaccination slot capacity");
    }

    Constraint requiredVaccineType(ConstraintFactory constraintFactory) {
        // Typical usage: if a person is coming for their 2nd dose, use the same vaccine type as their 1st dose.
        return constraintFactory
                .forEach(PersonAssignment.class)
                .filter((personAssignment -> personAssignment.getRequiredVaccineType() != null
                        && personAssignment.getVaccinationSlot().getVaccineType() != personAssignment.getRequiredVaccineType()))
                .penalize(ofHard(10_000_000))
                .asConstraint("Required vaccine type");
    }

    Constraint requiredVaccinationCenter(ConstraintFactory constraintFactory) {
        // Typical usage: if a person is coming for their 2nd dose, enforce the same vaccination center as their 1st dose.
        return constraintFactory
                .forEach(PersonAssignment.class)
                .filter((personAssignment -> personAssignment.getRequiredVaccinationCenter() != null
                        && personAssignment.getVaccinationSlot().getVaccinationCenter() != personAssignment.getRequiredVaccinationCenter()))
                .penalize(ofHard(1_000_000))
                .asConstraint("Required vaccination center");
    }

    Constraint minimumAgeVaccineType(ConstraintFactory constraintFactory) {
        // Don't inject too young people with a vaccine that has minimum age
        return constraintFactory
                .forEach(PersonAssignment.class)
                .filter(personAssignment -> personAssignment.getVaccinationSlot().getVaccineType().getMaximumAge() != null
                        && personAssignment.getAgeOnVaccinationDate()
                        < personAssignment.getVaccinationSlot().getVaccineType().getMinimumAge()
                        && personAssignment.getRequiredVaccineType() == null)
                .penalizeLong(ofHard(1),
                        personAssignment -> personAssignment.getVaccinationSlot().getVaccineType().getMinimumAge()
                                - personAssignment.getAgeOnVaccinationDate())
                .asConstraint("Minimum age of vaccination type");
    }

    Constraint maximumAgeVaccineType(ConstraintFactory constraintFactory) {
        // Don't inject with a vaccine that has maximum age people over that age
        return constraintFactory
                .forEach(PersonAssignment.class)
                .filter(personAssignment -> personAssignment.getVaccinationSlot().getVaccineType().getMaximumAge() != null
                        && personAssignment.getAgeOnVaccinationDate()
                        > personAssignment.getVaccinationSlot().getVaccineType().getMaximumAge()
                        // If the 1th dose was a max 55 year vaccine, then it's ok to inject someone who only turned 56 last week with it
                        && personAssignment.getRequiredVaccineType() == null)
                .penalizeLong(ofHard(1),
                        personAssignment -> personAssignment.getAgeOnVaccinationDate()
                                - personAssignment.getVaccinationSlot().getVaccineType().getMaximumAge())
                .asConstraint("Maximum age of vaccination type");
    }

    Constraint readyDate(ConstraintFactory constraintFactory) {
        // Typical usage 1: If a person is coming for their 2nd dose, don't inject it before the ready day.
        // For example, Pfizer is ready to injected 19 days after the first dose. Moderna after 26 days.
        // Typical usage 2: If a person wants to reschedule an invited/accepted appointment,
        // set the readyDate one day after the appointment date to avoid inviting the same day (especially for multiple reschedules)
        // and also prohibit gamification (to get an earlier appointment).
        return constraintFactory
                .forEach(PersonAssignment.class)
                .filter(personAssignment -> personAssignment.getReadyDate() != null
                        && personAssignment.getVaccinationSlot().getDate().compareTo(personAssignment.getReadyDate()) < 0)
                .penalizeLong(ofHard(1),
                        personAssignment -> DAYS.between(personAssignment.getVaccinationSlot().getDate(),
                                personAssignment.getReadyDate()))
                .asConstraint("Ready date");
    }

    Constraint dueDate(ConstraintFactory constraintFactory) {
        // Typical usage 1: If a person is coming for their 2nd dose, don't inject it after the due day.
        // For example, Pfizer is due to be injected 3 months after the first dose.
        return constraintFactory
                .forEach(PersonAssignment.class)
                .filter(personAssignment -> personAssignment.getDueDate() != null
                        && personAssignment.getVaccinationSlot().getDate().compareTo(personAssignment.getDueDate()) > 0)
                .penalizeLong(ofHard(1),
                        personAssignment -> DAYS.between(personAssignment.getDueDate(),
                                personAssignment.getVaccinationSlot().getDate()))
                .asConstraint("Due date");
    }

    // ************************************************************************
    // Medium constraints
    // ************************************************************************

    Constraint scheduleSecondOrLaterDosePeople(ConstraintFactory constraintFactory) {
        // If a person is coming for their 2nd dose, assign them to an appointment,
        // even before 1st dose healthcare workers and older people (although 2nd dosers will normally be that too).
        // This is to avoid a snowball effect on the backlog.
        return constraintFactory
                .forEachIncludingUnassigned(PersonAssignment.class)
                // TODO filter for ideal date is earlier or equal to planning window last day
                .filter(personAssignment -> personAssignment.getDoseNumber() > 1 && personAssignment.getVaccinationSlot() == null)
                .penalizeLong(ofSoft(0, 1),
                        personAssignment -> personAssignment.getDoseNumber() - 1)
                .asConstraint("Schedule second (or later) dose people");
    }

    Constraint scheduleHigherPriorityRatingPeople(ConstraintFactory constraintFactory) {
        // Assign healthcare workers and older people for an appointment.
        // Priority rating is a person's age augmented by a few hundred points if they're a healthcare worker.
        return constraintFactory
                .forEachIncludingUnassigned(PersonAssignment.class)
                .filter(personAssignment -> personAssignment.getVaccinationSlot() == null)
                // This is softer than scheduleSecondOrLaterDosePeople()
                // to avoid creating a backlog of 2nd dose persons, that would grow too big to respect due dates.
                .penalizeLong(ofSoft(1, 1),
                        PersonAssignment::getPriorityRating)
                .asConstraint("Schedule higher priority rating people");
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    Constraint preferredVaccineType(ConstraintFactory constraintFactory) {
        // Typical usage: if a person can pick a favorite vaccine type
        return constraintFactory
                .forEach(PersonAssignment.class)
                .filter((personAssignment -> personAssignment.getPreferredVaccineType() != null
                        && personAssignment.getVaccinationSlot().getVaccineType() != personAssignment.getPreferredVaccineType()))
                .penalize(ofSoft(2, 1_000_000_000))
                .asConstraint("Preferred vaccine type");
    }

    Constraint preferredVaccinationCenter(ConstraintFactory constraintFactory) {
        // Typical usage: if a person is coming for their 2nd dose, prefer the same vaccination center as their 1st dose.
        return constraintFactory
                .forEach(PersonAssignment.class)
                .filter((personAssignment -> personAssignment.getPreferredVaccinationCenter() != null
                        && personAssignment.getVaccinationSlot().getVaccinationCenter() != personAssignment.getPreferredVaccinationCenter()))
                // TODO ignore the distance cost instead
                .penalize(ofSoft(2, 1_000_000_000))
                .asConstraint("Preferred vaccination center");
    }

    Constraint regretDistance(ConstraintFactory constraintFactory) {
        // Minimize the distance from each person's home location to their assigned vaccination center
        // subtracted by the distance to the nearest vaccination center
        return constraintFactory
                .forEach(PersonAssignment.class)
                .penalizeLong(ofSoft(2, 1),
                        personAssignment -> {
                            long regretDistance = personAssignment.getRegretDistanceTo(
                                    personAssignment.getVaccinationSlot().getVaccinationCenter());
                            // Penalize outliers more for fairness
                            return regretDistance * regretDistance;
                        })
                .asConstraint("Regret distance cost");
    }

    Constraint idealDate(ConstraintFactory constraintFactory) {
        // Typical usage: If a person is coming for their 2nd dose, inject it on the ideal day.
        // For example, Pfizer is ideally injected 21 days after the first dose. Moderna after 28 days.
        return constraintFactory
                .forEach(PersonAssignment.class)
                .filter(personAssignment -> personAssignment.getIdealDate() != null
                        && !personAssignment.getIdealDate().equals(personAssignment.getVaccinationSlot().getDate()))
                // This constraint is softer than distanceCost() to avoid sending people
                // half-way across the country just to be one day closer to their ideal date.
                .penalizeLong(ofSoft(3, 1),
                        personAssignment -> {
                            long daysDiff = DAYS.between(personAssignment.getIdealDate(),
                                    personAssignment.getVaccinationSlot().getDate());
                            // Penalize outliers more for fairness
                            return daysDiff * daysDiff;
                        })
                .asConstraint("Ideal date");
    }

    Constraint higherPriorityRatingEarlier(ConstraintFactory constraintFactory) {
        // Assign healthcare workers and older people earlier in the planning window.
        // Priority rating is a person's age augmented by a few hundred points if they're a healthcare worker.
        // Differs from scheduleHigherPriorityRatingPeople(), which requires they be assigned
        return constraintFactory
                .forEach(PersonAssignment.class)
                // This constraint is softer than distanceCost() to avoid sending people
                // half-way across the country just to get their vaccine one day earlier.
                .penalizeLong(ofSoft(4, 1),
                        personAssignment -> personAssignment.getPriorityRating()
                                * MINUTES.between(COVID_EPOCH, personAssignment.getVaccinationSlot().getStartDateTime()))
                .asConstraint("Higher priority rating earlier");
    }

}
