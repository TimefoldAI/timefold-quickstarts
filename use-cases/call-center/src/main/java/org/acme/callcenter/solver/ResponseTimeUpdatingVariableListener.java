package org.acme.callcenter.solver;

import java.time.Duration;

import org.acme.callcenter.domain.Call;
import org.acme.callcenter.domain.CallCenter;
import org.acme.callcenter.domain.PreviousCallOrAgent;
import ai.timefold.solver.core.api.domain.variable.VariableListener;
import ai.timefold.solver.core.api.score.director.ScoreDirector;

public class ResponseTimeUpdatingVariableListener implements VariableListener<CallCenter, Call> {

    @Override
    public void beforeEntityAdded(ScoreDirector<CallCenter> scoreDirector, Call call) {

    }

    @Override
    public void afterEntityAdded(ScoreDirector<CallCenter> scoreDirector, Call call) {
        updateResponseTime(scoreDirector, call);
    }

    @Override
    public void beforeVariableChanged(ScoreDirector<CallCenter> scoreDirector, Call call) {

    }

    @Override
    public void afterVariableChanged(ScoreDirector<CallCenter> scoreDirector, Call call) {
        updateResponseTime(scoreDirector, call);
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector<CallCenter> scoreDirector, Call call) {

    }

    @Override
    public void afterEntityRemoved(ScoreDirector<CallCenter> scoreDirector, Call call) {

    }

    protected void updateResponseTime(ScoreDirector<CallCenter> scoreDirector, Call call) {
        PreviousCallOrAgent previous = call.getPreviousCallOrAgent();
        Call shadowCall = call;
        Duration previousDurationTillPickUp = (previous == null ? null : previous.getDurationTillPickUp());
        Duration estimatedWaiting = calculateWaitingTimeEstimate(shadowCall, previousDurationTillPickUp);
        while (shadowCall != null) {
            scoreDirector.beforeVariableChanged(shadowCall, "estimatedWaiting");
            shadowCall.setEstimatedWaiting(estimatedWaiting);
            scoreDirector.afterVariableChanged(shadowCall, "estimatedWaiting");
            previousDurationTillPickUp = shadowCall.getDurationTillPickUp();
            shadowCall = shadowCall.getNextCall();
            estimatedWaiting = calculateWaitingTimeEstimate(shadowCall, previousDurationTillPickUp);
        }
    }

    private Duration calculateWaitingTimeEstimate(Call call, Duration previousEndTime) {
        if (call == null || previousEndTime == null) {
            return null;
        }
        return previousEndTime;
    }
}
