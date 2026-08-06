package org.acme.employeescheduling.solver;

import static ai.timefold.solver.core.api.score.stream.Joiners.equal;
import static ai.timefold.solver.core.api.score.stream.Joiners.lessThanOrEqual;
import static ai.timefold.solver.core.api.score.stream.Joiners.overlapping;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.function.Function;

import ai.timefold.solver.core.api.score.HardSoftBigDecimalScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.common.LoadBalance;

import org.acme.employeescheduling.domain.Employee;
import org.acme.employeescheduling.domain.Shift;
import org.acme.employeescheduling.domain.MustWorkTogether;
import org.acme.employeescheduling.domain.ConstraintConfiguration;

public class EmployeeSchedulingConstraintProvider implements ConstraintProvider {

    private static final int MAX_MINUTES_PER_WEEK = 40 * 60;
    private static final int MAX_MINUTES_PER_MONTH = 160 * 60;

    private static int getMinuteOverlap(Shift shift1, Shift shift2) {
        LocalDateTime shift1Start = shift1.getStart();
        LocalDateTime shift1End = shift1.getEnd();
        LocalDateTime shift2Start = shift2.getStart();
        LocalDateTime shift2End = shift2.getEnd();
        return (int) Duration.between((shift1Start.isAfter(shift2Start)) ? shift1Start : shift2Start,
                (shift1End.isBefore(shift2End)) ? shift1End : shift2End).toMinutes();
    }

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // Hard constraints
                requiredSkill(constraintFactory),
                noOverlappingShifts(constraintFactory),
                atLeast10HoursBetweenTwoShifts(constraintFactory),
                oneShiftPerDay(constraintFactory),
                unavailableEmployee(constraintFactory),
                mustWorkTogetherHard(constraintFactory),
                mustWorkTogetherSoft(constraintFactory),
                maxWeeklyHoursHard(constraintFactory),
                maxWeeklyHoursSoft(constraintFactory),
                maxMonthlyHoursHard(constraintFactory),
                maxMonthlyHoursSoft(constraintFactory),
                // Goal constraints (HARD variants will only apply if configured as HARD)
                goalShiftsPerWeekHard(constraintFactory),
                goalMinutesPerWeekHard(constraintFactory),

