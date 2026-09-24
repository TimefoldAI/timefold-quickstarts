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
import ai.timefold.solver.service.definition.api.description.ConstraintInfo;

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
                .asConstraint(new ConstraintInfo(BedPlanConstraintProperties.SAME_BED_IN_SAME_NIGHT,
                        BedPlanConstraintProperties.SAME_BED_IN_SAME_NIGHT,
                        "Two patient stays must not occupy the same bed on overlapping nights.",
                        BedPlanConstraintGroup.BED_CONFLICTS));
    }

    public Constraint femaleInMaleRoom(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(Stay.class)
                .filter(st -> st.getPatientGender() == Gender.FEMALE
                        && st.getRoomGenderLimitation() == GenderLimitation.MALE_ONLY)
                .penalize(HardMediumSoftScore.ofHard(50), Stay::getNightCount)
                .justifyWith((st, score) -> FemaleInMaleRoomJustification.of(st))
                .asConstraint(new ConstraintInfo(BedPlanConstraintProperties.FEMALE_IN_MALE_ROOM,
                        BedPlanConstraintProperties.FEMALE_IN_MALE_ROOM,
                        "A female patient must not be assigned to a male-only room.",
                        BedPlanConstraintGroup.GENDER_RULES));
    }

    public Constraint maleInFemaleRoom(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(Stay.class)
                .filter(st -> st.getPatientGender() == Gender.MALE
                        && st.getRoomGenderLimitation() == GenderLimitation.FEMALE_ONLY)
                .penalize(HardMediumSoftScore.ofHard(50), Stay::getNightCount)
                .justifyWith((st, score) -> MaleInFemaleRoomJustification.of(st))
                .asConstraint(new ConstraintInfo(BedPlanConstraintProperties.MALE_IN_FEMALE_ROOM,
                        BedPlanConstraintProperties.MALE_IN_FEMALE_ROOM,
                        "A male patient must not be assigned to a female-only room.",
                        BedPlanConstraintGroup.GENDER_RULES));
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
                .asConstraint(new ConstraintInfo(
                        BedPlanConstraintProperties.DIFFERENT_GENDER_IN_SAME_GENDER_ROOM_IN_SAME_NIGHT,
                        BedPlanConstraintProperties.DIFFERENT_GENDER_IN_SAME_GENDER_ROOM_IN_SAME_NIGHT,
                        "Patients of different genders must not share a same-gender room on overlapping nights.",
                        BedPlanConstraintGroup.GENDER_RULES));
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
                .asConstraint(new ConstraintInfo(BedPlanConstraintProperties.DEPARTMENT_MINIMUM_AGE,
                        BedPlanConstraintProperties.DEPARTMENT_MINIMUM_AGE,
                        "A patient must not be younger than the department's minimum age.",
                        BedPlanConstraintGroup.ELIGIBILITY_RULES));
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
                .asConstraint(new ConstraintInfo(BedPlanConstraintProperties.DEPARTMENT_MAXIMUM_AGE,
                        BedPlanConstraintProperties.DEPARTMENT_MAXIMUM_AGE,
                        "A patient must not be older than the department's maximum age.",
                        BedPlanConstraintGroup.ELIGIBILITY_RULES));
    }

    public Constraint requiredPatientEquipment(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Stay.class)
                .filter(st -> st.getBed() != null
                        && !st.getRoom().equipments().containsAll(st.getPatientRequiredEquipments()))
                .penalize(HardMediumSoftScore.ofHard(50),
                        st -> st.getNightCount() * (int) st.getPatientRequiredEquipments().stream()
                                .filter(equipment -> !st.getRoom().equipments().contains(equipment)).count())
                .justifyWith((st, score) -> MissingRequiredEquipmentJustification.of(st))
                .asConstraint(new ConstraintInfo(BedPlanConstraintProperties.REQUIRED_PATIENT_EQUIPMENT,
                        BedPlanConstraintProperties.REQUIRED_PATIENT_EQUIPMENT,
                        "A patient's assigned room must have every piece of equipment the patient requires.",
                        BedPlanConstraintGroup.ELIGIBILITY_RULES));
    }

    // Medium constraints
    public Constraint assignEveryPatientToABed(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(Stay.class)
                .filter(st -> st.getBed() == null)
                .penalize(HardMediumSoftScore.ONE_MEDIUM, Stay::getNightCount)
                .justifyWith((st, score) -> UnassignedStayJustification.of(st))
                .asConstraint(new ConstraintInfo(BedPlanConstraintProperties.ASSIGN_EVERY_PATIENT_TO_A_BED,
                        BedPlanConstraintProperties.ASSIGN_EVERY_PATIENT_TO_A_BED,
                        "Every patient stay should be assigned to a bed.",
                        BedPlanConstraintGroup.BED_ASSIGNMENT));
    }

    // Soft constraints
    public Constraint preferredMaximumRoomCapacity(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Stay.class)
                .filter(st -> st.getPatientPreferredMaximumRoomCapacity() != null
                        && st.getPatientPreferredMaximumRoomCapacity() < st.getRoomCapacity())
                .penalize(HardMediumSoftScore.ofSoft(8), Stay::getNightCount)
                .justifyWith((st, score) -> PreferredMaximumRoomCapacityJustification.of(st))
                .asConstraint(new ConstraintInfo(BedPlanConstraintProperties.PREFERRED_MAXIMUM_ROOM_CAPACITY,
                        BedPlanConstraintProperties.PREFERRED_MAXIMUM_ROOM_CAPACITY,
                        "A patient's assigned room should not exceed their preferred maximum room capacity.",
                        BedPlanConstraintGroup.PATIENT_PREFERENCES));
    }

    public Constraint departmentSpecialty(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Stay.class)
                .filter(st -> !st.hasDepartmentSpecialty())
                .penalize(HardMediumSoftScore.ofSoft(10), Stay::getNightCount)
                .justifyWith((st, score) -> MissingDepartmentSpecialtyJustification.of(st))
                .asConstraint(new ConstraintInfo(BedPlanConstraintProperties.DEPARTMENT_SPECIALTY,
                        BedPlanConstraintProperties.DEPARTMENT_SPECIALTY,
                        "A patient's assigned department should offer the patient's required specialty.",
                        BedPlanConstraintGroup.PATIENT_PREFERENCES));
    }

    public Constraint departmentSpecialtyNotFirstPriority(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Stay.class)
                .filter(st -> st.hasDepartmentSpecialty() && st.getSpecialtyPriority() > 1)
                .penalize(HardMediumSoftScore.ofSoft(10), stay -> (stay.getSpecialtyPriority() - 1) * stay.getNightCount())
                .justifyWith((st, score) -> DepartmentSpecialtyNotFirstPriorityJustification.of(st))
                .asConstraint(new ConstraintInfo(BedPlanConstraintProperties.DEPARTMENT_SPECIALTY_NOT_FIRST_PRIORITY,
                        BedPlanConstraintProperties.DEPARTMENT_SPECIALTY_NOT_FIRST_PRIORITY,
                        "A patient's required specialty should be the department's first-priority specialty.",
                        BedPlanConstraintGroup.PATIENT_PREFERENCES));
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
                .asConstraint(new ConstraintInfo(BedPlanConstraintProperties.PREFERRED_PATIENT_EQUIPMENT,
                        BedPlanConstraintProperties.PREFERRED_PATIENT_EQUIPMENT,
                        "A patient's assigned room should have every piece of equipment the patient prefers.",
                        BedPlanConstraintGroup.PATIENT_PREFERENCES));
    }
}
