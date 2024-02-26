package org.acme.schooltimetabling.domain;

import java.time.LocalDate; //added
import java.time.DayOfWeek;
import java.time.LocalTime;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = Timeslot.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Timeslot {

    @PlanningId
    private Long id;

    private LocalDate localDate; //added
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;

    // No-arg constructor required for Hibernate
    public Timeslot() {
    }

   /*  public Timeslot(long id, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.id = id;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime; 
    } */

    public Timeslot(long id, LocalDate localDate, LocalTime startTime, LocalTime endTime) {
        this.id = id;
        this.localDate = localDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Timeslot(long id, LocalDate localDate, LocalTime startTime) {
        this(id, localDate, startTime, startTime.plusMinutes(50));
    }

    @Override
    public String toString() {
        return dayOfWeek + " " + startTime;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public Long getId() {
        return id;
    }

    /* public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    } */

    public LocalDate getLocalDate() {
        return localDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }
}
