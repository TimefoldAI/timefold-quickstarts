package org.acme.maintenancescheduling.support;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedSet;

import org.acme.maintenancescheduling.domain.Crew;
import org.acme.maintenancescheduling.domain.Job;
import org.acme.maintenancescheduling.dto.input.CrewInputDTO;
import org.acme.maintenancescheduling.dto.input.JobInputDTO;
import org.acme.maintenancescheduling.dto.input.MaintenanceScheduleInput;
import org.acme.maintenancescheduling.dto.input.WorkCalendarInputDTO;

// To keep our production classes as simple as possible, we've added these methods to help construct the data needed for testing.
public final class TestHelper {

    /** A Monday, so that a work calendar anchored to it starts on a workday. */
    public static final LocalDate FIRST_MONDAY = LocalDate.of(2024, 1, 1);

    private static final int PROBLEM_WEEK_COUNT = 4;

    private TestHelper() {
    }

    public static MaintenanceScheduleInput input(WorkCalendarInputDTO workCalendar, List<CrewInputDTO> crews,
            List<JobInputDTO> jobs) {
        return new MaintenanceScheduleInput(workCalendar, crews, jobs);
    }

    /**
     * @return a small but feasible problem: every job fits on one of the crews well inside its window
     */
    public static MaintenanceScheduleInput createProblem() {
        WorkCalendarInputDTO workCalendar = aWorkCalendarDTO("1").weekCount(PROBLEM_WEEK_COUNT).build();
        List<CrewInputDTO> crews = List.of(
                aCrewDTO("1").name("Alpha crew").build(),
                aCrewDTO("2").name("Beta crew").build());
        List<JobInputDTO> jobs = List.of(
                aJobDTO("1").name("Downtown Street").tags(List.of("Downtown")).build(),
                aJobDTO("2").name("Uptown Bridge").tags(List.of("Uptown")).build(),
                aJobDTO("3").name("Park Tunnel").tags(List.of("Park")).build(),
                aJobDTO("4").name("Airport Highway").tags(List.of("Airport")).build());
        return input(workCalendar, crews, jobs);
    }

    public static WorkCalendarDTOBuilder aWorkCalendarDTO(String id) {
        return new WorkCalendarDTOBuilder(id);
    }

    public static CrewDTOBuilder aCrewDTO(String id) {
        return new CrewDTOBuilder(id);
    }

    public static JobDTOBuilder aJobDTO(String id) {
        return new JobDTOBuilder(id);
    }

    public static CrewBuilder aCrew(String id) {
        return new CrewBuilder(id);
    }

    public static JobBuilder aJob(String id) {
        return new JobBuilder(id);
    }

    @SafeVarargs
    public static <T> SequencedSet<T> sequencedSet(T... values) {
        return new LinkedHashSet<>(List.of(values));
    }

    public static final class WorkCalendarDTOBuilder {

        private final String id;
        private LocalDate fromDate = FIRST_MONDAY;
        private int weekCount = PROBLEM_WEEK_COUNT;
        private LocalDate toDate;

        private WorkCalendarDTOBuilder(String id) {
            this.id = id;
        }

        public WorkCalendarDTOBuilder fromDate(LocalDate fromDate) {
            this.fromDate = fromDate;
            return this;
        }

        public WorkCalendarDTOBuilder weekCount(int weekCount) {
            this.weekCount = weekCount;
            return this;
        }

        /** Overrides the {@link #weekCount(int) week count}, for windows that are not a whole number of weeks. */
        public WorkCalendarDTOBuilder toDate(LocalDate toDate) {
            this.toDate = toDate;
            return this;
        }

        public WorkCalendarInputDTO build() {
            return new WorkCalendarInputDTO(id, fromDate, toDate != null ? toDate : fromDate.plusWeeks(weekCount));
        }
    }

    public static final class CrewDTOBuilder {

        private final String id;
        private String name;

        private CrewDTOBuilder(String id) {
            this.id = id;
            this.name = "Crew " + id;
        }

        public CrewDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public CrewInputDTO build() {
            return new CrewInputDTO(id, name);
        }
    }

    public static final class JobDTOBuilder {

        private static final int DEFAULT_DURATION_IN_DAYS = 3;

        private final String id;
        private String name;
        private Integer durationInDays = DEFAULT_DURATION_IN_DAYS;
        private LocalDate minStartDate = FIRST_MONDAY;
        private LocalDate maxEndDate = FIRST_MONDAY.plusWeeks(PROBLEM_WEEK_COUNT);
        private LocalDate idealEndDate = Job.calculateEndDate(FIRST_MONDAY, 10);
        private List<String> tags = List.of();
        private String crewId;
        private LocalDate startDate;

        private JobDTOBuilder(String id) {
            this.id = id;
            this.name = "Job " + id;
        }

        public JobDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public JobDTOBuilder durationInDays(Integer durationInDays) {
            this.durationInDays = durationInDays;
            return this;
        }

        public JobDTOBuilder minStartDate(LocalDate minStartDate) {
            this.minStartDate = minStartDate;
            return this;
        }

        public JobDTOBuilder maxEndDate(LocalDate maxEndDate) {
            this.maxEndDate = maxEndDate;
            return this;
        }

        public JobDTOBuilder idealEndDate(LocalDate idealEndDate) {
            this.idealEndDate = idealEndDate;
            return this;
        }

        public JobDTOBuilder tags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public JobDTOBuilder crewId(String crewId) {
            this.crewId = crewId;
            return this;
        }

        public JobDTOBuilder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public JobInputDTO build() {
            return new JobInputDTO(id, name, durationInDays, minStartDate, maxEndDate, idealEndDate, tags, crewId,
                    startDate);
        }
    }

    public static final class CrewBuilder {

        private final String id;
        private String name;

        private CrewBuilder(String id) {
            this.id = id;
            this.name = "Crew " + id;
        }

        public CrewBuilder name(String name) {
            this.name = name;
            return this;
        }

        public Crew build() {
            return new Crew(id, name);
        }
    }

    public static final class JobBuilder {

        private final String id;
        private String name;
        private int durationInDays = 1;
        private LocalDate minStartDate;
        private LocalDate maxEndDate;
        private LocalDate idealEndDate;
        private SequencedSet<String> tags = sequencedSet();
        private CrewBuilder crew;
        private LocalDate startDate;

        private JobBuilder(String id) {
            this.id = id;
            this.name = "Job " + id;
        }

        public JobBuilder name(String name) {
            this.name = name;
            return this;
        }

        public JobBuilder durationInDays(int durationInDays) {
            this.durationInDays = durationInDays;
            return this;
        }

        public JobBuilder minStartDate(LocalDate minStartDate) {
            this.minStartDate = minStartDate;
            return this;
        }

        public JobBuilder maxEndDate(LocalDate maxEndDate) {
            this.maxEndDate = maxEndDate;
            return this;
        }

        public JobBuilder idealEndDate(LocalDate idealEndDate) {
            this.idealEndDate = idealEndDate;
            return this;
        }

        public JobBuilder tags(SequencedSet<String> tags) {
            this.tags = tags;
            return this;
        }

        public JobBuilder crew(CrewBuilder crew) {
            this.crew = crew;
            return this;
        }

        public JobBuilder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        /**
         * ConstraintVerifier does not run the shadow variable suppliers, so the canonical constructor
         * derives the endDate shadow the way the solver would.
         */
        public Job build() {
            return new Job(id, name, durationInDays, minStartDate, maxEndDate, idealEndDate, tags,
                    crew == null ? null : crew.build(), startDate);
        }
    }
}
