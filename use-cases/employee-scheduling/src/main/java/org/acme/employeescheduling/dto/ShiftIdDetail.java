package org.acme.employeescheduling.dto;

import ai.timefold.solver.service.definition.api.validation.IssueMetadata;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Details about a shift ID validation issue.")
public record ShiftIdDetail(
        @Schema(description = "The ID of the shift.") String shiftId) implements IssueMetadata {

    public ShiftIdDetail {
        shiftId = shiftId == null ? "" : shiftId;
    }

    public ShiftIdDetail withShiftId(String shiftId) {
        return new ShiftIdDetail(shiftId);
    }

    @Override
    public String getType() {
        return "ShiftId";
    }
}
