package org.acme.schooltimetabling.dto.input;

import ai.timefold.solver.service.definition.api.ModelConfigOverrides;
import ai.timefold.solver.service.definition.api.domain.ConstraintReference;

import org.acme.schooltimetabling.domain.TimetableConstraintProperties;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(description = """
        Soft constraint weights. Set a weight to 0 to disable the corresponding constraint. \
        A weight left unset (null) is not overridden here, so the value from the configuration profile \
        (or the constraint's default) applies. This makes it possible to override some weights via the \
        input while leaving others to the configuration profile.""")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TimetableConfigOverrides(
        @ConstraintReference(TimetableConstraintProperties.TEACHER_ROOM_STABILITY) @Schema(
                description = "Soft weight of the teacher room stability constraint.",
                minimum = "0") Long teacherRoomStabilityWeight,
        @ConstraintReference(TimetableConstraintProperties.TEACHER_TIME_EFFICIENCY) @Schema(
                description = "Soft weight of the teacher time efficiency constraint.",
                minimum = "0") Long teacherTimeEfficiencyWeight,
        @ConstraintReference(TimetableConstraintProperties.STUDENT_GROUP_SUBJECT_VARIETY) @Schema(
                description = "Soft weight of the student group subject variety constraint.",
                minimum = "0") Long studentGroupSubjectVarietyWeight)
        implements
            ModelConfigOverrides {

    /**
     * Creates an empty overrides instance: no weight is overridden, so the configuration profile
     * (or each constraint's default) applies. Required by the Service Module to generate the default config profile.
     */
    public TimetableConfigOverrides() {
        this(null, null, null);
    }
}
