package org.acme.bedallocation.domain;

public enum GenderRoomLimitation {
    ANY_GENDER("N"), // mixed
    MALE_ONLY("M"),
    FEMALE_ONLY("F"),
    SAME_GENDER("D");

    private final String code;

    GenderRoomLimitation(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
