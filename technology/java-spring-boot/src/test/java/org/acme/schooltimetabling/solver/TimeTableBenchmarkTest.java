package org.acme.schooltimetabling.solver;

import org.acme.schooltimetabling.rest.TimeTableController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import ai.timefold.solver.benchmark.api.PlannerBenchmarkFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TimeTableBenchmarkTest {

    @Autowired
    private TimeTableController timeTableController;

    @Autowired
    private PlannerBenchmarkFactory benchmarkFactory;

    @Test
    @Timeout(600_000)
    public void benchmark() {
        benchmarkFactory.buildPlannerBenchmark(timeTableController.getTimeTable()).benchmark();
    }
}
