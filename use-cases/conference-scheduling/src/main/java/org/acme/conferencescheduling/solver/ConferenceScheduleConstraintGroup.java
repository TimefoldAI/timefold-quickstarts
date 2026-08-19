package org.acme.conferencescheduling.solver;

import ai.timefold.solver.service.definition.api.description.ConstraintGroupInfo;

public final class ConferenceScheduleConstraintGroup {

    public static final ConstraintGroupInfo CONFLICT_AVOIDANCE = new ConstraintGroupInfo("conflictAvoidance",
            "Conflict avoidance",
            "Keep rooms and speakers free of double bookings and respect talk ordering, pauses and crowd control.",
            "IconDiamond",
            new String[] { ConstraintGroupTag.CONFLICT_FREE_PLANNING.getTag() });

    public static final ConstraintGroupInfo TAG_REQUIREMENTS = new ConstraintGroupInfo("tagRequirements",
            "Tag requirements",
            "Honour the hard timeslot and room tags required or prohibited by talks and speakers.",
            "IconTag",
            new String[] { ConstraintGroupTag.TAG_COMPLIANCE.getTag() });

    public static final ConstraintGroupInfo PROGRAM_QUALITY = new ConstraintGroupInfo("programQuality",
            "Program quality",
            "Spread out related talks and diversify each timeslot across themes, sectors, content, audience and language.",
            "IconStar",
            new String[] { ConstraintGroupTag.PROGRAM_QUALITY.getTag() });

    public static final ConstraintGroupInfo TAG_PREFERENCES = new ConstraintGroupInfo("tagPreferences",
            "Tag preferences",
            "Satisfy the soft timeslot and room tag preferences of talks and speakers and keep speaker schedules compact.",
            "IconHeart",
            new String[] { ConstraintGroupTag.ATTENDEE_AND_SPEAKER_SATISFACTION.getTag() });

    private ConferenceScheduleConstraintGroup() {
    }
}
