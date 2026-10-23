package org.acme.employeescheduling.demo;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.SequencedSet;

import org.acme.employeescheduling.dto.input.EmployeeInputDTO;
import org.acme.employeescheduling.dto.input.EmployeeScheduleInput;
import org.acme.employeescheduling.dto.input.ShiftInputDTO;

/**
 * Builds the demo datasets: a {@link DemoDataSize#BASIC small} one of 15 employees at three locations over two weeks,
 * and a {@link DemoDataSize#LARGE large} one of 50 employees at seven locations over four weeks. The schedule starts
 * on the upcoming Monday and every shift starts unassigned.
 */
public final class DemoDataBuilder {
    private static final int WEEK_DAYS = 7;
    private static final int BASIC_EMPLOYEE_COUNT = 15;
    private static final int LARGE_EMPLOYEE_COUNT = 15;

    public enum DemoDataSize {

        BASIC(new DemoDataParameters(
                List.of("Ambulatory care", "Critical care", "Pediatric care"),
                List.of("Doctor", "Nurse"),
                List.of("Anaesthetics", "Cardiology"),
                WEEK_DAYS * 2,
                BASIC_EMPLOYEE_COUNT,
                List.of(new CountDistribution(1, 3), new CountDistribution(2, 1)),
                List.of(new CountDistribution(1, 0.9), new CountDistribution(2, 0.1)),
                List.of(new CountDistribution(1, 4), new CountDistribution(2, 3), new CountDistribution(3, 2),
                        new CountDistribution(4, 1)))),
        LARGE(new DemoDataParameters(
                List.of("Ambulatory care", "Neurology", "Critical care", "Pediatric care", "Surgery", "Radiology",
                        "Outpatient"),
                List.of("Doctor", "Nurse"),
                List.of("Anaesthetics", "Cardiology", "Radiology"),
                WEEK_DAYS * 4,
                LARGE_EMPLOYEE_COUNT,
                List.of(new CountDistribution(1, 3), new CountDistribution(2, 1)),
                List.of(new CountDistribution(1, 0.5), new CountDistribution(2, 0.3), new CountDistribution(3, 0.2)),
                List.of(new CountDistribution(5, 4), new CountDistribution(10, 3), new CountDistribution(15, 2),
                        new CountDistribution(20, 1))));

        private final DemoDataParameters parameters;

        DemoDataSize(DemoDataParameters parameters) {
            this.parameters = parameters;
        }
    }

    /**
     * @param requiredSkills every employee has exactly one of these
     * @param optionalSkills every employee has a few of these on top
     * @param optionalSkillDistribution how many optional skills an employee has
     * @param shiftCountDistribution how many shifts a location has per shift start time
     * @param availabilityCountDistribution how many employees have an unavailable, undesired or desired day per day
     */
    private record DemoDataParameters(List<String> locations, List<String> requiredSkills,
            List<String> optionalSkills, int daysInSchedule, int employeeCount,
            List<CountDistribution> optionalSkillDistribution, List<CountDistribution> shiftCountDistribution,
            List<CountDistribution> availabilityCountDistribution) {
    }

    private record CountDistribution(int count, double weight) {
    }

    private static final long RANDOM_SEED = 0;
    private static final String[] FIRST_NAMES = { "Amy", "Beth", "Carl", "Dan", "Elsa", "Flo", "Gus", "Hugo", "Ivy",
            "Jay" };
    private static final String[] LAST_NAMES = { "Cole", "Fox", "Green", "Jones", "King", "Li", "Poe", "Rye", "Smith",
            "Watt" };
    private static final Duration SHIFT_LENGTH = Duration.ofHours(8);
    private static final LocalTime MORNING_SHIFT_START_TIME = LocalTime.of(6, 0);
    private static final LocalTime DAY_SHIFT_START_TIME = LocalTime.of(9, 0);
    private static final LocalTime AFTERNOON_SHIFT_START_TIME = LocalTime.of(14, 0);
    private static final LocalTime NIGHT_SHIFT_START_TIME = LocalTime.of(22, 0);
    /** The locations take turns using these shift start times. */
    private static final List<List<LocalTime>> SHIFT_START_TIMES_COMBOS = List.of(
            List.of(MORNING_SHIFT_START_TIME, AFTERNOON_SHIFT_START_TIME),
            List.of(MORNING_SHIFT_START_TIME, AFTERNOON_SHIFT_START_TIME, NIGHT_SHIFT_START_TIME),
            List.of(MORNING_SHIFT_START_TIME, DAY_SHIFT_START_TIME, AFTERNOON_SHIFT_START_TIME,
                    NIGHT_SHIFT_START_TIME));

