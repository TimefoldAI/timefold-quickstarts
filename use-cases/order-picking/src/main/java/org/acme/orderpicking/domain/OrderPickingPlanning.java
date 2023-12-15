package org.acme.orderpicking.domain;

import java.util.HashMap;
import java.util.Map;

import ai.timefold.solver.core.api.solver.SolverStatus;

/**
 * Helper class for sending information to the UI.
 */
public class OrderPickingPlanning {

    private SolverStatus solverStatus;
    private OrderPickingSolution solution;
    private boolean solverWasNeverStarted;
    private Map<String, Integer> distanceToTravelByTrolley = new HashMap<>();

    public OrderPickingPlanning() {
        //marshalling constructor
    }

    public OrderPickingPlanning(SolverStatus solverStatus, OrderPickingSolution solution, boolean solverWasNeverStarted) {
        this.solverStatus = solverStatus;
        this.solution = solution;
        this.solverWasNeverStarted = solverWasNeverStarted;
        for (Trolley trolley : solution.getTrolleys()) {
            distanceToTravelByTrolley.put(trolley.getId(), Warehouse.calculateDistanceToTravel(trolley));
        }
    }

    public SolverStatus getSolverStatus() {
        return solverStatus;
    }

    public OrderPickingSolution getSolution() {
        return solution;
    }

    public boolean getSolverWasNeverStarted() {
        return solverWasNeverStarted;
    }

    public Map<String, Integer> getDistanceToTravelByTrolley() {
        return distanceToTravelByTrolley;
    }
}
