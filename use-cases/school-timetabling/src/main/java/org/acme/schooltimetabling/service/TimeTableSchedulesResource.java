package org.acme.schooltimetabling.service;

import ai.timefold.solver.core.api.solver.SolverManager;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import org.acme.schooltimetabling.domain.TimeTable;
import org.acme.schooltimetabling.timefoldcommon.AbstractSchedulesResource;

@Path("schedules")
public class TimeTableSchedulesResource extends AbstractSchedulesResource<TimeTable> {

    // Workaround to make Quarkus CDI happy. Do not use.
    public TimeTableSchedulesResource() {
        super(null);
    }

    @Inject
    public TimeTableSchedulesResource(SolverManager<TimeTable, String> solverManager) {
        super(solverManager);
    }

}
