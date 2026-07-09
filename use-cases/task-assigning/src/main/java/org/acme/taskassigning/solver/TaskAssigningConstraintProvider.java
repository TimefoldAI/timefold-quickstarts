package org.acme.taskassigning.solver;

import ai.timefold.solver.core.api.score.BendableScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.uni.UniConstraintStream;
import ai.timefold.solver.service.definition.api.description.ConstraintInfo;

import org.acme.taskassigning.domain.Employee;
import org.acme.taskassigning.domain.Priority;
import org.acme.taskassigning.domain.Task;

public class TaskAssigningConstraintProvider implements ConstraintProvider {

    public static final String NO_MISSING_SKILLS = "No missing skills";
    public static final String MINIMIZE_UNASSIGNED_TASKS = "Minimize unassigned tasks";
    public static final String MINIMIZE_MAKESPAN = "Minimize makespan - latest ending employee first";
    public static final String CRITICAL_PRIORITY_TASK_END_TIME = "Critical priority task end time";
    public static final String MAJOR_PRIORITY_TASK_END_TIME = "Major priority task end time";
    public static final String MINOR_PRIORITY_TASK_END_TIME = "Minor priority task end time";

    private static final int BENDABLE_SCORE_HARD_LEVELS_SIZE = 1;
    private static final int BENDABLE_SCORE_SOFT_LEVELS_SIZE = 3;

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
                .asConstraint(new ConstraintInfo(NO_MISSING_SKILLS, NO_MISSING_SKILLS,
                        "A task must be assigned to an employee who has all the required skills.",
                        TaskAssigningConstraintGroup.SKILLS));
    }

    protected Constraint minimizeUnassignedTasks(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(Task.class)
                .filter(task -> task.getEmployee() == null)
                .penalize(BendableScore.ofSoft(BENDABLE_SCORE_HARD_LEVELS_SIZE, BENDABLE_SCORE_SOFT_LEVELS_SIZE, 0, 1))
                .asConstraint(new ConstraintInfo(MINIMIZE_UNASSIGNED_TASKS, MINIMIZE_UNASSIGNED_TASKS,
                        "Every task should ideally be assigned to an employee.",
                        TaskAssigningConstraintGroup.TASK_ASSIGNMENT));
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
                .asConstraint(new ConstraintInfo(MINIMIZE_MAKESPAN, MINIMIZE_MAKESPAN,
                        "Balance the workload by penalizing the squared end time of every employee.",
                        TaskAssigningConstraintGroup.MAKESPAN));
    }

    protected Constraint criticalPriorityTaskEndTime(ConstraintFactory constraintFactory) {
        return getTaskWithPriority(constraintFactory, Priority.CRITICAL)
                .penalize(BendableScore.ofSoft(BENDABLE_SCORE_HARD_LEVELS_SIZE, BENDABLE_SCORE_SOFT_LEVELS_SIZE, 2, 1),
                        task -> task.getEndTime() * 4)
                .asConstraint(new ConstraintInfo(CRITICAL_PRIORITY_TASK_END_TIME, CRITICAL_PRIORITY_TASK_END_TIME,
                        "Finish critical priority tasks as early as possible.",
                        TaskAssigningConstraintGroup.PRIORITY));
    }

    protected Constraint majorPriorityTaskEndTime(ConstraintFactory constraintFactory) {
        return getTaskWithPriority(constraintFactory, Priority.MAJOR)
                .penalize(BendableScore.ofSoft(BENDABLE_SCORE_HARD_LEVELS_SIZE, BENDABLE_SCORE_SOFT_LEVELS_SIZE, 2, 1),
                        task -> task.getEndTime() * 2)
                .asConstraint(new ConstraintInfo(MAJOR_PRIORITY_TASK_END_TIME, MAJOR_PRIORITY_TASK_END_TIME,
                        "Finish major priority tasks as early as possible.",
                        TaskAssigningConstraintGroup.PRIORITY));
    }

    protected Constraint minorPriorityTaskEndTime(ConstraintFactory constraintFactory) {
        return getTaskWithPriority(constraintFactory, Priority.MINOR)
                .penalize(BendableScore.ofSoft(BENDABLE_SCORE_HARD_LEVELS_SIZE, BENDABLE_SCORE_SOFT_LEVELS_SIZE, 2, 1),
                        Task::getEndTime)
                .asConstraint(new ConstraintInfo(MINOR_PRIORITY_TASK_END_TIME, MINOR_PRIORITY_TASK_END_TIME,
                        "Finish minor priority tasks as early as possible.",
                        TaskAssigningConstraintGroup.PRIORITY));
    }
}
