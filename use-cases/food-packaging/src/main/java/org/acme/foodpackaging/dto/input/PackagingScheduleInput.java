package org.acme.foodpackaging.dto.input;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelInput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The food packaging planning problem input.")
public record PackagingScheduleInput(
        @Schema(description = "The window the schedule covers.", required = true) WorkCalendarDTO workCalendar,
        @Schema(description = "Products the jobs package, with their changeover cleaning durations.", required = true,
                minItems = 1) List<ProductDTO> products,
        @Schema(description = "Operators who can run the production lines.", required = true,
                minItems = 1) List<OperatorDTO> operators,
        @Schema(description = "Production lines the jobs can be produced on.", required = true,
                minItems = 1) List<LineDTO> lines,
        @Schema(description = "Jobs that should each be produced on one of the lines.", required = true,
                minItems = 1) List<JobDTO> jobs)
        implements
            ModelInput {

    public PackagingScheduleInput withLines(List<LineDTO> lines) {
        return new PackagingScheduleInput(workCalendar, products, operators, lines, jobs);
    }
}
