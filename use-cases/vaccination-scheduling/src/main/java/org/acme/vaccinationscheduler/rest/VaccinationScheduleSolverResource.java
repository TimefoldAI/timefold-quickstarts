package org.acme.vaccinationscheduler.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

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
            int appointmentsSize = schedule.getAppointments().size();
            if (appointmentsSize > APPOINTMENT_PAGE_LIMIT) {
                List<VaccineType> vaccineTypes = schedule.getVaccineTypes();
                List<VaccinationCenter> vaccinationCenters = schedule.getVaccinationCenters();
                List<Appointment> appointments;
                List<Person> people;
                if (appointmentsSize <= APPOINTMENT_PAGE_LIMIT) {
                    appointments = schedule.getAppointments();
                    people = schedule.getPeople();
                } else {
                    Map<VaccinationCenter, Set<String>> boothIdSetMap = new HashMap<>(vaccinationCenters.size());
                    for (VaccinationCenter vaccinationCenter : vaccinationCenters) {
                        boothIdSetMap.put(vaccinationCenter, new LinkedHashSet<>());
                    }
                    for (Appointment appointment : schedule.getAppointments()) {
                        Set<String> boothIds = boothIdSetMap.get(appointment.getVaccinationCenter());
                        boothIds.add(appointment.getBoothId());
                    }
                    Map<VaccinationCenter, Set<String>> subBoothIds = new HashMap<>(vaccinationCenters.size());
                    boothIdSetMap.forEach((vaccinationCenter, boothIdSet) -> {
                        List<String> boothIds = new ArrayList<>(boothIdSet);
                        int pageLength = Math.max(1, boothIds.size() * APPOINTMENT_PAGE_LIMIT / appointmentsSize);
                        subBoothIds.put(vaccinationCenter, new HashSet<>(
                                // For a page, filter the number of booths per page from each vaccination center
                                boothIds.subList(page * pageLength,
                                        Math.min(boothIds.size(), (page + 1) * pageLength))));
                    });
                    appointments = schedule.getAppointments().stream()
                            .filter(appointment -> subBoothIds.get(appointment.getVaccinationCenter())
                                    .contains(appointment.getBoothId()))
                            .collect(Collectors.toList());
                    people = schedule.getPeople().stream()
                            .filter(person -> person.getAppointment() != null
                                    && subBoothIds.get(person.getAppointment().getVaccinationCenter())
                                    .contains(person.getAppointment().getBoothId()))
                            .collect(Collectors.toList());

                    List<Person> unassignedPeople = people.stream()
                            .filter(person -> person.getAppointment() == null)
                            .collect(Collectors.toList());
                    int pageLength = unassignedPeople.size() * APPOINTMENT_PAGE_LIMIT / appointmentsSize;
                    people.addAll(unassignedPeople.subList(page * pageLength,
                            Math.min(unassignedPeople.size(), (page + 1) * pageLength)));
                }
                VaccinationSchedule pagedSchedule = new VaccinationSchedule(
                        vaccineTypes, vaccinationCenters, appointments, people);
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
