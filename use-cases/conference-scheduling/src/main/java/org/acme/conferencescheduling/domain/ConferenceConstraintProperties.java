
package org.acme.conferencescheduling.domain;

public class ConferenceConstraintProperties {

    public static final String ROOM_UNAVAILABLE_TIMESLOT = "Room unavailable timeslot";
    public static final String ROOM_CONFLICT = "Room conflict";
    public static final String SPEAKER_UNAVAILABLE_TIMESLOT = "Speaker unavailable timeslot";
    public static final String SPEAKER_CONFLICT = "Speaker conflict";
    public static final String TALK_PREREQUISITE_TALKS = "Talk prerequisite talks";
    public static final String TALK_MUTUALLY_EXCLUSIVE_TALKS_TAGS = "Talk mutually-exclusive-talks tags";
    public static final String CONSECUTIVE_TALKS_PAUSE = "Consecutive talks pause";
    public static final String CROWD_CONTROL = "Crowd control";

    public static final String SPEAKER_REQUIRED_TIMESLOT_TAGS = "Speaker required timeslot tags";
    public static final String SPEAKER_PROHIBITED_TIMESLOT_TAGS = "Speaker prohibited timeslot tags";
    public static final String TALK_REQUIRED_TIMESLOT_TAGS = "Talk required timeslot tags";
    public static final String TALK_PROHIBITED_TIMESLOT_TAGS = "Talk prohibited timeslot tags";
    public static final String SPEAKER_REQUIRED_ROOM_TAGS = "Speaker required room tags";
    public static final String SPEAKER_PROHIBITED_ROOM_TAGS = "Speaker prohibited room tags";
    public static final String TALK_REQUIRED_ROOM_TAGS = "Talk required room tags";
    public static final String TALK_PROHIBITED_ROOM_TAGS = "Talk prohibited room tags";

    public static final String THEME_TRACK_CONFLICT = "Theme track conflict";
    public static final String THEME_TRACK_ROOM_STABILITY = "Theme track room stability";
    public static final String SECTOR_CONFLICT = "Sector conflict";
    public static final String AUDIENCE_TYPE_DIVERSITY = "Audience type diversity";
    public static final String AUDIENCE_TYPE_THEME_TRACK_CONFLICT = "Audience type theme track conflict";
    public static final String AUDIENCE_LEVEL_DIVERSITY = "Audience level diversity";
    public static final String CONTENT_AUDIENCE_LEVEL_FLOW_VIOLATION = "Content audience level flow violation";
    public static final String CONTENT_CONFLICT = "Content conflict";
    public static final String LANGUAGE_DIVERSITY = "Language diversity";
    public static final String SAME_DAY_TALKS = "Same day talks";
    public static final String POPULAR_TALKS = "Popular talks";

    public static final String SPEAKER_PREFERRED_TIMESLOT_TAGS = "Speaker preferred timeslot tags";
    public static final String SPEAKER_UNDESIRED_TIMESLOT_TAGS = "Speaker undesired timeslot tags";
    public static final String TALK_PREFERRED_TIMESLOT_TAGS = "Talk preferred timeslot tags";
    public static final String TALK_UNDESIRED_TIMESLOT_TAGS = "Talk undesired timeslot tags";
    public static final String SPEAKER_PREFERRED_ROOM_TAGS = "Speaker preferred room tags";
    public static final String SPEAKER_UNDESIRED_ROOM_TAGS = "Speaker undesired room tags";
    public static final String TALK_PREFERRED_ROOM_TAGS = "Talk preferred room tags";
    public static final String TALK_UNDESIRED_ROOM_TAGS = "Talk undesired room tags";
    public static final String SPEAKER_MAKESPAN = "Speaker makespan";

    private int minimumConsecutiveTalksPauseInMinutes = 30;

    public ConferenceConstraintProperties() {
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public int getMinimumConsecutiveTalksPauseInMinutes() {
        return minimumConsecutiveTalksPauseInMinutes;
    }

    public void setMinimumConsecutiveTalksPauseInMinutes(int minimumConsecutiveTalksPauseInMinutes) {
        this.minimumConsecutiveTalksPauseInMinutes = minimumConsecutiveTalksPauseInMinutes;
    }

}
