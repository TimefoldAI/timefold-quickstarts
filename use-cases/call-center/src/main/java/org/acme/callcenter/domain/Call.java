package org.acme.callcenter.domain;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.acme.callcenter.solver.ResponseTimeUpdatingVariableListener;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.entity.PlanningPin;
import ai.timefold.solver.core.api.domain.variable.AnchorShadowVariable;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import ai.timefold.solver.core.api.domain.variable.PlanningVariableGraphType;
import ai.timefold.solver.core.api.domain.variable.ShadowVariable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import static java.util.stream.Collectors.joining;

@PlanningEntity
public class Call extends PreviousCallOrAgent {

    private String phoneNumber;
    private Set<Skill> requiredSkills;
    private Duration duration = Duration.ZERO;
    private LocalTime startTime;
    private LocalTime pickUpTime;

    @PlanningPin
    private boolean pinned;

    @JsonIgnore
    @PlanningVariable(graphType = PlanningVariableGraphType.CHAINED)
    private PreviousCallOrAgent previousCallOrAgent;

    @JsonIgnore
    @AnchorShadowVariable(sourceVariableName = "previousCallOrAgent")
    private Agent agent;

    @ShadowVariable(variableListenerClass = ResponseTimeUpdatingVariableListener.class, sourceVariableName = "previousCallOrAgent")
    private Duration estimatedWaiting;

    public Call() {
        // Required by Timefold.
    }

    public Call(long id, String phoneNumber) {
        super(id);
        this.phoneNumber = phoneNumber;
        this.requiredSkills = EnumSet.noneOf(Skill.class);
        this.startTime = LocalTime.now();
    }

    public Call(long id, String phoneNumber, Set<Skill> requiredSkills, int durationSeconds) {
        super(id);
        this.phoneNumber = phoneNumber;
        this.requiredSkills = EnumSet.copyOf(requiredSkills);
        this.duration = Duration.ofSeconds(durationSeconds);
        this.startTime = LocalTime.now();
    }

    public Call(long id, String phoneNumber, Skill... requiredSkills) {
        this(id, phoneNumber);
        this.requiredSkills.addAll(Arrays.asList(requiredSkills));
    }

    public int getMissingSkillCount() {
        if (agent == null) {
            return 0;
        }

        return (int) requiredSkills.stream()
                .filter(skill -> !agent.getSkills().contains(skill))
                .count();
    }

    @Override
    public Duration getDurationTillPickUp() {
        Duration durationTillPickUp;
        if (estimatedWaiting == null) {
            return null;
        } else {
            durationTillPickUp = estimatedWaiting.plus(getDuration());
            if (pickUpTime != null) {
                durationTillPickUp = durationTillPickUp.minus(Duration.between(pickUpTime, LocalTime.now()));
            }
        }
        return durationTillPickUp;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Set<Skill> getRequiredSkills() {
        return requiredSkills;
    }

    public boolean isPinned() {
        return pinned;
    }

    public PreviousCallOrAgent getPreviousCallOrAgent() {
        return previousCallOrAgent;
    }

    public Agent getAgent() {
        return agent;
    }

    public Duration getEstimatedWaiting() {
        return estimatedWaiting;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public void setPreviousCallOrAgent(PreviousCallOrAgent previousCallOrAgent) {
        this.previousCallOrAgent = previousCallOrAgent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public void setEstimatedWaiting(Duration estimatedWaiting) {
        this.estimatedWaiting = estimatedWaiting;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getPickUpTime() {
        return pickUpTime;
    }

    public void setPickUpTime(LocalTime pickUpTime) {
        this.pickUpTime = pickUpTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Call))
            return false;
        Call call = (Call) o;
        return getId().equals(call.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "Call{" +
                "phoneNumber='" + phoneNumber + '\'' +
                ", requiredSkills=" + Optional.ofNullable(requiredSkills).map(s -> s.stream().map(Skill::getName).collect(joining(", "))).orElse("-") +
                ", duration=" + duration +
                ", startTime=" + startTime +
                ", pickUpTime=" + pickUpTime +
                ", pinned=" + pinned +
                ", previousCallOrAgent=" + previousCallOrAgent +
                ", agent=" + agent +
                ", estimatedWaiting=" + estimatedWaiting +
                '}';
    }
}
