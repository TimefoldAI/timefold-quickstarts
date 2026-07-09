package org.acme.facilitylocation.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.ModelConvertor;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.score.HardMediumSoftScore;

import org.acme.facilitylocation.domain.Consumer;
import org.acme.facilitylocation.domain.Facility;
import org.acme.facilitylocation.domain.FacilityLocationProblem;
import org.acme.facilitylocation.domain.Location;
import org.acme.facilitylocation.dto.ConsumerDTO;
import org.acme.facilitylocation.dto.FacilityDTO;
import org.acme.facilitylocation.dto.FacilityLocationConfigOverrides;
import org.acme.facilitylocation.dto.FacilityLocationInput;
import org.acme.facilitylocation.dto.FacilityLocationOutput;
import org.acme.facilitylocation.dto.LocationDTO;
import org.acme.facilitylocation.solver.FacilityLocationConstraintProvider;

@ApplicationScoped
public class FacilityLocationModelConvertor
        implements
        ModelConvertor<HardMediumSoftScore, FacilityLocationInput, FacilityLocationConfigOverrides, FacilityLocationProblem, FacilityLocationOutput> {

    @Override
    public FacilityLocationInput applyOutputToInput(FacilityLocationInput modelInput,
            FacilityLocationOutput modelOutput) {
        Map<String, String> assignmentMap = modelOutput.consumers().stream()
                .filter(c -> c.facilityId() != null)
                .collect(Collectors.toMap(ConsumerDTO::id, ConsumerDTO::facilityId));
        List<ConsumerDTO> updatedConsumers = modelInput.consumers().stream()
                .map(c -> {
                    String facilityId = assignmentMap.get(c.id());
                    boolean assigned = facilityId != null;
                    return new ConsumerDTO(c.id(), c.location(), c.demand(), facilityId, assigned);
                })
                .collect(Collectors.toList());
        return new FacilityLocationInput(modelInput.facilities(), updatedConsumers, modelInput.bounds());
    }

    @Override
    public FacilityLocationProblem toSolverModel(FacilityLocationInput modelInput,
            ModelConfig<FacilityLocationConfigOverrides> modelConfig,
            Optional<FacilityLocationOutput> lastModelOutput) {
        Map<String, Facility> facilityMap = new HashMap<>();
        List<Facility> facilities = modelInput.facilities().stream().map(dto -> {
            Facility facility = new Facility(dto.id(), dto.name(), fromDTO(dto.location()), dto.setupCost(),
                    dto.capacity());
            facilityMap.put(facility.getId(), facility);
            return facility;
        }).collect(Collectors.toList());

        List<Consumer> consumers = modelInput.consumers().stream().map(dto -> {
            Consumer consumer = new Consumer(dto.id(), fromDTO(dto.location()), dto.demand());
            if (dto.facilityId() != null) {
                consumer.setFacility(facilityMap.get(dto.facilityId()));
            }
            return consumer;
        }).collect(Collectors.toList());

        FacilityLocationProblem problem = new FacilityLocationProblem(facilities, consumers,
                Stream.concat(facilities.stream().map(Facility::getLocation),
                        consumers.stream().map(Consumer::getLocation)).reduce(Location::min).orElse(null),
                Stream.concat(facilities.stream().map(Facility::getLocation),
                        consumers.stream().map(Consumer::getLocation)).reduce(Location::max).orElse(null));

        applyConstraintWeightOverrides(problem, modelConfig);
        applyLastOutput(consumers, facilityMap, lastModelOutput);

        return problem;
    }

    private static void applyConstraintWeightOverrides(FacilityLocationProblem problem,
            ModelConfig<FacilityLocationConfigOverrides> modelConfig) {
        if (modelConfig == null || modelConfig.overrides() == null) {
            return;
        }
        FacilityLocationConfigOverrides overrides = modelConfig.overrides();
        // Only apply weights that are actually set (non-null) in the merged overrides. A null weight means the
        // input did not override it, so the configuration profile value (or the constraint's default) is kept.
        Map<String, HardMediumSoftScore> weights = new HashMap<>();
        putIfPresent(weights, FacilityLocationConstraintProvider.FACILITY_SETUP_COST, overrides.setupCostWeight());
        putIfPresent(weights, FacilityLocationConstraintProvider.DISTANCE_FROM_FACILITY,
                overrides.distanceFromFacilityWeight());
        if (!weights.isEmpty()) {
            problem.setConstraintWeightOverrides(ConstraintWeightOverrides.of(weights));
        }
    }

    private static void putIfPresent(Map<String, HardMediumSoftScore> weights, String constraintName, Long weight) {
        if (weight != null) {
            weights.put(constraintName, HardMediumSoftScore.ofSoft(weight));
        }
    }

    private static void applyLastOutput(List<Consumer> consumers, Map<String, Facility> facilityMap,
            Optional<FacilityLocationOutput> lastModelOutput) {
        if (lastModelOutput.isEmpty()) {
            return;
        }
        Map<String, String> assignmentMap = lastModelOutput.get().consumers().stream()
                .filter(c -> c.facilityId() != null)
                .collect(Collectors.toMap(ConsumerDTO::id, ConsumerDTO::facilityId));
        for (Consumer consumer : consumers) {
            String facilityId = assignmentMap.get(consumer.getId());
            if (facilityId != null) {
                consumer.setFacility(facilityMap.get(facilityId));
            }
        }
    }

    @Override
    public FacilityLocationOutput toModelOutput(FacilityLocationProblem solverModel) {
        List<FacilityDTO> facilities = solverModel.getFacilities().stream().map(this::toDTO)
                .collect(Collectors.toList());

        List<ConsumerDTO> consumers = solverModel.getConsumers().stream().map(this::toDTO).collect(Collectors.toList());

        String score = solverModel.getScore() == null ? "" : solverModel.getScore().toString();

        return new FacilityLocationOutput(facilities, consumers,
                score, solverModel.getTotalCost(),
                solverModel.getPotentialCost(), solverModel.getTotalDistance(),
                solverModel.getBounds().stream().map(this::toDTO).collect(Collectors.toList()));
    }

    private Location fromDTO(LocationDTO dto) {
        return new Location(dto.latitude(), dto.longitude());
    }

    private LocationDTO toDTO(Location location) {
        return new LocationDTO(location.getLatitude(), location.getLongitude());
    }

    private FacilityDTO toDTO(Facility facility) {
        return new FacilityDTO(facility.getId(), facility.getName(), toDTO(facility.getLocation()),
                facility.getSetupCost(), facility.getCapacity(), facility.getUsedCapacity(), facility.isUsed());
    }

    private ConsumerDTO toDTO(Consumer consumer) {
        String facilityId = consumer.getFacility() == null ? null : consumer.getFacility().getId();
        return new ConsumerDTO(consumer.getId(), toDTO(consumer.getLocation()), consumer.getDemand(),
                facilityId, consumer.isAssigned());
    }
}
