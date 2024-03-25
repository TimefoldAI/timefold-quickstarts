package org.acme.bedallocation.rest;

import static java.time.temporal.TemporalAdjusters.firstInMonth;
import static org.acme.bedallocation.domain.Gender.FEMALE;
import static org.acme.bedallocation.domain.Gender.MALE;
import static org.acme.bedallocation.domain.GenderLimitation.ANY_GENDER;
import static org.acme.bedallocation.domain.GenderLimitation.FEMALE_ONLY;
import static org.acme.bedallocation.domain.GenderLimitation.MALE_ONLY;
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
import org.acme.bedallocation.domain.BedDesignation;
import org.acme.bedallocation.domain.Department;
import org.acme.bedallocation.domain.GenderLimitation;
import org.acme.bedallocation.domain.Patient;
import org.acme.bedallocation.domain.Room;
import org.acme.bedallocation.domain.Schedule;
import org.acme.bedallocation.domain.Stay;

@ApplicationScoped
public class DemoDataGenerator {

    private static final List<String> SPECIALISMS = List.of("Specialism1", "Specialism2", "Specialism3");
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

    /**
     * The dataset was generated based on the probability distributions found in the test dataset file.
     * However, the number of patients was reduced to 40 to simplify the quickstart process.
     */
    public Schedule generateDemoData() {
        Schedule schedule = new Schedule();
        // Department
        List<Department> departments = List.of(new Department("1", "Department"));
        schedule.setDepartments(departments);
        schedule.getDepartments().get(0).getSpecialismsToPriority().put(SPECIALISMS.get(0), 1);
        schedule.getDepartments().get(0).getSpecialismsToPriority().put(SPECIALISMS.get(1), 2);
        schedule.getDepartments().get(0).getSpecialismsToPriority().put(SPECIALISMS.get(2), 2);
        // Rooms
        schedule.getDepartments().get(0).setRooms(generateRooms(25, departments));
        // Beds
        generateBeds(schedule.getRooms());
        // Patients
        List<Patient> patients = generatePatients(40);
        // Stays
        List<Stay> stays = generateStays(patients, SPECIALISMS);
        // Bed designations
        schedule.setBedDesignations(generateBedDesignations(stays));

        return schedule;
    }

