package org.acme.bedallocation.solver;

import static org.acme.bedallocation.support.TestHelper.aBed;
import static org.acme.bedallocation.support.TestHelper.aDepartment;
import static org.acme.bedallocation.support.TestHelper.aRoom;
import static org.acme.bedallocation.support.TestHelper.aStay;

import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.score.stream.test.ConstraintVerifier;

import org.acme.bedallocation.domain.Bed;
import org.acme.bedallocation.domain.BedPlan;
import org.acme.bedallocation.domain.Department;
import org.acme.bedallocation.domain.Gender;
import org.acme.bedallocation.domain.GenderLimitation;
import org.acme.bedallocation.domain.Room;
import org.acme.bedallocation.domain.Stay;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class BedAllocationConstraintProviderTest {

    @Inject
    ConstraintVerifier<BedAllocationConstraintProvider, BedPlan> constraintVerifier;

    @Test
    void femaleInMaleRoom() {
        Room room = aRoom("1").genderLimitation(GenderLimitation.MALE_ONLY).build();
        Bed bed = aBed("1-bed0").room(room).build();

        Stay genderAdmission = aStay("0", bed)
                .patientGender(Gender.FEMALE)
                .build();

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::femaleInMaleRoom)
                .given(genderAdmission)
                .penalizesBy(6);
    }

    @Test
    void maleInFemaleRoom() {
        Room room = aRoom("1").genderLimitation(GenderLimitation.FEMALE_ONLY).build();
        Bed bed = aBed("1-bed0").room(room).build();

        Stay genderAdmission = aStay("0", bed)
                .patientGender(Gender.MALE)
                .build();

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::maleInFemaleRoom)
                .given(genderAdmission)
                .penalizesBy(6);
    }

    @Test
    void sameBedInSameNight() {
        Room room = aRoom("1").build();
        Bed bed = aBed("1-bed0").room(room).build();

        Stay stay = aStay("0", bed).build();
        Stay sameBedAndNightsStay = aStay("2", bed).build();

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::sameBedInSameNight)
                .given(stay, sameBedAndNightsStay)
                .penalizesBy(6);
    }

    @Test
    void departmentMinimumAge() {
        Department department = aDepartment("1").name("Adult department").minimumAge(18).build();
        Room room = aRoom("1").department(department).build();
        Bed bed = aBed("1-bed0").room(room).build();

        Stay admission = aStay("0", bed).patientAge(5).build();

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::departmentMinimumAge)
                .given(admission, department)
                .penalizesBy(6);
    }

    @Test
    void departmentMaximumAge() {
        Department department = aDepartment("2").name("Underage department").maximumAge(18).build();
        Room room = aRoom("2").department(department).build();
        Bed bed = aBed("2-bed0").room(room).build();

        Stay admission = aStay("0", bed).patientAge(42).build();

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::departmentMaximumAge)
                .given(admission, department)
                .penalizesBy(6);
    }

    @Test
    void requiredPatientEquipment() {
        Room room = aRoom("1").equipments(Set.of("TELEMETRY")).build();
        Bed bed = aBed("1-bed0").room(room).build();

        Stay admission = aStay("0", bed)
                .patientRequiredEquipments(List.of("TELEVISION", "TELEMETRY"))
                .build();

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::requiredPatientEquipment)
                .given(admission)
                .penalizesBy(6);
    }

    @Test
    void differentGenderInSameGenderRoomInSameNight() {
        Room room = aRoom("1").genderLimitation(GenderLimitation.SAME_GENDER).build();

        // Assign female
        Bed bed1 = aBed("1-bed0").room(room).build();
        Stay stayFemale = aStay("0", bed1)
                .patientGender(Gender.FEMALE)
                .build();

        // Assign male
        Bed bed2 = aBed("1-bed1").room(room).build();
        Stay stayMale = aStay("1", bed2)
                .patientGender(Gender.MALE)
                .build();

        constraintVerifier
                .verifyThat(BedAllocationConstraintProvider::differentGenderInSameGenderRoomInSameNight)
                .given(stayFemale, stayMale)
                .penalizesBy(6);
    }

    @Test
    void assignEveryPatientToABed() {
        Stay stay = aStay("0", null).build();

        constraintVerifier
                .verifyThat(BedAllocationConstraintProvider::assignEveryPatientToABed)
                .given(stay)
                .penalizesBy(6);
    }

    @Test
    void preferredMaximumRoomCapacity() {
        Room room = aRoom("1").capacity(6).build();
        Bed assignedBedInExceedCapacity = aBed("1-bed0").room(room).build();

        Stay stay = aStay("0", assignedBedInExceedCapacity)
                .patientPreferredMaximumRoomCapacity(3)
                .build();

        constraintVerifier
                .verifyThat(BedAllocationConstraintProvider::preferredMaximumRoomCapacity)
                .given(stay)
                .penalizesBy(6);
    }

    @Test
    void preferredPatientEquipment() {
        Room room = aRoom("1").equipments(Set.of("TELEMETRY")).build();
        Bed bed = aBed("1-bed0").room(room).build();

        Stay stay = aStay("0", bed)
                .patientPreferredEquipments(List.of("TELEVISION", "TELEMETRY"))
                .build();

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::preferredPatientEquipment)
                .given(stay)
                .penalizesBy(6);
    }

    @Test
    void departmentSpecialty() {
        Department department = aDepartment("0").specialtyToPriority(Map.of("spec1", 1)).build();
        Room roomInDep = aRoom("1").department(department).build();
        Bed bedInRoomInDep = aBed("1-bed0").room(roomInDep).build();

        // Stay with 1st spec
        Stay staySpec1 = aStay("0", bedInRoomInDep).specialty("spec1").build();

        // Stay with 2nd spec
        Stay staySpec2 = aStay("1", bedInRoomInDep).specialty("spec2").build();

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::departmentSpecialty)
                .given(staySpec1, staySpec2)
                .penalizesBy(6);
    }

    @Test
    void departmentSpecialtyNotFirstPriorityConstraint() {
        Department department = aDepartment("0").specialtyToPriority(Map.of("spec1", 2, "spec2", 1)).build();
        Room roomInDep = aRoom("1").department(department).build();
        Bed bedInDep = aBed("1-bed0").room(roomInDep).build();

        // Stay with 1st spec
        Stay stay1 = aStay("0", bedInDep).specialty("spec1").build();

        // Stay with 2nd spec
        Stay stay2 = aStay("1", bedInDep).specialty("spec2").build();

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::departmentSpecialtyNotFirstPriority)
                .given(stay1, stay2)
                .penalizesBy(6);
    }
}
