package org.acme.foodpackaging.domain;

import ai.timefold.solver.core.api.domain.variable.VariableListener;
import ai.timefold.solver.core.api.score.director.ScoreDirector;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public class LineOperatorUpdatingVariableListener implements VariableListener<PackagingSchedule, Line> {
    @Override
    public void beforeVariableChanged(@NonNull ScoreDirector<PackagingSchedule> scoreDirector, @NonNull Line line) {
        // No need to do anything.
    }

    @Override
    public void afterVariableChanged(@NonNull ScoreDirector<PackagingSchedule> scoreDirector, @NonNull Line line) {
        for (var job : line.getJobs()) {
            if (!Objects.equals(job.getLineOperator(), line.getOperator())) {
                scoreDirector.beforeVariableChanged(job, "lineOperator");
                job.setLineOperator(line.getOperator());
                scoreDirector.afterVariableChanged(job, "lineOperator");
            }
        }
    }

    @Override
    public void beforeEntityAdded(@NonNull ScoreDirector<PackagingSchedule> scoreDirector, @NonNull Line line) {
        // No need to do anything.
    }

    @Override
    public void afterEntityAdded(@NonNull ScoreDirector<PackagingSchedule> scoreDirector, @NonNull Line line) {
        // No need to do anything.
    }

    @Override
    public void beforeEntityRemoved(@NonNull ScoreDirector<PackagingSchedule> scoreDirector, @NonNull Line line) {
        // No need to do anything.
    }

    @Override
    public void afterEntityRemoved(@NonNull ScoreDirector<PackagingSchedule> scoreDirector, @NonNull Line line) {
        // No need to do anything.
    }
}
