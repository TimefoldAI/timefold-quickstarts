package org.acme.callcenter.solver;

import java.time.Duration;

import jakarta.inject.Inject;

import org.acme.callcenter.domain.Agent;
import org.acme.callcenter.domain.Call;
import org.acme.callcenter.domain.CallCenter;
import org.acme.callcenter.domain.Skill;
import org.junit.jupiter.api.Test;
import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class CallCenterConstraintProviderTest {

    @Inject
    ConstraintVerifier<CallCenterConstraintsProvider, CallCenter> constraintVerifier;

    @Test
    void noRequiredSkillMissing() {
        Agent agent = new Agent("1", "Carl", Skill.ENGLISH);
        Call call = new Call("1","123-456-7890", Skill.ENGLISH, Skill.PROPERTY_INSURANCE, Skill.CAR_INSURANCE);
        call.setPreviousCallOrAgent(agent);
        call.setAgent(agent);
        constraintVerifier.verifyThat(CallCenterConstraintsProvider::noRequiredSkillMissing)
                .given(call, agent)
                .penalizesBy(2);
    }

    @Test
    void minimizeWaitingTime() {
        Agent agent = new Agent("1", "Carl", Skill.ENGLISH);
        Call call1 = new Call("1","123-456-7890", Skill.ENGLISH);
        Call call2 = new Call("2","123-456-7891", Skill.ENGLISH);
        Call call3 = new Call("3","123-456-7892", Skill.ENGLISH);

        call1.setPreviousCallOrAgent(agent);
        call1.setAgent(agent);
        call1.setEstimatedWaiting(Duration.ZERO);

        call2.setPreviousCallOrAgent(call1);
        call2.setAgent(agent);
        call2.setEstimatedWaiting(Duration.ofSeconds(10));

        call3.setPreviousCallOrAgent(call2);
        call3.setAgent(agent);
        call3.setEstimatedWaiting(Duration.ofSeconds(20));

        constraintVerifier.verifyThat(CallCenterConstraintsProvider::minimizeWaitingTime)
                .given(call1, call2, call3, agent)
                .penalizesBy(0 + 100 + 400);
    }
}
