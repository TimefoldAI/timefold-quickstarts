package org.acme.bedallocation.solver;

import static org.acme.bedallocation.support.TestBedBuilder.aBed;
import static org.acme.bedallocation.support.TestDepartmentBuilder.aDepartment;
import static org.acme.bedallocation.support.TestRoomBuilder.aRoom;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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

    private static final LocalDate ZERO_NIGHT = LocalDate.of(2021, 2, 1);
    private static final LocalDate FIVE_NIGHT = ZERO_NIGHT.plusDays(5);

    private static final String DEFAULT_SPECIALTY = "default";

    @Inject
    ConstraintVerifier<BedAllocationConstraintProvider, BedPlan> constraintVerifier;

    @Test
    void femaleInMaleRoom() {
        Room room = aRoom("1").genderLimitation(GenderLimitation.MALE_ONLY).build();
        Bed bed = aBed("1-bed0").room(room).build();

        Stay genderAdmission = new Stay("0", ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALTY, bed);
        genderAdmission.setPatientGender(Gender.FEMALE);

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::femaleInMaleRoom)
                .given(genderAdmission)
                .penalizesBy(6);
    }

    @Test
    void maleInFemaleRoom() {
        Room room = aRoom("1").genderLimitation(GenderLimitation.FEMALE_ONLY).build();
        Bed bed = aBed("1-bed0").room(room).build();

        Stay genderAdmission = new Stay("0", ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALTY, bed);
        genderAdmission.setPatientGender(Gender.MALE);

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::maleInFemaleRoom)
                .given(genderAdmission)
                .penalizesBy(6);
    }

    @Test
    void sameBedInSameNight() {
        Room room = aRoom("1").build();
        Bed bed = aBed("1-bed0").room(room).build();

        Stay stay = new Stay("0", ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALTY, bed);
        Stay sameBedAndNightsStay = new Stay("2", ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALTY, bed);

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::sameBedInSameNight)
                .given(stay, sameBedAndNightsStay)
                .penalizesBy(6);
    }

    @Test
    void departmentMinimumAge() {
        Department department = aDepartment("1").name("Adult department").minimumAge(18).build();
        Room room = aRoom("1").department(department).build();
        Bed bed = aBed("1-bed0").room(room).build();

        Stay admission = new Stay("0", ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALTY, bed);
        admission.setPatientAge(5);

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::departmentMinimumAge)
                .given(admission, department)
                .penalizesBy(6);
    }

    @Test
    void departmentMaximumAge() {
        Department department = aDepartment("2").name("Underage department").maximumAge(18).build();
        Room room = aRoom("2").department(department).build();
        Bed bed = aBed("2-bed0").room(room).build();

        Stay admission = new Stay("0", ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALTY, bed);
        admission.setPatientAge(42);

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::departmentMaximumAge)
                .given(admission, department)
                .penalizesBy(6);
    }

    @Test
    void requiredPatientEquipment() {
        Room room = aRoom("1").equipments(List.of("TELEMETRY")).build();
        Bed bed = aBed("1-bed0").room(room).build();

        Stay admission = new Stay("0", ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALTY, bed);
        admission.setPatientRequiredEquipments(List.of("TELEVISION", "TELEMETRY"));

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::requiredPatientEquipment)
                .given(admission)
                .penalizesBy(6);
    }

    @Test
    void differentGenderInSameGenderRoomInSameNight() {
        Room room = aRoom("1").genderLimitation(GenderLimitation.SAME_GENDER).build();

        // Assign female
        Bed bed1 = aBed("1-bed0").room(room).build();
        Stay stayFemale = new Stay("0", ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALTY, bed1);
        stayFemale.setPatientGender(Gender.FEMALE);

        // Assign male
        Bed bed2 = aBed("1-bed1").room(room).build();
        Stay stayMale = new Stay("1", ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALTY, bed2);
        stayMale.setPatientGender(Gender.MALE);

        constraintVerifier
                .verifyThat(BedAllocationConstraintProvider::differentGenderInSameGenderRoomInSameNight)
                .given(stayFemale, stayMale)
                .penalizesBy(6);
    }

    @Test
    void assignEveryPatientToABed() {
        Stay stay = new Stay("0", ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALTY, null);

        constraintVerifier
                .verifyThat(BedAllocationConstraintProvider::assignEveryPatientToABed)
                .given(stay)
                .penalizesBy(6);
    }

    @Test
    void preferredMaximumRoomCapacity() {
        Room room = aRoom("1").capacity(6).build();
        Bed assignedBedInExceedCapacity = aBed("1-bed0").room(room).build();

        Stay stay = new Stay("0", ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALTY, assignedBedInExceedCapacity);
        stay.setPatientPreferredMaximumRoomCapacity(3);

        constraintVerifier
                .verifyThat(BedAllocationConstraintProvider::preferredMaximumRoomCapacity)
                .given(stay)
                .penalizesBy(6);
    }

    @Test
    void preferredPatientEquipment() {
        Room room = aRoom("1").equipments(List.of("TELEMETRY")).build();
        Bed bed = aBed("1-bed0").room(room).build();

        Stay stay = new Stay("0", ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALTY, bed);
        stay.setPatientPreferredEquipments(List.of("TELEVISION", "TELEMETRY"));

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
        Stay staySpec1 = new Stay("0", ZERO_NIGHT, FIVE_NIGHT, "spec1", bedInRoomInDep);

        // Stay with 2nd spec
        Stay staySpec2 = new Stay("1", ZERO_NIGHT, FIVE_NIGHT, "spec2", bedInRoomInDep);

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
        Stay stay1 = new Stay("0", ZERO_NIGHT, FIVE_NIGHT, "spec1", bedInDep);

        // Stay with 2nd spec
        Stay stay2 = new Stay("1", ZERO_NIGHT, FIVE_NIGHT, "spec2", bedInDep);

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::departmentSpecialtyNotFirstPriority)
                .given(stay1, stay2)
                .penalizesBy(6);
    }
}
