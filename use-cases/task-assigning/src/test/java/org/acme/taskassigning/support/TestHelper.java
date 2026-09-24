package org.acme.taskassigning.support;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.acme.taskassigning.domain.Affinity;
import org.acme.taskassigning.domain.Customer;
import org.acme.taskassigning.domain.Employee;
import org.acme.taskassigning.domain.Priority;
import org.acme.taskassigning.domain.Task;
import org.acme.taskassigning.domain.TaskType;
import org.acme.taskassigning.dto.input.CustomerInputDTO;
import org.acme.taskassigning.dto.input.EmployeeInputDTO;
import org.acme.taskassigning.dto.input.TaskAssigningInput;
import org.acme.taskassigning.dto.input.TaskInputDTO;
import org.acme.taskassigning.dto.input.TaskTypeInputDTO;

// To keep our production classes as simple as possible, we've added these methods to help construct the data needed for testing.
public final class TestHelper {

    private TestHelper() {
    }

    // ************************************************************************
    // Solver model
    // ************************************************************************

    public static CustomerBuilder aCustomer(String id) {
        return new CustomerBuilder(id);
    }

    public static TaskTypeBuilder aTaskType(String code) {
        return new TaskTypeBuilder(code);
    }

    public static EmployeeBuilder anEmployee(String id) {
        return new EmployeeBuilder(id);
    }

    public static TaskBuilder aTask(String id) {
        return new TaskBuilder(id);
    }

    public static final class CustomerBuilder {

        private final String id;
        private String name;

        private CustomerBuilder(String id) {
            this.id = id;
            this.name = "Customer " + id;
        }

        public CustomerBuilder name(String name) {
            this.name = name;
            return this;
        }

        public Customer build() {
            return new Customer(id, name);
        }
    }

    public static final class TaskTypeBuilder {

        private final String code;
        private String title;
        private int baseDuration = 60;
        private List<String> requiredSkills = List.of();

        private TaskTypeBuilder(String code) {
            this.code = code;
            this.title = "Task Type " + code;
        }

        public TaskTypeBuilder baseDuration(int baseDuration) {
            this.baseDuration = baseDuration;
            return this;
        }

        public TaskTypeBuilder requiredSkills(List<String> requiredSkills) {
            this.requiredSkills = requiredSkills;
            return this;
        }

        public TaskType build() {
            return new TaskType(code, title, baseDuration, requiredSkills);
        }
    }

    public static final class EmployeeBuilder {

        private final String id;
        private String fullName;
        private List<String> skills = List.of();
        private final Map<Customer, Affinity> customerToAffinity = new LinkedHashMap<>();
        private List<TaskBuilder> tasks = List.of();

        private EmployeeBuilder(String id) {
            this.id = id;
            this.fullName = "Employee " + id;
        }

        public EmployeeBuilder skills(List<String> skills) {
            this.skills = skills;
            return this;
        }

        public EmployeeBuilder affinity(CustomerBuilder customer, Affinity affinity) {
            this.customerToAffinity.put(customer.build(), affinity);
            return this;
        }

        /**
         * Sets the tasks assigned to this employee, in order. Each task's own shadow variables (startTime, and so on)
         * must already be set through {@link TaskBuilder}, since building this employee does not run the solver.
         */
        public EmployeeBuilder tasks(List<TaskBuilder> tasks) {
            this.tasks = tasks;
            return this;
        }

        public Employee build() {
            var employee = new Employee(id, fullName, new ArrayList<>(skills), new LinkedHashMap<>(customerToAffinity));
            employee.setTasks(new ArrayList<>(tasks.stream().map(TaskBuilder::build).toList()));
            return employee;
        }
    }

    public static final class TaskBuilder {

        private final String id;
        private TaskTypeBuilder taskType = new TaskTypeBuilder("DEFAULT_TYPE");
        private int indexInTaskType = 1;
        private CustomerBuilder customer = new CustomerBuilder("DEFAULT_CUSTOMER");
        private long minStartTime;
        private Priority priority = Priority.MINOR;
        private EmployeeBuilder employee;
        private TaskBuilder previousTask;
        private Long startTime;

        private TaskBuilder(String id) {
            this.id = id;
        }

        public TaskBuilder taskType(TaskTypeBuilder taskType) {
            this.taskType = taskType;
            return this;
        }

        public TaskBuilder customer(CustomerBuilder customer) {
            this.customer = customer;
            return this;
        }

        public TaskBuilder minStartTime(long minStartTime) {
            this.minStartTime = minStartTime;
            return this;
        }

        public TaskBuilder priority(Priority priority) {
            this.priority = priority;
            return this;
        }

        /**
         * Directly sets the {@code employee} shadow variable, since {@code ConstraintVerifier} does not run the
         * solver's variable listeners for isolated constraint tests.
         */
        public TaskBuilder employee(EmployeeBuilder employee) {
            this.employee = employee;
            return this;
        }

        public TaskBuilder previousTask(TaskBuilder previousTask) {
            this.previousTask = previousTask;
            return this;
        }

        public TaskBuilder startTime(long startTime) {
            this.startTime = startTime;
            return this;
        }

        public Task build() {
            var task = new Task(id, taskType.build(), indexInTaskType, customer.build(), minStartTime, priority);
            if (employee != null) {
                task.setEmployee(employee.build());
            }
            if (previousTask != null) {
                task.setPreviousTask(previousTask.build());
            }
            if (startTime != null) {
                task.setStartTime(startTime);
            }
            return task;
        }
    }

