package org.acme.taskassigning.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.ModelValidator;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;

import org.acme.taskassigning.dto.input.CustomerInputDTO;
import org.acme.taskassigning.dto.input.EmployeeInputDTO;
import org.acme.taskassigning.dto.input.TaskAssigningConfigOverrides;
import org.acme.taskassigning.dto.input.TaskAssigningInput;
import org.acme.taskassigning.dto.input.TaskInputDTO;
import org.acme.taskassigning.dto.input.TaskTypeInputDTO;
import org.acme.taskassigning.service.validation.TaskAssigningIssue;
import org.acme.taskassigning.service.validation.TaskAssigningIssue.DuplicateCustomerIdIssue;
import org.acme.taskassigning.service.validation.TaskAssigningIssue.DuplicateEmployeeIdIssue;
import org.acme.taskassigning.service.validation.TaskAssigningIssue.DuplicateTaskIdIssue;
import org.acme.taskassigning.service.validation.TaskAssigningIssue.DuplicateTaskTypeCodeIssue;
import org.acme.taskassigning.service.validation.TaskAssigningIssue.NonExistingCustomerAffinityReferenceIssue;
import org.acme.taskassigning.service.validation.TaskAssigningIssue.NonExistingCustomerReferenceIssue;
import org.acme.taskassigning.service.validation.TaskAssigningIssue.NonExistingTaskReferenceInAssignmentIssue;
import org.acme.taskassigning.service.validation.TaskAssigningIssue.NonExistingTaskTypeReferenceIssue;
import org.acme.taskassigning.service.validation.TaskAssigningIssue.TaskAssignedToMultipleEmployeesIssue;

@ApplicationScoped
public class TaskAssigningValidator implements ModelValidator<TaskAssigningInput, TaskAssigningConfigOverrides> {

    @Override
    public void validate(ValidationBuilder validationBuilder, TaskAssigningInput modelInput,
            ModelConfig<TaskAssigningConfigOverrides> modelConfig) {
        // OpenAPI spec (Bean Validation) compliance is enforced by the Service module at the REST layer,
        // before this validator ever runs; only domain-specific checks belong here.
        Set<String> customerIds = collectIds(validationBuilder, orEmpty(modelInput.customers()), CustomerInputDTO::id,
                DuplicateCustomerIdIssue::new);
        Set<String> taskTypeCodes = collectIds(validationBuilder, orEmpty(modelInput.taskTypes()),
                TaskTypeInputDTO::code, DuplicateTaskTypeCodeIssue::new);
        Set<String> taskIds = collectIds(validationBuilder, orEmpty(modelInput.tasks()), TaskInputDTO::id,
                DuplicateTaskIdIssue::new);
        collectIds(validationBuilder, orEmpty(modelInput.employees()), EmployeeInputDTO::id,
                DuplicateEmployeeIdIssue::new);

        validateTasks(validationBuilder, orEmpty(modelInput.tasks()), taskTypeCodes, customerIds);
        validateEmployees(validationBuilder, orEmpty(modelInput.employees()), customerIds, taskIds);
    }

    /**
     * Collects the ids of {@code elements}, reporting one duplicate issue per repeated id.
     *
     * @return the distinct, non-blank ids, so the reference checks below can resolve against them
     */
    private static <T> Set<String> collectIds(ValidationBuilder validationBuilder, List<T> elements,
            Function<T, String> idExtractor, Function<String, TaskAssigningIssue> duplicateIssueFactory) {
        Set<String> ids = new HashSet<>();
        for (T element : elements) {
            String id = idExtractor.apply(element);
            if (hasId(id) && !ids.add(id)) {
                validationBuilder.addIssue(duplicateIssueFactory.apply(id));
            }
        }
        return ids;
    }

    private static void validateTasks(ValidationBuilder validationBuilder, List<TaskInputDTO> tasks,
            Set<String> taskTypeCodes, Set<String> customerIds) {
        for (TaskInputDTO task : tasks) {
            if (hasId(task.taskTypeCode()) && !taskTypeCodes.contains(task.taskTypeCode())) {
                validationBuilder.addIssue(new NonExistingTaskTypeReferenceIssue(task.id()));
            }
            if (hasId(task.customerId()) && !customerIds.contains(task.customerId())) {
                validationBuilder.addIssue(new NonExistingCustomerReferenceIssue(task.id()));
            }
        }
    }

    private static void validateEmployees(ValidationBuilder validationBuilder, List<EmployeeInputDTO> employees,
            Set<String> customerIds, Set<String> taskIds) {
        Set<String> assignedTaskIds = new HashSet<>();
        for (EmployeeInputDTO employee : employees) {
            if (!customerIds.containsAll(employee.customerAffinities().keySet())) {
                validationBuilder.addIssue(new NonExistingCustomerAffinityReferenceIssue(employee.id()));
            }
            // Only one issue per employee, rather than one per unknown task id, keeps the issue list
            // bounded for a dataset that refers to a whole batch of tasks that were never submitted.
            if (!taskIds.containsAll(employee.taskIds())) {
                validationBuilder.addIssue(new NonExistingTaskReferenceInAssignmentIssue(employee.id()));
            }
            for (String taskId : employee.taskIds()) {
                if (hasId(taskId) && !assignedTaskIds.add(taskId)) {
                    validationBuilder.addIssue(new TaskAssignedToMultipleEmployeesIssue(taskId));
                }
            }
        }
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }

    private static boolean hasId(String id) {
        return id != null && !id.isBlank();
    }
}
