package org.acme.maintenancescheduling.demo;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import org.acme.maintenancescheduling.domain.Job;
import org.acme.maintenancescheduling.dto.input.CrewInputDTO;
import org.acme.maintenancescheduling.dto.input.JobInputDTO;
import org.acme.maintenancescheduling.dto.input.MaintenanceScheduleInput;
import org.acme.maintenancescheduling.dto.input.WorkCalendarInputDTO;

/**
 * Builds the demo dataset: road maintenance jobs spread over a fixed number of work weeks.
 * <p>
 * The job durations and windows come from a {@link Random} with a fixed seed, so the dataset is
 * reproducible; only the work calendar moves, because it is anchored to the next Monday.
 */
public final class DemoDataBuilder {

    private static final int CREW_LIST_SIZE = 4;
    private static final int WEEK_LIST_SIZE = 12;
    private static final int WORKDAYS_PER_WEEK = 5;
    private static final long RANDOM_SEED = 17L;

    /** At least this many workdays of slack on top of a job's duration, so its window always fits. */
    private static final int MINIMUM_SLACK_IN_WORKDAYS = 5;
    private static final int MAXIMUM_DURATION_IN_WORKDAYS = 10;
    /** Roughly one in ten jobs also blocks the subway, on top of blocking its own area. */
    private static final double SUBWAY_JOB_RATIO = 0.1;
    private static final String SUBWAY_TAG = "Subway";

    private static final String[] CREW_NAMES = { "Alpha crew", "Beta crew", "Gamma crew", "Delta crew", "Epsilon crew" };

    private static final String[] JOB_AREA_NAMES = {
            "Downtown", "Uptown", "Park", "Airport", "Bay", "Hill", "Forest", "Station", "Hospital",
            "Harbor", "Market", "Fort", "Beach", "Garden", "River", "Springs", "Tower", "Mountain" };
    private static final String[] JOB_TARGET_NAMES = { "Street", "Bridge", "Tunnel", "Highway", "Boulevard", "Avenue",
            "Square", "Plaza" };

    private DemoDataBuilder() {
    }

    public static DemoDataBuilder builder() {
        return new DemoDataBuilder();
    }

    public MaintenanceScheduleInput build() {
        // Anchored to the next Monday (never today), so the schedule always starts on a workday.
        LocalDate fromDate = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDate toDate = fromDate.plusWeeks(WEEK_LIST_SIZE);
        WorkCalendarInputDTO workCalendar = new WorkCalendarInputDTO("1", fromDate, toDate);

        List<CrewInputDTO> crews = IntStream.rangeClosed(1, CREW_LIST_SIZE)
                .mapToObj(i -> new CrewInputDTO(Integer.toString(i), CREW_NAMES[i - 1]))
                .toList();

        return new MaintenanceScheduleInput(workCalendar, crews, buildJobs(fromDate, crews.size()));
    }

    private static List<JobInputDTO> buildJobs(LocalDate fromDate, int crewListSize) {
        int workdayTotal = WEEK_LIST_SIZE * WORKDAYS_PER_WEEK;
        // Roughly 60% of the available crew capacity, which leaves the schedule feasible but not trivial.
        int jobListSize = WEEK_LIST_SIZE * crewListSize * 3 / 5;
        int jobAreaTargetLimit = Math.min(JOB_TARGET_NAMES.length, crewListSize * 2);

        Random random = new Random(RANDOM_SEED);
        return IntStream.range(0, jobListSize)
                .mapToObj(i -> {
                    String jobArea = JOB_AREA_NAMES[i / jobAreaTargetLimit];
                    String jobTarget = JOB_TARGET_NAMES[i % jobAreaTargetLimit];
                    // 1 day to 2 workweeks (1 workweek on average)
                    int durationInDays = 1 + random.nextInt(MAXIMUM_DURATION_IN_WORKDAYS);
                    int minMaxBetweenWorkdays = durationInDays + MINIMUM_SLACK_IN_WORKDAYS
                            + random.nextInt(workdayTotal - (durationInDays + MINIMUM_SLACK_IN_WORKDAYS));
                    int minWorkdayOffset = random.nextInt(workdayTotal - minMaxBetweenWorkdays + 1);
                    int minIdealEndBetweenWorkdays = minMaxBetweenWorkdays - 1 - random.nextInt(4);
                    LocalDate minStartDate = Job.calculateEndDate(fromDate, minWorkdayOffset);
                    LocalDate maxEndDate = Job.calculateEndDate(minStartDate, minMaxBetweenWorkdays);
                    LocalDate idealEndDate = Job.calculateEndDate(minStartDate, minIdealEndBetweenWorkdays);
                    List<String> tags = random.nextDouble() < SUBWAY_JOB_RATIO
                            ? List.of(jobArea, SUBWAY_TAG)
                            : List.of(jobArea);
                    return new JobInputDTO(Integer.toString(i), jobArea + " " + jobTarget, durationInDays,
                            minStartDate, maxEndDate, idealEndDate, tags, null, null);
                })
                .toList();
    }
}
