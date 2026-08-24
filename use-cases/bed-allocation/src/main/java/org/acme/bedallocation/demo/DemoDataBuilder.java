package org.acme.bedallocation.demo;

import static org.acme.bedallocation.domain.Gender.FEMALE;
import static org.acme.bedallocation.domain.Gender.MALE;
import static org.acme.bedallocation.domain.GenderLimitation.ANY_GENDER;
import static org.acme.bedallocation.domain.GenderLimitation.FEMALE_ONLY;
import static org.acme.bedallocation.domain.GenderLimitation.MALE_ONLY;
import static org.acme.bedallocation.domain.GenderLimitation.SAME_GENDER;

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

        RoomDTO room1 = room("R1", 1, ANY_GENDER.name(), Set.of(TELEMETRY, OXYGEN));
        RoomDTO room2 = room("R2", 1, ANY_GENDER.name(), Set.of(TELEVISION, NITROGEN));
        RoomDTO room3 = room("R3", 2, ANY_GENDER.name(), Set.of(TELEMETRY, TELEVISION, OXYGEN, NITROGEN));
        RoomDTO room4 = room("R4", 1, ANY_GENDER.name(), Set.of());
        RoomDTO room5 = room("R5", 2, SAME_GENDER.name(), Set.of());
        RoomDTO room6 = room("R6", 1, ANY_GENDER.name(), Set.of(OXYGEN, NITROGEN));
        RoomDTO room7 = room("R7", 1, ANY_GENDER.name(), Set.of(TELEMETRY, TELEVISION));
        RoomDTO room8 = room("R8", 2, ANY_GENDER.name(), Set.of(TELEMETRY, TELEVISION, OXYGEN, NITROGEN));
        RoomDTO room9 = room("R9", 1, ANY_GENDER.name(), Set.of());
        RoomDTO room10 = room("R10", 2, SAME_GENDER.name(), Set.of());
        RoomDTO room11 = room("R11", 2, MALE_ONLY.name(), Set.of(OXYGEN, TELEMETRY));
        RoomDTO room12 = room("R12", 2, FEMALE_ONLY.name(), Set.of(TELEVISION, NITROGEN));
        RoomDTO room13 = room("R13", 1, ANY_GENDER.name(), Set.of(OXYGEN, TELEMETRY, NITROGEN));
        RoomDTO room14 = room("R14", 2, ANY_GENDER.name(), Set.of(TELEMETRY, TELEVISION, OXYGEN, NITROGEN));
        RoomDTO room15 = room("R15", 2, SAME_GENDER.name(), Set.of());

        DepartmentDTO department = new DepartmentDTO("1", "General Ward", 1, 100,
                Map.of(CARDIOLOGY, 1, NEUROLOGY, 2, ONCOLOGY, 2),
                List.of(room1, room2, room3, room4, room5, room6, room7, room8, room9, room10,
                        room11, room12, room13, room14, room15));

        List<StayDTO> stays = List.of(
                stay("stay-1", "Bob", MALE.name(), 5, 1, List.of(), List.of(TELEMETRY),
                        firstMonday, firstMonday.plusDays(1), CARDIOLOGY),
                stay("stay-2", "Alice", FEMALE.name(), 12, 1, List.of(OXYGEN), List.of(TELEVISION),
                        firstMonday.plusDays(4), firstMonday.plusDays(7), NEUROLOGY),
                stay("stay-3", "David", MALE.name(), 19, 1, List.of(TELEMETRY, OXYGEN), List.of(TELEVISION),
                        firstMonday.plusDays(10), firstMonday.plusDays(12), ONCOLOGY),
                stay("stay-4", "Carol", FEMALE.name(), 25, 1, List.of(), List.of(NITROGEN),
                        firstMonday.plusDays(16), firstMonday.plusDays(21), CARDIOLOGY),
                stay("stay-5", "Frank", MALE.name(), 28, 1, List.of(TELEMETRY), List.of(TELEVISION),
                        firstMonday.plusDays(25), firstMonday.plusDays(29), NEUROLOGY),
                stay("stay-6", "Eve", FEMALE.name(), 33, 1, List.of(NITROGEN), List.of(TELEVISION),
                        firstMonday, firstMonday.plusDays(3), ONCOLOGY),
                stay("stay-7", "Hank", MALE.name(), 36, 1, List.of(TELEVISION, NITROGEN), List.of(OXYGEN),
                        firstMonday.plusDays(6), firstMonday.plusDays(8), CARDIOLOGY),
                stay("stay-8", "Grace", FEMALE.name(), 41, 1, List.of(), List.of(NITROGEN),
                        firstMonday.plusDays(12), firstMonday.plusDays(17), NEUROLOGY),
                stay("stay-9", "Jack", MALE.name(), 44, 1, List.of(TELEVISION), List.of(TELEMETRY),
                        firstMonday.plusDays(21), firstMonday.plusDays(25), ONCOLOGY),
                stay("stay-10", "Ivy", FEMALE.name(), 47, 1, List.of(NITROGEN, TELEVISION), List.of(TELEMETRY),
                        firstMonday.plusDays(29), firstMonday.plusDays(30), CARDIOLOGY),
                stay("stay-11", "Leo", MALE.name(), 50, 2, List.of(OXYGEN, NITROGEN), List.of(TELEMETRY),
                        firstMonday, firstMonday.plusDays(2), NEUROLOGY),
                stay("stay-12", "Karen", FEMALE.name(), 52, 1, List.of(NITROGEN, TELEMETRY, TELEVISION), List.of(OXYGEN),
                        firstMonday.plusDays(5), firstMonday.plusDays(10), ONCOLOGY),
                stay("stay-13", "Oscar", MALE.name(), 54, 2, List.of(TELEMETRY, TELEVISION, OXYGEN, NITROGEN), List.of(),
                        firstMonday.plusDays(14), firstMonday.plusDays(18), CARDIOLOGY),
                stay("stay-14", "Mona", FEMALE.name(), 55, 2, List.of(), List.of(TELEVISION),
                        firstMonday.plusDays(22), firstMonday.plusDays(23), NEUROLOGY),
                stay("stay-15", "Quinn", MALE.name(), 58, 1, List.of(OXYGEN), List.of(TELEMETRY),
                        firstMonday.plusDays(26), firstMonday.plusDays(29), ONCOLOGY),
                stay("stay-16", "Nora", FEMALE.name(), 60, 2, List.of(NITROGEN, TELEMETRY, TELEVISION), List.of(OXYGEN),
                        firstMonday, firstMonday.plusDays(5), CARDIOLOGY),
                stay("stay-17", "Priya", FEMALE.name(), 66, 1, List.of(), List.of(TELEVISION),
                        firstMonday.plusDays(17), firstMonday.plusDays(18), ONCOLOGY),
                stay("stay-18", "Victor", MALE.name(), 68, 2, List.of(OXYGEN), List.of(TELEMETRY),
                        firstMonday.plusDays(21), firstMonday.plusDays(24), CARDIOLOGY),
                stay("stay-19", "Rosa", FEMALE.name(), 70, 2, List.of(NITROGEN, TELEMETRY), List.of(TELEVISION),
                        firstMonday.plusDays(27), firstMonday.plusDays(29), NEUROLOGY),
                stay("stay-20", "Zack", MALE.name(), 72, 1, List.of(), List.of(TELEMETRY),
                        firstMonday, firstMonday.plusDays(4), ONCOLOGY),
                stay("stay-21", "Tina", FEMALE.name(), 75, 1, List.of(), List.of(TELEVISION),
                        firstMonday.plusDays(8), firstMonday.plusDays(9), CARDIOLOGY),
                stay("stay-22", "Aaron", MALE.name(), 80, 1, List.of(), List.of(OXYGEN),
                        firstMonday.plusDays(12), firstMonday.plusDays(15), NEUROLOGY),
                stay("stay-23", "Uma", FEMALE.name(), 88, 1, List.of(), List.of(NITROGEN),
                        firstMonday.plusDays(18), firstMonday.plusDays(20), ONCOLOGY),
                stay("stay-24", "Caleb", MALE.name(), 92, 1, List.of(), List.of(TELEMETRY),
                        firstMonday.plusDays(23), firstMonday.plusDays(28), CARDIOLOGY),
                stay("stay-25", "Wendy", FEMALE.name(), 5, 2, List.of(), List.of(TELEVISION),
                        firstMonday, firstMonday.plusDays(1), NEUROLOGY),
                stay("stay-26", "Xandra", FEMALE.name(), 12, 1, List.of(), List.of(OXYGEN),
                        firstMonday.plusDays(4), firstMonday.plusDays(7), ONCOLOGY),
                stay("stay-27", "Yara", FEMALE.name(), 19, 2, List.of(), List.of(NITROGEN),
                        firstMonday.plusDays(10), firstMonday.plusDays(12), CARDIOLOGY),
                stay("stay-28", "Bella", FEMALE.name(), 25, 2, List.of(), List.of(TELEMETRY),
                        firstMonday.plusDays(16), firstMonday.plusDays(21), NEUROLOGY),
                stay("stay-29", "Dana", FEMALE.name(), 28, 1, List.of(), List.of(TELEVISION),
                        firstMonday.plusDays(25), firstMonday.plusDays(29), ONCOLOGY),
                stay("stay-30", "Fiona", FEMALE.name(), 33, 2, List.of(), List.of(OXYGEN),
                        firstMonday, firstMonday.plusDays(3), CARDIOLOGY),
                stay("stay-31", "Gina", FEMALE.name(), 36, 2, List.of(), List.of(NITROGEN),
                        firstMonday.plusDays(6), firstMonday.plusDays(8), NEUROLOGY),
                stay("stay-32", "Iris", FEMALE.name(), 41, 1, List.of(), List.of(TELEMETRY),
                        firstMonday.plusDays(12), firstMonday.plusDays(17), ONCOLOGY),
                stay("stay-33", "Kira", FEMALE.name(), 44, 2, List.of(), List.of(TELEVISION),
                        firstMonday.plusDays(21), firstMonday.plusDays(25), CARDIOLOGY),
                stay("stay-34", "Mira", FEMALE.name(), 47, 2, List.of(), List.of(OXYGEN),
                        firstMonday.plusDays(29), firstMonday.plusDays(30), NEUROLOGY),
                stay("stay-35", "Olga", FEMALE.name(), 50, 1, List.of(NITROGEN), List.of(TELEMETRY),
                        firstMonday, firstMonday.plusDays(2), ONCOLOGY),
                stay("stay-36", "Ethan", MALE.name(), 52, 1, List.of(OXYGEN, NITROGEN), List.of(TELEMETRY),
                        firstMonday.plusDays(5), firstMonday.plusDays(10), CARDIOLOGY),
                stay("stay-37", "Queenie", FEMALE.name(), 54, 1, List.of(), List.of(TELEVISION),
                        firstMonday.plusDays(14), firstMonday.plusDays(18), NEUROLOGY),
                stay("stay-38", "Hugo", MALE.name(), 55, 1, List.of(OXYGEN), List.of(TELEMETRY),
                        firstMonday.plusDays(22), firstMonday.plusDays(23), ONCOLOGY),
                stay("stay-39", "Sofia", FEMALE.name(), 58, 1, List.of(NITROGEN, OXYGEN), List.of(TELEMETRY),
                        firstMonday.plusDays(26), firstMonday.plusDays(29), CARDIOLOGY),
                stay("stay-40", "Jorge", MALE.name(), 60, 1, List.of(TELEMETRY, TELEVISION), List.of(OXYGEN),
                        firstMonday, firstMonday.plusDays(5), NEUROLOGY),
                stay("stay-41", "Ursula", FEMALE.name(), 63, 1, List.of(), List.of(TELEVISION),
                        firstMonday.plusDays(9), firstMonday.plusDays(13), ONCOLOGY),
                stay("stay-42", "Liam", MALE.name(), 66, 1, List.of(TELEMETRY), List.of(OXYGEN),
                        firstMonday.plusDays(17), firstMonday.plusDays(18), CARDIOLOGY),
                stay("stay-43", "Renata", FEMALE.name(), 68, 1, List.of(TELEVISION, TELEMETRY), List.of(NITROGEN),
                        firstMonday.plusDays(21), firstMonday.plusDays(24), NEUROLOGY),
                stay("stay-44", "Noah", MALE.name(), 70, 1, List.of(), List.of(TELEMETRY),
                        firstMonday.plusDays(27), firstMonday.plusDays(29), ONCOLOGY),
                stay("stay-45", "Delia", FEMALE.name(), 72, 2, List.of(TELEVISION, OXYGEN, NITROGEN, TELEMETRY), List.of(),
                        firstMonday, firstMonday.plusDays(4), CARDIOLOGY),
                stay("stay-46", "Pablo", MALE.name(), 75, 2, List.of(), List.of(OXYGEN),
                        firstMonday.plusDays(8), firstMonday.plusDays(9), NEUROLOGY),
                stay("stay-47", "Elena", FEMALE.name(), 80, 1, List.of(NITROGEN), List.of(TELEMETRY),
                        firstMonday.plusDays(12), firstMonday.plusDays(15), ONCOLOGY),
                stay("stay-48", "Ravi", MALE.name(), 88, 2, List.of(TELEMETRY, TELEVISION), List.of(OXYGEN),
                        firstMonday.plusDays(18), firstMonday.plusDays(20), CARDIOLOGY),
                stay("stay-49", "Helga", FEMALE.name(), 92, 2, List.of(TELEVISION, OXYGEN, NITROGEN), List.of(TELEMETRY),
                        firstMonday.plusDays(23), firstMonday.plusDays(28), NEUROLOGY),
                stay("stay-50", "Theo", MALE.name(), 5, 1, List.of(), List.of(OXYGEN),
                        firstMonday, firstMonday.plusDays(1), ONCOLOGY),
                stay("stay-51", "Alice", FEMALE.name(), 12, 2, List.of(NITROGEN), List.of(TELEMETRY),
                        firstMonday.plusDays(4), firstMonday.plusDays(7), CARDIOLOGY),
                stay("stay-52", "Vince", MALE.name(), 19, 2, List.of(TELEMETRY, TELEVISION), List.of(OXYGEN),
                        firstMonday.plusDays(10), firstMonday.plusDays(12), NEUROLOGY),
                stay("stay-53", "Carol", FEMALE.name(), 25, 1, List.of(TELEVISION, OXYGEN, NITROGEN), List.of(TELEMETRY),
                        firstMonday.plusDays(16), firstMonday.plusDays(21), ONCOLOGY),
                stay("stay-54", "Eve", FEMALE.name(), 33, 1, List.of(), List.of(NITROGEN),
                        firstMonday, firstMonday.plusDays(3), NEUROLOGY),
                stay("stay-55", "Aiden", MALE.name(), 36, 1, List.of(), List.of(TELEMETRY),
                        firstMonday.plusDays(6), firstMonday.plusDays(8), ONCOLOGY),
                stay("stay-56", "Grace", FEMALE.name(), 41, 1, List.of(), List.of(TELEVISION),
                        firstMonday.plusDays(12), firstMonday.plusDays(17), CARDIOLOGY),
                stay("stay-57", "Carlos", MALE.name(), 44, 1, List.of(), List.of(OXYGEN),
                        firstMonday.plusDays(21), firstMonday.plusDays(25), NEUROLOGY),
                stay("stay-58", "Ivy", FEMALE.name(), 47, 1, List.of(), List.of(NITROGEN),
                        firstMonday.plusDays(29), firstMonday.plusDays(30), ONCOLOGY),
                stay("stay-59", "Elian", MALE.name(), 50, 2, List.of(), List.of(TELEMETRY),
                        firstMonday, firstMonday.plusDays(2), CARDIOLOGY),
                stay("stay-60", "Felix", MALE.name(), 52, 2, List.of(), List.of(TELEVISION),
                        firstMonday.plusDays(5), firstMonday.plusDays(10), NEUROLOGY),
                stay("stay-61", "Gustavo", MALE.name(), 54, 1, List.of(), List.of(OXYGEN),
                        firstMonday.plusDays(14), firstMonday.plusDays(18), ONCOLOGY),
                stay("stay-62", "Ian", MALE.name(), 55, 2, List.of(), List.of(NITROGEN),
                        firstMonday.plusDays(22), firstMonday.plusDays(23), CARDIOLOGY),
                stay("stay-63", "Marcus", MALE.name(), 58, 2, List.of(), List.of(TELEMETRY),
                        firstMonday.plusDays(26), firstMonday.plusDays(29), NEUROLOGY),
                stay("stay-64", "Bob", MALE.name(), 60, 1, List.of(), List.of(TELEVISION),
                        firstMonday, firstMonday.plusDays(5), ONCOLOGY),
                stay("stay-65", "David", MALE.name(), 63, 2, List.of(), List.of(OXYGEN),
                        firstMonday.plusDays(9), firstMonday.plusDays(13), CARDIOLOGY),
                stay("stay-66", "Frank", MALE.name(), 66, 2, List.of(), List.of(NITROGEN),
                        firstMonday.plusDays(17), firstMonday.plusDays(18), NEUROLOGY),
                stay("stay-67", "Hank", MALE.name(), 68, 1, List.of(), List.of(TELEMETRY),
                        firstMonday.plusDays(21), firstMonday.plusDays(24), ONCOLOGY),
                stay("stay-68", "Jack", MALE.name(), 70, 2, List.of(), List.of(TELEVISION),
                        firstMonday.plusDays(27), firstMonday.plusDays(29), CARDIOLOGY),
                stay("stay-69", "Leo", MALE.name(), 72, 2, List.of(OXYGEN, TELEMETRY), List.of(TELEVISION),
                        firstMonday, firstMonday.plusDays(4), NEUROLOGY),
                stay("stay-70", "Oscar", MALE.name(), 75, 1, List.of(), List.of(NITROGEN),
                        firstMonday.plusDays(8), firstMonday.plusDays(9), ONCOLOGY),
                stay("stay-71", "Quinn", MALE.name(), 80, 2, List.of(OXYGEN), List.of(TELEMETRY),
                        firstMonday.plusDays(12), firstMonday.plusDays(15), CARDIOLOGY),
                stay("stay-72", "Victor", MALE.name(), 92, 1, List.of(), List.of(OXYGEN),
                        firstMonday.plusDays(23), firstMonday.plusDays(28), ONCOLOGY),
                stay("stay-73", "Zack", MALE.name(), 5, 2, List.of(), List.of(NITROGEN),
                        firstMonday, firstMonday.plusDays(1), CARDIOLOGY),
                stay("stay-74", "Aaron", MALE.name(), 12, 2, List.of(OXYGEN), List.of(TELEMETRY),
                        firstMonday.plusDays(4), firstMonday.plusDays(7), NEUROLOGY),
                stay("stay-75", "Caleb", MALE.name(), 19, 1, List.of(TELEMETRY, OXYGEN), List.of(TELEVISION),
                        firstMonday.plusDays(10), firstMonday.plusDays(12), ONCOLOGY),
                stay("stay-76", "Ethan", MALE.name(), 25, 2, List.of(), List.of(OXYGEN),
                        firstMonday.plusDays(16), firstMonday.plusDays(21), CARDIOLOGY),
                stay("stay-77", "Hugo", MALE.name(), 28, 2, List.of(TELEMETRY), List.of(NITROGEN),
                        firstMonday.plusDays(25), firstMonday.plusDays(29), NEUROLOGY),
                stay("stay-78", "Karen", FEMALE.name(), 33, 1, List.of(TELEVISION), List.of(TELEMETRY),
                        firstMonday, firstMonday.plusDays(3), ONCOLOGY),
                stay("stay-79", "Mona", FEMALE.name(), 36, 2, List.of(NITROGEN, TELEVISION), List.of(TELEMETRY),
                        firstMonday.plusDays(6), firstMonday.plusDays(8), CARDIOLOGY),
                stay("stay-80", "Nora", FEMALE.name(), 41, 2, List.of(), List.of(OXYGEN),
                        firstMonday.plusDays(12), firstMonday.plusDays(17), NEUROLOGY),
                stay("stay-81", "Priya", FEMALE.name(), 44, 1, List.of(NITROGEN), List.of(TELEMETRY),
                        firstMonday.plusDays(21), firstMonday.plusDays(25), ONCOLOGY),
                stay("stay-82", "Rosa", FEMALE.name(), 47, 2, List.of(TELEVISION, NITROGEN), List.of(TELEMETRY),
                        firstMonday.plusDays(29), firstMonday.plusDays(30), CARDIOLOGY),
                stay("stay-83", "Tina", FEMALE.name(), 50, 2, List.of(NITROGEN, TELEVISION), List.of(TELEMETRY),
                        firstMonday, firstMonday.plusDays(2), NEUROLOGY),
                stay("stay-84", "Uma", FEMALE.name(), 52, 1, List.of(), List.of(OXYGEN),
                        firstMonday.plusDays(5), firstMonday.plusDays(10), ONCOLOGY),
                stay("stay-85", "Wendy", FEMALE.name(), 54, 2, List.of(NITROGEN), List.of(TELEMETRY),
                        firstMonday.plusDays(14), firstMonday.plusDays(18), CARDIOLOGY),
                stay("stay-86", "Xandra", FEMALE.name(), 55, 2, List.of(TELEVISION, NITROGEN), List.of(TELEMETRY),
                        firstMonday.plusDays(22), firstMonday.plusDays(23), NEUROLOGY),
                stay("stay-87", "Yara", FEMALE.name(), 58, 1, List.of(), List.of(TELEVISION),
                        firstMonday.plusDays(26), firstMonday.plusDays(29), ONCOLOGY),
                stay("stay-88", "Jorge", MALE.name(), 60, 1, List.of(OXYGEN, TELEMETRY), List.of(TELEVISION),
                        firstMonday, firstMonday.plusDays(5), CARDIOLOGY),
                stay("stay-89", "Bella", FEMALE.name(), 63, 1, List.of(TELEMETRY, NITROGEN, OXYGEN), List.of(TELEVISION),
                        firstMonday.plusDays(9), firstMonday.plusDays(13), NEUROLOGY),
                stay("stay-90", "Liam", MALE.name(), 66, 1, List.of(), List.of(TELEMETRY),
                        firstMonday.plusDays(17), firstMonday.plusDays(18), ONCOLOGY),
                stay("stay-91", "Dana", FEMALE.name(), 68, 1, List.of(OXYGEN), List.of(TELEVISION),
                        firstMonday.plusDays(21), firstMonday.plusDays(24), CARDIOLOGY),
                stay("stay-92", "Noah", MALE.name(), 70, 1, List.of(TELEMETRY, NITROGEN), List.of(OXYGEN),
                        firstMonday.plusDays(27), firstMonday.plusDays(29), NEUROLOGY),
                stay("stay-93", "Fiona", FEMALE.name(), 72, 1, List.of(NITROGEN, TELEMETRY, TELEVISION, OXYGEN), List.of(),
                        firstMonday, firstMonday.plusDays(4), ONCOLOGY),
                stay("stay-94", "Pablo", MALE.name(), 75, 2, List.of(), List.of(TELEMETRY),
                        firstMonday.plusDays(8), firstMonday.plusDays(9), CARDIOLOGY),
                stay("stay-95", "Gina", FEMALE.name(), 80, 2, List.of(TELEVISION), List.of(TELEMETRY),
                        firstMonday.plusDays(12), firstMonday.plusDays(15), NEUROLOGY),
                stay("stay-96", "Ravi", MALE.name(), 88, 1, List.of(OXYGEN, NITROGEN), List.of(TELEMETRY),
                        firstMonday.plusDays(18), firstMonday.plusDays(20), ONCOLOGY),
                stay("stay-97", "Iris", FEMALE.name(), 92, 2, List.of(NITROGEN, TELEMETRY, TELEVISION), List.of(OXYGEN),
                        firstMonday.plusDays(23), firstMonday.plusDays(28), CARDIOLOGY),
                stay("stay-98", "Theo", MALE.name(), 5, 2, List.of(), List.of(TELEMETRY),
                        firstMonday, firstMonday.plusDays(1), NEUROLOGY),
                stay("stay-99", "Kira", FEMALE.name(), 12, 1, List.of(TELEVISION), List.of(TELEMETRY),
                        firstMonday.plusDays(4), firstMonday.plusDays(7), ONCOLOGY),
                stay("stay-100", "Vince", MALE.name(), 19, 2, List.of(OXYGEN, NITROGEN), List.of(TELEMETRY),
                        firstMonday.plusDays(10), firstMonday.plusDays(12), CARDIOLOGY),
                stay("stay-101", "Mira", FEMALE.name(), 25, 2, List.of(NITROGEN, TELEMETRY, TELEVISION), List.of(OXYGEN),
                        firstMonday.plusDays(16), firstMonday.plusDays(21), NEUROLOGY),
                stay("stay-102", "Olga", FEMALE.name(), 33, 2, List.of(), List.of(TELEVISION),
                        firstMonday, firstMonday.plusDays(3), CARDIOLOGY),
                stay("stay-103", "Queenie", FEMALE.name(), 36, 2, List.of(), List.of(OXYGEN),
                        firstMonday.plusDays(6), firstMonday.plusDays(8), NEUROLOGY),
                stay("stay-104", "Sofia", FEMALE.name(), 41, 1, List.of(), List.of(NITROGEN),
                        firstMonday.plusDays(12), firstMonday.plusDays(17), ONCOLOGY),
                stay("stay-105", "Ursula", FEMALE.name(), 44, 2, List.of(), List.of(TELEMETRY),
                        firstMonday.plusDays(21), firstMonday.plusDays(25), CARDIOLOGY),
                stay("stay-106", "Renata", FEMALE.name(), 47, 2, List.of(), List.of(TELEVISION),
                        firstMonday.plusDays(29), firstMonday.plusDays(30), NEUROLOGY),
                stay("stay-107", "Delia", FEMALE.name(), 50, 1, List.of(), List.of(OXYGEN),
                        firstMonday, firstMonday.plusDays(2), ONCOLOGY),
                stay("stay-108", "Elena", FEMALE.name(), 52, 2, List.of(), List.of(NITROGEN),
                        firstMonday.plusDays(5), firstMonday.plusDays(10), CARDIOLOGY),
                stay("stay-109", "Helga", FEMALE.name(), 54, 2, List.of(), List.of(TELEMETRY),
                        firstMonday.plusDays(14), firstMonday.plusDays(18), NEUROLOGY),
                stay("stay-110", "Alice", FEMALE.name(), 55, 1, List.of(), List.of(TELEVISION),
                        firstMonday.plusDays(22), firstMonday.plusDays(23), ONCOLOGY),
                stay("stay-111", "Carol", FEMALE.name(), 58, 2, List.of(), List.of(OXYGEN),
                        firstMonday.plusDays(26), firstMonday.plusDays(29), CARDIOLOGY));

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