    private DemoDataSize size = DemoDataSize.BASIC;

    private DemoDataBuilder() {
    }

    public static DemoDataBuilder builder() {
        return new DemoDataBuilder();
    }

    public DemoDataBuilder withSize(DemoDataSize size) {
        this.size = size;
        return this;
    }

    public EmployeeScheduleInput build() {
        DemoDataParameters parameters = size.parameters;
        LocalDate startDate = LocalDate.now(ZoneOffset.UTC).with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
        Random random = new Random(RANDOM_SEED);

        List<String> names = joinAllCombinations(FIRST_NAMES, LAST_NAMES);
        Collections.shuffle(names, random);
        List<EmployeeBuilder> employees = new ArrayList<>(parameters.employeeCount());
        for (int i = 0; i < parameters.employeeCount(); i++) {
            SequencedSet<String> skills =
                    pickSubset(parameters.optionalSkills(), random, parameters.optionalSkillDistribution());
            skills.add(pickRandom(parameters.requiredSkills(), random));
            employees.add(new EmployeeBuilder(names.get(i), List.copyOf(skills)));
        }

        List<ShiftInputDTO> shifts = new ArrayList<>();
        for (int i = 0; i < parameters.daysInSchedule(); i++) {
            LocalDate date = startDate.plusDays(i);
            for (EmployeeBuilder employee : pickSubset(employees, random,
                    parameters.availabilityCountDistribution())) {
                switch (random.nextInt(3)) {
                    case 0 -> employee.unavailableDates.add(date);
                    case 1 -> employee.undesiredDates.add(date);
                    default -> employee.desiredDates.add(date);
                }
            }
            for (int locationIndex = 0; locationIndex < parameters.locations().size(); locationIndex++) {
                String location = parameters.locations().get(locationIndex);
                for (LocalTime startTime : SHIFT_START_TIMES_COMBOS
                        .get(locationIndex % SHIFT_START_TIMES_COMBOS.size())) {
                    OffsetDateTime start = OffsetDateTime.of(date, startTime, ZoneOffset.UTC);
                    int shiftCount = pickCount(random, parameters.shiftCountDistribution());
                    for (int j = 0; j < shiftCount; j++) {
                        String requiredSkill = random.nextBoolean()
                                ? pickRandom(parameters.requiredSkills(), random)
                                : pickRandom(parameters.optionalSkills(), random);
                        shifts.add(new ShiftInputDTO(String.valueOf(shifts.size()), start, start.plus(SHIFT_LENGTH),
                                location, requiredSkill, null));
                    }
                }
            }
        }
        return new EmployeeScheduleInput(employees.stream().map(EmployeeBuilder::build).toList(),
                List.copyOf(shifts));
    }

    private static <T> T pickRandom(List<T> source, Random random) {
        return source.get(random.nextInt(source.size()));
    }

    private static int pickCount(Random random, List<CountDistribution> countDistribution) {
        double probabilitySum = 0;
        for (CountDistribution possibility : countDistribution) {
            probabilitySum += possibility.weight();
        }
        double choice = random.nextDouble(probabilitySum);
        int index = 0;
        while (choice >= countDistribution.get(index).weight()) {
            choice -= countDistribution.get(index).weight();
            index++;
        }
        return countDistribution.get(index).count();
    }

    private static <T> SequencedSet<T> pickSubset(List<T> source, Random random,
            List<CountDistribution> countDistribution) {
        int count = pickCount(random, countDistribution);
        List<T> items = new ArrayList<>(source);
        Collections.shuffle(items, random);
        return new LinkedHashSet<>(items.subList(0, count));
    }

    private static List<String> joinAllCombinations(String[] firstNames, String[] lastNames) {
        List<String> names = new ArrayList<>(firstNames.length * lastNames.length);
        for (String lastName : lastNames) {
            for (String firstName : firstNames) {
                names.add(firstName + " " + lastName);
            }
        }
        return names;
    }

    /**
     * Collects the dates of one employee while the days are generated one after another.
     */
    private static final class EmployeeBuilder {

        private final String name;
        private final List<String> skills;
        private final List<LocalDate> unavailableDates = new ArrayList<>();
        private final List<LocalDate> undesiredDates = new ArrayList<>();
        private final List<LocalDate> desiredDates = new ArrayList<>();

        private EmployeeBuilder(String name, List<String> skills) {
            this.name = name;
            this.skills = skills;
        }

        private EmployeeInputDTO build() {
            return new EmployeeInputDTO(name, skills, List.copyOf(unavailableDates), List.copyOf(undesiredDates),
                    List.copyOf(desiredDates));
        }
    }
}
