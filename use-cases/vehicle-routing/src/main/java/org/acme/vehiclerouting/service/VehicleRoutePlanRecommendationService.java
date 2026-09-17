package org.acme.vehiclerouting.service;

import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.enrichment.SolverModelEnricherService;

import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.acme.vehiclerouting.domain.Visit;
import org.acme.vehiclerouting.dto.input.VehicleInputDTO;
import org.acme.vehiclerouting.dto.input.VehicleRoutePlanInput;
import org.acme.vehiclerouting.dto.recommendation.ScoreAnalysisDTO;
import org.acme.vehiclerouting.dto.recommendation.VehicleRecommendation;
import org.acme.vehiclerouting.dto.recommendation.VehicleRecommendationDTO;

@ApplicationScoped
public class VehicleRoutePlanRecommendationService {

    /**
     * Recommendations come back best-first, and a caller is picking one by hand; beyond a handful
     * the rest are noise.
     */
    private static final int MAX_RECOMMENDATION_COUNT = 5;

    private final SolutionManager<VehicleRoutePlan, HardMediumSoftScore> solutionManager;
    private final VehicleRoutePlanModelConvertor modelConvertor;
    private final SolverModelEnricherService enricherService;

    @Inject
    public VehicleRoutePlanRecommendationService(SolutionManager<VehicleRoutePlan, HardMediumSoftScore> solutionManager,
            VehicleRoutePlanModelConvertor modelConvertor, SolverModelEnricherService enricherService) {
        this.solutionManager = solutionManager;
        this.modelConvertor = modelConvertor;
        this.enricherService = enricherService;
    }

    /**
     * @return the best places the visit could take, best first, including leaving it unassigned when
     *         that is among the best options; never null
     */
    public List<VehicleRecommendationDTO> recommend(VehicleRoutePlanInput modelInput, String visitId) {
        // The request is checked before the edition is, so that a caller gets told what is wrong with
        // it either way rather than only learning that recommendations are unavailable.
        requireKnownAndUnassigned(modelInput, visitId);

        VehicleRoutePlan routePlan = toSolverModel(modelInput);
        Visit visit = routePlan.getVisits().stream()
                .filter(candidate -> candidate.getId().equals(visitId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown visit '%s'.".formatted(visitId)));

        return solutionManager.recommendAssignment(routePlan, visit, VehicleRoutePlanRecommendationService::toProposition)
                .stream()
                .limit(MAX_RECOMMENDATION_COUNT)
                .map(recommendation -> new VehicleRecommendationDTO(recommendation.proposition(),
                        ScoreAnalysisDTO.of(recommendation.scoreAnalysisDiff())))
                .toList();
    }

    private VehicleRoutePlan toSolverModel(VehicleRoutePlanInput modelInput) {
        VehicleRoutePlan routePlan = modelConvertor.toSolverModel(modelInput, ModelConfig.empty(), Optional.empty());
        return enricherService.enrich(routePlan);
    }

    private static VehicleRecommendation toProposition(Visit visit) {
        // The solver proposes leaving the visit unassigned too, which this reports as a null
        // proposition rather than as a route position that does not exist.
        if (visit.getVehicle() == null) {
            return null;
        }
        return new VehicleRecommendation(visit.getVehicle().getId(), visit.getVehicle().getVisits().indexOf(visit));
    }

    /**
     * A recommendation is about a visit that is in the plan but on no route yet. Rejecting anything
     */
    private static void requireKnownAndUnassigned(VehicleRoutePlanInput modelInput, String visitId) {
        if (modelInput.visits().stream().noneMatch(visit -> visit.id().equals(visitId))) {
            throw new IllegalArgumentException("Unknown visit '%s'.".formatted(visitId));
        }
        for (VehicleInputDTO vehicle : modelInput.vehicles()) {
            if (vehicle.visitIds().contains(visitId)) {
                throw new IllegalArgumentException(
                        "Visit '%s' is already assigned to vehicle '%s'.".formatted(visitId, vehicle.id()));
            }
        }
    }
}
