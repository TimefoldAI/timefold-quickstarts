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

// TODO the default solution cloner doesn't scale to 100k+ appointments https://issues.redhat.com/browse/PLANNER-2398
@PlanningSolution
public class VaccinationSolution {

    protected static final Logger logger = LoggerFactory.getLogger(VaccinationSolution.class);

    @ProblemFactCollectionProperty
    private List<VaccineType> vaccineTypeList;

    @ProblemFactCollectionProperty
    private List<VaccinationCenter> vaccinationCenterList;

    private List<Appointment> appointmentList;

    /**
     * Following the bucket design pattern, a {@link VaccinationSlot} is a bucket of {@link Appointment} instances.
     * <p>
     * Translated from {@link VaccinationSchedule#getAppointmentList()} before solving and back again after solving.
     * See {@link #VaccinationSolution(VaccinationSchedule)} and {@link #toSchedule()}.
     */
    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<VaccinationSlot> vaccinationSlotList;

    @PlanningEntityCollectionProperty
    private List<PersonAssignment> personAssignmentList;

    @PlanningScore(bendableHardLevelsSize = VaccinationScheduleConstraintProvider.HARD_LEVELS_SIZE,
            bendableSoftLevelsSize = VaccinationScheduleConstraintProvider.SOFT_LEVELS_SIZE)
    private BendableLongScore score;

    // No-arg constructor required for Timefold
    public VaccinationSolution() {
    }

    public VaccinationSolution(List<VaccineType> vaccineTypeList, List<VaccinationCenter> vaccinationCenterList,
            List<Appointment> appointmentList, List<VaccinationSlot> vaccinationSlotList,
            List<PersonAssignment> personAssignmentList, BendableLongScore score) {
        this.vaccineTypeList = vaccineTypeList;
        this.vaccinationCenterList = vaccinationCenterList;
        this.appointmentList = appointmentList;
        this.vaccinationSlotList = vaccinationSlotList;
        this.personAssignmentList = personAssignmentList;
        this.score = score;
    }

    /**
     * Translates {@link VaccinationSchedule#getAppointmentList()} into {@link #vaccinationSlotList}.
     */
    public VaccinationSolution(VaccinationSchedule schedule) {
        this(schedule, new EuclideanDistanceCalculator());
    }

