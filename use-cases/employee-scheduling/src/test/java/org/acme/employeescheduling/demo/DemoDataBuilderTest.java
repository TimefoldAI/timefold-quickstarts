package org.acme.employeescheduling.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.stream.Stream;

import org.acme.employeescheduling.demo.DemoDataBuilder.DemoDataSize;
import org.acme.employeescheduling.dto.input.EmployeeInputDTO;
import org.acme.employeescheduling.dto.input.EmployeeScheduleInput;
import org.acme.employeescheduling.dto.input.ShiftInputDTO;
import org.junit.jupiter.api.Test;

class DemoDataBuilderTest {

    @Test
    void shouldBuildBasicData() {
        EmployeeScheduleInput problem = DemoDataBuilder.builder().withSize(DemoDataSize.BASIC).build();

        assertThat(problem.employees()).hasSize(15);
        assertThat(problem.shifts()).extracting(ShiftInputDTO::location)
                .containsOnly("Ambulatory care", "Critical care", "Pediatric care");
        assertThat(problem.shifts().stream().map(shift -> shift.start().toLocalDate()).distinct()).hasSize(14);
        assertValidShape(problem);
    }

    @Test
    void shouldBuildLargeData() {
        EmployeeScheduleInput problem = DemoDataBuilder.builder().withSize(DemoDataSize.LARGE).build();

        assertThat(problem.employees()).hasSize(50);
        assertThat(problem.shifts().stream().map(ShiftInputDTO::location).distinct()).hasSize(7);
        assertThat(problem.shifts().stream().map(shift -> shift.start().toLocalDate()).distinct()).hasSize(28);
        assertValidShape(problem);
    }

    @Test
    void shouldBeReproducible() {
        assertThat(DemoDataBuilder.builder().withSize(DemoDataSize.LARGE).build())
                .isEqualTo(DemoDataBuilder.builder().withSize(DemoDataSize.LARGE).build());
    }

    private static void assertValidShape(EmployeeScheduleInput problem) {
        assertThat(problem.employees()).extracting(EmployeeInputDTO::name).doesNotHaveDuplicates();
        // Every employee has exactly one of the required skills.
        assertThat(problem.employees()).allSatisfy(employee -> assertThat(employee.skills())
                .doesNotHaveDuplicates()
                .filteredOn(skill -> skill.equals("Doctor") || skill.equals("Nurse")).hasSize(1));
        // A day is either unavailable, undesired or desired for an employee, never two of those.
        assertThat(problem.employees()).allSatisfy(employee -> assertThat(Stream
                .of(employee.unavailableDates(), employee.undesiredDates(), employee.desiredDates())
                .flatMap(dates -> dates.stream())
                .toList()).doesNotHaveDuplicates());

        LocalDate firstDay = problem.shifts().getFirst().start().toLocalDate();
        assertThat(firstDay.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(problem.shifts()).extracting(ShiftInputDTO::id).doesNotHaveDuplicates();
        assertThat(problem.shifts()).allSatisfy(shift -> {
            assertThat(shift.end()).isAfter(shift.start());
            assertThat(shift.employeeName()).isNull();
        });
    }
}
