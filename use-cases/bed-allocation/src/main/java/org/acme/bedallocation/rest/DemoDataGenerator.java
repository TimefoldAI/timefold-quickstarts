package org.acme.bedallocation.rest;

import static java.time.temporal.TemporalAdjusters.firstInMonth;
import static org.acme.bedallocation.domain.Gender.FEMALE;
import static org.acme.bedallocation.domain.Gender.MALE;
import static org.acme.bedallocation.domain.GenderLimitation.SAME_GENDER;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.core.impl.util.Pair;

import org.acme.bedallocation.domain.Bed;
import org.acme.bedallocation.domain.BedPlan;
import org.acme.bedallocation.domain.Department;
import org.acme.bedallocation.domain.DepartmentSpecialty;
import org.acme.bedallocation.domain.Room;
import org.acme.bedallocation.domain.Stay;

@ApplicationScoped
public class DemoDataGenerator {

    private static final List<String> SPECIALTIES = List.of("Specialty1", "Specialty2", "Specialty3");
    private static final String TELEMETRY = "telemetry";
    private static final String TELEVISION = "television";
    private static final String OXYGEN = "oxygen";
    private static final String NITROGEN = "nitrogen";
    private static final List<String> EQUIPMENTS = List.of(TELEMETRY, TELEVISION, OXYGEN, NITROGEN);
    private static final List<LocalDate> DATES = List.of(
            LocalDate.now().with(firstInMonth(DayOfWeek.MONDAY)), // First Monday of the month
            LocalDate.now().with(firstInMonth(DayOfWeek.MONDAY)).plusDays(1),
            LocalDate.now().with(firstInMonth(DayOfWeek.MONDAY)).plusDays(2),
            LocalDate.now().with(firstInMonth(DayOfWeek.MONDAY)).plusDays(3),
            LocalDate.now().with(firstInMonth(DayOfWeek.MONDAY)).plusDays(4),
            LocalDate.now().with(firstInMonth(DayOfWeek.MONDAY)).plusDays(5),
            LocalDate.now().with(firstInMonth(DayOfWeek.MONDAY)).plusDays(6));
    private final Random random = new Random(0);

    public BedPlan generateDemoData() {
        BedPlan schedule = new BedPlan();
        // Department
        List<Department> departments = List.of(new Department("1", "Department"));
        schedule.setDepartments(departments);
        schedule.getDepartments().get(0).getSpecialtyToPriority().put(SPECIALTIES.get(0), 1);
        schedule.getDepartments().get(0).getSpecialtyToPriority().put(SPECIALTIES.get(1), 2);
        schedule.getDepartments().get(0).getSpecialtyToPriority().put(SPECIALTIES.get(2), 2);
        schedule.setDepartmentSpecialties(departments.stream()
                .flatMap(d -> d.getSpecialtyToPriority().entrySet().stream()
                        .map(e -> new DepartmentSpecialty("%s-%s".formatted(d.getId(), e.getKey()), d, e.getKey(),
                                e.getValue()))
                        .toList()
                        .stream())
                .toList());

        // Rooms
        schedule.getDepartments().get(0).setRooms(generateRooms(25, departments));
        schedule.setRooms(departments.stream().flatMap(d -> d.getRooms().stream()).toList());
        // Beds
        generateBeds(schedule.getRooms());
        schedule.setBeds(departments.stream()
                .flatMap(d -> d.getRooms().stream())
                .flatMap(r -> r.getBeds().stream())
                .toList());
        // Stays
        schedule.setStays(generateStays(40, SPECIALTIES));
        // Patients
        generatePatients(schedule.getStays());

        return schedule;
    }

