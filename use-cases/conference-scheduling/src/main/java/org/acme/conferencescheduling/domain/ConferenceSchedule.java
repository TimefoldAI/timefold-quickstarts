package org.acme.conferencescheduling.domain;

import java.util.Set;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.ProblemFactProperty;
import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.service.definition.api.SolverModel;
import ai.timefold.solver.service.definition.api.metrics.InputMetricsAware;
import ai.timefold.solver.service.definition.api.metrics.OutputMetricsAware;

import org.acme.conferencescheduling.dto.ConferenceScheduleInputMetrics;
import org.acme.conferencescheduling.dto.ConferenceScheduleOutputMetrics;

@PlanningSolution
public class ConferenceSchedule implements SolverModel<HardMediumSoftScore>,
        InputMetricsAware<ConferenceScheduleInputMetrics>, OutputMetricsAware<ConferenceScheduleOutputMetrics> {

    private String name;

    @ProblemFactProperty
    private ConferenceConstraintProperties constraintProperties;

    @ProblemFactCollectionProperty
    private Set<TalkType> talkTypes;

    @ProblemFactCollectionProperty
    private Set<Timeslot> timeslots;

    @ProblemFactCollectionProperty
    private Set<Room> rooms;

    @ProblemFactCollectionProperty
    private Set<Speaker> speakers;

    @PlanningEntityCollectionProperty
    private Set<Talk> talks;

    @PlanningScore
    private HardMediumSoftScore score;

    private ConstraintWeightOverrides<HardMediumSoftScore> constraintWeightOverrides = ConstraintWeightOverrides.none();

    public ConferenceSchedule() {
    }

    public ConferenceSchedule(String name, Set<TalkType> talkTypes, Set<Timeslot> timeslots, Set<Room> rooms,
            Set<Speaker> speakers, Set<Talk> talks) {
        this.name = name;
        this.talkTypes = talkTypes;
        this.timeslots = timeslots;
        this.rooms = rooms;
        this.speakers = speakers;
        this.talks = talks;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ConferenceConstraintProperties getConstraintProperties() {
        return constraintProperties;
    }

    public void setConstraintProperties(ConferenceConstraintProperties constraintProperties) {
        this.constraintProperties = constraintProperties;
    }

    public Set<TalkType> getTalkTypes() {
        return talkTypes;
    }

    public void setTalkTypes(Set<TalkType> talkTypes) {
        this.talkTypes = talkTypes;
    }

    public Set<Timeslot> getTimeslots() {
        return timeslots;
    }

    public void setTimeslots(Set<Timeslot> timeslots) {
        this.timeslots = timeslots;
    }

    public Set<Room> getRooms() {
        return rooms;
    }

    public void setRooms(Set<Room> rooms) {
        this.rooms = rooms;
    }

    public Set<Speaker> getSpeakers() {
        return speakers;
    }

    public void setSpeakers(Set<Speaker> speakers) {
        this.speakers = speakers;
    }

    public Set<Talk> getTalks() {
        return talks;
    }

    public void setTalks(Set<Talk> talks) {
        this.talks = talks;
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
    public ConferenceScheduleInputMetrics getInputMetrics() {
        return new ConferenceScheduleInputMetrics(talks.size(), speakers.size(), rooms.size(), timeslots.size(),
                talkTypes.size());
    }

    @Override
    public ConferenceScheduleOutputMetrics getOutputMetrics() {
        int scheduledTalks = (int) talks.stream().filter(Talk::isScheduled).count();
        int unscheduledTalks = talks.size() - scheduledTalks;
        int usedRooms = (int) talks.stream().filter(Talk::isScheduled).map(Talk::getRoom).distinct().count();
        int usedTimeslots = (int) talks.stream().filter(Talk::isScheduled).map(Talk::getTimeslot).distinct().count();
        return new ConferenceScheduleOutputMetrics(scheduledTalks, unscheduledTalks, usedRooms, usedTimeslots);
    }

    @Override
    public String toString() {
        return name;
    }
}
