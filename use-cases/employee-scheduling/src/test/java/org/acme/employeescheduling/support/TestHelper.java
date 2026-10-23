package org.acme.employeescheduling.support;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashSet;
import java.util.List;

import org.acme.employeescheduling.domain.Employee;
import org.acme.employeescheduling.domain.Shift;
import org.acme.employeescheduling.dto.input.EmployeeInputDTO;
import org.acme.employeescheduling.dto.input.EmployeeScheduleConfigOverrides;
import org.acme.employeescheduling.dto.input.EmployeeScheduleInput;
import org.acme.employeescheduling.dto.input.ShiftInputDTO;
import org.acme.employeescheduling.dto.output.EmployeeScheduleOutput;
import org.acme.employeescheduling.dto.output.ShiftOutputDTO;

// To keep our production classes as simple as possible, we've added these methods to help construct the data needed for testing.
public final class TestHelper {

    public static final LocalDate DAY_1 = LocalDate.of(2021, 2, 1);
    public static final LocalDate DAY_2 = DAY_1.plusDays(1);

    private TestHelper() {
    }

    public static OffsetDateTime at(LocalDate date, int hour) {
        return OffsetDateTime.of(date, LocalTime.of(hour, 0), ZoneOffset.UTC);
    }

    // ************************************************************************
    // DTOs
    // ************************************************************************

    public static EmployeeScheduleInput input(List<EmployeeInputDTO> employees, List<ShiftInputDTO> shifts) {
        return new EmployeeScheduleInput(employees, shifts);
    }

    /**
     * A small but feasible problem: three employees and four eight-hour shifts over two days, a morning shift that
     * needs a nurse and an afternoon shift that needs a doctor on each day.
     */
    public static EmployeeScheduleInput createProblem() {
        List<EmployeeInputDTO> employees = List.of(
                anEmployeeDTO("Amy").skills("Nurse").unavailableDates(DAY_2).build(),
                anEmployeeDTO("Beth").skills("Doctor").desiredDates(DAY_1).build(),
                anEmployeeDTO("Carl").skills("Nurse", "Doctor").undesiredDates(DAY_1).build());
        List<ShiftInputDTO> shifts = List.of(
                aShiftDTO("s1").start(at(DAY_1, 6)).requiredSkill("Nurse").build(),
                aShiftDTO("s2").start(at(DAY_1, 14)).requiredSkill("Doctor").build(),
                aShiftDTO("s3").start(at(DAY_2, 6)).requiredSkill("Nurse").build(),
                aShiftDTO("s4").start(at(DAY_2, 14)).requiredSkill("Doctor").build());
        return input(employees, shifts);
    }

    public static EmployeeScheduleConfigOverrides configOverrides(Long undesiredDayForEmployeeWeight,
            Long desiredDayForEmployeeWeight, Long balanceEmployeeShiftAssignmentsWeight) {
        return new EmployeeScheduleConfigOverrides(undesiredDayForEmployeeWeight, desiredDayForEmployeeWeight,
                balanceEmployeeShiftAssignmentsWeight);
    }

    public static EmployeeDTOBuilder anEmployeeDTO(String name) {
        return new EmployeeDTOBuilder(name);
    }

    public static ShiftDTOBuilder aShiftDTO(String id) {
        return new ShiftDTOBuilder(id);
    }

    public static ShiftOutputDTO shiftOutputDTO(String id, String employeeName) {
        return new ShiftOutputDTO(id, employeeName);
    }

    public static EmployeeScheduleOutput output(List<ShiftOutputDTO> shifts) {
        return new EmployeeScheduleOutput(shifts);
    }

    // ************************************************************************
    // Solver model
    // ************************************************************************

    public static EmployeeBuilder anEmployee(String name) {
        return new EmployeeBuilder(name);
    }

    public static ShiftBuilder aShift(String id) {
        return new ShiftBuilder(id);
    }

    public static final class EmployeeBuilder {

        private final String name;
        private List<String> skills = List.of();
        private List<LocalDate> unavailableDates = List.of();
        private List<LocalDate> undesiredDates = List.of();
        private List<LocalDate> desiredDates = List.of();

