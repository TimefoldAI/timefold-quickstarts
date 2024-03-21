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
import org.acme.bedallocation.domain.BedDesignation;
import org.acme.bedallocation.domain.Department;
import org.acme.bedallocation.domain.DepartmentSpecialism;
import org.acme.bedallocation.domain.Gender;
import org.acme.bedallocation.domain.GenderLimitation;
import org.acme.bedallocation.domain.RoomSpecialism;

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
                roomSpecialismNotExists(constraintFactory),
                roomSpecialismNotFirstPriority(constraintFactory),
                preferredPatientEquipment(constraintFactory)
        };
    }

    public Constraint sameBedInSameNight(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(BedDesignation.class,
                equal(BedDesignation::getBed))
                .filter((left, right) -> left.getStay().calculateSameNightCount(right.getStay()) > 0)
                .penalize(HardMediumSoftScore.ofHard(1000),
                        (left, right) -> left.getStay().calculateSameNightCount(right.getStay()))
                .asConstraint("sameBedInSameNight");
    }

    public Constraint femaleInMaleRoom(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(BedDesignation.class)
                .filter(bd -> bd.getPatientGender() == Gender.FEMALE
                        && bd.getRoomGenderLimitation() == GenderLimitation.MALE_ONLY)
                .penalize(HardMediumSoftScore.ofHard(50), BedDesignation::getNightCount)
                .asConstraint("femaleInMaleRoom");
    }

    public Constraint maleInFemaleRoom(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(BedDesignation.class)
                .filter(bd -> bd.getPatientGender() == Gender.MALE
                        && bd.getRoomGenderLimitation() == GenderLimitation.FEMALE_ONLY)
                .penalize(HardMediumSoftScore.ofHard(50), BedDesignation::getNightCount)
                .asConstraint("maleInFemaleRoom");
    }

    public Constraint differentGenderInSameGenderRoomInSameNight(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(BedDesignation.class)
                .filter(bd -> bd.getRoomGenderLimitation() == GenderLimitation.SAME_GENDER)
                .join(constraintFactory.forEach(BedDesignation.class)
                        .filter(bd -> bd.getRoomGenderLimitation() == GenderLimitation.SAME_GENDER),
                        equal(BedDesignation::getRoom),
                        lessThan(BedDesignation::getId),
                        filtering((left, right) -> left.getPatient().getGender() != right.getPatient().getGender()
                                && left.getStay().calculateSameNightCount(right.getStay()) > 0))
                .penalize(HardMediumSoftScore.ofHard(1000),
                        (left, right) -> left.getStay().calculateSameNightCount(right.getStay()))
                .asConstraint("differentGenderInSameGenderRoomInSameNight");
    }

    public Constraint departmentMinimumAge(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(Department.class)
                .filter(d -> d.getMinimumAge() != null)
                .join(constraintFactory.forEachIncludingUnassigned(BedDesignation.class),
                        equal(Function.identity(), BedDesignation::getDepartment),
                        greaterThan(Department::getMinimumAge, BedDesignation::getPatientAge))
                .penalize(HardMediumSoftScore.ofHard(100),
                        (d, bd) -> bd.getNightCount())
                .asConstraint("departmentMinimumAge");
    }

    public Constraint departmentMaximumAge(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(Department.class)
                .filter(d -> d.getMaximumAge() != null)
                .join(constraintFactory.forEachIncludingUnassigned(BedDesignation.class),
                        equal(Function.identity(), BedDesignation::getDepartment),
                        lessThan(Department::getMaximumAge, BedDesignation::getPatientAge))
                .penalize(HardMediumSoftScore.ofHard(100),
                        (d, bd) -> bd.getNightCount())
                .asConstraint("departmentMaximumAge");
    }

    public Constraint requiredPatientEquipment(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(BedDesignation.class)
            .filter(bedDesignation ->
                    !bedDesignation.getRoom().getEquipments().containsAll(
                        bedDesignation.getPatient().getRequiredEquipments())
            )
            .penalize(HardMediumSoftScore.ofHard(50),
                (bedDesignation) -> bedDesignation.getNightCount() * (int)
                    bedDesignation.getPatient().getRequiredEquipments().stream()
                        .filter(equipment -> bedDesignation.getRoom().getEquipments().contains(equipment)).count())
            .asConstraint("requiredPatientEquipment");
    }

    //Medium
    public Constraint assignEveryPatientToABed(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(BedDesignation.class)
                .filter(bd -> bd.getBed() == null)
                .penalize(HardMediumSoftScore.ONE_MEDIUM, BedDesignation::getNightCount)
                .asConstraint("assignEveryPatientToABed");
    }

    //Soft
    public Constraint preferredMaximumRoomCapacity(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(BedDesignation.class)
                .filter(bd -> bd.getPatient().getPreferredMaximumRoomCapacity() != null
                        && bd.getPatient().getPreferredMaximumRoomCapacity() < bd.getRoom().getCapacity())
                .penalize(HardMediumSoftScore.ofSoft(8), BedDesignation::getNightCount)
                .asConstraint("preferredMaximumRoomCapacity");
    }

    public Constraint departmentSpecialism(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(BedDesignation.class)
                .ifNotExists(DepartmentSpecialism.class,
                        equal(BedDesignation::getDepartment, DepartmentSpecialism::getDepartment),
                        equal(BedDesignation::getSpecialism, DepartmentSpecialism::getSpecialism))
                .penalize(HardMediumSoftScore.ofSoft(10), BedDesignation::getNightCount)
                .asConstraint("departmentSpecialism");
    }

    public Constraint roomSpecialismNotExists(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(BedDesignation.class)
                .filter(bd -> bd.getSpecialism() != null)
                .ifNotExists(RoomSpecialism.class,
                        equal(BedDesignation::getRoom, RoomSpecialism::getRoom),
                        equal(BedDesignation::getSpecialism, RoomSpecialism::getSpecialism))
                .penalize(HardMediumSoftScore.ofSoft(20), BedDesignation::getNightCount)
                .asConstraint("roomSpecialismNotExists");
    }

    public Constraint roomSpecialismNotFirstPriority(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(BedDesignation.class)
                .filter(bd -> bd.getSpecialism() != null)
                .join(constraintFactory.forEach(RoomSpecialism.class)
                        .filter(rs -> rs.getPriority() > 1),
                        equal(BedDesignation::getRoom, RoomSpecialism::getRoom),
                        equal(BedDesignation::getSpecialism, RoomSpecialism::getSpecialism))
                .penalize(HardMediumSoftScore.ofSoft(10),
                        (bd, rs) -> (rs.getPriority() - 1) * bd.getNightCount())
                .asConstraint("roomSpecialismNotFirstPriority");
    }

    public Constraint preferredPatientEquipment(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(BedDesignation.class)
            .filter(bedDesignation ->
                !bedDesignation.getRoom().getEquipments().containsAll(
                    bedDesignation.getPatient().getPreferredEquipments())
            )
            .penalize(HardMediumSoftScore.ofHard(50),
                (bedDesignation) -> bedDesignation.getNightCount() * (int)
                    bedDesignation.getPatient().getPreferredEquipments().stream()
                        .filter(equipment -> bedDesignation.getRoom().getEquipments().contains(equipment)).count())
            .asConstraint("preferredPatientEquipment");
    }
}
