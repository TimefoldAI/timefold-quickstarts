package org.acme.bedallocation.dto;

import static org.acme.bedallocation.support.ObjectHelper.immutableCopy;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelInput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The bed allocation planning problem input.")
public record BedPlanInput(
        @Schema(description = "Departments, each grouping the rooms and beds available for scheduling.",
                required = true) List<DepartmentDTO> departments,
        @Schema(description = "Patient stays that must each be assigned to a bed.",
                required = true) List<StayDTO> stays)
        implements
            ModelInput {

    public BedPlanInput {
        departments = immutableCopy(departments);
        stays = immutableCopy(stays);
    }

    public BedPlanInput withStays(List<StayDTO> stays) {
        return new BedPlanInput(departments, stays);
    }
}
