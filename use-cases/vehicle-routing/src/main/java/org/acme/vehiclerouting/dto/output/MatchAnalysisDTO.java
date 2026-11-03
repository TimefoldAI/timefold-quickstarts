package org.acme.vehiclerouting.dto.output;

import ai.timefold.solver.service.definition.api.ModelConstraintJustification;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * One match of a constraint inside a {@link ConstraintAnalysisDTO}, and why it matched.
 * <p>
 * The justification is typed as the Service module's {@link ModelConstraintJustification} rather than
 * as this model's own justification interface, because the DTO layer must not depend on the domain
 * layer; the schema reference below points the generated OpenAPI document at the model's
 * justifications all the same.
 */
@Schema(description = "One match of a constraint, and why it matched.")
public record MatchAnalysisDTO(
        @Schema(description = "Score difference of this single match.") String score,
        @Schema(ref = "VehicleRoutePlanJustification") ModelConstraintJustification justification) {
}
