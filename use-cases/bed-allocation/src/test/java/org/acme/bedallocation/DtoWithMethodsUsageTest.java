package org.acme.bedallocation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.acme.bedallocation.dto.BedDTO;
import org.acme.bedallocation.dto.BedIdDetail;
import org.acme.bedallocation.dto.BedScheduleConfigOverrides;
import org.acme.bedallocation.dto.BedScheduleInput;
import org.acme.bedallocation.dto.BedScheduleInputMetrics;
import org.acme.bedallocation.dto.BedScheduleOutput;
import org.acme.bedallocation.dto.BedScheduleOutputMetrics;
import org.acme.bedallocation.dto.DepartmentDTO;
import org.acme.bedallocation.dto.RoomDTO;
import org.acme.bedallocation.dto.StayDTO;
import org.acme.bedallocation.dto.StayIdDetail;
import org.junit.jupiter.api.Test;

class DtoWithMethodsUsageTest {

    @Test
    void allWithMethodsProduceUpdatedCopies() {
        Integer noAgeLimit = null;
        var baseDepartment = new DepartmentDTO("d1", "Dep", noAgeLimit, noAgeLimit, Map.of("spec", 1));
        var updatedDepartment = baseDepartment
                .withId("d2")
                .withName("Other dep")
                .withMinimumAge(10)
                .withMaximumAge(90)
                .withSpecialtyToPriority(Map.of("spec2", 2));

        var baseRoom = new RoomDTO("r1", "Room", "d1", 2, "ANY_GENDER", List.of("telemetry"));
        var updatedRoom = baseRoom
                .withId("r2")
                .withName("Other room")
                .withDepartmentId("d2")
                .withCapacity(4)
                .withGenderLimitation("MALE_ONLY")
                .withEquipments(List.of("oxygen"));

        var baseBed = new BedDTO("b1", "r1", 0);
        var updatedBed = baseBed.withId("b2").withRoomId("r2").withIndexInRoom(1);

        var baseStay = new StayDTO("s1", "Patient", "MALE", 40, noAgeLimit, List.of(), List.of(),
                "2024-01-01", "2024-01-03", "spec", "");
        var updatedStay = baseStay
                .withId("s2")
                .withPatientName("Other patient")
                .withPatientGender("FEMALE")
                .withPatientAge(50)
                .withPatientPreferredMaximumRoomCapacity(2)
                .withPatientRequiredEquipments(List.of("telemetry"))
                .withPatientPreferredEquipments(List.of("television"))
                .withArrivalDate("2024-02-01")
                .withDepartureDate("2024-02-05")
                .withSpecialty("spec2")
                .withBedId("b2");

        var updatedStayIdDetail = new StayIdDetail("s1").withStayId("s2");
        var updatedBedIdDetail = new BedIdDetail("b1").withBedId("b2");

        var updatedOverrides = new BedScheduleConfigOverrides()
                .withPreferredMaximumRoomCapacityWeight(11L)
                .withDepartmentSpecialtyWeight(22L)
                .withDepartmentSpecialtyNotFirstPriorityWeight(33L)
                .withPreferredPatientEquipmentWeight(44L);

        var updatedInput = new BedScheduleInput(List.of(baseDepartment), List.of(baseRoom), List.of(baseBed),
                List.of(baseStay))
                .withDepartments(List.of(updatedDepartment))
                .withRooms(List.of(updatedRoom))
                .withBeds(List.of(updatedBed))
                .withStays(List.of(updatedStay));

        var updatedOutput = new BedScheduleOutput(List.of(baseDepartment), List.of(baseRoom), List.of(baseBed),
                List.of(baseStay), "0hard/0soft")
                .withDepartments(List.of(updatedDepartment))
                .withRooms(List.of(updatedRoom))
                .withBeds(List.of(updatedBed))
                .withStays(List.of(updatedStay))
                .withScore("1hard/0soft");

        var updatedInputMetrics = new BedScheduleInputMetrics(1, 2, 3)
                .withStays(10)
                .withBeds(20)
                .withRooms(30);

        var updatedOutputMetrics = new BedScheduleOutputMetrics(1, 2, 3)
                .withTotalAssignedStays(10)
                .withTotalUnassignedStays(20)
                .withTotalUsedRooms(30);

        assertThat(updatedDepartment.id()).isEqualTo("d2");
        assertThat(updatedDepartment.name()).isEqualTo("Other dep");
        assertThat(updatedDepartment.minimumAge()).isEqualTo(10);
        assertThat(updatedDepartment.maximumAge()).isEqualTo(90);
        assertThat(updatedDepartment.specialtyToPriority()).containsEntry("spec2", 2);
        assertThat(updatedRoom.id()).isEqualTo("r2");
        assertThat(updatedRoom.name()).isEqualTo("Other room");
        assertThat(updatedRoom.departmentId()).isEqualTo("d2");
        assertThat(updatedRoom.capacity()).isEqualTo(4);
        assertThat(updatedRoom.genderLimitation()).isEqualTo("MALE_ONLY");
        assertThat(updatedRoom.equipments()).containsExactly("oxygen");
        assertThat(updatedBed.id()).isEqualTo("b2");
        assertThat(updatedBed.roomId()).isEqualTo("r2");
        assertThat(updatedBed.indexInRoom()).isEqualTo(1);
        assertThat(updatedStay.id()).isEqualTo("s2");
        assertThat(updatedStay.patientName()).isEqualTo("Other patient");
        assertThat(updatedStay.patientGender()).isEqualTo("FEMALE");
        assertThat(updatedStay.patientAge()).isEqualTo(50);
        assertThat(updatedStay.patientPreferredMaximumRoomCapacity()).isEqualTo(2);
        assertThat(updatedStay.patientRequiredEquipments()).containsExactly("telemetry");
        assertThat(updatedStay.patientPreferredEquipments()).containsExactly("television");
        assertThat(updatedStay.arrivalDate()).isEqualTo("2024-02-01");
        assertThat(updatedStay.departureDate()).isEqualTo("2024-02-05");
        assertThat(updatedStay.specialty()).isEqualTo("spec2");
        assertThat(updatedStay.bedId()).isEqualTo("b2");
        assertThat(updatedStayIdDetail.stayId()).isEqualTo("s2");
        assertThat(updatedBedIdDetail.bedId()).isEqualTo("b2");
        assertThat(updatedOverrides.preferredMaximumRoomCapacityWeight()).isEqualTo(11L);
        assertThat(updatedOverrides.departmentSpecialtyWeight()).isEqualTo(22L);
        assertThat(updatedOverrides.departmentSpecialtyNotFirstPriorityWeight()).isEqualTo(33L);
        assertThat(updatedOverrides.preferredPatientEquipmentWeight()).isEqualTo(44L);
        assertThat(updatedInput.departments()).containsExactly(updatedDepartment);
        assertThat(updatedInput.rooms()).containsExactly(updatedRoom);
        assertThat(updatedInput.beds()).containsExactly(updatedBed);
        assertThat(updatedInput.stays()).containsExactly(updatedStay);
        assertThat(updatedOutput.departments()).containsExactly(updatedDepartment);
        assertThat(updatedOutput.rooms()).containsExactly(updatedRoom);
        assertThat(updatedOutput.beds()).containsExactly(updatedBed);
        assertThat(updatedOutput.stays()).containsExactly(updatedStay);
        assertThat(updatedOutput.score()).isEqualTo("1hard/0soft");
        assertThat(updatedInputMetrics.stays()).isEqualTo(10);
        assertThat(updatedInputMetrics.beds()).isEqualTo(20);
        assertThat(updatedInputMetrics.rooms()).isEqualTo(30);
        assertThat(updatedOutputMetrics.totalAssignedStays()).isEqualTo(10);
        assertThat(updatedOutputMetrics.totalUnassignedStays()).isEqualTo(20);
        assertThat(updatedOutputMetrics.totalUsedRooms()).isEqualTo(30);
    }
}
