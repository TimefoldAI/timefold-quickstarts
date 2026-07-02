package org.acme.taskassigning.domain;

public enum Affinity {
    NONE(4),
    LOW(3),
    MEDIUM(2),
    HIGH(1);

    private final int durationMultiplier;

    Affinity(int durationMultiplier) {
        this.durationMultiplier = durationMultiplier;
    }

    public int getDurationMultiplier() {
        return durationMultiplier;
    }
}
