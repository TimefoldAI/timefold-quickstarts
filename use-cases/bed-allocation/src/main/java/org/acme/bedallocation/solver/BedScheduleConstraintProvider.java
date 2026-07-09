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

import org.acme.bedallocation.domain.Department;
import org.acme.bedallocation.domain.Gender;
import org.acme.bedallocation.domain.GenderLimitation;
import org.acme.bedallocation.domain.Stay;

public class BedScheduleConstraintProvider implements ConstraintProvider {

    public static final String SAME_BED_IN_SAME_NIGHT = "Same bed in same night";
    public static final String FEMALE_IN_MALE_ROOM = "Female in male room";
    public static final String MALE_IN_FEMALE_ROOM = "Male in female room";
    public static final String DIFFERENT_GENDER_IN_SAME_GENDER_ROOM = "Different gender in same gender room in same night";
    public static final String DEPARTMENT_MINIMUM_AGE = "Department minimum age";
    public static final String DEPARTMENT_MAXIMUM_AGE = "Department maximum age";
    public static final String REQUIRED_PATIENT_EQUIPMENT = "Required patient equipment";
    public static final String ASSIGN_EVERY_PATIENT_TO_A_BED = "Assign every patient to a bed";
    public static final String PREFERRED_MAXIMUM_ROOM_CAPACITY = "Preferred maximum room capacity";
    public static final String DEPARTMENT_SPECIALTY = "Department specialty";
    public static final String DEPARTMENT_SPECIALTY_NOT_FIRST_PRIORITY = "Department specialty not first priority";
    public static final String PREFERRED_PATIENT_EQUIPMENT = "Preferred patient equipment";

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
                assignEveryPatientToABed(constraintFactory),

