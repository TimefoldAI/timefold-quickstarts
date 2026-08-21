package org.acme.bedallocation.demo;

import static java.time.temporal.TemporalAdjusters.firstInMonth;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import org.acme.bedallocation.dto.BedDTO;
import org.acme.bedallocation.dto.BedPlanInput;
import org.acme.bedallocation.dto.DepartmentDTO;
import org.acme.bedallocation.dto.RoomDTO;
import org.acme.bedallocation.dto.StayDTO;

/**
 * Builds a randomized (but seeded, so reproducible) demo dataset. Rooms and their beds are staged in
 * {@link RoomDraft}/mutable lists first, since the DTOs themselves are immutable records; the drafts are
 * converted to DTOs only once every random property has been assigned.
 */
public final class DemoDataBuilder {

    private static final List<String> SPECIALTIES = List.of("Specialty1", "Specialty2", "Specialty3");
    private static final String TELEMETRY = "telemetry";
    private static final String TELEVISION = "television";
    private static final String OXYGEN = "oxygen";
    private static final String NITROGEN = "nitrogen";
    private static final List<String> EQUIPMENTS = List.of(TELEMETRY, TELEVISION, OXYGEN, NITROGEN);

    private DemoDataBuilder() {
    }

    public static DemoDataBuilder builder() {
        return new DemoDataBuilder();
    }

    public BedPlanInput build() {
        Random random = new Random(0);

        Map<String, Integer> specialtyToPriority = new LinkedHashMap<>();
        specialtyToPriority.put(SPECIALTIES.get(0), 1);
        specialtyToPriority.put(SPECIALTIES.get(1), 2);
        specialtyToPriority.put(SPECIALTIES.get(2), 2);

        int countRooms = 10;
        List<RoomDraft> roomDrafts = generateRooms(countRooms, random);
        generateBeds(roomDrafts);

        int totalBeds = roomDrafts.stream().mapToInt(r -> r.beds.size()).sum();

        LocalDate firstMonthMonday = LocalDate.now().with(firstInMonth(DayOfWeek.MONDAY));
        List<LocalDate> dates = new ArrayList<>(7);
        dates.add(firstMonthMonday);
        int countDays = 28;
        for (int i = 1; i < countDays; i++) {
            dates.add(firstMonthMonday.with(firstInMonth(DayOfWeek.MONDAY)).plusDays(i));
        }

        List<StayDraft> stayDrafts = generateStays(countDays, totalBeds, random);
        generatePatients(stayDrafts, random);
        generateStayDates(stayDrafts, countRooms, dates, random);

        List<RoomDTO> rooms = roomDrafts.stream().map(RoomDraft::toDTO).toList();
        // Excludes newborns (age 0) and centenarians (age 101+), so the departmentMinimumAge and
        // departmentMaximumAge hard constraints have a (small) population to actually act on.
        DepartmentDTO department = DepartmentDTO.builder("1", "Department")
                .minimumAge(1)
                .maximumAge(100)
                .specialtyToPriority(specialtyToPriority)
                .rooms(rooms)
                .build();
        List<StayDTO> stays = stayDrafts.stream()
                .filter(s -> s.arrivalDate != null)
                .map(StayDraft::toDTO)
                .toList();
        return new BedPlanInput(List.of(department), stays);
    }

    private static List<RoomDraft> generateRooms(int size, Random random) {
        List<RoomDraft> rooms = IntStream.range(0, size)
                .mapToObj(i -> new RoomDraft(String.valueOf(i), "%s%d".formatted("Room", i)))
                .toList();

        // Room gender limitation
        applyRandomValue(size, rooms, r -> r.genderLimitation == null,
                r -> r.genderLimitation = "SAME_GENDER", random);

        // Room capacity
        List<Pair<Float, Integer>> capacityValues = List.of(
                new Pair<>(0.8f, 1), // 20% for capacity 1
                new Pair<>(0.1f, 2),
                new Pair<>(0.1f, 3));
        capacityValues.forEach(c -> applyRandomValue((int) (size * c.key()), rooms, r -> r.capacity == 0,
                r -> r.capacity = c.value(), random));
        rooms.stream()
                .filter(r -> r.capacity == 0)
                .toList()
                .forEach(r -> r.capacity = 1);

        // Room equipments
        // 11% - 1 equipment; 16% 2 equipments; 42% 3 equipments; 31% 4 equipments
        List<Double> countEquipments = List.of(0.11, 0.27, 0.69, 1d);
        Consumer<RoomDraft> equipmentConsumer = room -> {
            double count = random.nextDouble();
            int numEquipments = IntStream.range(0, countEquipments.size())
                    .filter(i -> count <= countEquipments.get(i))
                    .findFirst()
                    .getAsInt() + 1;
            List<String> roomEquipments = new LinkedList<>(EQUIPMENTS);
            Collections.shuffle(roomEquipments, random);
            room.equipments = roomEquipments.subList(0, numEquipments);
        };
        // Only 76% of rooms have equipment
        applyRandomValue((int) (0.76 * size), rooms, r -> r.equipments.isEmpty(), equipmentConsumer, random);

        return rooms;
    }

