package org.acme.employeescheduling.domain.justification;

import java.time.LocalDate;
import java.util.Map;

import ai.timefold.solver.core.api.score.stream.common.LoadBalance;
import ai.timefold.solver.service.definition.api.ModelConstraintJustification;

import org.acme.employeescheduling.domain.Employee;
import org.acme.employeescheduling.domain.Shift;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Common contract for every employee scheduling justification.
 * <p>
 * Each implementation is a record dedicated to exactly one thing that is being justified, so that the Timefold Platform can
 * both render a human-readable {@link #getDescription() description} and expose the individual facts behind it through the
 * OpenAPI schema.
 * <p>
 * Every implementation must be listed in the {@link Schema#oneOf()} below, otherwise it does not show up in the generated
 * OpenAPI schema.
 */
@Schema(description = "Explains why an employee scheduling constraint was matched.",
        oneOf = {
                // Hard constraints
                EmployeeScheduleJustification.MissingRequiredSkillJustification.class,
                EmployeeScheduleJustification.OverlappingShiftJustification.class,
                EmployeeScheduleJustification.MinimumRestJustification.class,
                EmployeeScheduleJustification.MaxOneShiftPerDayJustification.class,
                EmployeeScheduleJustification.UnavailableEmployeeJustification.class,

                // Soft constraints
                EmployeeScheduleJustification.UndesiredDayJustification.class,
                EmployeeScheduleJustification.DesiredDayJustification.class,
                EmployeeScheduleJustification.ShiftAssignmentBalanceJustification.class
        })
public interface EmployeeScheduleJustification extends ModelConstraintJustification {

    /**
     * @return never null, a human-readable explanation of the constraint match
     */
    String getDescription();

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    @Schema(description = "An employee is assigned to a shift that requires a skill they don't have.",
            allOf = { EmployeeScheduleJustification.class })
    record MissingRequiredSkillJustification(
            @Schema(description = "The name of the employee.") String employee,
            @Schema(description = "The id of the shift.") String shift,
            @Schema(description = "The skill the shift requires.") String requiredSkill)
            implements
                EmployeeScheduleJustification {

        public static MissingRequiredSkillJustification of(Shift shift) {
            return new MissingRequiredSkillJustification(shift.getEmployee().name(), shift.getId(),
                    shift.getRequiredSkill());
        }

        @Override
        public String getDescription() {
            return "Employee '%s' is assigned to shift '%s', but lacks its required skill '%s'."
                    .formatted(employee, shift, requiredSkill);
        }
    }

    @Schema(description = "An employee is assigned to two shifts that overlap in time.",
            allOf = { EmployeeScheduleJustification.class })
    record OverlappingShiftJustification(
            @Schema(description = "The name of the double-booked employee.") String employee,
            @Schema(description = "The id of the first shift.") String shift1,
            @Schema(description = "The id of the second shift.") String shift2,
            @Schema(description = "The number of minutes both shifts overlap.") int overlappingMinutes)
            implements
                EmployeeScheduleJustification {

        public static OverlappingShiftJustification of(Shift shift1, Shift shift2) {
            return new OverlappingShiftJustification(shift1.getEmployee().name(), shift1.getId(), shift2.getId(),
                    shift1.getOverlappingMinutes(shift2));
        }

        @Override
        public String getDescription() {
            return "Employee '%s' is assigned to shifts '%s' and '%s', which overlap by %d minute(s)."
                    .formatted(employee, shift1, shift2, overlappingMinutes);
        }
    }

    @Schema(description = "An employee has less than 10 hours of rest between two shifts.",
            allOf = { EmployeeScheduleJustification.class })
    record MinimumRestJustification(
            @Schema(description = "The name of the employee.") String employee,
            @Schema(description = "The id of the earlier shift.") String firstShift,
            @Schema(description = "The id of the later shift.") String secondShift,
            @Schema(description = "The number of minutes of rest between both shifts.") int restMinutes)
            implements
                EmployeeScheduleJustification {

        public static MinimumRestJustification of(Shift firstShift, Shift secondShift) {
            return new MinimumRestJustification(firstShift.getEmployee().name(), firstShift.getId(),
                    secondShift.getId(), firstShift.getMinutesUntil(secondShift));
        }

        @Override
        public String getDescription() {
            return "Employee '%s' only has %d minute(s) of rest between shift '%s' and shift '%s'."
                    .formatted(employee, restMinutes, firstShift, secondShift);
        }
    }

    @Schema(description = "An employee is assigned to two shifts that start on the same day.",
            allOf = { EmployeeScheduleJustification.class })
    record MaxOneShiftPerDayJustification(
            @Schema(description = "The name of the employee.") String employee,
            @Schema(description = "The day both shifts start on, in ISO-8601 date format.") String date,
            @Schema(description = "The id of the first shift.") String shift1,
            @Schema(description = "The id of the second shift.") String shift2)
            implements
                EmployeeScheduleJustification {

        public static MaxOneShiftPerDayJustification of(Shift shift1, Shift shift2) {
            return new MaxOneShiftPerDayJustification(shift1.getEmployee().name(), shift1.getStartDate().toString(),
                    shift1.getId(), shift2.getId());
        }

        @Override
        public String getDescription() {
            return "Employee '%s' is assigned to both shift '%s' and shift '%s' on %s."
                    .formatted(employee, shift1, shift2, date);
        }
    }

    @Schema(description = "An employee is assigned to a shift on a day they are unavailable.",
            allOf = { EmployeeScheduleJustification.class })
    record UnavailableEmployeeJustification(
            @Schema(description = "The name of the employee.") String employee,
            @Schema(description = "The id of the shift.") String shift,
            @Schema(description = "The unavailable day, in ISO-8601 date format.") String date,
            @Schema(description = "The number of minutes of the shift that fall on that day.") int minutes)
            implements
                EmployeeScheduleJustification {

        public static UnavailableEmployeeJustification of(Shift shift, LocalDate date) {
            return new UnavailableEmployeeJustification(shift.getEmployee().name(), shift.getId(), date.toString(),
                    shift.getOverlappingMinutes(date));
        }

        @Override
        public String getDescription() {
            return "Employee '%s' is unavailable on %s, but works %d minute(s) of shift '%s' on that day."
                    .formatted(employee, date, minutes, shift);
        }
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    @Schema(description = "An employee is assigned to a shift on a day they would rather not work.",
            allOf = { EmployeeScheduleJustification.class })
    record UndesiredDayJustification(
            @Schema(description = "The name of the employee.") String employee,
            @Schema(description = "The id of the shift.") String shift,
            @Schema(description = "The undesired day, in ISO-8601 date format.") String date,
            @Schema(description = "The number of minutes of the shift that fall on that day.") int minutes)
            implements
                EmployeeScheduleJustification {

        public static UndesiredDayJustification of(Shift shift, LocalDate date) {
            return new UndesiredDayJustification(shift.getEmployee().name(), shift.getId(), date.toString(),
                    shift.getOverlappingMinutes(date));
        }

        @Override
        public String getDescription() {
            return "Employee '%s' would rather not work on %s, but works %d minute(s) of shift '%s' on that day."
                    .formatted(employee, date, minutes, shift);
        }
    }

    @Schema(description = "An employee is assigned to a shift on a day they would like to work.",
            allOf = { EmployeeScheduleJustification.class })
    record DesiredDayJustification(
            @Schema(description = "The name of the employee.") String employee,
            @Schema(description = "The id of the shift.") String shift,
            @Schema(description = "The desired day, in ISO-8601 date format.") String date,
            @Schema(description = "The number of minutes of the shift that fall on that day.") int minutes)
            implements
                EmployeeScheduleJustification {

        public static DesiredDayJustification of(Shift shift, LocalDate date) {
            return new DesiredDayJustification(shift.getEmployee().name(), shift.getId(), date.toString(),
                    shift.getOverlappingMinutes(date));
        }

        @Override
        public String getDescription() {
            return "Employee '%s' would like to work on %s and works %d minute(s) of shift '%s' on that day."
                    .formatted(employee, date, minutes, shift);
        }
    }

    @Schema(description = "The shifts are not spread evenly across the employees.",
            allOf = { EmployeeScheduleJustification.class })
    record ShiftAssignmentBalanceJustification(
            @Schema(description = "The name of the employee with the most shifts.") String mostAssignedEmployee,
            @Schema(description = "The number of shifts that employee has.") int mostAssignedCount,
            @Schema(description = "The name of the employee with the fewest shifts.") String leastAssignedEmployee,
            @Schema(description = "The number of shifts that employee has.") int leastAssignedCount)
            implements
                EmployeeScheduleJustification {

        public static ShiftAssignmentBalanceJustification of(LoadBalance<Employee> loadBalance) {
            Map.Entry<Employee, Long> most = loadBalance.loads().entrySet().stream()
                    .max(Map.Entry.comparingByValue()).orElseThrow();
            Map.Entry<Employee, Long> least = loadBalance.loads().entrySet().stream()
                    .min(Map.Entry.comparingByValue()).orElseThrow();
            return new ShiftAssignmentBalanceJustification(most.getKey().name(), most.getValue().intValue(),
                    least.getKey().name(), least.getValue().intValue());
        }

        @Override
        public String getDescription() {
            return "Employee '%s' has %d shift(s) while employee '%s' has only %d, an uneven distribution."
                    .formatted(mostAssignedEmployee, mostAssignedCount, leastAssignedEmployee, leastAssignedCount);
        }
    }
}
