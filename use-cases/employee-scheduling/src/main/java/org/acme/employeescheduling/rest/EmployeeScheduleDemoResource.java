package org.acme.employeescheduling.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.acme.employeescheduling.domain.EmployeeSchedule;
import org.acme.employeescheduling.rest.DemoDataGenerator.DemoData;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Tag(name = "Demo data", description = "Timefold-provided demo employee schedule data.")
@Path("demo-data")
public class EmployeeScheduleDemoResource {

    private final DemoDataGenerator dataGenerator;

    @Inject
    public EmployeeScheduleDemoResource(DemoDataGenerator dataGenerator) {
        this.dataGenerator = dataGenerator;
    }

    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "List of demo data represented as IDs.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DemoData.class, type = SchemaType.ARRAY))) })
    @Operation(summary = "List demo data.")
    @GET
    public DemoData[] list() {
        return DemoData.values();
    }

    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Unsolved demo schedule.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = EmployeeSchedule.class)))})
    @Operation(summary = "Find an unsolved demo schedule by ID.")
    @GET
    @Path("/{demoDataId}")
    public Response generate(@PathParam("demoDataId") DemoData demoData) {
        return Response.ok(dataGenerator.generateDemoData(demoData)).build();
    }
}
