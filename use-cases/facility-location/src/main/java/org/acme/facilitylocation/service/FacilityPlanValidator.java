package org.acme.facilitylocation.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.ModelValidator;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;

import org.acme.facilitylocation.dto.input.ConsumerInputDTO;
import org.acme.facilitylocation.dto.input.FacilityInputDTO;
import org.acme.facilitylocation.dto.input.FacilityPlanConfigOverrides;
import org.acme.facilitylocation.dto.input.FacilityPlanInput;
import org.acme.facilitylocation.service.validation.FacilityPlanIssue.DuplicateConsumerIdIssue;
import org.acme.facilitylocation.service.validation.FacilityPlanIssue.DuplicateFacilityIdIssue;
import org.acme.facilitylocation.service.validation.FacilityPlanIssue.InsufficientTotalCapacityIssue;
import org.acme.facilitylocation.service.validation.FacilityPlanIssue.NonExistingFacilityReferenceIssue;

@ApplicationScoped
public class FacilityPlanValidator implements ModelValidator<FacilityPlanInput, FacilityPlanConfigOverrides> {

    @Override
    public void validate(ValidationBuilder validationBuilder, FacilityPlanInput modelInput,
            ModelConfig<FacilityPlanConfigOverrides> modelConfig) {
        // OpenAPI spec compliance is enforced by the Service module at the REST layer, before this validator ever
        // runs; only domain-specific checks belong here.
        List<FacilityInputDTO> facilities = orEmpty(modelInput.facilities());
        List<ConsumerInputDTO> consumers = orEmpty(modelInput.consumers());
        Set<String> facilityIds = validateFacilities(validationBuilder, facilities);
        validateConsumers(validationBuilder, consumers, facilityIds);
        validateTotalCapacity(validationBuilder, facilities, consumers);
    }

    private Set<String> validateFacilities(ValidationBuilder validationBuilder, List<FacilityInputDTO> facilities) {
        Set<String> facilityIds = new HashSet<>();
        for (FacilityInputDTO facility : facilities) {
            if (hasId(facility.id()) && !facilityIds.add(facility.id())) {
                validationBuilder.addIssue(new DuplicateFacilityIdIssue(facility.id()));
            }
        }
        return facilityIds;
    }

    private void validateConsumers(ValidationBuilder validationBuilder, List<ConsumerInputDTO> consumers,
            Set<String> facilityIds) {
        Set<String> consumerIds = new HashSet<>();
        for (ConsumerInputDTO consumer : consumers) {
            if (hasId(consumer.id()) && !consumerIds.add(consumer.id())) {
                validationBuilder.addIssue(new DuplicateConsumerIdIssue(consumer.id()));
            }
            if (consumer.facilityId() != null && !facilityIds.contains(consumer.facilityId())) {
                validationBuilder.addIssue(new NonExistingFacilityReferenceIssue(consumer.id()));
            }
        }
    }

    private void validateTotalCapacity(ValidationBuilder validationBuilder, List<FacilityInputDTO> facilities,
            List<ConsumerInputDTO> consumers) {
        long totalCapacity = facilities.stream()
                .map(FacilityInputDTO::capacity)
                .filter(capacity -> capacity != null)
                .mapToLong(Long::longValue)
                .sum();
        long totalDemand = consumers.stream()
                .map(ConsumerInputDTO::demand)
                .filter(demand -> demand != null)
                .mapToLong(Long::longValue)
                .sum();
        // Every consumer has to be served, so an under-capacitated dataset can never become feasible.
        if (totalCapacity < totalDemand) {
            validationBuilder.addIssue(new InsufficientTotalCapacityIssue(totalCapacity, totalDemand));
        }
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }

    private static boolean hasId(String id) {
        return id != null && !id.isBlank();
    }
}
