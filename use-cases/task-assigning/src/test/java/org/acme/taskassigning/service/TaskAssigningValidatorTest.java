package org.acme.taskassigning.service;

import static org.acme.taskassigning.support.TestHelper.aCustomerDTO;
import static org.acme.taskassigning.support.TestHelper.aTaskDTO;
import static org.acme.taskassigning.support.TestHelper.aTaskTypeDTO;
import static org.acme.taskassigning.support.TestHelper.anEmployeeDTO;
import static org.acme.taskassigning.support.TestHelper.createProblem;
import static org.acme.taskassigning.support.TestHelper.input;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.Issue;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;
import ai.timefold.solver.service.definition.api.validation.dto.ValidationResult;

import org.acme.taskassigning.demo.DemoDataBuilder;
import org.acme.taskassigning.domain.Affinity;
import org.acme.taskassigning.dto.input.CustomerInputDTO;
import org.acme.taskassigning.dto.input.EmployeeInputDTO;
import org.acme.taskassigning.dto.input.TaskAssigningInput;
import org.acme.taskassigning.dto.input.TaskInputDTO;
import org.acme.taskassigning.dto.input.TaskTypeInputDTO;
import org.acme.taskassigning.service.validation.TaskAssigningIssue.DuplicateCustomerIdIssue;
import org.acme.taskassigning.service.validation.TaskAssigningIssue.DuplicateEmployeeIdIssue;
import org.acme.taskassigning.service.validation.TaskAssigningIssue.DuplicateTaskIdIssue;
import org.acme.taskassigning.service.validation.TaskAssigningIssue.DuplicateTaskTypeCodeIssue;
import org.acme.taskassigning.service.validation.TaskAssigningIssue.NonExistingCustomerAffinityReferenceIssue;
import org.acme.taskassigning.service.validation.TaskAssigningIssue.NonExistingCustomerReferenceIssue;
import org.acme.taskassigning.service.validation.TaskAssigningIssue.NonExistingTaskReferenceInAssignmentIssue;
import org.acme.taskassigning.service.validation.TaskAssigningIssue.NonExistingTaskTypeReferenceIssue;
import org.acme.taskassigning.service.validation.TaskAssigningIssue.TaskAssignedToMultipleEmployeesIssue;
import org.junit.jupiter.api.Test;

// OpenAPI spec compliance (Bean Validation) is enforced by the Service module at the REST layer, so it's
// covered by org.acme.taskassigning.rest.TaskAssigningOpenApiValidationTest instead. This class only covers
// the domain-specific checks TaskAssigningValidator implements itself.
class TaskAssigningValidatorTest {

    private static final List<CustomerInputDTO> CUSTOMERS = List.of(aCustomerDTO("C1").build());
    private static final List<TaskTypeInputDTO> TASK_TYPES = List.of(aTaskTypeDTO("T1").build());
    private static final List<TaskInputDTO> VALID_TASKS = List.of(aTaskDTO("1").taskTypeCode("T1").customerId("C1").build());

    private final TaskAssigningValidator validator = new TaskAssigningValidator();

    @Test
    void validInputHasNoIssues() {
        ValidationResult<Issue> result = validate(createProblem());
        assertThat(result.issues()).isEmpty();
    }

    @Test
    void demoDataHasNoIssues() {
        // The service must never ship demo data it would reject itself.
        ValidationResult<Issue> result = validate(DemoDataBuilder.basic());
        assertThat(result.issues()).isEmpty();
    }

    @Test
    void duplicateCustomerId() {
        CustomerInputDTO customer = aCustomerDTO("C1").build();
        TaskAssigningInput problem = input(List.of(customer, customer), TASK_TYPES, List.of(), VALID_TASKS);
        assertSingleIssue(validate(problem), DuplicateCustomerIdIssue.class);
    }

    @Test
    void duplicateTaskTypeCode() {
        TaskTypeInputDTO taskType = aTaskTypeDTO("T1").build();
        TaskAssigningInput problem = input(CUSTOMERS, List.of(taskType, taskType), List.of(), VALID_TASKS);
        assertSingleIssue(validate(problem), DuplicateTaskTypeCodeIssue.class);
    }

    @Test
    void duplicateTaskId() {
        TaskInputDTO task = aTaskDTO("1").taskTypeCode("T1").customerId("C1").build();
        TaskAssigningInput problem = input(CUSTOMERS, TASK_TYPES, List.of(), List.of(task, task));
        assertSingleIssue(validate(problem), DuplicateTaskIdIssue.class);
    }

