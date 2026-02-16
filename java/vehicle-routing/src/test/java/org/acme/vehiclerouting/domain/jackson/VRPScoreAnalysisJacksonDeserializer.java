package org.acme.vehiclerouting.domain.jackson;

import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.constraint.ConstraintRef;
import ai.timefold.solver.core.api.score.stream.ConstraintJustification;
import ai.timefold.solver.core.api.score.stream.DefaultConstraintJustification;
import ai.timefold.solver.quarkus.jackson.score.analysis.AbstractScoreAnalysisJacksonDeserializer;

public class VRPScoreAnalysisJacksonDeserializer extends AbstractScoreAnalysisJacksonDeserializer<HardMediumSoftScore> {

    @Override
    protected HardMediumSoftScore parseScore(String scoreString) {
        return HardMediumSoftScore.parseScore(scoreString);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <ConstraintJustification_ extends ConstraintJustification> ConstraintJustification_ parseConstraintJustification(
            ConstraintRef constraintRef, String constraintJustificationString, HardMediumSoftScore score) {
        return (ConstraintJustification_) DefaultConstraintJustification.of(score);
    }
}
