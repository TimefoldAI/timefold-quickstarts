package org.acme.bedallocation.solver;

import ai.timefold.solver.service.definition.api.description.ConstraintGroupInfo;

public final class BedScheduleConstraintGroup {
    public static final ConstraintGroupInfo BED_OCCUPANCY = new ConstraintGroupInfo("bedOccupancy",
            "Bed occupancy",
            "Ensure each bed holds at most one patient per night and that every patient gets a bed.",
            "IconDiamond",
            new String[] { ConstraintGroupTag.CONFLICT_FREE_PLANNING.getTag() });
    public static final ConstraintGroupInfo PATIENT_SAFETY = new ConstraintGroupInfo("patientSafety",
            "Patient safety",
            "Respect gender, age and required equipment restrictions of each room.",
            "IconCalendar",
            new String[] { ConstraintGroupTag.PATIENT_SAFETY.getTag() });
    public static final ConstraintGroupInfo PATIENT_COMFORT = new ConstraintGroupInfo("patientComfort",
            "Patient comfort",
            "Honour patient preferences for room capacity, specialty and equipment.",
            "IconUser",
            new String[] { ConstraintGroupTag.PATIENT_COMFORT.getTag() });

    private BedScheduleConstraintGroup() {
    }
}