    @Test
    void duplicateEmployeeId() {
        EmployeeInputDTO employee = anEmployeeDTO("E1").build();
        TaskAssigningInput problem = input(CUSTOMERS, TASK_TYPES, List.of(employee, employee), VALID_TASKS);
        assertSingleIssue(validate(problem), DuplicateEmployeeIdIssue.class);
    }

    @Test
    void nonExistingTaskTypeReference() {
        TaskInputDTO task = aTaskDTO("1").taskTypeCode("does-not-exist").customerId("C1").build();
        TaskAssigningInput problem = input(CUSTOMERS, TASK_TYPES, List.of(), List.of(task));
        assertSingleIssue(validate(problem), NonExistingTaskTypeReferenceIssue.class);
    }

    @Test
    void nonExistingCustomerReference() {
        TaskInputDTO task = aTaskDTO("1").taskTypeCode("T1").customerId("does-not-exist").build();
        TaskAssigningInput problem = input(CUSTOMERS, TASK_TYPES, List.of(), List.of(task));
        assertSingleIssue(validate(problem), NonExistingCustomerReferenceIssue.class);
    }

    @Test
    void nonExistingCustomerAffinityReference() {
        EmployeeInputDTO employee =
                anEmployeeDTO("E1").customerAffinities(Map.of("does-not-exist", Affinity.HIGH)).build();
        TaskAssigningInput problem = input(CUSTOMERS, TASK_TYPES, List.of(employee), VALID_TASKS);
        assertSingleIssue(validate(problem), NonExistingCustomerAffinityReferenceIssue.class);
    }

    @Test
    void nonExistingTaskReferenceInAssignment() {
        EmployeeInputDTO employee = anEmployeeDTO("E1").taskIds(List.of("does-not-exist")).build();
        TaskAssigningInput problem = input(CUSTOMERS, TASK_TYPES, List.of(employee), VALID_TASKS);
        assertSingleIssue(validate(problem), NonExistingTaskReferenceInAssignmentIssue.class);
    }

    @Test
    void taskAssignedToMultipleEmployees() {
        List<TaskInputDTO> tasks = List.of(aTaskDTO("1").taskTypeCode("T1").customerId("C1").build(),
                aTaskDTO("2").taskTypeCode("T1").customerId("C1").build());
        List<EmployeeInputDTO> employees = List.of(
                anEmployeeDTO("E1").taskIds(List.of("1", "2")).build(),
                anEmployeeDTO("E2").taskIds(List.of("1")).build());
        TaskAssigningInput problem = input(CUSTOMERS, TASK_TYPES, employees, tasks);
        assertSingleIssue(validate(problem), TaskAssignedToMultipleEmployeesIssue.class);
    }

    @Test
    void mixedDatasetReportsEveryIssue() {
        CustomerInputDTO customer = aCustomerDTO("C1").build();
        TaskTypeInputDTO taskType = aTaskTypeDTO("T1").build();
        TaskAssigningInput problem = input(List.of(customer, customer), List.of(taskType, taskType), List.of(),
                List.of(aTaskDTO("1").taskTypeCode("does-not-exist").customerId("C1").build(),
                        aTaskDTO("2").taskTypeCode("T1").customerId("does-not-exist").build()));

        ValidationResult<Issue> result = validate(problem);
        assertThat(result.issues()).hasSize(4);
        assertThat(result.issues()).hasAtLeastOneElementOfType(DuplicateCustomerIdIssue.class);
        assertThat(result.issues()).hasAtLeastOneElementOfType(DuplicateTaskTypeCodeIssue.class);
        assertThat(result.issues()).hasAtLeastOneElementOfType(NonExistingTaskTypeReferenceIssue.class);
        assertThat(result.issues()).hasAtLeastOneElementOfType(NonExistingCustomerReferenceIssue.class);
    }

    private ValidationResult<Issue> validate(TaskAssigningInput input) {
        ValidationBuilder validationBuilder = new ValidationBuilder();
        validator.validate(validationBuilder, input, ModelConfig.empty());
        return validationBuilder.build();
    }

    private static <T extends Issue> void assertSingleIssue(ValidationResult<Issue> result, Class<T> expectedType) {
        var issues = result.issues();
        assertThat(issues).hasSize(1);
        Issue issue = issues.iterator().next();
        assertThat(issue).isInstanceOf(expectedType);
    }
}