    private List<Room> generateRooms(int size, List<Department> departments) {
        List<Room> rooms = IntStream.range(0, size)
                .mapToObj(i -> new Room(String.valueOf(i), "%s%d".formatted("Room", i), departments.get(0)))
                .toList();

        // Room gender limitation
        applyRandomValue(size, rooms, r -> r.getGenderLimitation() == null,
                r -> r.setGenderLimitation(SAME_GENDER));

        // Room capacity
        List<Pair<Float, Integer>> capacityValues = List.of(
                new Pair<>(0.2f, 1), // 20% for capacity 1
                new Pair<>(0.32f, 2),
                new Pair<>(0.48f, 4));
        capacityValues.forEach(c -> applyRandomValue((int) (size * c.key()), rooms, r -> r.getCapacity() == 0,
                r -> r.setCapacity(c.value())));
        rooms.stream()
                .filter(r -> r.getCapacity() == 0)
                .toList()
                .forEach(r -> r.setCapacity(1));

        // Room equipments
        // 11% - 1 equipment; 16% 2 equipments; 42% 3 equipments; 31% 4 equipments
        List<Double> countEquipments = List.of(0.11, 0.27, 0.69, 1d);
        Consumer<Room> equipmentConsumer = room -> {
            double count = random.nextDouble();
            int numEquipments = IntStream.range(0, countEquipments.size())
                    .filter(i -> count <= countEquipments.get(i))
                    .findFirst()
                    .getAsInt() + 1;
            List<String> roomEquipments = new LinkedList<>(EQUIPMENTS);
            Collections.shuffle(roomEquipments, random);
            room.setEquipments(roomEquipments.subList(0, numEquipments));
        };
        // Only 76% of rooms have equipment
        applyRandomValue((int) (0.76 * size), rooms, r -> r.getEquipments().isEmpty(), equipmentConsumer);

        return rooms;
    }

    private void generateBeds(List<Room> rooms) {
        // 20% - 1 bed; 32% 2 beds; 48% 4 beds
        List<Double> countBeds = List.of(0.2, 0.52, 1d);
        for (Room room : rooms) {
            double count = random.nextDouble();
            int numBeds = IntStream.range(0, countBeds.size())
                    .filter(i -> count <= countBeds.get(i))
                    .findFirst()
                    .getAsInt() + 1;
            IntStream.range(0, numBeds)
                    .forEach(i -> room.addBed(new Bed("%s-bed%d".formatted(room.getId(), i), room, i)));
        }
    }

    private List<Stay> generateStays(int size, List<String> specialties) {
        List<Stay> stays = IntStream.range(0, size)
                .mapToObj(i -> new Stay("stay-%s".formatted(i), "patient%d".formatted(i)))
                .toList();

        // specialty - 27% Specialty1; 36% Specialty2; 37% Specialty3
        applyRandomValue((int) (0.27 * size), stays, s -> s.getSpecialty() == null,
                s -> s.setSpecialty(specialties.get(0)));
        applyRandomValue((int) (0.36 * size), stays, s -> s.getSpecialty() == null,
                s -> s.setSpecialty(specialties.get(1)));
        applyRandomValue((int) (0.37 * size), stays, s -> s.getSpecialty() == null,
                s -> s.setSpecialty(specialties.get(2)));
        stays.stream()
                .filter(s -> s.getSpecialty() == null)
                .toList()
                .forEach(s -> s.setSpecialty(specialties.get(0)));

        // Start date - 18% Mon/Fri and 5% Sat/Sun
        // Stay period
        List<Pair<Float, Integer>> periodCount = List.of(
                new Pair<>(0f, 0), // 16% for 0 days - workaround: using 0% to force the UI show the card information
                new Pair<>(0.18f, 1), // 18% one day, etc
                new Pair<>(0.06f, 2),
                new Pair<>(0.13f, 3),
                new Pair<>(0.22f, 4),
                new Pair<>(0.19f, 5),
                new Pair<>(0.06f, 6));
        BiConsumer<Stay, Integer> dateConsumer = (stay, count) -> {
            int start = random.nextInt(DATES.size() - count);
            stay.setArrivalDate(DATES.get(start));
            stay.setDepartureDate(DATES.get(start + count));
        };
        periodCount.forEach(p -> applyRandomValue((int) (p.key() * stays.size()), stays, p.value(),
                s -> s.getArrivalDate() == null, dateConsumer));
        stays.stream()
                .filter(s -> s.getArrivalDate() == null)
                .toList()
                .forEach(s -> dateConsumer.accept(s, 2));
        return stays;
    }

