package org.acme.maintenancescheduling.domain.justification;

import static java.time.temporal.ChronoUnit.DAYS;

import java.time.LocalDate;
import java.util.List;

import ai.timefold.solver.service.definition.api.ModelConstraintJustification;

import org.acme.maintenancescheduling.domain.Job;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Common contract for every maintenance scheduling justification.
 * <p>
 * Each implementation is a record dedicated to exactly one thing that is being justified, so that the Timefold Platform can
 * both render a human-readable {@link #getDescription() description} and expose the individual facts behind it through the
 * OpenAPI schema.
 * <p>
 * Every implementation must be listed in the {@link Schema#oneOf()} below, otherwise it does not show up in the generated
 * OpenAPI schema.
 */
@Schema(description = "Explains why a maintenance scheduling constraint was matched.",
        oneOf = {
                // Hard constraints
                MaintenanceScheduleJustification.CrewConflictJustification.class,
                MaintenanceScheduleJustification.MinStartDateJustification.class,
                MaintenanceScheduleJustification.MaxEndDateJustification.class,

                // Soft constraints
                MaintenanceScheduleJustification.BeforeIdealEndDateJustification.class,
                MaintenanceScheduleJustification.AfterIdealEndDateJustification.class,
                MaintenanceScheduleJustification.TagConflictJustification.class
        })
public interface MaintenanceScheduleJustification extends ModelConstraintJustification {

    /**
     * @return never null, a human-readable explanation of the constraint match
     */
    String getDescription();

    /**
     * Exposes the description as the {@code description} property of {@link ModelConstraintJustification}.
     */
    default String description() {
        return getDescription();
    }

    @Schema(description = "One crew is working on two jobs whose workdays overlap.",
            allOf = { MaintenanceScheduleJustification.class })
    record CrewConflictJustification(
            @Schema(description = "The id of the crew assigned to both jobs.") String crew,
            @Schema(description = "The id of the first job.") String job,
            @Schema(description = "The id of the second job.") String otherJob,
            @Schema(description = "The number of days during which both jobs overlap.") long overlapInDays)
            implements
                MaintenanceScheduleJustification {

        public static CrewConflictJustification of(Job job, Job otherJob) {
            return new CrewConflictJustification(job.getCrew().id(), job.getId(), otherJob.getId(),
                    job.calculateOverlapInDays(otherJob));
        }

        @Override
        public String getDescription() {
            return "Crew '%s' works on jobs '%s' and '%s' during %d overlapping day(s)."
                    .formatted(crew, job, otherJob, overlapInDays);
        }
    }

    @Schema(description = "A job starts before the date from which it is ready to be worked on.",
            allOf = { MaintenanceScheduleJustification.class })
    record MinStartDateJustification(
            @Schema(description = "The id of the job.") String job,
            @Schema(description = "The earliest date the job may start on.") LocalDate minStartDate,
            @Schema(description = "The date the job is scheduled to start on.") LocalDate startDate,
            @Schema(description = "The number of days the job starts too early.") long daysTooEarly)
            implements
                MaintenanceScheduleJustification {

        public static MinStartDateJustification of(Job job) {
            return new MinStartDateJustification(job.getId(), job.getMinStartDate(), job.getStartDate(),
                    DAYS.between(job.getStartDate(), job.getMinStartDate()));
        }

        @Override
        public String getDescription() {
            return "Job '%s' starts on %s, which is %d day(s) before its minimum start date %s."
                    .formatted(job, startDate, daysTooEarly, minStartDate);
        }
    }

    @Schema(description = "A job ends after the date on which it is due.",
            allOf = { MaintenanceScheduleJustification.class })
    record MaxEndDateJustification(
            @Schema(description = "The id of the job.") String job,
            @Schema(description = "The latest date the job may end on.") LocalDate maxEndDate,
            @Schema(description = "The date the job is scheduled to end on.") LocalDate endDate,
            @Schema(description = "The number of days the job ends too late.") long daysTooLate)
            implements
                MaintenanceScheduleJustification {

        public static MaxEndDateJustification of(Job job) {
            return new MaxEndDateJustification(job.getId(), job.getMaxEndDate(), job.getEndDate(),
                    DAYS.between(job.getMaxEndDate(), job.getEndDate()));
        }

        @Override
        public String getDescription() {
            return "Job '%s' ends on %s, which is %d day(s) after its maximum end date %s."
                    .formatted(job, endDate, daysTooLate, maxEndDate);
        }
    }

    @Schema(description = "A job ends before its ideal end date, so its maintenance has to be repeated sooner.",
            allOf = { MaintenanceScheduleJustification.class })
    record BeforeIdealEndDateJustification(
            @Schema(description = "The id of the job.") String job,
            @Schema(description = "The ideal end date of the job.") LocalDate idealEndDate,
            @Schema(description = "The date the job is scheduled to end on.") LocalDate endDate,
            @Schema(description = "The number of days the job ends before its ideal end date.") long daysEarly)
            implements
                MaintenanceScheduleJustification {

        public static BeforeIdealEndDateJustification of(Job job) {
            return new BeforeIdealEndDateJustification(job.getId(), job.getIdealEndDate(), job.getEndDate(),
                    DAYS.between(job.getEndDate(), job.getIdealEndDate()));
        }

        @Override
        public String getDescription() {
            return "Job '%s' ends on %s, which is %d day(s) before its ideal end date %s."
                    .formatted(job, endDate, daysEarly, idealEndDate);
        }
    }

    @Schema(description = "A job ends after its ideal end date, so it risks running over its due date.",
            allOf = { MaintenanceScheduleJustification.class })
    record AfterIdealEndDateJustification(
            @Schema(description = "The id of the job.") String job,
            @Schema(description = "The ideal end date of the job.") LocalDate idealEndDate,
            @Schema(description = "The date the job is scheduled to end on.") LocalDate endDate,
            @Schema(description = "The number of days the job ends after its ideal end date.") long daysLate)
            implements
                MaintenanceScheduleJustification {

        public static AfterIdealEndDateJustification of(Job job) {
            return new AfterIdealEndDateJustification(job.getId(), job.getIdealEndDate(), job.getEndDate(),
                    DAYS.between(job.getIdealEndDate(), job.getEndDate()));
        }

        @Override
        public String getDescription() {
            return "Job '%s' ends on %s, which is %d day(s) after its ideal end date %s."
                    .formatted(job, endDate, daysLate, idealEndDate);
        }
    }

    @Schema(description = "Two jobs that share at least one tag are being worked on at the same time.",
            allOf = { MaintenanceScheduleJustification.class })
    record TagConflictJustification(
            @Schema(description = "The id of the first job.") String job,
            @Schema(description = "The id of the second job.") String otherJob,
            @Schema(description = "The tags both jobs carry.") List<String> sharedTags,
            @Schema(description = "The number of days during which both jobs overlap.") long overlapInDays)
            implements
                MaintenanceScheduleJustification {

        public static TagConflictJustification of(Job job, Job otherJob) {
            return new TagConflictJustification(job.getId(), otherJob.getId(),
                    List.copyOf(job.calculateSharedTags(otherJob)), job.calculateOverlapInDays(otherJob));
        }

        @Override
        public String getDescription() {
            return "Jobs '%s' and '%s' share tag(s) [%s] during %d overlapping day(s)."
                    .formatted(job, otherJob, String.join(", ", sharedTags), overlapInDays);
        }
    }
}
