package org.acme.bedallocation.dto;

import ai.timefold.solver.service.definition.api.ModelConfigOverrides;
import ai.timefold.solver.service.definition.api.domain.ConstraintReference;

import org.acme.bedallocation.solver.BedScheduleConstraintProvider;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(description = "Soft constraint weights. Set a weight to 0 to disable the corresponding constraint. "
        + "A weight left unset (null) is not overridden here, so the value from the configuration profile "
        + "(or the constraint's default) applies. This makes it possible to override some weights via the "
        + "input while leaving others to the configuration profile.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BedScheduleConfigOverrides(
        @ConstraintReference(BedScheduleConstraintProvider.PREFERRED_MAXIMUM_ROOM_CAPACITY) @Schema(
                description = "Soft weight of the preferred maximum room capacity constraint.") Long preferredMaximumRoomCapacityWeight,
        @ConstraintReference(BedScheduleConstraintProvider.DEPARTMENT_SPECIALTY) @Schema(
                description = "Soft weight of the department specialty constraint.") Long departmentSpecialtyWeight,
        @ConstraintReference(BedScheduleConstraintProvider.DEPARTMENT_SPECIALTY_NOT_FIRST_PRIORITY) @Schema(
                description = "Soft weight of the department specialty not first priority constraint.") Long departmentSpecialtyNotFirstPriorityWeight,
        @ConstraintReference(BedScheduleConstraintProvider.PREFERRED_PATIENT_EQUIPMENT) @Schema(
                description = "Soft weight of the preferred patient equipment constraint.") Long preferredPatientEquipmentWeight)
        implements
            ModelConfigOverrides {

    public BedScheduleConfigOverrides {
        preferredMaximumRoomCapacityWeight =
                preferredMaximumRoomCapacityWeight != null && preferredMaximumRoomCapacityWeight < 0L ? 0L
                        : preferredMaximumRoomCapacityWeight;
        departmentSpecialtyWeight =
                departmentSpecialtyWeight != null && departmentSpecialtyWeight < 0L ? 0L : departmentSpecialtyWeight;
        departmentSpecialtyNotFirstPriorityWeight =
                departmentSpecialtyNotFirstPriorityWeight != null && departmentSpecialtyNotFirstPriorityWeight < 0L ? 0L
                        : departmentSpecialtyNotFirstPriorityWeight;
        preferredPatientEquipmentWeight =
                preferredPatientEquipmentWeight != null && preferredPatientEquipmentWeight < 0L ? 0L
                        : preferredPatientEquipmentWeight;
    }

    public BedScheduleConfigOverrides() {
        this(8L, 10L, 10L, 50L);
    }

    public BedScheduleConfigOverrides withPreferredMaximumRoomCapacityWeight(Long preferredMaximumRoomCapacityWeight) {
        return new BedScheduleConfigOverrides(preferredMaximumRoomCapacityWeight, departmentSpecialtyWeight,
                departmentSpecialtyNotFirstPriorityWeight, preferredPatientEquipmentWeight);
    }

    public BedScheduleConfigOverrides withDepartmentSpecialtyWeight(Long departmentSpecialtyWeight) {
        return new BedScheduleConfigOverrides(preferredMaximumRoomCapacityWeight, departmentSpecialtyWeight,
                departmentSpecialtyNotFirstPriorityWeight, preferredPatientEquipmentWeight);
    }

    public BedScheduleConfigOverrides withDepartmentSpecialtyNotFirstPriorityWeight(
            Long departmentSpecialtyNotFirstPriorityWeight) {
        return new BedScheduleConfigOverrides(preferredMaximumRoomCapacityWeight, departmentSpecialtyWeight,
                departmentSpecialtyNotFirstPriorityWeight, preferredPatientEquipmentWeight);
    }

    public BedScheduleConfigOverrides withPreferredPatientEquipmentWeight(Long preferredPatientEquipmentWeight) {
        return new BedScheduleConfigOverrides(preferredMaximumRoomCapacityWeight, departmentSpecialtyWeight,
                departmentSpecialtyNotFirstPriorityWeight, preferredPatientEquipmentWeight);
    }
}
