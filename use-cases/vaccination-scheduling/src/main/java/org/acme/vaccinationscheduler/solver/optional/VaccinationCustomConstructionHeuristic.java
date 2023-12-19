package org.acme.vaccinationscheduler.solver.optional;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.YEARS;
import static java.util.stream.Collectors.groupingBy;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.acme.vaccinationscheduler.domain.VaccinationCenter;
import org.acme.vaccinationscheduler.domain.VaccineType;
import org.acme.vaccinationscheduler.domain.solver.PersonAssignment;
import org.acme.vaccinationscheduler.domain.solver.VaccinationSlot;
import org.acme.vaccinationscheduler.domain.solver.VaccinationSolution;
import org.acme.vaccinationscheduler.solver.PersonAssignmentDifficultyComparator;
import ai.timefold.solver.core.api.score.director.ScoreDirector;
import ai.timefold.solver.core.impl.phase.custom.CustomPhaseCommand;

/**
 * Power specialized custom Construction Heuristic.
 */
public class VaccinationCustomConstructionHeuristic implements CustomPhaseCommand<VaccinationSolution> {

    @Override
    public void changeWorkingSolution(ScoreDirector<VaccinationSolution> scoreDirector) {
        VaccinationSolution schedule = scoreDirector.getWorkingSolution();

        // Index the VaccinationSlot instances by vaccinationCenter, vaccineType and date
        Map<VaccinationCenter, Map<VaccineType, Map<LocalDate, Map<VaccinationSlot, Integer>>>> vaccinationCenterToSlotMap
                = schedule.getVaccinationSlots().stream()
                .sorted(Comparator
                        .comparing((VaccinationSlot vaccinationSlot) -> vaccinationSlot.getVaccineType().getName())
                        .thenComparing(VaccinationSlot::getDate)
                        .thenComparing(VaccinationSlot::getStartTime))
                .collect(groupingBy(VaccinationSlot::getVaccinationCenter, LinkedHashMap::new,
                        groupingBy(VaccinationSlot::getVaccineType, LinkedHashMap::new,
                                groupingBy(VaccinationSlot::getDate,
                                        LinkedHashMap::new, Collectors.toMap(
                                                vaccinationSlot -> vaccinationSlot, VaccinationSlot::getCapacity,
                                                (key, value) -> {
                                                    throw new IllegalStateException("Duplicate key (" + key + ").");
                                                },
                                                LinkedHashMap::new)))));
        schedule.getPersonAssignments().stream()
                .filter(person -> person.getVaccinationSlot() != null)
                .forEach(person ->  {
            VaccinationSlot vaccinationSlot = person.getVaccinationSlot();
            VaccinationCenter vaccinationCenter = vaccinationSlot.getVaccinationCenter();
            Map<VaccineType, Map<LocalDate, Map<VaccinationSlot, Integer>>> vaccineTypeToSlotMap
                    = vaccinationCenterToSlotMap.get(vaccinationCenter);
            VaccineType vaccineType = vaccinationSlot.getVaccineType();
            Map<LocalDate, Map<VaccinationSlot, Integer>> dateToSlotMap = vaccineTypeToSlotMap.get(vaccineType);
            LocalDate date = vaccinationSlot.getDate();
            Map<VaccinationSlot, Integer> slotToAvailabilityMap = dateToSlotMap.get(date);
            int availability = slotToAvailabilityMap.get(vaccinationSlot);
            reduceAvailability(vaccinationCenterToSlotMap, vaccinationCenter,
                    vaccineTypeToSlotMap, vaccineType,
                    dateToSlotMap, date,
                    slotToAvailabilityMap, vaccinationSlot,
                    availability);
        });
        List<PersonAssignment> people = schedule.getPersonAssignments().stream()
                .filter(person -> !person.isPinned() && person.getVaccinationSlot() == null)
                .sorted(new PersonAssignmentDifficultyComparator().reversed())
                .collect(Collectors.toList());
        for (PersonAssignment person : people) {
            VaccinationSlot vaccinationSlot = findAvailableVaccinationSlot(scoreDirector, vaccinationCenterToSlotMap, person);
            if (vaccinationSlot != null) {
                scoreDirector.beforeVariableChanged(person, "vaccinationSlot");
                person.setVaccinationSlot(vaccinationSlot);
                scoreDirector.afterVariableChanged(person, "vaccinationSlot");
                scoreDirector.triggerVariableListeners();
            }
        }
    }

