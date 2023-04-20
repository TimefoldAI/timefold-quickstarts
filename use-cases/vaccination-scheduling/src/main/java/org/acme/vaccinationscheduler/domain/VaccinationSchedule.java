package org.acme.vaccinationscheduler.domain;

import java.util.List;

import org.acme.vaccinationscheduler.domain.solver.VaccinationSolution;
import ai.timefold.solver.core.api.score.buildin.bendablelong.BendableLongScore;
import ai.timefold.solver.core.api.solver.SolverStatus;

public class VaccinationSchedule {

    private List<VaccineType> vaccineTypeList;

    private List<VaccinationCenter> vaccinationCenterList;

    /**
     * Translated to {@link VaccinationSolution#getVaccinationCenterList()} before solving and back again after solving.
     * See {@link VaccinationSolution#VaccinationSolution(VaccinationSchedule)} and {@link VaccinationSolution#toSchedule()}.
     */
    private List<Appointment> appointmentList;

    private List<Person> personList;

    private BendableLongScore score;

    private SolverStatus solverStatus;

    // No-arg constructor required for Jackson
    public VaccinationSchedule() {
    }

    public VaccinationSchedule(List<VaccineType> vaccineTypeList, List<VaccinationCenter> vaccinationCenterList,
            List<Appointment> appointmentList, List<Person> personList) {
        this.vaccineTypeList = vaccineTypeList;
        this.vaccinationCenterList = vaccinationCenterList;
        this.appointmentList = appointmentList;
        this.personList = personList;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public List<VaccineType> getVaccineTypeList() {
        return vaccineTypeList;
    }

    public List<VaccinationCenter> getVaccinationCenterList() {
        return vaccinationCenterList;
    }

    public List<Appointment> getAppointmentList() {
        return appointmentList;
    }

    public List<Person> getPersonList() {
        return personList;
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
