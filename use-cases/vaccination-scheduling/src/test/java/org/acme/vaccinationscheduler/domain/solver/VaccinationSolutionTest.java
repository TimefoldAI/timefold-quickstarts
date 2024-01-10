package org.acme.vaccinationscheduler.domain.solver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.acme.vaccinationscheduler.domain.Appointment;
import org.acme.vaccinationscheduler.domain.Location;
import org.acme.vaccinationscheduler.domain.Person;
import org.acme.vaccinationscheduler.domain.VaccinationCenter;
import org.acme.vaccinationscheduler.domain.VaccinationSchedule;
import org.acme.vaccinationscheduler.domain.VaccineType;
import org.junit.jupiter.api.Test;

class VaccinationSolutionTest {

    private static final VaccineType PFIZER = new VaccineType("Pfizer");
    private static final VaccineType MODERNA = new VaccineType("Moderna");
    private static final VaccinationCenter VACCINATION_CENTER_1 = new VaccinationCenter("1", "Downtown", new Location(0, 0));
    private static final VaccinationCenter VACCINATION_CENTER_2 = new VaccinationCenter("2", "Uptown", new Location(10, 10));

    private static final LocalDate MONDAY = LocalDate.of(2021, 2, 1);
    private static final LocalDateTime MONDAY_0900 = LocalDateTime.of(MONDAY, LocalTime.of(9, 0));
    private static final LocalDateTime MONDAY_0910 = LocalDateTime.of(MONDAY, LocalTime.of(9, 10));
    private static final LocalDateTime MONDAY_0920 = LocalDateTime.of(MONDAY, LocalTime.of(9, 20));
    private static final LocalDateTime MONDAY_0930 = LocalDateTime.of(MONDAY, LocalTime.of(9, 30));
    private static final LocalDateTime MONDAY_1000 = LocalDateTime.of(MONDAY, LocalTime.of(10, 0));
    private static final LocalDateTime MONDAY_1010 = LocalDateTime.of(MONDAY, LocalTime.of(10, 10));

    @Test
    void empty() {
        List<VaccineType> vaccineTypeList = Arrays.asList(PFIZER, MODERNA);
        List<VaccinationCenter> vaccinationCenterList = Arrays.asList(VACCINATION_CENTER_1, VACCINATION_CENTER_2);
        List<Appointment> appointmentList = Arrays.asList();
        List<Person> personList = Collections.emptyList();
        VaccinationSchedule schedule = new VaccinationSchedule(vaccineTypeList, vaccinationCenterList,
                appointmentList, personList);

        VaccinationSolution solution = new VaccinationSolution(schedule);
        assertTrue(solution.getVaccinationSlots().isEmpty());
        assertTrue(solution.getPersonAssignments().isEmpty());

        schedule = solution.toSchedule();
        assertEquals(0, schedule.getPeople().size());
        assertTrue(schedule.getAppointments().isEmpty());
        assertTrue(schedule.getPeople().isEmpty());
    }