    private VaccinationSlot findAvailableVaccinationSlot(ScoreDirector<VaccinationSolution> scoreDirector,
            Map<VaccinationCenter, Map<VaccineType, Map<LocalDate, Map<VaccinationSlot, Integer>>>> vaccinationCenterToSlotMap,
            PersonAssignment person) {
        // Iterate the nearest VaccinationCenters to the person first.
        List<VaccinationCenter> vaccinationCenterList = vaccinationCenterToSlotMap.keySet().stream()
                .sorted(Comparator
                        .comparing((VaccinationCenter vaccinationCenter) -> person.getRequiredVaccinationCenter() != vaccinationCenter)
                        .thenComparing((VaccinationCenter vaccinationCenter) -> person.getPreferredVaccinationCenter() != vaccinationCenter)
                        .thenComparing(person::getDistanceTo))
                .collect(Collectors.toList());

        for (VaccinationCenter vaccinationCenter : vaccinationCenterList) {
            Map<VaccineType, Map<LocalDate, Map<VaccinationSlot, Integer>>> vaccineTypeToSlotMap
                    = vaccinationCenterToSlotMap.get(vaccinationCenter);
            for (Map.Entry<VaccineType, Map<LocalDate, Map<VaccinationSlot, Integer>>> vaccineTypeEntry : vaccineTypeToSlotMap.entrySet()) {
                VaccineType vaccineType = vaccineTypeEntry.getKey();
                // Skip all slots with the wrong vaccineType
                if (person.getRequiredVaccineType() != null && person.getRequiredVaccineType() != vaccineType) {
                    if (person.getRequiredVaccineType().getName().equals(vaccineType.getName())) {
                        throw new IllegalStateException("Don't have 2 VaccineType with the same name ("
                                + vaccineType.getName() + ") instances in your input data.");
                    }
                    continue;
                }
                Map<LocalDate, Map<VaccinationSlot, Integer>> dateToSlotMap = vaccineTypeEntry.getValue();
                List<LocalDate> dates = dateToSlotMap.keySet().stream()
                        .filter(date -> {
                            // Skip all slots with an invalid date
                            long age = YEARS.between(person.getBirthdate(), date);
                            if (vaccineType.getMinimumAge() != null && age < vaccineType.getMinimumAge()) {
                                return false;
                            }
                            if (vaccineType.getMaximumAge() != null && age > vaccineType.getMaximumAge()) {
                                return false;
                            }
                            if (person.getReadyDate() != null && date.compareTo(person.getReadyDate()) < 0) {
                                return false;
                            }
                            if (person.getDueDate() != null && date.compareTo(person.getDueDate()) > 0) {
                                return false;
                            }
                            return true;
                        })
                        .sorted(person.getIdealDate() == null ? Comparator.naturalOrder()
                                : Comparator.<LocalDate, Long>comparing(date ->
                                Math.abs(DAYS.between(person.getIdealDate(), date))))
                        .collect(Collectors.toList());
                for (LocalDate date : dates) {
                    Map<VaccinationSlot, Integer> slotToAvailabilityMap = dateToSlotMap.get(date);
                    for (Map.Entry<VaccinationSlot, Integer> slotEntry : slotToAvailabilityMap.entrySet()) {
                        VaccinationSlot vaccinationSlot = slotEntry.getKey();
                        int availability = slotEntry.getValue();
                        reduceAvailability(vaccinationCenterToSlotMap, vaccinationCenter,
                                vaccineTypeToSlotMap, vaccineType,
                                dateToSlotMap, date,
                                slotToAvailabilityMap, vaccinationSlot,
                                availability);
                        return vaccinationSlot;
                    }
                }
            }
        }
        return null;
    }

    private void reduceAvailability(
            Map<VaccinationCenter, Map<VaccineType, Map<LocalDate, Map<VaccinationSlot, Integer>>>> vaccinationCenterToSlotMap, VaccinationCenter vaccinationCenter,
            Map<VaccineType, Map<LocalDate, Map<VaccinationSlot, Integer>>> vaccineTypeToSlotMap, VaccineType vaccineType,
            Map<LocalDate, Map<VaccinationSlot, Integer>> dateToSlotMap, LocalDate date,
            Map<VaccinationSlot, Integer> slotToAvailabilityMap, VaccinationSlot vaccinationSlot,
            int availability) {
        availability--;
        slotToAvailabilityMap.put(vaccinationSlot, availability);
        if (availability == 0) {
            slotToAvailabilityMap.remove(vaccinationSlot);
            if (slotToAvailabilityMap.isEmpty()) {
                dateToSlotMap.remove(date);
                if (dateToSlotMap.isEmpty()) {
                    vaccineTypeToSlotMap.remove(vaccineType);
                    if (vaccineTypeToSlotMap.isEmpty()) {
                        vaccinationCenterToSlotMap.remove(vaccinationCenter);
                    }
                }
            }
        }
    }

}
