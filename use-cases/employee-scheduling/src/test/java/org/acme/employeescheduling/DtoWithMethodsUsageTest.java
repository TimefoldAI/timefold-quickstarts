package org.acme.employeescheduling;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.acme.employeescheduling.dto.EmployeeDTO;
import org.acme.employeescheduling.dto.EmployeeIdDetail;
import org.acme.employeescheduling.dto.EmployeeScheduleConfigOverrides;
import org.acme.employeescheduling.dto.EmployeeScheduleInput;
import org.acme.employeescheduling.dto.EmployeeScheduleInputMetrics;
import org.acme.employeescheduling.dto.EmployeeScheduleOutput;
import org.acme.employeescheduling.dto.EmployeeScheduleOutputMetrics;
import org.acme.employeescheduling.dto.ShiftDTO;
import org.acme.employeescheduling.dto.ShiftIdDetail;
import org.junit.jupiter.api.Test;

class DtoWithMethodsUsageTest {

    @Test
    void allWithMethodsProduceUpdatedCopies() {
        var baseEmployee = new EmployeeDTO("e1", List.of("Skill1"), List.of("2024-01-01"), List.of(), List.of());
        var updatedEmployee = baseEmployee
                .withId("e2")
                .withSkills(List.of("Skill2"))
                .withUnavailableDates(List.of("2024-02-01"))
                .withUndesiredDates(List.of("2024-03-01"))
                .withDesiredDates(List.of("2024-04-01"));

        var baseShift = new ShiftDTO("s1", "2024-01-01T09:00:00", "2024-01-01T17:00:00", "Location", "Skill1", "");
        var updatedShift = baseShift
                .withId("s2")
                .withStart("2024-02-01T09:00:00")
                .withEnd("2024-02-01T17:00:00")
                .withLocation("OtherLocation")
                .withRequiredSkill("Skill2")
                .withEmployeeId("e2");

        var updatedEmployeeIdDetail = new EmployeeIdDetail("e1").withEmployeeId("e2");
        var updatedShiftIdDetail = new ShiftIdDetail("s1").withShiftId("s2");

        var updatedOverrides = new EmployeeScheduleConfigOverrides()
                .withUndesiredDayForEmployeeWeight(10L)
                .withDesiredDayForEmployeeWeight(20L)
                .withBalanceEmployeeShiftAssignmentsWeight(30L);

        var updatedInput = new EmployeeScheduleInput(List.of(baseEmployee), List.of(baseShift))
                .withEmployees(List.of(updatedEmployee))
                .withShifts(List.of(updatedShift));

        var updatedOutput = new EmployeeScheduleOutput(List.of(baseEmployee), List.of(baseShift), "0hard/0soft")
                .withEmployees(List.of(updatedEmployee))
                .withShifts(List.of(updatedShift))
                .withScore("1hard/0soft");

        var updatedInputMetrics = new EmployeeScheduleInputMetrics(1, 2, 3, 4)
                .withEmployees(10)
                .withShifts(20)
                .withLocations(30)
                .withSkills(40);

        var updatedOutputMetrics = new EmployeeScheduleOutputMetrics(1, 2, 3)
                .withTotalAssignedShifts(10)
                .withTotalUnassignedShifts(20)
                .withTotalUsedEmployees(30);

        assertThat(updatedEmployee.id()).isEqualTo("e2");
        assertThat(updatedEmployee.skills()).containsExactly("Skill2");
        assertThat(updatedEmployee.unavailableDates()).containsExactly("2024-02-01");
        assertThat(updatedEmployee.undesiredDates()).containsExactly("2024-03-01");
        assertThat(updatedEmployee.desiredDates()).containsExactly("2024-04-01");
        assertThat(updatedShift.id()).isEqualTo("s2");
        assertThat(updatedShift.start()).isEqualTo("2024-02-01T09:00:00");
        assertThat(updatedShift.end()).isEqualTo("2024-02-01T17:00:00");
        assertThat(updatedShift.location()).isEqualTo("OtherLocation");
        assertThat(updatedShift.requiredSkill()).isEqualTo("Skill2");
        assertThat(updatedShift.employeeId()).isEqualTo("e2");
        assertThat(updatedEmployeeIdDetail.employeeId()).isEqualTo("e2");
        assertThat(updatedShiftIdDetail.shiftId()).isEqualTo("s2");
        assertThat(updatedOverrides.undesiredDayForEmployeeWeight()).isEqualTo(10L);
        assertThat(updatedOverrides.desiredDayForEmployeeWeight()).isEqualTo(20L);
        assertThat(updatedOverrides.balanceEmployeeShiftAssignmentsWeight()).isEqualTo(30L);
        assertThat(updatedInput.employees()).containsExactly(updatedEmployee);
        assertThat(updatedInput.shifts()).containsExactly(updatedShift);
        assertThat(updatedOutput.employees()).containsExactly(updatedEmployee);
        assertThat(updatedOutput.shifts()).containsExactly(updatedShift);
        assertThat(updatedOutput.score()).isEqualTo("1hard/0soft");
        assertThat(updatedInputMetrics.employees()).isEqualTo(10);
        assertThat(updatedInputMetrics.shifts()).isEqualTo(20);
        assertThat(updatedInputMetrics.locations()).isEqualTo(30);
        assertThat(updatedInputMetrics.skills()).isEqualTo(40);
        assertThat(updatedOutputMetrics.totalAssignedShifts()).isEqualTo(10);
        assertThat(updatedOutputMetrics.totalUnassignedShifts()).isEqualTo(20);
        assertThat(updatedOutputMetrics.totalUsedEmployees()).isEqualTo(30);
    }
}
