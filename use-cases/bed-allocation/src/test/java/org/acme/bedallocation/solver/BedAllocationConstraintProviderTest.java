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

import org.acme.bedallocation.domain.BedPlan;
import org.acme.bedallocation.domain.Gender;
import org.acme.bedallocation.domain.GenderLimitation;
import org.acme.bedallocation.domain.Stay;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class BedAllocationConstraintProviderTest {

    @Inject
    ConstraintVerifier<BedAllocationConstraintProvider, BedPlan> constraintVerifier;

    @Test
    void femaleInMaleRoom() {
        var room = aRoom("1").genderLimitation(GenderLimitation.MALE_ONLY);
        var bed = aBed("1-bed0").room(room);

        Stay genderAdmission = aStay("0", bed)
                .patientGender(Gender.FEMALE)
                .build();

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::femaleInMaleRoom)
                .given(genderAdmission)
                .penalizesBy(6);
    }

    @Test
    void maleInFemaleRoom() {
        var room = aRoom("1").genderLimitation(GenderLimitation.FEMALE_ONLY);
        var bed = aBed("1-bed0").room(room);

        Stay genderAdmission = aStay("0", bed)
                .patientGender(Gender.MALE)
                .build();

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::maleInFemaleRoom)
                .given(genderAdmission)
                .penalizesBy(6);
    }

    @Test
    void sameBedInSameNight() {
        var room = aRoom("1");
        var bed = aBed("1-bed0").room(room);

        Stay stay = aStay("0", bed).build();
        Stay sameBedAndNightsStay = aStay("2", bed).build();

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::sameBedInSameNight)
                .given(stay, sameBedAndNightsStay)
                .penalizesBy(6);
    }

    @Test
    void departmentMinimumAge() {
        var department = aDepartment("1").name("Adult department").minimumAge(18);
        var room = aRoom("1").department(department);
        var bed = aBed("1-bed0").room(room);

        Stay admission = aStay("0", bed).patientAge(5).build();

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::departmentMinimumAge)
                .given(admission, department.build())
                .penalizesBy(6);
    }

    @Test
    void departmentMaximumAge() {
        var department = aDepartment("2").name("Underage department").maximumAge(18);
        var room = aRoom("2").department(department);
        var bed = aBed("2-bed0").room(room);

        Stay admission = aStay("0", bed).patientAge(42).build();

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::departmentMaximumAge)
                .given(admission, department.build())
                .penalizesBy(6);
    }

    @Test
    void requiredPatientEquipment() {
        var room = aRoom("1").equipments(Set.of("TELEMETRY"));
        var bed = aBed("1-bed0").room(room);

        Stay admission = aStay("0", bed)
                .patientRequiredEquipments(List.of("TELEVISION", "TELEMETRY"))
                .build();

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::requiredPatientEquipment)
                .given(admission)
                .penalizesBy(6);
    }

    @Test
    void differentGenderInSameGenderRoomInSameNight() {
        var room = aRoom("1").genderLimitation(GenderLimitation.SAME_GENDER);

        // Assign female
        var bed1 = aBed("1-bed0").room(room);
        Stay stayFemale = aStay("0", bed1)
                .patientGender(Gender.FEMALE)
                .build();

        // Assign male
        var bed2 = aBed("1-bed1").room(room);
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
        var room = aRoom("1").capacity(6);
        var assignedBedInExceedCapacity = aBed("1-bed0").room(room);

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
        var room = aRoom("1").equipments(Set.of("TELEMETRY"));
        var bed = aBed("1-bed0").room(room);

        Stay stay = aStay("0", bed)
                .patientPreferredEquipments(List.of("TELEVISION", "TELEMETRY"))
                .build();

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::preferredPatientEquipment)
                .given(stay)
                .penalizesBy(6);
    }

    @Test
    void departmentSpecialty() {
        var department = aDepartment("0").specialtyToPriority(Map.of("spec1", 1));
        var roomInDep = aRoom("1").department(department);
        var bedInRoomInDep = aBed("1-bed0").room(roomInDep);

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
        var department = aDepartment("0").specialtyToPriority(Map.of("spec1", 2, "spec2", 1));
        var roomInDep = aRoom("1").department(department);
        var bedInDep = aBed("1-bed0").room(roomInDep);

        // Stay with 1st spec
        Stay stay1 = aStay("0", bedInDep).specialty("spec1").build();

        // Stay with 2nd spec
        Stay stay2 = aStay("1", bedInDep).specialty("spec2").build();

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::departmentSpecialtyNotFirstPriority)
                .given(stay1, stay2)
                .penalizesBy(6);
    }
}
