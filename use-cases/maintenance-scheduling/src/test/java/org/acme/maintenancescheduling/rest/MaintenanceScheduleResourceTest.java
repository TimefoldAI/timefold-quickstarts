package org.acme.maintenancescheduling.rest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.inject.Inject;

import org.acme.maintenancescheduling.domain.Job;
import org.acme.maintenancescheduling.domain.MaintenanceSchedule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import ai.timefold.solver.core.api.solver.SolverStatus;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class MaintenanceScheduleResourceTest {

    @Inject
    MaintenanceScheduleResource maintenanceScheduleResource;

    @Test
    @Timeout(600_000)
    public void solveDemoDataUntilFeasible() throws InterruptedException {
        maintenanceScheduleResource.solve();
        MaintenanceSchedule maintenanceSchedule = maintenanceScheduleResource.getSchedule();
        while (maintenanceSchedule.getSolverStatus() != SolverStatus.NOT_SOLVING
                || !maintenanceSchedule.getScore().isFeasible()) {
            // Quick polling (not a Test Thread Sleep anti-pattern)
            // Test is still fast on fast machines and doesn't randomly fail on slow machines.
            Thread.sleep(20L);
            maintenanceSchedule = maintenanceScheduleResource.getSchedule();
        }
        assertFalse(maintenanceSchedule.getJobs().isEmpty());
        for (Job job : maintenanceSchedule.getJobs()) {
            assertNotNull(job.getCrew());
            assertNotNull(job.getStartDate());
        }
        assertTrue(maintenanceSchedule.getScore().isFeasible());
    }
}
