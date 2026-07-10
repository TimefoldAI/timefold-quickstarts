package org.acme.taskassigning.solver;

import ai.timefold.solver.core.api.score.BendableScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.uni.UniConstraintStream;

import org.acme.taskassigning.domain.Employee;
import org.acme.taskassigning.domain.Priority;
import org.acme.taskassigning.domain.Task;

public class TaskAssigningConstraintProvider implements ConstraintProvider {

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
                .asConstraint("No missing skills");
    }

    protected Constraint minimizeUnassignedTasks(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(Task.class)
                .filter(task -> task.getEmployee() == null)
                .penalize(BendableScore.ofSoft(BENDABLE_SCORE_HARD_LEVELS_SIZE, BENDABLE_SCORE_SOFT_LEVELS_SIZE, 0, 1))
                .asConstraint("Minimize unassigned tasks");
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
                .asConstraint("Minimize makespan - latest ending employee first");
    }

    protected Constraint criticalPriorityTaskEndTime(ConstraintFactory constraintFactory) {
        return getTaskWithPriority(constraintFactory, Priority.CRITICAL)
                .penalize(BendableScore.ofSoft(BENDABLE_SCORE_HARD_LEVELS_SIZE, BENDABLE_SCORE_SOFT_LEVELS_SIZE, 2, 1),
                        task -> task.getEndTime() * 4)
                .asConstraint("Critical priority task end time");
    }

    protected Constraint majorPriorityTaskEndTime(ConstraintFactory constraintFactory) {
        return getTaskWithPriority(constraintFactory, Priority.MAJOR)
                .penalize(BendableScore.ofSoft(BENDABLE_SCORE_HARD_LEVELS_SIZE, BENDABLE_SCORE_SOFT_LEVELS_SIZE, 2, 1),
                        task -> task.getEndTime() * 2)
                .asConstraint("Major priority task end time");
    }

    protected Constraint minorPriorityTaskEndTime(ConstraintFactory constraintFactory) {
        return getTaskWithPriority(constraintFactory, Priority.MINOR)
                .penalize(BendableScore.ofSoft(BENDABLE_SCORE_HARD_LEVELS_SIZE, BENDABLE_SCORE_SOFT_LEVELS_SIZE, 2, 1),
                        Task::getEndTime)
                .asConstraint("Minor priority task end time");
    }
}
