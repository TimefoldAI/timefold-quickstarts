package org.acme.bedallocation.domain;

public enum Equipment {
    TELEMETRY("telemetry"),
    OXYGEN("oxygen"),
    TELEVISION("television"),
    NITROGEN("nitrogen");

    public static Equipment valueOfCode(String code) {
        for (Equipment equipment : values()) {
            if (code.equalsIgnoreCase(equipment.getCode())) {
                return equipment;
            }
        }
        return null;
    }

    private final String code;

    Equipment(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