    @Test
    void open() {
        List<VaccineType> vaccineTypeList = Arrays.asList(PFIZER, MODERNA);
        List<VaccinationCenter> vaccinationCenterList = Arrays.asList(VACCINATION_CENTER_1, VACCINATION_CENTER_2);
        Appointment vc1_13_0900 = new Appointment(VACCINATION_CENTER_1, "13", MONDAY_0900, MODERNA);
        Appointment vc2_21_0900 = new Appointment(VACCINATION_CENTER_2, "21", MONDAY_0900, PFIZER);
        List<Appointment> appointmentList = Arrays.asList(
                new Appointment(VACCINATION_CENTER_1, "11", MONDAY_0900, PFIZER),
                new Appointment(VACCINATION_CENTER_1, "12", MONDAY_0900, PFIZER),
                vc1_13_0900,
                vc2_21_0900,
                new Appointment(VACCINATION_CENTER_1, "11", MONDAY_0910, PFIZER),
                new Appointment(VACCINATION_CENTER_1, "12", MONDAY_0910, PFIZER),
                new Appointment(VACCINATION_CENTER_1, "13", MONDAY_0910, MODERNA),
                new Appointment(VACCINATION_CENTER_2, "21", MONDAY_0910, PFIZER),
                new Appointment(VACCINATION_CENTER_1, "11", MONDAY_0920, PFIZER),
                new Appointment(VACCINATION_CENTER_1, "13", MONDAY_0920, MODERNA),
                new Appointment(VACCINATION_CENTER_1, "11", MONDAY_1000, PFIZER),
                new Appointment(VACCINATION_CENTER_1, "11", MONDAY_1010, PFIZER)
        );
        Person ann = new Person("1", "Ann", new Location(1, 0), LocalDate.of(1990, 1, 1), 31);
        Person beth = new Person("2", "Beth", new Location(2, 0), LocalDate.of(1980, 1, 1), 41);
        Person carl = new Person("3", "Carl", new Location(2, 0), LocalDate.of(1970, 1, 1), 51);
        List<Person> personList = Arrays.asList(ann, beth, carl);
        VaccinationSchedule schedule = new VaccinationSchedule(vaccineTypeList, vaccinationCenterList,
                appointmentList, personList);

        VaccinationSolution solution = new VaccinationSolution(schedule);
        List<VaccinationSlot> vaccinationSlotList = solution.getVaccinationSlots();
        assertEquals(4, vaccinationSlotList.size());
        assertVaccinationSlot(vaccinationSlotList.get(0), VACCINATION_CENTER_1, MONDAY_0900, PFIZER, 5);
        assertVaccinationSlot(vaccinationSlotList.get(1), VACCINATION_CENTER_1, MONDAY_0900, MODERNA, 3);
        assertVaccinationSlot(vaccinationSlotList.get(2), VACCINATION_CENTER_2, MONDAY_0900, PFIZER, 2);
        assertVaccinationSlot(vaccinationSlotList.get(3), VACCINATION_CENTER_1, MONDAY_1000, PFIZER, 2);
        List<PersonAssignment> personAssignmentList = solution.getPersonAssignments();
        assertEquals(3, personAssignmentList.size());
        assertEquals("Ann", personAssignmentList.get(0).getName());
        assertEquals("Beth", personAssignmentList.get(1).getName());
        assertEquals("Carl", personAssignmentList.get(2).getName());

        personAssignmentList.get(0).setVaccinationSlot(vaccinationSlotList.get(1));
        personAssignmentList.get(2).setVaccinationSlot(vaccinationSlotList.get(2));
        schedule = solution.toSchedule();
        assertEquals(3, schedule.getPeople().size());
        assertSame(vc1_13_0900, ann.getAppointment());
        assertNull(beth.getAppointment());
        assertSame(vc2_21_0900, carl.getAppointment());
    }

    @Test
    void pinned() {
        List<VaccineType> vaccineTypeList = Arrays.asList(PFIZER, MODERNA);
        List<VaccinationCenter> vaccinationCenterList = Arrays.asList(VACCINATION_CENTER_1, VACCINATION_CENTER_2);
        Appointment vc1_11_0900 = new Appointment(VACCINATION_CENTER_1, "11", MONDAY_0900, PFIZER);
        Appointment vc1_11_0910 = new Appointment(VACCINATION_CENTER_1, "11", MONDAY_0910, PFIZER);
        Appointment vc1_11_0920 = new Appointment(VACCINATION_CENTER_1, "11", MONDAY_0920, PFIZER);
        Appointment vc1_11_0930 = new Appointment(VACCINATION_CENTER_1, "11", MONDAY_0930, PFIZER);
        Appointment vc2_21_1000 = new Appointment(VACCINATION_CENTER_2, "21", MONDAY_1000, PFIZER);
        List<Appointment> appointmentList = Arrays.asList(
                vc1_11_0900, vc1_11_0910, vc1_11_0920, vc1_11_0930, vc2_21_1000
        );
        Person ann = new Person("1", "Ann", new Location(1, 0), LocalDate.of(1990, 1, 1), 31);
        ann.setPinned(true);
        ann.setAppointment(vc1_11_0910);
        Person beth = new Person("2", "Beth", new Location(2, 0), LocalDate.of(1980, 1, 1), 41);
        beth.setPinned(true);
        beth.setAppointment(null);
        Person carl = new Person("3", "Carl", new Location(2, 0), LocalDate.of(1970, 1, 1), 51);
        Person dan = new Person("4", "Dan", new Location(3, 0), LocalDate.of(1960, 1, 1), 61);
        Person ed = new Person("5", "Ed", new Location(4, 0), LocalDate.of(1960, 1, 1), 61);
        ed.setPinned(true);
        ed.setAppointment(vc1_11_0920);
        Person zara = new Person("6", "Zara", new Location(5, 0), LocalDate.of(2000, 1, 1), 21);
        zara.setPinned(true);
        zara.setAppointment(vc2_21_1000); // All appointments in this VaccinationSlot are now taken
        List<Person> personList = Arrays.asList(ann, beth, carl, dan, ed, zara);
        VaccinationSchedule schedule = new VaccinationSchedule(vaccineTypeList, vaccinationCenterList,
                appointmentList, personList);

        VaccinationSolution solution = new VaccinationSolution(schedule);
        List<VaccinationSlot> vaccinationSlotList = solution.getVaccinationSlots();
        assertEquals(2, vaccinationSlotList.size());
        assertVaccinationSlot(vaccinationSlotList.get(0), VACCINATION_CENTER_1, MONDAY_0900, PFIZER, 4);
        List<PersonAssignment> personAssignmentList = solution.getPersonAssignments();
        assertEquals(6, personAssignmentList.size());
        assertEquals("Ann", personAssignmentList.get(0).getName());
        assertEquals("Beth", personAssignmentList.get(1).getName());
        assertEquals("Carl", personAssignmentList.get(2).getName());
        assertEquals("Dan", personAssignmentList.get(3).getName());
        assertEquals("Ed", personAssignmentList.get(4).getName());
        assertEquals("Zara", personAssignmentList.get(5).getName());

        personAssignmentList.get(2).setVaccinationSlot(vaccinationSlotList.get(0));
        personAssignmentList.get(3).setVaccinationSlot(vaccinationSlotList.get(0));
        schedule = solution.toSchedule();
        assertEquals(6, schedule.getPeople().size());
        assertSame(vc1_11_0910, ann.getAppointment());
        assertNull(beth.getAppointment());
        assertSame(vc1_11_0900, carl.getAppointment());
        assertSame(vc1_11_0930, dan.getAppointment());
        assertSame(vc1_11_0920, ed.getAppointment());
        assertSame(vc2_21_1000, zara.getAppointment());
    }

