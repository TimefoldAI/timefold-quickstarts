package org.acme.employeescheduling.solver;

import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.count;
import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.loadBalance;
import static ai.timefold.solver.core.api.score.stream.Joiners.equal;
import static ai.timefold.solver.core.api.score.stream.Joiners.lessThanOrEqual;
import static ai.timefold.solver.core.api.score.stream.Joiners.overlapping;

import java.util.function.Function;

import ai.timefold.solver.core.api.score.HardSoftBigDecimalScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.common.LoadBalance;
import ai.timefold.solver.service.definition.api.description.ConstraintInfo;

import org.acme.employeescheduling.domain.Employee;
import org.acme.employeescheduling.domain.EmployeeScheduleConstraintProperties;
import org.acme.employeescheduling.domain.Shift;
import org.acme.employeescheduling.domain.justification.EmployeeScheduleJustification.DesiredDayJustification;
import org.acme.employeescheduling.domain.justification.EmployeeScheduleJustification.MaxOneShiftPerDayJustification;
import org.acme.employeescheduling.domain.justification.EmployeeScheduleJustification.MinimumRestJustification;
import org.acme.employeescheduling.domain.justification.EmployeeScheduleJustification.MissingRequiredSkillJustification;
import org.acme.employeescheduling.domain.justification.EmployeeScheduleJustification.OverlappingShiftJustification;
import org.acme.employeescheduling.domain.justification.EmployeeScheduleJustification.ShiftAssignmentBalanceJustification;
import org.acme.employeescheduling.domain.justification.EmployeeScheduleJustification.UnavailableEmployeeJustification;
import org.acme.employeescheduling.domain.justification.EmployeeScheduleJustification.UndesiredDayJustification;

public class EmployeeScheduleConstraintProvider implements ConstraintProvider {

    private static final int MINIMUM_REST_MINUTES = 10 * 60;

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // Hard constraints
                requiredSkill(constraintFactory),
                noOverlappingShifts(constraintFactory),
                atLeast10HoursBetweenTwoShifts(constraintFactory),
                oneShiftPerDay(constraintFactory),
                unavailableEmployee(constraintFactory),

