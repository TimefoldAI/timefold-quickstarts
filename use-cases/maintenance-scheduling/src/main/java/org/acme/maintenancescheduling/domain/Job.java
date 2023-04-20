package org.acme.maintenancescheduling.domain;

import java.time.LocalDate;
import java.util.Set;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.acme.maintenancescheduling.solver.EndDateUpdatingVariableListener;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import ai.timefold.solver.core.api.domain.variable.ShadowVariable;

@PlanningEntity
@Entity
public class Job {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private int durationInDays;
    private LocalDate readyDate; // Inclusive
    private LocalDate dueDate; // Exclusive
    private LocalDate idealEndDate; // Exclusive

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> tagSet;

    @PlanningVariable
    @ManyToOne
    private Crew crew;
    // Follows the TimeGrain Design Pattern
    @PlanningVariable
    private LocalDate startDate; // Inclusive
    @ShadowVariable(variableListenerClass = EndDateUpdatingVariableListener.class, sourceVariableName = "startDate")
    private LocalDate endDate; // Exclusive

    // No-arg constructor required for Hibernate and Timefold
    public Job() {
    }

    public Job(String name, int durationInDays, LocalDate readyDate, LocalDate dueDate, LocalDate idealEndDate, Set<String> tagSet) {
        this.name = name;
        this.durationInDays = durationInDays;
        this.readyDate = readyDate;
        this.dueDate = dueDate;
        this.idealEndDate = idealEndDate;
        this.tagSet = tagSet;
    }

    public Job(Long id, String name, int durationInDays, LocalDate readyDate, LocalDate dueDate, LocalDate idealEndDate, Set<String> tagSet,
            Crew crew, LocalDate startDate) {
        this.id = id;
        this.name = name;
        this.durationInDays = durationInDays;
        this.readyDate = readyDate;
        this.dueDate = dueDate;
        this.idealEndDate = idealEndDate;
        this.tagSet = tagSet;
        this.crew = crew;
        this.startDate = startDate;
        this.endDate = EndDateUpdatingVariableListener.calculateEndDate(startDate, durationInDays);
    }

    @Override
    public String toString() {
        return name + "(" + id + ")";
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    @PlanningId
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getDurationInDays() {
        return durationInDays;
    }

    public LocalDate getReadyDate() {
        return readyDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getIdealEndDate() {
        return idealEndDate;
    }

    public Set<String> getTagSet() {
        return tagSet;
    }

    public Crew getCrew() {
        return crew;
    }

    public void setCrew(Crew crew) {
        this.crew = crew;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
