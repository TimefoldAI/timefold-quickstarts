package org.acme.vaccinationscheduler.domain.solver;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.acme.vaccinationscheduler.domain.Appointment;
import org.acme.vaccinationscheduler.domain.Location;
import org.acme.vaccinationscheduler.domain.Person;
import org.acme.vaccinationscheduler.domain.VaccinationCenter;
import org.acme.vaccinationscheduler.domain.VaccinationSchedule;
import org.acme.vaccinationscheduler.domain.VaccineType;
import org.acme.vaccinationscheduler.solver.VaccinationScheduleConstraintProvider;
import org.acme.vaccinationscheduler.solver.geo.DistanceCalculator;
import org.acme.vaccinationscheduler.solver.geo.EuclideanDistanceCalculator;
import org.apache.commons.lang3.tuple.Triple;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.bendablelong.BendableLongScore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PlanningSolution
public class VaccinationSolution {

    protected static final Logger logger = LoggerFactory.getLogger(VaccinationSolution.class);

    @ProblemFactCollectionProperty
    private List<VaccineType> vaccineTypes;

    @ProblemFactCollectionProperty
    private List<VaccinationCenter> vaccinationCenters;

    private List<Appointment> appointments;

    /**
     * Following the bucket design pattern, a {@link VaccinationSlot} is a bucket of {@link Appointment} instances.
     * <p>
     * Translated from {@link VaccinationSchedule#getAppointments()} before solving and back again after solving.
     * See {@link #VaccinationSolution(VaccinationSchedule)} and {@link #toSchedule()}.
     */
    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<VaccinationSlot> vaccinationSlots;

    @PlanningEntityCollectionProperty
    private List<PersonAssignment> personAssignments;

    @PlanningScore(bendableHardLevelsSize = VaccinationScheduleConstraintProvider.HARD_LEVELS_SIZE,
            bendableSoftLevelsSize = VaccinationScheduleConstraintProvider.SOFT_LEVELS_SIZE)
    private BendableLongScore score;

    // No-arg constructor required for Timefold
    public VaccinationSolution() {
    }

    public VaccinationSolution(List<VaccineType> vaccineTypes, List<VaccinationCenter> vaccinationCenters,
                               List<Appointment> appointments, List<VaccinationSlot> vaccinationSlots,
                               List<PersonAssignment> personAssignments, BendableLongScore score) {
        this.vaccineTypes = vaccineTypes;
        this.vaccinationCenters = vaccinationCenters;
        this.appointments = appointments;
        this.vaccinationSlots = vaccinationSlots;
        this.personAssignments = personAssignments;
        this.score = score;
    }

    /**
     * Translates {@link VaccinationSchedule#getAppointments()} into {@link #vaccinationSlots}.
     */
    public VaccinationSolution(VaccinationSchedule schedule) {
        this(schedule, new EuclideanDistanceCalculator());
    }

