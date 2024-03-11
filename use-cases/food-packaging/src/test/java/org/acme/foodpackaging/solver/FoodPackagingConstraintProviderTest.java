package org.acme.foodpackaging.solver;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

import jakarta.inject.Inject;

import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;

import org.acme.foodpackaging.domain.Job;
import org.acme.foodpackaging.domain.Line;
import org.acme.foodpackaging.domain.PackagingSchedule;
import org.acme.foodpackaging.domain.Product;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class FoodPackagingConstraintProviderTest {

    private static final LocalDate DAY = LocalDate.of(2021, 2, 1);
    private static final LocalDateTime DAY_START_TIME = DAY.atTime(LocalTime.of(9, 0));
    private static final Product PRODUCT_A_SMALL = new Product("1", "Product A small");
    private static final Product PRODUCT_A_LARGE = new Product("2", "Product A large");
    private static final Product PRODUCT_B = new Product("3", "Product B");

    static {
        PRODUCT_A_SMALL.setCleaningDurations(Map.of(
                PRODUCT_A_LARGE, Duration.ofMinutes(5),
                PRODUCT_B, Duration.ofMinutes(60)));
        PRODUCT_A_LARGE.setCleaningDurations(Map.of(
                PRODUCT_A_SMALL, Duration.ofMinutes(0),
                PRODUCT_B, Duration.ofMinutes(60)));
        PRODUCT_B.setCleaningDurations(Map.of(
                PRODUCT_A_SMALL, Duration.ofMinutes(40),
                PRODUCT_A_LARGE, Duration.ofMinutes(40)));
    }

    @Inject
    ConstraintVerifier<FoodPackagingConstraintProvider, PackagingSchedule> constraintVerifier;

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    @Test
    void dueDateTime() {
        Job job1 = new Job("1", "job1", PRODUCT_A_SMALL, Duration.ofMinutes(6000), null, null, null, 1, false);
        Job job2 = new Job("2", "job2", PRODUCT_A_SMALL, Duration.ofMinutes(200), null, null, DAY_START_TIME.plusMinutes(200), 1, false,
                DAY_START_TIME, DAY_START_TIME);
        Job job3 = new Job("3", "job3", PRODUCT_A_SMALL, Duration.ofMinutes(150), null, null, DAY_START_TIME.plusMinutes(100), 1, false,
                DAY_START_TIME, DAY_START_TIME);
        Line line = new Line("1", "line1", "operator A", DAY_START_TIME);
        addJobs(line, job1, job2, job3);

        constraintVerifier.verifyThat(FoodPackagingConstraintProvider::dueDateTime)
                .given(job1, job2, job3)
                .penalizesBy(50L);
    }

    // ************************************************************************
    // Medium constraints
    // ************************************************************************

    @Test
    void idealEndDateTime() {
        Job job1 = new Job("1", "job1", PRODUCT_A_SMALL, Duration.ofMinutes(6000), null, null, null, 1, false);
        Job job2 = new Job("2", "job2", PRODUCT_A_SMALL, Duration.ofMinutes(200), null, DAY_START_TIME.plusMinutes(200), null, 1, false,
                DAY_START_TIME, DAY_START_TIME);
        Job job3 = new Job("3", "job3", PRODUCT_A_SMALL, Duration.ofMinutes(150), null, DAY_START_TIME.plusMinutes(100), null, 1, false,
                DAY_START_TIME, DAY_START_TIME);
        Line line = new Line("1", "line1", "operator A", DAY_START_TIME);
        addJobs(line, job1, job2, job3);

        constraintVerifier.verifyThat(FoodPackagingConstraintProvider::idealEndDateTime)
                .given(job1, job2, job3)
                .penalizesBy(50L);
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    @Test
    void operatorCleaningConflict() {
        Line line1 = new Line("1", "line1", "operator A", DAY_START_TIME);
        Line line2 = new Line("2", "line2", "operator A", DAY_START_TIME);
        Line line3 = new Line("3", "line3", "operator B", DAY_START_TIME);
        Job job1 = new Job("1", "job1", PRODUCT_A_SMALL, Duration.ofMinutes(100), DAY_START_TIME, null, null, 1, false,
                DAY_START_TIME, DAY_START_TIME.plusMinutes(30));
        Job job2 = new Job("2", "job2", PRODUCT_A_SMALL, Duration.ofMinutes(200), DAY_START_TIME, null, null, 1, false,
                DAY_START_TIME.plusMinutes(10), DAY_START_TIME.plusMinutes(50));
        Job job3 = new Job("3", "job3", PRODUCT_A_SMALL, Duration.ofMinutes(300), DAY_START_TIME, null, null, 1, false,
                DAY_START_TIME.plusMinutes(5), DAY_START_TIME.plusMinutes(60));
        addJobs(line1, job1);
        addJobs(line2, job2);
        addJobs(line3, job3);

        constraintVerifier.verifyThat(FoodPackagingConstraintProvider::operatorCleaningConflict)
                .given(job1, job2, job3)
                .penalizesBy(20L);
    }

    @Test
    void minimizeAndLoadBalanceMakeSpan() {
        Line line1 = new Line("1", "line1", null, DAY_START_TIME);
        Line line2 = new Line("2", "line2", null, DAY_START_TIME);
        Job job1 = new Job("1", "job1", PRODUCT_A_SMALL, Duration.ofMinutes(6000), null, null, null, 1, false);
        Job job2 = new Job("2", "job2", PRODUCT_A_SMALL, Duration.ofMinutes(100), null, null, null, 1, false,
                DAY_START_TIME, DAY_START_TIME);
        Job job3 = new Job("3", "job3", PRODUCT_A_SMALL, Duration.ofMinutes(200), null, null, null, 3, false,
                DAY_START_TIME, DAY_START_TIME);
        Job job4 = new Job("4", "job4", PRODUCT_A_SMALL, Duration.ofMinutes(1000), null, null, null, 3, false,
                DAY_START_TIME.plusMinutes(200), DAY_START_TIME.plusMinutes(250));
        addJobs(line1, job2);
        addJobs(line2, job3, job4);

        constraintVerifier.verifyThat(FoodPackagingConstraintProvider::minimizeAndLoadBalanceMakeSpan)
                .given(line1, line2, job1, job2, job3, job4)
                .penalizesBy(100L * 100L + 1250L * 1250L);
    }

    @Test @Disabled("The constraint is currently commented out.")
    void minimizeCleaningDuration() {
        Job job1 = new Job("1", "job1", PRODUCT_A_SMALL, Duration.ofMinutes(6000), DAY_START_TIME, null, null, 1, false);
        Job job2 = new Job("2", "job2", PRODUCT_A_SMALL, Duration.ofMinutes(200), DAY_START_TIME, null, null, 1, false,
                DAY_START_TIME, DAY_START_TIME);
        Job job3 = new Job("3", "job3", PRODUCT_A_SMALL, Duration.ofMinutes(150), DAY_START_TIME, null, null, 1, false,
                DAY_START_TIME.plusMinutes(30), DAY_START_TIME.plusMinutes(40));

        constraintVerifier.verifyThat(FoodPackagingConstraintProvider::minimizeCleaningDuration)
                .given(job1, job2, job3)
                .penalizesBy(10L);
    }

    // ************************************************************************
    // Helper methods
    // ************************************************************************

    private static void addJobs(Line line, Job... jobs) {
        for (int i = 0; i < jobs.length; i++) {
            Job job = jobs[i];
            job.setLine(line);
            line.getJobs().add(job);
            if (i > 0) {
                job.setPreviousJob(jobs[i - 1]);
            }
            if (i < jobs.length - 1) {
                job.setNextJob(jobs[i + 1]);
            }
        }
    }

}
