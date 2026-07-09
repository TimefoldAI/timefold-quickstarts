package org.acme.vehiclerouting.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongFunction;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.service.definition.api.ModelConvertor;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.vehiclerouting.domain.Location;
import org.acme.vehiclerouting.domain.Vehicle;
import org.acme.vehiclerouting.domain.VehicleRoutePlan;
import org.acme.vehiclerouting.domain.Visit;
import org.acme.vehiclerouting.dto.LocationDTO;
import org.acme.vehiclerouting.dto.VehicleDTO;
import org.acme.vehiclerouting.dto.VehicleRoutingConfigOverrides;
import org.acme.vehiclerouting.dto.VehicleRoutingInput;
import org.acme.vehiclerouting.dto.VehicleRoutingOutput;
import org.acme.vehiclerouting.dto.VisitDTO;
import org.acme.vehiclerouting.solver.VehicleRoutingConstraintProvider;

@ApplicationScoped
public class VehicleRoutingModelConvertor
        implements
        ModelConvertor<HardMediumSoftScore, VehicleRoutingInput, VehicleRoutingConfigOverrides, VehicleRoutePlan, VehicleRoutingOutput> {

    @Override
    public VehicleRoutingInput applyOutputToInput(VehicleRoutingInput modelInput, VehicleRoutingOutput modelOutput) {
        Map<String, VehicleDTO> outputVehicles = modelOutput.vehicles().stream()
                .collect(Collectors.toMap(VehicleDTO::id, vehicle -> vehicle));
        List<VehicleDTO> updatedVehicles = modelInput.vehicles().stream()
                .map(vehicle -> {
                    VehicleDTO solved = outputVehicles.get(vehicle.id());
                    if (solved == null) {
                        return vehicle;
                    }
                    return vehicle.withVisitIds(solved.visitIds());
                })
                .collect(Collectors.toList());
        return new VehicleRoutingInput(modelInput.name(), modelInput.southWestCorner(), modelInput.northEastCorner(),
                modelInput.startDateTime(), modelInput.endDateTime(), updatedVehicles, modelInput.visits());
    }

    @Override
    public VehicleRoutePlan toSolverModel(VehicleRoutingInput modelInput,
            ModelConfig<VehicleRoutingConfigOverrides> modelConfig, Optional<VehicleRoutingOutput> lastModelOutput) {
        Map<String, Visit> visitMap = new HashMap<>();
        List<Visit> visits = modelInput.visits().stream()
                .map(dto -> {
                    Visit visit = toVisit(dto);
                    visitMap.put(visit.getId(), visit);
                    return visit;
                })
                .collect(Collectors.toList());

        Map<String, Vehicle> vehicleMap = new HashMap<>();
        List<Vehicle> vehicles = modelInput.vehicles().stream()
                .map(dto -> {
                    Vehicle vehicle = toVehicle(dto);
                    vehicleMap.put(vehicle.getId(), vehicle);
                    return vehicle;
                })
                .collect(Collectors.toList());

        assignVisits(modelInput.vehicles(), vehicleMap, visitMap);
        applyLastOutput(vehicleMap, visitMap, lastModelOutput);

        VehicleRoutePlan solution = new VehicleRoutePlan(modelInput.name(),
                toLocation(modelInput.southWestCorner()), toLocation(modelInput.northEastCorner()),
                toLocalDateTime(modelInput.startDateTime()), toLocalDateTime(modelInput.endDateTime()), vehicles, visits);
        applyConstraintWeightOverrides(solution, modelConfig);
        return solution;
    }

    private Visit toVisit(VisitDTO dto) {
        return new Visit(dto.id(), dto.name(), toLocation(dto.location()), dto.demand(),
                toLocalDateTime(dto.minStartTime()), toLocalDateTime(dto.maxEndTime()),
                Duration.ofSeconds(dto.serviceDurationSeconds()));
    }

    private Vehicle toVehicle(VehicleDTO dto) {
        return new Vehicle(dto.id(), dto.capacity(), toLocation(dto.homeLocation()), toLocalDateTime(dto.departureTime()));
    }

    private static Location toLocation(LocationDTO dto) {
        return new Location(dto.latitude(), dto.longitude());
    }

    private static LocalDateTime toLocalDateTime(OffsetDateTime offsetDateTime) {
        return offsetDateTime == null ? null : offsetDateTime.toLocalDateTime();
    }

    private static OffsetDateTime toOffsetDateTime(LocalDateTime localDateTime) {
        return localDateTime == null ? null : localDateTime.atOffset(ZoneOffset.UTC);
    }

    private static void assignVisits(List<VehicleDTO> vehicleDTOs, Map<String, Vehicle> vehicleMap,
            Map<String, Visit> visitMap) {
        for (VehicleDTO dto : vehicleDTOs) {
            Vehicle vehicle = vehicleMap.get(dto.id());
            if (vehicle == null) {
                continue;
            }
            vehicle.getVisits().clear();
            for (String visitId : dto.visitIds()) {
                Visit visit = visitMap.get(visitId);
                if (visit != null) {
                    vehicle.getVisits().add(visit);
                }
            }
        }
    }

    private static void applyLastOutput(Map<String, Vehicle> vehicleMap, Map<String, Visit> visitMap,
            Optional<VehicleRoutingOutput> lastModelOutput) {
        if (lastModelOutput.isEmpty()) {
            return;
        }
        assignVisits(lastModelOutput.get().vehicles(), vehicleMap, visitMap);
    }

    private static void applyConstraintWeightOverrides(VehicleRoutePlan solution,
            ModelConfig<VehicleRoutingConfigOverrides> modelConfig) {
        if (modelConfig == null || modelConfig.overrides() == null) {
            return;
        }
        VehicleRoutingConfigOverrides overrides = modelConfig.overrides();
        // Only apply weights that are actually set (non-null) in the merged overrides.
        // A null weight means the input did not override it, so the configuration profile value
        // (or the constraint's default) is kept.
        Map<String, HardMediumSoftScore> weightOverrides = new HashMap<>();
        putIfPresent(weightOverrides, VehicleRoutingConstraintProvider.MAXIMIZE_VISITS_ASSIGNED,
                overrides.maximizeVisitsAssignedWeight(), HardMediumSoftScore::ofMedium);
        putIfPresent(weightOverrides, VehicleRoutingConstraintProvider.MINIMIZE_TRAVEL_TIME,
                overrides.minimizeTravelTimeWeight(), HardMediumSoftScore::ofSoft);
        if (!weightOverrides.isEmpty()) {
            solution.setConstraintWeightOverrides(ConstraintWeightOverrides.of(weightOverrides));
        }
    }

    private static void putIfPresent(Map<String, HardMediumSoftScore> weights, String constraintName, Long weight,
            LongFunction<HardMediumSoftScore> scoreFactory) {
        if (weight != null) {
            weights.put(constraintName, scoreFactory.apply(weight));
        }
    }

    @Override
    public VehicleRoutingOutput toModelOutput(VehicleRoutePlan solverModel) {
        List<VehicleDTO> vehicles = solverModel.getVehicles().stream().map(this::toDTO).collect(Collectors.toList());
        List<VisitDTO> visits = solverModel.getVisits().stream().map(this::toDTO).collect(Collectors.toList());
        String score = solverModel.getScore() == null ? "" : solverModel.getScore().toString();
        return new VehicleRoutingOutput(vehicles, visits, score);
    }

    private VehicleDTO toDTO(Vehicle vehicle) {
        List<String> visitIds = vehicle.getVisits().stream().map(Visit::getId).collect(Collectors.toList());
        return new VehicleDTO(vehicle.getId(), vehicle.getCapacity(), toDTO(vehicle.getHomeLocation()),
                toOffsetDateTime(vehicle.getDepartureTime()), visitIds, vehicle.getTotalDemand(),
                vehicle.getTotalDrivingTimeSeconds(), toOffsetDateTime(vehicle.arrivalTime()));
    }

    private VisitDTO toDTO(Visit visit) {
        String vehicleId = visit.getVehicle() == null ? null : visit.getVehicle().getId();
        long drivingTime = visit.getVehicle() == null ? 0L : visit.getDrivingTimeSecondsFromPreviousStandstill();
        return new VisitDTO(visit.getId(), visit.getName(), toDTO(visit.getLocation()), visit.getDemand(),
                toOffsetDateTime(visit.getMinStartTime()), toOffsetDateTime(visit.getMaxEndTime()),
                visit.getServiceDuration().toSeconds(), vehicleId,
                toOffsetDateTime(visit.getArrivalTime()), toOffsetDateTime(visit.getStartServiceTime()),
                toOffsetDateTime(visit.getDepartureTime()), drivingTime);
    }

    private static LocationDTO toDTO(Location location) {
        return new LocationDTO(location.getLatitude(), location.getLongitude());
    }
}
