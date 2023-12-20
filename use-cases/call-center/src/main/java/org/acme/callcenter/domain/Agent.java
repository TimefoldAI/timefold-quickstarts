package org.acme.callcenter.domain;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Agent extends PreviousCallOrAgent {

    private String name;
    private Set<Skill> skills;

    public Agent() {
        // Required by Timefold.
    }

    public Agent(long id, String name) {
        super(id);
        this.name = name;
        this.skills = EnumSet.noneOf(Skill.class);
    }

    public Agent(long id, String name, Set<Skill> skills) {
        super(id);
        this.name = name;
        this.skills = EnumSet.copyOf(skills);
    }

    public Agent(long id, String name, Skill... skills) {
        this(id, name, EnumSet.copyOf(Arrays.asList(skills)));
    }

    @JsonProperty(value = "calls", access = JsonProperty.Access.READ_ONLY)
    public List<Call> getAssignedCalls() {
        Call nextCall = getNextCall();
        List<Call> assignedCalls = new ArrayList<>();
        while (nextCall != null) {
            assignedCalls.add(nextCall);
            nextCall = nextCall.getNextCall();
        }
        return assignedCalls;
    }

    @Override
    public Duration getDurationTillPickUp() {
        return Duration.ZERO;
    }

    public String getName() {
        return name;
    }

    public Set<Skill> getSkills() {
        return skills;
    }
}
