package org.acme.bedallocation.solver;

import static ai.timefold.solver.core.api.score.stream.Joiners.equal;
import static ai.timefold.solver.core.api.score.stream.Joiners.filtering;
import static ai.timefold.solver.core.api.score.stream.Joiners.greaterThan;
import static ai.timefold.solver.core.api.score.stream.Joiners.lessThan;

import java.util.function.Function;

import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

import org.acme.bedallocation.domain.BedPlanConstraintProperties;
import org.acme.bedallocation.domain.Department;
import org.acme.bedallocation.domain.Gender;
import org.acme.bedallocation.domain.GenderLimitation;
import org.acme.bedallocation.domain.Stay;
import org.acme.bedallocation.domain.justification.BedPlanJustification.DepartmentMaximumAgeJustification;
import org.acme.bedallocation.domain.justification.BedPlanJustification.DepartmentMinimumAgeJustification;
import org.acme.bedallocation.domain.justification.BedPlanJustification.DepartmentSpecialtyNotFirstPriorityJustification;
import org.acme.bedallocation.domain.justification.BedPlanJustification.DifferentGenderInSameGenderRoomJustification;
import org.acme.bedallocation.domain.justification.BedPlanJustification.FemaleInMaleRoomJustification;
import org.acme.bedallocation.domain.justification.BedPlanJustification.MaleInFemaleRoomJustification;
import org.acme.bedallocation.domain.justification.BedPlanJustification.MissingDepartmentSpecialtyJustification;
import org.acme.bedallocation.domain.justification.BedPlanJustification.MissingPreferredEquipmentJustification;
import org.acme.bedallocation.domain.justification.BedPlanJustification.MissingRequiredEquipmentJustification;
import org.acme.bedallocation.domain.justification.BedPlanJustification.PreferredMaximumRoomCapacityJustification;
import org.acme.bedallocation.domain.justification.BedPlanJustification.SameBedInSameNightJustification;
import org.acme.bedallocation.domain.justification.BedPlanJustification.UnassignedStayJustification;

