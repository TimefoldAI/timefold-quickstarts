package org.acme.orderpicking.rest;

import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;

import org.acme.orderpicking.domain.OrderPickingPlanning;
import org.acme.orderpicking.domain.OrderPickingSolution;
import org.acme.orderpicking.persistence.OrderPickingRepository;

@Path("orderPicking")
@ApplicationScoped
public class OrderPickingSolverResource {

    private static final String PROBLEM_ID = "1";
    private final AtomicBoolean solverWasNeverStarted = new AtomicBoolean(true);

    @Inject
    SolverManager<OrderPickingSolution, String> solverManager;

    @Inject
    OrderPickingRepository orderPickingRepository;

    @GET
    public OrderPickingPlanning getBestSolution() {
        OrderPickingSolution solution = orderPickingRepository.find();
        SolverStatus solverStatus = solverManager.getSolverStatus(PROBLEM_ID);
        return new OrderPickingPlanning(solverStatus, solution, solverWasNeverStarted.get());
    }

    @POST
    @Path("solve")
    public void solve() {
        solverWasNeverStarted.set(false);
        solverManager.solveBuilder()
                .withProblemId(PROBLEM_ID)
                .withProblemFinder((problemId) -> orderPickingRepository.find())
                .withBestSolutionConsumer(orderPickingRepository::save)
                .run();
    }

    @POST
    @Path("stopSolving")
    public void stopSolving() {
        solverManager.terminateEarly(PROBLEM_ID);
    }
}