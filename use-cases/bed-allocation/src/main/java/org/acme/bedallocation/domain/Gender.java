package org.acme.bedallocation.domain;

public enum Gender {
    MALE("M"),
    FEMALE("F");
    // TODO add NONBINARY(X)

    private final String code;

    Gender(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
