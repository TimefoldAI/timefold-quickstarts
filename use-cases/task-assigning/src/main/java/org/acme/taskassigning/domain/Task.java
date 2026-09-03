package org.acme.taskassigning.domain;

import java.util.Objects;

import ai.timefold.solver.core.api.domain.common.PlanningId;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;
import ai.timefold.solver.core.api.domain.variable.PreviousElementShadowVariable;
import ai.timefold.solver.core.api.domain.variable.ShadowSources;
import ai.timefold.solver.core.api.domain.variable.ShadowVariable;

@PlanningEntity
public class Task {

    @PlanningId
    private String id;
    private TaskType taskType;
    private int indexInTaskType;
    private Customer customer;
    private long minStartTime;
    private Priority priority;

    // Shadow variables
    @InverseRelationShadowVariable(sourceVariableName = "tasks")
    private Employee employee;
    @PreviousElementShadowVariable(sourceVariableName = "tasks")
    private Task previousTask;
    @ShadowVariable(supplierName = "startTimeSupplier")
    private Long startTime; // In minutes

    public Task() {
    }

    public Task(String id, TaskType taskType, int indexInTaskType, Customer customer, long minStartTime,
            Priority priority) {
        this.id = id;
        this.taskType = taskType;
        this.indexInTaskType = indexInTaskType;
        this.customer = customer;
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

    public long getMinStartTime() {
        return minStartTime;
    }

    public void setMinStartTime(long minStartTime) {
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

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @SuppressWarnings("unused")
    @ShadowSources({ "employee", "previousTask.startTime" })
    public Long startTimeSupplier() {
        if (employee == null) {
            return null;
        } else if (previousTask == null) {
            return (long) minStartTime;
        } else {
            var previousEndTime = previousTask.getEndTime();
            return Math.max(previousEndTime, minStartTime);
        }
    }

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

    public long getDuration() {
        Affinity affinity = getAffinity();
        return taskType.getBaseDuration() * affinity.getDurationMultiplier();
    }

    public Affinity getAffinity() {
        return (employee == null) ? Affinity.NONE : employee.getAffinity(customer);
    }

    public Long getEndTime() {
        if (startTime == null) {
            return null;
        }
        return startTime + getDuration();
    }

    public String getCode() {
        return taskType + "-" + indexInTaskType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Task task))
            return false;
        return Objects.equals(getId(), task.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return getCode();
    }

}
