package org.acme.maintenancescheduling.solver;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.SequencedSet;

import ai.timefold.solver.core.api.score.stream.test.ConstraintVerifier;

import org.acme.maintenancescheduling.domain.Crew;
import org.acme.maintenancescheduling.domain.Job;
import org.acme.maintenancescheduling.domain.MaintenanceSchedule;
import org.junit.jupiter.api.Test;

class MaintenanceScheduleConstraintProviderTest {

    private static final Crew CREW1 = new Crew("1", "Crew1");
    private static final Crew CREW2 = new Crew("2", "Crew2");
    private static final LocalDate DAY_1 = LocalDate.of(2024, 1, 1);
    private static final LocalDate DAY_3 = LocalDate.of(2024, 1, 3);
    private static final LocalDate DAY_5 = LocalDate.of(2024, 1, 5);
    private static final LocalDate DAY_10 = LocalDate.of(2024, 1, 10);

    private final ConstraintVerifier<MaintenanceScheduleConstraintProvider, MaintenanceSchedule> constraintVerifier =
            ConstraintVerifier.build(new MaintenanceScheduleConstraintProvider(), MaintenanceSchedule.class, Job.class);

    private static SequencedSet<String> tags(String... values) {
        SequencedSet<String> set = new LinkedHashSet<>();
        for (String value : values) {
            set.add(value);
        }
        return set;
    }

    private static Job job(String id, Crew crew, LocalDate minStartDate, LocalDate maxEndDate, LocalDate idealEndDate,
            LocalDate startDate, int durationInDays, SequencedSet<String> tags) {
        return new Job(id, "Job " + id, durationInDays, minStartDate, maxEndDate, idealEndDate, tags, crew, startDate);
    }

    @Test
    void crewConflict() {
        // Three overlapping jobs for the same crew so the penalty weigher exercises both
        // branches of its start/end date ternaries.
        Job job1 = job("1", CREW1, DAY_1, DAY_10, DAY_5, DAY_5, 1, tags("A"));
        Job job2 = job("2", CREW1, DAY_1, DAY_10, DAY_5, DAY_1, 9, tags("B"));
        Job job3 = job("3", CREW1, DAY_1, DAY_10, DAY_5, DAY_1, 9, tags("C"));
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::crewConflict)
                .given(job1, job2, job3)
                .penalizesByMoreThan(0);
    }

    @Test
    void minStartDate() {
        Job job = job("1", CREW1, DAY_5, DAY_10, DAY_10, DAY_1, 2, tags("A"));
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::minStartDate)
                .given(job)
                .penalizesByMoreThan(0);
    }

    @Test
    void maxEndDate() {
        Job job = job("1", CREW1, DAY_1, DAY_3, DAY_3, DAY_1, 10, tags("A"));
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::maxEndDate)
                .given(job)
                .penalizesByMoreThan(0);
    }

    @Test
    void beforeIdealEndDate() {
        Job job = job("1", CREW1, DAY_1, DAY_10, DAY_10, DAY_1, 2, tags("A"));
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::beforeIdealEndDate)
                .given(job)
                .penalizesByMoreThan(0);
    }

    @Test
    void afterIdealEndDate() {
        Job job = job("1", CREW1, DAY_1, DAY_10, DAY_1, DAY_5, 4, tags("A"));
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::afterIdealEndDate)
                .given(job)
                .penalizesByMoreThan(0);
    }

    @Test
    void tagConflict() {
        // Three overlapping jobs sharing a tag so the penalty weigher exercises both
        // branches of its start/end date ternaries.
        Job job1 = job("1", CREW1, DAY_1, DAY_10, DAY_10, DAY_5, 1, tags("Downtown"));
        Job job2 = job("2", CREW2, DAY_1, DAY_10, DAY_10, DAY_1, 9, tags("Downtown"));
        Job job3 = job("3", CREW2, DAY_1, DAY_10, DAY_10, DAY_1, 9, tags("Downtown"));
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::tagConflict)
                .given(job1, job2, job3)
                .penalizesByMoreThan(0);
    }
}
