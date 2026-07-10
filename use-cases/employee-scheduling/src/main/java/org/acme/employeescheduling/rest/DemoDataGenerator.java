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
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.context.ApplicationScoped;

import org.acme.employeescheduling.domain.Employee;
import org.acme.employeescheduling.domain.EmployeeSchedule;
import org.acme.employeescheduling.domain.Shift;

@ApplicationScoped
public class DemoDataGenerator {
    public enum DemoData {
        SMALL(new DemoDataParameters(
                List.of("Ambulatory care", "Critical care", "Pediatric care"),
                List.of("Doctor", "Nurse"),
                List.of("Anaesthetics", "Cardiology"),
                14,
                15,
                List.of(new CountDistribution(1, 3),
                        new CountDistribution(2, 1)
                ),
                List.of(new CountDistribution(1, 0.9),
                        new CountDistribution(2, 0.1)
                ),
                List.of(new CountDistribution(1, 4),
                        new CountDistribution(2, 3),
                        new CountDistribution(3, 2),
                        new CountDistribution(4, 1)
                ),
                0
        )),
        LARGE(new DemoDataParameters(
                List.of("Ambulatory care",
                        "Neurology",
                        "Critical care",
                        "Pediatric care",
                        "Surgery",
                        "Radiology",
                        "Outpatient"),
                List.of("Doctor", "Nurse"),
                List.of("Anaesthetics", "Cardiology", "Radiology"),
                28,
                50,
                List.of(new CountDistribution(1, 3),
                        new CountDistribution(2, 1)
                ),
                List.of(new CountDistribution(1, 0.5),
                        new CountDistribution(2, 0.3),
                        new CountDistribution(3, 0.2)
                ),
                List.of(new CountDistribution(5, 4),
                        new CountDistribution(10, 3),
                        new CountDistribution(15, 2),
                        new CountDistribution(20, 1)
                ),
                0
        ));

        private final DemoDataParameters parameters;

        DemoData(DemoDataParameters parameters) {
            this.parameters = parameters;
        }

        public DemoDataParameters getParameters() {
            return parameters;
        }
    }

    public record CountDistribution(int count, double weight) {}

    public record DemoDataParameters(List<String> locations,
                                     List<String> requiredSkills,
                                     List<String> optionalSkills,
                                     int daysInSchedule,
                                     int employeeCount,
                                     List<CountDistribution> optionalSkillDistribution,
                                     List<CountDistribution> shiftCountDistribution,
                                     List<CountDistribution> availabilityCountDistribution,
                                     int randomSeed) {}

    private static final String[] FIRST_NAMES = { "Amy", "Beth", "Carl", "Dan", "Elsa", "Flo", "Gus", "Hugo", "Ivy", "Jay" };
    private static final String[] LAST_NAMES = { "Cole", "Fox", "Green", "Jones", "King", "Li", "Poe", "Rye", "Smith", "Watt" };
    private static final Duration SHIFT_LENGTH = Duration.ofHours(8);
    private static final LocalTime MORNING_SHIFT_START_TIME = LocalTime.of(6, 0);
    private static final LocalTime DAY_SHIFT_START_TIME = LocalTime.of(9, 0);
    private static final LocalTime AFTERNOON_SHIFT_START_TIME = LocalTime.of(14, 0);
    private static final LocalTime NIGHT_SHIFT_START_TIME = LocalTime.of(22, 0);

    static final LocalTime[][] SHIFT_START_TIMES_COMBOS = {
            { MORNING_SHIFT_START_TIME, AFTERNOON_SHIFT_START_TIME },
            { MORNING_SHIFT_START_TIME, AFTERNOON_SHIFT_START_TIME, NIGHT_SHIFT_START_TIME },
            { MORNING_SHIFT_START_TIME, DAY_SHIFT_START_TIME, AFTERNOON_SHIFT_START_TIME, NIGHT_SHIFT_START_TIME },
    };

    Map<String, List<LocalTime>> locationToShiftStartTimeListMap = new HashMap<>();

    public EmployeeSchedule generateDemoData(DemoData demoData) {
        return generateDemoData(demoData.getParameters());
    }

