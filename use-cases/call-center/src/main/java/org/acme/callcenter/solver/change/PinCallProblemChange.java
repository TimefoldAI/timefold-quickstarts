package org.acme.callcenter.solver.change;

import java.time.LocalTime;

import org.acme.callcenter.domain.Call;
import org.acme.callcenter.domain.CallCenter;
import ai.timefold.solver.core.api.solver.change.ProblemChange;
import ai.timefold.solver.core.api.solver.change.ProblemChangeDirector;

public class PinCallProblemChange implements ProblemChange<CallCenter> {

    private final Call call;

    public PinCallProblemChange(Call call) {
        this.call = call;
    }

    @Override
    public void doChange(CallCenter workingCallCenter, ProblemChangeDirector problemChangeDirector) {
        problemChangeDirector.changeProblemProperty(call, workingCall -> {
            workingCall.setPinned(true);
            workingCall.setPickUpTime(LocalTime.now());
        });
    }
}
