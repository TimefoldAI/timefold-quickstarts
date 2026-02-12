package org.acme.meetingschedule.domain;

import java.util.List;
import java.util.stream.Stream;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.core.api.solver.SolverStatus;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@PlanningSolution
public class MeetingSchedule {

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
    @JsonIgnore
    @ProblemFactCollectionProperty
    private List<Attendance> attendances;
    @PlanningEntityCollectionProperty
    private List<MeetingAssignment> meetingAssignments;

    @PlanningScore
    private HardMediumSoftScore score;

    private SolverStatus solverStatus;

    public MeetingSchedule() {
    }

    @JsonCreator
    public MeetingSchedule(@JsonProperty("people") List<Person> people, @JsonProperty("timeGrains") List<TimeGrain> timeGrains,
            @JsonProperty("rooms") List<Room> rooms, @JsonProperty("meetings") List<Meeting> meetings,
            @JsonProperty("meetingAssignments") List<MeetingAssignment> meetingAssignments) {
        this.people = people;
        this.timeGrains = timeGrains;
        this.rooms = rooms;
        this.meetings = meetings;
        this.meetingAssignments = meetingAssignments;
        this.attendances = Stream.concat(
                this.meetings.stream().flatMap(m -> m.getRequiredAttendances().stream()),
                this.meetings.stream().flatMap(m -> m.getPreferredAttendances().stream()))
                .toList();
    }

    public MeetingSchedule(HardMediumSoftScore score, SolverStatus solverStatus) {
        this.score = score;
        this.solverStatus = solverStatus;
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

    public HardMediumSoftScore getScore() {
        return score;
    }

    public void setScore(HardMediumSoftScore score) {
        this.score = score;
    }

    public SolverStatus getSolverStatus() {
        return solverStatus;
    }

    public void setSolverStatus(SolverStatus solverStatus) {
        this.solverStatus = solverStatus;
    }
}
