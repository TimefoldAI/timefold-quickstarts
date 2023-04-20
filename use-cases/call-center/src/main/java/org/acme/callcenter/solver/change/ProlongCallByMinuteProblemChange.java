package org.acme.callcenter.solver.change;

import java.time.Duration;

import org.acme.callcenter.domain.Call;
import org.acme.callcenter.domain.CallCenter;
import ai.timefold.solver.core.api.solver.change.ProblemChange;
import ai.timefold.solver.core.api.solver.change.ProblemChangeDirector;

public class ProlongCallByMinuteProblemChange implements ProblemChange<CallCenter> {

    private static final Duration PROLONGATION = Duration.ofMinutes(1L);
    private final long callId;

    public ProlongCallByMinuteProblemChange(long callId) {
        this.callId = callId;
    }

    @Override
    public void doChange(CallCenter workingSolution, ProblemChangeDirector problemChangeDirector) {
        Call call = new Call(callId, null);

        problemChangeDirector.changeProblemProperty(call,
                workingCall -> workingCall.setDuration(workingCall.getDuration().plus(PROLONGATION)));
    }
}
