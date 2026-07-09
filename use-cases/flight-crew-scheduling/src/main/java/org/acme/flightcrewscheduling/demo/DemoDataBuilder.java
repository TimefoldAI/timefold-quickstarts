package org.acme.flightcrewscheduling.demo;

import static java.util.Collections.unmodifiableList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.acme.flightcrewscheduling.domain.Airport;
import org.acme.flightcrewscheduling.domain.Employee;
import org.acme.flightcrewscheduling.domain.Flight;
import org.acme.flightcrewscheduling.domain.FlightAssignment;
import org.acme.flightcrewscheduling.dto.AirportDTO;
import org.acme.flightcrewscheduling.dto.EmployeeDTO;
import org.acme.flightcrewscheduling.dto.FlightAssignmentDTO;
import org.acme.flightcrewscheduling.dto.FlightCrewScheduleInput;
import org.acme.flightcrewscheduling.dto.FlightDTO;

public final class DemoDataBuilder {

    private static final String UNASSIGNED = "";
    private static final String ATTENDANT_SKILL = "Flight attendant";
    private static final String PILOT_SKILL = "Pilot";
    private static final String CNF = "CNF";
    private static final int MIN_DAY_COUNT = 1;
    private static final int MIN_FLIGHT_COUNT = 2;
    private static final int EVEN_DIVISOR = 2;
    private static final String[] FIRST_NAMES = { "Amy", "Beth", "Carl", "Dan", "Elsa", "Flo", "Gus", "Hugo", "Ivy", "Jay",
            "Jeri", "Hope", "Avis", "Lino", "Lyle", "Nick", "Dino", "Otha", "Gwen", "Jose", "Dena", "Jana", "Dave",
            "Russ", "Josh", "Dana", "Katy" };
    private static final String[] LAST_NAMES = { "Cole", "Fox", "Green", "Jones", "King", "Li", "Poe", "Rye", "Smith",
            "Watt", "Howe", "Lowe", "Wise", "Clay", "Carr", "Hood", "Long", "Horn", "Haas", "Meza" };

    private int flightCount = 14;
    private int dayCount = 5;

    private DemoDataBuilder() {
    }

    public static DemoDataBuilder builder() {
        return new DemoDataBuilder();
    }

    public DemoDataBuilder setFlightCount(int flightCount) {
        this.flightCount = flightCount;
        return this;
    }

    public DemoDataBuilder setDayCount(int dayCount) {
        this.dayCount = dayCount;
        return this;
    }

    public FlightCrewScheduleInput build() {
        if (dayCount < MIN_DAY_COUNT) {
            throw new IllegalStateException("Number of days (" + dayCount + ") must be greater than zero.");
        }
        if (flightCount < MIN_FLIGHT_COUNT || flightCount % EVEN_DIVISOR != 0) {
            throw new IllegalStateException("Number of flights (" + flightCount + ") must be an even number above one.");
        }

        Random random = new Random(0);
        List<Airport> airports = List.of(
                new Airport("LHR", "LHR"),
                new Airport("JFK", "JFK"),
                new Airport(CNF, CNF),
                new Airport("BRU", "BRU"),
                new Airport("ATL", "ATL"),
                new Airport("BNE", "BNE"));
        Map<String, Integer> distances = buildDistances();

        LocalDate firstDate = LocalDate.now(ZoneOffset.UTC);
        List<LocalDate> dates = new ArrayList<>(dayCount);
        for (int i = 0; i < dayCount; i++) {
            dates.add(firstDate.plusDays(i));
        }

        List<Airport> homeAirports = new ArrayList<>(2);
        homeAirports.add(pickRandomAirport(airports, "", random));
        homeAirports.add(pickRandomAirport(airports, homeAirports.get(0).getCode(), random));

        List<LocalTime> times = IntStream.range(0, 23)
                .mapToObj(i -> LocalTime.of(i, 0))
                .toList();
        List<Flight> flights = generateFlights(flightCount, LocalDateTime.now(ZoneOffset.UTC).plusMinutes(1), airports,
                homeAirports, dates, times, distances, random);
        List<FlightAssignment> flightAssignments = generateFlightAssignments(flights);
        List<Employee> employees = generateEmployees(flights, dates, random);

        return new FlightCrewScheduleInput(toAirportDTOs(airports), toEmployeeDTOs(employees), toFlightDTOs(flights),
                toFlightAssignmentDTOs(flightAssignments));
    }