public class BedAllocationConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // Hard constraints
                sameBedInSameNight(constraintFactory),
                femaleInMaleRoom(constraintFactory),
                maleInFemaleRoom(constraintFactory),
                differentGenderInSameGenderRoomInSameNight(constraintFactory),
                departmentMinimumAge(constraintFactory),
                departmentMaximumAge(constraintFactory),
                requiredPatientEquipment(constraintFactory),

                // Medium constraints
                assignEveryPatientToABed(constraintFactory),

                // Soft constraints
                preferredMaximumRoomCapacity(constraintFactory),
                departmentSpecialty(constraintFactory),
                departmentSpecialtyNotFirstPriority(constraintFactory),
                preferredPatientEquipment(constraintFactory)
        };
    }

    public Constraint sameBedInSameNight(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(Stay.class,
                equal(Stay::getBed))
                .filter((left, right) -> left.calculateSameNightCount(right) > 0)
                .penalize(HardMediumSoftScore.ofHard(1000),
                        Stay::calculateSameNightCount)
                .justifyWith((left, right, score) -> SameBedInSameNightJustification.of(left, right))
                .asConstraint(BedPlanConstraintProperties.SAME_BED_IN_SAME_NIGHT);
    }

    public Constraint femaleInMaleRoom(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(Stay.class)
                .filter(st -> st.getPatientGender() == Gender.FEMALE
                        && st.getRoomGenderLimitation() == GenderLimitation.MALE_ONLY)
                .penalize(HardMediumSoftScore.ofHard(50), Stay::getNightCount)
                .justifyWith((st, score) -> FemaleInMaleRoomJustification.of(st))
                .asConstraint(BedPlanConstraintProperties.FEMALE_IN_MALE_ROOM);
    }

    public Constraint maleInFemaleRoom(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(Stay.class)
                .filter(st -> st.getPatientGender() == Gender.MALE
                        && st.getRoomGenderLimitation() == GenderLimitation.FEMALE_ONLY)
                .penalize(HardMediumSoftScore.ofHard(50), Stay::getNightCount)
                .justifyWith((st, score) -> MaleInFemaleRoomJustification.of(st))
                .asConstraint(BedPlanConstraintProperties.MALE_IN_FEMALE_ROOM);
    }

    public Constraint differentGenderInSameGenderRoomInSameNight(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Stay.class)
                .filter(bd -> bd.getRoomGenderLimitation() == GenderLimitation.SAME_GENDER)
                .join(constraintFactory.forEach(Stay.class)
                        .filter(st -> st.getRoomGenderLimitation() == GenderLimitation.SAME_GENDER),
                        equal(Stay::getRoom),
                        lessThan(Stay::getId),
                        filtering((left, right) -> left.getPatientGender() != right.getPatientGender()
                                && left.calculateSameNightCount(right) > 0))
                .penalize(HardMediumSoftScore.ofHard(1000),
                        Stay::calculateSameNightCount)
                .justifyWith((left, right, score) -> DifferentGenderInSameGenderRoomJustification.of(left, right))
                .asConstraint(BedPlanConstraintProperties.DIFFERENT_GENDER_IN_SAME_GENDER_ROOM_IN_SAME_NIGHT);
    }

    public Constraint departmentMinimumAge(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(Department.class)
                .filter(d -> d.minimumAge() != null)
                .join(constraintFactory.forEachIncludingUnassigned(Stay.class),
                        equal(Function.identity(), Stay::getDepartment),
                        greaterThan(Department::minimumAge, Stay::getPatientAge))
                .penalize(HardMediumSoftScore.ofHard(100),
                        (d, st) -> st.getNightCount())
                .justifyWith((d, st, score) -> DepartmentMinimumAgeJustification.of(d, st))
                .asConstraint(BedPlanConstraintProperties.DEPARTMENT_MINIMUM_AGE);
    }

    public Constraint departmentMaximumAge(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(Department.class)
                .filter(d -> d.maximumAge() != null)
                .join(constraintFactory.forEachIncludingUnassigned(Stay.class),
                        equal(Function.identity(), Stay::getDepartment),
                        lessThan(Department::maximumAge, Stay::getPatientAge))
                .penalize(HardMediumSoftScore.ofHard(100),
                        (d, st) -> st.getNightCount())
                .justifyWith((d, st, score) -> DepartmentMaximumAgeJustification.of(d, st))
                .asConstraint(BedPlanConstraintProperties.DEPARTMENT_MAXIMUM_AGE);
    }

    public Constraint requiredPatientEquipment(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Stay.class)
                .filter(st -> st.getBed() != null
                        && !st.getRoom().equipments().containsAll(st.getPatientRequiredEquipments()))
                .penalize(HardMediumSoftScore.ofHard(50),
                        st -> st.getNightCount() * (int) st.getPatientRequiredEquipments().stream()
                                .filter(equipment -> !st.getRoom().equipments().contains(equipment)).count())
                .justifyWith((st, score) -> MissingRequiredEquipmentJustification.of(st))
                .asConstraint(BedPlanConstraintProperties.REQUIRED_PATIENT_EQUIPMENT);
    }

    // Medium constraints
    public Constraint assignEveryPatientToABed(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(Stay.class)
                .filter(st -> st.getBed() == null)
                .penalize(HardMediumSoftScore.ONE_MEDIUM, Stay::getNightCount)
                .justifyWith((st, score) -> UnassignedStayJustification.of(st))
                .asConstraint(BedPlanConstraintProperties.ASSIGN_EVERY_PATIENT_TO_A_BED);
    }

    // Soft constraints
    public Constraint preferredMaximumRoomCapacity(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Stay.class)
                .filter(st -> st.getPatientPreferredMaximumRoomCapacity() != null
                        && st.getPatientPreferredMaximumRoomCapacity() < st.getRoomCapacity())
                .penalize(HardMediumSoftScore.ofSoft(8), Stay::getNightCount)
                .justifyWith((st, score) -> PreferredMaximumRoomCapacityJustification.of(st))
                .asConstraint(BedPlanConstraintProperties.PREFERRED_MAXIMUM_ROOM_CAPACITY);
    }

    public Constraint departmentSpecialty(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Stay.class)
                .filter(st -> !st.hasDepartmentSpecialty())
                .penalize(HardMediumSoftScore.ofSoft(10), Stay::getNightCount)
                .justifyWith((st, score) -> MissingDepartmentSpecialtyJustification.of(st))
                .asConstraint(BedPlanConstraintProperties.DEPARTMENT_SPECIALTY);
    }

    public Constraint departmentSpecialtyNotFirstPriority(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Stay.class)
                .filter(st -> st.hasDepartmentSpecialty() && st.getSpecialtyPriority() > 1)
                .penalize(HardMediumSoftScore.ofSoft(10), stay -> (stay.getSpecialtyPriority() - 1) * stay.getNightCount())
                .justifyWith((st, score) -> DepartmentSpecialtyNotFirstPriorityJustification.of(st))
                .asConstraint(BedPlanConstraintProperties.DEPARTMENT_SPECIALTY_NOT_FIRST_PRIORITY);
    }

    public Constraint preferredPatientEquipment(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Stay.class)
                .filter(bedDesignation -> bedDesignation.getBed() != null
                        && !bedDesignation.getRoom().equipments().containsAll(
                                bedDesignation.getPatientPreferredEquipments()))
                .penalize(HardMediumSoftScore.ofSoft(50),
                        st -> st.getNightCount() * (int) st.getPatientPreferredEquipments().stream()
                                .filter(equipment -> !st.getRoom().equipments().contains(equipment)).count())
                .justifyWith((st, score) -> MissingPreferredEquipmentJustification.of(st))
                .asConstraint(BedPlanConstraintProperties.PREFERRED_PATIENT_EQUIPMENT);
    }
}
