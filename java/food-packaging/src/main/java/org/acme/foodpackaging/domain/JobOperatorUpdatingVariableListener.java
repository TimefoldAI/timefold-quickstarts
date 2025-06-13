package org.acme.foodpackaging.domain;

import ai.timefold.solver.core.api.domain.variable.VariableListener;
import ai.timefold.solver.core.api.score.director.ScoreDirector;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public class JobOperatorUpdatingVariableListener implements VariableListener<PackagingSchedule, Job> {

    private static final String LINE_OPERATOR_FIELD = "lineOperator";

    @Override
    public void beforeVariableChanged(@NonNull ScoreDirector<PackagingSchedule> scoreDirector, @NonNull Job job) {
        // Empty method
    }

    @Override
    public void afterVariableChanged(@NonNull ScoreDirector<PackagingSchedule> scoreDirector, @NonNull Job job) {
        if (job.getLine() == null && job.getLineOperator() != null) {
            scoreDirector.beforeVariableChanged(job, LINE_OPERATOR_FIELD);
            job.setLineOperator(null);
            scoreDirector.afterVariableChanged(job, LINE_OPERATOR_FIELD);
        } else if (!Objects.equals(job.getLineOperator(), job.getLine().getOperator())) {
            scoreDirector.beforeVariableChanged(job, LINE_OPERATOR_FIELD);
            job.setLineOperator(job.getLine().getOperator());
            scoreDirector.afterVariableChanged(job, LINE_OPERATOR_FIELD);
        }
    }

    @Override
    public void beforeEntityAdded(@NonNull ScoreDirector<PackagingSchedule> scoreDirector, @NonNull Job job) {
        // Empty method
    }

    @Override
    public void afterEntityAdded(@NonNull ScoreDirector<PackagingSchedule> scoreDirector, @NonNull Job job) {
        // Empty method
    }

    @Override
    public void beforeEntityRemoved(@NonNull ScoreDirector<PackagingSchedule> scoreDirector, @NonNull Job job) {
        // Empty method
    }

    @Override
    public void afterEntityRemoved(@NonNull ScoreDirector<PackagingSchedule> scoreDirector, @NonNull Job job) {
        // Empty method
    }
}