        private EmployeeBuilder(String name) {
            this.name = name;
        }

        public EmployeeBuilder skills(String... skills) {
            this.skills = List.of(skills);
            return this;
        }

        public EmployeeBuilder unavailableDates(LocalDate... dates) {
            this.unavailableDates = List.of(dates);
            return this;
        }

        public EmployeeBuilder undesiredDates(LocalDate... dates) {
            this.undesiredDates = List.of(dates);
            return this;
        }

        public EmployeeBuilder desiredDates(LocalDate... dates) {
            this.desiredDates = List.of(dates);
            return this;
        }

        public Employee build() {
            return new Employee(name, new LinkedHashSet<>(skills),
                    new LinkedHashSet<>(unavailableDates), new LinkedHashSet<>(undesiredDates),
                    new LinkedHashSet<>(desiredDates));
        }
    }

    /** Builds an eight-hour shift by default. */
    public static final class ShiftBuilder {

        private final String id;
        private OffsetDateTime start = at(DAY_1, 9);
        private OffsetDateTime end;
        private String location = "Location";
        private String requiredSkill = "Skill";
        private Employee employee;

        private ShiftBuilder(String id) {
            this.id = id;
        }

        public ShiftBuilder start(OffsetDateTime start) {
            this.start = start;
            return this;
        }

        public ShiftBuilder end(OffsetDateTime end) {
            this.end = end;
            return this;
        }

        public ShiftBuilder location(String location) {
            this.location = location;
            return this;
        }

        public ShiftBuilder requiredSkill(String requiredSkill) {
            this.requiredSkill = requiredSkill;
            return this;
        }

        public ShiftBuilder employee(Employee employee) {
            this.employee = employee;
            return this;
        }

        public Shift build() {
            return new Shift(id, start, end == null ? start.plusHours(8) : end, location, requiredSkill, employee);
        }
    }

    public static final class EmployeeDTOBuilder {

        private final String name;
        private List<String> skills = List.of("Skill");
        private List<LocalDate> unavailableDates = List.of();
        private List<LocalDate> undesiredDates = List.of();
        private List<LocalDate> desiredDates = List.of();

        private EmployeeDTOBuilder(String name) {
            this.name = name;
        }

        public EmployeeDTOBuilder skills(String... skills) {
            this.skills = List.of(skills);
            return this;
        }

        public EmployeeDTOBuilder unavailableDates(LocalDate... dates) {
            this.unavailableDates = List.of(dates);
            return this;
        }

        public EmployeeDTOBuilder undesiredDates(LocalDate... dates) {
            this.undesiredDates = List.of(dates);
            return this;
        }

        public EmployeeDTOBuilder desiredDates(LocalDate... dates) {
            this.desiredDates = List.of(dates);
            return this;
        }

        public EmployeeInputDTO build() {
            return new EmployeeInputDTO(name, skills, unavailableDates, undesiredDates,
                    desiredDates);
        }
    }

    /** Builds an eight-hour shift by default. */
    public static final class ShiftDTOBuilder {

        private final String id;
        private OffsetDateTime start = at(DAY_1, 9);
        private OffsetDateTime end;
        private String location = "Location";
        private String requiredSkill = "Skill";
        private String employeeName;

        private ShiftDTOBuilder(String id) {
            this.id = id;
        }

        public ShiftDTOBuilder start(OffsetDateTime start) {
            this.start = start;
            return this;
        }

        public ShiftDTOBuilder end(OffsetDateTime end) {
            this.end = end;
            return this;
        }

        public ShiftDTOBuilder requiredSkill(String requiredSkill) {
            this.requiredSkill = requiredSkill;
            return this;
        }

        public ShiftDTOBuilder employeeName(String employeeName) {
            this.employeeName = employeeName;
            return this;
        }

        public ShiftInputDTO build() {
            return new ShiftInputDTO(id, start, end == null ? start.plusHours(8) : end, location, requiredSkill,
                    employeeName);
        }
    }
}
