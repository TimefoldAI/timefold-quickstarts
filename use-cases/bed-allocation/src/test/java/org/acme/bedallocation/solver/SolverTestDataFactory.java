package org.acme.bedallocation.solver;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.acme.bedallocation.dto.BedDTO;
import org.acme.bedallocation.dto.BedScheduleInput;
import org.acme.bedallocation.dto.DepartmentDTO;
import org.acme.bedallocation.dto.RoomDTO;
import org.acme.bedallocation.dto.StayDTO;

final class SolverTestDataFactory {

    private static final List<String> SPECIALTIES = List.of("Specialty1", "Specialty2", "Specialty3");
    private static final List<String> GENDERS = List.of("MALE", "FEMALE");
    private static final List<String> EQUIPMENTS = List.of("telemetry", "television", "oxygen", "nitrogen");

    private SolverTestDataFactory() {
    }

    static BedScheduleInput createProblem() {
        int roomCount = 5;
        int bedsPerRoom = 2;
        int dayCount = 7;
        int stayCount = 12;

        Map<String, Integer> specialtyToPriority = new LinkedHashMap<>();
        for (String specialty : SPECIALTIES) {
            specialtyToPriority.put(specialty, 1);
        }
        Integer noAgeLimit = null;
        List<DepartmentDTO> departments =
                List.of(new DepartmentDTO("0", "General", noAgeLimit, noAgeLimit, specialtyToPriority));

        List<RoomDTO> rooms = new ArrayList<>();
        List<BedDTO> beds = new ArrayList<>();
        for (int r = 0; r < roomCount; r++) {
            String roomId = Integer.toString(r);
            rooms.add(new RoomDTO(roomId, "Room" + r, "0", bedsPerRoom, "ANY_GENDER", EQUIPMENTS));
            for (int b = 0; b < bedsPerRoom; b++) {
                beds.add(new BedDTO(roomId + "-bed" + b, roomId, b));
            }
        }

        LocalDate startDate =
                LocalDate.now(ZoneId.systemDefault()).with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY));
        List<StayDTO> stays = new ArrayList<>();
        for (int i = 0; i < stayCount; i++) {
            LocalDate arrival = startDate.plusDays(i % dayCount);
            LocalDate departure = arrival.plusDays(i % 2);
            stays.add(new StayDTO("stay-" + i, "Patient " + i, GENDERS.get(i % GENDERS.size()), 30 + (i % 40),
                    noAgeLimit, List.of(), List.of(), arrival.toString(), departure.toString(),
                    SPECIALTIES.get(i % SPECIALTIES.size()), ""));
        }

        return new BedScheduleInput(departments, rooms, beds, stays);
    }
}
