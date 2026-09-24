package org.acme.flightcrewscheduling.support;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.IntStream;

import org.acme.flightcrewscheduling.domain.Airport;
import org.acme.flightcrewscheduling.domain.Employee;
import org.acme.flightcrewscheduling.domain.Flight;
import org.acme.flightcrewscheduling.domain.FlightAssignment;
import org.acme.flightcrewscheduling.dto.input.AirportInputDTO;
import org.acme.flightcrewscheduling.dto.input.EmployeeInputDTO;
import org.acme.flightcrewscheduling.dto.input.FlightAssignmentInputDTO;
import org.acme.flightcrewscheduling.dto.input.FlightCrewScheduleInput;
import org.acme.flightcrewscheduling.dto.input.FlightInputDTO;

// To keep our production classes as simple as possible, we've added these methods to help construct the data needed for testing.
public final class TestHelper {

    public static final String PILOT_SKILL = "Pilot";
    public static final String ATTENDANT_SKILL = "Flight attendant";

    /** The Monday every date in a test problem is relative to; fixed so tests never depend on today. */
    public static final LocalDate FIRST_DAY = LocalDate.of(2024, 1, 1);

    private TestHelper() {
    }

    // ************************************************************************
    // DTO factories
    // ************************************************************************

    public static FlightCrewScheduleInput input(List<AirportInputDTO> airports, List<EmployeeInputDTO> employees,
            List<FlightInputDTO> flights, List<FlightAssignmentInputDTO> flightAssignments) {
        return new FlightCrewScheduleInput(airports, employees, flights, flightAssignments);
    }

    public static AirportInputDTO airportDTO(String code) {
        return new AirportInputDTO(code, "Airport " + code, 0.0, 0.0);
    }

    public static EmployeeDTOBuilder anEmployeeDTO(String id) {
        return new EmployeeDTOBuilder(id);
    }

    public static FlightDTOBuilder aFlightDTO(String flightNumber) {
        return new FlightDTOBuilder(flightNumber);
    }

    public static FlightAssignmentDTOBuilder aFlightAssignmentDTO(String id, String flightNumber) {
        return new FlightAssignmentDTOBuilder(id, flightNumber);
    }

    /**
     * A small but complete problem: one out-and-back rotation between two airports, crewed by two
     * pilots and two flight attendants who are all based at the departure airport. Feasible by
     * construction, so a solve over it must reach a feasible score.
     */
    public static FlightCrewScheduleInput createProblem() {
        List<AirportInputDTO> airports = List.of(airportDTO("LHR"), airportDTO("JFK"));
        List<EmployeeInputDTO> employees = List.of(
                anEmployeeDTO("crew-1").skills(List.of(PILOT_SKILL)).build(),
                anEmployeeDTO("crew-2").skills(List.of(PILOT_SKILL)).build(),
                anEmployeeDTO("crew-3").skills(List.of(ATTENDANT_SKILL)).build(),
                anEmployeeDTO("crew-4").skills(List.of(ATTENDANT_SKILL)).build());
        List<FlightInputDTO> flights = List.of(
                aFlightDTO("TF1").departure("LHR", offsetDateTime(0, 6)).arrival("JFK", offsetDateTime(0, 14)).build(),
                aFlightDTO("TF2").departure("JFK", offsetDateTime(1, 8)).arrival("LHR", offsetDateTime(1, 16)).build());
        List<String> requiredSkills = List.of(PILOT_SKILL, PILOT_SKILL, ATTENDANT_SKILL, ATTENDANT_SKILL);
        List<FlightAssignmentInputDTO> flightAssignments = flights.stream()
                .flatMap(flight -> IntStream.range(0, requiredSkills.size())
                        .mapToObj(index -> aFlightAssignmentDTO("%s-seat-%d".formatted(flight.flightNumber(), index + 1),
                                flight.flightNumber())
                                .indexInFlight(index + 1)
                                .requiredSkill(requiredSkills.get(index))
                                .build()))
                .toList();
        return input(airports, employees, flights, flightAssignments);
    }

    public static OffsetDateTime offsetDateTime(int dayOffset, int hour) {
        return OffsetDateTime.of(FIRST_DAY.plusDays(dayOffset), LocalTime.of(hour, 0), ZoneOffset.UTC);
    }

    // ************************************************************************
    // Solver model factories
    // ************************************************************************

    public static Airport anAirport(String code) {
        return new Airport(code, "Airport " + code, 0.0, 0.0);
    }

    public static EmployeeBuilder anEmployee(String id) {
        return new EmployeeBuilder(id);
    }

    public static FlightBuilder aFlight(String flightNumber) {
        return new FlightBuilder(flightNumber);
    }

    public static FlightAssignmentBuilder aFlightAssignment(String id, Flight flight) {
        return new FlightAssignmentBuilder(id, flight);
    }

    public static final class EmployeeBuilder {

        private final String id;
        private String name;
        private Airport homeAirport = anAirport("LHR");
        private List<String> skills = List.of(PILOT_SKILL);
        private List<LocalDate> unavailableDays = List.of();

        private EmployeeBuilder(String id) {
            this.id = id;
            this.name = "Crew " + id;
        }

        public EmployeeBuilder name(String name) {
            this.name = name;
            return this;
        }

        public EmployeeBuilder homeAirport(Airport homeAirport) {
            this.homeAirport = homeAirport;
            return this;
        }

