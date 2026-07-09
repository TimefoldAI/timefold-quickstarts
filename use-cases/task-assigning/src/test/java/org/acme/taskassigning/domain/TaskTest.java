package org.acme.taskassigning.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class TaskTest {

    @Test
    void startTimeSupplierUnassigned() {
        Task task = new Task("1", new TaskType("T", "T", 10), 1, new Customer("c"), Priority.MINOR);
        // employee == null branch
        assertThat(task.startTimeSupplier()).isNull();
    }

    @Test
    void startTimeSupplierFirstTask() {
        Customer customer = new Customer("c");
        Employee employee = new Employee("e", "Emp", List.of("s"), Map.of(customer, Affinity.HIGH));
        Task task = new Task("1", new TaskType("T", "T", 10), 1, customer, Priority.MINOR);
        task.setMinStartTime(7L);
        task.setEmployee(employee);
        // previousTask == null branch -> minStartTime
        assertThat(task.startTimeSupplier()).isEqualTo(7L);
    }

    @Test
    void startTimeSupplierFollowingTask() {
        Customer customer = new Customer("c");
        Employee employee = new Employee("e", "Emp", List.of("s"), Map.of(customer, Affinity.HIGH));
        TaskType type = new TaskType("T", "T", 10);

        Task previous = new Task("1", type, 1, customer, Priority.MINOR);
        previous.setEmployee(employee);
        previous.setMinStartTime(5L);
        previous.setStartTime(5L);

        Task task = new Task("2", type, 2, customer, Priority.MINOR);
        task.setEmployee(employee);
        task.setMinStartTime(1L);
        task.setPreviousTask(previous);
        // else branch -> max(previousEndTime, minStartTime); previous endTime = 5 + 10 = 15
        assertThat(task.startTimeSupplier()).isEqualTo(15L);
    }

    @Test
    void endTimeAndDurationAndMissingSkills() {
        Customer customer = new Customer("c");
        Employee employee = new Employee("e", "Emp", List.of("present"), Map.of(customer, Affinity.MEDIUM));
        TaskType type = new TaskType("T", "T", 10, List.of("present", "missing"));
        Task task = new Task("1", type, 1, customer, Priority.MINOR);
        task.setEmployee(employee);
        task.setStartTime(2L);

        assertThat(task.getDuration()).isEqualTo(20L); // 10 * MEDIUM(2)
        assertThat(task.getEndTime()).isEqualTo(22L);
        assertThat(task.getMissingSkillCount()).isEqualTo(1);
        assertThat(task.getAffinity()).isEqualTo(Affinity.MEDIUM);

        Task unassigned = new Task("2", type, 1, customer, Priority.MINOR);
        assertThat(unassigned.getEndTime()).isNull();
        assertThat(unassigned.getMissingSkillCount()).isZero();
        assertThat(unassigned.getAffinity()).isEqualTo(Affinity.NONE);
    }
}
