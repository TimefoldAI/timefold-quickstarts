package org.acme.sportsleagueschedule.domain;

public final class LeagueScheduleConstraintProperties {

    public static final String MATCHES_ON_SAME_DAY = "Matches on the same day";
    public static final String FOUR_CONSECUTIVE_HOME_MATCHES = "4 or more consecutive home matches";
    public static final String FOUR_CONSECUTIVE_AWAY_MATCHES = "4 or more consecutive away matches";
    public static final String REPEAT_MATCH_ON_THE_NEXT_DAY = "Repeat match on the next day";

    public static final String START_TO_AWAY_HOP = "Start to away hop";
    public static final String HOME_TO_AWAY_HOP = "Home to away hop";
    public static final String AWAY_TO_AWAY_HOP = "Away to away hop";
    public static final String AWAY_TO_HOME_HOP = "Away to home hop";
    public static final String AWAY_TO_END_HOP = "Away to end hop";
    public static final String CLASSIC_MATCHES = "Classic matches played on weekends or holidays";

    private LeagueScheduleConstraintProperties() {
    }
}
