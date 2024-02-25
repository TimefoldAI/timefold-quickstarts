package org.acme.bedallocation.solver;

import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.acme.bedallocation.domain.AdmissionPart;
import org.acme.bedallocation.domain.Bed;
import org.acme.bedallocation.domain.BedAllocationSchedule;
import org.acme.bedallocation.domain.Equipment;
import org.acme.bedallocation.domain.Night;
import org.acme.bedallocation.domain.Patient;
import org.acme.bedallocation.domain.BedDesignation;
import org.acme.bedallocation.domain.Department;
import org.acme.bedallocation.domain.DepartmentSpecialism;
import org.acme.bedallocation.domain.Gender;
import org.acme.bedallocation.domain.GenderLimitation;
import org.acme.bedallocation.domain.PreferredPatientEquipment;
import org.acme.bedallocation.domain.RequiredPatientEquipment;
import org.acme.bedallocation.domain.Room;
import org.acme.bedallocation.domain.RoomEquipment;
import org.acme.bedallocation.domain.RoomSpecialism;
import org.acme.bedallocation.domain.Specialism;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class BedAllocationConstraintProviderTest {

    private static final Night ZERO_NIGHT = new Night("0", 0);
    private static final Night FIVE_NIGHT = new Night("5", 5);

    private static final Specialism DEFAULT_SPECIALISM = new Specialism();

    @Inject
    ConstraintVerifier<BedAllocationConstraintProvider, BedAllocationSchedule> constraintVerifier;

    @Test
    void femaleInMaleRoom() {
        Room room = new Room();
        room.setGenderLimitation(GenderLimitation.MALE_ONLY);

        Bed bed = new Bed();
        bed.setRoom(room);

        Patient patient = new Patient();
        patient.setGender(Gender.FEMALE);

        AdmissionPart genderAdmission = new AdmissionPart("0", patient, ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALISM);
        BedDesignation genderLimitationDesignation = new BedDesignation("0", genderAdmission, bed);

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::femaleInMaleRoom)
                .given(genderLimitationDesignation)
                .penalizesBy(6);
    }

    @Test
    void maleInFemaleRoom() {
        Room room = new Room();
        room.setGenderLimitation(GenderLimitation.FEMALE_ONLY);

        Bed bed = new Bed();
        bed.setRoom(room);

        Patient patient = new Patient();
        patient.setGender(Gender.MALE);

        AdmissionPart genderAdmission = new AdmissionPart("0", patient, ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALISM);
        BedDesignation genderLimitationDesignation = new BedDesignation("0", genderAdmission, bed);

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::maleInFemaleRoom)
                .given(genderLimitationDesignation)
                .penalizesBy(6);
    }

    @Test
    void sameBedInSameNight() {

        Patient patient = new Patient();
        Bed bed = new Bed();

        AdmissionPart admissionPart = new AdmissionPart("0", patient, ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALISM);
        BedDesignation designation = new BedDesignation("1", admissionPart, bed);

        BedDesignation sameBedAndNightsDesignation = new BedDesignation("2", admissionPart, bed);

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::sameBedInSameNight)
                .given(designation, sameBedAndNightsDesignation)
                .penalizesBy(6);
    }

    @Test
    void departmentMinimumAge() {
        Department department = new Department("1", "Adult department");
        department.setMinimumAge(18);

        Room room = new Room();
        room.setDepartment(department);

        Patient patient = new Patient();
        patient.setAge(5);

        Bed bed = new Bed();
        bed.setRoom(room);

        AdmissionPart admission = new AdmissionPart("0", patient, ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALISM);
        BedDesignation designation = new BedDesignation("0", admission, bed);

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::departmentMinimumAge)
                .given(designation, department)
                .penalizesBy(6);
    }

    @Test
    void departmentMaximumAge() {
        Department department = new Department("2", "Underage department");
        department.setMaximumAge(18);

        Room room = new Room();
        room.setDepartment(department);

        Patient patient = new Patient();
        patient.setAge(42);

        Bed bed = new Bed();
        bed.setRoom(room);

        AdmissionPart admission = new AdmissionPart("0", patient, ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALISM);
        BedDesignation designation = new BedDesignation("0", admission, bed);

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::departmentMaximumAge)
                .given(designation, department)
                .penalizesBy(6);
    }

    @Test
    void requiredPatientEquipment() {
        Patient patient = new Patient();
        Room room = new Room();

        Equipment equipment1 = new Equipment();
        Equipment equipment2 = new Equipment();

        Bed bed = new Bed();
        bed.setRoom(room);

        AdmissionPart admission = new AdmissionPart("0", patient, ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALISM);
        BedDesignation designation = new BedDesignation("0", admission, bed);

        //ReqPatientEq1
        RequiredPatientEquipment requiredPatientEquipment1 = new RequiredPatientEquipment();
        requiredPatientEquipment1.setPatient(patient);
        requiredPatientEquipment1.setEquipment(equipment1);
        //ReqPatientEq2
        RequiredPatientEquipment requiredPatientEquipment2 = new RequiredPatientEquipment();
        requiredPatientEquipment2.setPatient(patient);
        requiredPatientEquipment2.setEquipment(equipment2);
        //RoomEquipment
        RoomEquipment roomEquipment = new RoomEquipment();
        roomEquipment.setEquipment(equipment2);
        roomEquipment.setRoom(room);

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::requiredPatientEquipment)
                .given(requiredPatientEquipment1, requiredPatientEquipment2, roomEquipment, designation)
                .penalizesBy(6);
    }

    @Test
    void differentGenderInSameGenderRoomInSameNight() {

        Room room = new Room();
        room.setGenderLimitation(GenderLimitation.SAME_GENDER);

        //Assign female
        Patient female = new Patient();
        female.setGender(Gender.FEMALE);

        Bed bed1 = new Bed();
        bed1.setRoom(room);

        AdmissionPart admissionPartFemale = new AdmissionPart("0", female, ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALISM);
        BedDesignation bedDesignationFemale = new BedDesignation("1", admissionPartFemale, bed1);

        //Assign male
        Patient male = new Patient();
        male.setGender(Gender.MALE);

        Bed bed2 = new Bed();
        bed2.setRoom(room);

        AdmissionPart admissionPartMale = new AdmissionPart("1", male, ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALISM);
        BedDesignation bedDesignationMale = new BedDesignation("2", admissionPartMale, bed2);

        constraintVerifier
                .verifyThat(BedAllocationConstraintProvider::differentGenderInSameGenderRoomInSameNight)
                .given(bedDesignationFemale, bedDesignationMale)
                .penalizesBy(6);
    }

    @Test
    void assignEveryPatientToABed() {

        Patient patient = new Patient();

        AdmissionPart admissionPart = new AdmissionPart("0", patient, ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALISM);
        BedDesignation bedUnassignedDesignation = new BedDesignation("2", admissionPart, null);

        constraintVerifier
                .verifyThat(BedAllocationConstraintProvider::assignEveryPatientToABed)
                .given(bedUnassignedDesignation)
                .penalizesBy(6);
    }

    @Test
    void preferredMaximumRoomCapacity() {

        Patient patientWithRoomPreferences = new Patient();
        patientWithRoomPreferences.setPreferredMaximumRoomCapacity(3);

        Room room = new Room();
        room.setCapacity(6);

        Bed assignedBedInExceedCapacity = new Bed();
        assignedBedInExceedCapacity.setRoom(room);

        AdmissionPart admissionPart =
                new AdmissionPart("0", patientWithRoomPreferences, ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALISM);
        BedDesignation bedDesignation = new BedDesignation("0", admissionPart, assignedBedInExceedCapacity);

        constraintVerifier
                .verifyThat(BedAllocationConstraintProvider::preferredMaximumRoomCapacity)
                .given(bedDesignation)
                .penalizesBy(6);
    }

    @Test
    void preferredPatientEquipment() {

        Patient patient = new Patient();

        Room room = new Room();

        Bed bed = new Bed();
        bed.setRoom(room);

        AdmissionPart admissionPart = new AdmissionPart("0", patient, ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALISM);
        BedDesignation bedDesignation = new BedDesignation("0", admissionPart, bed);

        Equipment equipment1 = new Equipment();
        Equipment equipment2 = new Equipment();

        PreferredPatientEquipment preferredPatientEquipment1 = new PreferredPatientEquipment();
        preferredPatientEquipment1.setEquipment(equipment1);
        preferredPatientEquipment1.setPatient(patient);

        PreferredPatientEquipment preferredPatientEquipment2 = new PreferredPatientEquipment();
        preferredPatientEquipment2.setEquipment(equipment2);
        preferredPatientEquipment2.setPatient(patient);

        RoomEquipment roomEquippedOnlyByOneEq = new RoomEquipment();
        roomEquippedOnlyByOneEq.setEquipment(equipment2);
        roomEquippedOnlyByOneEq.setRoom(room);

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::preferredPatientEquipment)
                .given(preferredPatientEquipment1, preferredPatientEquipment2, roomEquippedOnlyByOneEq, bedDesignation)
                .penalizesBy(6);
    }

    @Test
    void departmentSpecialism() {

        Patient patient = new Patient();

        Department department = new Department();

        Room roomInDep = new Room();
        roomInDep.setDepartment(department);

        Bed bedInRoomInDep = new Bed();
        bedInRoomInDep.setRoom(roomInDep);

        //Designation with 1st spec
        Specialism spec1 = new Specialism();

        AdmissionPart admissionPartSpec1 = new AdmissionPart("0", patient, ZERO_NIGHT, FIVE_NIGHT, spec1);
        BedDesignation designationWithDepartmentSpecialism1 = new BedDesignation("0", admissionPartSpec1, bedInRoomInDep);

        //Designation with 2nd spec
        Specialism spec2 = new Specialism();

        AdmissionPart admissionPartSpec2 = new AdmissionPart("1", patient, ZERO_NIGHT, FIVE_NIGHT, spec2);
        BedDesignation designationWithDepartmentSpecialism2 = new BedDesignation("1", admissionPartSpec2, bedInRoomInDep);

        DepartmentSpecialism departmentSpecialismWithOneSpec = new DepartmentSpecialism();
        departmentSpecialismWithOneSpec.setDepartment(department);
        departmentSpecialismWithOneSpec.setSpecialism(spec1);

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::departmentSpecialism)
                .given(designationWithDepartmentSpecialism1, designationWithDepartmentSpecialism2,
                        departmentSpecialismWithOneSpec)
                .penalizesBy(6);
    }

    @Test
    void roomSpecialism() {

        Patient patient = new Patient();

        Room roomInDep = new Room();
        Bed bedInDep = new Bed();
        bedInDep.setRoom(roomInDep);

        //Designation with 1st spec
        Specialism spec1 = new Specialism();
        AdmissionPart admissionPart = new AdmissionPart("0", patient, ZERO_NIGHT, FIVE_NIGHT, spec1);
        BedDesignation designationWithRoomSpecialism1 = new BedDesignation("0", admissionPart, bedInDep);

        //Designation with 2nd spec
        Specialism spec2 = new Specialism();
        AdmissionPart admissionPart2 = new AdmissionPart("1", patient, ZERO_NIGHT, FIVE_NIGHT, spec2);
        BedDesignation designationWithRoomSpecialism2 = new BedDesignation("1", admissionPart2, bedInDep);

        RoomSpecialism roomSpecialism = new RoomSpecialism();
        roomSpecialism.setRoom(roomInDep);
        roomSpecialism.setSpecialism(spec1);

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::roomSpecialismNotExists)
                .given(designationWithRoomSpecialism1, designationWithRoomSpecialism2, roomSpecialism)
                .penalizesBy(6);
    }

    @Test
    void roomSpecialismNotFirstPriorityConstraint() {

        Patient patient = new Patient();

        Room roomInDep = new Room();
        Bed bedInDep = new Bed();

        bedInDep.setRoom(roomInDep);
        //Designation with 1st spec
        Specialism spec1 = new Specialism();
        AdmissionPart admissionPart1 = new AdmissionPart("0", patient, ZERO_NIGHT, FIVE_NIGHT, spec1);
        BedDesignation designationWithRoomSpecialism1 = new BedDesignation("0", admissionPart1, bedInDep);

        //Designation with 2nd spec
        Specialism spec2 = new Specialism();
        AdmissionPart admissionPart2 = new AdmissionPart("1", patient, ZERO_NIGHT, FIVE_NIGHT, spec2);
        BedDesignation designationWithRoomSpecialism2 = new BedDesignation("1", admissionPart2, bedInDep);

        RoomSpecialism roomSpecialism = new RoomSpecialism();
        roomSpecialism.setRoom(roomInDep);
        roomSpecialism.setSpecialism(spec1);
        roomSpecialism.setPriority(2);

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::roomSpecialismNotFirstPriority)
                .given(designationWithRoomSpecialism1, designationWithRoomSpecialism2, roomSpecialism)
                .penalizesBy(6);
    }

}