    /**
     * Translates {@link VaccinationSchedule#getAppointments()} into {@link #vaccinationSlots}.
     */
    public VaccinationSolution(VaccinationSchedule schedule, DistanceCalculator distanceCalculator) {
        this.vaccineTypes = schedule.getVaccineTypes();
        this.vaccinationCenters = schedule.getVaccinationCenters();
        this.appointments = schedule.getAppointments();

        Function<Appointment, Triple<VaccinationCenter, LocalDateTime, VaccineType>> tripleFunction
                = (appointment) -> Triple.of(
                        appointment.getVaccinationCenter(),
                        appointment.getDateTime().truncatedTo(ChronoUnit.HOURS),
                        appointment.getVaccineType());
        Set<Appointment> scheduledAppointments = schedule.getPeople().stream()
                .map(Person::getAppointment)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Triple<VaccinationCenter, LocalDateTime, VaccineType>, List<Appointment>> appointmentListMap
                = schedule.getAppointments().stream()
                .collect(groupingBy(tripleFunction, LinkedHashMap::new, Collectors.collectingAndThen(
                        Collectors.toList(), subAppointmentList -> subAppointmentList.stream().sorted(
                                Comparator.comparing(Appointment::getDateTime).thenComparing(Appointment::getBoothId))
                                .collect(Collectors.toList()))));
        vaccinationSlots = new ArrayList<>(appointmentListMap.size());
        Map<Triple<VaccinationCenter, LocalDateTime, VaccineType>, VaccinationSlot> vaccinationSlotMap = new HashMap<>(appointmentListMap.size());
        long nextVaccinationSlotId = 0;
        for (Map.Entry<Triple<VaccinationCenter, LocalDateTime, VaccineType>, List<Appointment>> entry : appointmentListMap.entrySet()) {
            Triple<VaccinationCenter, LocalDateTime, VaccineType> triple = entry.getKey();
            List<Appointment> appointmentList = entry.getValue();
            VaccinationCenter vaccinationCenter = triple.getLeft();
            LocalDateTime startDateTime = triple.getMiddle();
            VaccineType vaccineType = triple.getRight();
            List<Appointment> unscheduledAppointmentList = appointmentList.stream()
                    .filter(appointment -> !scheduledAppointments.contains(appointment))
                    .collect(Collectors.toList());
            int capacity = appointmentList.size();
            VaccinationSlot vaccinationSlot = new VaccinationSlot(nextVaccinationSlotId++, vaccinationCenter,
                    startDateTime, vaccineType, unscheduledAppointmentList, capacity);
            vaccinationSlots.add(vaccinationSlot);
            vaccinationSlotMap.put(triple, vaccinationSlot);
        }

        List<Person> personList = schedule.getPeople();
        personAssignments = new ArrayList<>(personList.size());

        Location[] fromLocations = personList.stream().map(Person::getHomeLocation).toArray(Location[]::new);
        Location[] toLocations = vaccinationCenters.stream().map(VaccinationCenter::getLocation).toArray(Location[]::new);
        // One single call to enable bulk mapping optimizations
        long[][] distanceMatrix = distanceCalculator.calculateBulkDistance(fromLocations, toLocations);
        for (int personIndex = 0; personIndex < personList.size(); personIndex++) {
            Person person = personList.get(personIndex);
            Map<VaccinationCenter, Long> distanceMap = new HashMap<>(vaccinationCenters.size());
            for (int vaccinationCenterIndex = 0; vaccinationCenterIndex < vaccinationCenters.size(); vaccinationCenterIndex++) {
                VaccinationCenter vaccinationCenter = vaccinationCenters.get(vaccinationCenterIndex);
                long distance = distanceMatrix[personIndex][vaccinationCenterIndex];
                distanceMap.put(vaccinationCenter, distance);
            }
            PersonAssignment personAssignment = new PersonAssignment(person, distanceMap);
            Appointment appointment = person.getAppointment();
            // Person.appointment is non-null with pinned persons but maybe also with non-pinned persons from draft runs
            if (appointment != null) {
                VaccinationSlot vaccinationSlot = vaccinationSlotMap.get(tripleFunction.apply(appointment));
                if (vaccinationSlot == null) {
                    throw new IllegalStateException("The person (" + person
                            + ") has a pre-set appointment (" + appointment
                            + ") that is not part of the schedule's appointmentList with size ("
                            + schedule.getAppointments().size() + ")");
                }
                personAssignment.setVaccinationSlot(vaccinationSlot);
            }
            personAssignments.add(personAssignment);
        }
        this.score = schedule.getScore();
    }

    /**
     * Translates {@link #vaccinationSlots} back into {@link VaccinationSchedule#getAppointments()}.
     */
    public VaccinationSchedule toSchedule() {
        Map<VaccinationSlot, List<Appointment>> appointmentListMap =
                vaccinationSlots.stream().collect(toMap(vaccinationSlot -> vaccinationSlot,
                        // Shallow clone the appointmentList so the best solution event consumer doesn't corrupt the working solution
                        vaccinationSlot -> new ArrayList<>(vaccinationSlot.getUnscheduledAppointments())));
        List<Person> personList = new ArrayList<>(personAssignments.size());
        for (PersonAssignment personAssignment : personAssignments) {
            Person person = personAssignment.getPerson();
            if (!person.isPinned()) {
                VaccinationSlot vaccinationSlot = personAssignment.getVaccinationSlot();
                Appointment appointment;
                if (vaccinationSlot == null) {
                    appointment = null;
                } else {
                    List<Appointment> appointmentList = appointmentListMap.get(vaccinationSlot);
                    if (appointmentList.isEmpty()) {
                        logger.error("The solution is infeasible: the person (" + personAssignment
                                + ") is assigned to vaccinationSlot (" + vaccinationSlot
                                + ") but all the appointments are already taken, so leaving that person unassigned.\n"
                                + "Impossible situation: even if the problem has no feasible solution,"
                                + " the capacity hard constraint should force the all-but-one person to remain unassigned"
                                + " because the planning variable has nullable=true.");
                        appointment = null;
                    } else {
                        appointment = appointmentList.remove(0);
                    }
                }
                // No need to clone Person because during solving, the constraints ignore Person.appointment
                person.setAppointment(appointment);
            }
            personList.add(person);
        }
        VaccinationSchedule schedule = new VaccinationSchedule(vaccineTypes, vaccinationCenters, appointments, personList);
        schedule.setScore(score);
        return schedule;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public List<VaccineType> getVaccineTypes() {
        return vaccineTypes;
    }

    public List<VaccinationCenter> getVaccinationCenters() {
        return vaccinationCenters;
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public List<VaccinationSlot> getVaccinationSlots() {
        return vaccinationSlots;
    }

    public void setVaccinationSlots(List<VaccinationSlot> vaccinationSlots) {
        this.vaccinationSlots = vaccinationSlots;
    }

    public List<PersonAssignment> getPersonAssignments() {
        return personAssignments;
    }

    public BendableLongScore getScore() {
        return score;
    }

}
