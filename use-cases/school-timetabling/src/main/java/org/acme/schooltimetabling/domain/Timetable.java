package org.acme.schooltimetabling.domain;

import java.util.List;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.service.definition.api.SolverModel;
import ai.timefold.solver.service.definition.api.metrics.InputMetricsAware;
import ai.timefold.solver.service.definition.api.metrics.OutputMetricsAware;

import org.acme.schooltimetabling.dto.TimetableInputMetrics;
import org.acme.schooltimetabling.dto.TimetableOutputMetrics;

@PlanningSolution
public class Timetable implements SolverModel<HardMediumSoftScore>,
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
    private HardMediumSoftScore score;

    private ConstraintWeightOverrides<HardMediumSoftScore> constraintWeightOverrides = ConstraintWeightOverrides.none();

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
    public HardMediumSoftScore getScore() {
        return score;
    }

    public void setScore(HardMediumSoftScore score) {
        this.score = score;
    }

    @Override
    public ConstraintWeightOverrides<HardMediumSoftScore> getConstraintWeightOverrides() {
        return constraintWeightOverrides;
    }

    public void setConstraintWeightOverrides(ConstraintWeightOverrides<HardMediumSoftScore> constraintWeightOverrides) {
        this.constraintWeightOverrides = constraintWeightOverrides;
    }

    @Override
    public TimetableInputMetrics getInputMetrics() {
        long teachers = lessons.stream().map(Lesson::getTeacher).distinct().count();
        long studentGroups = lessons.stream().map(Lesson::getStudentGroup).distinct().count();
        return new TimetableInputMetrics(lessons.size(), timeslots.size(), rooms.size(), (int) teachers,
                (int) studentGroups);
    }

    @Override
    public TimetableOutputMetrics getOutputMetrics() {
        int scheduledLessons = (int) lessons.stream().filter(Lesson::isScheduled).count();
        int unscheduledLessons = lessons.size() - scheduledLessons;
        int usedRooms = (int) lessons.stream().filter(Lesson::isScheduled).map(Lesson::getRoom).distinct().count();
        int usedTimeslots =
                (int) lessons.stream().filter(Lesson::isScheduled).map(Lesson::getTimeslot).distinct().count();
        return new TimetableOutputMetrics(scheduledLessons, unscheduledLessons, usedRooms, usedTimeslots);
    }

    @Override
    public String toString() {
        return "Timetable{lessons: " + lessons.size() + ", score: " + score + '}';
    }
}
