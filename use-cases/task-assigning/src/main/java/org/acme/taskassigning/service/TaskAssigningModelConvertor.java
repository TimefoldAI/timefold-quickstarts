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
import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.service.definition.api.ModelConvertor;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.taskassigning.domain.Affinity;
import org.acme.taskassigning.domain.Customer;
import org.acme.taskassigning.domain.Employee;
import org.acme.taskassigning.domain.Task;
import org.acme.taskassigning.domain.TaskAssigningConstraintProperties;
import org.acme.taskassigning.domain.TaskAssigningSolution;
import org.acme.taskassigning.domain.TaskType;
import org.acme.taskassigning.dto.input.EmployeeInputDTO;
import org.acme.taskassigning.dto.input.TaskAssigningConfigOverrides;
import org.acme.taskassigning.dto.input.TaskAssigningInput;
import org.acme.taskassigning.dto.input.TaskInputDTO;
import org.acme.taskassigning.dto.output.AssignedTaskOutputDTO;
import org.acme.taskassigning.dto.output.EmployeeOutputDTO;
import org.acme.taskassigning.dto.output.TaskAssigningOutput;

@ApplicationScoped
public class TaskAssigningModelConvertor implements
        ModelConvertor<HardMediumSoftScore, TaskAssigningInput, TaskAssigningConfigOverrides, TaskAssigningSolution, TaskAssigningOutput> {

    @Override
    public TaskAssigningSolution toSolverModel(TaskAssigningInput modelInput,
            ModelConfig<TaskAssigningConfigOverrides> modelConfig, Optional<TaskAssigningOutput> lastModelOutput) {
        Map<String, Customer> customerMap = modelInput.customers().stream()
                .map(dto -> new Customer(dto.id(), dto.name()))
                .collect(Collectors.toMap(Customer::getId, customer -> customer, (left, right) -> left,
                        LinkedHashMap::new));
        Map<String, TaskType> taskTypeMap = modelInput.taskTypes().stream()
                .map(dto -> new TaskType(dto.code(), dto.title(), dto.baseDurationInMinutes(), dto.requiredSkills()))
                .collect(Collectors.toMap(TaskType::getCode, taskType -> taskType, (left, right) -> left,
                        LinkedHashMap::new));

        Map<String, Task> taskMap = modelInput.tasks().stream()
                .collect(Collectors.toMap(TaskInputDTO::id,
                        dto -> new Task(dto.id(), require(taskTypeMap, dto.taskTypeCode(), "task type"),
                                dto.indexInTaskType(), require(customerMap, dto.customerId(), "customer"),
                                dto.minStartTimeInMinutes(), dto.priority()),
                        (left, right) -> left, LinkedHashMap::new));

        List<Employee> employees = new ArrayList<>(modelInput.employees().size());
        for (EmployeeInputDTO dto : modelInput.employees()) {
            var affinities = new LinkedHashMap<Customer, Affinity>();
            dto.customerAffinities()
                    .forEach((customerId, affinity) -> affinities.put(require(customerMap, customerId, "customer"), affinity));
            employees.add(new Employee(dto.id(), dto.fullName(), new ArrayList<>(dto.skills()), affinities));
        }
        Map<String, Employee> employeeMap = employees.stream()
                .collect(Collectors.toMap(Employee::getId, employee -> employee, (left, right) -> left,
                        LinkedHashMap::new));

        applyAssignments(employeeMap, taskMap, modelInput.employees(), lastModelOutput);

        var solution = new TaskAssigningSolution(List.copyOf(taskTypeMap.values()), List.copyOf(customerMap.values()),
                List.copyOf(taskMap.values()), employees);
        applyConstraintWeightOverrides(solution, modelConfig);
        return solution;
    }

    @Override
    public TaskAssigningOutput toModelOutput(TaskAssigningSolution solverModel) {
        var employees = solverModel.getEmployees().stream()
                .map(employee -> new EmployeeOutputDTO(employee.getId(),
                        employee.getTasks().stream()
                                .map(task -> new AssignedTaskOutputDTO(task.getId(),
                                        task.getStartTime() == null ? 0L : task.getStartTime()))
                                .toList()))
                .toList();
        return new TaskAssigningOutput(employees);
    }

    @Override
    public TaskAssigningInput applyOutputToInput(TaskAssigningInput modelInput, TaskAssigningOutput modelOutput) {
        Map<String, EmployeeOutputDTO> outputEmployees = modelOutput.employees().stream()
                .collect(Collectors.toMap(EmployeeOutputDTO::id, employee -> employee));
        List<EmployeeInputDTO> updatedEmployees = modelInput.employees().stream()
                .map(employee -> {
                    EmployeeOutputDTO solved = outputEmployees.get(employee.id());
                    return solved == null ? employee
                            : employee.withTaskIds(solved.assignedTasks().stream()
                                    .map(AssignedTaskOutputDTO::taskId)
                                    .toList());
                })
                .toList();
        return modelInput.withEmployees(updatedEmployees);
    }

    /**
     * Assigns tasks to employees, in order, from {@code lastModelOutput} when it is present (to recover a run that
     * stopped halfway), or otherwise from the input's own {@code EmployeeInputDTO.taskIds()}. Only the list variable
     * itself is set here; the solver computes the {@code employee}, {@code previousTask} and {@code startTime} shadow
     * variables from it once solving starts.
     */
    private static void applyAssignments(Map<String, Employee> employeeMap, Map<String, Task> taskMap,
            List<EmployeeInputDTO> employeeInputs, Optional<TaskAssigningOutput> lastModelOutput) {
        if (lastModelOutput.isPresent()) {
            for (EmployeeOutputDTO solved : lastModelOutput.get().employees()) {
                Employee employee = employeeMap.get(solved.id());
                if (employee == null) {
                    continue;
                }
                employee.setTasks(toTasks(taskMap, solved.assignedTasks().stream()
                        .map(AssignedTaskOutputDTO::taskId)
                        .toList()));
            }
            return;
        }
        for (EmployeeInputDTO dto : employeeInputs) {
            Employee employee = require(employeeMap, dto.id(), "employee");
            employee.setTasks(toTasks(taskMap, dto.taskIds()));
        }
    }

    private static List<Task> toTasks(Map<String, Task> taskMap, List<String> taskIds) {
        List<Task> tasks = new ArrayList<>(taskIds.size());
        for (String taskId : taskIds) {
            tasks.add(require(taskMap, taskId, "task"));
        }
        return tasks;
    }

    /**
     * Fails fast with an actionable message instead of letting an unknown reference
     * turn into a null in the solver model and a delayed NullPointerException.
     */
    private static <K, T> T require(Map<K, T> map, K key, String kind) {
        T value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Unknown %s '%s'.".formatted(kind, key));
        }
        return value;
    }

    private static void applyConstraintWeightOverrides(TaskAssigningSolution solution,
            ModelConfig<TaskAssigningConfigOverrides> modelConfig) {
        if (modelConfig == null || modelConfig.overrides() == null) {
            return;
        }
        var overrides = modelConfig.overrides();
        // Only apply weights that are actually set (non-null) in the merged overrides. A null weight means the
        // input did not override it, so the configuration profile value (or the constraint's default) is kept.
        Map<String, HardMediumSoftScore> weights = new HashMap<>();
        putIfPresent(weights, TaskAssigningConstraintProperties.NO_MISSING_SKILLS,
                overrides.missingSkillsWeight() == null ? null : HardMediumSoftScore.ofHard(overrides.missingSkillsWeight()));
        putIfPresent(weights, TaskAssigningConstraintProperties.MINIMIZE_UNASSIGNED_TASKS,
                overrides.unassignedTasksWeight() == null ? null
                        : HardMediumSoftScore.ofMedium(overrides.unassignedTasksWeight()));
        putIfPresent(weights, TaskAssigningConstraintProperties.MINIMIZE_MAKESPAN,
                overrides.makespanWeight() == null ? null : HardMediumSoftScore.ofSoft(overrides.makespanWeight()));
        putIfPresent(weights, TaskAssigningConstraintProperties.CRITICAL_PRIORITY_TASK_END_TIME,
                overrides.criticalPriorityTaskEndTimeWeight() == null ? null
                        : HardMediumSoftScore.ofSoft(overrides.criticalPriorityTaskEndTimeWeight()));
        putIfPresent(weights, TaskAssigningConstraintProperties.MAJOR_PRIORITY_TASK_END_TIME,
                overrides.majorPriorityTaskEndTimeWeight() == null ? null
                        : HardMediumSoftScore.ofSoft(overrides.majorPriorityTaskEndTimeWeight()));
        putIfPresent(weights, TaskAssigningConstraintProperties.MINOR_PRIORITY_TASK_END_TIME,
                overrides.minorPriorityTaskEndTimeWeight() == null ? null
                        : HardMediumSoftScore.ofSoft(overrides.minorPriorityTaskEndTimeWeight()));
        if (!weights.isEmpty()) {
            solution.setConstraintWeightOverrides(ConstraintWeightOverrides.of(weights));
        }
    }

    private static void putIfPresent(Map<String, HardMediumSoftScore> weights, String constraintName,
            HardMediumSoftScore weight) {
        if (weight != null) {
            weights.put(constraintName, weight);
        }
    }
}
