package org.acme.taskassigning.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.score.BendableScore;
import ai.timefold.solver.service.definition.api.ModelConvertor;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.taskassigning.domain.Affinity;
import org.acme.taskassigning.domain.Customer;
import org.acme.taskassigning.domain.Employee;
import org.acme.taskassigning.domain.Priority;
import org.acme.taskassigning.domain.Task;
import org.acme.taskassigning.domain.TaskAssigningSolution;
import org.acme.taskassigning.domain.TaskType;
import org.acme.taskassigning.dto.CustomerAffinityDTO;
import org.acme.taskassigning.dto.EmployeeDTO;
import org.acme.taskassigning.dto.TaskAssigningConfigOverrides;
import org.acme.taskassigning.dto.TaskAssigningInput;
import org.acme.taskassigning.dto.TaskAssigningOutput;
import org.acme.taskassigning.dto.TaskDTO;
import org.acme.taskassigning.dto.TaskTypeDTO;
import org.acme.taskassigning.solver.TaskAssigningConstraintProvider;

@ApplicationScoped
public class TaskAssigningModelConvertor
        implements
        ModelConvertor<BendableScore, TaskAssigningInput, TaskAssigningConfigOverrides, TaskAssigningSolution, TaskAssigningOutput> {

    private static final int HARD_LEVELS = 1;
    private static final int SOFT_LEVELS = 3;

    @Override
    public TaskAssigningInput applyOutputToInput(TaskAssigningInput modelInput, TaskAssigningOutput modelOutput) {
        Map<String, EmployeeDTO> outputEmployees = modelOutput.employees().stream()
                .collect(Collectors.toMap(EmployeeDTO::id, employee -> employee));
        List<EmployeeDTO> updatedEmployees = modelInput.employees().stream()
                .map(employee -> {
                    EmployeeDTO solved = outputEmployees.get(employee.id());
                    if (solved == null) {
                        return employee;
                    }
                    return employee.withTaskIds(solved.taskIds());
                })
                .collect(Collectors.toList());
        return new TaskAssigningInput(modelInput.taskTypes(), modelInput.customers(), updatedEmployees, modelInput.tasks());
    }

    @Override
    public TaskAssigningSolution toSolverModel(TaskAssigningInput modelInput,
            ModelConfig<TaskAssigningConfigOverrides> modelConfig, Optional<TaskAssigningOutput> lastModelOutput) {
        Map<String, TaskType> taskTypeMap = modelInput.taskTypes().stream()
                .map(this::toTaskType)
                .collect(Collectors.toMap(TaskType::getCode, taskType -> taskType, (a, b) -> a, LinkedHashMap::new));
        Map<String, Customer> customerMap = modelInput.customers().stream()
                .map(dto -> new Customer(dto.id(), dto.name()))
                .collect(Collectors.toMap(Customer::getId, customer -> customer, (a, b) -> a, LinkedHashMap::new));

        List<Task> tasks = modelInput.tasks().stream()
                .map(dto -> toTask(dto, taskTypeMap, customerMap))
                .collect(Collectors.toList());
        Map<String, Task> taskMap = tasks.stream()
                .collect(Collectors.toMap(Task::getId, task -> task));

        Map<String, Employee> employeeMap = new HashMap<>();
        List<Employee> employees = modelInput.employees().stream()
                .map(dto -> {
                    Employee employee = toEmployee(dto, customerMap);
                    employeeMap.put(employee.getId(), employee);
                    return employee;
                })
                .collect(Collectors.toList());

        assignTasks(modelInput.employees(), employeeMap, taskMap);

        TaskAssigningSolution solution = new TaskAssigningSolution(
                new ArrayList<>(taskTypeMap.values()), new ArrayList<>(customerMap.values()), tasks, employees);
        applyConstraintWeightOverrides(solution, modelConfig);
        applyLastOutput(employeeMap, taskMap, lastModelOutput);
        return solution;
    }

    private TaskType toTaskType(TaskTypeDTO dto) {
        return new TaskType(dto.code(), dto.title(), (int) dto.baseDuration(), List.copyOf(dto.requiredSkills()));
    }

    private Task toTask(TaskDTO dto, Map<String, TaskType> taskTypeMap, Map<String, Customer> customerMap) {
        TaskType taskType = taskTypeMap.get(dto.taskTypeCode());
        Customer customer = customerMap.get(dto.customerId());
        Priority priority = Priority.valueOf(dto.priority());
        Task task = new Task(dto.id(), taskType, dto.indexInTaskType(), customer, priority);
        task.setMinStartTime(dto.minStartTime());
        return task;
    }

    private Employee toEmployee(EmployeeDTO dto, Map<String, Customer> customerMap) {
        Map<Customer, Affinity> customerToAffinity = new LinkedHashMap<>();
        for (CustomerAffinityDTO affinity : dto.affinities()) {
            Customer customer = customerMap.get(affinity.customerId());
            if (customer != null) {
                customerToAffinity.put(customer, Affinity.valueOf(affinity.affinity()));
            }
        }
        return new Employee(dto.id(), dto.fullName(), new ArrayList<>(dto.skills()), customerToAffinity);
    }

    private static void assignTasks(List<EmployeeDTO> employeeDTOs, Map<String, Employee> employeeMap,
            Map<String, Task> taskMap) {
        for (EmployeeDTO dto : employeeDTOs) {
            Employee employee = employeeMap.get(dto.id());
            if (employee == null) {
                continue;
            }
            employee.getTasks().clear();
            for (String taskId : dto.taskIds()) {
                Task task = taskMap.get(taskId);
                if (task != null) {
                    employee.getTasks().add(task);
                }
            }
        }
    }

    private static void applyLastOutput(Map<String, Employee> employeeMap, Map<String, Task> taskMap,
            Optional<TaskAssigningOutput> lastModelOutput) {
        if (lastModelOutput.isEmpty()) {
            return;
        }
        assignTasks(lastModelOutput.get().employees(), employeeMap, taskMap);
    }

    private static void applyConstraintWeightOverrides(TaskAssigningSolution solution,
            ModelConfig<TaskAssigningConfigOverrides> modelConfig) {
        if (modelConfig == null || modelConfig.overrides() == null) {
            return;
        }
        TaskAssigningConfigOverrides overrides = modelConfig.overrides();
        // Only apply weights that are actually set (non-null) in the merged overrides. A null weight means the
        // input did not override it, so the configuration profile value (or the constraint's default) is kept.
        Map<String, BendableScore> weightOverrides = new HashMap<>();
        putIfPresent(weightOverrides, TaskAssigningConstraintProvider.MINIMIZE_UNASSIGNED_TASKS, 0,
                overrides.minimizeUnassignedTasksWeight());
        putIfPresent(weightOverrides, TaskAssigningConstraintProvider.MINIMIZE_MAKESPAN, 1,
                overrides.minimizeMakespanWeight());
        putIfPresent(weightOverrides, TaskAssigningConstraintProvider.CRITICAL_PRIORITY_TASK_END_TIME, 2,
                overrides.criticalPriorityWeight());
        putIfPresent(weightOverrides, TaskAssigningConstraintProvider.MAJOR_PRIORITY_TASK_END_TIME, 2,
                overrides.majorPriorityWeight());
        putIfPresent(weightOverrides, TaskAssigningConstraintProvider.MINOR_PRIORITY_TASK_END_TIME, 2,
                overrides.minorPriorityWeight());
        if (!weightOverrides.isEmpty()) {
            solution.setConstraintWeightOverrides(ConstraintWeightOverrides.of(weightOverrides));
        }
    }

    private static void putIfPresent(Map<String, BendableScore> weights, String constraintName, int softLevel, Long weight) {
        if (weight != null) {
            weights.put(constraintName, BendableScore.ofSoft(HARD_LEVELS, SOFT_LEVELS, softLevel, weight));
        }
    }

    @Override
    public TaskAssigningOutput toModelOutput(TaskAssigningSolution solverModel) {
        List<EmployeeDTO> employees = solverModel.getEmployees().stream().map(this::toDTO).collect(Collectors.toList());
        List<TaskDTO> tasks = solverModel.getTasks().stream().map(this::toDTO).collect(Collectors.toList());
        String score = solverModel.getScore() == null ? "" : solverModel.getScore().toString();
        return new TaskAssigningOutput(employees, tasks, score);
    }

    private EmployeeDTO toDTO(Employee employee) {
        List<String> taskIds = employee.getTasks().stream().map(Task::getId).collect(Collectors.toList());
        List<CustomerAffinityDTO> affinities = employee.getCustomerToAffinity().entrySet().stream()
                .map(entry -> new CustomerAffinityDTO(entry.getKey().getId(), entry.getValue().name()))
                .collect(Collectors.toList());
        return new EmployeeDTO(employee.getId(), employee.getFullName(), List.copyOf(employee.getSkills()), affinities,
                taskIds);
    }

    private TaskDTO toDTO(Task task) {
        String taskTypeCode = task.getTaskType() == null ? "" : task.getTaskType().getCode();
        String customerId = task.getCustomer() == null ? "" : task.getCustomer().getId();
        Long startTime = task.getStartTime();
        return new TaskDTO(task.getId(), taskTypeCode, task.getIndexInTaskType(), customerId, task.getMinStartTime(),
                task.getPriority().name(), startTime);
    }
}
