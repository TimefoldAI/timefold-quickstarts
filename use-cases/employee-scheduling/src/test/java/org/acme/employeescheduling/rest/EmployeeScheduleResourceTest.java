package org.acme.employeescheduling.rest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.inject.Inject;

import org.acme.employeescheduling.domain.EmployeeSchedule;
import org.acme.employeescheduling.domain.Shift;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import ai.timefold.solver.core.api.solver.SolverStatus;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class EmployeeScheduleResourceTest {

    @Inject
    EmployeeScheduleResource employeeScheduleResource;

    @Test
    @Timeout(600_000)
    public void solveDemoDataUntilFeasible() throws InterruptedException {
        employeeScheduleResource.solve();
        EmployeeSchedule employeeSchedule = employeeScheduleResource.getSchedule();
        while (employeeSchedule.getSolverStatus() != SolverStatus.NOT_SOLVING
                || !employeeSchedule.getScore().isFeasible()) {
            // Quick polling (not a Test Thread Sleep anti-pattern)
            // Test is still fast on fast machines and doesn't randomly fail on slow machines.
            Thread.sleep(20L);
            employeeSchedule = employeeScheduleResource.getSchedule();
        }
        assertFalse(employeeSchedule.getShiftList().isEmpty());
        for (Shift shift : employeeSchedule.getShiftList()) {
            assertNotNull(shift.getEmployee());
        }
        assertTrue(employeeSchedule.getScore().isFeasible());
    }
}
