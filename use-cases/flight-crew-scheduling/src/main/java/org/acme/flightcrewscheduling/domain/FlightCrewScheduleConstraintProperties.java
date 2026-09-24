package org.acme.flightcrewscheduling.domain;

public final class FlightCrewScheduleConstraintProperties {

    public static final String REQUIRED_SKILL = "Required skill";
    public static final String FLIGHT_CONFLICT = "Flight conflict";
    public static final String TRANSFER_BETWEEN_TWO_FLIGHTS = "Transfer between two flights";
    public static final String EMPLOYEE_UNAVAILABILITY = "Employee unavailability";

    public static final String FIRST_ASSIGNMENT_NOT_DEPARTING_FROM_HOME = "First assignment not departing from home";
    public static final String LAST_ASSIGNMENT_NOT_ARRIVING_AT_HOME = "Last assignment not arriving at home";

    private FlightCrewScheduleConstraintProperties() {
    }
}