    public EmployeeSchedule generateDemoData(DemoDataParameters parameters) {
        EmployeeSchedule employeeSchedule = new EmployeeSchedule();

        LocalDate startDate = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));

        Random random = new Random(parameters.randomSeed);

        int shiftTemplateIndex = 0;
        for (String location : parameters.locations) {
            locationToShiftStartTimeListMap.put(location, List.of(SHIFT_START_TIMES_COMBOS[shiftTemplateIndex]));
            shiftTemplateIndex = (shiftTemplateIndex + 1) % SHIFT_START_TIMES_COMBOS.length;
        }

        List<String> namePermutations = joinAllCombinations(FIRST_NAMES, LAST_NAMES);
        Collections.shuffle(namePermutations, random);

        List<Employee> employees = new ArrayList<>();
        for (int i = 0; i < parameters.employeeCount; i++) {
            Set<String> skills = pickSubset(parameters.optionalSkills, random, parameters.optionalSkillDistribution);
            skills.add(pickRandom(parameters.requiredSkills, random));
            Employee employee = new Employee(namePermutations.get(i), skills, new LinkedHashSet<>(), new LinkedHashSet<>(), new LinkedHashSet<>());
            employees.add(employee);
        }
        employeeSchedule.setEmployees(employees);

        List<Shift> shifts = new LinkedList<>();
        for (int i = 0; i < parameters.daysInSchedule; i++) {
            Set<Employee> employeesWithAvailabilitiesOnDay = pickSubset(employees, random,
                    parameters.availabilityCountDistribution);
            LocalDate date = startDate.plusDays(i);
            for (Employee employee : employeesWithAvailabilitiesOnDay) {
                switch (random.nextInt(3)) {
                    case 0 -> employee.getUnavailableDates().add(date);
                    case 1 -> employee.getUndesiredDates().add(date);
                    case 2 -> employee.getDesiredDates().add(date);
                }
            }
            shifts.addAll(generateShiftsForDay(parameters, date, random));
        }
        AtomicInteger countShift = new AtomicInteger();
        shifts.forEach(s -> s.setId(Integer.toString(countShift.getAndIncrement())));
        employeeSchedule.setShifts(shifts);

        return employeeSchedule;
    }

    private List<Shift> generateShiftsForDay(DemoDataParameters parameters, LocalDate date, Random random) {
        List<Shift> shifts = new LinkedList<>();
        for (String location : parameters.locations) {
            List<LocalTime> shiftStartTimes = locationToShiftStartTimeListMap.get(location);
            for (LocalTime shiftStartTime : shiftStartTimes) {
                LocalDateTime shiftStartDateTime = date.atTime(shiftStartTime);
                LocalDateTime shiftEndDateTime = shiftStartDateTime.plus(SHIFT_LENGTH);
                shifts.addAll(generateShiftForTimeslot(parameters, shiftStartDateTime, shiftEndDateTime, location, random));
            }
        }
        return shifts;
    }

    private List<Shift> generateShiftForTimeslot(DemoDataParameters parameters,
            LocalDateTime timeslotStart, LocalDateTime timeslotEnd, String location,
            Random random) {
        var shiftCount = pickCount(random, parameters.shiftCountDistribution);

        List<Shift> shifts = new LinkedList<>();
        for (int i = 0; i < shiftCount; i++) {
            String requiredSkill;
            if (random.nextBoolean()) {
                requiredSkill = pickRandom(parameters.requiredSkills, random);
            } else {
                requiredSkill = pickRandom(parameters.optionalSkills, random);
            }
            shifts.add(new Shift(timeslotStart, timeslotEnd, location, requiredSkill));
        }
        return shifts;
    }

    private <T> T pickRandom(List<T> source, Random random) {
        return source.get(random.nextInt(source.size()));
    }

    private int pickCount(Random random, List<CountDistribution> countDistribution) {
        double probabilitySum = 0;
        for (var possibility : countDistribution) {
            probabilitySum += possibility.weight;
        }
        var choice = random.nextDouble(probabilitySum);
        int numOfItems = 0;
        while (choice >= countDistribution.get(numOfItems).weight) {
            choice -= countDistribution.get(numOfItems).weight;
            numOfItems++;
        }
        return countDistribution.get(numOfItems).count;
    }

    private <T> Set<T> pickSubset(List<T> sourceSet, Random random, List<CountDistribution> countDistribution) {
        var count = pickCount(random, countDistribution);
        List<T> items = new ArrayList<>(sourceSet);
        Collections.shuffle(items, random);
        return new HashSet<>(items.subList(0, count));
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
