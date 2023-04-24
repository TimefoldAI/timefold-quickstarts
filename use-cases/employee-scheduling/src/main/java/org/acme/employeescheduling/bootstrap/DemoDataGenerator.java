package org.acme.employeescheduling.bootstrap;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.acme.employeescheduling.domain.Availability;
import org.acme.employeescheduling.domain.AvailabilityType;
import org.acme.employeescheduling.domain.Employee;
import org.acme.employeescheduling.domain.ScheduleState;
import org.acme.employeescheduling.domain.Shift;
import org.acme.employeescheduling.persistence.AvailabilityRepository;
import org.acme.employeescheduling.persistence.EmployeeRepository;
import org.acme.employeescheduling.persistence.ScheduleStateRepository;
import org.acme.employeescheduling.persistence.ShiftRepository;
import org.acme.employeescheduling.rest.EmployeeScheduleResource;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class DemoDataGenerator {

    @ConfigProperty(name = "schedule.demoData", defaultValue = "SMALL")
    DemoData demoData;

    public enum DemoData {
        NONE,
        SMALL,
        LARGE
    }

    static final String[] FIRST_NAMES = {"Amy", "Beth", "Chad", "Dan", "Elsa", "Flo", "Gus", "Hugo", "Ivy", "Jay"};
    static final String[] LAST_NAMES = {"Cole", "Fox", "Green", "Jones", "King", "Li", "Poe", "Rye", "Smith", "Watt"};
    static final String[] REQUIRED_SKILLS = { "Doctor", "Nurse"};
    static final String[] OPTIONAL_SKILLS = { "Anaesthetics", "Cardiology"};
    static final String[] LOCATIONS = { "Ambulatory care", "Critical care", "Pediatric care"};
    static final Duration SHIFT_LENGTH = Duration.ofHours(8);
    static final LocalTime MORNING_SHIFT_START_TIME = LocalTime.of(6, 0);
    static final LocalTime DAY_SHIFT_START_TIME = LocalTime.of(9, 0);
    static final LocalTime AFTERNOON_SHIFT_START_TIME = LocalTime.of(14, 0);
    static final LocalTime NIGHT_SHIFT_START_TIME = LocalTime.of(22, 0);

    static final LocalTime[][] SHIFT_START_TIMES_COMBOS = {
            {MORNING_SHIFT_START_TIME, AFTERNOON_SHIFT_START_TIME},
            {MORNING_SHIFT_START_TIME, AFTERNOON_SHIFT_START_TIME, NIGHT_SHIFT_START_TIME},
            {MORNING_SHIFT_START_TIME, DAY_SHIFT_START_TIME, AFTERNOON_SHIFT_START_TIME, NIGHT_SHIFT_START_TIME},
    };

    Map<String,List<LocalTime>> locationToShiftStartTimeListMap = new HashMap<>();

    @Inject
    EmployeeRepository employeeRepository;
    @Inject
    AvailabilityRepository availabilityRepository;
    @Inject
    ShiftRepository shiftRepository;
    @Inject
    ScheduleStateRepository scheduleStateRepository;

    @Transactional
    public void generateDemoData(@Observes StartupEvent startupEvent) {
        final int INITIAL_ROSTER_LENGTH_IN_DAYS = 14;
        final LocalDate START_DATE = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));

        ScheduleState scheduleState = new ScheduleState();
        scheduleState.setFirstDraftDate(START_DATE);
        scheduleState.setDraftLength(INITIAL_ROSTER_LENGTH_IN_DAYS);
        scheduleState.setPublishLength(7);
        scheduleState.setLastHistoricDate(START_DATE.minusDays(7));
        scheduleState.setTenantId(EmployeeScheduleResource.SINGLETON_SCHEDULE_ID);

        scheduleStateRepository.persist(scheduleState);

        Random random = new Random(0);

        int shiftTemplateIndex = 0;
        for (String location : LOCATIONS) {
            locationToShiftStartTimeListMap.put(location, List.of(SHIFT_START_TIMES_COMBOS[shiftTemplateIndex]));
            shiftTemplateIndex = (shiftTemplateIndex + 1) % SHIFT_START_TIMES_COMBOS.length;
        }

        if (demoData == DemoData.NONE) {
            return;
        }
        List<String> namePermutations = joinAllCombinations(FIRST_NAMES, LAST_NAMES);
        Collections.shuffle(namePermutations, random);

        List<Employee> employeeList = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            Set<String> skills = pickSubset(List.of(OPTIONAL_SKILLS), random, 3, 1);
            skills.add(pickRandom(REQUIRED_SKILLS, random));
            Employee employee = new Employee(namePermutations.get(i), skills);
            employeeRepository.persist(employee);
            employeeList.add(employee);
        }

        for (int i = 0; i < INITIAL_ROSTER_LENGTH_IN_DAYS; i++) {
            Set<Employee> employeesWithAvailabitiesOnDay = pickSubset(employeeList, random, 4, 3, 2, 1);
            LocalDate date = START_DATE.plusDays(i);
            for (Employee employee : employeesWithAvailabitiesOnDay) {
                AvailabilityType availabilityType = pickRandom(AvailabilityType.values(), random);
                availabilityRepository.persist(new Availability(employee, date, availabilityType));
            }

            generateShiftsForDay(date, random);
        }
    }

    private void generateShiftsForDay(LocalDate date, Random random) {
        for (String location : LOCATIONS) {
            List<LocalTime> shiftStartTimeList = locationToShiftStartTimeListMap.get(location);
            for (LocalTime shiftStartTime : shiftStartTimeList) {
                LocalDateTime shiftStartDateTime = date.atTime(shiftStartTime);
                LocalDateTime shiftEndDateTime = shiftStartDateTime.plus(SHIFT_LENGTH);
                generateShiftForTimeslot(shiftStartDateTime, shiftEndDateTime, location, random);
            }
        }
    }

    private void generateShiftForTimeslot(LocalDateTime timeslotStart, LocalDateTime timeslotEnd, String location,
                                          Random random) {
        int shiftCount = 1;

        if (random.nextDouble() > 0.9) {
            // generate an extra shift
            shiftCount++;
        }

        for (int i = 0; i < shiftCount; i++) {
            String requiredSkill;
            if (random.nextBoolean()) {
                requiredSkill = pickRandom(REQUIRED_SKILLS, random);
            } else {
                requiredSkill = pickRandom(OPTIONAL_SKILLS, random);
            }
            shiftRepository.persist(new Shift(timeslotStart, timeslotEnd, location, requiredSkill));
        }
    }

    private <T> T pickRandom(T[] source, Random random) {
        return source[random.nextInt(source.length)];
    }

    private <T> Set<T> pickSubset(List<T> sourceSet, Random random, int... distribution) {
        int probabilitySum = 0;
        for (int probability : distribution) {
            probabilitySum += probability;
        }
        int choice = random.nextInt(probabilitySum);
        int numOfItems = 0;
        while (choice >= distribution[numOfItems]) {
            choice -= distribution[numOfItems];
            numOfItems++;
        }
        List<T> items = new ArrayList<>(sourceSet);
        Collections.shuffle(items, random);
        return new HashSet<>(items.subList(0, numOfItems + 1));
    }

    private List<String> joinAllCombinations(String[]... partArrays) {
        int size = 1;
        for (String[] partArray : partArrays) {
            size *= partArray.length;
        }
        List<String> out = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            StringBuilder item = new StringBuilder();
            int sizePerIncrement = 1;
            for (String[] partArray : partArrays) {
                item.append(' ');
                item.append(partArray[(i / sizePerIncrement) % partArray.length]);
                sizePerIncrement *= partArray.length;
            }
            item.delete(0,1);
            out.add(item.toString());
        }
        return out;
    }

    public void generateDraftShifts(ScheduleState scheduleState) {
        List<Employee> employeeList = employeeRepository.listAll();
        Random random = new Random(0);

        for (int i = 0; i < scheduleState.getPublishLength(); i++) {
            Set<Employee> employeesWithAvailabitiesOnDay = pickSubset(employeeList, random, 4, 3, 2, 1);
            LocalDate date = scheduleState.getFirstDraftDate().plusDays(scheduleState.getPublishLength() + i);
            for (Employee employee : employeesWithAvailabitiesOnDay) {
                AvailabilityType availabilityType = pickRandom(AvailabilityType.values(), random);
                availabilityRepository.persist(new Availability(employee, date, availabilityType));
            }

            generateShiftsForDay(date, random);
        }
    }

}
