package org.acme.maintenancescheduling.demo;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.acme.maintenancescheduling.domain.Job;
import org.acme.maintenancescheduling.dto.CrewDTO;
import org.acme.maintenancescheduling.dto.JobDTO;
import org.acme.maintenancescheduling.dto.MaintenanceScheduleInput;
import org.acme.maintenancescheduling.dto.WorkCalendarDTO;

public final class DemoDataBuilder {

    private static final String UNASSIGNED = "";
    private static final long RANDOM_SEED = 17L;
    private static final int MINIMUM_COUNT = 1;

    private final List<String> crewNames = new ArrayList<>();
    private int weekListSize = 8;

    private DemoDataBuilder() {
    }

    public static DemoDataBuilder builder() {
        return new DemoDataBuilder();
    }

    public DemoDataBuilder setWeekListSize(int weekListSize) {
        this.weekListSize = weekListSize;
        return this;
    }

    public DemoDataBuilder addCrew(String name) {
        crewNames.add(name);
        return this;
    }

    public MaintenanceScheduleInput build() {
        if (weekListSize < MINIMUM_COUNT) {
            throw new IllegalStateException("Number of weeks (" + weekListSize + ") must be greater than zero.");
        }
        if (crewNames.isEmpty()) {
            throw new IllegalStateException("At least one crew must be defined.");
        }

        List<CrewDTO> crews = new ArrayList<>();
        for (int i = 0; i < crewNames.size(); i++) {
            crews.add(new CrewDTO(Integer.toString(i + 1), crewNames.get(i)));
        }

        LocalDate fromDate = LocalDate.now(ZoneId.systemDefault()).with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDate toDate = fromDate.plusWeeks(weekListSize);
        WorkCalendarDTO workCalendar = new WorkCalendarDTO("1", fromDate.toString(), toDate.toString());

        List<JobDTO> jobs = buildJobs(fromDate, crews.size());
        return new MaintenanceScheduleInput(workCalendar, crews, jobs);
    }

    private List<JobDTO> buildJobs(LocalDate fromDate, int crewCount) {
        int workdayTotal = weekListSize * 5;
        final String[] jobAreaNames = {
                "Downtown", "Uptown", "Park", "Airport", "Bay", "Hill", "Forest", "Station", "Hospital",
                "Harbor", "Market", "Fort", "Beach", "Garden", "River", "Springs", "Tower", "Mountain" };
        final String[] jobTargetNames = { "Street", "Bridge", "Tunnel", "Highway", "Boulevard", "Avenue",
                "Square", "Plaza" };

        List<JobDTO> jobs = new ArrayList<>();
        int jobListSize = weekListSize * crewCount * 3 / 5;
        int jobAreaTargetLimit = Math.min(jobTargetNames.length, crewCount * 2);
        Random random = new Random(RANDOM_SEED);
        for (int i = 0; i < jobListSize; i++) {
            // 1 day to 2 workweeks (1 workweek on average)
            int durationInDays = 1 + random.nextInt(10);
            int minMaxBetweenWorkdays = durationInDays + 5 // at least 5 days of flexibility
                    + random.nextInt(workdayTotal - (durationInDays + 5));
            int minWorkdayOffset = random.nextInt(workdayTotal - minMaxBetweenWorkdays + 1);
            int minIdealEndBetweenWorkdays = minMaxBetweenWorkdays - 1 - random.nextInt(4);
            LocalDate minStartDate = Job.calculateEndDate(fromDate, minWorkdayOffset);
            LocalDate maxEndDate = Job.calculateEndDate(minStartDate, minMaxBetweenWorkdays);
            LocalDate idealEndDate = Job.calculateEndDate(minStartDate, minIdealEndBetweenWorkdays);
            String jobArea = jobAreaNames[i / jobAreaTargetLimit];
            String jobTarget = jobTargetNames[i % jobAreaTargetLimit];
            List<String> tags = random.nextDouble() < 0.1
                    ? List.of(jobArea, "Subway")
                    : List.of(jobArea);
            jobs.add(new JobDTO(Integer.toString(i), jobArea + " " + jobTarget, durationInDays,
                    minStartDate.toString(), maxEndDate.toString(), idealEndDate.toString(), tags,
                    UNASSIGNED, UNASSIGNED, UNASSIGNED));
        }
        return jobs;
    }
}
