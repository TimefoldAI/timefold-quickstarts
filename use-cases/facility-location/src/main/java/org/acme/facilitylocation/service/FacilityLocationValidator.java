package org.acme.facilitylocation.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.ModelValidator;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;

import org.acme.facilitylocation.dto.ConsumerDTO;
import org.acme.facilitylocation.dto.ConsumerIdDetail;
import org.acme.facilitylocation.dto.FacilityDTO;
import org.acme.facilitylocation.dto.FacilityIdDetail;
import org.acme.facilitylocation.dto.FacilityLocationConfigOverrides;
import org.acme.facilitylocation.dto.FacilityLocationInput;
import org.acme.facilitylocation.service.FacilityLocationIssues.ConsumerIdMissingIssue;
import org.acme.facilitylocation.service.FacilityLocationIssues.DuplicateConsumerIdIssue;
import org.acme.facilitylocation.service.FacilityLocationIssues.DuplicateFacilityIdIssue;
import org.acme.facilitylocation.service.FacilityLocationIssues.FacilityIdMissingIssue;
import org.acme.facilitylocation.service.FacilityLocationIssues.NegativeConsumerDemandIssue;
import org.acme.facilitylocation.service.FacilityLocationIssues.NegativeFacilityCapacityIssue;
import org.acme.facilitylocation.service.FacilityLocationIssues.NegativeFacilitySetupCostIssue;
import org.acme.facilitylocation.service.FacilityLocationIssues.NonExistingFacilityReferenceIssue;

@ApplicationScoped
public class FacilityLocationValidator
        implements
        ModelValidator<FacilityLocationInput, FacilityLocationConfigOverrides> {

    @Override
    public void validate(ValidationBuilder validationBuilder, FacilityLocationInput modelInput,
            ModelConfig<FacilityLocationConfigOverrides> modelConfig) {
        Set<String> facilityIds = validateFacilities(validationBuilder, modelInput.facilities());
        validateConsumers(validationBuilder, modelInput.consumers(), facilityIds);
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private Set<String> validateFacilities(ValidationBuilder validationBuilder, List<FacilityDTO> facilities) {
        Set<String> facilityIds = new HashSet<>();
        for (FacilityDTO facility : facilities) {
            if (facility.id() == null || facility.id().isBlank()) {
                validationBuilder.addIssue(new FacilityIdMissingIssue());
            } else if (!facilityIds.add(facility.id())) {
                validationBuilder.addIssue(new DuplicateFacilityIdIssue(new FacilityIdDetail(facility.id())));
            }
            if (facility.capacity() < 0) {
                validationBuilder.addIssue(new NegativeFacilityCapacityIssue(new FacilityIdDetail(facility.id())));
            }
            if (facility.setupCost() < 0) {
                validationBuilder.addIssue(new NegativeFacilitySetupCostIssue(new FacilityIdDetail(facility.id())));
            }
        }
        return facilityIds;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private void validateConsumers(ValidationBuilder validationBuilder, List<ConsumerDTO> consumers,
            Set<String> facilityIds) {
        Set<String> consumerIds = new HashSet<>();
        for (ConsumerDTO consumer : consumers) {
            if (consumer.id() == null || consumer.id().isBlank()) {
                validationBuilder.addIssue(new ConsumerIdMissingIssue());
            } else if (!consumerIds.add(consumer.id())) {
                validationBuilder.addIssue(new DuplicateConsumerIdIssue(new ConsumerIdDetail(consumer.id())));
            }
            if (consumer.demand() < 0) {
                validationBuilder.addIssue(new NegativeConsumerDemandIssue(new ConsumerIdDetail(consumer.id())));
            }
            if (consumer.facilityId() != null && !facilityIds.contains(consumer.facilityId())) {
                validationBuilder.addIssue(new NonExistingFacilityReferenceIssue(new ConsumerIdDetail(consumer.id())));
            }
        }
    }
}
