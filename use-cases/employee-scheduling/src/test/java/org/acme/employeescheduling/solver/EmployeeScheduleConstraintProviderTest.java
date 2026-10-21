package org.acme.employeescheduling.solver;

import static org.acme.employeescheduling.support.TestHelper.DAY_1;
import static org.acme.employeescheduling.support.TestHelper.aShift;
import static org.acme.employeescheduling.support.TestHelper.anEmployee;
import static org.acme.employeescheduling.support.TestHelper.at;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.score.stream.test.ConstraintVerifier;

import org.acme.employeescheduling.domain.Employee;
import org.acme.employeescheduling.domain.EmployeeSchedule;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class EmployeeScheduleConstraintProviderTest {

    private static final LocalDate DAY_3 = DAY_1.plusDays(2);

    private static final OffsetDateTime DAY_START_TIME = at(DAY_1, 9);
    private static final OffsetDateTime DAY_END_TIME = at(DAY_1, 17);
    private static final OffsetDateTime AFTERNOON_START_TIME = at(DAY_1, 13);
    private static final OffsetDateTime AFTERNOON_END_TIME = at(DAY_1, 21);

    private static final int EIGHT_HOURS_IN_MINUTES = (int) Duration.ofHours(8).toMinutes();
    private static final int SEVENTEEN_HOURS_IN_MINUTES = (int) Duration.ofHours(17).toMinutes();

    @Inject
    ConstraintVerifier<EmployeeScheduleConstraintProvider, EmployeeSchedule> constraintVerifier;

    @Test
    void requiredSkill() {
        Employee employeeWithoutSkill = anEmployee("Amy").build();
        constraintVerifier.verifyThat(EmployeeScheduleConstraintProvider::requiredSkill)
                .given(employeeWithoutSkill, aShift("1").requiredSkill("Skill").employee(employeeWithoutSkill).build())
                .penalizes(1);

        Employee employeeWithSkill = anEmployee("Beth").skills("Skill").build();
        constraintVerifier.verifyThat(EmployeeScheduleConstraintProvider::requiredSkill)
                .given(employeeWithSkill, aShift("2").requiredSkill("Skill").employee(employeeWithSkill).build())
                .penalizes(0);
    }

    @Test
    void overlappingShifts() {
        Employee employee1 = anEmployee("Amy").build();
        Employee employee2 = anEmployee("Beth").build();
        constraintVerifier.verifyThat(EmployeeScheduleConstraintProvider::noOverlappingShifts)
                .given(employee1, employee2,
                        aShift("1").start(DAY_START_TIME).end(DAY_END_TIME).employee(employee1).build(),
                        aShift("2").start(DAY_START_TIME).end(DAY_END_TIME).employee(employee1).build())
                .penalizesBy(EIGHT_HOURS_IN_MINUTES);

        constraintVerifier.verifyThat(EmployeeScheduleConstraintProvider::noOverlappingShifts)
                .given(employee1, employee2,
                        aShift("1").start(DAY_START_TIME).end(DAY_END_TIME).employee(employee1).build(),
                        aShift("2").start(DAY_START_TIME).end(DAY_END_TIME).employee(employee2).build())
                .penalizes(0);

        constraintVerifier.verifyThat(EmployeeScheduleConstraintProvider::noOverlappingShifts)
                .given(employee1, employee2,
                        aShift("1").start(DAY_START_TIME).end(DAY_END_TIME).employee(employee1).build(),
                        aShift("2").start(AFTERNOON_START_TIME).end(AFTERNOON_END_TIME).employee(employee1).build())
                .penalizesBy((int) Duration.ofHours(4).toMinutes());
    }

    @Test
    void oneShiftPerDay() {
        Employee employee1 = anEmployee("Amy").build();
        Employee employee2 = anEmployee("Beth").build();
        constraintVerifier.verifyThat(EmployeeScheduleConstraintProvider::oneShiftPerDay)
                .given(employee1, employee2,
                        aShift("1").start(DAY_START_TIME).end(DAY_END_TIME).employee(employee1).build(),
                        aShift("2").start(AFTERNOON_START_TIME).end(AFTERNOON_END_TIME).employee(employee1).build())
                .penalizes(1);

        constraintVerifier.verifyThat(EmployeeScheduleConstraintProvider::oneShiftPerDay)
                .given(employee1, employee2,
                        aShift("1").start(DAY_START_TIME).end(DAY_END_TIME).employee(employee1).build(),
                        aShift("2").start(DAY_START_TIME).end(DAY_END_TIME).employee(employee2).build())
                .penalizes(0);

        constraintVerifier.verifyThat(EmployeeScheduleConstraintProvider::oneShiftPerDay)
                .given(employee1, employee2,
                        aShift("1").start(DAY_START_TIME).end(DAY_END_TIME).employee(employee1).build(),
                        aShift("2").start(DAY_START_TIME.plusDays(1)).end(DAY_END_TIME.plusDays(1))
                                .employee(employee1).build())
                .penalizes(0);
    }

    @Test
    void atLeast10HoursBetweenConsecutiveShifts() {
        Employee employee1 = anEmployee("Amy").build();
        Employee employee2 = anEmployee("Beth").build();
        constraintVerifier.verifyThat(EmployeeScheduleConstraintProvider::atLeast10HoursBetweenTwoShifts)
                .given(employee1, employee2,
                        aShift("1").start(DAY_START_TIME).end(DAY_END_TIME).employee(employee1).build(),
                        aShift("2").start(AFTERNOON_END_TIME).end(DAY_START_TIME.plusDays(1)).employee(employee1)
                                .build())
                .penalizesBy(360);
        constraintVerifier.verifyThat(EmployeeScheduleConstraintProvider::atLeast10HoursBetweenTwoShifts)
                .given(employee1, employee2,
                        aShift("1").start(DAY_START_TIME).end(DAY_END_TIME).employee(employee1).build(),
                        aShift("2").start(DAY_END_TIME).end(DAY_START_TIME.plusDays(1)).employee(employee1).build())
                .penalizesBy(600);
        constraintVerifier.verifyThat(EmployeeScheduleConstraintProvider::atLeast10HoursBetweenTwoShifts)
                .given(employee1, employee2,
                        aShift("1").start(DAY_END_TIME).end(DAY_START_TIME.plusDays(1)).employee(employee1).build(),
                        aShift("2").start(DAY_START_TIME).end(DAY_END_TIME).employee(employee1).build())
                .penalizesBy(600);
        constraintVerifier.verifyThat(EmployeeScheduleConstraintProvider::atLeast10HoursBetweenTwoShifts)
                .given(employee1, employee2,
                        aShift("1").start(DAY_START_TIME).end(DAY_END_TIME).employee(employee1).build(),
                        aShift("2").start(DAY_END_TIME.plusHours(10)).end(DAY_START_TIME.plusDays(1))
                                .employee(employee1).build())
                .penalizes(0);
        constraintVerifier.verifyThat(EmployeeScheduleConstraintProvider::atLeast10HoursBetweenTwoShifts)
                .given(employee1, employee2,
                        aShift("1").start(DAY_START_TIME).end(DAY_END_TIME).employee(employee1).build(),
                        aShift("2").start(AFTERNOON_END_TIME).end(DAY_START_TIME.plusDays(1)).employee(employee2)
                                .build())
                .penalizes(0);
    }

    @Test
    void unavailableEmployee() {
        Employee employee1 = anEmployee("Amy").unavailableDates(DAY_1, DAY_3).build();
        Employee employee2 = anEmployee("Beth").build();
        constraintVerifier.verifyThat(EmployeeScheduleConstraintProvider::unavailableEmployee)
                .given(employee1, employee2,
                        aShift("1").start(DAY_START_TIME).end(DAY_END_TIME).employee(employee1).build())
                .penalizesBy(EIGHT_HOURS_IN_MINUTES);
        constraintVerifier.verifyThat(EmployeeScheduleConstraintProvider::unavailableEmployee)
                .given(employee1, employee2,
                        aShift("1").start(DAY_START_TIME.minusDays(1)).end(DAY_END_TIME).employee(employee1).build())
                .penalizesBy(SEVENTEEN_HOURS_IN_MINUTES);
        constraintVerifier.verifyThat(EmployeeScheduleConstraintProvider::unavailableEmployee)
                .given(employee1, employee2,
                        aShift("1").start(DAY_START_TIME.plusDays(1)).end(DAY_END_TIME.plusDays(1))
                                .employee(employee1).build())
                .penalizes(0);
        constraintVerifier.verifyThat(EmployeeScheduleConstraintProvider::unavailableEmployee)
                .given(employee1, employee2,
                        aShift("1").start(DAY_START_TIME).end(DAY_END_TIME).employee(employee2).build())
                .penalizes(0);
    }

    @Test
    void shiftEndingAtMidnightDoesNotTouchTheNextDay() {
        Employee employee = anEmployee("Amy").unavailableDates(DAY_1.plusDays(1)).build();
        constraintVerifier.verifyThat(EmployeeScheduleConstraintProvider::unavailableEmployee)
                .given(employee, aShift("1").start(at(DAY_1, 16)).end(at(DAY_1.plusDays(1), 0)).employee(employee)
                        .build())
                .penalizes(0);
    }

    @Test
    void undesiredDayForEmployee() {
        Employee employee1 = anEmployee("Amy").undesiredDates(DAY_1, DAY_3).build();
        Employee employee2 = anEmployee("Beth").build();
        constraintVerifier.verifyThat(EmployeeScheduleConstraintProvider::undesiredDayForEmployee)
                .given(employee1, employee2,
                        aShift("1").start(DAY_START_TIME).end(DAY_END_TIME).employee(employee1).build())
                .penalizesBy(EIGHT_HOURS_IN_MINUTES);
        constraintVerifier.verifyThat(EmployeeScheduleConstraintProvider::undesiredDayForEmployee)
                .given(employee1, employee2,
                        aShift("1").start(DAY_START_TIME.minusDays(1)).end(DAY_END_TIME).employee(employee1).build())
                .penalizesBy(SEVENTEEN_HOURS_IN_MINUTES);
        constraintVerifier.verifyThat(EmployeeScheduleConstraintProvider::undesiredDayForEmployee)
                .given(employee1, employee2,
                        aShift("1").start(DAY_START_TIME.plusDays(1)).end(DAY_END_TIME.plusDays(1))
                                .employee(employee1).build())
                .penalizes(0);
        constraintVerifier.verifyThat(EmployeeScheduleConstraintProvider::undesiredDayForEmployee)
                .given(employee1, employee2,
                        aShift("1").start(DAY_START_TIME).end(DAY_END_TIME).employee(employee2).build())
                .penalizes(0);
    }

    @Test
    void desiredDayForEmployee() {
        Employee employee1 = anEmployee("Amy").desiredDates(DAY_1, DAY_3).build();
        Employee employee2 = anEmployee("Beth").build();
        constraintVerifier.verifyThat(EmployeeScheduleConstraintProvider::desiredDayForEmployee)
                .given(employee1, employee2,
                        aShift("1").start(DAY_START_TIME).end(DAY_END_TIME).employee(employee1).build())
                .rewardsWith(EIGHT_HOURS_IN_MINUTES);
        constraintVerifier.verifyThat(EmployeeScheduleConstraintProvider::desiredDayForEmployee)
                .given(employee1, employee2,
                        aShift("1").start(DAY_START_TIME.minusDays(1)).end(DAY_END_TIME).employee(employee1).build())
                .rewardsWith(SEVENTEEN_HOURS_IN_MINUTES);
        constraintVerifier.verifyThat(EmployeeScheduleConstraintProvider::desiredDayForEmployee)
                .given(employee1, employee2,
                        aShift("1").start(DAY_START_TIME.plusDays(1)).end(DAY_END_TIME.plusDays(1))
                                .employee(employee1).build())
                .rewards(0);
        constraintVerifier.verifyThat(EmployeeScheduleConstraintProvider::desiredDayForEmployee)
                .given(employee1, employee2,
                        aShift("1").start(DAY_START_TIME).end(DAY_END_TIME).employee(employee2).build())
                .rewards(0);
    }

    @Test
    void balanceEmployeeShiftAssignments() {
        Employee employee1 = anEmployee("Amy").build();
        Employee employee2 = anEmployee("Beth").build();
        // No employees have shifts assigned; the schedule is perfectly balanced.
        constraintVerifier.verifyThat(EmployeeScheduleConstraintProvider::balanceEmployeeShiftAssignments)
                .given(employee1, employee2)
                .penalizesBy(BigDecimal.ZERO);
        // Only one employee has a shift assigned; the schedule is less balanced.
        constraintVerifier.verifyThat(EmployeeScheduleConstraintProvider::balanceEmployeeShiftAssignments)
                .given(employee1, employee2, aShift("1").employee(employee1).build())
                .penalizesByMoreThan(BigDecimal.ZERO);
        // Every employee has a shift assigned; the schedule is once again perfectly balanced.
        constraintVerifier.verifyThat(EmployeeScheduleConstraintProvider::balanceEmployeeShiftAssignments)
                .given(employee1, employee2, aShift("1").employee(employee1).build(),
                        aShift("2").employee(employee2).build())
                .penalizesBy(BigDecimal.ZERO);
    }
}
