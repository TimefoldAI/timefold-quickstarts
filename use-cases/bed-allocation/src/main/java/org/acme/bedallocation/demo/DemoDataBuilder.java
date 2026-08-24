package org.acme.bedallocation.demo;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.acme.bedallocation.dto.BedDTO;
import org.acme.bedallocation.dto.BedPlanInput;
import org.acme.bedallocation.dto.DepartmentDTO;
import org.acme.bedallocation.dto.RoomDTO;
import org.acme.bedallocation.dto.StayDTO;

/**
 * Builds a fully hand-picked demo dataset (no randomness) that is deliberately hardcoded to be
 * feasible: every stay can be assigned to a bed without violating any hard constraint.
 */
public final class DemoDataBuilder {

    private static final String TELEMETRY = "telemetry";
    private static final String TELEVISION = "television";
    private static final String OXYGEN = "oxygen";
    private static final String NITROGEN = "nitrogen";

    private static final String CARDIOLOGY = "Cardiology";
    private static final String NEUROLOGY = "Neurology";
    private static final String ONCOLOGY = "Oncology";

    private DemoDataBuilder() {
    }

    public static DemoDataBuilder builder() {
        return new DemoDataBuilder();
    }

    public BedPlanInput build() {
        // Anchored to the next Monday (never today)
        LocalDate firstMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));

        RoomDTO room1 = room("R1", 1, "ANY_GENDER", Set.of(TELEMETRY, OXYGEN));
        RoomDTO room2 = room("R2", 1, "ANY_GENDER", Set.of(TELEVISION, NITROGEN));
        RoomDTO room3 = room("R3", 2, "ANY_GENDER", Set.of(TELEMETRY, TELEVISION, OXYGEN, NITROGEN));
        RoomDTO room4 = room("R4", 1, "ANY_GENDER", Set.of());
        RoomDTO room5 = room("R5", 2, "SAME_GENDER", Set.of());
        RoomDTO room6 = room("R6", 1, "ANY_GENDER", Set.of(OXYGEN, NITROGEN));
        RoomDTO room7 = room("R7", 1, "ANY_GENDER", Set.of(TELEMETRY, TELEVISION));
        RoomDTO room8 = room("R8", 2, "ANY_GENDER", Set.of(TELEMETRY, TELEVISION, OXYGEN, NITROGEN));
        RoomDTO room9 = room("R9", 1, "ANY_GENDER", Set.of());
        RoomDTO room10 = room("R10", 2, "SAME_GENDER", Set.of());
        RoomDTO room11 = room("R11", 2, "MALE_ONLY", Set.of(OXYGEN, TELEMETRY));
        RoomDTO room12 = room("R12", 2, "FEMALE_ONLY", Set.of(TELEVISION, NITROGEN));
        RoomDTO room13 = room("R13", 1, "ANY_GENDER", Set.of(OXYGEN, TELEMETRY, NITROGEN));
        RoomDTO room14 = room("R14", 2, "ANY_GENDER", Set.of(TELEMETRY, TELEVISION, OXYGEN, NITROGEN));
        RoomDTO room15 = room("R15", 2, "SAME_GENDER", Set.of());

        DepartmentDTO department = new DepartmentDTO("1", "General Ward", 1, 100,
                Map.of(CARDIOLOGY, 1, NEUROLOGY, 2, ONCOLOGY, 2),
                List.of(room1, room2, room3, room4, room5, room6, room7, room8, room9, room10,
                        room11, room12, room13, room14, room15));

        List<StayDTO> stays = List.of(
                stay("stay-1", "Bob", "MALE", 5, 1, List.of(), List.of(TELEMETRY),
                        firstMonday, firstMonday.plusDays(1), CARDIOLOGY),
                stay("stay-2", "Alice", "FEMALE", 12, 1, List.of(OXYGEN), List.of(TELEVISION),
                        firstMonday.plusDays(3), firstMonday.plusDays(6), NEUROLOGY),
                stay("stay-3", "David", "MALE", 19, 1, List.of(TELEMETRY, OXYGEN), List.of(OXYGEN),
                        firstMonday.plusDays(8), firstMonday.plusDays(10), ONCOLOGY),
                stay("stay-4", "Carol", "FEMALE", 25, 1, List.of(), List.of(NITROGEN),
                        firstMonday.plusDays(12), firstMonday.plusDays(17), CARDIOLOGY),
                stay("stay-5", "Frank", "MALE", 28, 1, List.of(TELEMETRY), List.of(TELEMETRY),
                        firstMonday.plusDays(19), firstMonday.plusDays(23), NEUROLOGY),
                stay("stay-6", "Eve", "FEMALE", 33, 1, List.of(NITROGEN), List.of(TELEVISION),
                        firstMonday, firstMonday.plusDays(3), ONCOLOGY),
                stay("stay-7", "Hank", "MALE", 36, 1, List.of(TELEVISION, NITROGEN), List.of(OXYGEN),
                        firstMonday.plusDays(5), firstMonday.plusDays(7), CARDIOLOGY),
                stay("stay-8", "Grace", "FEMALE", 41, 1, List.of(), List.of(NITROGEN),
                        firstMonday.plusDays(9), firstMonday.plusDays(14), NEUROLOGY),
                stay("stay-9", "Jack", "MALE", 44, 1, List.of(TELEVISION), List.of(TELEMETRY),
                        firstMonday.plusDays(16), firstMonday.plusDays(20), ONCOLOGY),
                stay("stay-10", "Ivy", "FEMALE", 47, 1, List.of(NITROGEN, TELEVISION), List.of(TELEVISION),
                        firstMonday.plusDays(22), firstMonday.plusDays(23), CARDIOLOGY),
                stay("stay-11", "Leo", "MALE", 50, 2, List.of(OXYGEN, NITROGEN), List.of(OXYGEN),
                        firstMonday, firstMonday.plusDays(2), NEUROLOGY),
                stay("stay-12", "Karen", "FEMALE", 52, 1, List.of(NITROGEN, TELEMETRY, TELEVISION), List.of(NITROGEN),
                        firstMonday.plusDays(4), firstMonday.plusDays(9), ONCOLOGY),
                stay("stay-13", "Oscar", "MALE", 54, 2, List.of(TELEMETRY, TELEVISION, OXYGEN, NITROGEN), List.of(TELEMETRY),
                        firstMonday.plusDays(11), firstMonday.plusDays(15), CARDIOLOGY),
                stay("stay-14", "Mona", "FEMALE", 55, 2, List.of(), List.of(TELEVISION),
                        firstMonday.plusDays(17), firstMonday.plusDays(18), NEUROLOGY),
                stay("stay-15", "Quinn", "MALE", 58, 1, List.of(OXYGEN), List.of(OXYGEN),
                        firstMonday.plusDays(20), firstMonday.plusDays(23), ONCOLOGY),
                stay("stay-16", "Nora", "FEMALE", 60, 2, List.of(NITROGEN, TELEMETRY, TELEVISION), List.of(NITROGEN),
                        firstMonday, firstMonday.plusDays(5), CARDIOLOGY),
                stay("stay-17", "Priya", "FEMALE", 66, 1, List.of(), List.of(TELEVISION),
                        firstMonday.plusDays(13), firstMonday.plusDays(14), ONCOLOGY),
                stay("stay-18", "Victor", "MALE", 68, 2, List.of(OXYGEN), List.of(OXYGEN),
                        firstMonday.plusDays(16), firstMonday.plusDays(19), CARDIOLOGY),
                stay("stay-19", "Rosa", "FEMALE", 70, 2, List.of(NITROGEN, TELEMETRY), List.of(NITROGEN),
                        firstMonday.plusDays(21), firstMonday.plusDays(23), NEUROLOGY),
                stay("stay-20", "Zack", "MALE", 72, 1, List.of(), List.of(TELEMETRY),
                        firstMonday, firstMonday.plusDays(4), ONCOLOGY),
                stay("stay-21", "Tina", "FEMALE", 75, 1, List.of(), List.of(TELEVISION),
                        firstMonday.plusDays(6), firstMonday.plusDays(7), CARDIOLOGY),
                stay("stay-22", "Aaron", "MALE", 80, 1, List.of(), List.of(OXYGEN),
                        firstMonday.plusDays(9), firstMonday.plusDays(12), NEUROLOGY),
                stay("stay-23", "Uma", "FEMALE", 88, 1, List.of(), List.of(NITROGEN),
                        firstMonday.plusDays(14), firstMonday.plusDays(16), ONCOLOGY),
                stay("stay-24", "Caleb", "MALE", 92, 1, List.of(), List.of(TELEMETRY),
                        firstMonday.plusDays(18), firstMonday.plusDays(23), CARDIOLOGY),
                stay("stay-25", "Wendy", "FEMALE", 5, 2, List.of(), List.of(TELEVISION),
                        firstMonday, firstMonday.plusDays(1), NEUROLOGY),
                stay("stay-26", "Xandra", "FEMALE", 12, 1, List.of(), List.of(OXYGEN),
                        firstMonday.plusDays(3), firstMonday.plusDays(6), ONCOLOGY),
                stay("stay-27", "Yara", "FEMALE", 19, 2, List.of(), List.of(NITROGEN),
                        firstMonday.plusDays(8), firstMonday.plusDays(10), CARDIOLOGY),
                stay("stay-28", "Bella", "FEMALE", 25, 2, List.of(), List.of(TELEMETRY),
                        firstMonday.plusDays(12), firstMonday.plusDays(17), NEUROLOGY),
                stay("stay-29", "Dana", "FEMALE", 28, 1, List.of(), List.of(TELEVISION),
                        firstMonday.plusDays(19), firstMonday.plusDays(23), ONCOLOGY),
                stay("stay-30", "Fiona", "FEMALE", 33, 2, List.of(), List.of(OXYGEN),
                        firstMonday, firstMonday.plusDays(3), CARDIOLOGY),
                stay("stay-31", "Gina", "FEMALE", 36, 2, List.of(), List.of(NITROGEN),
                        firstMonday.plusDays(5), firstMonday.plusDays(7), NEUROLOGY),
                stay("stay-32", "Iris", "FEMALE", 41, 1, List.of(), List.of(TELEMETRY),
                        firstMonday.plusDays(9), firstMonday.plusDays(14), ONCOLOGY),
                stay("stay-33", "Kira", "FEMALE", 44, 2, List.of(), List.of(TELEVISION),
                        firstMonday.plusDays(16), firstMonday.plusDays(20), CARDIOLOGY),
                stay("stay-34", "Mira", "FEMALE", 47, 2, List.of(), List.of(OXYGEN),
                        firstMonday.plusDays(22), firstMonday.plusDays(23), NEUROLOGY),
                stay("stay-35", "Olga", "FEMALE", 50, 1, List.of(NITROGEN), List.of(NITROGEN),
                        firstMonday, firstMonday.plusDays(2), ONCOLOGY),
                stay("stay-36", "Ethan", "MALE", 52, 1, List.of(OXYGEN, NITROGEN), List.of(TELEMETRY),
                        firstMonday.plusDays(4), firstMonday.plusDays(9), CARDIOLOGY),
                stay("stay-37", "Queenie", "FEMALE", 54, 1, List.of(), List.of(TELEVISION),
                        firstMonday.plusDays(11), firstMonday.plusDays(15), NEUROLOGY),
                stay("stay-38", "Hugo", "MALE", 55, 1, List.of(OXYGEN), List.of(OXYGEN),
                        firstMonday.plusDays(17), firstMonday.plusDays(18), ONCOLOGY),
                stay("stay-39", "Sofia", "FEMALE", 58, 1, List.of(NITROGEN, OXYGEN), List.of(NITROGEN),
                        firstMonday.plusDays(20), firstMonday.plusDays(23), CARDIOLOGY),
                stay("stay-40", "Jorge", "MALE", 60, 1, List.of(TELEMETRY, TELEVISION), List.of(TELEMETRY),
                        firstMonday, firstMonday.plusDays(5), NEUROLOGY),
                stay("stay-41", "Ursula", "FEMALE", 63, 1, List.of(), List.of(TELEVISION),
                        firstMonday.plusDays(7), firstMonday.plusDays(11), ONCOLOGY),
                stay("stay-42", "Liam", "MALE", 66, 1, List.of(TELEMETRY), List.of(OXYGEN),
                        firstMonday.plusDays(13), firstMonday.plusDays(14), CARDIOLOGY),
                stay("stay-43", "Renata", "FEMALE", 68, 1, List.of(TELEVISION, TELEMETRY), List.of(NITROGEN),
                        firstMonday.plusDays(16), firstMonday.plusDays(19), NEUROLOGY),
                stay("stay-44", "Noah", "MALE", 70, 1, List.of(), List.of(TELEMETRY),
                        firstMonday.plusDays(21), firstMonday.plusDays(23), ONCOLOGY),
                stay("stay-45", "Delia", "FEMALE", 72, 2, List.of(TELEVISION, OXYGEN, NITROGEN, TELEMETRY), List.of(TELEVISION),
                        firstMonday, firstMonday.plusDays(4), CARDIOLOGY),
                stay("stay-46", "Pablo", "MALE", 75, 2, List.of(), List.of(OXYGEN),
                        firstMonday.plusDays(6), firstMonday.plusDays(7), NEUROLOGY),
                stay("stay-47", "Elena", "FEMALE", 80, 1, List.of(NITROGEN), List.of(NITROGEN),
                        firstMonday.plusDays(9), firstMonday.plusDays(12), ONCOLOGY),
                stay("stay-48", "Ravi", "MALE", 88, 2, List.of(TELEMETRY, TELEVISION), List.of(TELEMETRY),
                        firstMonday.plusDays(14), firstMonday.plusDays(16), CARDIOLOGY),
                stay("stay-49", "Helga", "FEMALE", 92, 2, List.of(TELEVISION, OXYGEN, NITROGEN), List.of(TELEVISION),
                        firstMonday.plusDays(18), firstMonday.plusDays(23), NEUROLOGY),
                stay("stay-50", "Theo", "MALE", 5, 1, List.of(), List.of(OXYGEN),
                        firstMonday, firstMonday.plusDays(1), ONCOLOGY),
                stay("stay-51", "Alice", "FEMALE", 12, 2, List.of(NITROGEN), List.of(NITROGEN),
                        firstMonday.plusDays(3), firstMonday.plusDays(6), CARDIOLOGY),
                stay("stay-52", "Vince", "MALE", 19, 2, List.of(TELEMETRY, TELEVISION), List.of(TELEMETRY),
                        firstMonday.plusDays(8), firstMonday.plusDays(10), NEUROLOGY),
                stay("stay-53", "Carol", "FEMALE", 25, 1, List.of(TELEVISION, OXYGEN, NITROGEN), List.of(TELEVISION),
                        firstMonday.plusDays(12), firstMonday.plusDays(17), ONCOLOGY),
                stay("stay-54", "Eve", "FEMALE", 33, 1, List.of(), List.of(NITROGEN),
                        firstMonday, firstMonday.plusDays(3), NEUROLOGY),
                stay("stay-55", "Aiden", "MALE", 36, 1, List.of(), List.of(TELEMETRY),
                        firstMonday.plusDays(5), firstMonday.plusDays(7), ONCOLOGY),
                stay("stay-56", "Grace", "FEMALE", 41, 1, List.of(), List.of(TELEVISION),
                        firstMonday.plusDays(9), firstMonday.plusDays(14), CARDIOLOGY),
                stay("stay-57", "Carlos", "MALE", 44, 1, List.of(), List.of(OXYGEN),
                        firstMonday.plusDays(16), firstMonday.plusDays(20), NEUROLOGY),
                stay("stay-58", "Ivy", "FEMALE", 47, 1, List.of(), List.of(NITROGEN),
                        firstMonday.plusDays(22), firstMonday.plusDays(23), ONCOLOGY),
                stay("stay-59", "Elian", "MALE", 50, 2, List.of(), List.of(TELEMETRY),
                        firstMonday, firstMonday.plusDays(2), CARDIOLOGY),
                stay("stay-60", "Felix", "MALE", 52, 2, List.of(), List.of(TELEVISION),
                        firstMonday.plusDays(4), firstMonday.plusDays(9), NEUROLOGY),
                stay("stay-61", "Gustavo", "MALE", 54, 1, List.of(), List.of(OXYGEN),
                        firstMonday.plusDays(11), firstMonday.plusDays(15), ONCOLOGY),
                stay("stay-62", "Ian", "MALE", 55, 2, List.of(), List.of(NITROGEN),
                        firstMonday.plusDays(17), firstMonday.plusDays(18), CARDIOLOGY),
                stay("stay-63", "Marcus", "MALE", 58, 2, List.of(), List.of(TELEMETRY),
                        firstMonday.plusDays(20), firstMonday.plusDays(23), NEUROLOGY),
                stay("stay-64", "Bob", "MALE", 60, 1, List.of(), List.of(TELEVISION),
                        firstMonday, firstMonday.plusDays(5), ONCOLOGY),
                stay("stay-65", "David", "MALE", 63, 2, List.of(), List.of(OXYGEN),
                        firstMonday.plusDays(7), firstMonday.plusDays(11), CARDIOLOGY),
                stay("stay-66", "Frank", "MALE", 66, 2, List.of(), List.of(NITROGEN),
                        firstMonday.plusDays(13), firstMonday.plusDays(14), NEUROLOGY),
                stay("stay-67", "Hank", "MALE", 68, 1, List.of(), List.of(TELEMETRY),
                        firstMonday.plusDays(16), firstMonday.plusDays(19), ONCOLOGY),
                stay("stay-68", "Jack", "MALE", 70, 2, List.of(), List.of(TELEVISION),
                        firstMonday.plusDays(21), firstMonday.plusDays(23), CARDIOLOGY),
                stay("stay-69", "Leo", "MALE", 72, 2, List.of(OXYGEN, TELEMETRY), List.of(OXYGEN),
                        firstMonday, firstMonday.plusDays(4), NEUROLOGY),
                stay("stay-70", "Oscar", "MALE", 75, 1, List.of(), List.of(NITROGEN),
                        firstMonday.plusDays(6), firstMonday.plusDays(7), ONCOLOGY),
                stay("stay-71", "Quinn", "MALE", 80, 2, List.of(OXYGEN), List.of(TELEMETRY),
                        firstMonday.plusDays(9), firstMonday.plusDays(12), CARDIOLOGY),
                stay("stay-72", "Victor", "MALE", 92, 1, List.of(), List.of(OXYGEN),
                        firstMonday.plusDays(18), firstMonday.plusDays(23), ONCOLOGY),
                stay("stay-73", "Zack", "MALE", 5, 2, List.of(), List.of(NITROGEN),
                        firstMonday, firstMonday.plusDays(1), CARDIOLOGY),
                stay("stay-74", "Aaron", "MALE", 12, 2, List.of(OXYGEN), List.of(TELEMETRY),
                        firstMonday.plusDays(3), firstMonday.plusDays(6), NEUROLOGY),
                stay("stay-75", "Caleb", "MALE", 19, 1, List.of(TELEMETRY, OXYGEN), List.of(TELEVISION),
                        firstMonday.plusDays(8), firstMonday.plusDays(10), ONCOLOGY),
                stay("stay-76", "Ethan", "MALE", 25, 2, List.of(), List.of(OXYGEN),
                        firstMonday.plusDays(12), firstMonday.plusDays(17), CARDIOLOGY),
                stay("stay-77", "Hugo", "MALE", 28, 2, List.of(TELEMETRY), List.of(NITROGEN),
                        firstMonday.plusDays(19), firstMonday.plusDays(23), NEUROLOGY),
                stay("stay-78", "Karen", "FEMALE", 33, 1, List.of(TELEVISION), List.of(TELEMETRY),
                        firstMonday, firstMonday.plusDays(3), ONCOLOGY),
                stay("stay-79", "Mona", "FEMALE", 36, 2, List.of(NITROGEN, TELEVISION), List.of(TELEVISION),
                        firstMonday.plusDays(5), firstMonday.plusDays(7), CARDIOLOGY),
                stay("stay-80", "Nora", "FEMALE", 41, 2, List.of(), List.of(OXYGEN),
                        firstMonday.plusDays(9), firstMonday.plusDays(14), NEUROLOGY),
                stay("stay-81", "Priya", "FEMALE", 44, 1, List.of(NITROGEN), List.of(NITROGEN),
                        firstMonday.plusDays(16), firstMonday.plusDays(20), ONCOLOGY),
                stay("stay-82", "Rosa", "FEMALE", 47, 2, List.of(TELEVISION, NITROGEN), List.of(TELEMETRY),
                        firstMonday.plusDays(22), firstMonday.plusDays(23), CARDIOLOGY),
                stay("stay-83", "Tina", "FEMALE", 50, 2, List.of(NITROGEN, TELEVISION), List.of(TELEVISION),
                        firstMonday, firstMonday.plusDays(2), NEUROLOGY),
                stay("stay-84", "Uma", "FEMALE", 52, 1, List.of(), List.of(OXYGEN),
                        firstMonday.plusDays(4), firstMonday.plusDays(9), ONCOLOGY),
                stay("stay-85", "Wendy", "FEMALE", 54, 2, List.of(NITROGEN), List.of(NITROGEN),
                        firstMonday.plusDays(11), firstMonday.plusDays(15), CARDIOLOGY),
                stay("stay-86", "Xandra", "FEMALE", 55, 2, List.of(TELEVISION, NITROGEN), List.of(TELEMETRY),
                        firstMonday.plusDays(17), firstMonday.plusDays(18), NEUROLOGY),
                stay("stay-87", "Yara", "FEMALE", 58, 1, List.of(), List.of(TELEVISION),
                        firstMonday.plusDays(20), firstMonday.plusDays(23), ONCOLOGY),
                stay("stay-88", "Jorge", "MALE", 60, 1, List.of(OXYGEN, TELEMETRY), List.of(OXYGEN),
                        firstMonday, firstMonday.plusDays(5), CARDIOLOGY),
                stay("stay-89", "Bella", "FEMALE", 63, 1, List.of(TELEMETRY, NITROGEN, OXYGEN), List.of(NITROGEN),
                        firstMonday.plusDays(7), firstMonday.plusDays(11), NEUROLOGY),
                stay("stay-90", "Liam", "MALE", 66, 1, List.of(), List.of(TELEMETRY),
                        firstMonday.plusDays(13), firstMonday.plusDays(14), ONCOLOGY),
                stay("stay-91", "Dana", "FEMALE", 68, 1, List.of(OXYGEN), List.of(TELEVISION),
                        firstMonday.plusDays(16), firstMonday.plusDays(19), CARDIOLOGY),
                stay("stay-92", "Noah", "MALE", 70, 1, List.of(TELEMETRY, NITROGEN), List.of(OXYGEN),
                        firstMonday.plusDays(21), firstMonday.plusDays(23), NEUROLOGY),
                stay("stay-93", "Fiona", "FEMALE", 72, 1, List.of(NITROGEN, TELEMETRY, TELEVISION, OXYGEN), List.of(NITROGEN),
                        firstMonday, firstMonday.plusDays(4), ONCOLOGY),
                stay("stay-94", "Pablo", "MALE", 75, 2, List.of(), List.of(TELEMETRY),
                        firstMonday.plusDays(6), firstMonday.plusDays(7), CARDIOLOGY),
                stay("stay-95", "Gina", "FEMALE", 80, 2, List.of(TELEVISION), List.of(TELEVISION),
                        firstMonday.plusDays(9), firstMonday.plusDays(12), NEUROLOGY),
                stay("stay-96", "Ravi", "MALE", 88, 1, List.of(OXYGEN, NITROGEN), List.of(OXYGEN),
                        firstMonday.plusDays(14), firstMonday.plusDays(16), ONCOLOGY),
                stay("stay-97", "Iris", "FEMALE", 92, 2, List.of(NITROGEN, TELEMETRY, TELEVISION), List.of(NITROGEN),
                        firstMonday.plusDays(18), firstMonday.plusDays(23), CARDIOLOGY),
                stay("stay-98", "Theo", "MALE", 5, 2, List.of(), List.of(TELEMETRY),
                        firstMonday, firstMonday.plusDays(1), NEUROLOGY),
                stay("stay-99", "Kira", "FEMALE", 12, 1, List.of(TELEVISION), List.of(TELEVISION),
                        firstMonday.plusDays(3), firstMonday.plusDays(6), ONCOLOGY),
                stay("stay-100", "Vince", "MALE", 19, 2, List.of(OXYGEN, NITROGEN), List.of(OXYGEN),
                        firstMonday.plusDays(8), firstMonday.plusDays(10), CARDIOLOGY),
                stay("stay-101", "Mira", "FEMALE", 25, 2, List.of(NITROGEN, TELEMETRY, TELEVISION), List.of(NITROGEN),
                        firstMonday.plusDays(12), firstMonday.plusDays(17), NEUROLOGY),
                stay("stay-102", "Olga", "FEMALE", 33, 2, List.of(), List.of(TELEVISION),
                        firstMonday, firstMonday.plusDays(3), CARDIOLOGY),
                stay("stay-103", "Queenie", "FEMALE", 36, 2, List.of(), List.of(OXYGEN),
                        firstMonday.plusDays(5), firstMonday.plusDays(7), NEUROLOGY),
                stay("stay-104", "Sofia", "FEMALE", 41, 1, List.of(), List.of(NITROGEN),
                        firstMonday.plusDays(9), firstMonday.plusDays(14), ONCOLOGY),
                stay("stay-105", "Ursula", "FEMALE", 44, 2, List.of(), List.of(TELEMETRY),
                        firstMonday.plusDays(16), firstMonday.plusDays(20), CARDIOLOGY),
                stay("stay-106", "Renata", "FEMALE", 47, 2, List.of(), List.of(TELEVISION),
                        firstMonday.plusDays(22), firstMonday.plusDays(23), NEUROLOGY),
                stay("stay-107", "Delia", "FEMALE", 50, 1, List.of(), List.of(OXYGEN),
                        firstMonday, firstMonday.plusDays(2), ONCOLOGY),
                stay("stay-108", "Elena", "FEMALE", 52, 2, List.of(), List.of(NITROGEN),
                        firstMonday.plusDays(4), firstMonday.plusDays(9), CARDIOLOGY),
                stay("stay-109", "Helga", "FEMALE", 54, 2, List.of(), List.of(TELEMETRY),
                        firstMonday.plusDays(11), firstMonday.plusDays(15), NEUROLOGY),
                stay("stay-110", "Alice", "FEMALE", 55, 1, List.of(), List.of(TELEVISION),
                        firstMonday.plusDays(17), firstMonday.plusDays(18), ONCOLOGY),
                stay("stay-111", "Carol", "FEMALE", 58, 2, List.of(), List.of(OXYGEN),
                        firstMonday.plusDays(20), firstMonday.plusDays(23), CARDIOLOGY));

        return new BedPlanInput(List.of(department), stays);
    }

    private static RoomDTO room(String id, int capacity, String genderLimitation, Set<String> equipments) {
        List<BedDTO> beds = capacity == 1
                ? List.of(new BedDTO(id + "-bed0", 0))
                : List.of(new BedDTO(id + "-bed0", 0), new BedDTO(id + "-bed1", 1));
        return new RoomDTO(id, "Room " + id.substring(1), capacity, genderLimitation, equipments, beds);
    }

    private static StayDTO stay(String id, String patientName, String patientGender, int patientAge,
            Integer patientPreferredMaximumRoomCapacity, List<String> patientRequiredEquipments,
            List<String> patientPreferredEquipments, LocalDate arrivalDate, LocalDate departureDate,
            String specialty) {
        return new StayDTO(id, patientName, patientGender, patientAge, patientPreferredMaximumRoomCapacity,
                patientRequiredEquipments, patientPreferredEquipments, arrivalDate.toString(), departureDate.toString(),
                specialty, null);
    }
}
