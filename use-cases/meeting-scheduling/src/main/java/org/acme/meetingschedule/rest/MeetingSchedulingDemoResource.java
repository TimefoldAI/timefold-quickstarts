package org.acme.meetingschedule.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.acme.meetingschedule.domain.MeetingSchedule;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Tag(name = "Demo data", description = "Timefold-provided demo meeting scheduling data.")
@Path("demo-data")
public class MeetingSchedulingDemoResource {

    private final DemoDataGenerator dataGenerator;

    @Inject
    public MeetingSchedulingDemoResource(DemoDataGenerator dataGenerator) {
        this.dataGenerator = dataGenerator;
    }

    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Unsolved demo schedule.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = MeetingSchedule.class))) })
    @Operation(summary = "Find an unsolved demo schedule by ID.")
    @GET
    public Response generate() {
        return Response.ok(dataGenerator.generateDemoData()).build();
    }

}
