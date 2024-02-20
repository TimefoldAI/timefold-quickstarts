package org.acme.vaccinationscheduler.domain;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "name")
public class VaccineType {

    @PlanningId
    private String name;

    // Inclusive: a 55 year old can get a minimumAge=55 vaccine
    private Integer minimumAge;
    // Inclusive: a 55 year old can get a maximumAge=55 vaccine
    private Integer maximumAge;

    // No-arg constructor required for Jackson
    public VaccineType() {}

    public VaccineType(String name) {
        this(name, null, null);
    }

    public VaccineType(String name, Integer minimumAge, Integer maximumAge) {
        this.name = name;
        this.minimumAge = minimumAge;
        this.maximumAge = maximumAge;
        if (minimumAge != null && maximumAge != null && minimumAge > maximumAge) {
            throw new IllegalArgumentException("The minimumAge (" + minimumAge
                    + ") cannot be higher than the maximumAge (" + maximumAge + ").");
        }
    }

    @Override
    public String toString() {
        return name;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public String getName() {
        return name;
    }

    public Integer getMinimumAge() {
        return minimumAge;
    }

    public Integer getMaximumAge() {
        return maximumAge;
    }

}
