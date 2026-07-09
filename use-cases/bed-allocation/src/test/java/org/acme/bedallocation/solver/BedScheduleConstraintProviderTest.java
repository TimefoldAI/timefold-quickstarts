package org.acme.bedallocation.solver;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import ai.timefold.solver.core.api.score.stream.test.ConstraintVerifier;

import org.acme.bedallocation.domain.Bed;
import org.acme.bedallocation.domain.BedSchedule;
import org.acme.bedallocation.domain.Department;
import org.acme.bedallocation.domain.Gender;
import org.acme.bedallocation.domain.GenderLimitation;
import org.acme.bedallocation.domain.Room;
import org.acme.bedallocation.domain.Stay;
import org.junit.jupiter.api.Test;

class BedScheduleConstraintProviderTest {

    private static final LocalDate ZERO_NIGHT = LocalDate.of(2021, 2, 1);
    private static final LocalDate FIVE_NIGHT = ZERO_NIGHT.plusDays(5);

    private static final String DEFAULT_SPECIALTY = "default";

    private final ConstraintVerifier<BedScheduleConstraintProvider, BedSchedule> constraintVerifier =
            ConstraintVerifier.build(new BedScheduleConstraintProvider(), BedSchedule.class, Stay.class);

    @Test
    void femaleInMaleRoom() {
        Room room = new Room();
        room.setGenderLimitation(GenderLimitation.MALE_ONLY);

        Bed bed = new Bed();
        bed.setRoom(room);

        Stay genderAdmission = new Stay("0", ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALTY, bed);
        genderAdmission.setPatientGender(Gender.FEMALE);

        constraintVerifier.verifyThat(BedScheduleConstraintProvider::femaleInMaleRoom)
                .given(genderAdmission)
                .penalizesBy(6);
    }

    @Test
    void maleInFemaleRoom() {
        Room room = new Room();
        room.setGenderLimitation(GenderLimitation.FEMALE_ONLY);

        Bed bed = new Bed();
        bed.setRoom(room);

        Stay genderAdmission = new Stay("0", ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALTY, bed);
        genderAdmission.setPatientGender(Gender.MALE);

        constraintVerifier.verifyThat(BedScheduleConstraintProvider::maleInFemaleRoom)
                .given(genderAdmission)
                .penalizesBy(6);
    }

    @Test
    void sameBedInSameNight() {
        Bed bed = new Bed("1");

        Stay stay = new Stay("0", ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALTY, bed);
        Stay sameBedAndNightsStay = new Stay("2", ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALTY, bed);

        constraintVerifier.verifyThat(BedScheduleConstraintProvider::sameBedInSameNight)
                .given(stay, sameBedAndNightsStay)
                .penalizesBy(6);
    }

    @Test
    void departmentMinimumAge() {
        Department department = new Department("1", "Adult department");
        department.setMinimumAge(18);

        Room room = new Room();
        room.setDepartment(department);

        Bed bed = new Bed();
        bed.setRoom(room);

        Stay admission = new Stay("0", ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALTY, bed);
        admission.setPatientAge(5);

        constraintVerifier.verifyThat(BedScheduleConstraintProvider::departmentMinimumAge)
                .given(admission, department)
                .penalizesBy(6);
    }

    @Test
    void departmentMaximumAge() {
        Department department = new Department("2", "Underage department");
        department.setMaximumAge(18);

        Room room = new Room();
        room.setDepartment(department);

        Bed bed = new Bed();
        bed.setRoom(room);

        Stay admission = new Stay("0", ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALTY, bed);
        admission.setPatientAge(42);

        constraintVerifier.verifyThat(BedScheduleConstraintProvider::departmentMaximumAge)
                .given(admission, department)
                .penalizesBy(6);
    }

    @Test
    void requiredPatientEquipment() {
        Room room = new Room();
        room.setEquipments(List.of("TELEMETRY"));

        Bed bed = new Bed();
        bed.setRoom(room);

        Stay admission = new Stay("0", ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALTY, bed);
        admission.setPatientRequiredEquipments(List.of("TELEVISION", "TELEMETRY"));

        constraintVerifier.verifyThat(BedScheduleConstraintProvider::requiredPatientEquipment)
                .given(admission)
                .penalizesBy(6);
    }

