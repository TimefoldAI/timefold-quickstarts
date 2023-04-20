package org.acme.callcenter.solver.change;

import org.acme.callcenter.domain.Call;
import org.acme.callcenter.domain.CallCenter;
import ai.timefold.solver.core.api.solver.change.ProblemChange;
import ai.timefold.solver.core.api.solver.change.ProblemChangeDirector;

public class AddCallProblemChange implements ProblemChange<CallCenter> {

    private final Call call;

    public AddCallProblemChange(Call call) {
        this.call = call;
    }

    @Override
    public void doChange(CallCenter workingCallCenter, ProblemChangeDirector problemChangeDirector) {
        problemChangeDirector.addEntity(call, workingCallCenter.getCalls()::add);
    }
}