    private void generatePatients(List<Stay> stays) {
        // 50% MALE - 50% FEMALE
        applyRandomValue((int) (stays.size() * 0.5), stays, p -> p.getPatientGender() == null, p -> p.setPatientGender(MALE));
        applyRandomValue((int) (stays.size() * 0.5), stays, p -> p.getPatientGender() == null, p -> p.setPatientGender(FEMALE));
        stays.stream().filter(p -> p.getPatientGender() == null).forEach(p -> p.setPatientGender(MALE));

        // Age group
        List<Pair<Float, Integer[]>> ageValues = List.of(
                new Pair<>(0.1f, new Integer[] { 0, 10 }), // 10% for age group [0, 10]
                new Pair<>(0.09f, new Integer[] { 11, 20 }),
                new Pair<>(0.07f, new Integer[] { 21, 30 }),
                new Pair<>(0.1f, new Integer[] { 31, 40 }),
                new Pair<>(0.09f, new Integer[] { 41, 50 }),
                new Pair<>(0.08f, new Integer[] { 51, 60 }),
                new Pair<>(0.08f, new Integer[] { 61, 70 }),
                new Pair<>(0.13f, new Integer[] { 71, 80 }),
                new Pair<>(0.08f, new Integer[] { 81, 90 }),
                new Pair<>(0.09f, new Integer[] { 91, 100 }),
                new Pair<>(0.09f, new Integer[] { 101, 109 }));

        ageValues.forEach(ag -> applyRandomValue((int) (ag.key() * stays.size()), stays, a -> a.getPatientAge() == -1,
                p -> p.setPatientAge(random.nextInt(ag.value()[0], ag.value()[1] + 1))));
        stays.stream()
                .filter(p -> p.getPatientAge() == -1)
                .toList()
                .forEach(p -> p.setPatientAge(71));

        // Preferred maximum capacity
        List<Pair<Float, Integer>> capacityValues = List.of(
                new Pair<>(0.34f, 1), // 34% for capacity 1
                new Pair<>(0.68f, 2),
                new Pair<>(1f, 4));
        for (Stay stay : stays) {
            double count = random.nextDouble();
            IntStream.range(0, capacityValues.size())
                    .filter(i -> count <= capacityValues.get(i).key())
                    .map(i -> capacityValues.get(i).value())
                    .findFirst()
                    .ifPresent(stay::setPatientPreferredMaximumRoomCapacity);
        }

        // Required equipments - 12% no equipments; 47% one equipment; 41% two equipments
        // one required equipment
        List<Pair<Float, String>> oneEquipmentValues = List.of(
                new Pair<>(0.22f, NITROGEN), // 22% for nitrogen
                new Pair<>(0.47f, TELEVISION),
                new Pair<>(0.72f, OXYGEN),
                new Pair<>(1f, TELEMETRY));
        BiConsumer<Stay, List<Pair<Float, String>>> oneEquipmentConsumer = (stay, values) -> {
            double count = random.nextDouble();
            IntStream.range(0, values.size())
                    .filter(i -> count <= values.get(i).key())
                    .mapToObj(i -> values.get(i).value())
                    .findFirst()
                    .ifPresent(stay::addRequiredEquipment);
        };
        applyRandomValue((int) (stays.size() * 0.47), stays, oneEquipmentValues,
                p -> p.getPatientRequiredEquipments().isEmpty(),
                oneEquipmentConsumer);
        // Two required equipments
        List<Pair<Float, String>> twoEquipmentValues = List.of(
                new Pair<>(0.13f, NITROGEN), // 13% for nitrogen
                new Pair<>(0.29f, TELEVISION),
                new Pair<>(0.49f, OXYGEN),
                new Pair<>(1f, TELEMETRY));
        Consumer<Stay> twoEquipmentsConsumer = patient -> {
            while (patient.getPatientRequiredEquipments().size() < 2) {
                oneEquipmentConsumer.accept(patient, twoEquipmentValues);
            }
        };
        applyRandomValue((int) (stays.size() * 0.41), stays, p -> p.getPatientRequiredEquipments().isEmpty(),
                twoEquipmentsConsumer);

        // Preferred equipments - 29% one equipment; 53% two equipments; 16% three equipments; 2% four equipments
        // one preferred equipment
        List<Pair<Float, String>> onePreferredEquipmentValues = List.of(
                new Pair<>(0.34f, NITROGEN), // 34% for nitrogen
                new Pair<>(0.63f, TELEVISION),
                new Pair<>(1f, OXYGEN));
        BiConsumer<Stay, List<Pair<Float, String>>> onePreferredEquipmentConsumer = (patient, values) -> {
            double count = random.nextDouble();
            IntStream.range(0, values.size())
                    .filter(i -> count <= values.get(i).key())
                    .mapToObj(i -> values.get(i).value())
                    .findFirst()
                    .ifPresent(patient::addPreferredEquipment);
        };
        applyRandomValue((int) (stays.size() * 0.29), stays, onePreferredEquipmentValues,
                p -> p.getPatientPreferredEquipments().isEmpty(),
                onePreferredEquipmentConsumer);
        // two preferred equipments
        List<Pair<Float, String>> twoPreferredEquipmentValues = List.of(
                new Pair<>(0.32f, NITROGEN), // 32% for nitrogen
                new Pair<>(0.62f, TELEVISION),
                new Pair<>(0.90f, OXYGEN),
                new Pair<>(1f, TELEMETRY));
        Consumer<Stay> twoPreferredEquipmentsConsumer = patient -> {
            while (patient.getPatientPreferredEquipments().size() < 2) {
                onePreferredEquipmentConsumer.accept(patient, twoPreferredEquipmentValues);
            }
        };
        applyRandomValue((int) (stays.size() * 0.53), stays, p -> p.getPatientPreferredEquipments().isEmpty(),
                twoPreferredEquipmentsConsumer);
        // three preferred equipments
        List<Pair<Float, String>> threePreferredEquipmentValues = List.of(
                new Pair<>(0.26f, NITROGEN), // 26% for nitrogen
                new Pair<>(0.50f, TELEVISION),
                new Pair<>(0.77f, OXYGEN),
                new Pair<>(1f, TELEMETRY));
        Consumer<Stay> threePreferredEquipmentsConsumer = patient -> {
            while (patient.getPatientPreferredEquipments().size() < 3) {
                onePreferredEquipmentConsumer.accept(patient, threePreferredEquipmentValues);
            }
        };
        applyRandomValue((int) (stays.size() * 0.16), stays, p -> p.getPatientPreferredEquipments().isEmpty(),
                threePreferredEquipmentsConsumer);
        // four preferred equipments
        Consumer<Stay> fourPreferredEquipmentsConsumer = patient -> {
            patient.addPreferredEquipment(NITROGEN);
            patient.addPreferredEquipment(TELEVISION);
            patient.addPreferredEquipment(OXYGEN);
            patient.addPreferredEquipment(TELEMETRY);
        };
        applyRandomValue((int) (stays.size() * 0.02), stays, p -> p.getPatientPreferredEquipments().isEmpty(),
                fourPreferredEquipmentsConsumer);

        stays.stream()
                .filter(p -> p.getPatientPreferredEquipments().isEmpty())
                .toList()
                .forEach(p -> onePreferredEquipmentConsumer.accept(p, onePreferredEquipmentValues));
    }

    private <T> void applyRandomValue(int count, List<T> values, Predicate<T> filter, Consumer<T> consumer) {
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

    private <T, L> void applyRandomValue(int count, List<T> values, L secondParam, Predicate<T> filter,
            BiConsumer<T, L> consumer) {
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
}
