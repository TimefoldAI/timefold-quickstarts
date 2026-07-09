package org.acme.orderpicking.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.ModelValidator;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;

import org.acme.orderpicking.dto.OrderPickingConfigOverrides;
import org.acme.orderpicking.dto.OrderPickingInput;
import org.acme.orderpicking.dto.PickTaskDTO;
import org.acme.orderpicking.dto.PickTaskIdDetail;
import org.acme.orderpicking.dto.TrolleyDTO;
import org.acme.orderpicking.dto.TrolleyIdDetail;
import org.acme.orderpicking.service.OrderPickingIssues.DuplicatePickTaskAssignmentIssue;
import org.acme.orderpicking.service.OrderPickingIssues.DuplicatePickTaskIdIssue;
import org.acme.orderpicking.service.OrderPickingIssues.DuplicateTrolleyIdIssue;
import org.acme.orderpicking.service.OrderPickingIssues.NonExistingPickTaskReferenceIssue;
import org.acme.orderpicking.service.OrderPickingIssues.PickTaskIdMissingIssue;
import org.acme.orderpicking.service.OrderPickingIssues.TrolleyIdMissingIssue;

@ApplicationScoped
public class OrderPickingValidator implements ModelValidator<OrderPickingInput, OrderPickingConfigOverrides> {

    @Override
    public void validate(ValidationBuilder validationBuilder, OrderPickingInput modelInput,
            ModelConfig<OrderPickingConfigOverrides> modelConfig) {
        Set<String> pickTaskIds = validatePickTasks(validationBuilder, modelInput.pickTasks());
        validateTrolleys(validationBuilder, modelInput.trolleys(), pickTaskIds);
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private Set<String> validatePickTasks(ValidationBuilder validationBuilder, List<PickTaskDTO> pickTasks) {
        Set<String> pickTaskIds = new HashSet<>();
        for (PickTaskDTO pickTask : pickTasks) {
            if (pickTask.id() == null || pickTask.id().isBlank()) {
                validationBuilder.addIssue(new PickTaskIdMissingIssue());
            } else if (!pickTaskIds.add(pickTask.id())) {
                validationBuilder.addIssue(new DuplicatePickTaskIdIssue(new PickTaskIdDetail(pickTask.id())));
            }
        }
        return pickTaskIds;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private void validateTrolleys(ValidationBuilder validationBuilder, List<TrolleyDTO> trolleys,
            Set<String> pickTaskIds) {
        Set<String> trolleyIds = new HashSet<>();
        Set<String> assignedPickTaskIds = new HashSet<>();
        for (TrolleyDTO trolley : trolleys) {
            if (trolley.id() == null || trolley.id().isBlank()) {
                validationBuilder.addIssue(new TrolleyIdMissingIssue());
            } else if (!trolleyIds.add(trolley.id())) {
                validationBuilder.addIssue(new DuplicateTrolleyIdIssue(new TrolleyIdDetail(trolley.id())));
            }
            for (String pickTaskId : trolley.pickTaskIds()) {
                if (pickTaskIds.contains(pickTaskId)) {
                    if (!assignedPickTaskIds.add(pickTaskId)) {
                        validationBuilder.addIssue(new DuplicatePickTaskAssignmentIssue(new PickTaskIdDetail(pickTaskId)));
                    }
                } else {
                    validationBuilder.addIssue(new NonExistingPickTaskReferenceIssue(new PickTaskIdDetail(pickTaskId)));
                }
            }
        }
    }
}
