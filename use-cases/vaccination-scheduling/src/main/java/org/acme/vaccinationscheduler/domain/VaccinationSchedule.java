package org.acme.vaccinationscheduler.domain;

import java.util.List;

import org.acme.vaccinationscheduler.domain.solver.VaccinationSolution;
import ai.timefold.solver.core.api.score.buildin.bendablelong.BendableLongScore;
import ai.timefold.solver.core.api.solver.SolverStatus;

public class VaccinationSchedule {

    private List<VaccineType> vaccineTypes;

    private List<VaccinationCenter> vaccinationCenters;

    /**
     * Translated to {@link VaccinationSolution#getVaccinationCenters()} before solving and back again after solving.
     * See {@link VaccinationSolution#VaccinationSolution(VaccinationSchedule)} and {@link VaccinationSolution#toSchedule()}.
     */
    private List<Appointment> appointments;

    private List<Person> people;

    private BendableLongScore score;

    private SolverStatus solverStatus;

    // No-arg constructor required for Jackson
    public VaccinationSchedule() {
    }

    public VaccinationSchedule(List<VaccineType> vaccineTypes, List<VaccinationCenter> vaccinationCenters,
                               List<Appointment> appointments, List<Person> people) {
        this.vaccineTypes = vaccineTypes;
        this.vaccinationCenters = vaccinationCenters;
        this.appointments = appointments;
        this.people = people;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public List<VaccineType> getVaccineTypes() {
        return vaccineTypes;
    }

    public List<VaccinationCenter> getVaccinationCenters() {
        return vaccinationCenters;
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public List<Person> getPeople() {
        return people;
    }

    public BendableLongScore getScore() {
        return score;
    }

    public void setScore(BendableLongScore score) {
        this.score = score;
    }

    public SolverStatus getSolverStatus() {
        return solverStatus;
    }

    public void setSolverStatus(SolverStatus solverStatus) {
        this.solverStatus = solverStatus;
    }

}
