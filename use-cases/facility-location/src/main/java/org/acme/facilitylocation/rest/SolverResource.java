package org.acme.facilitylocation.rest;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.SolverManager;

import org.acme.facilitylocation.domain.FacilityLocationProblem;
import org.acme.facilitylocation.persistence.FacilityLocationProblemRepository;

@Path("/flp")
public class SolverResource {

    private static final long PROBLEM_ID = 0L;

    private final AtomicReference<Throwable> solverError = new AtomicReference<>();

    private final FacilityLocationProblemRepository repository;
    private final SolverManager<FacilityLocationProblem, Long> solverManager;
    private final SolutionManager<FacilityLocationProblem, HardSoftLongScore> solutionManager;

    public SolverResource(FacilityLocationProblemRepository repository,
            SolverManager<FacilityLocationProblem, Long> solverManager,
            SolutionManager<FacilityLocationProblem, HardSoftLongScore> solutionManager) {
        this.repository = repository;
        this.solverManager = solverManager;
        this.solutionManager = solutionManager;
    }

    private Status statusFromSolution(FacilityLocationProblem solution) {
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
        return statusFromSolution(repository.solution().orElse(FacilityLocationProblem.empty()));
    }

    @POST
    @Path("solve")
    public void solve() {
        Optional<FacilityLocationProblem> maybeSolution = repository.solution();
        maybeSolution.ifPresent(facilityLocationProblem -> solverManager.solveBuilder()
                .withProblemId(PROBLEM_ID)
                .withProblemFinder(id -> facilityLocationProblem)
                .withBestSolutionConsumer(repository::update)
                .withExceptionHandler((problemId, throwable) -> solverError.set(throwable))
                .run());
    }

    @POST
    @Path("stopSolving")
    public void stopSolving() {
        solverManager.terminateEarly(PROBLEM_ID);
    }
}
