package org.acme.schooltimetabling.domain;

import java.util.List;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.HardSoftScore;
import ai.timefold.solver.service.definition.api.SolverModel;
import ai.timefold.solver.service.definition.api.metrics.InputMetricsAware;
import ai.timefold.solver.service.definition.api.metrics.OutputMetricsAware;

import org.acme.schooltimetabling.dto.input.TimetableInputMetrics;
import org.acme.schooltimetabling.dto.output.TimetableOutputMetrics;

@PlanningSolution
public class Timetable implements SolverModel<HardSoftScore>,
        InputMetricsAware<TimetableInputMetrics>, OutputMetricsAware<TimetableOutputMetrics> {

    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<Timeslot> timeslots;
    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<Room> rooms;
    @PlanningEntityCollectionProperty
    private List<Lesson> lessons;

    @PlanningScore
    private HardSoftScore score;

    private ConstraintWeightOverrides<HardSoftScore> constraintWeightOverrides = ConstraintWeightOverrides.none();

    public Timetable() {
    }

    public Timetable(List<Timeslot> timeslots, List<Room> rooms, List<Lesson> lessons) {
        this.timeslots = timeslots;
        this.rooms = rooms;
        this.lessons = lessons;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public List<Timeslot> getTimeslots() {
        return timeslots;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public List<Lesson> getLessons() {
        return lessons;
    }

    @Override
    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }

    @Override
    public ConstraintWeightOverrides<HardSoftScore> getConstraintWeightOverrides() {
        return constraintWeightOverrides;
    }

    public void setConstraintWeightOverrides(ConstraintWeightOverrides<HardSoftScore> constraintWeightOverrides) {
        this.constraintWeightOverrides = constraintWeightOverrides;
    }

    @Override
    public TimetableInputMetrics getInputMetrics() {
        int teachers = (int) lessons.stream().map(Lesson::getTeacher).distinct().count();
        int studentGroups = (int) lessons.stream().map(Lesson::getStudentGroup).distinct().count();
        return new TimetableInputMetrics(lessons.size(), timeslots.size(), rooms.size(), teachers, studentGroups);
    }

    @Override
    public TimetableOutputMetrics getOutputMetrics() {
        int assignedLessons = (int) lessons.stream().filter(Lesson::isAssigned).count();
        return new TimetableOutputMetrics(assignedLessons, lessons.size() - assignedLessons);
    }
}
