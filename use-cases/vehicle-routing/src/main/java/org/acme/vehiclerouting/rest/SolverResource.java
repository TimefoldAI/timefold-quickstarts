package org.acme.vehiclerouting.rest;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.SolverManager;

import org.acme.vehiclerouting.domain.VehicleRoutingSolution;

//@Path("/vrp")
//@Produces(MediaType.APPLICATION_JSON)
//@Consumes(MediaType.APPLICATION_JSON)
public class SolverResource {

    private static final long PROBLEM_ID = 0L;

    private final AtomicReference<Throwable> solverError = new AtomicReference<>();

   // private final VehicleRoutingSolutionRepository repository;
    private final SolverManager<VehicleRoutingSolution, Long> solverManager;
    private final SolutionManager<VehicleRoutingSolution, HardSoftLongScore> solutionManager;

    public SolverResource(//VehicleRoutingSolutionRepository repository,
            SolverManager<VehicleRoutingSolution, Long> solverManager,
            SolutionManager<VehicleRoutingSolution, HardSoftLongScore> solutionManager) {
    //    this.repository = repository;
        this.solverManager = solverManager;
        this.solutionManager = solutionManager;
    }

    private Status statusFromSolution(VehicleRoutingSolution solution) {
        return new Status(solution,
                solutionManager.explain(solution).getSummary(),
                solverManager.getSolverStatus(PROBLEM_ID));
    }

    @GET
    @Path("status")
    public Status status() {
        Optional.ofNullable(solverError.getAndSet(null)).ifPresent(throwable -> {
            throw new RuntimeException("Solver failed", throwable);
        });

        Optional<VehicleRoutingSolution> s1 = null;//repository.solution();

        VehicleRoutingSolution s = s1.get();
        return statusFromSolution(s);
    }

    @POST
    @Path("solve")
    public void solve() {
        Optional<VehicleRoutingSolution> maybeSolution = Optional.empty();//repository.solution();
        maybeSolution.ifPresent(
                vehicleRoutingSolution -> solverManager.solveAndListen(PROBLEM_ID, id -> vehicleRoutingSolution,
                        vehicleRoutingSolution1 -> {}, (problemId, throwable) -> solverError.set(throwable)));
    }

    @POST
    @Path("stopSolving")
    public void stopSolving() {
        solverManager.terminateEarly(PROBLEM_ID);
    }
}
