package org.acme.vehiclerouting.domain.jackson;

import ai.timefold.solver.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import ai.timefold.solver.core.api.score.constraint.ConstraintRef;
import ai.timefold.solver.core.api.score.stream.ConstraintJustification;
import ai.timefold.solver.core.api.score.stream.DefaultConstraintJustification;
import ai.timefold.solver.jackson.api.score.analysis.AbstractScoreAnalysisJacksonDeserializer;

public class VRPScoreAnalysisJacksonDeserializer extends AbstractScoreAnalysisJacksonDeserializer<HardMediumSoftLongScore> {

    @Override
    protected HardMediumSoftLongScore parseScore(String scoreString) {
        return HardMediumSoftLongScore.parseScore(scoreString);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <ConstraintJustification_ extends ConstraintJustification> ConstraintJustification_ parseConstraintJustification(
            ConstraintRef constraintRef, String constraintJustificationString, HardMediumSoftLongScore score) {
        return (ConstraintJustification_) DefaultConstraintJustification.of(score);
    }
}
