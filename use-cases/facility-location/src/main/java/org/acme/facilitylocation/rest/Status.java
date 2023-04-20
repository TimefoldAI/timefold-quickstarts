package org.acme.facilitylocation.rest;

import org.acme.facilitylocation.domain.FacilityLocationProblem;
import ai.timefold.solver.core.api.solver.SolverStatus;

class Status {
    public final FacilityLocationProblem solution;
    public final String scoreExplanation;
    public final boolean isSolving;

    Status(FacilityLocationProblem solution, String scoreExplanation, SolverStatus solverStatus) {
        this.solution = solution;
        this.scoreExplanation = scoreExplanation;
        this.isSolving = solverStatus != SolverStatus.NOT_SOLVING;
    }
}
