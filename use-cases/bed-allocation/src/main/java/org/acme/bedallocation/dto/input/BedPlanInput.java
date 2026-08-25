package org.acme.bedallocation.dto.input;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import ai.timefold.solver.service.definition.api.ModelInput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The bed allocation planning problem input.")
public record BedPlanInput(
        @Schema(description = "Departments, each grouping the rooms and beds available for scheduling.") @NotEmpty List<@Valid DepartmentInputDTO> departments,
        @Schema(description = "Patient stays that must each be assigned to a bed.") @NotEmpty List<@Valid StayInputDTO> stays)
        implements
            ModelInput {

    public BedPlanInput withStays(List<StayInputDTO> stays) {
        return new BedPlanInput(departments, stays);
    }
}
