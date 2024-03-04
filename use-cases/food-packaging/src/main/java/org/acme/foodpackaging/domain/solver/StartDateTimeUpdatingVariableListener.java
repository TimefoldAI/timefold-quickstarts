package org.acme.foodpackaging.domain.solver;

import java.time.LocalDateTime;
import java.util.Objects;

import org.acme.foodpackaging.domain.Job;
import org.acme.foodpackaging.domain.Line;
import org.acme.foodpackaging.domain.PackagingSchedule;
import ai.timefold.solver.core.api.domain.variable.VariableListener;
import ai.timefold.solver.core.api.score.director.ScoreDirector;

public class StartDateTimeUpdatingVariableListener implements VariableListener<PackagingSchedule, Job> {

    @Override
    public void beforeEntityAdded(ScoreDirector<PackagingSchedule> scoreDirector, Job job) {
        // Do nothing
    }

    @Override
    public void afterEntityAdded(ScoreDirector<PackagingSchedule> scoreDirector, Job job) {
        updateStartDateTime(scoreDirector, job);
    }

    @Override
    public void beforeVariableChanged(ScoreDirector<PackagingSchedule> scoreDirector, Job job) {
        // Do nothing
    }

    @Override
    public void afterVariableChanged(ScoreDirector<PackagingSchedule> scoreDirector, Job job) {
        updateStartDateTime(scoreDirector, job);
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector<PackagingSchedule> scoreDirector, Job job) {
        // Do nothing
    }

    @Override
    public void afterEntityRemoved(ScoreDirector<PackagingSchedule> scoreDirector, Job job) {
        updateStartDateTime(scoreDirector, job);
    }

    private void updateStartDateTime(ScoreDirector<PackagingSchedule> scoreDirector, Job job) {
        Line line = job.getLine();
        if (line == null) {
            if (job.getStartCleaningDateTime() != null) {
                scoreDirector.beforeVariableChanged(job, "startCleaningDateTime");
                job.setStartCleaningDateTime(null);
                scoreDirector.afterVariableChanged(job, "startCleaningDateTime");
                scoreDirector.beforeVariableChanged(job, "startProductionDateTime");
                job.setStartProductionDateTime(null);
                scoreDirector.afterVariableChanged(job, "startProductionDateTime");
                scoreDirector.beforeVariableChanged(job, "endDateTime");
                job.setEndDateTime(null);
                scoreDirector.afterVariableChanged(job, "endDateTime");
            }
            return;
        }

        Job previousJob = job.getPreviousJob();
        LocalDateTime startCleaningDateTime;
        LocalDateTime startProductionDateTime;
        if (previousJob == null) {
            startCleaningDateTime = line.getStartDateTime();
            startProductionDateTime = line.getStartDateTime();
        } else {
            startCleaningDateTime = previousJob.getEndDateTime();
            startProductionDateTime = startCleaningDateTime == null ? null : startCleaningDateTime.plus(job.getProduct().getCleanupDuration(previousJob.getProduct()));
        }
        // An equal startCleaningDateTime does not guarantee an equal startProductionDateTime
        for (Job shadowJob = job;
                shadowJob != null && (!Objects.equals(shadowJob.getStartCleaningDateTime(), startCleaningDateTime)
                || !Objects.equals(shadowJob.getStartProductionDateTime(), startProductionDateTime));) {
            scoreDirector.beforeVariableChanged(shadowJob, "startCleaningDateTime");
            shadowJob.setStartCleaningDateTime(startCleaningDateTime);
            scoreDirector.afterVariableChanged(shadowJob, "startCleaningDateTime");
            scoreDirector.beforeVariableChanged(shadowJob, "startProductionDateTime");
            shadowJob.setStartProductionDateTime(startProductionDateTime);
            scoreDirector.afterVariableChanged(shadowJob, "startProductionDateTime");
            scoreDirector.beforeVariableChanged(shadowJob, "endDateTime");
            // TODO skip weekends and holidays according to WorkCalendar
            shadowJob.setEndDateTime(startProductionDateTime == null ? null : startProductionDateTime.plus(shadowJob.getDuration()));
            scoreDirector.afterVariableChanged(shadowJob, "endDateTime");
            LocalDateTime endDateTime = shadowJob.getEndDateTime();
            previousJob = shadowJob;
            shadowJob = shadowJob.getNextJob();
            if (shadowJob == null || endDateTime == null) {
                startCleaningDateTime = null;
                startProductionDateTime = null;
            } else {
                startCleaningDateTime = endDateTime;
                startProductionDateTime = startCleaningDateTime.plus(shadowJob.getProduct().getCleanupDuration(previousJob.getProduct()));
                if (startProductionDateTime.isBefore(shadowJob.getReadyDateTime())) {
                    startProductionDateTime = shadowJob.getReadyDateTime();
                }
            }
        }

    }

}