                // Soft constraints
                undesiredDayForEmployee(constraintFactory),
                desiredDayForEmployee(constraintFactory),
                balanceEmployeeShiftAssignments(constraintFactory),
                // Goal constraints (SOFT variants)
                goalShiftsPerWeekSoft(constraintFactory),
                goalMinutesPerWeekSoft(constraintFactory)
        };
    }

    Constraint requiredSkill(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .filter(shift -> !shift.getEmployee().getSkills().contains(shift.getRequiredSkill()))
                .penalize(HardSoftBigDecimalScore.ONE_HARD)
                .asConstraint("Missing required skill");
    }

    Constraint noOverlappingShifts(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(Shift.class, equal(Shift::getEmployee),
                overlapping(Shift::getStart, Shift::getEnd))
                .penalize(HardSoftBigDecimalScore.ONE_HARD,
                        EmployeeSchedulingConstraintProvider::getMinuteOverlap)
                .asConstraint("Overlapping shift");
    }

    Constraint atLeast10HoursBetweenTwoShifts(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .join(Shift.class, equal(Shift::getEmployee), lessThanOrEqual(Shift::getEnd, Shift::getStart))
                .filter((firstShift,
                        secondShift) -> Duration.between(firstShift.getEnd(), secondShift.getStart()).toHours() < 10)
                .penalize(HardSoftBigDecimalScore.ONE_HARD,
                        (firstShift, secondShift) -> {
                            int breakLength = (int) Duration.between(firstShift.getEnd(), secondShift.getStart()).toMinutes();
                            return (10 * 60) - breakLength;
                        })
                .asConstraint("At least 10 hours between 2 shifts");
    }

    Constraint oneShiftPerDay(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(Shift.class, equal(Shift::getEmployee),
                equal(shift -> shift.getStart().toLocalDate()))
                .penalize(HardSoftBigDecimalScore.ONE_HARD)
                .asConstraint("Max one shift per day");
    }

    Constraint unavailableEmployee(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .join(Employee.class, equal(Shift::getEmployee, Function.identity()))
                .flattenLast(Employee::getUnavailableDates)
                .filter(Shift::isOverlappingWithDate)
                .penalize(HardSoftBigDecimalScore.ONE_HARD, Shift::getOverlappingDurationInMinutes)
                .asConstraint("Unavailable employee");
    }

    Constraint undesiredDayForEmployee(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .join(Employee.class, equal(Shift::getEmployee, Function.identity()))
                .flattenLast(Employee::getUndesiredDates)
                .filter(Shift::isOverlappingWithDate)
                .penalize(HardSoftBigDecimalScore.ONE_SOFT, Shift::getOverlappingDurationInMinutes)
                .asConstraint("Undesired day for employee");
    }

    Constraint desiredDayForEmployee(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .join(Employee.class, equal(Shift::getEmployee, Function.identity()))
                .flattenLast(Employee::getDesiredDates)
                .filter(Shift::isOverlappingWithDate)
                .reward(HardSoftBigDecimalScore.ONE_SOFT, Shift::getOverlappingDurationInMinutes)
                .asConstraint("Desired day for employee");
    }

    Constraint balanceEmployeeShiftAssignments(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .groupBy(Shift::getEmployee, ConstraintCollectors.count())
                .complement(Employee.class, e -> 0L) // Include all employees which are not assigned to any shift.
                .groupBy(ConstraintCollectors.loadBalance((employee, shiftCount) -> employee,
                        (employee, shiftCount) -> shiftCount))
                .penalizeBigDecimal(HardSoftBigDecimalScore.ONE_SOFT, LoadBalance::unfairness)
                .asConstraint("Balance employee shift assignments");
    }

    // Must work together - partner missing (A assigned, B missing) [HARD]
    Constraint mustWorkTogetherHard(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .join(MustWorkTogether.class,
                      equal(Shift::getEmployee, MustWorkTogether::getEmployeeA))
                .join(ConstraintConfiguration.class)
                .filter((shiftA, mw, cfg) -> cfg.getMustWorkTogetherSeverity() == ConstraintConfiguration.Severity.HARD)
                // Look for any Shift for employeeB that overlaps in time with shiftA.
                .ifNotExists(Shift.class,
                        // employeeB must match Shift.employee
                        equal((shiftA, mw, cfg) -> mw.getEmployeeB(), Shift::getEmployee),
                        // and the times must overlap
                        overlapping((shiftA, mw, cfg) -> shiftA.getStart(), (shiftA, mw, cfg) -> shiftA.getEnd(),
                                    Shift::getStart, Shift::getEnd))
                .penalize(HardSoftBigDecimalScore.ONE_HARD)
                .asConstraint("Must work together - partner missing (A assigned, B missing) [HARD]");
    }

    // Must work together - partner missing (A assigned, B missing) [SOFT]
    Constraint mustWorkTogetherSoft(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .join(MustWorkTogether.class,
                      equal(Shift::getEmployee, MustWorkTogether::getEmployeeA))
                .join(ConstraintConfiguration.class)
                .filter((shiftA, mw, cfg) -> cfg.getMustWorkTogetherSeverity() == ConstraintConfiguration.Severity.SOFT)
                .ifNotExists(Shift.class,
                        equal((shiftA, mw, cfg) -> mw.getEmployeeB(), Shift::getEmployee),
                        overlapping((shiftA, mw, cfg) -> shiftA.getStart(), (shiftA, mw, cfg) -> shiftA.getEnd(),
                                    Shift::getStart, Shift::getEnd))
                .penalize(HardSoftBigDecimalScore.ONE_SOFT)
                .asConstraint("Must work together - partner missing (A assigned, B missing) [SOFT]");
    }

    Constraint maxWeeklyHoursHard(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .groupBy(
                        Shift::getEmployee,
                        shift -> shift.getStart().get(WeekFields.ISO.weekOfWeekBasedYear()),
                        ConstraintCollectors.sumLong(shift -> Duration.between(shift.getStart(), shift.getEnd()).toMinutes()))
                .join(ConstraintConfiguration.class)
                .filter((employee, week, totalMinutes, cfg) -> totalMinutes > MAX_MINUTES_PER_WEEK
                        && cfg.getMaxWeeklySeverity() == ConstraintConfiguration.Severity.HARD)
                .penalize(HardSoftBigDecimalScore.ONE_HARD,
                        (employee, week, totalMinutes, cfg) -> (int) (totalMinutes - MAX_MINUTES_PER_WEEK))
                .asConstraint("Max weekly hours per employee [HARD]");
    }

    Constraint maxWeeklyHoursSoft(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .groupBy(
                        Shift::getEmployee,
                        shift -> shift.getStart().get(WeekFields.ISO.weekOfWeekBasedYear()),
                        ConstraintCollectors.sumLong(shift -> Duration.between(shift.getStart(), shift.getEnd()).toMinutes()))
                .join(ConstraintConfiguration.class)
                .filter((employee, week, totalMinutes, cfg) -> totalMinutes > MAX_MINUTES_PER_WEEK
                        && cfg.getMaxWeeklySeverity() == ConstraintConfiguration.Severity.SOFT)
                .penalize(HardSoftBigDecimalScore.ONE_SOFT,
                        (employee, week, totalMinutes, cfg) -> (int) (totalMinutes - MAX_MINUTES_PER_WEEK))
                .asConstraint("Max weekly hours per employee [SOFT]");
    }

    Constraint maxMonthlyHoursHard(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .groupBy(
                        Shift::getEmployee,
                        shift -> YearMonth.from(shift.getStart()),
                        ConstraintCollectors.sumLong(shift -> Duration.between(shift.getStart(), shift.getEnd()).toMinutes()))
                .join(ConstraintConfiguration.class)
                .filter((employee, month, totalMinutes, cfg) -> totalMinutes > MAX_MINUTES_PER_MONTH
                        && cfg.getMaxMonthlySeverity() == ConstraintConfiguration.Severity.HARD)
                .penalize(HardSoftBigDecimalScore.ONE_HARD,
                        (employee, month, totalMinutes, cfg) -> (int) (totalMinutes - MAX_MINUTES_PER_MONTH))
                .asConstraint("Max monthly hours per employee [HARD]");
    }

    Constraint maxMonthlyHoursSoft(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .groupBy(
                        Shift::getEmployee,
                        shift -> YearMonth.from(shift.getStart()),
                        ConstraintCollectors.sumLong(shift -> Duration.between(shift.getStart(), shift.getEnd()).toMinutes()))
                .join(ConstraintConfiguration.class)
                .filter((employee, month, totalMinutes, cfg) -> totalMinutes > MAX_MINUTES_PER_MONTH
                        && cfg.getMaxMonthlySeverity() == ConstraintConfiguration.Severity.SOFT)
                .penalize(HardSoftBigDecimalScore.ONE_SOFT,
                        (employee, month, totalMinutes, cfg) -> (int) (totalMinutes - MAX_MINUTES_PER_MONTH))
                .asConstraint("Max monthly hours per employee [SOFT]");
    }

    // Goal: number of shifts per week (HARD)
    Constraint goalShiftsPerWeekHard(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .groupBy(
                        Shift::getEmployee,
                        shift -> shift.getStart().get(WeekFields.ISO.weekOfWeekBasedYear()),
                        ConstraintCollectors.count())
                .join(ConstraintConfiguration.class)
                .filter((employee, week, shiftCount, cfg) -> cfg.getTargetShiftsPerWeek() > 0
                        && cfg.getTargetShiftsPerWeekSeverity() == ConstraintConfiguration.Severity.HARD)
                .penalize(HardSoftBigDecimalScore.ONE_HARD,
                        (employee, week, shiftCount, cfg) -> (int) Math.abs(shiftCount - cfg.getTargetShiftsPerWeek()))
                .asConstraint("Goal: target shifts per employee per week [HARD]");
    }

    // Goal: number of shifts per week (SOFT)
    Constraint goalShiftsPerWeekSoft(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .groupBy(
                        Shift::getEmployee,
                        shift -> shift.getStart().get(WeekFields.ISO.weekOfWeekBasedYear()),
                        ConstraintCollectors.count())
                .join(ConstraintConfiguration.class)
                .filter((employee, week, shiftCount, cfg) -> cfg.getTargetShiftsPerWeek() > 0
                        && cfg.getTargetShiftsPerWeekSeverity() == ConstraintConfiguration.Severity.SOFT)
                .penalize(HardSoftBigDecimalScore.ONE_SOFT,
                        (employee, week, shiftCount, cfg) -> (int) Math.abs(shiftCount - cfg.getTargetShiftsPerWeek()))
                .asConstraint("Goal: target shifts per employee per week [SOFT]");
    }

    // Goal: minutes per week (HARD)
    Constraint goalMinutesPerWeekHard(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .groupBy(
                        Shift::getEmployee,
                        shift -> shift.getStart().get(WeekFields.ISO.weekOfWeekBasedYear()),
                        ConstraintCollectors.sumLong(shift -> Duration.between(shift.getStart(), shift.getEnd()).toMinutes()))
                .join(ConstraintConfiguration.class)
                .filter((employee, week, totalMinutes, cfg) -> cfg.getTargetMinutesPerWeek() > 0
                        && cfg.getTargetMinutesPerWeekSeverity() == ConstraintConfiguration.Severity.HARD)
                .penalize(HardSoftBigDecimalScore.ONE_HARD,
                        (employee, week, totalMinutes, cfg) -> (int) Math.abs(totalMinutes - cfg.getTargetMinutesPerWeek()))
                .asConstraint("Goal: target minutes per employee per week [HARD]");
    }

    // Goal: minutes per week (SOFT)
    Constraint goalMinutesPerWeekSoft(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Shift.class)
                .groupBy(
                        Shift::getEmployee,
                        shift -> shift.getStart().get(WeekFields.ISO.weekOfWeekBasedYear()),
                        ConstraintCollectors.sumLong(shift -> Duration.between(shift.getStart(), shift.getEnd()).toMinutes()))
                .join(ConstraintConfiguration.class)
                .filter((employee, week, totalMinutes, cfg) -> cfg.getTargetMinutesPerWeek() > 0
                        && cfg.getTargetMinutesPerWeekSeverity() == ConstraintConfiguration.Severity.SOFT)
                .penalize(HardSoftBigDecimalScore.ONE_SOFT,
                        (employee, week, totalMinutes, cfg) -> (int) Math.abs(totalMinutes - cfg.getTargetMinutesPerWeek()))
                .asConstraint("Goal: target minutes per employee per week [SOFT]");
    }

}
