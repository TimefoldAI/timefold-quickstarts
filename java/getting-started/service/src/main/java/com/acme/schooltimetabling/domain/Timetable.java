package com.acme.schooltimetabling.domain;

import java.util.List;
import java.util.Optional;

import ai.timefold.solver.service.definition.api.AbstractSimpleModel;
import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.HardMediumSoftScore;

@PlanningSolution
public class Timetable extends AbstractSimpleModel {

    @ValueRangeProvider
    @ProblemFactCollectionProperty
    private List<Timeslot> timeslots;
    @ValueRangeProvider
    @ProblemFactCollectionProperty
    private List<Room> rooms;
    @PlanningEntityCollectionProperty
    private List<Lesson> lessons;
    private ConstraintWeightOverrides<HardMediumSoftScore> constraintWeightOverrides;

    public Timetable() {
    }

    public Timetable(List<Timeslot> timeslots, List<Room> rooms, List<Lesson> lessons) {
        this.timeslots = timeslots;
        this.rooms = rooms;
        this.lessons = lessons;
    }

    public List<Timeslot> getTimeslots() {
        return timeslots;
    }

    public void setTimeslots(List<Timeslot> timeslots) {
        this.timeslots = timeslots;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }

    public List<Lesson> getLessons() {
        return lessons;
    }

    public void setLessons(List<Lesson> lessons) {
        this.lessons = lessons;
    }

    @Override
    public ConstraintWeightOverrides<HardMediumSoftScore> getConstraintWeightOverrides() {
        return Optional.ofNullable(constraintWeightOverrides).orElse(ConstraintWeightOverrides.none());
    } 

    public void setConstraintWeightOverrides(ConstraintWeightOverrides constraintWeightOverrides) {
        this.constraintWeightOverrides = constraintWeightOverrides;
    }
}
