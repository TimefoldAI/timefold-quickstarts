package org.acme.taskassigning.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;
import ai.timefold.solver.core.api.domain.variable.PreviousElementShadowVariable;
import ai.timefold.solver.core.api.domain.variable.ShadowSources;
import ai.timefold.solver.core.api.domain.variable.ShadowVariable;
import ai.timefold.solver.core.api.domain.variable.ShadowVariablesInconsistent;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@PlanningEntity
@JsonIdentityInfo(scope = Task.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Task {

    @PlanningId
    private String id;
    private TaskType taskType;
    private int indexInTaskType;
    private Customer customer;
    private int minStartTime;
    private Priority priority;

    // Shadow variables
    @JsonIgnore
    @InverseRelationShadowVariable(sourceVariableName = "tasks")
    private Employee employee;
    @JsonIgnore
    @PreviousElementShadowVariable(sourceVariableName = "tasks")
    private Task previousTask;
    // Not ignored, used in the UI.
    @ShadowVariable(supplierName = "startTimeSupplier")
    private Integer startTime; // In minutes

    public Task() {
    }

    public Task(String id, TaskType taskType, int indexInTaskType, Customer customer, Priority priority) {
        this.id = id;
        this.taskType = taskType;
        this.indexInTaskType = indexInTaskType;
        this.customer = customer;
        this.priority = priority;
    }

    public Task(String id, TaskType taskType, int indexInTaskType, Customer customer, Employee employee, int minStartTime,
                Priority priority) {
        this.id = id;
        this.taskType = taskType;
        this.indexInTaskType = indexInTaskType;
        this.customer = customer;
        this.employee = employee;
        this.minStartTime = minStartTime;
        this.priority = priority;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public int getIndexInTaskType() {
        return indexInTaskType;
    }

    public void setIndexInTaskType(int indexInTaskType) {
        this.indexInTaskType = indexInTaskType;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public int getMinStartTime() {
        return minStartTime;
    }

    public void setMinStartTime(int minStartTime) {
        this.minStartTime = minStartTime;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Task getPreviousTask() {
        return previousTask;
    }

    public void setPreviousTask(Task previousTask) {
        this.previousTask = previousTask;
    }

    public Integer getStartTime() {
        return startTime;
    }

    public void setStartTime(Integer startTime) {
        this.startTime = startTime;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @SuppressWarnings("unused")
    @ShadowSources({"employee", "previousTask.startTime"})
    private Integer startTimeSupplier() {
        if (employee == null) {
            return null;
        } else if (previousTask == null) {
            return minStartTime;
        } else {
            var previousEndTime = previousTask.getEndTime();
            return Math.max(previousEndTime, minStartTime);
        }
    }

    @JsonIgnore
    public int getMissingSkillCount() {
        if (employee == null) {
            return 0;
        }
        int count = 0;
        for (String skill : taskType.getRequiredSkills()) {
            if (!employee.getSkills().contains(skill)) {
                count++;
            }
        }
        return count;
    }

    @JsonIgnore
    public int getDuration() {
        Affinity affinity = getAffinity();
        return taskType.getBaseDuration() * affinity.getDurationMultiplier();
    }

    @JsonIgnore
    public Affinity getAffinity() {
        return (employee == null) ? Affinity.NONE : employee.getAffinity(customer);
    }

    @JsonIgnore
    public Integer getEndTime() {
        if (startTime == null) {
            return null;
        }
        return startTime + getDuration();
    }

    @JsonIgnore
    public String getCode() {
        return taskType + "-" + indexInTaskType;
    }

    @Override
    public String toString() {
        return getCode();
    }

}
