package org.acme.maintenancescheduling.dto.input;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelInput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The maintenance scheduling problem input.")
public record MaintenanceScheduleInput(
        @Schema(description = "The window of workdays that jobs can be scheduled in.",
                required = true) WorkCalendarInputDTO workCalendar,
        @Schema(description = "Maintenance crews that jobs can be assigned to.", required = true,
                minItems = 1) List<CrewInputDTO> crews,
        @Schema(description = "Maintenance jobs that must each be assigned to a crew and a start date.", required = true,
                minItems = 1) List<JobInputDTO> jobs)
        implements
            ModelInput {

    public MaintenanceScheduleInput withJobs(List<JobInputDTO> jobs) {
        return new MaintenanceScheduleInput(workCalendar, crews, jobs);
    }
}