    private static Map<String, Integer> buildDistances() {
        Map<String, Integer> distances = new HashMap<>();
        distances.put("LHR-JFK", 8);
        distances.put("LHR-CNF", 12);
        distances.put("LHR-BRU", 13);
        distances.put("LHR-ATL", 9);
        distances.put("LHR-BNE", 21);
        distances.put("JFK-LHR", 8);
        distances.put("JFK-BRU", 14);
        distances.put("JFK-CNF", 10);
        distances.put("JFK-ATL", 6);
        distances.put("JFK-BNE", 20);
        distances.put("CNF-LHR", 12);
        distances.put("CNF-JFK", 10);
        distances.put("CNF-BRU", 19);
        distances.put("CNF-ATL", 10);
        distances.put("CNF-BNE", 19);
        distances.put("BRU-LHR", 13);
        distances.put("BRU-JFK", 14);
        distances.put("BRU-CNF", 19);
        distances.put("BRU-ATL", 9);
        distances.put("BRU-BNE", 21);
        distances.put("ATL-LHR", 9);
        distances.put("ATL-JFK", 6);
        distances.put("ATL-CNF", 10);
        distances.put("ATL-BRU", 9);
        distances.put("ATL-BNE", 18);
        distances.put("BNE-LHR", 21);
        distances.put("BNE-JFK", 20);
        distances.put("BNE-CNF", 19);
        distances.put("BNE-BRU", 21);
        distances.put("BNE-ATL", 18);
        return distances;
    }

    private List<Employee> generateEmployees(List<Flight> flights, List<LocalDate> dates, Random random) {
        Supplier<String> nameSupplier = () -> {
            Function<String[], String> randomStringSelector = strings -> strings[random.nextInt(strings.length)];
            String firstName = randomStringSelector.apply(FIRST_NAMES);
            String lastName = randomStringSelector.apply(LAST_NAMES);
            return firstName + " " + lastName;
        };

        List<Airport> flightAirports = flights.stream()
                .map(Flight::getDepartureAirport)
                .distinct()
                .toList();

        List<Employee> employees = new ArrayList<>(flightAirports.size() * 5);
        AtomicInteger count = new AtomicInteger();
        flightAirports.forEach(airport -> IntStream.range(0, 2).forEach(i -> {
            addEmployee(employees, count, nameSupplier, airport, PILOT_SKILL);
            addEmployee(employees, count, nameSupplier, airport, PILOT_SKILL);
            addEmployee(employees, count, nameSupplier, airport, ATTENDANT_SKILL);
            addEmployee(employees, count, nameSupplier, airport, ATTENDANT_SKILL);
            if (CNF.equals(airport.getCode())) {
                addEmployee(employees, count, nameSupplier, airport, ATTENDANT_SKILL);
            }
        }));

        applyRandomValue((int) (0.28 * employees.size()), employees, e -> e.getUnavailableDays().isEmpty(),
                e -> e.setUnavailableDays(List.of(dates.get(random.nextInt(dates.size())))), random);
        applyRandomValue((int) (0.04 * employees.size()), employees, e -> e.getUnavailableDays().isEmpty(),
                e -> e.setUnavailableDays(pickTwoDates(dates, random)), random);

        return employees;
    }

    private static List<LocalDate> pickTwoDates(List<LocalDate> dates, Random random) {
        List<LocalDate> unavailableDates = new ArrayList<>(2);
        while (unavailableDates.size() < 2) {
            LocalDate nextDate = dates.get(random.nextInt(dates.size()));
            if (!unavailableDates.contains(nextDate)) {
                unavailableDates.add(nextDate);
            }
        }
        return unmodifiableList(unavailableDates);
    }

