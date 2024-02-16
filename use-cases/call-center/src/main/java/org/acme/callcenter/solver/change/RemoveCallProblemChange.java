package org.acme.callcenter.solver.change;

import java.util.Optional;

import org.acme.callcenter.domain.Call;
import org.acme.callcenter.domain.CallCenter;
import org.acme.callcenter.domain.PreviousCallOrAgent;
import ai.timefold.solver.core.api.solver.change.ProblemChange;
import ai.timefold.solver.core.api.solver.change.ProblemChangeDirector;

public class RemoveCallProblemChange implements ProblemChange<CallCenter> {

    private final String callId;

    public RemoveCallProblemChange(String callId) {
        this.callId = callId;
    }

    @Override
    public void doChange(CallCenter workingCallCenter, ProblemChangeDirector problemChangeDirector) {
        Call call = new Call(callId, null);
        Optional<Call> workingCallOptional = problemChangeDirector.lookUpWorkingObject(call);
        workingCallOptional.ifPresent(workingCall -> removeCall(workingCall, workingCallCenter, problemChangeDirector));
    }

    private void removeCall(Call call, CallCenter workingCallCenter, ProblemChangeDirector problemChangeDirector) {
        PreviousCallOrAgent previousCallOrAgent = call.getPreviousCallOrAgent();

        Call nextCall = call.getNextCall();
        if (nextCall != null) {
            problemChangeDirector.changeVariable(nextCall, "previousCallOrAgent",
                    workingNextCall -> workingNextCall.setPreviousCallOrAgent(previousCallOrAgent));
        }

        problemChangeDirector.removeEntity(call, workingCallCenter.getCalls()::remove);
    }
}
