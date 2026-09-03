package org.acme.taskassigning.solver;

import static org.acme.taskassigning.domain.TaskAssigningConstraintProperties.BENDABLE_SCORE_HARD_LEVELS_SIZE;
import static org.acme.taskassigning.domain.TaskAssigningConstraintProperties.BENDABLE_SCORE_SOFT_LEVELS_SIZE;

import ai.timefold.solver.core.api.score.BendableScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.uni.UniConstraintStream;
import ai.timefold.solver.service.definition.api.description.ConstraintInfo;

import org.acme.taskassigning.domain.Employee;
import org.acme.taskassigning.domain.Priority;
import org.acme.taskassigning.domain.Task;
import org.acme.taskassigning.domain.TaskAssigningConstraintProperties;
import org.acme.taskassigning.domain.justification.TaskAssigningJustification.MakespanJustification;
import org.acme.taskassigning.domain.justification.TaskAssigningJustification.MissingSkillsJustification;
import org.acme.taskassigning.domain.justification.TaskAssigningJustification.PriorityTaskEndTimeJustification;
import org.acme.taskassigning.domain.justification.TaskAssigningJustification.UnassignedTaskJustification;

public class TaskAssigningConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // Hard constraints
                noMissingSkills(constraintFactory),

                // Soft constraints
                minimizeUnassignedTasks(constraintFactory),
                minimizeMakespan(constraintFactory),
                criticalPriorityTaskEndTime(constraintFactory),
                majorPriorityTaskEndTime(constraintFactory),
                minorPriorityTaskEndTime(constraintFactory)
        };
    }

    protected Constraint noMissingSkills(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Task.class)
                .filter(task -> task.getMissingSkillCount() > 0)
                .penalize(BendableScore.ofHard(BENDABLE_SCORE_HARD_LEVELS_SIZE, BENDABLE_SCORE_SOFT_LEVELS_SIZE, 0, 1),
                        Task::getMissingSkillCount)
                .justifyWith((task, score) -> MissingSkillsJustification.of(task))
                .asConstraint(new ConstraintInfo(TaskAssigningConstraintProperties.NO_MISSING_SKILLS,
                        TaskAssigningConstraintProperties.NO_MISSING_SKILLS,
                        "An employee must have all the required skills for their assigned task.",
                        TaskAssigningConstraintGroup.SKILLS));
    }

    protected Constraint minimizeUnassignedTasks(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(Task.class)
                .filter(task -> task.getEmployee() == null)
                .penalize(BendableScore.ofSoft(BENDABLE_SCORE_HARD_LEVELS_SIZE, BENDABLE_SCORE_SOFT_LEVELS_SIZE, 0, 1))
                .justifyWith((task, score) -> UnassignedTaskJustification.of(task))
                .asConstraint(new ConstraintInfo(TaskAssigningConstraintProperties.MINIMIZE_UNASSIGNED_TASKS,
                        TaskAssigningConstraintProperties.MINIMIZE_UNASSIGNED_TASKS,
                        "All tasks should be assigned to an employee.",
                        TaskAssigningConstraintGroup.WORKLOAD));
    }

    private UniConstraintStream<Task> getTaskWithPriority(ConstraintFactory constraintFactory, Priority priority) {
        return constraintFactory.forEach(Task.class)
                .filter(task -> task.getEmployee() != null)
                .filter(task -> task.getPriority() == priority);
    }

    protected Constraint minimizeMakespan(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Employee.class)
                .penalize(BendableScore.ofSoft(BENDABLE_SCORE_HARD_LEVELS_SIZE, BENDABLE_SCORE_SOFT_LEVELS_SIZE, 1, 1),
                        employee -> employee.getEndTime() * employee.getEndTime())
                .justifyWith((employee, score) -> MakespanJustification.of(employee))
                .asConstraint(new ConstraintInfo(TaskAssigningConstraintProperties.MINIMIZE_MAKESPAN,
                        TaskAssigningConstraintProperties.MINIMIZE_MAKESPAN,
                        "Minimize the time until all tasks are completed, weighing the latest ending employee the most.",
                        TaskAssigningConstraintGroup.WORKLOAD));
    }

    protected Constraint criticalPriorityTaskEndTime(ConstraintFactory constraintFactory) {
        return getTaskWithPriority(constraintFactory, Priority.CRITICAL)
                .penalize(BendableScore.ofSoft(BENDABLE_SCORE_HARD_LEVELS_SIZE, BENDABLE_SCORE_SOFT_LEVELS_SIZE, 2, 1),
                        task -> task.getEndTime() * 4)
                .justifyWith((task, score) -> PriorityTaskEndTimeJustification.of(task))
                .asConstraint(new ConstraintInfo(TaskAssigningConstraintProperties.CRITICAL_PRIORITY_TASK_END_TIME,
                        TaskAssigningConstraintProperties.CRITICAL_PRIORITY_TASK_END_TIME,
                        "Critical priority tasks should be completed as early as possible.",
                        TaskAssigningConstraintGroup.TASK_PRIORITY));
    }

    protected Constraint majorPriorityTaskEndTime(ConstraintFactory constraintFactory) {
        return getTaskWithPriority(constraintFactory, Priority.MAJOR)
                .penalize(BendableScore.ofSoft(BENDABLE_SCORE_HARD_LEVELS_SIZE, BENDABLE_SCORE_SOFT_LEVELS_SIZE, 2, 1),
                        task -> task.getEndTime() * 2)
                .justifyWith((task, score) -> PriorityTaskEndTimeJustification.of(task))
                .asConstraint(new ConstraintInfo(TaskAssigningConstraintProperties.MAJOR_PRIORITY_TASK_END_TIME,
                        TaskAssigningConstraintProperties.MAJOR_PRIORITY_TASK_END_TIME,
                        "Major priority tasks should be completed as early as possible.",
                        TaskAssigningConstraintGroup.TASK_PRIORITY));
    }

    protected Constraint minorPriorityTaskEndTime(ConstraintFactory constraintFactory) {
        return getTaskWithPriority(constraintFactory, Priority.MINOR)
                .penalize(BendableScore.ofSoft(BENDABLE_SCORE_HARD_LEVELS_SIZE, BENDABLE_SCORE_SOFT_LEVELS_SIZE, 2, 1),
                        Task::getEndTime)
                .justifyWith((task, score) -> PriorityTaskEndTimeJustification.of(task))
                .asConstraint(new ConstraintInfo(TaskAssigningConstraintProperties.MINOR_PRIORITY_TASK_END_TIME,
                        TaskAssigningConstraintProperties.MINOR_PRIORITY_TASK_END_TIME,
                        "Minor priority tasks should be completed as early as possible.",
                        TaskAssigningConstraintGroup.TASK_PRIORITY));
    }
}
