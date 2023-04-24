package org.acme.foodpackaging.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import org.acme.foodpackaging.domain.PackagingSchedule;
import org.acme.foodpackaging.persistence.PackagingScheduleRepository;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;

@Path("schedule")
public class PackagingScheduleResource {

    public static final Long SINGLETON_SOLUTION_ID = 1L;

    @Inject
    PackagingScheduleRepository repository;

    @Inject
    SolverManager<PackagingSchedule, Long> solverManager;

    @GET
    public PackagingSchedule get() {
        // Get the solver status before loading the solution
        // to avoid the race condition that the solver terminates between them
        SolverStatus solverStatus = solverManager.getSolverStatus(SINGLETON_SOLUTION_ID);
        PackagingSchedule schedule = repository.read();
        schedule.setSolverStatus(solverStatus);
        return schedule;
    }

    @POST
    @Path("solve")
    public void solve() {
        solverManager.solveAndListen(SINGLETON_SOLUTION_ID,
                id -> repository.read(),
                schedule -> repository.write(schedule));
    }

    @POST
    @Path("stopSolving")
    public void stopSolving() {
        solverManager.terminateEarly(SINGLETON_SOLUTION_ID);
    }

}
