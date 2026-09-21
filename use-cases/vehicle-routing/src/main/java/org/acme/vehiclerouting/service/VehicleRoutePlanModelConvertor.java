package org.acme.vehiclerouting.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.service.definition.api.ModelConvertor;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.maps.api.model.Location;

import org.acme.vehiclerouting.domain.Vehicle;
import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.acme.vehiclerouting.domain.VehicleRoutePlanConstraintProperties;
import org.acme.vehiclerouting.domain.Visit;
import org.acme.vehiclerouting.dto.input.LocationInputDTO;
import org.acme.vehiclerouting.dto.input.VehicleInputDTO;
import org.acme.vehiclerouting.dto.input.VehicleRoutePlanConfigOverrides;
import org.acme.vehiclerouting.dto.input.VehicleRoutePlanInput;
import org.acme.vehiclerouting.dto.input.VisitInputDTO;
import org.acme.vehiclerouting.dto.output.VehicleOutputDTO;
import org.acme.vehiclerouting.dto.output.VehicleRoutePlanOutput;
import org.acme.vehiclerouting.dto.output.VisitOutputDTO;

@ApplicationScoped
public class VehicleRoutePlanModelConvertor implements
        ModelConvertor<HardMediumSoftScore, VehicleRoutePlanInput, VehicleRoutePlanConfigOverrides, VehicleRoutePlan, VehicleRoutePlanOutput> {

    @Override
    public VehicleRoutePlan toSolverModel(VehicleRoutePlanInput modelInput,
            ModelConfig<VehicleRoutePlanConfigOverrides> modelConfig,
            Optional<VehicleRoutePlanOutput> lastModelOutput) {
        Map<String, Visit> visitMap = modelInput.visits().stream()
                .map(VehicleRoutePlanModelConvertor::toVisit)
                .collect(Collectors.toMap(Visit::getId, visit -> visit, (first, second) -> first, LinkedHashMap::new));
        List<Vehicle> vehicles = modelInput.vehicles().stream()
                .map(VehicleRoutePlanModelConvertor::toVehicle)
                .toList();

        VehicleRoutePlan routePlan = new VehicleRoutePlan(vehicles, List.copyOf(visitMap.values()));
        applyConstraintWeightOverrides(routePlan, modelConfig);
        applyRoutes(vehicles, visitMap, modelInput, lastModelOutput);
        return routePlan;
    }

    @Override
    public VehicleRoutePlanOutput toModelOutput(VehicleRoutePlan solverModel) {
        List<VehicleOutputDTO> vehicles = solverModel.getVehicles().stream()
                .map(vehicle -> new VehicleOutputDTO(vehicle.getId(),
                        vehicle.getVisits().stream().map(Visit::getId).toList(),
                        vehicle.getTotalDemand(), vehicle.getTotalDrivingTimeSeconds(), vehicle.arrivalTime()))
                .toList();
        List<VisitOutputDTO> visits = solverModel.getVisits().stream()
                .map(visit -> new VisitOutputDTO(visit.getId(),
                        visit.getVehicle() == null ? null : visit.getVehicle().getId(),
                        visit.getArrivalTime(), visit.getStartServiceTime(), visit.getDepartureTime(),
                        visit.getDrivingTimeSecondsFromPreviousStandstillOrNull()))
                .toList();
        return new VehicleRoutePlanOutput(vehicles, visits);
    }

    @Override
    public VehicleRoutePlanInput applyOutputToInput(VehicleRoutePlanInput modelInput,
            VehicleRoutePlanOutput modelOutput) {
        // The assignment is the route list, so overlaying the output means replacing one list per
        // vehicle; a visit that ends up in no list is unassigned, exactly as in the output.
        Map<String, VehicleOutputDTO> routeByVehicleId = modelOutput.vehicles().stream()
                .collect(Collectors.toMap(VehicleOutputDTO::id, vehicle -> vehicle));
        List<VehicleInputDTO> updatedVehicles = modelInput.vehicles().stream()
                .map(vehicle -> {
                    VehicleOutputDTO solved = routeByVehicleId.get(vehicle.id());
                    return solved == null || solved.visitIds() == null ? vehicle : vehicle.withVisitIds(solved.visitIds());
                })
                .toList();
        return modelInput.withVehicles(updatedVehicles);
    }

    private static Location toLocation(LocationInputDTO dto) {
        return new Location(dto.latitude(), dto.longitude());
    }

    private static Vehicle toVehicle(VehicleInputDTO dto) {
        return new Vehicle(dto.id(), dto.capacity(), toLocation(dto.homeLocation()), dto.departureTime());
    }

    private static Visit toVisit(VisitInputDTO dto) {
        return new Visit(dto.id(), dto.name(), toLocation(dto.location()), dto.demand(), dto.minStartTime(),
                dto.maxEndTime(), Duration.ofMinutes(dto.serviceDurationMinutes()));
    }

    /**
     * Fails fast with an actionable message instead of letting an unknown reference
     * turn into a null in the solver model and a delayed NullPointerException.
     */
    private static <T> T require(Map<String, T> map, String key, String kind) {
        T value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Unknown %s '%s'.".formatted(kind, key));
        }
        return value;
    }

    private static void applyConstraintWeightOverrides(VehicleRoutePlan routePlan,
            ModelConfig<VehicleRoutePlanConfigOverrides> modelConfig) {
        if (modelConfig == null || modelConfig.overrides() == null) {
            return;
        }
        var overrides = modelConfig.overrides();
        // Only apply weights that are actually set (non-null) in the merged overrides. A null weight means the
        // input did not override it, so the configuration profile value (or the constraint's default) is kept.
        Map<String, HardMediumSoftScore> weights = new HashMap<>();
        putIfPresent(weights, VehicleRoutePlanConstraintProperties.MINIMIZE_TRAVEL_TIME,
                overrides.minimizeTravelTimeWeight());
        if (!weights.isEmpty()) {
            routePlan.setConstraintWeightOverrides(ConstraintWeightOverrides.of(weights));
        }
    }

    private static void putIfPresent(Map<String, HardMediumSoftScore> weights, String constraintName, Long weight) {
        if (weight != null) {
            weights.put(constraintName, HardMediumSoftScore.ofSoft(weight));
        }
    }

    /**
     * Fills the list variable of every vehicle: from lastModelOutput when a halted run is being
     * recovered, and from the input's own routes otherwise. The shadow variables (vehicle,
     * previousVisit, arrivalTime) are deliberately not set here - the solver derives them when it
     * loads the solution.
     */
    private static void applyRoutes(List<Vehicle> vehicles, Map<String, Visit> visitMap,
            VehicleRoutePlanInput modelInput, Optional<VehicleRoutePlanOutput> lastModelOutput) {
        Map<String, List<String>> routeByVehicleId = lastModelOutput
                .map(output -> output.vehicles().stream()
                        .filter(vehicle -> vehicle.visitIds() != null)
                        .collect(Collectors.toMap(VehicleOutputDTO::id, VehicleOutputDTO::visitIds)))
                .orElseGet(() -> modelInput.vehicles().stream()
                        .collect(Collectors.toMap(VehicleInputDTO::id, VehicleInputDTO::visitIds)));

        for (Vehicle vehicle : vehicles) {
            List<String> visitIds = routeByVehicleId.get(vehicle.getId());
            if (visitIds == null || visitIds.isEmpty()) {
                continue;
            }
            // The solver mutates this list, so it cannot be an immutable copy of the input's.
            List<Visit> route = new ArrayList<>(visitIds.size());
            for (String visitId : visitIds) {
                route.add(require(visitMap, visitId, "visit"));
            }
            vehicle.setVisits(route);
        }
    }
}