                // Soft constraints
                preferredMaximumRoomCapacity(constraintFactory),
                departmentSpecialty(constraintFactory),
                departmentSpecialtyNotFirstPriority(constraintFactory),
                preferredPatientEquipment(constraintFactory)
        };
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    Constraint sameBedInSameNight(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(Stay.class,
                equal(Stay::getBed))
                .filter((left, right) -> left.calculateSameNightCount(right) > 0)
                .penalize(HardMediumSoftScore.ofHard(1000),
                        Stay::calculateSameNightCount)
                .asConstraint(new ConstraintInfo(SAME_BED_IN_SAME_NIGHT, SAME_BED_IN_SAME_NIGHT,
                        "Two patients must not occupy the same bed on the same night.",
                        BedScheduleConstraintGroup.BED_OCCUPANCY));
    }

    Constraint femaleInMaleRoom(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(Stay.class)
                .filter(st -> st.getPatientGender() == Gender.FEMALE
                        && st.getRoomGenderLimitation() == GenderLimitation.MALE_ONLY)
                .penalize(HardMediumSoftScore.ofHard(50), Stay::getNightCount)
                .asConstraint(new ConstraintInfo(FEMALE_IN_MALE_ROOM, FEMALE_IN_MALE_ROOM,
                        "A female patient must not be placed in a male-only room.",
                        BedScheduleConstraintGroup.PATIENT_SAFETY));
    }

    Constraint maleInFemaleRoom(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(Stay.class)
                .filter(st -> st.getPatientGender() == Gender.MALE
                        && st.getRoomGenderLimitation() == GenderLimitation.FEMALE_ONLY)
                .penalize(HardMediumSoftScore.ofHard(50), Stay::getNightCount)
                .asConstraint(new ConstraintInfo(MALE_IN_FEMALE_ROOM, MALE_IN_FEMALE_ROOM,
                        "A male patient must not be placed in a female-only room.",
                        BedScheduleConstraintGroup.PATIENT_SAFETY));
    }

    Constraint differentGenderInSameGenderRoomInSameNight(ConstraintFactory constraintFactory) {
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
                .asConstraint(new ConstraintInfo(DIFFERENT_GENDER_IN_SAME_GENDER_ROOM, DIFFERENT_GENDER_IN_SAME_GENDER_ROOM,
                        "Patients of different genders must not share a same-gender room on the same night.",
                        BedScheduleConstraintGroup.PATIENT_SAFETY));
    }

    Constraint departmentMinimumAge(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(Department.class)
                .filter(d -> d.getMinimumAge() != null)
                .join(constraintFactory.forEachIncludingUnassigned(Stay.class),
                        equal(Function.identity(), Stay::getDepartment),
                        greaterThan(Department::getMinimumAge, Stay::getPatientAge))
                .penalize(HardMediumSoftScore.ofHard(100),
                        (d, st) -> st.getNightCount())
                .asConstraint(new ConstraintInfo(DEPARTMENT_MINIMUM_AGE, DEPARTMENT_MINIMUM_AGE,
                        "A patient must not be younger than the minimum age of the department.",
                        BedScheduleConstraintGroup.PATIENT_SAFETY));
    }

    Constraint departmentMaximumAge(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(Department.class)
                .filter(d -> d.getMaximumAge() != null)
                .join(constraintFactory.forEachIncludingUnassigned(Stay.class),
                        equal(Function.identity(), Stay::getDepartment),
                        lessThan(Department::getMaximumAge, Stay::getPatientAge))
                .penalize(HardMediumSoftScore.ofHard(100),
                        (d, st) -> st.getNightCount())
                .asConstraint(new ConstraintInfo(DEPARTMENT_MAXIMUM_AGE, DEPARTMENT_MAXIMUM_AGE,
                        "A patient must not be older than the maximum age of the department.",
                        BedScheduleConstraintGroup.PATIENT_SAFETY));
    }

    Constraint requiredPatientEquipment(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Stay.class)
                .filter(st -> !st.getRoom().getEquipments().containsAll(st.getPatientRequiredEquipments()))
                .penalize(HardMediumSoftScore.ofHard(50),
                        st -> st.getNightCount() * st.getPatientRequiredEquipments().stream()
                                .filter(equipment -> st.getRoom().getEquipments().contains(equipment)).count())
                .asConstraint(new ConstraintInfo(REQUIRED_PATIENT_EQUIPMENT, REQUIRED_PATIENT_EQUIPMENT,
                        "A room must provide all equipment required by the patient.",
                        BedScheduleConstraintGroup.PATIENT_SAFETY));
    }

    Constraint assignEveryPatientToABed(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(Stay.class)
                .filter(st -> st.getBed() == null)
                .penalize(HardMediumSoftScore.ONE_HARD, Stay::getNightCount)
                .asConstraint(new ConstraintInfo(ASSIGN_EVERY_PATIENT_TO_A_BED, ASSIGN_EVERY_PATIENT_TO_A_BED,
                        "Every patient stay must be assigned to a bed.",
                        BedScheduleConstraintGroup.BED_OCCUPANCY));
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    Constraint preferredMaximumRoomCapacity(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Stay.class)
                .filter(st -> st.getPatientPreferredMaximumRoomCapacity() != null
                        && st.getPatientPreferredMaximumRoomCapacity() < st.getRoom().getCapacity())
                .penalize(HardMediumSoftScore.ofSoft(8), Stay::getNightCount)
                .asConstraint(new ConstraintInfo(PREFERRED_MAXIMUM_ROOM_CAPACITY, PREFERRED_MAXIMUM_ROOM_CAPACITY,
                        "A patient prefers a room no larger than the preferred maximum capacity.",
                        BedScheduleConstraintGroup.PATIENT_COMFORT));
    }

    Constraint departmentSpecialty(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Stay.class)
                .filter(st -> !st.hasDepartmentSpecialty())
                .penalize(HardMediumSoftScore.ofSoft(10), Stay::getNightCount)
                .asConstraint(new ConstraintInfo(DEPARTMENT_SPECIALTY, DEPARTMENT_SPECIALTY,
                        "A patient should stay in a department that handles their specialty.",
                        BedScheduleConstraintGroup.PATIENT_COMFORT));
    }

    Constraint departmentSpecialtyNotFirstPriority(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Stay.class)
                .filter(st -> st.hasDepartmentSpecialty() && st.getSpecialtyPriority() > 1)
                .penalize(HardMediumSoftScore.ofSoft(10),
                        stay -> (long) (stay.getSpecialtyPriority() - 1) * stay.getNightCount())
                .asConstraint(new ConstraintInfo(DEPARTMENT_SPECIALTY_NOT_FIRST_PRIORITY,
                        DEPARTMENT_SPECIALTY_NOT_FIRST_PRIORITY,
                        "A patient should stay in a department where their specialty is the first priority.",
                        BedScheduleConstraintGroup.PATIENT_COMFORT));
    }

    Constraint preferredPatientEquipment(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Stay.class)
                .filter(st -> !st.getRoom().getEquipments().containsAll(st.getPatientPreferredEquipments()))
                .penalize(HardMediumSoftScore.ofSoft(50),
                        st -> st.getNightCount() * st.getPatientPreferredEquipments().stream()
                                .filter(equipment -> !st.getRoom().getEquipments().contains(equipment)).count())
                .asConstraint(new ConstraintInfo(PREFERRED_PATIENT_EQUIPMENT, PREFERRED_PATIENT_EQUIPMENT,
                        "A room should provide all equipment preferred by the patient.",
                        BedScheduleConstraintGroup.PATIENT_COMFORT));
    }
}
