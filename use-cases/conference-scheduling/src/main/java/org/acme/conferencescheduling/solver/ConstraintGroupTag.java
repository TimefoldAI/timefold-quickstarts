package org.acme.conferencescheduling.solver;

public enum ConstraintGroupTag {
    CONFLICT_FREE_PLANNING("conflict-free planning"),
    TAG_COMPLIANCE("tag compliance"),
    PROGRAM_QUALITY("program quality"),
    ATTENDEE_AND_SPEAKER_SATISFACTION("attendee and speaker satisfaction");

    private final String tag;

    ConstraintGroupTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
