package org.acme.employeescheduling.solver;

import static ai.timefold.solver.core.api.score.stream.Joiners.equal;
import static ai.timefold.solver.core.api.score.stream.Joiners.lessThanOrEqual;
import static ai.timefold.solver.core.api.score.stream.Joiners.overlapping;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Function;

import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.service.definition.api.description.ConstraintInfo;

import org.acme.employeescheduling.domain.Employee;
import org.acme.employeescheduling.domain.Shift;

public class EmployeeSchedulingConstraintProvider implements ConstraintProvider {

    public static final String REQUIRED_SKILL = "Required skill";
    public static final String NO_OVERLAPPING_SHIFTS = "No overlapping shifts";
    public static final String AT_LEAST_10_HOURS_BETWEEN_TWO_SHIFTS = "At least 10 hours between 2 shifts";
    public static final String ONE_SHIFT_PER_DAY = "Max one shift per day";
    public static final String UNAVAILABLE_EMPLOYEE = "Unavailable employee";
    public static final String UNDESIRED_DAY_FOR_EMPLOYEE = "Undesired day for employee";
    public static final String DESIRED_DAY_FOR_EMPLOYEE = "Desired day for employee";
    public static final String BALANCE_EMPLOYEE_SHIFT_ASSIGNMENTS = "Balance employee shift assignments";

    private static int getMinuteOverlap(Shift shift1, Shift shift2) {
        LocalDateTime shift1Start = shift1.getStart();
        LocalDateTime shift1End = shift1.getEnd();
        LocalDateTime shift2Start = shift2.getStart();
        LocalDateTime shift2End = shift2.getEnd();
        return (int) Duration.between(shift1Start.isAfter(shift2Start) ? shift1Start : shift2Start,
                shift1End.isBefore(shift2End) ? shift1End : shift2End).toMinutes();
    }

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                requiredSkill(constraintFactory),
                noOverlappingShifts(constraintFactory),
                atLeast10HoursBetweenTwoShifts(constraintFactory),
                oneShiftPerDay(constraintFactory),
                unavailableEmployee(constraintFactory),
                undesiredDayForEmployee(constraintFactory),
                desiredDayForEmployee(constraintFactory),
                balanceEmployeeShiftAssignments(constraintFactory)
        };
    }

    Constraint requiredSkill(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .filter(shift -> !shift.getEmployee().getSkills().contains(shift.getRequiredSkill()))
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint(new ConstraintInfo(REQUIRED_SKILL, REQUIRED_SKILL,
                        "An employee must have the required skill to cover a shift.",
                        EmployeeScheduleConstraintGroup.SHIFT_COVERAGE));
    }

    Constraint noOverlappingShifts(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(Shift.class, equal(Shift::getEmployee),
                overlapping(Shift::getStart, Shift::getEnd))
                .penalize(HardMediumSoftScore.ONE_HARD,
                        EmployeeSchedulingConstraintProvider::getMinuteOverlap)
                .asConstraint(new ConstraintInfo(NO_OVERLAPPING_SHIFTS, NO_OVERLAPPING_SHIFTS,
                        "An employee cannot cover two shifts that overlap in time.",
                        EmployeeScheduleConstraintGroup.SHIFT_COVERAGE));
    }

    Constraint atLeast10HoursBetweenTwoShifts(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .join(Shift.class, equal(Shift::getEmployee), lessThanOrEqual(Shift::getEnd, Shift::getStart))
                .filter((firstShift,
                        secondShift) -> Duration.between(firstShift.getEnd(), secondShift.getStart()).toHours() < 10)
                .penalize(HardMediumSoftScore.ONE_HARD,
                        (firstShift, secondShift) -> {
                            long breakLength =
                                    Duration.between(firstShift.getEnd(), secondShift.getStart()).toMinutes();
                            return 600L - breakLength;
                        })
                .asConstraint(new ConstraintInfo(AT_LEAST_10_HOURS_BETWEEN_TWO_SHIFTS,
                        AT_LEAST_10_HOURS_BETWEEN_TWO_SHIFTS,
                        "An employee must have at least 10 hours rest between two consecutive shifts.",
                        EmployeeScheduleConstraintGroup.SHIFT_COVERAGE));
    }

    Constraint oneShiftPerDay(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(Shift.class, equal(Shift::getEmployee),
                equal(shift -> shift.getStart().toLocalDate()))
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint(new ConstraintInfo(ONE_SHIFT_PER_DAY, ONE_SHIFT_PER_DAY,
                        "An employee can only cover one shift per day.",
                        EmployeeScheduleConstraintGroup.SHIFT_COVERAGE));
    }

    Constraint unavailableEmployee(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .join(Employee.class, equal(Shift::getEmployee, Function.identity()))
                .flattenLast(Employee::getUnavailableDates)
                .filter(Shift::isOverlappingWithDate)
                .penalize(HardMediumSoftScore.ONE_HARD, Shift::getOverlappingDurationInMinutes)
                .asConstraint(new ConstraintInfo(UNAVAILABLE_EMPLOYEE, UNAVAILABLE_EMPLOYEE,
                        "An employee cannot be assigned to a shift on a day they are unavailable.",
                        EmployeeScheduleConstraintGroup.EMPLOYEE_AVAILABILITY));
    }

    Constraint undesiredDayForEmployee(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .join(Employee.class, equal(Shift::getEmployee, Function.identity()))
                .flattenLast(Employee::getUndesiredDates)
                .filter(Shift::isOverlappingWithDate)
                .penalize(HardMediumSoftScore.ONE_SOFT, Shift::getOverlappingDurationInMinutes)
                .asConstraint(new ConstraintInfo(UNDESIRED_DAY_FOR_EMPLOYEE, UNDESIRED_DAY_FOR_EMPLOYEE,
                        "An employee should not be assigned to a shift on a day they would prefer not to work.",
                        EmployeeScheduleConstraintGroup.EMPLOYEE_AVAILABILITY));
    }

    Constraint desiredDayForEmployee(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .join(Employee.class, equal(Shift::getEmployee, Function.identity()))
                .flattenLast(Employee::getDesiredDates)
                .filter(Shift::isOverlappingWithDate)
                .reward(HardMediumSoftScore.ONE_SOFT, Shift::getOverlappingDurationInMinutes)
                .asConstraint(new ConstraintInfo(DESIRED_DAY_FOR_EMPLOYEE, DESIRED_DAY_FOR_EMPLOYEE,
                        "An employee should be assigned to a shift on a day they would prefer to work.",
                        EmployeeScheduleConstraintGroup.EMPLOYEE_AVAILABILITY));
    }

    Constraint balanceEmployeeShiftAssignments(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .groupBy(Shift::getEmployee, ConstraintCollectors.count())
                .complement(Employee.class, e -> 0L)
                .groupBy(ConstraintCollectors.loadBalance((employee, shiftCount) -> employee,
                        (employee, shiftCount) -> shiftCount))
                .penalize(HardMediumSoftScore.ONE_SOFT,
                        loadBalance -> loadBalance.unfairness().movePointRight(6).longValue())
                .asConstraint(new ConstraintInfo(BALANCE_EMPLOYEE_SHIFT_ASSIGNMENTS,
                        BALANCE_EMPLOYEE_SHIFT_ASSIGNMENTS,
                        "Distribute shifts fairly across all employees.",
                        EmployeeScheduleConstraintGroup.WORKLOAD_BALANCE));
    }
}
