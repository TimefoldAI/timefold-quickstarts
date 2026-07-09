package org.acme.bedallocation.domain;

public enum Gender {
    MALE("M"),
    FEMALE("F");

    private final String code;

    Gender(String code) {
        this.code = code;
    }

    public static Gender valueOfCode(String code) {
        for (Gender gender : values()) {
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