        public EmployeeBuilder skills(List<String> skills) {
            this.skills = skills;
            return this;
        }

        public EmployeeBuilder unavailableDays(List<LocalDate> unavailableDays) {
            this.unavailableDays = unavailableDays;
            return this;
        }

        public Employee build() {
            return new Employee(id, name, homeAirport, skills, unavailableDays);
        }
    }

    public static final class FlightBuilder {

        private final String flightNumber;
        private Airport departureAirport = anAirport("LHR");
        private OffsetDateTime departureUTCDateTime = offsetDateTime(0, 6);
        private Airport arrivalAirport = anAirport("JFK");
        private OffsetDateTime arrivalUTCDateTime = offsetDateTime(0, 14);

        private FlightBuilder(String flightNumber) {
            this.flightNumber = flightNumber;
        }

        public FlightBuilder departure(Airport departureAirport, OffsetDateTime departureUTCDateTime) {
            this.departureAirport = departureAirport;
            this.departureUTCDateTime = departureUTCDateTime;
            return this;
        }

        public FlightBuilder arrival(Airport arrivalAirport, OffsetDateTime arrivalUTCDateTime) {
            this.arrivalAirport = arrivalAirport;
            this.arrivalUTCDateTime = arrivalUTCDateTime;
            return this;
        }

        public Flight build() {
            return new Flight(flightNumber, departureAirport, departureUTCDateTime, arrivalAirport,
                    arrivalUTCDateTime);
        }
    }

    public static final class FlightAssignmentBuilder {

        private final String id;
        private final Flight flight;
        private int indexInFlight = 1;
        private String requiredSkill = PILOT_SKILL;
        private Employee employee;

        private FlightAssignmentBuilder(String id, Flight flight) {
            this.id = id;
            this.flight = flight;
        }

        public FlightAssignmentBuilder indexInFlight(int indexInFlight) {
            this.indexInFlight = indexInFlight;
            return this;
        }

        public FlightAssignmentBuilder requiredSkill(String requiredSkill) {
            this.requiredSkill = requiredSkill;
            return this;
        }

        public FlightAssignmentBuilder employee(Employee employee) {
            this.employee = employee;
            return this;
        }

        public FlightAssignment build() {
            return new FlightAssignment(id, flight, indexInFlight, requiredSkill, employee);
        }
    }

    // ************************************************************************
    // DTO builders
    // ************************************************************************

    public static final class EmployeeDTOBuilder {

        private final String id;
        private String name;
        private String homeAirportCode = "LHR";
        private List<String> skills = List.of(PILOT_SKILL);
        private List<LocalDate> unavailableDays = List.of();

        private EmployeeDTOBuilder(String id) {
            this.id = id;
            this.name = "Crew " + id;
        }

        public EmployeeDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public EmployeeDTOBuilder homeAirportCode(String homeAirportCode) {
            this.homeAirportCode = homeAirportCode;
            return this;
        }

        public EmployeeDTOBuilder skills(List<String> skills) {
            this.skills = skills;
            return this;
        }

        public EmployeeDTOBuilder unavailableDays(List<LocalDate> unavailableDays) {
            this.unavailableDays = unavailableDays;
            return this;
        }

        public EmployeeInputDTO build() {
            return new EmployeeInputDTO(id, name, homeAirportCode, skills, unavailableDays);
        }
    }

    public static final class FlightDTOBuilder {

        private final String flightNumber;
        private String departureAirportCode = "LHR";
        private OffsetDateTime departureUTCDateTime = offsetDateTime(0, 6);
        private String arrivalAirportCode = "JFK";
        private OffsetDateTime arrivalUTCDateTime = offsetDateTime(0, 14);

        private FlightDTOBuilder(String flightNumber) {
            this.flightNumber = flightNumber;
        }

        public FlightDTOBuilder departure(String departureAirportCode, OffsetDateTime departureUTCDateTime) {
            this.departureAirportCode = departureAirportCode;
            this.departureUTCDateTime = departureUTCDateTime;
            return this;
        }

        public FlightDTOBuilder arrival(String arrivalAirportCode, OffsetDateTime arrivalUTCDateTime) {
            this.arrivalAirportCode = arrivalAirportCode;
            this.arrivalUTCDateTime = arrivalUTCDateTime;
            return this;
        }

        public FlightInputDTO build() {
            return new FlightInputDTO(flightNumber, departureAirportCode, departureUTCDateTime, arrivalAirportCode,
                    arrivalUTCDateTime);
        }
    }

    public static final class FlightAssignmentDTOBuilder {

        private final String id;
        private final String flightNumber;
        private int indexInFlight = 1;
        private String requiredSkill = PILOT_SKILL;
        private String employeeId;

        private FlightAssignmentDTOBuilder(String id, String flightNumber) {
            this.id = id;
            this.flightNumber = flightNumber;
        }

        public FlightAssignmentDTOBuilder indexInFlight(int indexInFlight) {
            this.indexInFlight = indexInFlight;
            return this;
        }

        public FlightAssignmentDTOBuilder requiredSkill(String requiredSkill) {
            this.requiredSkill = requiredSkill;
            return this;
        }

        public FlightAssignmentDTOBuilder employeeId(String employeeId) {
            this.employeeId = employeeId;
            return this;
        }

        public FlightAssignmentInputDTO build() {
            return new FlightAssignmentInputDTO(id, flightNumber, indexInFlight, requiredSkill, employeeId);
        }
    }
}
