package org.acme.callcenter.domain;

import java.util.List;
import java.util.Set;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;

@PlanningSolution
public class CallCenter {

    @ProblemFactCollectionProperty
    private Set<Skill> skills;

    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<Agent> agents;

    @PlanningEntityCollectionProperty
    @ValueRangeProvider
    private List<Call> calls;

    @PlanningScore
    private HardSoftScore score;

    private boolean solving;

    public CallCenter() {
        // Required by Timefold.
    }

    public CallCenter(Set<Skill> skills, List<Agent> agents, List<Call> calls) {
        this.skills = skills;
        this.agents = agents;
        this.calls = calls;
    }

    public Set<Skill> getSkills() {
        return skills;
    }

    public List<Agent> getAgents() {
        return agents;
    }

    public List<Call> getCalls() {
        return calls;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }

    public boolean isSolving() {
        return solving;
    }

    public void setSolving(boolean solving) {
        this.solving = solving;
    }
}
