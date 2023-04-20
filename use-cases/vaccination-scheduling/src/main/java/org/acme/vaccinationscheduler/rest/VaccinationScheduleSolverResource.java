package org.acme.vaccinationscheduler.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.acme.vaccinationscheduler.domain.Appointment;
import org.acme.vaccinationscheduler.domain.Person;
import org.acme.vaccinationscheduler.domain.VaccinationCenter;
import org.acme.vaccinationscheduler.domain.VaccinationSchedule;
import org.acme.vaccinationscheduler.domain.VaccineType;
import org.acme.vaccinationscheduler.domain.solver.VaccinationSolution;
import org.acme.vaccinationscheduler.persistence.VaccinationScheduleRepository;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;

@Path("vaccinationSchedule")
public class VaccinationScheduleSolverResource {

    private static final int APPOINTMENT_PAGE_LIMIT = 5_000;

    @Inject
    VaccinationScheduleRepository vaccinationScheduleRepository;

    @Inject
    SolverManager<VaccinationSolution, Long> solverManager;

    // To try, open http://localhost:8080/vaccinationSchedule
    @GET
    public VaccinationSchedule get(@QueryParam("page") Integer page) {
        // Get the solver status before loading the schedule
        // to avoid the race condition that the solver terminates between them
        SolverStatus solverStatus = getSolverStatus();
        VaccinationSchedule schedule = vaccinationScheduleRepository.find();
        schedule.setSolverStatus(solverStatus);
        // Optional pagination because the UI can't handle huge datasets
        if (page != null) {
            if (page < 0) {
                throw new IllegalArgumentException("Unsupported page (" + page + ").");
            }
            int appointmentListSize = schedule.getAppointmentList().size();
            if (appointmentListSize > APPOINTMENT_PAGE_LIMIT) {
                List<VaccineType> vaccineTypeList = schedule.getVaccineTypeList();
                List<VaccinationCenter> vaccinationCenterList = schedule.getVaccinationCenterList();
                List<Appointment> appointmentList;
                List<Person> personList;
                if (appointmentListSize <= APPOINTMENT_PAGE_LIMIT) {
                    appointmentList = schedule.getAppointmentList();
                    personList = schedule.getPersonList();
                } else {
                    Map<VaccinationCenter, Set<String>> boothIdSetMap = new HashMap<>(vaccinationCenterList.size());
                    for (VaccinationCenter vaccinationCenter : vaccinationCenterList) {
                        boothIdSetMap.put(vaccinationCenter, new LinkedHashSet<>());
                    }
                    for (Appointment appointment : schedule.getAppointmentList()) {
                        Set<String> boothIdSet = boothIdSetMap.get(appointment.getVaccinationCenter());
                        boothIdSet.add(appointment.getBoothId());
                    }
                    Map<VaccinationCenter, Set<String>> subBoothIdSetMap = new HashMap<>(vaccinationCenterList.size());
                    boothIdSetMap.forEach((vaccinationCenter, boothIdSet) -> {
                        List<String> boothIdList = new ArrayList<>(boothIdSet);
                        int pageLength = Math.max(1, boothIdList.size() * APPOINTMENT_PAGE_LIMIT / appointmentListSize);
                        subBoothIdSetMap.put(vaccinationCenter, new HashSet<>(
                                // For a page, filter the number of booths per page from each vaccination center
                                boothIdList.subList(page * pageLength,
                                        Math.min(boothIdList.size(), (page + 1) * pageLength))));
                    });
                    appointmentList = schedule.getAppointmentList().stream()
                            .filter(appointment -> subBoothIdSetMap.get(appointment.getVaccinationCenter())
                                    .contains(appointment.getBoothId()))
                            .collect(Collectors.toList());
                    personList = schedule.getPersonList().stream()
                            .filter(person -> person.getAppointment() != null
                                    && subBoothIdSetMap.get(person.getAppointment().getVaccinationCenter())
                                    .contains(person.getAppointment().getBoothId()))
                            .collect(Collectors.toList());

                    List<Person> unassignedPersonList = personList.stream()
                            .filter(person -> person.getAppointment() == null)
                            .collect(Collectors.toList());
                    int pageLength = unassignedPersonList.size() * APPOINTMENT_PAGE_LIMIT / appointmentListSize;
                    personList.addAll(unassignedPersonList.subList(page * pageLength,
                            Math.min(unassignedPersonList.size(), (page + 1) * pageLength)));
                }
                VaccinationSchedule pagedSchedule = new VaccinationSchedule(
                        vaccineTypeList, vaccinationCenterList, appointmentList, personList);
                pagedSchedule.setScore(schedule.getScore());
                pagedSchedule.setSolverStatus(schedule.getSolverStatus());
                return pagedSchedule;
            }
        }
        return schedule;
    }

    @POST
    @Path("solve")
    public void solve() {
        solverManager.solveAndListen(1L,
                (problemId) -> {
                    VaccinationSchedule schedule = vaccinationScheduleRepository.find();
                    return new VaccinationSolution(schedule);
                },
                vaccinationSolution -> {
                    vaccinationScheduleRepository.save(vaccinationSolution.toSchedule());
                });
    }

    public SolverStatus getSolverStatus() {
        return solverManager.getSolverStatus(1L);
    }

    @POST
    @Path("stopSolving")
    public void stopSolving() {
        solverManager.terminateEarly(1L);
    }

}
