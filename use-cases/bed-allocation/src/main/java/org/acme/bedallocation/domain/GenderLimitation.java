package org.acme.bedallocation.domain;

public enum GenderLimitation {
    ANY_GENDER("N"), // mixed
    MALE_ONLY("M"),
    FEMALE_ONLY("F"),
    SAME_GENDER("D"); // dependent on the first

    private final String code;

    GenderLimitation(String code) {
        this.code = code;
    }

    public static GenderLimitation valueOfCode(String code) {
        for (GenderLimitation gender : values()) {
            if (code.equalsIgnoreCase(gender.getCode())) {
                return gender;
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }

}
