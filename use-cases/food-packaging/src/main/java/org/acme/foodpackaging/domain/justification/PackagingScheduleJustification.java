package org.acme.foodpackaging.domain.justification;

import java.time.Duration;
import java.time.OffsetDateTime;

import ai.timefold.solver.service.definition.api.ModelConstraintJustification;

import org.acme.foodpackaging.domain.Job;
import org.acme.foodpackaging.domain.Line;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Common contract for every food packaging justification.
 * <p>
 * Each implementation is a record dedicated to exactly one thing that is being justified, so that the Timefold Platform can
 * both render a human-readable {@link #getDescription() description} and expose the individual facts behind it through the
 * OpenAPI schema.
 * <p>
 * Every implementation must be listed in the {@link Schema#oneOf()} below, otherwise it does not show up in the generated
 * OpenAPI schema.
 */
@Schema(description = "Explains why a food packaging constraint was matched.",
        oneOf = {
                // Hard constraints
                PackagingScheduleJustification.JobEndsAfterMaxEndTimeJustification.class,
                PackagingScheduleJustification.OperatorCleaningOverlapJustification.class,

                // Medium constraints
                PackagingScheduleJustification.JobEndsAfterIdealEndTimeJustification.class,
                PackagingScheduleJustification.UnassignedJobJustification.class,

                // Soft constraints
                PackagingScheduleJustification.LineMakespanJustification.class
        })
public interface PackagingScheduleJustification extends ModelConstraintJustification {

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

    private static long minutesBetween(OffsetDateTime from, OffsetDateTime to) {
        return Duration.between(from, to).toMinutes();
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    @Schema(description = "A job finishes after the maximum end time it was given.",
            allOf = { PackagingScheduleJustification.class })
    record JobEndsAfterMaxEndTimeJustification(
            @Schema(description = "The id of the job.") String job,
            @Schema(description = "The id of the line the job is produced on.") String line,
            @Schema(description = "The time at which the job actually finishes.") OffsetDateTime endDateTime,
            @Schema(description = "The latest time at which the job may finish.") OffsetDateTime maxEndTime,
            @Schema(description = "The number of minutes by which the job finishes too late.") long minutesLate)
            implements
                PackagingScheduleJustification {

        public static JobEndsAfterMaxEndTimeJustification of(Job job) {
            return new JobEndsAfterMaxEndTimeJustification(job.getId(), job.getLine().getId(), job.getEndDateTime(),
                    job.getMaxEndTime(), minutesBetween(job.getMaxEndTime(), job.getEndDateTime()));
        }

        @Override
        public String getDescription() {
            return "Job '%s' on line '%s' finishes at %s, which is %d minutes after its maximum end time %s."
                    .formatted(job, line, endDateTime, minutesLate, maxEndTime);
        }
    }

    @Schema(description = "One operator has to clean two lines at overlapping times.",
            allOf = { PackagingScheduleJustification.class })
    record OperatorCleaningOverlapJustification(
            @Schema(description = "The id of the operator who has to do both cleanings.") String operator,
            @Schema(description = "The id of the first job being cleaned up for.") String job,
            @Schema(description = "The id of the line of the first job.") String line,
            @Schema(description = "The id of the second job being cleaned up for.") String otherJob,
            @Schema(description = "The id of the line of the second job.") String otherLine,
            @Schema(description = "The number of minutes during which both cleanings overlap.") long overlapInMinutes)
            implements
                PackagingScheduleJustification {

        public static OperatorCleaningOverlapJustification of(Job job, Job otherJob, long overlapInMinutes) {
            return new OperatorCleaningOverlapJustification(job.getLineOperator().id(), job.getId(),
                    job.getLine().getId(), otherJob.getId(), otherJob.getLine().getId(), overlapInMinutes);
        }

        @Override
        public String getDescription() {
            return "Operator '%s' has to clean line '%s' for job '%s' and line '%s' for job '%s' at the same time, overlapping for %d minutes."
                    .formatted(operator, line, job, otherLine, otherJob, overlapInMinutes);
        }
    }

    // ************************************************************************
    // Medium constraints
    // ************************************************************************

    @Schema(description = "A job finishes after the time at which it would ideally have been finished.",
            allOf = { PackagingScheduleJustification.class })
    record JobEndsAfterIdealEndTimeJustification(
            @Schema(description = "The id of the job.") String job,
            @Schema(description = "The id of the line the job is produced on.") String line,
            @Schema(description = "The time at which the job actually finishes.") OffsetDateTime endDateTime,
            @Schema(description = "The time at which the job would ideally finish.") OffsetDateTime idealEndTime,
            @Schema(description = "The number of minutes by which the job misses its ideal end time.") long minutesLate)
            implements
                PackagingScheduleJustification {

        public static JobEndsAfterIdealEndTimeJustification of(Job job) {
            return new JobEndsAfterIdealEndTimeJustification(job.getId(), job.getLine().getId(), job.getEndDateTime(),
                    job.getIdealEndTime(), minutesBetween(job.getIdealEndTime(), job.getEndDateTime()));
        }

        @Override
        public String getDescription() {
            return "Job '%s' on line '%s' finishes at %s, which is %d minutes after its ideal end time %s."
                    .formatted(job, line, endDateTime, minutesLate, idealEndTime);
        }
    }

    @Schema(description = "A job is not produced on any line.", allOf = { PackagingScheduleJustification.class })
    record UnassignedJobJustification(
            @Schema(description = "The id of the unassigned job.") String job,
            @Schema(description = "The number of minutes the job would take to produce.") long durationInMinutes)
            implements
                PackagingScheduleJustification {

        public static UnassignedJobJustification of(Job job) {
            return new UnassignedJobJustification(job.getId(), job.getDuration().toMinutes());
        }

        @Override
        public String getDescription() {
            return "Job '%s', which takes %d minutes to produce, is not assigned to any line."
                    .formatted(job, durationInMinutes);
        }
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    @Schema(description = "A line keeps producing until well after it started, which is penalized quadratically so the "
            + "work is spread evenly over the lines instead of piling up on one of them.",
            allOf = { PackagingScheduleJustification.class })
    record LineMakespanJustification(
            @Schema(description = "The id of the line.") String line,
            @Schema(description = "The id of the last job produced on the line.") String lastJob,
            @Schema(description = "The time at which the line starts producing.") OffsetDateTime startDateTime,
            @Schema(description = "The time at which the line finishes its last job.") OffsetDateTime endDateTime,
            @Schema(description = "The number of minutes the line keeps producing.") long makespanInMinutes)
            implements
                PackagingScheduleJustification {

        public static LineMakespanJustification of(Job lastJob) {
            Line line = lastJob.getLine();
            return new LineMakespanJustification(line.getId(), lastJob.getId(), line.getStartDateTime(),
                    lastJob.getEndDateTime(),
                    minutesBetween(line.getStartDateTime(), lastJob.getEndDateTime()));
        }

        @Override
        public String getDescription() {
            return "Line '%s' keeps producing for %d minutes, from %s until its last job '%s' finishes at %s."
                    .formatted(line, makespanInMinutes, startDateTime, lastJob, endDateTime);
        }
    }
}
