package org.acme.vehiclerouting.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.ModelValidator;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;

import org.acme.vehiclerouting.dto.VehicleDTO;
import org.acme.vehiclerouting.dto.VehicleIdDetail;
import org.acme.vehiclerouting.dto.VehicleRoutingConfigOverrides;
import org.acme.vehiclerouting.dto.VehicleRoutingInput;
import org.acme.vehiclerouting.dto.VisitDTO;
import org.acme.vehiclerouting.dto.VisitIdDetail;
import org.acme.vehiclerouting.service.VehicleRoutingIssues.DuplicateVehicleIdIssue;
import org.acme.vehiclerouting.service.VehicleRoutingIssues.DuplicateVisitAssignmentIssue;
import org.acme.vehiclerouting.service.VehicleRoutingIssues.DuplicateVisitIdIssue;
import org.acme.vehiclerouting.service.VehicleRoutingIssues.NonExistingVisitReferenceIssue;
import org.acme.vehiclerouting.service.VehicleRoutingIssues.VehicleIdMissingIssue;
import org.acme.vehiclerouting.service.VehicleRoutingIssues.VisitIdMissingIssue;

@ApplicationScoped
public class VehicleRoutingValidator implements ModelValidator<VehicleRoutingInput, VehicleRoutingConfigOverrides> {

    @Override
    public void validate(ValidationBuilder validationBuilder, VehicleRoutingInput modelInput,
            ModelConfig<VehicleRoutingConfigOverrides> modelConfig) {
        Set<String> visitIds = validateVisits(validationBuilder, modelInput.visits());
        validateVehicles(validationBuilder, modelInput.vehicles(), visitIds);
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private Set<String> validateVisits(ValidationBuilder validationBuilder, List<VisitDTO> visits) {
        Set<String> visitIds = new HashSet<>();
        for (VisitDTO visit : visits) {
            if (visit.id() == null || visit.id().isBlank()) {
                validationBuilder.addIssue(new VisitIdMissingIssue());
            } else if (!visitIds.add(visit.id())) {
                validationBuilder.addIssue(new DuplicateVisitIdIssue(new VisitIdDetail(visit.id())));
            }
        }
        return visitIds;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private void validateVehicles(ValidationBuilder validationBuilder, List<VehicleDTO> vehicles, Set<String> visitIds) {
        Set<String> vehicleIds = new HashSet<>();
        Set<String> assignedVisitIds = new HashSet<>();
        for (VehicleDTO vehicle : vehicles) {
            if (vehicle.id() == null || vehicle.id().isBlank()) {
                validationBuilder.addIssue(new VehicleIdMissingIssue());
            } else if (!vehicleIds.add(vehicle.id())) {
                validationBuilder.addIssue(new DuplicateVehicleIdIssue(new VehicleIdDetail(vehicle.id())));
            }
            for (String visitId : vehicle.visitIds()) {
                if (visitIds.contains(visitId)) {
                    if (!assignedVisitIds.add(visitId)) {
                        validationBuilder.addIssue(new DuplicateVisitAssignmentIssue(new VisitIdDetail(visitId)));
                    }
                } else {
                    validationBuilder.addIssue(new NonExistingVisitReferenceIssue(new VisitIdDetail(visitId)));
                }
            }
        }
    }
}
