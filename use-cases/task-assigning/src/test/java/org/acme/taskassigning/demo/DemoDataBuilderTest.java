package org.acme.taskassigning.demo;

import static org.assertj.core.api.Assertions.assertThat;

import org.acme.taskassigning.dto.input.CustomerInputDTO;
import org.acme.taskassigning.dto.input.EmployeeInputDTO;
import org.acme.taskassigning.dto.input.TaskAssigningInput;
import org.acme.taskassigning.dto.input.TaskInputDTO;
import org.acme.taskassigning.dto.input.TaskTypeInputDTO;
import org.junit.jupiter.api.Test;

class DemoDataBuilderTest {

    @Test
    void shouldBuildData() {
        TaskAssigningInput problem = DemoDataBuilder.basic();

        assertThat(problem.customers()).hasSize(4);
        assertThat(problem.customers()).extracting(CustomerInputDTO::id).doesNotHaveDuplicates();
        assertThat(problem.taskTypes()).hasSize(4);
        assertThat(problem.taskTypes()).extracting(TaskTypeInputDTO::code).doesNotHaveDuplicates();
        assertThat(problem.employees()).hasSize(8);
        assertThat(problem.employees()).extracting(EmployeeInputDTO::id).doesNotHaveDuplicates();
        assertThat(problem.tasks()).hasSize(28);
        assertThat(problem.tasks()).extracting(TaskInputDTO::id).doesNotHaveDuplicates();
    }

    @Test
    void everyTaskStartsUnassigned() {
        TaskAssigningInput problem = DemoDataBuilder.basic();

        for (EmployeeInputDTO employee : problem.employees()) {
            assertThat(employee.taskIds()).isEmpty();
        }
    }

    @Test
    void everyTaskReferencesAKnownTaskTypeAndCustomer() {
        TaskAssigningInput problem = DemoDataBuilder.basic();
        var taskTypeCodes = problem.taskTypes().stream().map(TaskTypeInputDTO::code).toList();
        var customerIds = problem.customers().stream().map(CustomerInputDTO::id).toList();

        for (TaskInputDTO task : problem.tasks()) {
            assertThat(taskTypeCodes).contains(task.taskTypeCode());
            assertThat(customerIds).contains(task.customerId());
        }
    }

    @Test
    void everyEmployeeCustomerAffinityReferencesAKnownCustomer() {
        TaskAssigningInput problem = DemoDataBuilder.basic();
        var customerIds = problem.customers().stream().map(CustomerInputDTO::id).toList();

        for (EmployeeInputDTO employee : problem.employees()) {
            assertThat(customerIds).containsAll(employee.customerAffinities().keySet());
        }
    }
}
