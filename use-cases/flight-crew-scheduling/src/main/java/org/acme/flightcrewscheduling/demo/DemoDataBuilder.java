package org.acme.flightcrewscheduling.demo;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.acme.flightcrewscheduling.dto.input.AirportInputDTO;
import org.acme.flightcrewscheduling.dto.input.EmployeeInputDTO;
import org.acme.flightcrewscheduling.dto.input.FlightAssignmentInputDTO;
import org.acme.flightcrewscheduling.dto.input.FlightCrewScheduleInput;
import org.acme.flightcrewscheduling.dto.input.FlightInputDTO;

/**
 * Builds a fully hand-picked demo dataset (no randomness) that is deliberately hardcoded to be
 * feasible: the flights form seven out-and-back rotations, and every rotation has enough crew based
 * at its home airport to be flown end to end by crew members who are available on all of its days.
 */
public final class DemoDataBuilder {

    static final String PILOT_SKILL = "Pilot";
    static final String ATTENDANT_SKILL = "Flight attendant";

    private static final String LHR = "LHR";
    private static final String JFK = "JFK";
    private static final String CNF = "CNF";
    private static final String BRU = "BRU";
    private static final String ATL = "ATL";
    private static final String BNE = "BNE";

    private static final List<AirportInputDTO> AIRPORTS = List.of(
            new AirportInputDTO(LHR, "London Heathrow", 51.4775, -0.461389),
            new AirportInputDTO(JFK, "New York JFK", 40.639722, -73.778889),
            new AirportInputDTO(CNF, "Belo Horizonte Confins", -19.624444, -43.971944),
            new AirportInputDTO(BRU, "Brussels", 50.901389, 4.484444),
            new AirportInputDTO(ATL, "Atlanta", 33.636667, -84.428056),
            new AirportInputDTO(BNE, "Brisbane", -27.383333, 153.118333));

    private static final String[] FIRST_NAMES = { "Amy", "Beth", "Carl", "Dan", "Elsa", "Flo", "Gus", "Hugo", "Ivy",
            "Jay", "Jeri", "Hope", "Avis", "Lino", "Lyle", "Nick", "Dino", "Otha", "Gwen", "Jose", "Dena", "Jana" };
    private static final String[] LAST_NAMES = { "Cole", "Fox", "Green", "Jones", "King", "Li", "Poe", "Rye", "Smith",
            "Watt", "Howe", "Lowe" };
    private static final String[] AIRLINE_CODES =
            { "XQ", "ZJ", "QY", "JX", "WV", "YF", "KG", "VK", "JH", "XD", "YJ", "XN", "ZF" };
    // How many crew members of each skill are based at each hub. Each number leaves slack on top of
    private static final int LHR_PILOTS = 13;
    private static final int LHR_ATTENDANTS = 13;
    private static final int BRU_PILOTS = 11;
    private static final int BRU_ATTENDANTS = 11;

    /**
     * Days (as an offset from the first Monday) on which a crew member cannot fly, keyed by crew member ID.
     * Kept within the slack of each hub/skill group, so a fully available crew is always still available.
     */
    private static final Map<String, List<Integer>> UNAVAILABLE_DAY_OFFSETS = Map.ofEntries(
            Map.entry("crew-3", List.of(0)),
            Map.entry("crew-7", List.of(2, 3)),
            Map.entry("crew-10", List.of(4)),
            Map.entry("crew-12", List.of(1)),
            Map.entry("crew-15", List.of(1)),
            Map.entry("crew-19", List.of(3, 4)),
            Map.entry("crew-24", List.of(0)),
            Map.entry("crew-26", List.of(2)),
            Map.entry("crew-30", List.of(0, 1)),
            Map.entry("crew-33", List.of(5)),
            Map.entry("crew-34", List.of(3)),
            Map.entry("crew-36", List.of(2)),
            Map.entry("crew-39", List.of(4, 5)),
            Map.entry("crew-41", List.of(0)),
            Map.entry("crew-44", List.of(1)));

    private static int flightIndex = 0;

    private DemoDataBuilder() {
    }

    public static DemoDataBuilder builder() {
        return new DemoDataBuilder();
    }