    private static void generateBeds(List<RoomDraft> rooms) {
        for (RoomDraft room : rooms) {
            IntStream.range(0, room.capacity)
                    .forEach(i -> room.beds.add(new BedDTO("%s-bed%d".formatted(room.id, i), i)));
        }
    }

    private static List<StayDraft> generateStays(int countDays, int totalBeds, Random random) {
        List<StayDraft> stays = IntStream.range(0, countDays * totalBeds)
                .mapToObj(i -> new StayDraft("stay-%d".formatted(i), "patient-%d".formatted(i)))
                .toList();

        // specialty - 27% Specialty1; 36% Specialty2; 37% Specialty3
        applyRandomValue((int) (0.27 * stays.size()), stays, s -> s.specialty == null,
                s -> s.specialty = SPECIALTIES.get(0), random);
        applyRandomValue((int) (0.36 * stays.size()), stays, s -> s.specialty == null,
                s -> s.specialty = SPECIALTIES.get(1), random);
        applyRandomValue((int) (0.37 * stays.size()), stays, s -> s.specialty == null,
                s -> s.specialty = SPECIALTIES.get(2), random);
        stays.stream()
                .filter(s -> s.specialty == null)
                .toList()
                .forEach(s -> s.specialty = SPECIALTIES.get(0));
        return stays;
    }

    private static void generateStayDates(List<StayDraft> stays, int laneCount, List<LocalDate> dates, Random random) {
        LocalDate initialDate = dates.get(0);
        LocalDate maxDate = dates.get(dates.size() - 1);
        List<Pair<Float, Integer>> periodCount = List.of(
                new Pair<>(0.05f, 1), // 5% one day
                new Pair<>(0.30f, 2), // 25% two days, etc
                new Pair<>(0.95f, 3),
                new Pair<>(1f, 4));
        for (int i = 0; i < laneCount; i++) {
            LocalDate currentDate = LocalDate.from(initialDate);
            while (currentDate.isBefore(maxDate)) {
                double countDays = random.nextDouble();
                int numDays = periodCount.stream()
                        .filter(p -> countDays <= p.key())
                        .mapToInt(Pair::value)
                        .findFirst()
                        .getAsInt();
                LocalDate nextDate = currentDate.plusDays(numDays);
                if (nextDate.isAfter(maxDate)) {
                    nextDate = maxDate;
                }
                LocalDate finalCurrentDate = currentDate;
                LocalDate finalNextDate = nextDate;
                applyRandomValue(1, stays, stay -> stay.arrivalDate == null, stay -> {
                    stay.arrivalDate = finalCurrentDate;
                    stay.departureDate = finalNextDate;
                }, random);
                currentDate = nextDate.plusDays(1);
            }
        }
    }

