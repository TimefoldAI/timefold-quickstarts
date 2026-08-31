package org.acme.bedallocation.dto.input;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelInput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The bed allocation planning problem input.")
public record BedPlanInput(
        @Schema(description = "Departments, each grouping the rooms and beds available for scheduling.", required = true,
                minItems = 1) List<DepartmentInputDTO> departments,
        @Schema(description = "Patient stays that must each be assigned to a bed.", required = true,
                minItems = 1) List<StayInputDTO> stays)
        implements
            ModelInput {

    public BedPlanInput withStays(List<StayInputDTO> stays) {
        return new BedPlanInput(departments, stays);
    }
}
