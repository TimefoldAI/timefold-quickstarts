package org.acme.maintenancescheduling.dto;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelInput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The maintenance scheduling planning problem input.")
public record MaintenanceScheduleInput(
        @Schema(description = "The planning window in which jobs can be scheduled.") WorkCalendarDTO workCalendar,
        @Schema(description = "List of crews a job can be assigned to.") List<CrewDTO> crews,
        @Schema(description = "List of jobs that must each be assigned to a crew and a start date.") List<JobDTO> jobs)
        implements
            ModelInput {

    public MaintenanceScheduleInput {
        crews = List.copyOf(crews);
        jobs = List.copyOf(jobs);
    }

    public MaintenanceScheduleInput withWorkCalendar(WorkCalendarDTO workCalendar) {
        return new MaintenanceScheduleInput(workCalendar, crews, jobs);
    }

    public MaintenanceScheduleInput withCrews(List<CrewDTO> crews) {
        return new MaintenanceScheduleInput(workCalendar, crews, jobs);
    }

    public MaintenanceScheduleInput withJobs(List<JobDTO> jobs) {
        return new MaintenanceScheduleInput(workCalendar, crews, jobs);
    }
}
