package org.acme.bedallocation.support;

import java.util.List;

import org.acme.bedallocation.dto.BedDTO;
import org.acme.bedallocation.dto.BedPlanInput;
import org.acme.bedallocation.dto.DepartmentDTO;
import org.acme.bedallocation.dto.RoomDTO;
import org.acme.bedallocation.dto.StayDTO;

/**
 * Shared test data for the bed allocation tests.
 * <p>
 * {@link #createProblem()} returns a complete, feasible problem for the solver tests: there are more beds
 * than stays and none of the hard constraints can trigger, so the solver can reach a fully assigned,
 * feasible solution quickly. The constants and factory methods are the building blocks of that problem and
 * can be recombined, which lets tests that focus on a single aspect - such as the validation tests - vary
 * one collection and keep the rest valid.
 */
public final class BedPlanTestDataFactory {

    public static final String DEPARTMENT_ID = "d1";

    public static final List<RoomDTO> ROOMS = List.of(
            room("r1"),
            room("r2"),
            room("r3"));

    private BedPlanTestDataFactory() {
    }

    /**
     * @return a complete, feasible problem with every stay still unassigned
     */
    public static BedPlanInput createProblem() {
        return input(List.of(department(ROOMS)),
                List.of(stay("s1"), stay("s2"), stay("s3"), stay("s4")));
    }

    public static BedPlanInput input(List<DepartmentDTO> departments, List<StayDTO> stays) {
        return new BedPlanInput(departments, stays);
    }

    public static BedPlanInput inputWithDepartments(DepartmentDTO... departments) {
        return input(List.of(departments), List.of());
    }

    public static BedPlanInput inputWithStays(StayDTO... stays) {
        return input(List.of(department(ROOMS)), List.of(stays));
    }

    public static DepartmentDTO department(String id, List<RoomDTO> rooms) {
        return DepartmentDTO.builder(id, "Department " + id).rooms(rooms).build();
    }

    public static DepartmentDTO department(List<RoomDTO> rooms) {
        return department(DEPARTMENT_ID, rooms);
    }

    public static RoomDTO room(String id) {
        return roomWithBeds(id, List.of(bed(id + "-bed0", 0), bed(id + "-bed1", 1)));
    }

    public static RoomDTO roomWithBeds(String id, List<BedDTO> beds) {
        return new RoomDTO(id, "Room " + id, beds.size(), "ANY_GENDER", List.of(), beds);
    }

    public static BedDTO bed(String id, int indexInRoom) {
        return new BedDTO(id, indexInRoom);
    }

    public static StayDTO stay(String id) {
        return StayDTO.builder(id, "2024-01-01", "2024-01-03")
                .patientName("Patient " + id)
                .patientGender("MALE")
                .patientAge(30)
                .build();
    }

    public static StayDTO stayWithBedId(String id, String bedId) {
        return stay(id).withBedId(bedId);
    }
}
