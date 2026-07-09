package org.acme.maintenancescheduling.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "The planning window in which maintenance jobs can be scheduled.")
public record WorkCalendarDTO(
        @Schema(description = "Unique identifier of the work calendar.") String id,
        @Schema(description = "First date of the planning window, inclusive, in ISO-8601 format (yyyy-MM-dd).") String fromDate,
        @Schema(description = "End date of the planning window, exclusive, in ISO-8601 format (yyyy-MM-dd).") String toDate) {

    public WorkCalendarDTO {
        id = id == null ? "" : id;
    }

    public WorkCalendarDTO withId(String id) {
        return new WorkCalendarDTO(id, fromDate, toDate);
    }

    public WorkCalendarDTO withFromDate(String fromDate) {
        return new WorkCalendarDTO(id, fromDate, toDate);
    }

    public WorkCalendarDTO withToDate(String toDate) {
        return new WorkCalendarDTO(id, fromDate, toDate);
    }
}