    private static void addEmployee(List<Employee> employees, AtomicInteger count, Supplier<String> nameSupplier,
            Airport airport, String skill) {
        employees.add(new Employee(String.valueOf(count.incrementAndGet()), nameSupplier.get(), airport, List.of(skill)));
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private List<Flight> generateFlights(int size, LocalDateTime startDatetime, List<Airport> airports,
            List<Airport> homeAirports, List<LocalDate> dates, List<LocalTime> timeGroups,
            Map<String, Integer> distances, Random random) {
        List<Flight> flights = new ArrayList<>(size);
        List<Airport> nonHomeAirports = airports.stream()
                .filter(airport -> !homeAirports.contains(airport))
                .toList();
        int countFlights = 0;
        while (countFlights < size) {
            int routeSize = pickRandomRouteSize(countFlights, size, random);
            Airport homeAirport = homeAirports.get(random.nextInt(homeAirports.size()));
            Flight homeFlight = new Flight(String.valueOf(countFlights), homeAirport,
                    nonHomeAirports.get(random.nextInt(nonHomeAirports.size())));
            countFlights++;
            flights.add(homeFlight);
            Flight nextFlight = homeFlight;
            for (int i = 0; i < routeSize - 2; i++) {
                nextFlight = new Flight(String.valueOf(countFlights), nextFlight.getArrivalAirport(),
                        pickRandomAirport(nonHomeAirports, nextFlight.getArrivalAirport().getCode(), random));
                countFlights++;
                flights.add(nextFlight);
            }
            flights.add(new Flight(String.valueOf(countFlights), /* departureAirport= */ nextFlight.getArrivalAirport(),
                    /* arrivalAirport= */ homeFlight.getDepartureAirport()));
            countFlights++;
        }

        IntStream.range(0, flights.size()).forEach(i -> flights.get(i)
                .setFlightNumber("Flight %d".formatted(i + 1)));

        int countDates = size / dates.size();
        BiConsumer<Flight, LocalDate> flightConsumer = (flight, date) -> assignFlightTimes(flight, date, startDatetime,
                timeGroups, distances, random);
        dates.forEach(startDate -> applyRandomValue(countDates, flights, startDate,
                flight -> flight.getDepartureUTCDateTime() == null, flightConsumer, random));
        flights.stream()
                .filter(flight -> flight.getDepartureUTCDateTime() == null)
                .forEach(flight -> flightConsumer.accept(flight, dates.get(random.nextInt(dates.size()))));
        return unmodifiableList(flights);
    }

    private static void assignFlightTimes(Flight flight, LocalDate date, LocalDateTime startDatetime,
            List<LocalTime> timeGroups, Map<String, Integer> distances, Random random) {
        int countHours = distances
                .get("%s-%s".formatted(flight.getDepartureAirport().getCode(), flight.getArrivalAirport().getCode()));
        LocalTime startTime = timeGroups.get(random.nextInt(timeGroups.size()));
        LocalDateTime departureDateTime = LocalDateTime.of(date, startTime);
        if (departureDateTime.isBefore(startDatetime)) {
            departureDateTime = startDatetime.plusHours(random.nextInt(4));
        }
        flight.setDepartureUTCDateTime(departureDateTime);
        flight.setArrivalUTCDateTime(departureDateTime.plusHours(countHours));
    }

    private Airport pickRandomAirport(List<Airport> airports, String excludeCode, Random random) {
        Airport airport = null;
        while (airport == null || airport.getCode().equals(excludeCode)) {
            airport = airports.stream()
                    .skip(random.nextInt(airports.size()))
                    .findFirst()
                    .orElseThrow();
        }
        return airport;
    }

    private int pickRandomRouteSize(int countFlights, int maxCountFlights, Random random) {
        List<Integer> allowedSizes = List.of(2, 4, 6);
        int limit = maxCountFlights - countFlights;
        int routeSize = 0;
        while (routeSize == 0 || routeSize > limit) {
            routeSize = allowedSizes.stream()
                    .skip(random.nextInt(3))
                    .findFirst()
                    .orElseThrow();
        }
        return routeSize;
    }

    private List<FlightAssignment> generateFlightAssignments(List<Flight> flights) {
        List<FlightAssignment> flightAssignments = new ArrayList<>(flights.size() * 5);
        AtomicInteger count = new AtomicInteger();
        flights.forEach(flight -> {
            flightAssignments.add(new FlightAssignment(String.valueOf(count.incrementAndGet()), flight, 1, PILOT_SKILL));
            flightAssignments.add(new FlightAssignment(String.valueOf(count.incrementAndGet()), flight, 2, PILOT_SKILL));
            flightAssignments
                    .add(new FlightAssignment(String.valueOf(count.incrementAndGet()), flight, 3, ATTENDANT_SKILL));
            flightAssignments
                    .add(new FlightAssignment(String.valueOf(count.incrementAndGet()), flight, 4, ATTENDANT_SKILL));
            if (CNF.equals(flight.getDepartureAirport().getCode())
                    || CNF.equals(flight.getArrivalAirport().getCode())) {
                flightAssignments
                        .add(new FlightAssignment(String.valueOf(count.incrementAndGet()), flight, 5, ATTENDANT_SKILL));
            }
        });
        return unmodifiableList(flightAssignments);
    }

    private <T> void applyRandomValue(int count, List<T> values, Predicate<T> filter, Consumer<T> consumer,
            Random random) {
        int size = (int) values.stream().filter(filter).count();
        for (int i = 0; i < count; i++) {
            int skip = size > 0 ? random.nextInt(size) : 0;
            values.stream()
                    .filter(filter)
                    .skip(skip).findFirst()
                    .ifPresent(consumer);
            size--;
            if (size < 0) {
                break;
            }
        }
    }

    private <T, L> void applyRandomValue(int count, List<T> values, L secondParam, Predicate<T> filter,
            BiConsumer<T, L> consumer, Random random) {
        int size = (int) values.stream().filter(filter).count();
        for (int i = 0; i < count; i++) {
            int skip = size > 0 ? random.nextInt(size) : 0;
            values.stream()
                    .filter(filter)
                    .skip(skip).findFirst()
                    .ifPresent(v -> consumer.accept(v, secondParam));
            size--;
            if (size < 0) {
                break;
            }
        }
    }

    private static List<AirportDTO> toAirportDTOs(List<Airport> airports) {
        return airports.stream()
                .map(airport -> new AirportDTO(airport.getCode(), airport.getName()))
                .toList();
    }

    private static List<EmployeeDTO> toEmployeeDTOs(List<Employee> employees) {
        return employees.stream()
                .map(employee -> new EmployeeDTO(employee.getId(), employee.getName(),
                        employee.getHomeAirport() == null ? UNASSIGNED : employee.getHomeAirport().getCode(),
                        List.copyOf(employee.getSkills()),
                        employee.getUnavailableDays().stream().map(LocalDate::toString).toList()))
                .toList();
    }

    private static List<FlightDTO> toFlightDTOs(List<Flight> flights) {
        return flights.stream()
                .map(flight -> new FlightDTO(flight.getFlightNumber(), flight.getDepartureAirport().getCode(),
                        flight.getDepartureUTCDateTime().toString(), flight.getArrivalAirport().getCode(),
                        flight.getArrivalUTCDateTime().toString()))
                .toList();
    }

    private static List<FlightAssignmentDTO> toFlightAssignmentDTOs(List<FlightAssignment> flightAssignments) {
        return flightAssignments.stream()
                .map(assignment -> new FlightAssignmentDTO(assignment.getId(), assignment.getFlight().getFlightNumber(),
                        assignment.getIndexInFlight(), assignment.getRequiredSkill(), UNASSIGNED))
                .toList();
    }
}