    @Test
    void differentGenderInSameGenderRoomInSameNight() {
        Room room = new Room("1");
        room.setGenderLimitation(GenderLimitation.SAME_GENDER);

        Bed bed1 = new Bed();
        bed1.setRoom(room);

        Stay stayFemale = new Stay("0", ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALTY, bed1);
        stayFemale.setPatientGender(Gender.FEMALE);

        Bed bed2 = new Bed();
        bed2.setRoom(room);

        Stay stayMale = new Stay("1", ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALTY, bed2);
        stayMale.setPatientGender(Gender.MALE);

        constraintVerifier
                .verifyThat(BedScheduleConstraintProvider::differentGenderInSameGenderRoomInSameNight)
                .given(stayFemale, stayMale)
                .penalizesBy(6);
    }

    @Test
    void assignEveryPatientToABed() {
        Stay stay = new Stay("0", ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALTY, null);

        constraintVerifier
                .verifyThat(BedScheduleConstraintProvider::assignEveryPatientToABed)
                .given(stay)
                .penalizesBy(6);
    }

    @Test
    void preferredMaximumRoomCapacity() {
        Room room = new Room();
        room.setCapacity(6);

        Bed assignedBedInExceedCapacity = new Bed();
        assignedBedInExceedCapacity.setRoom(room);

        Stay stay = new Stay("0", ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALTY, assignedBedInExceedCapacity);
        stay.setPatientPreferredMaximumRoomCapacity(3);

        constraintVerifier
                .verifyThat(BedScheduleConstraintProvider::preferredMaximumRoomCapacity)
                .given(stay)
                .penalizesBy(6);
    }

    @Test
    void preferredPatientEquipment() {
        Room room = new Room();
        room.setEquipments(List.of("TELEMETRY"));

        Bed bed = new Bed();
        bed.setRoom(room);

        Stay stay = new Stay("0", ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALTY, bed);
        stay.setPatientPreferredEquipments(List.of("TELEVISION", "TELEMETRY"));

        constraintVerifier.verifyThat(BedScheduleConstraintProvider::preferredPatientEquipment)
                .given(stay)
                .penalizesBy(6);
    }

    @Test
    void departmentSpecialty() {
        Department department = new Department("0", "0");
        department.setSpecialtyToPriority(Map.of("spec1", 1));

        Room roomInDep = new Room();
        roomInDep.setDepartment(department);

        Bed bedInRoomInDep = new Bed();
        bedInRoomInDep.setRoom(roomInDep);

        Stay staySpec1 = new Stay("0", ZERO_NIGHT, FIVE_NIGHT, "spec1", bedInRoomInDep);
        Stay staySpec2 = new Stay("1", ZERO_NIGHT, FIVE_NIGHT, "spec2", bedInRoomInDep);

        constraintVerifier.verifyThat(BedScheduleConstraintProvider::departmentSpecialty)
                .given(staySpec1, staySpec2)
                .penalizesBy(6);
    }

    @Test
    void departmentSpecialtyNotFirstPriority() {
        Department department = new Department("0", "0");
        department.setSpecialtyToPriority(Map.of("spec1", 2, "spec2", 1));

        Room roomInDep = new Room("1");
        roomInDep.setDepartment(department);
        department.addRoom(roomInDep);

        Bed bedInDep = new Bed();
        bedInDep.setRoom(roomInDep);

        Stay stay1 = new Stay("0", ZERO_NIGHT, FIVE_NIGHT, "spec1", bedInDep);
        Stay stay2 = new Stay("1", ZERO_NIGHT, FIVE_NIGHT, "spec2", bedInDep);

        constraintVerifier.verifyThat(BedScheduleConstraintProvider::departmentSpecialtyNotFirstPriority)
                .given(stay1, stay2)
                .penalizesBy(6);
    }
}