    private List<Room> generateRooms(int size, List<Department> departments) {
        List<Room> rooms = IntStream.range(0, size)
                .mapToObj(i -> new Room(String.valueOf(i), "%s%d".formatted("Room", i), departments.get(0)))
                .toList();

        // Room gender limitation
        List<Pair<Float, GenderLimitation>> genderValues = List.of(
                new Pair<>(0.08f, SAME_GENDER), // 8% for SAME_GENDER
                new Pair<>(0.24f, MALE_ONLY),
                new Pair<>(0.32f, FEMALE_ONLY),
                new Pair<>(0.36f, ANY_GENDER));
        genderValues.forEach(g -> applyRandomValue((int) (size * g.key()), rooms, r -> r.getGenderLimitation() == null,
                r -> r.setGenderLimitation(g.value())));
        rooms.stream()
                .filter(g -> g.getGenderLimitation() == null)
                .toList()
                .forEach(r -> r.setGenderLimitation(ANY_GENDER));

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

        // Room specialism priority
        List<Pair<Double, Integer>> priorityValues = List.of(
                new Pair<>(0.72, 1), // 72% for priority 1
                new Pair<>(0.96, 2),
                new Pair<>(1d, 4));
        for (Room room : rooms) {
            SPECIALISMS.forEach(s -> {
                double index = random.nextDouble();
                priorityValues.stream()
                        .filter(p -> index <= p.key())
                        .findFirst()
                        .ifPresent(p -> room.addSpecialism(s, p.value()));
            });
        }

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

    private List<Patient> generatePatients(int size) {
        List<Patient> patients = IntStream.range(0, size)
                .mapToObj(i -> new Patient(String.valueOf(i), "Patient%d".formatted(i)))
                .toList();

        // 50% MALE - 50% FEMALE
        applyRandomValue(50, patients, p -> p.getGender() == null, p -> p.setGender(MALE));
        applyRandomValue(50, patients, p -> p.getGender() == null, p -> p.setGender(FEMALE));

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

        ageValues.forEach(ag -> applyRandomValue((int) (ag.key() * size), patients, a -> a.getAge() == -1,
                p -> p.setAge(random.nextInt(ag.value()[0], ag.value()[1] + 1))));
        patients.stream()
                .filter(p -> p.getAge() == -1)
                .toList()
                .forEach(p -> p.setAge(71));

        // Preferred maximum capacity
        List<Pair<Float, Integer>> capacityValues = List.of(
                new Pair<>(0.34f, 1), // 34% for capacity 1
                new Pair<>(0.68f, 2),
                new Pair<>(1f, 4));
        for (Patient patient : patients) {
            double count = random.nextDouble();
            IntStream.range(0, capacityValues.size())
                    .filter(i -> count <= capacityValues.get(i).key())
                    .map(i -> capacityValues.get(i).value())
                    .findFirst()
                    .ifPresent(patient::setPreferredMaximumRoomCapacity);
        }

        // Required equipments - 12% no equipments; 47% one equipment; 41% two equipments
        // one required equipment
        List<Pair<Float, String>> oneEquipmentValues = List.of(
                new Pair<>(0.22f, NITROGEN), // 22% for nitrogen
                new Pair<>(0.47f, TELEVISION),
                new Pair<>(0.72f, OXYGEN),
                new Pair<>(1f, TELEMETRY));
        BiConsumer<Patient, List<Pair<Float, String>>> oneEquipmentConsumer = (patient, values) -> {
            double count = random.nextDouble();
            IntStream.range(0, values.size())
                    .filter(i -> count <= values.get(i).key())
                    .mapToObj(i -> values.get(i).value())
                    .findFirst()
                    .ifPresent(patient::addRequiredEquipment);
        };
        applyRandomValue((int) (size * 0.47), patients, oneEquipmentValues, p -> p.getRequiredEquipments().isEmpty(),
                oneEquipmentConsumer);
        // Two required equipments
        List<Pair<Float, String>> twoEquipmentValues = List.of(
                new Pair<>(0.13f, NITROGEN), // 13% for nitrogen
                new Pair<>(0.29f, TELEVISION),
                new Pair<>(0.49f, OXYGEN),
                new Pair<>(1f, TELEMETRY));
        Consumer<Patient> twoEquipmentsConsumer = patient -> {
            while (patient.getRequiredEquipments().size() < 2) {
                oneEquipmentConsumer.accept(patient, twoEquipmentValues);
            }
        };
        applyRandomValue((int) (size * 0.41), patients, p -> p.getRequiredEquipments().isEmpty(), twoEquipmentsConsumer);

        // Preferred equipments - 29% one equipment; 53% two equipments; 16% three equipments; 2% four equipments
        // one preferred equipment
        List<Pair<Float, String>> onePreferredEquipmentValues = List.of(
                new Pair<>(0.34f, NITROGEN), // 34% for nitrogen
                new Pair<>(0.63f, TELEVISION),
                new Pair<>(1f, OXYGEN));
        BiConsumer<Patient, List<Pair<Float, String>>> onePreferredEquipmentConsumer = (patient, values) -> {
            double count = random.nextDouble();
            IntStream.range(0, values.size())
                    .filter(i -> count <= values.get(i).key())
                    .mapToObj(i -> values.get(i).value())
                    .findFirst()
                    .ifPresent(patient::addPreferredEquipment);
        };
        applyRandomValue((int) (size * 0.29), patients, onePreferredEquipmentValues, p -> p.getPreferredEquipments().isEmpty(),
                onePreferredEquipmentConsumer);
        // two preferred equipments
        List<Pair<Float, String>> twoPreferredEquipmentValues = List.of(
                new Pair<>(0.32f, NITROGEN), // 32% for nitrogen
                new Pair<>(0.62f, TELEVISION),
                new Pair<>(0.90f, OXYGEN),
                new Pair<>(1f, TELEMETRY));
        Consumer<Patient> twoPreferredEquipmentsConsumer = patient -> {
            while (patient.getPreferredEquipments().size() < 2) {
                onePreferredEquipmentConsumer.accept(patient, twoPreferredEquipmentValues);
            }
        };
        applyRandomValue((int) (size * 0.53), patients, p -> p.getPreferredEquipments().isEmpty(),
                twoPreferredEquipmentsConsumer);
        // three preferred equipments
        List<Pair<Float, String>> threePreferredEquipmentValues = List.of(
                new Pair<>(0.26f, NITROGEN), // 26% for nitrogen
                new Pair<>(0.50f, TELEVISION),
                new Pair<>(0.77f, OXYGEN),
                new Pair<>(1f, TELEMETRY));
        Consumer<Patient> threePreferredEquipmentsConsumer = patient -> {
            while (patient.getPreferredEquipments().size() < 3) {
                onePreferredEquipmentConsumer.accept(patient, threePreferredEquipmentValues);
            }
        };
        applyRandomValue((int) (size * 0.16), patients, p -> p.getPreferredEquipments().isEmpty(),
                threePreferredEquipmentsConsumer);
        // four preferred equipments
        Consumer<Patient> fourPreferredEquipmentsConsumer = patient -> {
            patient.addPreferredEquipment(NITROGEN);
            patient.addPreferredEquipment(TELEVISION);
            patient.addPreferredEquipment(OXYGEN);
            patient.addPreferredEquipment(TELEMETRY);
        };
        applyRandomValue((int) (size * 0.02), patients, p -> p.getPreferredEquipments().isEmpty(),
                fourPreferredEquipmentsConsumer);

        patients.stream()
                .filter(p -> p.getPreferredEquipments().isEmpty())
                .toList()
                .forEach(p -> onePreferredEquipmentConsumer.accept(p, onePreferredEquipmentValues));

        return patients;
    }

    private List<Stay> generateStays(List<Patient> patients, List<String> specialisms) {
        List<Stay> stays = IntStream.range(0, patients.size())
                .mapToObj(i -> new Stay("stay-%s".formatted(patients.get(i).getId()), patients.get(i)))
                .toList();

        // Specialism - 27% Specialism1; 36% Specialism2; 37% Specialism3
        applyRandomValue((int) (0.27 * patients.size()), stays, s -> s.getSpecialism() == null,
                s -> s.setSpecialism(specialisms.get(0)));
        applyRandomValue((int) (0.36 * patients.size()), stays, s -> s.getSpecialism() == null,
                s -> s.setSpecialism(specialisms.get(1)));
        applyRandomValue((int) (0.37 * patients.size()), stays, s -> s.getSpecialism() == null,
                s -> s.setSpecialism(specialisms.get(2)));
        stays.stream()
                .filter(s -> s.getSpecialism() == null)
                .toList()
                .forEach(s -> s.setSpecialism(specialisms.get(0)));

        // Start date - 18% Mon/Fri and 5% Sat/Sun
        // Stay period
        List<Pair<Float, Integer>> periodCount = List.of(
                new Pair<>(0.16f, 0), // 16% for 0 days
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
                .forEach(s -> dateConsumer.accept(s, 0));
        return stays;
    }

    private List<BedDesignation> generateBedDesignations(List<Stay> stays) {
        return IntStream.range(0, stays.size())
                .mapToObj(i -> new BedDesignation("designation%d".formatted(i), stays.get(i)))
                .toList();
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
