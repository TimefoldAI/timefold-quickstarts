package org.acme.facilitylocation.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.service.definition.api.ModelConvertor;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.maps.api.model.Location;

import org.acme.facilitylocation.domain.Consumer;
import org.acme.facilitylocation.domain.Facility;
import org.acme.facilitylocation.domain.FacilityPlan;
import org.acme.facilitylocation.domain.FacilityPlanConstraintProperties;
import org.acme.facilitylocation.dto.input.ConsumerInputDTO;
import org.acme.facilitylocation.dto.input.FacilityInputDTO;
import org.acme.facilitylocation.dto.input.FacilityPlanConfigOverrides;
import org.acme.facilitylocation.dto.input.FacilityPlanInput;
import org.acme.facilitylocation.dto.input.LocationDTO;
import org.acme.facilitylocation.dto.output.ConsumerOutputDTO;
import org.acme.facilitylocation.dto.output.FacilityOutputDTO;
import org.acme.facilitylocation.dto.output.FacilityPlanOutput;

@ApplicationScoped
public class FacilityPlanModelConvertor
        implements
        ModelConvertor<HardSoftScore, FacilityPlanInput, FacilityPlanConfigOverrides, FacilityPlan, FacilityPlanOutput> {

    @Override
    public FacilityPlan toSolverModel(FacilityPlanInput modelInput, ModelConfig<FacilityPlanConfigOverrides> modelConfig,
            Optional<FacilityPlanOutput> lastModelOutput) {
        Map<String, Facility> facilityMap = new LinkedHashMap<>();
        for (FacilityInputDTO dto : modelInput.facilities()) {
            facilityMap.put(dto.id(), new Facility(dto.id(), toLocation(dto.location()), dto.setupCost(), dto.capacity()));
        }
        List<Consumer> consumers = modelInput.consumers().stream()
                .map(dto -> toConsumer(dto, facilityMap))
                .toList();

        FacilityPlan facilityPlan = new FacilityPlan(new ArrayList<>(facilityMap.values()), consumers);
        applyConstraintWeightOverrides(facilityPlan, modelConfig);
        applyLastOutput(consumers, facilityMap, lastModelOutput);
        return facilityPlan;
    }

    @Override
    public FacilityPlanOutput toModelOutput(FacilityPlan solverModel) {
        List<ConsumerOutputDTO> consumers = solverModel.getConsumers().stream()
                .map(consumer -> consumer.getFacility() == null
                        ? new ConsumerOutputDTO(consumer.getId(), null, null)
                        : new ConsumerOutputDTO(consumer.getId(), consumer.getFacility().getId(),
                                consumer.distanceFromFacility()))
                .toList();
        // Facility.isUsed()/getUsedCapacity() read the inverse relation shadow variable, which the Service module
        // always brings up to date (via SolutionManager.update) before it asks for the model output.
        List<FacilityOutputDTO> facilities = solverModel.getFacilities().stream()
                .map(facility -> new FacilityOutputDTO(facility.getId(), facility.isUsed(), facility.getUsedCapacity(),
                        facility.getConsumers().size()))
                .toList();
        return new FacilityPlanOutput(consumers, facilities);
    }

    @Override
    public FacilityPlanInput applyOutputToInput(FacilityPlanInput modelInput, FacilityPlanOutput modelOutput) {
        Map<String, ConsumerOutputDTO> outputConsumers = modelOutput.consumers().stream()
                .collect(Collectors.toMap(ConsumerOutputDTO::id, consumer -> consumer));
        List<ConsumerInputDTO> updatedConsumers = modelInput.consumers().stream()
                .map(consumer -> {
                    ConsumerOutputDTO solved = outputConsumers.get(consumer.id());
                    return solved == null ? consumer : consumer.withFacilityId(solved.facilityId());
                })
                .toList();
        return modelInput.withConsumers(updatedConsumers);
    }

    private static Consumer toConsumer(ConsumerInputDTO dto, Map<String, Facility> facilityMap) {
        Consumer consumer = new Consumer(dto.id(), toLocation(dto.location()), dto.demand());
        if (dto.facilityId() != null) {
            consumer.setFacility(require(facilityMap, dto.facilityId(), "facility"));
        }
        return consumer;
    }

    private static Location toLocation(LocationDTO dto) {
        return new Location(dto.latitude(), dto.longitude());
    }

    private static <T> T require(Map<String, T> map, String id, String kind) {
        T value = map.get(id);
        if (value == null) {
            throw new IllegalArgumentException("The %s (%s) does not exist. Known %s ids are %s."
                    .formatted(kind, id, kind, map.keySet()));
        }
        return value;
    }

    private static void applyConstraintWeightOverrides(FacilityPlan facilityPlan,
            ModelConfig<FacilityPlanConfigOverrides> modelConfig) {
        if (modelConfig == null || modelConfig.overrides() == null) {
            return;
        }
        FacilityPlanConfigOverrides overrides = modelConfig.overrides();
        // Only apply weights that are actually set (non-null) in the merged overrides. A null weight means the
        // input did not override it, so the configuration profile value (or the constraint's default) is kept.
        Map<String, HardSoftScore> weights = new HashMap<>();
        putIfPresent(weights, FacilityPlanConstraintProperties.FACILITY_SETUP_COST, overrides.facilitySetupCostWeight());
        putIfPresent(weights, FacilityPlanConstraintProperties.DISTANCE_FROM_FACILITY,
                overrides.distanceFromFacilityWeight());
        if (!weights.isEmpty()) {
            facilityPlan.setConstraintWeightOverrides(ConstraintWeightOverrides.of(weights));
        }
    }

    private static void putIfPresent(Map<String, HardSoftScore> weights, String constraintName, Long weight) {
        if (weight != null) {
            weights.put(constraintName, HardSoftScore.ofSoft(weight));
        }
    }

    // lastModelOutput is used to recover a run that stopped halfway, so it overrides the input assignment.
    private static void applyLastOutput(List<Consumer> consumers, Map<String, Facility> facilityMap,
            Optional<FacilityPlanOutput> lastModelOutput) {
        if (lastModelOutput.isEmpty()) {
            return;
        }
        Map<String, Consumer> consumerMap = consumers.stream()
                .collect(Collectors.toMap(Consumer::getId, consumer -> consumer));
        for (ConsumerOutputDTO solved : lastModelOutput.get().consumers()) {
            Consumer consumer = consumerMap.get(solved.id());
            if (consumer == null || solved.facilityId() == null) {
                continue;
            }
            Facility facility = facilityMap.get(solved.facilityId());
            if (facility != null) {
                consumer.setFacility(facility);
            }
        }
    }
}
