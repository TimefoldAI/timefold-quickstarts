package org.acme.conferencescheduling.domain;

import java.util.List;

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

import org.acme.conferencescheduling.dto.input.ConferenceScheduleInputMetrics;
import org.acme.conferencescheduling.dto.output.ConferenceScheduleOutputMetrics;

@PlanningSolution
public class ConferenceSchedule implements SolverModel<HardMediumSoftScore>,
        InputMetricsAware<ConferenceScheduleInputMetrics>, OutputMetricsAware<ConferenceScheduleOutputMetrics> {

    private String name;

    @ProblemFactProperty
    private ConferenceConstraintProperties constraintProperties;

    @ProblemFactCollectionProperty
    private List<TalkType> talkTypes;

    @ProblemFactCollectionProperty
    private List<Timeslot> timeslots;

    @ProblemFactCollectionProperty
    private List<Room> rooms;

    @ProblemFactCollectionProperty
    private List<Speaker> speakers;

    @PlanningEntityCollectionProperty
    private List<Talk> talks;

    @PlanningScore
    private HardMediumSoftScore score;

    private ConstraintWeightOverrides<HardMediumSoftScore> constraintWeightOverrides = ConstraintWeightOverrides.none();

    public ConferenceSchedule() {
    }

    public ConferenceSchedule(String name, List<TalkType> talkTypes, List<Timeslot> timeslots, List<Room> rooms,
            List<Speaker> speakers, List<Talk> talks) {
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

    public ConferenceConstraintProperties getConstraintProperties() {
        return constraintProperties;
    }

    public void setConstraintProperties(ConferenceConstraintProperties constraintProperties) {
        this.constraintProperties = constraintProperties;
    }

    public List<TalkType> getTalkTypes() {
        return talkTypes;
    }

    public List<Timeslot> getTimeslots() {
        return timeslots;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public List<Speaker> getSpeakers() {
        return speakers;
    }

    public List<Talk> getTalks() {
        return talks;
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
}
