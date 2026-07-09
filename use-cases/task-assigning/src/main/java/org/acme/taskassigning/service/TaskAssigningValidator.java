package org.acme.taskassigning.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.ModelValidator;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;

import org.acme.taskassigning.dto.EmployeeDTO;
import org.acme.taskassigning.dto.EmployeeIdDetail;
import org.acme.taskassigning.dto.TaskAssigningConfigOverrides;
import org.acme.taskassigning.dto.TaskAssigningInput;
import org.acme.taskassigning.dto.TaskDTO;
import org.acme.taskassigning.dto.TaskIdDetail;
import org.acme.taskassigning.service.TaskAssigningIssues.DuplicateEmployeeIdIssue;
import org.acme.taskassigning.service.TaskAssigningIssues.DuplicateTaskAssignmentIssue;
import org.acme.taskassigning.service.TaskAssigningIssues.DuplicateTaskIdIssue;
import org.acme.taskassigning.service.TaskAssigningIssues.EmployeeIdMissingIssue;
import org.acme.taskassigning.service.TaskAssigningIssues.NonExistingTaskReferenceIssue;
import org.acme.taskassigning.service.TaskAssigningIssues.TaskIdMissingIssue;

@ApplicationScoped
public class TaskAssigningValidator implements ModelValidator<TaskAssigningInput, TaskAssigningConfigOverrides> {

    @Override
    public void validate(ValidationBuilder validationBuilder, TaskAssigningInput modelInput,
            ModelConfig<TaskAssigningConfigOverrides> modelConfig) {
        Set<String> taskIds = validateTasks(validationBuilder, modelInput.tasks());
        validateEmployees(validationBuilder, modelInput.employees(), taskIds);
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private Set<String> validateTasks(ValidationBuilder validationBuilder, List<TaskDTO> tasks) {
        Set<String> taskIds = new HashSet<>();
        for (TaskDTO task : tasks) {
            if (task.id() == null || task.id().isBlank()) {
                validationBuilder.addIssue(new TaskIdMissingIssue());
            } else if (!taskIds.add(task.id())) {
                validationBuilder.addIssue(new DuplicateTaskIdIssue(new TaskIdDetail(task.id())));
            }
        }
        return taskIds;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private void validateEmployees(ValidationBuilder validationBuilder, List<EmployeeDTO> employees,
            Set<String> taskIds) {
        Set<String> employeeIds = new HashSet<>();
        Set<String> assignedTaskIds = new HashSet<>();
        for (EmployeeDTO employee : employees) {
            if (employee.id() == null || employee.id().isBlank()) {
                validationBuilder.addIssue(new EmployeeIdMissingIssue());
            } else if (!employeeIds.add(employee.id())) {
                validationBuilder.addIssue(new DuplicateEmployeeIdIssue(new EmployeeIdDetail(employee.id())));
            }
            for (String taskId : employee.taskIds()) {
                if (taskIds.contains(taskId)) {
                    if (!assignedTaskIds.add(taskId)) {
                        validationBuilder.addIssue(new DuplicateTaskAssignmentIssue(new TaskIdDetail(taskId)));
                    }
                } else {
                    validationBuilder.addIssue(new NonExistingTaskReferenceIssue(new TaskIdDetail(taskId)));
                }
            }
        }
    }
}