    // ************************************************************************
    // Input DTOs
    // ************************************************************************

    public static TaskAssigningInput input(List<CustomerInputDTO> customers, List<TaskTypeInputDTO> taskTypes,
            List<EmployeeInputDTO> employees, List<TaskInputDTO> tasks) {
        return new TaskAssigningInput(customers, taskTypes, employees, tasks);
    }

    public static CustomerDTOBuilder aCustomerDTO(String id) {
        return new CustomerDTOBuilder(id);
    }

    public static TaskTypeDTOBuilder aTaskTypeDTO(String code) {
        return new TaskTypeDTOBuilder(code);
    }

    public static EmployeeDTOBuilder anEmployeeDTO(String id) {
        return new EmployeeDTOBuilder(id);
    }

    public static TaskDTOBuilder aTaskDTO(String id) {
        return new TaskDTOBuilder(id);
    }

    /**
     * A small, deliberately conflict-free problem: every employee has every skill the tasks require, so a zero hard
     * score is within easy reach of the solver.
     */
    public static TaskAssigningInput createProblem() {
        List<String> skills = List.of("Skill A", "Skill B");
        List<CustomerInputDTO> customers = List.of(aCustomerDTO("C1").build(), aCustomerDTO("C2").build());
        List<TaskTypeInputDTO> taskTypes = List.of(
                aTaskTypeDTO("T1").requiredSkills(List.of("Skill A")).build(),
                aTaskTypeDTO("T2").requiredSkills(List.of("Skill B")).build());
        List<EmployeeInputDTO> employees = List.of(
                anEmployeeDTO("E1").skills(skills).build(),
                anEmployeeDTO("E2").skills(skills).build());
        List<TaskInputDTO> tasks = List.of(
                aTaskDTO("1").taskTypeCode("T1").customerId("C1").priority(Priority.CRITICAL).build(),
                aTaskDTO("2").taskTypeCode("T2").customerId("C2").priority(Priority.MAJOR).build(),
                aTaskDTO("3").taskTypeCode("T1").customerId("C1").priority(Priority.MINOR).build(),
                aTaskDTO("4").taskTypeCode("T2").customerId("C2").priority(Priority.MINOR).build());
        return input(customers, taskTypes, employees, tasks);
    }

    public static final class CustomerDTOBuilder {

        private final String id;
        private String name;

        private CustomerDTOBuilder(String id) {
            this.id = id;
            this.name = "Customer " + id;
        }

        public CustomerDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public CustomerInputDTO build() {
            return new CustomerInputDTO(id, name);
        }
    }

    public static final class TaskTypeDTOBuilder {

        private final String code;
        private String title;
        private Integer baseDurationInMinutes = 60;
        private List<String> requiredSkills = List.of();

        private TaskTypeDTOBuilder(String code) {
            this.code = code;
            this.title = "Task Type " + code;
        }

        public TaskTypeDTOBuilder baseDurationInMinutes(Integer baseDurationInMinutes) {
            this.baseDurationInMinutes = baseDurationInMinutes;
            return this;
        }

        public TaskTypeDTOBuilder requiredSkills(List<String> requiredSkills) {
            this.requiredSkills = requiredSkills;
            return this;
        }

        public TaskTypeInputDTO build() {
            return new TaskTypeInputDTO(code, title, baseDurationInMinutes, requiredSkills);
        }
    }

    public static final class EmployeeDTOBuilder {

        private final String id;
        private String fullName;
        private List<String> skills = List.of();
        private Map<String, Affinity> customerAffinities = Map.of();
        private List<String> taskIds;

        private EmployeeDTOBuilder(String id) {
            this.id = id;
            this.fullName = "Employee " + id;
        }

        public EmployeeDTOBuilder skills(List<String> skills) {
            this.skills = skills;
            return this;
        }

        public EmployeeDTOBuilder customerAffinities(Map<String, Affinity> customerAffinities) {
            this.customerAffinities = customerAffinities;
            return this;
        }

        public EmployeeDTOBuilder taskIds(List<String> taskIds) {
            this.taskIds = taskIds;
            return this;
        }

        public EmployeeInputDTO build() {
            return new EmployeeInputDTO(id, fullName, skills, customerAffinities, taskIds);
        }
    }

    public static final class TaskDTOBuilder {

        private final String id;
        private String taskTypeCode;
        private Integer indexInTaskType = 1;
        private String customerId;
        private Long minStartTimeInMinutes = 0L;
        private Priority priority = Priority.MINOR;

        private TaskDTOBuilder(String id) {
            this.id = id;
        }

        public TaskDTOBuilder taskTypeCode(String taskTypeCode) {
            this.taskTypeCode = taskTypeCode;
            return this;
        }

        public TaskDTOBuilder indexInTaskType(Integer indexInTaskType) {
            this.indexInTaskType = indexInTaskType;
            return this;
        }

        public TaskDTOBuilder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public TaskDTOBuilder minStartTimeInMinutes(Long minStartTimeInMinutes) {
            this.minStartTimeInMinutes = minStartTimeInMinutes;
            return this;
        }

        public TaskDTOBuilder priority(Priority priority) {
            this.priority = priority;
            return this;
        }

        public TaskInputDTO build() {
            return new TaskInputDTO(id, taskTypeCode, indexInTaskType, customerId, minStartTimeInMinutes, priority);
        }
    }
}
