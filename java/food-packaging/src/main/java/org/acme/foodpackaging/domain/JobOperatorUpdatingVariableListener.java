package org.acme.foodpackaging.domain;

import ai.timefold.solver.core.api.domain.variable.VariableListener;
import ai.timefold.solver.core.api.score.director.ScoreDirector;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public class JobOperatorUpdatingVariableListener implements VariableListener<PackagingSchedule, Job> {

    private static final String LINE_OPERATOR_FIELD = "lineOperator";

    @Override
    public void beforeVariableChanged(@NonNull ScoreDirector<PackagingSchedule> scoreDirector, @NonNull Job job) {
        // No need to do anything.
    }

    @Override
    public void afterVariableChanged(@NonNull ScoreDirector<PackagingSchedule> scoreDirector, @NonNull Job job) {
        var line = job.getLine();
        var operator = line != null ? line.getOperator() : null;
        var lineOperator = job.getLineOperator();
        if (line == null && lineOperator != null) {
            scoreDirector.beforeVariableChanged(job, LINE_OPERATOR_FIELD);
            job.setLineOperator(null);
            scoreDirector.afterVariableChanged(job, LINE_OPERATOR_FIELD);
        } else if (!Objects.equals(operator, lineOperator)) {
            scoreDirector.beforeVariableChanged(job, LINE_OPERATOR_FIELD);
            job.setLineOperator(operator);
            scoreDirector.afterVariableChanged(job, LINE_OPERATOR_FIELD);
        }
    }

    @Override
    public void beforeEntityAdded(@NonNull ScoreDirector<PackagingSchedule> scoreDirector, @NonNull Job job) {
        // No need to do anything.
    }

    @Override
    public void afterEntityAdded(@NonNull ScoreDirector<PackagingSchedule> scoreDirector, @NonNull Job job) {
        // No need to do anything.
    }

    @Override
    public void beforeEntityRemoved(@NonNull ScoreDirector<PackagingSchedule> scoreDirector, @NonNull Job job) {
        // No need to do anything.
    }

    @Override
    public void afterEntityRemoved(@NonNull ScoreDirector<PackagingSchedule> scoreDirector, @NonNull Job job) {
        // No need to do anything.
    }
}
