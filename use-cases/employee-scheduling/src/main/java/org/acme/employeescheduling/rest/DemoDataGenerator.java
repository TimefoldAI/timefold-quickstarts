package org.acme.employeescheduling.rest;

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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.context.ApplicationScoped;

import org.acme.employeescheduling.domain.Availability;
import org.acme.employeescheduling.domain.AvailabilityType;
import org.acme.employeescheduling.domain.Employee;
import org.acme.employeescheduling.domain.EmployeeSchedule;
import org.acme.employeescheduling.domain.ScheduleState;
import org.acme.employeescheduling.domain.Shift;

@ApplicationScoped
public class DemoDataGenerator {

    public enum DemoData {
        NONE,
        SMALL,
        LARGE
    }

    static final String[] FIRST_NAMES = { "Amy", "Beth", "Chad", "Dan", "Elsa", "Flo", "Gus", "Hugo", "Ivy", "Jay" };
    static final String[] LAST_NAMES = { "Cole", "Fox", "Green", "Jones", "King", "Li", "Poe", "Rye", "Smith", "Watt" };
    static final String[] REQUIRED_SKILLS = { "Doctor", "Nurse" };
    static final String[] OPTIONAL_SKILLS = { "Anaesthetics", "Cardiology" };
    static final String[] LOCATIONS = { "Ambulatory care", "Critical care", "Pediatric care" };
    static final Duration SHIFT_LENGTH = Duration.ofHours(8);
    static final LocalTime MORNING_SHIFT_START_TIME = LocalTime.of(6, 0);
    static final LocalTime DAY_SHIFT_START_TIME = LocalTime.of(9, 0);
    static final LocalTime AFTERNOON_SHIFT_START_TIME = LocalTime.of(14, 0);
    static final LocalTime NIGHT_SHIFT_START_TIME = LocalTime.of(22, 0);

    static final LocalTime[][] SHIFT_START_TIMES_COMBOS = {
            { MORNING_SHIFT_START_TIME, AFTERNOON_SHIFT_START_TIME },
            { MORNING_SHIFT_START_TIME, AFTERNOON_SHIFT_START_TIME, NIGHT_SHIFT_START_TIME },
            { MORNING_SHIFT_START_TIME, DAY_SHIFT_START_TIME, AFTERNOON_SHIFT_START_TIME, NIGHT_SHIFT_START_TIME },
    };

    Map<String, List<LocalTime>> locationToShiftStartTimeListMap = new HashMap<>();

