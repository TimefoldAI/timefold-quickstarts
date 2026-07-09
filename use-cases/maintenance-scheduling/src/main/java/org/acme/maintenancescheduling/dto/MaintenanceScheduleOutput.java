package org.acme.maintenancescheduling.dto;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelOutput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The maintenance scheduling planning problem output.")
public record MaintenanceScheduleOutput(
        @Schema(description = "The planning window in which jobs can be scheduled.") WorkCalendarDTO workCalendar,
        @Schema(description = "List of crews a job can be assigned to.") List<CrewDTO> crews,
        @Schema(description = "List of jobs with their assigned crew and dates.") List<JobDTO> jobs,
        @Schema(description = "The score of the solution.") String score) implements ModelOutput {

    public MaintenanceScheduleOutput {
        crews = List.copyOf(crews);
        jobs = List.copyOf(jobs);
    }

    public MaintenanceScheduleOutput withWorkCalendar(WorkCalendarDTO workCalendar) {
        return new MaintenanceScheduleOutput(workCalendar, crews, jobs, score);
    }

    public MaintenanceScheduleOutput withCrews(List<CrewDTO> crews) {
        return new MaintenanceScheduleOutput(workCalendar, crews, jobs, score);
    }

    public MaintenanceScheduleOutput withJobs(List<JobDTO> jobs) {
        return new MaintenanceScheduleOutput(workCalendar, crews, jobs, score);
    }

    public MaintenanceScheduleOutput withScore(String score) {
        return new MaintenanceScheduleOutput(workCalendar, crews, jobs, score);
    }
}
