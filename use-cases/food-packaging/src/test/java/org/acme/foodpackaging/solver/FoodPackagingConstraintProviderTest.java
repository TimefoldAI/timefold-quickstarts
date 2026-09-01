package org.acme.foodpackaging.solver;

import static org.acme.foodpackaging.support.TestHelper.aJob;
import static org.acme.foodpackaging.support.TestHelper.aLine;
import static org.acme.foodpackaging.support.TestHelper.anOperator;
import static org.acme.foodpackaging.support.TestHelper.assignJobs;
import static org.acme.foodpackaging.support.TestHelper.at;
import static org.acme.foodpackaging.support.TestHelper.productsWithCleaningMatrix;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.score.stream.test.ConstraintVerifier;

import org.acme.foodpackaging.domain.Job;
import org.acme.foodpackaging.domain.Line;
import org.acme.foodpackaging.domain.PackagingSchedule;
import org.acme.foodpackaging.domain.Product;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class FoodPackagingConstraintProviderTest {

    private static final OffsetDateTime DAY_START_TIME = at(LocalDate.of(2021, 2, 1), 9);

    // The constraints under test only look at times that are already fixed below, so the cleaning duration
    // between two products never enters into it; a uniform matrix keeps the tests about the timing.
    private static final List<Product> PRODUCTS = productsWithCleaningMatrix(Duration.ofMinutes(30), "1", "2", "3");
    private static final Product PRODUCT = PRODUCTS.getFirst();

    private final ConstraintVerifier<FoodPackagingConstraintProvider, PackagingSchedule> constraintVerifier;

    @Inject
    public FoodPackagingConstraintProviderTest(
            ConstraintVerifier<FoodPackagingConstraintProvider, PackagingSchedule> constraintVerifier) {
        this.constraintVerifier = constraintVerifier;
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    @Test
    void maxEndDateTime() {
        Job unscheduledJob = aJob("1").product(PRODUCT).duration(Duration.ofMinutes(6000)).build();
        Job onTimeJob = aJob("2").product(PRODUCT).duration(Duration.ofMinutes(200))
                .maxEndTime(DAY_START_TIME.plusMinutes(200))
                .producedFrom(DAY_START_TIME, DAY_START_TIME)
                .build();
        Job lateJob = aJob("3").product(PRODUCT).duration(Duration.ofMinutes(150))
                .maxEndTime(DAY_START_TIME.plusMinutes(100))
                .producedFrom(DAY_START_TIME, DAY_START_TIME)
                .build();
        Line line = aLine("1").startDateTime(DAY_START_TIME).build();
        assignJobs(line, unscheduledJob, onTimeJob, lateJob);

        constraintVerifier.verifyThat(FoodPackagingConstraintProvider::maxEndDateTime)
                .given(unscheduledJob, onTimeJob, lateJob)
                .penalizesBy(50L);
    }

    @Test
    void operatorCleaningConflict() {
        Line line1 = aLine("1").operator(anOperator("A")).startDateTime(DAY_START_TIME).build();
        Line line2 = aLine("2").operator(anOperator("A")).startDateTime(DAY_START_TIME).build();
        Line line3 = aLine("3").operator(anOperator("B")).startDateTime(DAY_START_TIME).build();
        // Cleaned from minute 0 to 30, so it overlaps job2's cleaning (minute 10 to 50) for 20 minutes.
        Job job1 = aJob("1").product(PRODUCT).duration(Duration.ofMinutes(100)).minStartTime(DAY_START_TIME)
                .producedFrom(DAY_START_TIME, DAY_START_TIME.plusMinutes(30))
                .build();
        Job job2 = aJob("2").product(PRODUCT).duration(Duration.ofMinutes(200)).minStartTime(DAY_START_TIME)
                .producedFrom(DAY_START_TIME.plusMinutes(10), DAY_START_TIME.plusMinutes(50))
                .build();
        // Overlaps both of the above, but is cleaned by another operator.
        Job job3 = aJob("3").product(PRODUCT).duration(Duration.ofMinutes(300)).minStartTime(DAY_START_TIME)
                .producedFrom(DAY_START_TIME.plusMinutes(5), DAY_START_TIME.plusMinutes(60))
                .build();
        assignJobs(line1, job1);
        assignJobs(line2, job2);
        assignJobs(line3, job3);

        constraintVerifier.verifyThat(FoodPackagingConstraintProvider::operatorCleaningConflict)
                .given(job1, job2, job3)
                .penalizesBy(20L);
    }

    @Test
    void jobsOnLinesWithoutAnOperatorAreNoCleaningConflict() {
        Line line1 = aLine("1").startDateTime(DAY_START_TIME).build();
        Line line2 = aLine("2").startDateTime(DAY_START_TIME).build();
        Job job1 = aJob("1").product(PRODUCT).duration(Duration.ofMinutes(100))
                .producedFrom(DAY_START_TIME, DAY_START_TIME.plusMinutes(30))
                .build();
        Job job2 = aJob("2").product(PRODUCT).duration(Duration.ofMinutes(200))
                .producedFrom(DAY_START_TIME.plusMinutes(10), DAY_START_TIME.plusMinutes(50))
                .build();
        assignJobs(line1, job1);
        assignJobs(line2, job2);

        constraintVerifier.verifyThat(FoodPackagingConstraintProvider::operatorCleaningConflict)
                .given(job1, job2)
                .penalizesBy(0L);
    }

    // ************************************************************************
    // Medium constraints
    // ************************************************************************

    @Test
    void idealEndDateTime() {
        Job unscheduledJob = aJob("1").product(PRODUCT).duration(Duration.ofMinutes(6000)).build();
        Job onTimeJob = aJob("2").product(PRODUCT).duration(Duration.ofMinutes(200))
                .idealEndTime(DAY_START_TIME.plusMinutes(200))
                .producedFrom(DAY_START_TIME, DAY_START_TIME)
                .build();
        Job lateJob = aJob("3").product(PRODUCT).duration(Duration.ofMinutes(150))
                .idealEndTime(DAY_START_TIME.plusMinutes(100))
                .producedFrom(DAY_START_TIME, DAY_START_TIME)
                .build();
        Line line = aLine("1").startDateTime(DAY_START_TIME).build();
        assignJobs(line, unscheduledJob, onTimeJob, lateJob);

        constraintVerifier.verifyThat(FoodPackagingConstraintProvider::idealEndDateTime)
                .given(unscheduledJob, onTimeJob, lateJob)
                .penalizesBy(50L);
    }

    @Test
    void maximizeJobsAssigned() {
        long unassignedJobDurationMinutes = 200L;

        Job assignedJob = aJob("1").product(PRODUCT).duration(Duration.ofMinutes(6000)).build();
        Job unassignedJob = aJob("2").product(PRODUCT).duration(Duration.ofMinutes(unassignedJobDurationMinutes))
                .idealEndTime(DAY_START_TIME.plusMinutes(200))
                .producedFrom(DAY_START_TIME, DAY_START_TIME)
                .build();
        Line line = aLine("1").startDateTime(DAY_START_TIME).build();
        assignJobs(line, assignedJob);

        constraintVerifier.verifyThat(FoodPackagingConstraintProvider::maximizeJobsAssigned)
                .given(assignedJob, unassignedJob)
                .penalizesBy(unassignedJobDurationMinutes);
    }

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    @Test
    void minimizeMakespan() {
        Line line1 = aLine("1").startDateTime(DAY_START_TIME).build();
        Line line2 = aLine("2").startDateTime(DAY_START_TIME).build();
        Job unassignedJob = aJob("1").product(PRODUCT).duration(Duration.ofMinutes(6000)).build();
        Job lastJobOfLine1 = aJob("2").product(PRODUCT).duration(Duration.ofMinutes(100))
                .producedFrom(DAY_START_TIME, DAY_START_TIME)
                .build();
        Job firstJobOfLine2 = aJob("3").product(PRODUCT).duration(Duration.ofMinutes(200))
                .producedFrom(DAY_START_TIME, DAY_START_TIME)
                .build();
        Job lastJobOfLine2 = aJob("4").product(PRODUCT).duration(Duration.ofMinutes(1000))
                .producedFrom(DAY_START_TIME.plusMinutes(200), DAY_START_TIME.plusMinutes(250))
                .build();
        assignJobs(line1, lastJobOfLine1);
        assignJobs(line2, firstJobOfLine2, lastJobOfLine2);

        // Only the last job of each line is penalized, on the square of that line's makespan.
        constraintVerifier.verifyThat(FoodPackagingConstraintProvider::minimizeMakespan)
                .given(line1, line2, unassignedJob, lastJobOfLine1, firstJobOfLine2, lastJobOfLine2)
                .penalizesBy(100L * 100L + 1250L * 1250L);
    }
}
