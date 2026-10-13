package org.acme.projectjobschedule.domain.justification;

import ai.timefold.solver.service.definition.api.ModelConstraintJustification;

import org.acme.projectjobschedule.domain.Allocation;
import org.acme.projectjobschedule.domain.Resource;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Common contract for every project job scheduling justification.
 * <p>
 * Each implementation is a record dedicated to exactly one thing that is being justified, so that the Timefold Platform can
 * both render a human-readable {@link #getDescription() description} and expose the individual facts behind it through the
 * OpenAPI schema.
 * <p>
 * Every implementation must be listed in the {@link Schema#oneOf()} below, otherwise it does not show up in the generated
 * OpenAPI schema.
 */
@Schema(description = "Explains why a project job scheduling constraint was matched.",
        oneOf = {
                // Hard constraints
                ProjectJobScheduleJustification.NonRenewableResourceOverCapacityJustification.class,
                ProjectJobScheduleJustification.RenewableResourceOverCapacityJustification.class,

                // Medium constraints
                ProjectJobScheduleJustification.ProjectDelayJustification.class,

                // Soft constraints
                ProjectJobScheduleJustification.MakespanJustification.class
        })
public interface ProjectJobScheduleJustification extends ModelConstraintJustification {

    /**
     * @return never null, a human-readable explanation of the constraint match
     */
    String getDescription();

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    @Schema(description = "The jobs consume more of a non-renewable resource, over the whole schedule, than it has.",
            allOf = { ProjectJobScheduleJustification.class })
    record NonRenewableResourceOverCapacityJustification(
            @Schema(description = "The id of the overbooked resource.") String resource,
            @Schema(description = "The number of units the resource has over the whole schedule.") int capacity,
            @Schema(description = "The number of units the scheduled jobs consume in total.") long totalRequirement)
            implements
                ProjectJobScheduleJustification {

        public static NonRenewableResourceOverCapacityJustification of(Resource resource, long totalRequirement) {
            return new NonRenewableResourceOverCapacityJustification(resource.id(), resource.capacity(),
                    totalRequirement);
        }

        @Override
        public String getDescription() {
            return "The jobs scheduled on non-renewable resource '%s' consume %d units of its capacity of %d."
                    .formatted(resource, totalRequirement, capacity);
        }
    }

    @Schema(description = "The jobs running on one day consume more of a renewable resource than it has per day.",
            allOf = { ProjectJobScheduleJustification.class })
    record RenewableResourceOverCapacityJustification(
            @Schema(description = "The id of the overbooked resource.") String resource,
            @Schema(description = "The day number, counted from the start of the schedule, on which the resource is "
                    + "overbooked.") int date,
            @Schema(description = "The number of units the resource has per day.") int capacity,
            @Schema(description = "The number of units the jobs running on that day consume.") long totalRequirement)
            implements
                ProjectJobScheduleJustification {

        public static RenewableResourceOverCapacityJustification of(Resource resource, int date, long totalRequirement) {
            return new RenewableResourceOverCapacityJustification(resource.id(), date, resource.capacity(),
                    totalRequirement);
        }

        @Override
        public String getDescription() {
            return "The jobs running on day %d consume %d units of renewable resource '%s', which has a capacity of %d per day."
                    .formatted(date, totalRequirement, resource, capacity);
        }
    }

    // ************************************************************************
    // Medium constraints
    // ************************************************************************

    @Schema(description = "A project ends after its critical path end date.",
            allOf = { ProjectJobScheduleJustification.class })
    record ProjectDelayJustification(
            @Schema(description = "The id of the delayed project.") String project,
            @Schema(description = "The id of the project's sink job, whose end marks the end of the project.") String job,
            @Schema(description = "The day number on which the project ends.") int endDate,
            @Schema(description = "The day number on which the project could end at the earliest.") int criticalPathEndDate,
            @Schema(description = "The number of days the project ends past its critical path end date.") int delayInDays)
            implements
                ProjectJobScheduleJustification {

        public static ProjectDelayJustification of(Allocation allocation) {
            return new ProjectDelayJustification(allocation.getProject().id(), allocation.getJob().id(),
                    allocation.getEndDate(), allocation.getProjectCriticalPathEndDate(), allocation.getProjectDelay());
        }

        @Override
        public String getDescription() {
            return "Project '%s' ends on day %d (job '%s'), %d day(s) past its critical path end date of %d."
                    .formatted(project, endDate, job, delayInDays, criticalPathEndDate);
        }
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    @Schema(description = "The schedule keeps running until the last project ends.",
            allOf = { ProjectJobScheduleJustification.class })
    record MakespanJustification(
            @Schema(description = "The day number on which the last project ends.") int makespan)
            implements
                ProjectJobScheduleJustification {

        public static MakespanJustification of(int makespan) {
            return new MakespanJustification(makespan);
        }

        @Override
        public String getDescription() {
            return "The last project ends on day %d, so the schedule spans %d day(s).".formatted(makespan, makespan);
        }
    }
}
