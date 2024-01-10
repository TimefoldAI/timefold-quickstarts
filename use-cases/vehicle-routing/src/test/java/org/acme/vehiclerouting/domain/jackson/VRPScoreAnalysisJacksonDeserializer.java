package org.acme.vehiclerouting.domain.jackson;

import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.score.constraint.ConstraintRef;
import ai.timefold.solver.core.api.score.stream.ConstraintJustification;
import ai.timefold.solver.jackson.api.score.analysis.AbstractScoreAnalysisJacksonDeserializer;
import org.acme.vehiclerouting.solver.VehicleRoutingConstraintProvider;
import org.acme.vehiclerouting.solver.justifications.MinimizeTravelTimeJustification;
import org.acme.vehiclerouting.solver.justifications.ServiceFinishedAfterMaxEndTimeJustification;
import org.acme.vehiclerouting.solver.justifications.VehicleCapacityJustification;

public class VRPScoreAnalysisJacksonDeserializer extends AbstractScoreAnalysisJacksonDeserializer<HardSoftLongScore> {

    @Override
    protected HardSoftLongScore parseScore(String scoreString) {
        return HardSoftLongScore.parseScore(scoreString);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <ConstraintJustification_ extends ConstraintJustification> Class<ConstraintJustification_>
            getConstraintJustificationClass(ConstraintRef constraintRef) {

        switch (constraintRef.constraintName()) {
            case VehicleRoutingConstraintProvider.MINIMIZE_TRAVEL_TIME:
                return (Class<ConstraintJustification_>) MinimizeTravelTimeJustification.class;
            case VehicleRoutingConstraintProvider.VEHICLE_CAPACITY:
                return (Class<ConstraintJustification_>) VehicleCapacityJustification.class;
            case VehicleRoutingConstraintProvider.SERVICE_FINISHED_AFTER_MAX_END_TIME:
                return (Class<ConstraintJustification_>) ServiceFinishedAfterMaxEndTimeJustification.class;
            default:
                throw new UnsupportedOperationException("Deserialization of (%s) constraint not supported, please extend %s."
                        .formatted(constraintRef.constraintName(), this.getClass().getName()));
        }
    }
}
