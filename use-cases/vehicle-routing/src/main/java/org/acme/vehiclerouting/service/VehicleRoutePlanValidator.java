package org.acme.vehiclerouting.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.ModelValidator;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;

import org.acme.vehiclerouting.dto.input.VehicleInputDTO;
import org.acme.vehiclerouting.dto.input.VehicleRoutePlanConfigOverrides;
import org.acme.vehiclerouting.dto.input.VehicleRoutePlanInput;
import org.acme.vehiclerouting.dto.input.VisitInputDTO;
import org.acme.vehiclerouting.service.validation.VehicleRoutePlanIssue.DuplicateVehicleIdIssue;
import org.acme.vehiclerouting.service.validation.VehicleRoutePlanIssue.DuplicateVisitIdIssue;
import org.acme.vehiclerouting.service.validation.VehicleRoutePlanIssue.NonExistingVisitReferenceIssue;
import org.acme.vehiclerouting.service.validation.VehicleRoutePlanIssue.VisitAssignedMoreThanOnceIssue;
import org.acme.vehiclerouting.service.validation.VehicleRoutePlanIssue.VisitWindowTooShortIssue;

@ApplicationScoped
public class VehicleRoutePlanValidator
        implements ModelValidator<VehicleRoutePlanInput, VehicleRoutePlanConfigOverrides> {

    @Override
    public void validate(ValidationBuilder validationBuilder, VehicleRoutePlanInput modelInput,
            ModelConfig<VehicleRoutePlanConfigOverrides> modelConfig) {
        // OpenAPI spec (Bean Validation) compliance is enforced by the Service module at the REST layer,
        // before this validator ever runs; only domain-specific checks belong here.
        Set<String> visitIds = validateVisits(validationBuilder, orEmpty(modelInput.visits()));
        validateVehicles(validationBuilder, orEmpty(modelInput.vehicles()), visitIds);
    }

    private Set<String> validateVisits(ValidationBuilder validationBuilder, List<VisitInputDTO> visits) {
        Set<String> visitIds = new HashSet<>();
        for (var visit : visits) {
            // At most one issue per visit, so a single misconfigured visit cannot flood the report.
            if (hasId(visit.id()) && !visitIds.add(visit.id())) {
                validationBuilder.addIssue(new DuplicateVisitIdIssue(visit.id()));
            } else if (isWindowTooShort(visit)) {
                validationBuilder.addIssue(new VisitWindowTooShortIssue(visit.id()));
            }
        }
        return visitIds;
    }

    /**
     * The route lists are the assignment, so this is where they have to be checked: an id that no
     * visit carries, and an id that shows up in two routes (or twice in one) - the solver's list
     * variable can hold each visit at most once.
     */
    private void validateVehicles(ValidationBuilder validationBuilder, List<VehicleInputDTO> vehicles,
            Set<String> visitIds) {
        Set<String> vehicleIds = new HashSet<>();
        Set<String> assignedVisitIds = new HashSet<>();
        for (var vehicle : vehicles) {
            if (hasId(vehicle.id()) && !vehicleIds.add(vehicle.id())) {
                validationBuilder.addIssue(new DuplicateVehicleIdIssue(vehicle.id()));
                continue;
            }
            String unknownVisitId = null;
            for (String visitId : vehicle.visitIds()) {
                if (!visitIds.contains(visitId)) {
                    // Only the first unknown id per vehicle: a route built against the wrong dataset
                    // would otherwise report one issue per stop.
                    unknownVisitId = unknownVisitId != null ? unknownVisitId : visitId;
                } else if (!assignedVisitIds.add(visitId)) {
                    validationBuilder.addIssue(new VisitAssignedMoreThanOnceIssue(visitId));
                }
            }
            if (unknownVisitId != null) {
                validationBuilder.addIssue(new NonExistingVisitReferenceIssue(vehicle.id(), unknownVisitId));
            }
        }
    }

    /**
     * A visit whose servicing cannot fit between its earliest start time and its maximum end time
     * can never be assigned without breaking a hard constraint.
     */
    private static boolean isWindowTooShort(VisitInputDTO visit) {
        if (visit.minStartTime() == null || visit.maxEndTime() == null || visit.serviceDurationMinutes() == null) {
            return false;
        }
        return visit.minStartTime().plusMinutes(visit.serviceDurationMinutes()).isAfter(visit.maxEndTime());
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }

    private static boolean hasId(String id) {
        return id != null && !id.isBlank();
    }
}
