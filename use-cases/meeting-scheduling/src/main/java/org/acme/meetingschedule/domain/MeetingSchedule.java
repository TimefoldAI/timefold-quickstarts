package org.acme.meetingschedule.domain;

import java.util.List;
import java.util.Objects;

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

import org.acme.meetingschedule.dto.input.MeetingScheduleInputMetrics;
import org.acme.meetingschedule.dto.output.MeetingScheduleOutputMetrics;

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
            List<Attendance> attendances, List<MeetingAssignment> meetingAssignments) {
        this.people = people;
        this.timeGrains = timeGrains;
        this.rooms = rooms;
        this.meetings = meetings;
        this.attendances = attendances;
        this.meetingAssignments = meetingAssignments;
    }

    public List<Person> getPeople() {
        return people;
    }

    public List<TimeGrain> getTimeGrains() {
        return timeGrains;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public List<Meeting> getMeetings() {
        return meetings;
    }

    public List<Attendance> getAttendances() {
        return attendances;
    }

    public List<MeetingAssignment> getMeetingAssignments() {
        return meetingAssignments;
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
        return new MeetingScheduleInputMetrics(meetings.size(), people.size(), rooms.size(), timeGrains.size());
    }

    private static boolean isAssigned(MeetingAssignment assignment) {
        return assignment.getStartingTimeGrain() != null && assignment.getRoom() != null;
    }

    @Override
    public MeetingScheduleOutputMetrics getOutputMetrics() {
        int assignedMeetings = (int) meetingAssignments.stream().filter(MeetingSchedule::isAssigned).count();
        int unassignedMeetings = meetingAssignments.size() - assignedMeetings;
        int usedRooms = (int) meetingAssignments.stream()
                .map(MeetingAssignment::getRoom)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        int occupiedMinutes = meetingAssignments.stream()
                .filter(MeetingSchedule::isAssigned)
                .mapToInt(MeetingAssignment::getDurationInMinutes)
                .sum();
        return new MeetingScheduleOutputMetrics(assignedMeetings, unassignedMeetings, usedRooms, occupiedMinutes);
    }
}
