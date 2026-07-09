package org.acme.schooltimetabling.dto;

import ai.timefold.solver.service.definition.api.ModelConfigOverrides;
import ai.timefold.solver.service.definition.api.domain.ConstraintReference;

import org.acme.schooltimetabling.solver.TimetableConstraintProvider;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(description = "Soft constraint weights. Set a weight to 0 to disable the corresponding constraint. "
        + "A weight left unset (null) is not overridden here, so the value from the configuration profile "
        + "(or the constraint's default) applies. This makes it possible to override some weights via the "
        + "input while leaving others to the configuration profile.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TimetableConfigOverrides(
        @ConstraintReference(TimetableConstraintProvider.TEACHER_ROOM_STABILITY) @Schema(
                description = "Soft weight of the teacher room stability constraint.") Long teacherRoomStabilityWeight,
        @ConstraintReference(TimetableConstraintProvider.TEACHER_TIME_EFFICIENCY) @Schema(
                description = "Soft weight of the teacher time efficiency constraint.") Long teacherTimeEfficiencyWeight,
        @ConstraintReference(TimetableConstraintProvider.STUDENT_GROUP_SUBJECT_VARIETY) @Schema(
                description = "Soft weight of the student group subject variety constraint.") Long studentGroupSubjectVarietyWeight)
        implements
            ModelConfigOverrides {

    public TimetableConfigOverrides {
        teacherRoomStabilityWeight =
                teacherRoomStabilityWeight != null && teacherRoomStabilityWeight < 0L ? 0L : teacherRoomStabilityWeight;
        teacherTimeEfficiencyWeight =
                teacherTimeEfficiencyWeight != null && teacherTimeEfficiencyWeight < 0L ? 0L : teacherTimeEfficiencyWeight;
        studentGroupSubjectVarietyWeight =
                studentGroupSubjectVarietyWeight != null && studentGroupSubjectVarietyWeight < 0L ? 0L
                        : studentGroupSubjectVarietyWeight;
    }

    public TimetableConfigOverrides() {
        this(1L, 1L, 1L);
    }

    public TimetableConfigOverrides withTeacherRoomStabilityWeight(Long teacherRoomStabilityWeight) {
        return new TimetableConfigOverrides(teacherRoomStabilityWeight, teacherTimeEfficiencyWeight,
                studentGroupSubjectVarietyWeight);
    }

    public TimetableConfigOverrides withTeacherTimeEfficiencyWeight(Long teacherTimeEfficiencyWeight) {
        return new TimetableConfigOverrides(teacherRoomStabilityWeight, teacherTimeEfficiencyWeight,
                studentGroupSubjectVarietyWeight);
    }

    public TimetableConfigOverrides withStudentGroupSubjectVarietyWeight(Long studentGroupSubjectVarietyWeight) {
        return new TimetableConfigOverrides(teacherRoomStabilityWeight, teacherTimeEfficiencyWeight,
                studentGroupSubjectVarietyWeight);
    }
}
