package org.acme.employeescheduling.rest;

import java.time.LocalDate;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.acme.employeescheduling.bootstrap.DemoDataGenerator;
import org.acme.employeescheduling.domain.EmployeeSchedule;
import org.acme.employeescheduling.domain.ScheduleState;
import org.acme.employeescheduling.domain.Shift;
import org.acme.employeescheduling.persistence.AvailabilityRepository;
import org.acme.employeescheduling.persistence.EmployeeRepository;
import org.acme.employeescheduling.persistence.ScheduleStateRepository;
import org.acme.employeescheduling.persistence.ShiftRepository;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;

import io.quarkus.panache.common.Sort;

@Path("/schedule")
public class EmployeeScheduleResource {

    public static final Long SINGLETON_SCHEDULE_ID = 1L;

    @Inject
    AvailabilityRepository availabilityRepository;
    @Inject
    EmployeeRepository employeeRepository;
    @Inject
    ShiftRepository shiftRepository;
    @Inject
    ScheduleStateRepository scheduleStateRepository;

    @Inject
    DemoDataGenerator dataGenerator;

    @Inject
    SolverManager<EmployeeSchedule, Long> solverManager;
    @Inject
    SolutionManager<EmployeeSchedule, HardSoftScore> solutionManager;

    // To try, open http://localhost:8080/schedule
    @GET
    public EmployeeSchedule getSchedule() {
        // Get the solver status before loading the solution
        // to avoid the race condition that the solver terminates between them
        SolverStatus solverStatus = getSolverStatus();
        EmployeeSchedule solution = findById(SINGLETON_SCHEDULE_ID);
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
    @Transactional
    @Path("publish")
    public void publish() {
        if (!getSolverStatus().equals(SolverStatus.NOT_SOLVING)) {
            throw new IllegalStateException("Cannot publish a schedule while solving is in progress.");
        }
        ScheduleState scheduleState = scheduleStateRepository.findById(SINGLETON_SCHEDULE_ID);
        LocalDate newHistoricDate = scheduleState.getFirstDraftDate();
        LocalDate newDraftDate = scheduleState.getFirstDraftDate().plusDays(scheduleState.getPublishLength());

        scheduleState.setLastHistoricDate(newHistoricDate);
        scheduleState.setFirstDraftDate(newDraftDate);

        dataGenerator.generateDraftShifts(scheduleState);
    }

    @POST
    @Path("stopSolving")
    public void stopSolving() {
        solverManager.terminateEarly(SINGLETON_SCHEDULE_ID);
    }

    @Transactional
    protected EmployeeSchedule findById(Long id) {
        if (!SINGLETON_SCHEDULE_ID.equals(id)) {
            throw new IllegalStateException("There is no schedule with id (" + id + ").");
        }
        return new EmployeeSchedule(
                scheduleStateRepository.findById(SINGLETON_SCHEDULE_ID),
                availabilityRepository.listAll(Sort.by("date").and("id")),
                employeeRepository.listAll(Sort.by("name")),
                shiftRepository.listAll(Sort.by("location").and("start").and("id")));
    }

    @Transactional
    protected void save(EmployeeSchedule schedule) {
        for (Shift shift : schedule.getShiftList()) {
            // TODO this is awfully naive: optimistic locking causes issues if called by the SolverManager
            Shift attachedShift = shiftRepository.findById(shift.getId());
            attachedShift.setEmployee(shift.getEmployee());
        }
    }
}
