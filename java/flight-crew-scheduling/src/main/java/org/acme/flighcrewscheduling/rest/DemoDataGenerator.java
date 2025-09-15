package org.acme.flighcrewscheduling.rest;

import static java.util.Collections.unmodifiableList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

import jakarta.enterprise.context.ApplicationScoped;

import org.acme.flighcrewscheduling.domain.Airport;
import org.acme.flighcrewscheduling.domain.Employee;
import org.acme.flighcrewscheduling.domain.Flight;
import org.acme.flighcrewscheduling.domain.FlightAssignment;
import org.acme.flighcrewscheduling.domain.FlightCrewSchedule;

@ApplicationScoped
public class DemoDataGenerator {

    private static final String[] FIRST_NAMES = {"Amy", "Beth", "Carl", "Dan", "Elsa", "Flo", "Gus", "Hugo", "Ivy", "Jay",
            "Jeri", "Hope", "Avis", "Lino", "Lyle", "Nick", "Dino", "Otha", "Gwen", "Jose", "Dena", "Jana", "Dave",
            "Russ", "Josh", "Dana", "Katy"};
    private static final String[] LAST_NAMES =
            {"Cole", "Fox", "Green", "Jones", "King", "Li", "Poe", "Rye", "Smith", "Watt", "Howe", "Lowe", "Wise", "Clay",
                    "Carr", "Hood", "Long", "Horn", "Haas", "Meza"};
    private static final String ATTENDANT_SKILL = "Flight attendant";
    private static final String PILOT_SKILL = "Pilot";

    public FlightCrewSchedule generateDemoData() {
        Random random = new Random(0);
        FlightCrewSchedule schedule = new FlightCrewSchedule();
        // Airports
        List<Airport> airports = List.of(
                new Airport("LHR", "LHR", 51.4775, -0.461389),
                new Airport("JFK", "JFK", 40.639722, -73.778889),
                new Airport("CNF", "CNF", -19.624444, -43.971944),
                new Airport("BRU", "BRU", 50.901389, 4.484444),
                new Airport("ATL", "ATL", 33.636667, -84.428056),
                new Airport("BNE", "BNE", -27.383333, 153.118333));
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

        // Flights
        LocalDate firstDate = LocalDate.now();
        int countDays = 5;
        List<LocalDate> dates = new ArrayList<>(countDays);
        dates.add(firstDate);
        for (int i = 1; i < countDays; i++) {
            dates.add(firstDate.plusDays(i));
        }
        List<Airport> homeAirports = new ArrayList<>(2);
        homeAirports.add(pickRandomAirport(airports, "", random));
        homeAirports.add(pickRandomAirport(airports, homeAirports.get(0).getCode(), random));
        List<LocalTime> times = IntStream.range(0, 23)
                .mapToObj(i -> LocalTime.of(i, 0))
                .toList();
        int countFlights = 14;
        List<Flight> flights =
                generateFlights(countFlights, LocalDateTime.now().plusMinutes(1), airports, homeAirports, dates, times,
                        distances, random);

        // Flight assignments
        List<FlightAssignment> flightAssignments = generateFlightAssignments(flights);

        // Employees
        List<Employee> employees = generateEmployees(flights, dates, random);

        // Update problem facts
        schedule.setAirports(airports);
        schedule.setEmployees(employees);
        schedule.setFlights(flights);
        schedule.setFlightAssignments(flightAssignments);

        return schedule;
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

        // two pilots and three attendants per airport
        List<Employee> employees = new ArrayList<>(flightAirports.size() * 5);

        AtomicInteger count = new AtomicInteger();
        // Two teams per airport
        flightAirports.forEach(airport -> IntStream.range(0, 2).forEach(i -> {
            employees.add(new Employee(String.valueOf(count.incrementAndGet()), nameSupplier.get(), airport, List.of(PILOT_SKILL)));
            employees.add(new Employee(String.valueOf(count.incrementAndGet()), nameSupplier.get(), airport, List.of(PILOT_SKILL)));
            employees.add(
                    new Employee(String.valueOf(count.incrementAndGet()), nameSupplier.get(), airport, List.of(ATTENDANT_SKILL)));
            employees.add(
                    new Employee(String.valueOf(count.incrementAndGet()), nameSupplier.get(), airport, List.of(ATTENDANT_SKILL)));
            if (airport.getCode().equals("CNF")) {
                employees.add(
                        new Employee(String.valueOf(count.incrementAndGet()), nameSupplier.get(), airport, List.of(ATTENDANT_SKILL)));
            }
        }));

        // Unavailable dates - 28% one date; 4% two dates
        applyRandomValue((int) (0.28 * employees.size()), employees, e -> e.getUnavailableDays() == null,
                e -> e.setUnavailableDays(List.of(dates.get(random.nextInt(dates.size())))), random);
        applyRandomValue((int) (0.04 * employees.size()), employees, e -> e.getUnavailableDays() == null,
                e -> {
                    List<LocalDate> unavailableDates = new ArrayList<>(2);
                    while (unavailableDates.size() < 2) {
                        LocalDate nextDate = dates.get(random.nextInt(dates.size()));
                        if (!unavailableDates.contains(nextDate)) {
                            unavailableDates.add(nextDate);
                        }
                    }
                    e.setUnavailableDays(unmodifiableList(unavailableDates));
                }, random);

        return employees;
    }

