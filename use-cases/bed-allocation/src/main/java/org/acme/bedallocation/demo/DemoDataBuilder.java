package org.acme.bedallocation.demo;

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

public final class DemoDataBuilder {

    private static final List<String> SPECIALTIES = List.of("Specialty1", "Specialty2", "Specialty3");
    private static final List<String> GENDERS = List.of("MALE", "FEMALE");
    private static final List<String> EQUIPMENTS = List.of("telemetry", "television", "oxygen", "nitrogen");

    private int roomCount = 6;
    private int bedsPerRoom = 2;
    private int dayCount = 14;
    private int stayCount = 24;

    private DemoDataBuilder() {
    }

    public static DemoDataBuilder builder() {
        return new DemoDataBuilder();
    }

    public DemoDataBuilder setRoomCount(int roomCount) {
        this.roomCount = roomCount;
        return this;
    }

    public DemoDataBuilder setBedsPerRoom(int bedsPerRoom) {
        this.bedsPerRoom = bedsPerRoom;
        return this;
    }

    public DemoDataBuilder setDayCount(int dayCount) {
        this.dayCount = dayCount;
        return this;
    }

    public DemoDataBuilder setStayCount(int stayCount) {
        this.stayCount = stayCount;
        return this;
    }

    public BedScheduleInput build() {
        Map<String, Integer> specialtyToPriority = new LinkedHashMap<>();
        for (String specialty : SPECIALTIES) {
            specialtyToPriority.put(specialty, 1);
        }
        Integer noAgeLimit = null;
        List<DepartmentDTO> departments =
                List.of(new DepartmentDTO("0", "General", noAgeLimit, noAgeLimit, specialtyToPriority));

        List<RoomDTO> rooms = new ArrayList<>(roomCount);
        List<BedDTO> beds = new ArrayList<>(roomCount * bedsPerRoom);
        for (int r = 0; r < roomCount; r++) {
            String roomId = Integer.toString(r);
            rooms.add(new RoomDTO(roomId, "Room" + r, "0", bedsPerRoom, "ANY_GENDER", EQUIPMENTS));
            for (int b = 0; b < bedsPerRoom; b++) {
                beds.add(new BedDTO(roomId + "-bed" + b, roomId, b));
            }
        }

        LocalDate startDate =
                LocalDate.now(ZoneId.systemDefault()).with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY));
        List<StayDTO> stays = new ArrayList<>(stayCount);
        for (int i = 0; i < stayCount; i++) {
            int offset = i % dayCount;
            int duration = i % 3; // 0, 1 or 2 extra nights
            LocalDate arrival = startDate.plusDays(offset);
            LocalDate departure = arrival.plusDays(duration);
            String specialty = SPECIALTIES.get(i % SPECIALTIES.size());
            String gender = GENDERS.get(i % GENDERS.size());
            int age = 30 + (i % 40);
            stays.add(new StayDTO("stay-" + i, "Patient " + i, gender, age, noAgeLimit,
                    List.of(), List.of(), arrival.toString(), departure.toString(), specialty, ""));
        }

        return new BedScheduleInput(departments, rooms, beds, stays);
    }
}
