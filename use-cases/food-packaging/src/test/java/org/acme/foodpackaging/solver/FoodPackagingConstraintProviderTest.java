package org.acme.foodpackaging.solver;

import java.time.Duration;
import java.time.LocalDateTime;

import ai.timefold.solver.core.api.score.stream.test.ConstraintVerifier;

import org.acme.foodpackaging.domain.Job;
import org.acme.foodpackaging.domain.Line;
import org.acme.foodpackaging.domain.Operator;
import org.acme.foodpackaging.domain.PackagingSchedule;
import org.acme.foodpackaging.domain.Product;
import org.junit.jupiter.api.Test;

class FoodPackagingConstraintProviderTest {

    private static final LocalDateTime BASE = LocalDateTime.of(2024, 1, 1, 8, 0);

    private final ConstraintVerifier<FoodPackagingConstraintProvider, PackagingSchedule> constraintVerifier =
            ConstraintVerifier.build(new FoodPackagingConstraintProvider(), PackagingSchedule.class,
                    Job.class, Line.class, Operator.class);

    private static Job job(String id, LocalDateTime idealEndTime, LocalDateTime maxEndTime, long durationMinutes) {
        return new Job(id, "Job " + id, new Product(id, "Product " + id), Duration.ofMinutes(durationMinutes),
                BASE, idealEndTime, maxEndTime, 1, false);
    }

    private static Line lineWith(Job... jobs) {
        Line line = new Line("L", "Line", BASE);
        for (Job job : jobs) {
            job.setLine(line);
        }
        return line;
    }

    @Test
    void maxEndDateTime() {
        Job job = job("0", BASE.plusDays(10), BASE.plusHours(2), 60);
        Line line = lineWith(job);
        job.setEndDateTime(BASE.plusHours(2).plusMinutes(30));

        constraintVerifier.verifyThat(FoodPackagingConstraintProvider::maxEndDateTime)
                .given(job, line)
                .penalizesBy(30);
    }

    @Test
    void operatorCleaningConflict() {
        Operator operator = new Operator("op");

        Job job1 = job("0", BASE.plusDays(10), BASE.plusDays(10), 60);
        Job job2 = job("1", BASE.plusDays(10), BASE.plusDays(10), 60);
        Line line = lineWith(job1, job2);
        job1.setLineOperator(operator);
        job1.setStartCleaningDateTime(BASE);
        job1.setStartProductionDateTime(BASE.plusMinutes(60));
        job2.setLineOperator(operator);
        job2.setStartCleaningDateTime(BASE.plusMinutes(30));
        job2.setStartProductionDateTime(BASE.plusMinutes(90));

        constraintVerifier.verifyThat(FoodPackagingConstraintProvider::operatorCleaningConflict)
                .given(job1, job2, line, operator)
                .penalizesBy(30);
    }

    @Test
    void idealEndDateTime() {
        Job job = job("0", BASE.plusHours(2), BASE.plusDays(10), 60);
        Line line = lineWith(job);
        job.setEndDateTime(BASE.plusHours(2).plusMinutes(45));

        constraintVerifier.verifyThat(FoodPackagingConstraintProvider::idealEndDateTime)
                .given(job, line)
                .penalizesBy(45);
    }

    @Test
    void maximizeJobsAssigned() {
        Job job = job("0", BASE.plusDays(10), BASE.plusDays(10), 60);

        constraintVerifier.verifyThat(FoodPackagingConstraintProvider::maximizeJobsAssigned)
                .given(job)
                .penalizesBy(60);
    }

    @Test
    void minimizeMakespan() {
        Line line = new Line("0", "Line 0", BASE);
        Job job = job("0", BASE.plusDays(10), BASE.plusDays(10), 60);
        job.setLine(line);
        job.setEndDateTime(BASE.plusMinutes(60));

        constraintVerifier.verifyThat(FoodPackagingConstraintProvider::minimizeMakespan)
                .given(job)
                .penalizesBy(60L * 60L);
    }
}
