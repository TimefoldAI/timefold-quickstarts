package org.acme.vaccinationscheduler.domain.solver;

import static java.time.temporal.ChronoUnit.YEARS;

import java.time.LocalDate;
import java.util.Map;

import org.acme.vaccinationscheduler.domain.Appointment;
import org.acme.vaccinationscheduler.domain.Location;
import org.acme.vaccinationscheduler.domain.Person;
import org.acme.vaccinationscheduler.domain.VaccinationCenter;
import org.acme.vaccinationscheduler.domain.VaccinationSchedule;
import org.acme.vaccinationscheduler.domain.VaccineType;
import org.acme.vaccinationscheduler.solver.PersonAssignmentDifficultyComparator;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.entity.PlanningPin;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

@PlanningEntity(difficultyComparatorClass = PersonAssignmentDifficultyComparator.class)
public class PersonAssignment {

    private Person person;

    private Map<VaccinationCenter, Long> distances;
    private Long nearestVaccinationCenterDistance;

    /**
     * Following the bucket design pattern, a {@link VaccinationSlot} is a bucket of {@link Appointment} instances.
     * <p>
     * Translated from {@link Person#getAppointment()} before solving and back again after solving.
     * See {@link VaccinationSolution#VaccinationSolution(VaccinationSchedule)} and {@link VaccinationSolution#toSchedule()}.
     */
    private VaccinationSlot vaccinationSlot = null;

    // No-arg constructor required for Timefold
    public PersonAssignment() {
    }

    public PersonAssignment(Person person, Map<VaccinationCenter, Long> distances) {
        this.person = person;
        this.distances = distances;
        if (distances != null) {
            nearestVaccinationCenterDistance = distances.values().stream().mapToLong(Long::valueOf).min().orElse(0);
        }
    }

    public PersonAssignment(PersonAssignment original) {
        this.person = original.person;
        this.distances = original.distances;
        this.nearestVaccinationCenterDistance = original.nearestVaccinationCenterDistance;
        this.vaccinationSlot = original.vaccinationSlot;
    }

    public PersonAssignment(String id, String name, Location homeLocation, Map<VaccinationCenter, Long> distances, LocalDate birthdate, long priorityRating, VaccinationSlot vaccinationSlot) {
        this(new Person(id, name, homeLocation, birthdate, priorityRating), distances);
        this.vaccinationSlot = vaccinationSlot;
    }

    public PersonAssignment(String id, String name, Location homeLocation, Map<VaccinationCenter, Long> distances, LocalDate birthdate, long priorityRating,
                            int doseNumber, VaccineType requiredVaccineType, VaccineType preferredVaccineType,
                            VaccinationCenter requiredVaccinationCenter, VaccinationCenter preferredVaccinationCenter,
                            LocalDate readyDate, LocalDate idealDate, LocalDate dueDate,
                            VaccinationSlot vaccinationSlot) {
        this(new Person(id, name, homeLocation, birthdate, priorityRating, doseNumber,
                requiredVaccineType, preferredVaccineType, requiredVaccinationCenter, preferredVaccinationCenter,
                readyDate, idealDate, dueDate), distances);
        this.vaccinationSlot = vaccinationSlot;
    }

    public long getDistanceTo(VaccinationCenter vaccinationCenter) {
        Long distance = distances.get(vaccinationCenter);
        if (distance == null) {
            throw new IllegalStateException("The person (" + person
                    + ") is lacking a distance to vaccination center (" + vaccinationCenter + ").");
        }
        return distance;
    }

    public long getRegretDistanceTo(VaccinationCenter vaccinationCenter) {
        long distance = getDistanceTo(vaccinationCenter);
        VaccinationCenter requiredVaccinationCenter = getRequiredVaccinationCenter();
        if (requiredVaccinationCenter != null && requiredVaccinationCenter == vaccinationCenter) {
            return 0L;
        }
        VaccinationCenter preferedVaccinationCenter = getPreferredVaccinationCenter();
        if (preferedVaccinationCenter != null && preferedVaccinationCenter == vaccinationCenter) {
            return 0L;
        }
        return distance - nearestVaccinationCenterDistance;
    }

    public long getAgeOnVaccinationDate() {
        if (vaccinationSlot == null) {
            return -1;
        }
        return YEARS.between(person.getBirthdate(), vaccinationSlot.getDate());
    }

    @Override
    public String toString() {
        return person.toString();
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public Person getPerson() {
        return person;
    }

    @PlanningId
    public String getId() {
        return person.getId();
    }

    public String getName() {
        return person.getName();
    }

    public Location getHomeLocation() {
        return person.getHomeLocation();
    }

    public LocalDate getBirthdate() {
        return person.getBirthdate();
    }

    public long getPriorityRating() {
        return person.getPriorityRating();
    }

    public int getDoseNumber() {
        return person.getDoseNumber();
    }

    public VaccineType getRequiredVaccineType() {
        return person.getRequiredVaccineType();
    }

    public VaccineType getPreferredVaccineType() {
        return person.getPreferredVaccineType();
    }

    public VaccinationCenter getRequiredVaccinationCenter() {
        return person.getRequiredVaccinationCenter();
    }

    public VaccinationCenter getPreferredVaccinationCenter() {
        return person.getPreferredVaccinationCenter();
    }

    public LocalDate getReadyDate() {
        return person.getReadyDate();
    }

    public LocalDate getIdealDate() {
        return person.getIdealDate();
    }

    public LocalDate getDueDate() {
        return person.getDueDate();
    }

    @PlanningPin
    public boolean isPinned() {
        return person.isPinned();
    }

    @PlanningVariable(allowsUnassigned = true)
    public VaccinationSlot getVaccinationSlot() {
        return vaccinationSlot;
    }

    public void setVaccinationSlot(VaccinationSlot vaccinationSlot) {
        this.vaccinationSlot = vaccinationSlot;
    }

}