    public FlightCrewScheduleInput build() {
        // Anchored to the next Monday (never today), so the schedule always lies in the future.
        LocalDate firstMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        flightIndex = 0;

        List<FlightInputDTO> flights = List.of(
                // LHR rotations
                flight(LHR, at(firstMonday, 0, 6), JFK, at(firstMonday, 0, 14)),
                flight(JFK, at(firstMonday, 1, 8), LHR, at(firstMonday, 1, 16)),
                flight(LHR, at(firstMonday, 1, 6), ATL, at(firstMonday, 1, 15)),
                flight(ATL, at(firstMonday, 2, 8), LHR, at(firstMonday, 2, 17)),
                flight(LHR, at(firstMonday, 2, 5), CNF, at(firstMonday, 2, 17)),
                flight(CNF, at(firstMonday, 3, 6), LHR, at(firstMonday, 3, 18)),
                flight(LHR, at(firstMonday, 3, 5), BNE, at(firstMonday, 4, 2)),
                flight(BNE, at(firstMonday, 4, 8), LHR, at(firstMonday, 5, 5)),
                // BRU rotations
                flight(BRU, at(firstMonday, 0, 7), JFK, at(firstMonday, 0, 21)),
                flight(JFK, at(firstMonday, 1, 9), BRU, at(firstMonday, 1, 23)),
                flight(BRU, at(firstMonday, 1, 7), ATL, at(firstMonday, 1, 16)),
                flight(ATL, at(firstMonday, 2, 9), BRU, at(firstMonday, 2, 18)),
                flight(BRU, at(firstMonday, 2, 4), BNE, at(firstMonday, 3, 1)),
                flight(BNE, at(firstMonday, 3, 8), BRU, at(firstMonday, 4, 5)),
                // Additional LHR rotations
                flight(LHR, at(firstMonday, 0, 8), ATL, at(firstMonday, 0, 17)),
                flight(ATL, at(firstMonday, 1, 9), LHR, at(firstMonday, 1, 18)),
                flight(LHR, at(firstMonday, 1, 10), JFK, at(firstMonday, 1, 18)),
                flight(JFK, at(firstMonday, 2, 10), LHR, at(firstMonday, 2, 18)),
                flight(LHR, at(firstMonday, 3, 7), CNF, at(firstMonday, 3, 19)),
                flight(CNF, at(firstMonday, 4, 7), LHR, at(firstMonday, 4, 19)),
                flight(LHR, at(firstMonday, 4, 6), ATL, at(firstMonday, 4, 15)),
                flight(ATL, at(firstMonday, 5, 8), LHR, at(firstMonday, 5, 17)),
                // Additional BRU rotations
                flight(BRU, at(firstMonday, 0, 8), ATL, at(firstMonday, 0, 17)),
                flight(JFK, at(firstMonday, 3, 9), BRU, at(firstMonday, 3, 23)),
                flight(BRU, at(firstMonday, 3, 6), BNE, at(firstMonday, 4, 3)),
                flight(BNE, at(firstMonday, 4, 9), BRU, at(firstMonday, 5, 6)),
                flight(BRU, at(firstMonday, 5, 7), ATL, at(firstMonday, 5, 16)),
                flight(ATL, at(firstMonday, 6, 9), BRU, at(firstMonday, 6, 18)),
                // LHR-BRU cross-routing
                flight(LHR, at(firstMonday, 0, 10), BRU, at(firstMonday, 0, 12)),
                flight(BRU, at(firstMonday, 0, 14), LHR, at(firstMonday, 0, 16)),
                flight(LHR, at(firstMonday, 2, 11), BRU, at(firstMonday, 2, 13)),
                flight(BRU, at(firstMonday, 2, 15), LHR, at(firstMonday, 2, 17)),
                flight(LHR, at(firstMonday, 4, 10), BRU, at(firstMonday, 4, 12)),
                flight(BRU, at(firstMonday, 4, 14), LHR, at(firstMonday, 4, 16)),
                // Extended week rotations
                flight(LHR, at(firstMonday, 0, 9), BNE, at(firstMonday, 1, 6)),
                flight(BNE, at(firstMonday, 1, 9), LHR, at(firstMonday, 2, 6)),
                flight(JFK, at(firstMonday, 4, 10), BRU, at(firstMonday, 5, 0)),
                // Short-haul additions
                flight(LHR, at(firstMonday, 0, 7), BRU, at(firstMonday, 0, 9)),
                flight(BRU, at(firstMonday, 0, 11), LHR, at(firstMonday, 0, 13)),
                flight(ATL, at(firstMonday, 0, 7), BRU, at(firstMonday, 0, 16)),
                flight(LHR, at(firstMonday, 3, 8), JFK, at(firstMonday, 3, 16)),
                flight(JFK, at(firstMonday, 4, 9), LHR, at(firstMonday, 4, 17)),
                flight(BRU, at(firstMonday, 5, 6), CNF, at(firstMonday, 5, 18)),
                flight(CNF, at(firstMonday, 6, 6), BRU, at(firstMonday, 6, 18)),
                flight(LHR, at(firstMonday, 5, 7), ATL, at(firstMonday, 5, 16)),
                flight(ATL, at(firstMonday, 6, 8), LHR, at(firstMonday, 6, 17)));

        return new FlightCrewScheduleInput(AIRPORTS, buildEmployees(firstMonday), flights,
                buildFlightAssignments(flights));
    }

