package org.acme.bedallocation.domain.justification;

import java.util.Collection;
import java.util.List;

import ai.timefold.solver.service.definition.api.ModelConstraintJustification;

import org.acme.bedallocation.domain.Department;
import org.acme.bedallocation.domain.Stay;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Common contract for every bed allocation justification.
 * <p>
 * Each implementation is a record dedicated to exactly one thing that is being justified, so that the Timefold Platform can
 * both render a human-readable {@link #getDescription() description} and expose the individual facts behind it through the
 * OpenAPI schema.
 * <p>
 * Every implementation must be listed in the {@link Schema#oneOf()} below, otherwise it does not show up in the generated
 * OpenAPI schema.
 */
@Schema(description = "Explains why a bed allocation constraint was matched.",
        oneOf = {
                // Hard constraints
                BedPlanJustification.SameBedInSameNightJustification.class,
                BedPlanJustification.FemaleInMaleRoomJustification.class,
                BedPlanJustification.MaleInFemaleRoomJustification.class,
                BedPlanJustification.DifferentGenderInSameGenderRoomJustification.class,
                BedPlanJustification.DepartmentMinimumAgeJustification.class,
                BedPlanJustification.DepartmentMaximumAgeJustification.class,
                BedPlanJustification.MissingRequiredEquipmentJustification.class,

                // Medium constraints
                BedPlanJustification.UnassignedStayJustification.class,

                // Soft constraints
                BedPlanJustification.PreferredMaximumRoomCapacityJustification.class,
                BedPlanJustification.MissingDepartmentSpecialtyJustification.class,
                BedPlanJustification.DepartmentSpecialtyNotFirstPriorityJustification.class,
                BedPlanJustification.MissingPreferredEquipmentJustification.class
        })
public interface BedPlanJustification extends ModelConstraintJustification {

    /**
     * @return never null, a human-readable explanation of the constraint match
     */
    String getDescription();

    /**
     * Exposes the description as the {@code description} property of {@link ModelConstraintJustification}.
     */
    default String description() {
        return getDescription();
    }

    private static List<String> missing(Collection<String> required, Collection<String> available) {
        return required.stream()
                .filter(equipment -> !available.contains(equipment))
                .toList();
    }

    @Schema(allOf = { BedPlanJustification.class })
    record SameBedInSameNightJustification(String bed, String stay1, String stay2, int overlappingNightCount)
            implements
                BedPlanJustification {

        public static SameBedInSameNightJustification of(Stay left, Stay right) {
            return new SameBedInSameNightJustification(left.getBed().id(), left.getId(), right.getId(),
                    left.calculateSameNightCount(right));
        }

        @Override
        public String getDescription() {
            return "Bed '%s' is shared by stays '%s' and '%s' for %d overlapping night(s)."
                    .formatted(bed, stay1, stay2, overlappingNightCount);
        }
    }

    @Schema(allOf = { BedPlanJustification.class })
    record FemaleInMaleRoomJustification(String stay, String room) implements BedPlanJustification {

        public static FemaleInMaleRoomJustification of(Stay stay) {
            return new FemaleInMaleRoomJustification(stay.getId(), stay.getRoom().id());
        }

        @Override
        public String getDescription() {
            return "Stay '%s' assigns a female patient to male-only room '%s'.".formatted(stay, room);
        }
    }

    @Schema(allOf = { BedPlanJustification.class })
    record MaleInFemaleRoomJustification(String stay, String room) implements BedPlanJustification {

        public static MaleInFemaleRoomJustification of(Stay stay) {
            return new MaleInFemaleRoomJustification(stay.getId(), stay.getRoom().id());
        }

        @Override
        public String getDescription() {
            return "Stay '%s' assigns a male patient to female-only room '%s'.".formatted(stay, room);
        }
    }

    @Schema(allOf = { BedPlanJustification.class })
    record DifferentGenderInSameGenderRoomJustification(String room, String stay1, String stay2,
            int overlappingNightCount) implements BedPlanJustification {

        public static DifferentGenderInSameGenderRoomJustification of(Stay left, Stay right) {
            return new DifferentGenderInSameGenderRoomJustification(left.getRoom().id(), left.getId(), right.getId(),
                    left.calculateSameNightCount(right));
        }

        @Override
        public String getDescription() {
            return "Same-gender room '%s' has different-gender stays '%s' and '%s' overlapping for %d night(s)."
                    .formatted(room, stay1, stay2, overlappingNightCount);
        }
    }

    @Schema(allOf = { BedPlanJustification.class })
    record DepartmentMinimumAgeJustification(String department, String stay, int minimumAge, int patientAge)
            implements
                BedPlanJustification {

        public static DepartmentMinimumAgeJustification of(Department department, Stay stay) {
            return new DepartmentMinimumAgeJustification(department.id(), stay.getId(), department.minimumAge(),
                    stay.getPatientAge());
        }

        @Override
        public String getDescription() {
            return "Department '%s' requires a minimum age of %d, but stay '%s' has a patient aged %d."
                    .formatted(department, minimumAge, stay, patientAge);
        }
    }

    @Schema(allOf = { BedPlanJustification.class })
    record DepartmentMaximumAgeJustification(String department, String stay, int maximumAge, int patientAge)
            implements
                BedPlanJustification {

        public static DepartmentMaximumAgeJustification of(Department department, Stay stay) {
            return new DepartmentMaximumAgeJustification(department.id(), stay.getId(), department.maximumAge(),
                    stay.getPatientAge());
        }

        @Override
        public String getDescription() {
            return "Department '%s' allows a maximum age of %d, but stay '%s' has a patient aged %d."
                    .formatted(department, maximumAge, stay, patientAge);
        }
    }

    @Schema(allOf = { BedPlanJustification.class })
    record MissingRequiredEquipmentJustification(String stay, String room, List<String> missingEquipments)
            implements
                BedPlanJustification {

        public static MissingRequiredEquipmentJustification of(Stay stay) {
            return new MissingRequiredEquipmentJustification(stay.getId(), stay.getRoom().id(),
                    missing(stay.getPatientRequiredEquipments(), stay.getRoom().equipments()));
        }

        @Override
        public String getDescription() {
            return "Room '%s' of stay '%s' is missing required equipment [%s]."
                    .formatted(room, stay, String.join(", ", missingEquipments));
        }
    }

    @Schema(allOf = { BedPlanJustification.class })
    record UnassignedStayJustification(String stay) implements BedPlanJustification {

        public static UnassignedStayJustification of(Stay stay) {
            return new UnassignedStayJustification(stay.getId());
        }

        @Override
        public String getDescription() {
            return "Stay '%s' is not assigned to a bed.".formatted(stay);
        }
    }

    @Schema(allOf = { BedPlanJustification.class })
    record PreferredMaximumRoomCapacityJustification(String stay, int preferredMaximumRoomCapacity, int roomCapacity)
            implements
                BedPlanJustification {

        public static PreferredMaximumRoomCapacityJustification of(Stay stay) {
            return new PreferredMaximumRoomCapacityJustification(stay.getId(),
                    stay.getPatientPreferredMaximumRoomCapacity(), stay.getRoomCapacity());
        }

        @Override
        public String getDescription() {
            return "Stay '%s' prefers a room with at most %d bed(s), but is assigned to a room with %d bed(s)."
                    .formatted(stay, preferredMaximumRoomCapacity, roomCapacity);
        }
    }

    @Schema(allOf = { BedPlanJustification.class })
    record MissingDepartmentSpecialtyJustification(String stay, String specialty) implements BedPlanJustification {

        public static MissingDepartmentSpecialtyJustification of(Stay stay) {
            return new MissingDepartmentSpecialtyJustification(stay.getId(), stay.getSpecialty());
        }

        @Override
        public String getDescription() {
            return "Stay '%s' requires specialty '%s', which its department does not treat."
                    .formatted(stay, specialty);
        }
    }

    @Schema(allOf = { BedPlanJustification.class })
    record DepartmentSpecialtyNotFirstPriorityJustification(String stay, String specialty, int priority)
            implements
                BedPlanJustification {

        public static DepartmentSpecialtyNotFirstPriorityJustification of(Stay stay) {
            return new DepartmentSpecialtyNotFirstPriorityJustification(stay.getId(), stay.getSpecialty(),
                    stay.getSpecialtyPriority());
        }

        @Override
        public String getDescription() {
            return "Stay '%s' requires specialty '%s', which is only priority %d in its department."
                    .formatted(stay, specialty, priority);
        }
    }

    @Schema(allOf = { BedPlanJustification.class })
    record MissingPreferredEquipmentJustification(String stay, String room, List<String> missingEquipments)
            implements
                BedPlanJustification {

        public static MissingPreferredEquipmentJustification of(Stay stay) {
            return new MissingPreferredEquipmentJustification(stay.getId(), stay.getRoom().id(),
                    missing(stay.getPatientPreferredEquipments(), stay.getRoom().equipments()));
        }

        @Override
        public String getDescription() {
            return "Room '%s' of stay '%s' is missing preferred equipment [%s]."
                    .formatted(room, stay, String.join(", ", missingEquipments));
        }
    }
}
