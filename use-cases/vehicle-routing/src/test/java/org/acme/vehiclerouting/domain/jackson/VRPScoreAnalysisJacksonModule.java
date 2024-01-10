package org.acme.vehiclerouting.domain.jackson;

import ai.timefold.solver.core.api.score.analysis.ScoreAnalysis;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class VRPScoreAnalysisJacksonModule extends SimpleModule {
    public VRPScoreAnalysisJacksonModule() {
        super("VRP Custom Jackson Module for Score Analysis");
        addDeserializer(ScoreAnalysis.class, new VRPScoreAnalysisJacksonDeserializer());
    }
}
