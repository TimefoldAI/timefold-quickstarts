package org.acme.flightcrewscheduling.dto.input;

import static java.util.Collections.emptyList;

import java.time.LocalDate;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A crew member who can be assigned to flights.")
public record EmployeeInputDTO(
        @Schema(description = "Unique identifier of the crew member.", required = true, minLength = 1) String id,
        @Schema(description = "Display name of the crew member.", required = true, minLength = 1) String name,
        @Schema(description = "IATA code of the airport this crew member starts and ends their roster at.",
                required = true, minLength = 3, maxLength = 3) String homeAirportCode,
        @Schema(description = "Skills this crew member holds, e.g. Pilot or Flight attendant.", required = true,
                minItems = 1) List<String> skills,
        @Schema(description = "Days on which this crew member cannot fly, in ISO-8601 date format.") List<LocalDate> unavailableDays) {

    public EmployeeInputDTO {
        unavailableDays = unavailableDays != null ? unavailableDays : emptyList();
    }
}
