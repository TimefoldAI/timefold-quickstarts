package org.acme.employeescheduling.dto.input;

import java.time.LocalDate;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = """
        An employee who can work shifts that require one of their skills. The dates are calendar days in the \
        time zone offset of the shifts they are compared to.""")
public record EmployeeInputDTO(
        @Schema(description = "Unique name of the employee.", required = true, minLength = 1) String name,
        @Schema(description = "Skills the employee has, e.g. Nurse.", required = true,
                minItems = 1) List<String> skills,
        @Schema(description = "Days the employee cannot work at all.") List<LocalDate> unavailableDates,
        @Schema(description = "Days the employee would rather not work.") List<LocalDate> undesiredDates,
        @Schema(description = "Days the employee would like to work.") List<LocalDate> desiredDates) {

    public EmployeeInputDTO {
        unavailableDates = unavailableDates == null ? List.of() : unavailableDates;
        undesiredDates = undesiredDates == null ? List.of() : undesiredDates;
        desiredDates = desiredDates == null ? List.of() : desiredDates;
    }
}