    /**
     * Translates {@link VaccinationSchedule#getAppointmentList()} into {@link #vaccinationSlotList}.
     */
    public VaccinationSolution(VaccinationSchedule schedule, DistanceCalculator distanceCalculator) {
        this.vaccineTypeList = schedule.getVaccineTypeList();
        this.vaccinationCenterList = schedule.getVaccinationCenterList();
        this.appointmentList = schedule.getAppointmentList();

        Function<Appointment, Triple<VaccinationCenter, LocalDateTime, VaccineType>> tripleFunction
                = (appointment) -> Triple.of(
                        appointment.getVaccinationCenter(),
                        appointment.getDateTime().truncatedTo(ChronoUnit.HOURS),
                        appointment.getVaccineType());
        Set<Appointment> scheduledAppointmentSet = schedule.getPersonList().stream()
                .map(Person::getAppointment)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Triple<VaccinationCenter, LocalDateTime, VaccineType>, List<Appointment>> appointmentListMap
                = schedule.getAppointmentList().stream()
                .collect(groupingBy(tripleFunction, LinkedHashMap::new, Collectors.collectingAndThen(
                        Collectors.toList(), subAppointmentList -> subAppointmentList.stream().sorted(
                                Comparator.comparing(Appointment::getDateTime).thenComparing(Appointment::getBoothId))
                                .collect(Collectors.toList()))));
        vaccinationSlotList = new ArrayList<>(appointmentListMap.size());
        Map<Triple<VaccinationCenter, LocalDateTime, VaccineType>, VaccinationSlot> vaccinationSlotMap = new HashMap<>(appointmentListMap.size());
        long nextVaccinationSlotId = 0;
        for (Map.Entry<Triple<VaccinationCenter, LocalDateTime, VaccineType>, List<Appointment>> entry : appointmentListMap.entrySet()) {
            Triple<VaccinationCenter, LocalDateTime, VaccineType> triple = entry.getKey();
            List<Appointment> appointmentList = entry.getValue();
            VaccinationCenter vaccinationCenter = triple.getLeft();
            LocalDateTime startDateTime = triple.getMiddle();
            VaccineType vaccineType = triple.getRight();
            List<Appointment> unscheduledAppointmentList = appointmentList.stream()
                    .filter(appointment -> !scheduledAppointmentSet.contains(appointment))
                    .collect(Collectors.toList());
            int capacity = appointmentList.size();
            VaccinationSlot vaccinationSlot = new VaccinationSlot(nextVaccinationSlotId++, vaccinationCenter,
                    startDateTime, vaccineType, unscheduledAppointmentList, capacity);
            vaccinationSlotList.add(vaccinationSlot);
            vaccinationSlotMap.put(triple, vaccinationSlot);
        }

        List<Person> personList = schedule.getPersonList();
        personAssignmentList = new ArrayList<>(personList.size());

        Location[] fromLocations = personList.stream().map(Person::getHomeLocation).toArray(Location[]::new);
        Location[] toLocations = vaccinationCenterList.stream().map(VaccinationCenter::getLocation).toArray(Location[]::new);
        // One single call to enable bulk mapping optimizations
        long[][] distanceMatrix = distanceCalculator.calculateBulkDistance(fromLocations, toLocations);
        for (int personIndex = 0; personIndex < personList.size(); personIndex++) {
            Person person = personList.get(personIndex);
            Map<VaccinationCenter, Long> distanceMap = new HashMap<>(vaccinationCenterList.size());
            for (int vaccinationCenterIndex = 0; vaccinationCenterIndex < vaccinationCenterList.size(); vaccinationCenterIndex++) {
                VaccinationCenter vaccinationCenter = vaccinationCenterList.get(vaccinationCenterIndex);
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
                            + schedule.getAppointmentList().size() + ")");
                }
                personAssignment.setVaccinationSlot(vaccinationSlot);
            }
            personAssignmentList.add(personAssignment);
        }
        this.score = schedule.getScore();
    }

    /**
     * Translates {@link #vaccinationSlotList} back into {@link VaccinationSchedule#getAppointmentList()}.
     */
    public VaccinationSchedule toSchedule() {
        Map<VaccinationSlot, List<Appointment>> appointmentListMap =
                vaccinationSlotList.stream().collect(toMap(vaccinationSlot -> vaccinationSlot,
                        // Shallow clone the appointmentList so the best solution event consumer doesn't corrupt the working solution
                        vaccinationSlot -> new ArrayList<>(vaccinationSlot.getUnscheduledAppointmentList())));
        List<Person> personList = new ArrayList<>(personAssignmentList.size());
        for (PersonAssignment personAssignment : personAssignmentList) {
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
        VaccinationSchedule schedule = new VaccinationSchedule(vaccineTypeList, vaccinationCenterList, appointmentList, personList);
        schedule.setScore(score);
        return schedule;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public List<VaccineType> getVaccineTypeList() {
        return vaccineTypeList;
    }

    public List<VaccinationCenter> getVaccinationCenterList() {
        return vaccinationCenterList;
    }

    public List<Appointment> getAppointmentList() {
        return appointmentList;
    }

    public List<VaccinationSlot> getVaccinationSlotList() {
        return vaccinationSlotList;
    }

    public void setVaccinationSlotList(List<VaccinationSlot> vaccinationSlotList) {
        this.vaccinationSlotList = vaccinationSlotList;
    }

    public List<PersonAssignment> getPersonAssignmentList() {
        return personAssignmentList;
    }

    public BendableLongScore getScore() {
        return score;
    }

}
