package org.acme.bedallocation.domain;

public final class BedPlanConstraintProperties {

    public static final String SAME_BED_IN_SAME_NIGHT = "Same bed in same night";
    public static final String FEMALE_IN_MALE_ROOM = "Female in male room";
    public static final String MALE_IN_FEMALE_ROOM = "Male in female room";
    public static final String DIFFERENT_GENDER_IN_SAME_GENDER_ROOM_IN_SAME_NIGHT =
            "Different gender in same gender room in same night";
    public static final String DEPARTMENT_MINIMUM_AGE = "Department minimum age";
    public static final String DEPARTMENT_MAXIMUM_AGE = "Department maximum age";
    public static final String REQUIRED_PATIENT_EQUIPMENT = "Required patient equipment";

    public static final String ASSIGN_EVERY_PATIENT_TO_A_BED = "Assign every patient to a bed";

    public static final String PREFERRED_MAXIMUM_ROOM_CAPACITY = "Preferred maximum room capacity";
    public static final String DEPARTMENT_SPECIALTY = "Department specialty";
    public static final String DEPARTMENT_SPECIALTY_NOT_FIRST_PRIORITY = "Department specialty not first priority";
    public static final String PREFERRED_PATIENT_EQUIPMENT = "Preferred patient equipment";

    private BedPlanConstraintProperties() {
    }
}