    private static List<EmployeeInputDTO> buildEmployees(LocalDate firstMonday) {
        return IntStream.rangeClosed(1, LHR_PILOTS + LHR_ATTENDANTS + BRU_PILOTS + BRU_ATTENDANTS)
                .mapToObj(number -> employee(number, firstMonday))
                .toList();
    }

    private static EmployeeInputDTO employee(int number, LocalDate firstMonday) {
        String homeAirportCode;
        String skill;
        if (number <= LHR_PILOTS) {
            homeAirportCode = LHR;
            skill = PILOT_SKILL;
        } else if (number <= LHR_PILOTS + LHR_ATTENDANTS) {
            homeAirportCode = LHR;
            skill = ATTENDANT_SKILL;
        } else if (number <= LHR_PILOTS + LHR_ATTENDANTS + BRU_PILOTS) {
            homeAirportCode = BRU;
            skill = PILOT_SKILL;
        } else {
            homeAirportCode = BRU;
            skill = ATTENDANT_SKILL;
        }
        String id = "crew-" + number;
        List<LocalDate> unavailableDays = UNAVAILABLE_DAY_OFFSETS.getOrDefault(id, List.of()).stream()
                .map(firstMonday::plusDays)
                .toList();
        return new EmployeeInputDTO(id, name(number), homeAirportCode, List.of(skill), unavailableDays);
    }

    private static String name(int number) {
        int index = number - 1;
        return "%s %s".formatted(FIRST_NAMES[index % FIRST_NAMES.length],
                LAST_NAMES[(index / FIRST_NAMES.length) % LAST_NAMES.length]);
    }

    /**
     * Every flight carries two pilots and two flight attendants, plus a third attendant on the
     * long-haul CNF legs.
     */
    private static List<FlightAssignmentInputDTO> buildFlightAssignments(List<FlightInputDTO> flights) {
        return flights.stream()
                .flatMap(flight -> {
                    boolean extraAttendant = CNF.equals(flight.departureAirportCode())
                            || CNF.equals(flight.arrivalAirportCode());
                    List<String> requiredSkills = extraAttendant
                            ? List.of(PILOT_SKILL, PILOT_SKILL, ATTENDANT_SKILL, ATTENDANT_SKILL, ATTENDANT_SKILL)
                            : List.of(PILOT_SKILL, PILOT_SKILL, ATTENDANT_SKILL, ATTENDANT_SKILL);
                    return IntStream.range(0, requiredSkills.size())
                            .mapToObj(index -> new FlightAssignmentInputDTO(
                                    "%s-seat-%d".formatted(flight.flightNumber(), index + 1),
                                    flight.flightNumber(), index + 1, requiredSkills.get(index), null));
                })
                .toList();
    }

    private static FlightInputDTO flight(String departureAirportCode,
            OffsetDateTime departureUTCDateTime, String arrivalAirportCode, OffsetDateTime arrivalUTCDateTime) {
        String airline = AIRLINE_CODES[flightIndex % AIRLINE_CODES.length];
        int number = (flightIndex * 17 + 100) % 9000 + 100;
        String flightNumber = airline + number;
        flightIndex++;
        return new FlightInputDTO(flightNumber, departureAirportCode, departureUTCDateTime, arrivalAirportCode,
                arrivalUTCDateTime);
    }

    private static OffsetDateTime at(LocalDate firstMonday, int dayOffset, int hour) {
        return OffsetDateTime.of(firstMonday.plusDays(dayOffset), LocalTime.of(hour, 0), ZoneOffset.UTC);
    }
}