    private static void generatePatients(List<StayDraft> stays, Random random) {
        // 50% MALE - 50% FEMALE
        applyRandomValue((int) (stays.size() * 0.5), stays, p -> p.patientGender == null, p -> p.patientGender = "MALE",
                random);
        applyRandomValue((int) (stays.size() * 0.5), stays, p -> p.patientGender == null, p -> p.patientGender = "FEMALE",
                random);
        stays.stream().filter(p -> p.patientGender == null).forEach(p -> p.patientGender = "MALE");

        // Age group
        List<Pair<Float, Integer[]>> ageValues = List.of(
                new Pair<>(0.1f, new Integer[] { 1, 10 }), // 10% for age group [1, 10]
                new Pair<>(0.09f, new Integer[] { 11, 20 }),
                new Pair<>(0.07f, new Integer[] { 21, 30 }),
                new Pair<>(0.1f, new Integer[] { 31, 40 }),
                new Pair<>(0.09f, new Integer[] { 41, 50 }),
                new Pair<>(0.08f, new Integer[] { 51, 60 }),
                new Pair<>(0.08f, new Integer[] { 61, 70 }),
                new Pair<>(0.13f, new Integer[] { 71, 80 }),
                new Pair<>(0.08f, new Integer[] { 81, 90 }),
                new Pair<>(0.09f, new Integer[] { 91, 100 }));

        ageValues.forEach(ag -> applyRandomValue((int) (ag.key() * stays.size()), stays, a -> a.patientAge == -1,
                p -> p.patientAge = random.nextInt(ag.value()[0], ag.value()[1] + 1), random));
        stays.stream()
                .filter(p -> p.patientAge == -1)
                .toList()
                .forEach(p -> p.patientAge = 71);

        // Preferred maximum capacity
        List<Pair<Float, Integer>> capacityValues = List.of(
                new Pair<>(0.34f, 1), // 34% for capacity 1
                new Pair<>(0.68f, 2),
                new Pair<>(1f, 3));
        for (StayDraft stay : stays) {
            double count = random.nextDouble();
            IntStream.range(0, capacityValues.size())
                    .filter(i -> count <= capacityValues.get(i).key())
                    .map(i -> capacityValues.get(i).value())
                    .findFirst()
                    .ifPresent(value -> stay.patientPreferredMaximumRoomCapacity = value);
        }

        // Required equipments - 12% no equipments; 47% one equipment; 41% two equipments
        List<Pair<Float, String>> oneEquipmentValues = List.of(
                new Pair<>(0.22f, NITROGEN), // 22% for nitrogen
                new Pair<>(0.47f, TELEVISION),
                new Pair<>(0.72f, OXYGEN),
                new Pair<>(1f, TELEMETRY));
        BiConsumer<StayDraft, List<Pair<Float, String>>> oneEquipmentConsumer = (stay, values) -> {
            double count = random.nextDouble();
            IntStream.range(0, values.size())
                    .filter(i -> count <= values.get(i).key())
                    .mapToObj(i -> values.get(i).value())
                    .findFirst()
                    .ifPresent(stay::addRequiredEquipment);
        };
        applyRandomValue((int) (stays.size() * 0.47), stays, oneEquipmentValues,
                p -> p.patientRequiredEquipments.isEmpty(), oneEquipmentConsumer, random);
        // Two required equipments
        List<Pair<Float, String>> twoEquipmentValues = List.of(
                new Pair<>(0.13f, NITROGEN), // 13% for nitrogen
                new Pair<>(0.29f, TELEVISION),
                new Pair<>(0.49f, OXYGEN),
                new Pair<>(1f, TELEMETRY));
        Consumer<StayDraft> twoEquipmentsConsumer = patient -> {
            while (patient.patientRequiredEquipments.size() < 2) {
                oneEquipmentConsumer.accept(patient, twoEquipmentValues);
            }
        };
        applyRandomValue((int) (stays.size() * 0.41), stays, p -> p.patientRequiredEquipments.isEmpty(),
                twoEquipmentsConsumer, random);

        // Preferred equipments - 29% one equipment; 53% two equipments; 16% three equipments; 2% four equipments
        List<Pair<Float, String>> onePreferredEquipmentValues = List.of(
                new Pair<>(0.34f, NITROGEN), // 34% for nitrogen
                new Pair<>(0.63f, TELEVISION),
                new Pair<>(1f, OXYGEN));
        BiConsumer<StayDraft, List<Pair<Float, String>>> onePreferredEquipmentConsumer = (patient, values) -> {
            double count = random.nextDouble();
            IntStream.range(0, values.size())
                    .filter(i -> count <= values.get(i).key())
                    .mapToObj(i -> values.get(i).value())
                    .findFirst()
                    .ifPresent(patient::addPreferredEquipment);
        };
        applyRandomValue((int) (stays.size() * 0.29), stays, onePreferredEquipmentValues,
                p -> p.patientPreferredEquipments.isEmpty(), onePreferredEquipmentConsumer, random);
        // two preferred equipments
        List<Pair<Float, String>> twoPreferredEquipmentValues = List.of(
                new Pair<>(0.32f, NITROGEN), // 32% for nitrogen
                new Pair<>(0.62f, TELEVISION),
                new Pair<>(0.90f, OXYGEN),
                new Pair<>(1f, TELEMETRY));
        Consumer<StayDraft> twoPreferredEquipmentsConsumer = patient -> {
            while (patient.patientPreferredEquipments.size() < 2) {
                onePreferredEquipmentConsumer.accept(patient, twoPreferredEquipmentValues);
            }
        };
        applyRandomValue((int) (stays.size() * 0.53), stays, p -> p.patientPreferredEquipments.isEmpty(),
                twoPreferredEquipmentsConsumer, random);
        // three preferred equipments
        List<Pair<Float, String>> threePreferredEquipmentValues = List.of(
                new Pair<>(0.26f, NITROGEN), // 26% for nitrogen
                new Pair<>(0.50f, TELEVISION),
                new Pair<>(0.77f, OXYGEN),
                new Pair<>(1f, TELEMETRY));
        Consumer<StayDraft> threePreferredEquipmentsConsumer = patient -> {
            while (patient.patientPreferredEquipments.size() < 3) {
                onePreferredEquipmentConsumer.accept(patient, threePreferredEquipmentValues);
            }
        };
        applyRandomValue((int) (stays.size() * 0.16), stays, p -> p.patientPreferredEquipments.isEmpty(),
                threePreferredEquipmentsConsumer, random);
        // four preferred equipments
        Consumer<StayDraft> fourPreferredEquipmentsConsumer = patient -> {
            patient.addPreferredEquipment(NITROGEN);
            patient.addPreferredEquipment(TELEVISION);
            patient.addPreferredEquipment(OXYGEN);
            patient.addPreferredEquipment(TELEMETRY);
        };
        applyRandomValue((int) (stays.size() * 0.02), stays, p -> p.patientPreferredEquipments.isEmpty(),
                fourPreferredEquipmentsConsumer, random);

        stays.stream()
                .filter(p -> p.patientPreferredEquipments.isEmpty())
                .toList()
                .forEach(p -> onePreferredEquipmentConsumer.accept(p, onePreferredEquipmentValues));
    }