    private List<Flight> generateFlights(int size, LocalDateTime startDatetime, List<Airport> airports,
                                         List<Airport> homeAirports, List<LocalDate> dates, List<LocalTime> timeGroups, Map<String, Integer> distances, Random random) {
        if (size % 2 != 0) {
            throw new IllegalArgumentException("The size of flights must be even");
        }

        // Departure and arrival airports
        List<Flight> flights = new ArrayList<>(size);
        List<Airport> remainingAirports = airports.stream()
                .filter(airport -> !homeAirports.contains(airport))
                .toList();
        int countFlights = 0;
        while (countFlights < size) {
            int routeSize = pickRandomRouteSize(countFlights, size, random);
            Airport homeAirport = homeAirports.get(random.nextInt(homeAirports.size()));
            Flight homeFlight = new Flight(String.valueOf(countFlights++), homeAirport,
                    remainingAirports.get(random.nextInt(remainingAirports.size())));
            flights.add(homeFlight);
            Flight nextFlight = homeFlight;
            for (int i = 0; i < routeSize - 2; i++) {
                nextFlight = new Flight(String.valueOf(countFlights++), nextFlight.getArrivalAirport(),
                        pickRandomAirport(remainingAirports, nextFlight.getArrivalAirport().getCode(), random));
                flights.add(nextFlight);
            }
            flights.add(new Flight(String.valueOf(countFlights++), nextFlight.getArrivalAirport(),
                    homeFlight.getDepartureAirport()));
        }

        // Flight number
        IntStream.range(0, flights.size()).forEach(i -> flights.get(i)
                .setFlightNumber("Flight %d".formatted(i + 1)));

        // Flight duration
        int countDates = size / dates.size();
        BiConsumer<Flight, LocalDate> flightConsumer = (flight, date) -> {
            int countHours = distances
                    .get("%s-%s".formatted(flight.getDepartureAirport().getCode(), flight.getArrivalAirport().getCode()));
            LocalTime startTime = timeGroups.get(random.nextInt(timeGroups.size()));
            LocalDateTime departureDateTime = LocalDateTime.of(date, startTime);
            if (departureDateTime.isBefore(startDatetime)) {
                departureDateTime = startDatetime.plusHours(random.nextInt(4));
            }
            LocalDateTime arrivalDateTime = departureDateTime.plusHours(countHours);
            flight.setDepartureUTCDateTime(departureDateTime);
            flight.setArrivalUTCDateTime(arrivalDateTime);
        };
        dates.forEach(startDate -> applyRandomValue(countDates, flights, startDate,
                flight -> flight.getDepartureUTCDateTime() == null, flightConsumer, random));
        // Ensure there are no empty dates
        flights.stream()
                .filter(flight -> flight.getDepartureUTCDateTime() == null)
                .forEach(flight -> flightConsumer.accept(flight, dates.get(random.nextInt(dates.size()))));
        return unmodifiableList(flights);
    }

    private Airport pickRandomAirport(List<Airport> airports, String excludeCode, Random random) {
        Airport airport = null;
        while (airport == null || airport.getCode().equals(excludeCode)) {
            airport = airports.stream()
                    .skip(random.nextInt(airports.size()))
                    .findFirst()
                    .get();
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
                    .get();
        }
        return routeSize;
    }

    private List<FlightAssignment> generateFlightAssignments(List<Flight> flights) {
        // 2 pilots and 2 or 3 attendants
        List<FlightAssignment> flightAssignments = new ArrayList<>(flights.size() * 5);
        AtomicInteger count = new AtomicInteger();
        flights.forEach(flight -> {
            AtomicInteger indexSkill = new AtomicInteger();
            flightAssignments
                    .add(new FlightAssignment(String.valueOf(count.incrementAndGet()), flight, indexSkill.incrementAndGet(), PILOT_SKILL));
            flightAssignments
                    .add(new FlightAssignment(String.valueOf(count.incrementAndGet()), flight, indexSkill.incrementAndGet(), PILOT_SKILL));
            flightAssignments
                    .add(new FlightAssignment(String.valueOf(count.incrementAndGet()), flight, indexSkill.incrementAndGet(),
                            ATTENDANT_SKILL));
            flightAssignments
                    .add(new FlightAssignment(String.valueOf(count.incrementAndGet()), flight, indexSkill.incrementAndGet(),
                            ATTENDANT_SKILL));
            if (flight.getDepartureAirport().getCode().equals("CNF") || flight.getArrivalAirport().getCode().equals("CNF")) {
                flightAssignments
                        .add(new FlightAssignment(String.valueOf(count.incrementAndGet()), flight, indexSkill.incrementAndGet(),
                                ATTENDANT_SKILL));
            }
        });
        return unmodifiableList(flightAssignments);
    }

    private <T> void applyRandomValue(int count, List<T> values, Predicate<T> filter, Consumer<T> consumer, Random random) {
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
}
