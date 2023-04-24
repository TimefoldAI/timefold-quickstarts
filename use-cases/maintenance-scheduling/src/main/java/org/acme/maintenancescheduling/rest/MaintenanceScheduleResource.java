package org.acme.maintenancescheduling.rest;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import org.acme.maintenancescheduling.domain.Job;
import org.acme.maintenancescheduling.domain.MaintenanceSchedule;
import org.acme.maintenancescheduling.persistence.CrewRepository;
import org.acme.maintenancescheduling.persistence.JobRepository;
import org.acme.maintenancescheduling.persistence.WorkCalendarRepository;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;

import io.quarkus.panache.common.Sort;

@Path("/schedule")
public class MaintenanceScheduleResource {

    public static final Long SINGLETON_SCHEDULE_ID = 1L;

    @Inject
    WorkCalendarRepository workCalendarRepository;
    @Inject
    CrewRepository crewRepository;
    @Inject
    JobRepository jobRepository;

    @Inject
    SolverManager<MaintenanceSchedule, Long> solverManager;
    @Inject
    SolutionManager<MaintenanceSchedule, HardSoftScore> solutionManager;

    // To try, open http://localhost:8080/schedule
    @GET
    public MaintenanceSchedule getSchedule() {
        // Get the solver status before loading the solution
        // to avoid the race condition that the solver terminates between them
        SolverStatus solverStatus = getSolverStatus();
        MaintenanceSchedule solution = findById(SINGLETON_SCHEDULE_ID);
        solutionManager.update(solution); // Sets the score
        solution.setSolverStatus(solverStatus);
        return solution;
    }

    public SolverStatus getSolverStatus() {
        return solverManager.getSolverStatus(SINGLETON_SCHEDULE_ID);
    }

    @POST
    @Path("solve")
    public void solve() {
        solverManager.solveAndListen(SINGLETON_SCHEDULE_ID,
                this::findById,
                this::save);
    }

    @POST
    @Path("stopSolving")
    public void stopSolving() {
        solverManager.terminateEarly(SINGLETON_SCHEDULE_ID);
    }

    @Transactional
    protected MaintenanceSchedule findById(Long id) {
        if (!SINGLETON_SCHEDULE_ID.equals(id)) {
            throw new IllegalStateException("There is no schedule with id (" + id + ").");
        }
        return new MaintenanceSchedule(
                workCalendarRepository.listAll().get(0),
                crewRepository.listAll(Sort.by("name").and("id")),
                jobRepository.listAll(Sort.by("dueDate").and("readyDate").and("name").and("id")));
    }

    @Transactional
    protected void save(MaintenanceSchedule schedule) {
        for (Job job : schedule.getJobList()) {
            // TODO this is awfully naive: optimistic locking causes issues if called by the SolverManager
            Job attachedJob = jobRepository.findById(job.getId());
            attachedJob.setCrew(job.getCrew());
            attachedJob.setStartDate(job.getStartDate());
            attachedJob.setEndDate(job.getEndDate());
        }
    }
}
