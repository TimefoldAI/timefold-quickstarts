package org.acme.callcenter.solver;

import org.acme.callcenter.domain.Call;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

public class CallCenterConstraintsProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                noRequiredSkillMissing(constraintFactory),
                minimizeWaitingTime(constraintFactory),
        };
    }

    Constraint noRequiredSkillMissing(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Call.class)
                .filter(call -> call.getMissingSkillCount() > 0)
                .penalize(HardSoftScore.ONE_HARD, Call::getMissingSkillCount)
                .asConstraint("No required skills are missing");
    }

    Constraint minimizeWaitingTime(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Call.class)
                .filter(call -> call.getNextCall() == null)
                .penalize(HardSoftScore.ONE_SOFT, call -> Math.toIntExact(call.getEstimatedWaiting().getSeconds()
                                * call.getEstimatedWaiting().getSeconds()))
                .asConstraint("Minimize waiting time");
    }
}
