package org.acme.meetingschedule.domain;

import java.util.List;
import java.util.stream.Stream;

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

import org.acme.meetingschedule.dto.MeetingScheduleInputMetrics;
import org.acme.meetingschedule.dto.MeetingScheduleOutputMetrics;

@PlanningSolution
public class MeetingSchedule implements SolverModel<HardMediumSoftScore>,
        InputMetricsAware<MeetingScheduleInputMetrics>, OutputMetricsAware<MeetingScheduleOutputMetrics> {

    @ProblemFactCollectionProperty
    private List<Person> people;
    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<TimeGrain> timeGrains;
    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<Room> rooms;
    @ProblemFactCollectionProperty
    private List<Meeting> meetings;
    @ProblemFactCollectionProperty
    private List<Attendance> attendances;
    @PlanningEntityCollectionProperty
    private List<MeetingAssignment> meetingAssignments;

    @PlanningScore
    private HardMediumSoftScore score;

    private ConstraintWeightOverrides<HardMediumSoftScore> constraintWeightOverrides = ConstraintWeightOverrides.none();

    public MeetingSchedule() {
    }

    public MeetingSchedule(List<Person> people, List<TimeGrain> timeGrains, List<Room> rooms, List<Meeting> meetings,
            List<MeetingAssignment> meetingAssignments) {
        this.people = people;
        this.timeGrains = timeGrains;
        this.rooms = rooms;
        this.meetings = meetings;
        this.meetingAssignments = meetingAssignments;
        this.attendances = Stream.concat(
                meetings.stream().flatMap(m -> m.getRequiredAttendances().stream()),
                meetings.stream().flatMap(m -> m.getPreferredAttendances().stream()))
                .toList();
    }

    public List<Meeting> getMeetings() {
        return meetings;
    }

    public void setMeetings(List<Meeting> meetings) {
        this.meetings = meetings;
    }

    public List<TimeGrain> getTimeGrains() {
        return timeGrains;
    }

    public void setTimeGrains(List<TimeGrain> timeGrains) {
        this.timeGrains = timeGrains;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }

    public List<Person> getPeople() {
        return people;
    }

    public void setPeople(List<Person> people) {
        this.people = people;
    }

    public List<Attendance> getAttendances() {
        return attendances;
    }

    public void setAttendances(List<Attendance> attendances) {
        this.attendances = attendances;
    }

    public List<MeetingAssignment> getMeetingAssignments() {
        return meetingAssignments;
    }

    public void setMeetingAssignments(List<MeetingAssignment> meetingAssignments) {
        this.meetingAssignments = meetingAssignments;
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
    public MeetingScheduleInputMetrics getInputMetrics() {
        return new MeetingScheduleInputMetrics(meetings.size(), meetingAssignments.size(), people.size(), rooms.size(),
                timeGrains.size());
    }

    @Override
    public MeetingScheduleOutputMetrics getOutputMetrics() {
        int assigned = (int) meetingAssignments.stream()
                .filter(assignment -> assignment.getStartingTimeGrain() != null && assignment.getRoom() != null)
                .count();
        int unassigned = meetingAssignments.size() - assigned;
        int usedRooms = (int) meetingAssignments.stream()
                .filter(assignment -> assignment.getRoom() != null)
                .map(assignment -> assignment.getRoom().getId())
                .distinct()
                .count();
        return new MeetingScheduleOutputMetrics(assigned, unassigned, usedRooms);
    }

    @Override
    public String toString() {
        return "MeetingSchedule{meetingAssignments: " + meetingAssignments.size() + ", score: " + score + '}';
    }
}