    public EmployeeSchedule generateDemoData(DemoData demoData) {
        EmployeeSchedule employeeSchedule = new EmployeeSchedule();

        int initialRosterLengthInDays = 14;
        LocalDate startDate = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));

        ScheduleState scheduleState = new ScheduleState();
        scheduleState.setFirstDraftDate(startDate);
        scheduleState.setDraftLength(initialRosterLengthInDays);
        scheduleState.setPublishLength(7);
        scheduleState.setLastHistoricDate(startDate.minusDays(7));
        scheduleState.setTenantId(EmployeeScheduleResource.SINGLETON_SCHEDULE_ID);

        employeeSchedule.setScheduleState(scheduleState);

        Random random = new Random(0);

        int shiftTemplateIndex = 0;
        for (String location : LOCATIONS) {
            locationToShiftStartTimeListMap.put(location, List.of(SHIFT_START_TIMES_COMBOS[shiftTemplateIndex]));
            shiftTemplateIndex = (shiftTemplateIndex + 1) % SHIFT_START_TIMES_COMBOS.length;
        }

        if (demoData == DemoData.NONE) {
            return employeeSchedule;
        }
        List<String> namePermutations = joinAllCombinations(FIRST_NAMES, LAST_NAMES);
        Collections.shuffle(namePermutations, random);

        List<Employee> employees = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            Set<String> skills = pickSubset(List.of(OPTIONAL_SKILLS), random, 3, 1);
            skills.add(pickRandom(REQUIRED_SKILLS, random));
            Employee employee = new Employee(namePermutations.get(i), skills);
            employees.add(employee);
        }
        employeeSchedule.setEmployees(employees);

        List<Availability> availabilityList = new LinkedList<>();
        List<Shift> shiftList = new LinkedList<>();
        int count = 0;
        for (int i = 0; i < initialRosterLengthInDays; i++) {
            Set<Employee> employeesWithAvailabitiesOnDay = pickSubset(employees, random, 4, 3, 2, 1);
            LocalDate date = startDate.plusDays(i);
            for (Employee employee : employeesWithAvailabitiesOnDay) {
                AvailabilityType availabilityType = pickRandom(AvailabilityType.values(), random);
                availabilityList.add(new Availability(Integer.toString(count++), employee, date, availabilityType));
            }
            shiftList.addAll(generateShiftsForDay(date, random));
        }
        AtomicInteger countShift = new AtomicInteger();
        shiftList.forEach(s -> s.setId(Integer.toString(countShift.getAndIncrement())));
        employeeSchedule.setAvailabilities(availabilityList);
        employeeSchedule.setShifts(shiftList);

        return employeeSchedule;
    }

    private List<Shift> generateShiftsForDay(LocalDate date, Random random) {
        List<Shift> shiftList = new LinkedList<>();
        for (String location : LOCATIONS) {
            List<LocalTime> shiftStartTimes = locationToShiftStartTimeListMap.get(location);
            for (LocalTime shiftStartTime : shiftStartTimes) {
                LocalDateTime shiftStartDateTime = date.atTime(shiftStartTime);
                LocalDateTime shiftEndDateTime = shiftStartDateTime.plus(SHIFT_LENGTH);
                shiftList.addAll(generateShiftForTimeslot(shiftStartDateTime, shiftEndDateTime, location, random));
            }
        }
        return shiftList;
    }

    private List<Shift> generateShiftForTimeslot(LocalDateTime timeslotStart, LocalDateTime timeslotEnd, String location,
            Random random) {
        int shiftCount = 1;

        if (random.nextDouble() > 0.9) {
            // generate an extra shift
            shiftCount++;
        }

        List<Shift> shiftList = new LinkedList<>();
        for (int i = 0; i < shiftCount; i++) {
            String requiredSkill;
            if (random.nextBoolean()) {
                requiredSkill = pickRandom(REQUIRED_SKILLS, random);
            } else {
                requiredSkill = pickRandom(OPTIONAL_SKILLS, random);
            }
            shiftList.add(new Shift(timeslotStart, timeslotEnd, location, requiredSkill));
        }
        return shiftList;
    }

    public void addDraftShifts(EmployeeSchedule schedule) {
        List<Employee> employees = schedule.getEmployees();
        Random random = new Random(0);

        List<Shift> shiftList = new LinkedList<>();
        List<Availability> availabilityList = new LinkedList<>();
        int countAvailability = schedule.getAvailabilities().stream()
                .map(Availability::getId)
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0);
        AtomicInteger countShift = new AtomicInteger(schedule.getShifts().stream()
                .map(Shift::getId)
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0));
        for (int i = 0; i < schedule.getScheduleState().getPublishLength(); i++) {
            Set<Employee> employeesWithAvailabitiesOnDay = pickSubset(employees, random, 4, 3, 2, 1);
            LocalDate date = schedule.getScheduleState().getFirstDraftDate()
                    .plusDays(schedule.getScheduleState().getPublishLength() + i);
            for (Employee employee : employeesWithAvailabitiesOnDay) {
                AvailabilityType availabilityType = pickRandom(AvailabilityType.values(), random);
                availabilityList.add(new Availability(Integer.toString(++countAvailability), employee, date, availabilityType));
            }
            shiftList.addAll(generateShiftsForDay(date, random));
        }
        schedule.getAvailabilities().addAll(availabilityList);
        shiftList.forEach(s -> s.setId(Integer.toString(countShift.incrementAndGet())));
        schedule.getShifts().addAll(shiftList);
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
            item.delete(0, 1);
            out.add(item.toString());
        }
        return out;
    }
}
