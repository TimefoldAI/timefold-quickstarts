package org.acme.bedallocation.domain;

public enum GenderLimitation {
    ANY_GENDER("N"), // mixed
    MALE_ONLY("M"),
    FEMALE_ONLY("F"),
    SAME_GENDER("D");

    private final String code;

    GenderLimitation(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
