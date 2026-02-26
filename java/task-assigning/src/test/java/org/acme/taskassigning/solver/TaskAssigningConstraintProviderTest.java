package org.acme.taskassigning.solver;

import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.score.stream.test.ConstraintVerifier;

import org.acme.taskassigning.domain.Affinity;
import org.acme.taskassigning.domain.Customer;
import org.acme.taskassigning.domain.Employee;
import org.acme.taskassigning.domain.Priority;
import org.acme.taskassigning.domain.Task;
import org.acme.taskassigning.domain.TaskAssigningSolution;
import org.acme.taskassigning.domain.TaskType;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class TaskAssigningConstraintProviderTest {

    @Inject
    ConstraintVerifier<TaskAssigningConstraintProvider, TaskAssigningSolution> constraintVerifier;

    @Test
    void noMissingSkills() {
        Customer customer = new Customer();
        Employee employee = new Employee();
        employee.setSkills(List.of("3"));
        // Task 1
        TaskType invalidType = new TaskType("1", "1", 1);
        invalidType.setRequiredSkills(List.of("1", "2"));
        Task taskInvalid = new Task("1", invalidType, 1, customer, employee, 1, Priority.CRITICAL);
        // Task 2
        TaskType validType = new TaskType("2", "2", 1);
        validType.setRequiredSkills(List.of("3"));
        Task taskValid = new Task("2", validType, 1, customer, employee, 1, Priority.CRITICAL);
        // Task 3
        TaskType invalidType2 = new TaskType("3", "3", 1);
        invalidType2.setRequiredSkills(List.of("5"));
        Task taskInvalid2 = new Task("3", invalidType2, 1, customer, employee, 1, Priority.CRITICAL);

        constraintVerifier.verifyThat(TaskAssigningConstraintProvider::noMissingSkills)
                .given(taskInvalid, taskValid, taskInvalid2)
                .penalizesBy(3); // two invalid tasks
    }

    @Test
    void minimizeUnassignedTasks() {
        Employee employee = new Employee();
        // Task 1
        Task taskInvalid = new Task("1", null, 1, null, null, 1, Priority.CRITICAL);
        // Task 2
        Task taskInvalid2 = new Task("2", null, 1, null, null, 1, Priority.CRITICAL);
        // Task 3
        Task taskValid = new Task("3", null, 1, null, employee, 1, Priority.CRITICAL);

        constraintVerifier.verifyThat(TaskAssigningConstraintProvider::minimizeUnassignedTasks)
                .given(taskInvalid, taskInvalid2, taskValid)
                .penalizesBy(2); // two invalid tasks
    }

    @Test
    void criticalPriorityTaskEndTime() {
        Employee employee = new Employee();
        Customer customer = new Customer("1");
        employee.setCustomerToAffinity(Map.of(customer, Affinity.HIGH));
        // Task 1
        TaskType type1 = new TaskType("1", "1", 10);
        Task task1 = new Task("1", type1, 1, customer, employee, 1, Priority.CRITICAL);
        task1.setStartTime(1L);
        // Task 2
        TaskType type2 = new TaskType("1", "1", 20);
        Task task2 = new Task("2", type2, 1, customer, employee, 2, Priority.MINOR);

        constraintVerifier.verifyThat(TaskAssigningConstraintProvider::criticalPriorityTaskEndTime)
                .given(task1, task2)
                .penalizesBy(44); // penalizes task1
    }

    @Test
    void minimizeMakespan() {
        Employee employee1 = new Employee();
        Employee employee2 = new Employee();
        Customer customer = new Customer("1");
        employee1.setCustomerToAffinity(Map.of(customer, Affinity.MEDIUM));
        employee2.setCustomerToAffinity(Map.of(customer, Affinity.HIGH));
        // Task 1
        TaskType type1 = new TaskType("1", "1", 10);
        Task task1 = new Task("1", type1, 1, customer, employee1, 1, Priority.CRITICAL);
        task1.setStartTime(1L);
        // Task 2
        TaskType type2 = new TaskType("2", "1", 20);
        Task task2 = new Task("2", type2, 1, customer, employee1, 2, Priority.MINOR);
        task2.setStartTime(2L);
        // Task 3
        TaskType invalidType2 = new TaskType("3", "3", 1);
        Task task3 = new Task("3", invalidType2, 1, customer, employee2, 1, Priority.CRITICAL);
        task3.setStartTime(3L);

        employee1.setTasks(List.of(task1, task2));
        employee2.setTasks(List.of(task3));

        constraintVerifier.verifyThat(TaskAssigningConstraintProvider::minimizeMakespan)
                .given(employee1, employee2)
                .penalizesBy(1780); // penalizes employee1 and employee2
    }

    @Test
    void majorPriorityTaskEndTime() {
        Employee employee = new Employee();
        Customer customer = new Customer("1");
        employee.setCustomerToAffinity(Map.of(customer, Affinity.HIGH));
        // Task 1
        TaskType type1 = new TaskType("1", "1", 10);
        Task task1 = new Task("1", type1, 1, customer, employee, 1, Priority.MAJOR);
        task1.setStartTime(1L);
        // Task 2
        TaskType type2 = new TaskType("1", "1", 20);
        Task task2 = new Task("2", type2, 1, customer, employee, 2, Priority.MINOR);

        constraintVerifier.verifyThat(TaskAssigningConstraintProvider::majorPriorityTaskEndTime)
                .given(task1, task2)
                .penalizesBy(22); // penalizes task1
    }

    @Test
    void minorPriorityTaskEndTime() {
        Employee employee = new Employee();
        Customer customer = new Customer("1");
        employee.setCustomerToAffinity(Map.of(customer, Affinity.HIGH));
        // Task 1
        TaskType type1 = new TaskType("1", "1", 10);
        Task task1 = new Task("1", type1, 1, customer, employee, 1, Priority.MINOR);
        task1.setStartTime(1L);
        // Task 2
        TaskType type2 = new TaskType("1", "1", 20);
        Task task2 = new Task("2", type2, 1, customer, employee, 2, Priority.MAJOR);

        constraintVerifier.verifyThat(TaskAssigningConstraintProvider::minorPriorityTaskEndTime)
                .given(task1, task2)
                .penalizesBy(11); // penalizes task1
    }

}
