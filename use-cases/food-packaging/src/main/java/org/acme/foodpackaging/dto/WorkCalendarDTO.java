package org.acme.foodpackaging.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The planning horizon during which jobs may be scheduled.")
public record WorkCalendarDTO(
        @Schema(description = "First date of the horizon, inclusive, in ISO-8601 format (yyyy-MM-dd).") String fromDate,
        @Schema(description = "Last date of the horizon, exclusive, in ISO-8601 format (yyyy-MM-dd).") String toDate) {

    public WorkCalendarDTO {
        // no-op compact constructor required by repository rules
    }

    public WorkCalendarDTO withFromDate(String fromDate) {
        return new WorkCalendarDTO(fromDate, toDate);
    }

    public WorkCalendarDTO withToDate(String toDate) {
        return new WorkCalendarDTO(fromDate, toDate);
    }
}
