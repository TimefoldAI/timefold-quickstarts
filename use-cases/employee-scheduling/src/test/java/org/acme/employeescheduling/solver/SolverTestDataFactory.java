package org.acme.employeescheduling.solver;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import org.acme.employeescheduling.dto.EmployeeDTO;
import org.acme.employeescheduling.dto.EmployeeScheduleInput;
import org.acme.employeescheduling.dto.ShiftDTO;

final class SolverTestDataFactory {

    private SolverTestDataFactory() {
    }

    static EmployeeScheduleInput createProblem() {
        LocalDate startDate =
                LocalDate.now(ZoneId.systemDefault()).with(TemporalAdjusters.next(DayOfWeek.MONDAY));

        List<EmployeeDTO> employees = List.of(
                new EmployeeDTO("Alice", List.of("Doctor"), List.of(), List.of(), List.of()),
                new EmployeeDTO("Bob", List.of("Doctor"), List.of(), List.of(), List.of()),
                new EmployeeDTO("Carol", List.of("Nurse"), List.of(), List.of(), List.of()),
                new EmployeeDTO("Dan", List.of("Nurse"), List.of(), List.of(), List.of()),
                new EmployeeDTO("Eve", List.of("Doctor", "Nurse"), List.of(), List.of(), List.of()));

        List<ShiftDTO> shifts = new ArrayList<>();
        String[] skills = { "Doctor", "Nurse" };
        int shiftId = 0;
        for (int day = 0; day < 7; day++) {
            LocalDate date = startDate.plusDays(day);
            for (String skill : skills) {
                LocalDateTime shiftStart = date.atTime(LocalTime.of(9, 0));
                LocalDateTime shiftEnd = date.atTime(LocalTime.of(17, 0));
                shifts.add(new ShiftDTO(Integer.toString(shiftId++), shiftStart.toString(), shiftEnd.toString(),
                        "Clinic", skill, ""));
            }
        }

        return new EmployeeScheduleInput(employees, shifts);
    }
}