    private static <T> void applyRandomValue(int count, List<T> values, Predicate<T> filter, Consumer<T> consumer,
            Random random) {
        int size = (int) values.stream().filter(filter).count();
        for (int i = 0; i < count; i++) {
            values.stream()
                    .filter(filter)
                    .skip(size > 0 ? random.nextInt(size) : 0).findFirst()
                    .ifPresent(consumer::accept);
            size--;
            if (size < 0) {
                break;
            }
        }
    }

    private static <T, L> void applyRandomValue(int count, List<T> values, L secondParam, Predicate<T> filter,
            BiConsumer<T, L> consumer, Random random) {
        int size = (int) values.stream().filter(filter).count();
        for (int i = 0; i < count; i++) {
            values.stream()
                    .filter(filter)
                    .skip(size > 0 ? random.nextInt(size) : 0).findFirst()
                    .ifPresent(v -> consumer.accept(v, secondParam));
            size--;
            if (size < 0) {
                break;
            }
        }
    }

    private record Pair<K, V>(K key, V value) {
    }

    /**
     * Mutable staging object for a room, since {@link RoomDTO} is an immutable record.
     */
    private static final class RoomDraft {

        private final String id;
        private final String name;
        private String genderLimitation = "ANY_GENDER";
        private int capacity;
        private List<String> equipments = List.of();
        private final List<BedDTO> beds = new ArrayList<>();

        private RoomDraft(String id, String name) {
            this.id = id;
            this.name = name;
        }

        private RoomDTO toDTO() {
            return new RoomDTO(id, name, capacity, genderLimitation, equipments, beds);
        }
    }

    /**
     * Mutable staging object for a stay, since {@link StayDTO} is an immutable record.
     */
    private static final class StayDraft {

        private final String id;
        private final String patientName;
        private String patientGender;
        private int patientAge = -1;
        private Integer patientPreferredMaximumRoomCapacity;
        private final List<String> patientRequiredEquipments = new ArrayList<>();
        private final List<String> patientPreferredEquipments = new ArrayList<>();
        private LocalDate arrivalDate;
        private LocalDate departureDate;
        private String specialty;

        private StayDraft(String id, String patientName) {
            this.id = id;
            this.patientName = patientName;
        }

        private void addRequiredEquipment(String equipment) {
            if (!patientRequiredEquipments.contains(equipment)) {
                patientRequiredEquipments.add(equipment);
            }
        }

        private void addPreferredEquipment(String equipment) {
            if (!patientPreferredEquipments.contains(equipment)) {
                patientPreferredEquipments.add(equipment);
            }
        }

        private StayDTO toDTO() {
            return StayDTO.builder(id, arrivalDate.toString(), departureDate.toString())
                    .patientName(patientName)
                    .patientGender(patientGender)
                    .patientAge(patientAge)
                    .patientPreferredMaximumRoomCapacity(patientPreferredMaximumRoomCapacity)
                    .patientRequiredEquipments(patientRequiredEquipments)
                    .patientPreferredEquipments(patientPreferredEquipments)
                    .specialty(specialty)
                    .build();
        }
    }
}
