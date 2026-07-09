package org.acme.taskassigning;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.acme.taskassigning.dto.CustomerAffinityDTO;
import org.acme.taskassigning.dto.CustomerDTO;
import org.acme.taskassigning.dto.EmployeeDTO;
import org.acme.taskassigning.dto.EmployeeIdDetail;
import org.acme.taskassigning.dto.TaskAssigningConfigOverrides;
import org.acme.taskassigning.dto.TaskAssigningInput;
import org.acme.taskassigning.dto.TaskAssigningInputMetrics;
import org.acme.taskassigning.dto.TaskAssigningOutput;
import org.acme.taskassigning.dto.TaskAssigningOutputMetrics;
import org.acme.taskassigning.dto.TaskDTO;
import org.acme.taskassigning.dto.TaskIdDetail;
import org.acme.taskassigning.dto.TaskTypeDTO;
import org.junit.jupiter.api.Test;

class DtoWithMethodsUsageTest {

    @Test
    void allWithMethodsProduceUpdatedCopies() {
        var updatedTaskType = new TaskTypeDTO("IS", "Improve Sales", 46, List.of("a"))
                .withCode("ET")
                .withTitle("Expand Tax")
                .withBaseDuration(63)
                .withRequiredSkills(List.of("b"));

        var updatedCustomer = new CustomerDTO("1", "Steel Inc")
                .withId("2")
                .withName("Paper Corp");

        var updatedAffinity = new CustomerAffinityDTO("1", "HIGH")
                .withCustomerId("2")
                .withAffinity("LOW");

        var baseEmployee = new EmployeeDTO("1", "Amy", List.of("a"), List.of(updatedAffinity), List.of("1"));
        var updatedEmployee = baseEmployee
                .withId("2")
                .withFullName("Beth")
                .withSkills(List.of("b"))
                .withAffinities(List.of(updatedAffinity))
                .withTaskIds(List.of("2"));

        Long startTime = 5L;
        var baseTask = new TaskDTO("1", "IS", 1, "1", 0L, "MINOR", startTime);
        var updatedTask = baseTask
                .withId("2")
                .withTaskTypeCode("ET")
                .withIndexInTaskType(2)
                .withCustomerId("2")
                .withMinStartTime(10L)
                .withPriority("MAJOR")
                .withStartTime(20L);

        var updatedTaskIdDetail = new TaskIdDetail("1").withTaskId("2");
        var updatedEmployeeIdDetail = new EmployeeIdDetail("1").withEmployeeId("2");

        var updatedOverrides = new TaskAssigningConfigOverrides()
                .withMinimizeUnassignedTasksWeight(10L)
                .withMinimizeMakespanWeight(20L)
                .withCriticalPriorityWeight(30L)
                .withMajorPriorityWeight(40L)
                .withMinorPriorityWeight(50L);

        var updatedInput = new TaskAssigningInput(List.of(updatedTaskType), List.of(updatedCustomer),
                List.of(baseEmployee), List.of(baseTask))
                .withTaskTypes(List.of(updatedTaskType))
                .withCustomers(List.of(updatedCustomer))
                .withEmployees(List.of(updatedEmployee))
                .withTasks(List.of(updatedTask));

        var updatedOutput = new TaskAssigningOutput(List.of(baseEmployee), List.of(baseTask), "[0]hard/[0/0/0]soft")
                .withEmployees(List.of(updatedEmployee))
                .withTasks(List.of(updatedTask))
                .withScore("[0]hard/[1/1/1]soft");

        var updatedInputMetrics = new TaskAssigningInputMetrics(1, 2, 3, 4)
                .withEmployees(10)
                .withTasks(20)
                .withTaskTypes(30)
                .withCustomers(40);

        var updatedOutputMetrics = new TaskAssigningOutputMetrics(1, 2, 3, 4L)
                .withAssignedTasks(10)
                .withUnassignedTasks(20)
                .withUsedEmployees(30)
                .withMakespan(40L);

        assertThat(updatedTaskType.code()).isEqualTo("ET");
        assertThat(updatedTaskType.title()).isEqualTo("Expand Tax");
        assertThat(updatedTaskType.baseDuration()).isEqualTo(63L);
        assertThat(updatedTaskType.requiredSkills()).containsExactly("b");
        assertThat(updatedCustomer.id()).isEqualTo("2");
        assertThat(updatedCustomer.name()).isEqualTo("Paper Corp");
        assertThat(updatedAffinity.customerId()).isEqualTo("2");
        assertThat(updatedAffinity.affinity()).isEqualTo("LOW");
        assertThat(updatedEmployee.id()).isEqualTo("2");
        assertThat(updatedEmployee.fullName()).isEqualTo("Beth");
        assertThat(updatedEmployee.skills()).containsExactly("b");
        assertThat(updatedEmployee.taskIds()).containsExactly("2");
        assertThat(updatedTask.id()).isEqualTo("2");
        assertThat(updatedTask.taskTypeCode()).isEqualTo("ET");
        assertThat(updatedTask.indexInTaskType()).isEqualTo(2);
        assertThat(updatedTask.customerId()).isEqualTo("2");
        assertThat(updatedTask.minStartTime()).isEqualTo(10L);
        assertThat(updatedTask.priority()).isEqualTo("MAJOR");
        assertThat(updatedTask.startTime()).isEqualTo(20L);
        assertThat(updatedTaskIdDetail.taskId()).isEqualTo("2");
        assertThat(updatedEmployeeIdDetail.employeeId()).isEqualTo("2");
        assertThat(updatedOverrides.minimizeUnassignedTasksWeight()).isEqualTo(10L);
        assertThat(updatedOverrides.minimizeMakespanWeight()).isEqualTo(20L);
        assertThat(updatedOverrides.criticalPriorityWeight()).isEqualTo(30L);
        assertThat(updatedOverrides.majorPriorityWeight()).isEqualTo(40L);
        assertThat(updatedOverrides.minorPriorityWeight()).isEqualTo(50L);
        assertThat(updatedInput.taskTypes()).containsExactly(updatedTaskType);
        assertThat(updatedInput.customers()).containsExactly(updatedCustomer);
        assertThat(updatedInput.employees()).containsExactly(updatedEmployee);
        assertThat(updatedInput.tasks()).containsExactly(updatedTask);
        assertThat(updatedOutput.employees()).containsExactly(updatedEmployee);
        assertThat(updatedOutput.tasks()).containsExactly(updatedTask);
        assertThat(updatedOutput.score()).isEqualTo("[0]hard/[1/1/1]soft");
        assertThat(updatedInputMetrics.employees()).isEqualTo(10);
        assertThat(updatedInputMetrics.tasks()).isEqualTo(20);
        assertThat(updatedInputMetrics.taskTypes()).isEqualTo(30);
        assertThat(updatedInputMetrics.customers()).isEqualTo(40);
        assertThat(updatedOutputMetrics.assignedTasks()).isEqualTo(10);
        assertThat(updatedOutputMetrics.unassignedTasks()).isEqualTo(20);
        assertThat(updatedOutputMetrics.usedEmployees()).isEqualTo(30);
        assertThat(updatedOutputMetrics.makespan()).isEqualTo(40L);
    }
}
