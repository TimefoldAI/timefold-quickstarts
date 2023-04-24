package org.acme.schooltimetabling.solver;

import jakarta.inject.Inject;

import org.acme.schooltimetabling.rest.TimeTableResource;
import org.junit.jupiter.api.Test;
import ai.timefold.solver.benchmark.api.PlannerBenchmarkFactory;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class TimeTableBenchmarkTest {

    @Inject
    PlannerBenchmarkFactory benchmarkFactory;

    @Inject
    TimeTableResource timeTableResource;

    @Test
    public void benchmark() {
        benchmarkFactory.buildPlannerBenchmark(timeTableResource.getTimeTable())
                .benchmark();
    }
}