    @Test
    void pinnedDoubleBooking() {
        List<VaccineType> vaccineTypeList = Arrays.asList(PFIZER, MODERNA);
        List<VaccinationCenter> vaccinationCenterList = Arrays.asList(VACCINATION_CENTER_1, VACCINATION_CENTER_2);
        Appointment vc1_11_0900 = new Appointment(VACCINATION_CENTER_1, "11", MONDAY_0900, PFIZER);
        Appointment vc1_11_0910 = new Appointment(VACCINATION_CENTER_1, "11", MONDAY_0910, PFIZER);
        List<Appointment> appointmentList = Arrays.asList(
                vc1_11_0900, vc1_11_0910
        );
        Person ann = new Person("1", "Ann", new Location(1, 0), LocalDate.of(1990, 1, 1), 31);
        ann.setPinned(true);
        ann.setAppointment(vc1_11_0900);
        Person beth = new Person("2", "Beth", new Location(2, 0), LocalDate.of(1980, 1, 1), 41);
        beth.setPinned(true);
        beth.setAppointment(vc1_11_0900); // Double booking
        Person carl = new Person("3", "Carl", new Location(2, 0), LocalDate.of(1970, 1, 1), 51);
        carl.setPinned(true);
        carl.setAppointment(vc1_11_0910);
        List<Person> personList = Arrays.asList(ann, beth, carl);
        VaccinationSchedule schedule = new VaccinationSchedule(vaccineTypeList, vaccinationCenterList,
                appointmentList, personList);

        VaccinationSolution solution = new VaccinationSolution(schedule);
        List<VaccinationSlot> vaccinationSlotList = solution.getVaccinationSlots();
        assertEquals(1, vaccinationSlotList.size());
        assertVaccinationSlot(vaccinationSlotList.get(0), VACCINATION_CENTER_1, MONDAY_0900, PFIZER, 2);
        List<PersonAssignment> personAssignmentList = solution.getPersonAssignments();
        assertEquals(3, personAssignmentList.size());
        assertEquals("Ann", personAssignmentList.get(0).getName());
        assertEquals("Beth", personAssignmentList.get(1).getName());
        assertEquals("Carl", personAssignmentList.get(2).getName());

        schedule = solution.toSchedule();
        assertEquals(3, schedule.getPeople().size());
        assertSame(vc1_11_0900, ann.getAppointment());
        assertSame(vc1_11_0900, beth.getAppointment());
        assertSame(vc1_11_0910, carl.getAppointment());
    }

    private void assertVaccinationSlot(VaccinationSlot vaccinationSlot,
            VaccinationCenter vaccinationCenter, LocalDateTime startDateTime, VaccineType vaccineType, int capacity) {
        assertEquals(vaccinationCenter, vaccinationSlot.getVaccinationCenter());
        assertEquals(startDateTime, vaccinationSlot.getStartDateTime());
        assertEquals(vaccineType, vaccinationSlot.getVaccineType());
        assertEquals(capacity, vaccinationSlot.getCapacity());
    }

}
