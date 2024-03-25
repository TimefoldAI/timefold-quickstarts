package org.acme.bedallocation.solver;

import static ai.timefold.solver.core.api.score.stream.Joiners.equal;
import static ai.timefold.solver.core.api.score.stream.Joiners.filtering;
import static ai.timefold.solver.core.api.score.stream.Joiners.greaterThan;
import static ai.timefold.solver.core.api.score.stream.Joiners.lessThan;

import java.util.function.Function;

import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

import org.acme.bedallocation.domain.Department;
import org.acme.bedallocation.domain.DepartmentSpecialism;
import org.acme.bedallocation.domain.Gender;
import org.acme.bedallocation.domain.GenderLimitation;
import org.acme.bedallocation.domain.Stay;

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
                departmentSpecialism(constraintFactory),
                departmentSpecialismNotFirstPriority(constraintFactory),
                preferredPatientEquipment(constraintFactory)
        };
    }

    public Constraint sameBedInSameNight(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(Stay.class,
                equal(Stay::getBed))
                .filter((left, right) -> left.calculateSameNightCount(right) > 0)
                .penalize(HardMediumSoftScore.ofHard(1000),
                        Stay::calculateSameNightCount)
                .asConstraint("sameBedInSameNight");
    }

    public Constraint femaleInMaleRoom(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(Stay.class)
                .filter(st -> st.getPatientGender() == Gender.FEMALE
                        && st.getRoomGenderLimitation() == GenderLimitation.MALE_ONLY)
                .penalize(HardMediumSoftScore.ofHard(50), Stay::getNightCount)
                .asConstraint("femaleInMaleRoom");
    }

    public Constraint maleInFemaleRoom(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(Stay.class)
                .filter(st -> st.getPatientGender() == Gender.MALE
                        && st.getRoomGenderLimitation() == GenderLimitation.FEMALE_ONLY)
                .penalize(HardMediumSoftScore.ofHard(50), Stay::getNightCount)
                .asConstraint("maleInFemaleRoom");
    }

    public Constraint differentGenderInSameGenderRoomInSameNight(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Stay.class)
                .filter(bd -> bd.getRoomGenderLimitation() == GenderLimitation.SAME_GENDER)
                .join(constraintFactory.forEach(Stay.class)
                        .filter(st -> st.getRoomGenderLimitation() == GenderLimitation.SAME_GENDER),
                        equal(Stay::getRoom),
                        lessThan(Stay::getId),
                        filtering((left, right) -> left.getPatient().getGender() != right.getPatient().getGender()
                                && left.calculateSameNightCount(right) > 0))
                .penalize(HardMediumSoftScore.ofHard(1000),
                        Stay::calculateSameNightCount)
                .asConstraint("differentGenderInSameGenderRoomInSameNight");
    }

    public Constraint departmentMinimumAge(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(Department.class)
                .filter(d -> d.getMinimumAge() != null)
                .join(constraintFactory.forEachIncludingUnassigned(Stay.class),
                        equal(Function.identity(), Stay::getDepartment),
                        greaterThan(Department::getMinimumAge, Stay::getPatientAge))
                .penalize(HardMediumSoftScore.ofHard(100),
                        (d, st) -> st.getNightCount())
                .asConstraint("departmentMinimumAge");
    }

    public Constraint departmentMaximumAge(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(Department.class)
                .filter(d -> d.getMaximumAge() != null)
                .join(constraintFactory.forEachIncludingUnassigned(Stay.class),
                        equal(Function.identity(), Stay::getDepartment),
                        lessThan(Department::getMaximumAge, Stay::getPatientAge))
                .penalize(HardMediumSoftScore.ofHard(100),
                        (d, st) -> st.getNightCount())
                .asConstraint("departmentMaximumAge");
    }

    public Constraint requiredPatientEquipment(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Stay.class)
                .filter(st -> !st.getRoom().getEquipments().containsAll(st.getPatient().getRequiredEquipments()))
                .penalize(HardMediumSoftScore.ofHard(50),
                        (st) -> st.getNightCount() * (int) st.getPatient()
                                .getRequiredEquipments().stream()
                                .filter(equipment -> st.getRoom().getEquipments().contains(equipment)).count())
                .asConstraint("requiredPatientEquipment");
    }

    //Medium
    public Constraint assignEveryPatientToABed(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(Stay.class)
                .filter(st -> st.getBed() == null)
                .penalize(HardMediumSoftScore.ONE_MEDIUM, Stay::getNightCount)
                .asConstraint("assignEveryPatientToABed");
    }

    //Soft
    public Constraint preferredMaximumRoomCapacity(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Stay.class)
                .filter(st -> st.getPatient().getPreferredMaximumRoomCapacity() != null
                        && st.getPatient().getPreferredMaximumRoomCapacity() < st.getRoom().getCapacity())
                .penalize(HardMediumSoftScore.ofSoft(8), Stay::getNightCount)
                .asConstraint("preferredMaximumRoomCapacity");
    }

    public Constraint departmentSpecialism(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Stay.class)
                .ifNotExists(DepartmentSpecialism.class,
                        equal(Stay::getDepartment, DepartmentSpecialism::getDepartment),
                        equal(Stay::getSpecialism, DepartmentSpecialism::getSpecialism))
                .penalize(HardMediumSoftScore.ofSoft(10), Stay::getNightCount)
                .asConstraint("departmentSpecialism");
    }

    public Constraint departmentSpecialismNotFirstPriority(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Stay.class)
                .filter(st -> st.getSpecialism() != null)
                .join(constraintFactory.forEach(DepartmentSpecialism.class)
                        .filter(ds -> ds.getPriority() > 1),
                        equal(Stay::getDepartment, DepartmentSpecialism::getDepartment),
                        equal(Stay::getSpecialism, DepartmentSpecialism::getSpecialism))
                .penalize(HardMediumSoftScore.ofSoft(10),
                        (bd, rs) -> (rs.getPriority() - 1) * bd.getNightCount())
                .asConstraint("departmentSpecialismNotFirstPriority");
    }

    public Constraint preferredPatientEquipment(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Stay.class)
                .filter(bedDesignation -> !bedDesignation.getRoom().getEquipments().containsAll(
                        bedDesignation.getPatient().getPreferredEquipments()))
                .penalize(HardMediumSoftScore.ofHard(50),
                        (st) -> st.getNightCount() * (int) st.getPatient()
                                .getPreferredEquipments().stream()
                                .filter(equipment -> st.getRoom().getEquipments().contains(equipment)).count())
                .asConstraint("preferredPatientEquipment");
    }
}
