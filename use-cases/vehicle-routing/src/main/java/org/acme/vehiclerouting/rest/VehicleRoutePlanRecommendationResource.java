package org.acme.vehiclerouting.rest;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import ai.timefold.solver.core.enterprise.TimefoldSolverEnterpriseService;
import ai.timefold.solver.service.definition.api.error.ErrorInfo;

import org.acme.vehiclerouting.dto.input.RecommendationRequestInput;
import org.acme.vehiclerouting.dto.output.VehicleRecommendationDTO;
import org.acme.vehiclerouting.service.VehicleRoutePlanRecommendationService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * The recommended-assignment endpoint, which the Service module does not generate: it sits next to
 * the generated ones under the same {@code /route-plans} path
 */
@Tag(name = "Vehicle Routing recommendations",
        description = "Find where a single visit best fits into an existing route plan.")
@Path("/route-plans")
public class VehicleRoutePlanRecommendationResource {

    private static final String ENTERPRISE_REQUIRED_CODE = "RECOMMENDATIONS_NOT_AVAILABLE";
    private static final String ENTERPRISE_REQUIRED_MESSAGE =
            "Recommended assignments require Timefold Solver Enterprise Edition, which is not on the classpath. "
                    + "Reach out to Timefold to obtain a license, then run this quickstart with the enterprise profile.";

    private final VehicleRoutePlanRecommendationService recommendationService;

    @Inject
    public VehicleRoutePlanRecommendationResource(VehicleRoutePlanRecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @Operation(operationId = "recommendAssignment",
            summary = "Request recommended assignments for a visit that is not on any route yet.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "The best places the visit could take, best first.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = SchemaType.ARRAY, implementation = VehicleRecommendationDTO.class))),
            @APIResponse(responseCode = "400", description = "The visit or the plan is not usable as given.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErrorInfo.class))),
            @APIResponse(responseCode = "501",
                    description = "Recommendations require Timefold Solver Enterprise Edition.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErrorInfo.class))) })
    @POST
    @Path("recommendation")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response recommendAssignment(RecommendationRequestInput request) {
        List<VehicleRecommendationDTO> recommendations;

        if (isFeatureAvailable()) {
            recommendations = recommendationService.recommend(request.modelInput(), request.visitId());
            return Response.ok(recommendations).build();
        } else {
            return Response.status(Response.Status.NOT_IMPLEMENTED)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorInfo(ENTERPRISE_REQUIRED_CODE,
                            ENTERPRISE_REQUIRED_CODE,
                            ENTERPRISE_REQUIRED_MESSAGE,
                            ENTERPRISE_REQUIRED_MESSAGE))
                    .build();
        }
    }

    // Recommendations only available with Timefold Solver Enterprise Edition.
    public boolean isFeatureAvailable() {
        return TimefoldSolverEnterpriseService.loadOrNull(enterpriseService -> enterpriseService) != null;
    }
}
