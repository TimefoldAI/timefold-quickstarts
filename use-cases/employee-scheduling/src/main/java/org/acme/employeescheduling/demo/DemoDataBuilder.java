package org.acme.employeescheduling.demo;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.acme.employeescheduling.dto.EmployeeDTO;
import org.acme.employeescheduling.dto.EmployeeScheduleInput;
import org.acme.employeescheduling.dto.ShiftDTO;

public final class DemoDataBuilder {

    private static final String[] FIRST_NAMES = { "Amy", "Beth", "Carl", "Dan", "Elsa", "Flo", "Gus", "Hugo", "Ivy",
            "Jay" };
    private static final String[] LAST_NAMES =
            { "Cole", "Fox", "Green", "Jones", "King", "Li", "Poe", "Rye", "Smith", "Watt" };
    private static final long DEFAULT_RANDOM_SEED = 0L;

    private static final java.time.Duration SHIFT_LENGTH = java.time.Duration.ofHours(8);
    private static final LocalTime MORNING_SHIFT_START_TIME = LocalTime.of(6, 0);
    private static final LocalTime DAY_SHIFT_START_TIME = LocalTime.of(9, 0);
    private static final LocalTime AFTERNOON_SHIFT_START_TIME = LocalTime.of(14, 0);
    private static final LocalTime NIGHT_SHIFT_START_TIME = LocalTime.of(22, 0);

    static final LocalTime[][] SHIFT_START_TIMES_COMBOS = {
            { MORNING_SHIFT_START_TIME, AFTERNOON_SHIFT_START_TIME },
            { MORNING_SHIFT_START_TIME, AFTERNOON_SHIFT_START_TIME, NIGHT_SHIFT_START_TIME },
            { MORNING_SHIFT_START_TIME, DAY_SHIFT_START_TIME, AFTERNOON_SHIFT_START_TIME, NIGHT_SHIFT_START_TIME },
    };

    private List<String> locations = List.of("Ambulatory care", "Critical care", "Pediatric care");
    private List<String> requiredSkills = List.of("Doctor", "Nurse");
    private List<String> optionalSkills = List.of("Anaesthetics", "Cardiology");
    private int daysInSchedule = 14;
    private int employeeCount = 15;
    private long randomSeed = DEFAULT_RANDOM_SEED;

    private DemoDataBuilder() {
    }

    public static DemoDataBuilder builder() {
        return new DemoDataBuilder();
    }

    public DemoDataBuilder setLocations(List<String> locations) {
        this.locations = locations;
        return this;
    }

    public DemoDataBuilder setRequiredSkills(List<String> requiredSkills) {
        this.requiredSkills = requiredSkills;
        return this;
    }

    public DemoDataBuilder setOptionalSkills(List<String> optionalSkills) {
        this.optionalSkills = optionalSkills;
        return this;
    }

    public DemoDataBuilder setDaysInSchedule(int daysInSchedule) {
        this.daysInSchedule = daysInSchedule;
        return this;
    }

    public DemoDataBuilder setEmployeeCount(int employeeCount) {
        this.employeeCount = employeeCount;
        return this;
    }

    public DemoDataBuilder setRandomSeed(long randomSeed) {
        this.randomSeed = randomSeed;
        return this;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public EmployeeScheduleInput build() {
        Random random = new Random(randomSeed);
        LocalDate startDate =
                LocalDate.now(ZoneId.systemDefault()).with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));

        Map<String, List<LocalTime>> locationToShiftStartTimes = new HashMap<>();
        int shiftTemplateIndex = 0;
        for (String location : locations) {
            locationToShiftStartTimes.put(location, List.of(SHIFT_START_TIMES_COMBOS[shiftTemplateIndex]));
            shiftTemplateIndex = (shiftTemplateIndex + 1) % SHIFT_START_TIMES_COMBOS.length;
        }

        List<String> namePermutations = joinAllCombinations(FIRST_NAMES, LAST_NAMES);
        Collections.shuffle(namePermutations, random);

        List<EmployeeDTO> employees = new ArrayList<>();
        Map<String, Set<String>> employeeUnavailableDates = new HashMap<>();
        Map<String, Set<String>> employeeUndesiredDates = new HashMap<>();
        Map<String, Set<String>> employeeDesiredDates = new HashMap<>();

        for (int i = 0; i < employeeCount; i++) {
            String employeeId = namePermutations.get(i);
            Set<String> skills = new LinkedHashSet<>();
            skills.add(pickRandom(requiredSkills, random));
            if (random.nextBoolean()) {
                skills.add(pickRandom(optionalSkills, random));
            }
            employees.add(new EmployeeDTO(employeeId, List.copyOf(skills), List.of(), List.of(), List.of()));
            employeeUnavailableDates.put(employeeId, new HashSet<>());
            employeeUndesiredDates.put(employeeId, new HashSet<>());
            employeeDesiredDates.put(employeeId, new HashSet<>());
        }

        for (int i = 0; i < daysInSchedule; i++) {
            LocalDate date = startDate.plusDays(i);
            int availabilityCount = 1 + random.nextInt(3);
            List<EmployeeDTO> shuffled = new ArrayList<>(employees);
            Collections.shuffle(shuffled, random);
            for (int j = 0; j < Math.min(availabilityCount, shuffled.size()); j++) {
                String empId = shuffled.get(j).id();
                switch (random.nextInt(3)) {
                    case 0 -> employeeUnavailableDates.get(empId).add(date.toString());
                    case 1 -> employeeUndesiredDates.get(empId).add(date.toString());
                    case 2 -> employeeDesiredDates.get(empId).add(date.toString());
                    default -> throw new IllegalStateException("Unreachable");
                }
            }
        }

        List<EmployeeDTO> finalEmployees = employees.stream().map(e -> new EmployeeDTO(
                e.id(), e.skills(),
                new ArrayList<>(employeeUnavailableDates.get(e.id())),
                new ArrayList<>(employeeUndesiredDates.get(e.id())),
                new ArrayList<>(employeeDesiredDates.get(e.id())))).toList();

        Deque<ShiftDTO> shiftQueue = new ArrayDeque<>();
        for (int i = 0; i < daysInSchedule; i++) {
            LocalDate date = startDate.plusDays(i);
            for (String location : locations) {
                List<LocalTime> shiftStartTimes = locationToShiftStartTimes.get(location);
                for (LocalTime startTime : shiftStartTimes) {
                    LocalDateTime shiftStart = date.atTime(startTime);
                    LocalDateTime shiftEnd = shiftStart.plus(SHIFT_LENGTH);
                    int count = random.nextBoolean() ? 1 : 2;
                    for (int k = 0; k < count; k++) {
                        String skill;
                        if (random.nextBoolean()) {
                            skill = pickRandom(requiredSkills, random);
                        } else {
                            skill = pickRandom(optionalSkills, random);
                        }
                        shiftQueue.add(new ShiftDTO("", shiftStart.toString(), shiftEnd.toString(), location, skill,
                                ""));
                    }
                }
            }
        }

        AtomicInteger counter = new AtomicInteger();
        List<ShiftDTO> numberedShifts =
                shiftQueue.stream().map(s -> s.withId(Integer.toString(counter.getAndIncrement()))).toList();

        return new EmployeeScheduleInput(finalEmployees, numberedShifts);
    }

    private <T> T pickRandom(List<T> source, Random random) {
        return source.get(random.nextInt(source.size()));
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
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
                int index = i / sizePerIncrement % partArray.length;
                item.append(' ').append(partArray[index]);
                sizePerIncrement *= partArray.length;
            }
            item.delete(0, 1);
            out.add(item.toString());
        }
        return out;
    }
}
