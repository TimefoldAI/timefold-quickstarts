package org.acme.flighcrewscheduling.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.acme.flighcrewscheduling.domain.FlightCrewSchedule;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Tag(name = "Demo data", description = "Timefold-provided demo flight crew scheduling data.")
@Path("demo-data")
public class FlightCrewSchedulingDemoResource {

    private final DemoDataGenerator dataGenerator;

    @Inject
    public FlightCrewSchedulingDemoResource(DemoDataGenerator dataGenerator) {
        this.dataGenerator = dataGenerator;
    }

    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Unsolved demo schedule.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = FlightCrewSchedule.class))) })
    @Operation(summary = "Find an unsolved demo schedule by ID.")
    @GET
    public Response generate() {
        return Response.ok(dataGenerator.generateDemoData()).build();
    }

}
