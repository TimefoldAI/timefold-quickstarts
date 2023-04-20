package org.acme.orderpicking.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;

/**
 * Base class for implementing the CHAINED graph modelling strategy.
 * 
 * @see Trolley
 * @see TrolleyStep
 */
@PlanningEntity
public abstract class TrolleyOrTrolleyStep {

    public static final String PREVIOUS_ELEMENT = "previousElement";

    /**
     * Shadow variable: Is automatically set by the Solver and facilitates that all the elements in the chain, the
     * Trolley and the TrolleyStep, can have a reference to the next element in that chain.
     */
    @InverseRelationShadowVariable(sourceVariableName = PREVIOUS_ELEMENT)
    protected TrolleyStep nextElement;

    protected TrolleyOrTrolleyStep() {
        //marshalling constructor
    }

    public abstract WarehouseLocation getLocation();

    public TrolleyStep getNextElement() {
        return nextElement;
    }

    public void setNextElement(TrolleyStep nextElement) {
        this.nextElement = nextElement;
    }
}
