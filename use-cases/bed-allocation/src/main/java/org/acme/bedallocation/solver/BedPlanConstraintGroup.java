package org.acme.bedallocation.solver;

import ai.timefold.solver.service.definition.api.description.ConstraintGroupInfo;

public final class BedPlanConstraintGroup {

    public static final ConstraintGroupInfo BED_CONFLICTS = new ConstraintGroupInfo("bedConflicts",
            "Bed conflicts",
            "Avoid double-booking a single bed to two patient stays on the same night.",
            "IconBed",
            new String[] { "bed conflicts" });

    public static final ConstraintGroupInfo GENDER_RULES = new ConstraintGroupInfo("genderRules",
            "Gender rules",
            "Keep gender-restricted rooms free of the wrong gender, and same-gender rooms free of mixed genders "
                    + "on the same night.",
            "IconUsers",
            new String[] { "gender rules" });

    public static final ConstraintGroupInfo ELIGIBILITY_RULES = new ConstraintGroupInfo("eligibilityRules",
            "Eligibility rules",
            "Respect the department's age range and the patient's required equipment.",
            "IconShieldCheck",
            new String[] { "eligibility" });

    public static final ConstraintGroupInfo BED_ASSIGNMENT = new ConstraintGroupInfo("bedAssignment",
            "Bed assignment",
            "Assign every patient stay to a bed.",
            "IconCheckCircle",
            new String[] { "bed assignment" });

    public static final ConstraintGroupInfo PATIENT_PREFERENCES = new ConstraintGroupInfo("patientPreferences",
            "Patient preferences",
            "Honor the patient's preferred room capacity, department specialty, and equipment.",
            "IconHeart",
            new String[] { "patient preferences" });

    private BedPlanConstraintGroup() {
    }
}
