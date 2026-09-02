package org.acme.maintenancescheduling.dto.output;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelOutput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The maintenance scheduling problem output.")
public record MaintenanceScheduleOutput(
        @Schema(description = "Maintenance jobs with their assigned crew and dates, if any.",
                required = true) List<JobOutputDTO> jobs)
        implements
            ModelOutput {
}
