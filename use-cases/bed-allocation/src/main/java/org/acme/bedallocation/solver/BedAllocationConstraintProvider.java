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
import org.acme.bedallocation.domain.PreferredPatientEquipment;
import org.acme.bedallocation.domain.RequiredPatientEquipment;
import org.acme.bedallocation.domain.RoomEquipment;
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
                .filter((left, right) -> left.getAdmissionPart().calculateSameNightCount(right.getAdmissionPart()) > 0)
                .penalize(HardMediumSoftScore.ofHard(1000),
                        (left, right) -> left.getAdmissionPart().calculateSameNightCount(right.getAdmissionPart()))
                .asConstraint("sameBedInSameNight");
    }

    public Constraint femaleInMaleRoom(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(BedDesignation.class)
                .filter(bedDesignation -> bedDesignation.getPatientGender() == Gender.FEMALE
                        && bedDesignation.getRoomGenderLimitation() == GenderLimitation.MALE_ONLY)
                .penalize(HardMediumSoftScore.ofHard(50), BedDesignation::getAdmissionPartNightCount)
                .asConstraint("femaleInMaleRoom");
    }

    public Constraint maleInFemaleRoom(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(BedDesignation.class)
                .filter(bedDesignation -> bedDesignation.getPatientGender() == Gender.MALE
                        && bedDesignation.getRoomGenderLimitation() == GenderLimitation.FEMALE_ONLY)
                .penalize(HardMediumSoftScore.ofHard(50), BedDesignation::getAdmissionPartNightCount)
                .asConstraint("maleInFemaleRoom");
    }

    public Constraint differentGenderInSameGenderRoomInSameNight(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(BedDesignation.class)
                .filter(bedDesignation -> bedDesignation.getRoomGenderLimitation() == GenderLimitation.SAME_GENDER)
                .join(constraintFactory.forEach(BedDesignation.class)
                        .filter(bedDesignation -> bedDesignation.getRoomGenderLimitation() == GenderLimitation.SAME_GENDER),
                        equal(BedDesignation::getRoom),
                        lessThan(BedDesignation::getId),
                        filtering((left, right) -> left.getPatient().getGender() != right.getPatient().getGender()
                                && left.getAdmissionPart().calculateSameNightCount(right.getAdmissionPart()) > 0))
                .penalize(HardMediumSoftScore.ofHard(1000),
                        (left, right) -> left.getAdmissionPart().calculateSameNightCount(right.getAdmissionPart()))
                .asConstraint("differentGenderInSameGenderRoomInSameNight");
    }

    public Constraint departmentMinimumAge(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(Department.class)
                .filter(d -> d.getMinimumAge() != null)
                .join(constraintFactory.forEachIncludingUnassigned(BedDesignation.class),
                        equal(Function.identity(), BedDesignation::getDepartment),
                        greaterThan(Department::getMinimumAge, BedDesignation::getPatientAge))
                .penalize(HardMediumSoftScore.ofHard(100),
                        (d, bedDesignation) -> bedDesignation.getAdmissionPartNightCount())
                .asConstraint("departmentMinimumAge");
    }

    public Constraint departmentMaximumAge(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(Department.class)
                .filter(d -> d.getMaximumAge() != null)
                .join(constraintFactory.forEachIncludingUnassigned(BedDesignation.class),
                        equal(Function.identity(), BedDesignation::getDepartment),
                        lessThan(Department::getMaximumAge, BedDesignation::getPatientAge))
                .penalize(HardMediumSoftScore.ofHard(100),
                        (d, bedDesignation) -> bedDesignation.getAdmissionPartNightCount())
                .asConstraint("departmentMaximumAge");
    }

    public Constraint requiredPatientEquipment(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(RequiredPatientEquipment.class)
                .join(BedDesignation.class,
                        equal(RequiredPatientEquipment::getPatient, BedDesignation::getPatient))
                .ifNotExists(RoomEquipment.class,
                        equal((rpe, bedDesignation) -> bedDesignation.getRoom(), RoomEquipment::getRoom),
                        equal((rpe, bedDesignation) -> rpe.getEquipment(), RoomEquipment::getEquipment))
                .penalize(HardMediumSoftScore.ofHard(50),
                        (rpe, bedDesignation) -> bedDesignation.getAdmissionPartNightCount())
                .asConstraint("requiredPatientEquipment");
    }

    //Medium
    public Constraint assignEveryPatientToABed(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(BedDesignation.class)
                .filter(bedDesignation -> bedDesignation.getBed() == null)
                .penalize(HardMediumSoftScore.ONE_MEDIUM, BedDesignation::getAdmissionPartNightCount)
                .asConstraint("assignEveryPatientToABed");
    }

    //Soft
    public Constraint preferredMaximumRoomCapacity(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(BedDesignation.class)
                .filter(bedDesignation -> bedDesignation.getPatient().getPreferredMaximumRoomCapacity() != null
                        && bedDesignation.getPatient().getPreferredMaximumRoomCapacity() < bedDesignation.getRoom().getCapacity())
                .penalize(HardMediumSoftScore.ofSoft(8), BedDesignation::getAdmissionPartNightCount)
                .asConstraint("preferredMaximumRoomCapacity");
    }

    public Constraint departmentSpecialism(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(BedDesignation.class)
                .ifNotExists(DepartmentSpecialism.class,
                        equal(BedDesignation::getDepartment, DepartmentSpecialism::getDepartment),
                        equal(BedDesignation::getAdmissionPartSpecialism, DepartmentSpecialism::getSpecialism))
                .penalize(HardMediumSoftScore.ofSoft(10), BedDesignation::getAdmissionPartNightCount)
                .asConstraint("departmentSpecialism");
    }

    public Constraint roomSpecialismNotExists(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(BedDesignation.class)
                .filter(bedDesignation -> bedDesignation.getAdmissionPartSpecialism() != null)
                .ifNotExists(RoomSpecialism.class,
                        equal(BedDesignation::getRoom, RoomSpecialism::getRoom),
                        equal(BedDesignation::getAdmissionPartSpecialism, RoomSpecialism::getSpecialism))
                .penalize(HardMediumSoftScore.ofSoft(20), BedDesignation::getAdmissionPartNightCount)
                .asConstraint("roomSpecialismNotExists");
    }

    public Constraint roomSpecialismNotFirstPriority(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(BedDesignation.class)
                .filter(bedDesignation -> bedDesignation.getAdmissionPartSpecialism() != null)
                .join(constraintFactory.forEach(RoomSpecialism.class)
                        .filter(rs -> rs.getPriority() > 1),
                        equal(BedDesignation::getRoom, RoomSpecialism::getRoom),
                        equal(BedDesignation::getAdmissionPartSpecialism, RoomSpecialism::getSpecialism))
                .penalize(HardMediumSoftScore.ofSoft(10),
                        (bedDesignation, roomSpecialism) -> (roomSpecialism.getPriority() - 1) * bedDesignation.getAdmissionPartNightCount())
                .asConstraint("roomSpecialismNotFirstPriority");
    }

    public Constraint preferredPatientEquipment(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(PreferredPatientEquipment.class)
                .join(BedDesignation.class,
                        equal(PreferredPatientEquipment::getPatient, BedDesignation::getPatient))
                .ifNotExists(RoomEquipment.class,
                        equal((re, bedDesignation) -> bedDesignation.getRoom(), RoomEquipment::getRoom),
                        equal((re, bedDesignation) -> re.getEquipment(), RoomEquipment::getEquipment))
                .penalize(HardMediumSoftScore.ofSoft(20),
                        (re, bedDesignation) -> bedDesignation.getAdmissionPartNightCount())
                .asConstraint("preferredPatientEquipment");
    }
}