                // Soft constraints
                undesiredDayForEmployee(constraintFactory),
                desiredDayForEmployee(constraintFactory),
                balanceEmployeeShiftAssignments(constraintFactory)
        };
    }

    Constraint requiredSkill(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .filter(shift -> !shift.getEmployee().hasSkill(shift.getRequiredSkill()))
                .penalize(HardSoftBigDecimalScore.ONE_HARD)
                .justifyWith((shift, score) -> MissingRequiredSkillJustification.of(shift))
                .asConstraint(new ConstraintInfo(EmployeeScheduleConstraintProperties.MISSING_REQUIRED_SKILL,
                        EmployeeScheduleConstraintProperties.MISSING_REQUIRED_SKILL,
                        "An employee must have the required skill of every shift they are assigned to.",
                        EmployeeScheduleConstraintGroup.SKILLS));
    }

    Constraint noOverlappingShifts(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(Shift.class, equal(Shift::getEmployee),
                overlapping(Shift::getStart, Shift::getEnd))
                .penalize(HardSoftBigDecimalScore.ONE_HARD, Shift::getOverlappingMinutes)
                .justifyWith((shift1, shift2, score) -> OverlappingShiftJustification.of(shift1, shift2))
                .asConstraint(new ConstraintInfo(EmployeeScheduleConstraintProperties.OVERLAPPING_SHIFT,
                        EmployeeScheduleConstraintProperties.OVERLAPPING_SHIFT,
                        "An employee cannot work two shifts that overlap in time.",
                        EmployeeScheduleConstraintGroup.WORKING_TIME));
    }

    Constraint atLeast10HoursBetweenTwoShifts(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .join(Shift.class, equal(Shift::getEmployee), lessThanOrEqual(Shift::getEnd, Shift::getStart))
                .filter((firstShift, secondShift) -> firstShift.getMinutesUntil(secondShift) < MINIMUM_REST_MINUTES)
                .penalize(HardSoftBigDecimalScore.ONE_HARD,
                        (firstShift, secondShift) -> MINIMUM_REST_MINUTES - firstShift.getMinutesUntil(secondShift))
                .justifyWith((firstShift, secondShift, score) -> MinimumRestJustification.of(firstShift, secondShift))
                .asConstraint(new ConstraintInfo(EmployeeScheduleConstraintProperties.AT_LEAST_10_HOURS_BETWEEN_TWO_SHIFTS,
                        EmployeeScheduleConstraintProperties.AT_LEAST_10_HOURS_BETWEEN_TWO_SHIFTS,
                        "An employee must have at least 10 hours of rest between two shifts.",
                        EmployeeScheduleConstraintGroup.WORKING_TIME));
    }

    Constraint oneShiftPerDay(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(Shift.class, equal(Shift::getEmployee),
                equal(Shift::getStartDate))
                .penalize(HardSoftBigDecimalScore.ONE_HARD)
                .justifyWith((shift1, shift2, score) -> MaxOneShiftPerDayJustification.of(shift1, shift2))
                .asConstraint(new ConstraintInfo(EmployeeScheduleConstraintProperties.MAX_ONE_SHIFT_PER_DAY,
                        EmployeeScheduleConstraintProperties.MAX_ONE_SHIFT_PER_DAY,
                        "An employee can work at most one shift that starts on a given day.",
                        EmployeeScheduleConstraintGroup.WORKING_TIME));
    }

    Constraint unavailableEmployee(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .join(Employee.class, equal(Shift::getEmployee, Function.identity()))
                .flattenLast(Employee::unavailableDates)
                .filter(Shift::isOverlappingWithDate)
                .penalize(HardSoftBigDecimalScore.ONE_HARD, Shift::getOverlappingMinutes)
                .justifyWith((shift, date, score) -> UnavailableEmployeeJustification.of(shift, date))
                .asConstraint(new ConstraintInfo(EmployeeScheduleConstraintProperties.UNAVAILABLE_EMPLOYEE,
                        EmployeeScheduleConstraintProperties.UNAVAILABLE_EMPLOYEE,
                        "An employee cannot work a shift on a day they are unavailable.",
                        EmployeeScheduleConstraintGroup.AVAILABILITY));
    }

    Constraint undesiredDayForEmployee(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .join(Employee.class, equal(Shift::getEmployee, Function.identity()))
                .flattenLast(Employee::undesiredDates)
                .filter(Shift::isOverlappingWithDate)
                .penalize(HardSoftBigDecimalScore.ONE_SOFT, Shift::getOverlappingMinutes)
                .justifyWith((shift, date, score) -> UndesiredDayJustification.of(shift, date))
                .asConstraint(new ConstraintInfo(EmployeeScheduleConstraintProperties.UNDESIRED_DAY_FOR_EMPLOYEE,
                        EmployeeScheduleConstraintProperties.UNDESIRED_DAY_FOR_EMPLOYEE,
                        "Avoid scheduling an employee on a day they would rather not work.",
                        EmployeeScheduleConstraintGroup.AVAILABILITY));
    }

    Constraint desiredDayForEmployee(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .join(Employee.class, equal(Shift::getEmployee, Function.identity()))
                .flattenLast(Employee::desiredDates)
                .filter(Shift::isOverlappingWithDate)
                .reward(HardSoftBigDecimalScore.ONE_SOFT, Shift::getOverlappingMinutes)
                .justifyWith((shift, date, score) -> DesiredDayJustification.of(shift, date))
                .asConstraint(new ConstraintInfo(EmployeeScheduleConstraintProperties.DESIRED_DAY_FOR_EMPLOYEE,
                        EmployeeScheduleConstraintProperties.DESIRED_DAY_FOR_EMPLOYEE,
                        "Prefer scheduling an employee on a day they would like to work.",
                        EmployeeScheduleConstraintGroup.AVAILABILITY));
    }

    Constraint balanceEmployeeShiftAssignments(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .groupBy(Shift::getEmployee, count())
                // Include the employees without any shift, so they count as a load of 0.
                .complement(Employee.class, employee -> 0L)
                .groupBy(loadBalance((employee, shiftCount) -> employee, (employee, shiftCount) -> shiftCount))
                .penalizeBigDecimal(HardSoftBigDecimalScore.ONE_SOFT, LoadBalance::unfairness)
                .justifyWith((loadBalance, score) -> ShiftAssignmentBalanceJustification.of(loadBalance))
                .asConstraint(new ConstraintInfo(EmployeeScheduleConstraintProperties.BALANCE_EMPLOYEE_SHIFT_ASSIGNMENTS,
                        EmployeeScheduleConstraintProperties.BALANCE_EMPLOYEE_SHIFT_ASSIGNMENTS,
                        "Spread the shifts evenly across all employees.",
                        EmployeeScheduleConstraintGroup.FAIRNESS));
    }
}
