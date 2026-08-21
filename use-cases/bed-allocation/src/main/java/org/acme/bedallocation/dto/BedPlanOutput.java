package org.acme.bedallocation.dto;

import static org.acme.bedallocation.support.ObjectHelper.immutableCopy;

import java.util.List;

import ai.timefold.solver.service.definition.api.ModelOutput;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The bed allocation planning problem output.")
public record BedPlanOutput(
        @Schema(description = "Departments, each grouping the rooms and beds available for scheduling.",
                required = true) List<DepartmentDTO> departments,
        @Schema(description = "Patient stays with their assigned bed, if any.", required = true) List<StayDTO> stays,
        @Schema(description = "The score of the solution.", required = true) String score) implements ModelOutput {

    public BedPlanOutput {
        departments = immutableCopy(departments);
        stays = immutableCopy(stays);
    }
}
