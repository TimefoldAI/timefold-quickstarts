package org.acme.conferencescheduling.domain;

import static java.util.Collections.emptyList;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.SequencedSet;
import java.util.Set;

import ai.timefold.solver.core.api.domain.common.PlanningId;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

import com.fasterxml.jackson.annotation.JsonIgnore;

@PlanningEntity
public class Talk {

    @PlanningId
    private String code;
    private String title;
    private TalkType talkType;
    private List<Speaker> speakers = emptyList();
    private SequencedSet<String> themeTrackTags = new LinkedHashSet<>();
    private SequencedSet<String> sectorTags = new LinkedHashSet<>();
    private SequencedSet<String> audienceTypes = new LinkedHashSet<>();
    private int audienceLevel;
    private SequencedSet<String> contentTags = new LinkedHashSet<>();
    private String language;
    private SequencedSet<String> requiredTimeslotTags = new LinkedHashSet<>();
    private SequencedSet<String> preferredTimeslotTags = new LinkedHashSet<>();
    private SequencedSet<String> prohibitedTimeslotTags = new LinkedHashSet<>();
    private SequencedSet<String> undesiredTimeslotTags = new LinkedHashSet<>();
    private SequencedSet<String> requiredRoomTags = new LinkedHashSet<>();
    private SequencedSet<String> preferredRoomTags = new LinkedHashSet<>();
    private SequencedSet<String> prohibitedRoomTags = new LinkedHashSet<>();
    private SequencedSet<String> undesiredRoomTags = new LinkedHashSet<>();
    private SequencedSet<String> mutuallyExclusiveTalksTags = new LinkedHashSet<>();
    private SequencedSet<Talk> prerequisiteTalks = new LinkedHashSet<>();
    private int favoriteCount;
    private int crowdControlRisk;

    @PlanningVariable
    private Timeslot timeslot;

    @PlanningVariable
    private Room room;

    public Talk() {
    }

    public static Builder builder(String code) {
        return new Builder(code);
    }

    public static final class Builder {

        private final String code;
        private String title;
        private TalkType talkType;
        private List<Speaker> speakers = emptyList();
        private SequencedSet<String> themeTrackTags = new LinkedHashSet<>();
        private SequencedSet<String> sectorTags = new LinkedHashSet<>();
        private SequencedSet<String> audienceTypes = new LinkedHashSet<>();
        private int audienceLevel;
        private SequencedSet<String> contentTags = new LinkedHashSet<>();
        private String language;
        private SequencedSet<String> requiredTimeslotTags = new LinkedHashSet<>();
        private SequencedSet<String> preferredTimeslotTags = new LinkedHashSet<>();
        private SequencedSet<String> prohibitedTimeslotTags = new LinkedHashSet<>();
        private SequencedSet<String> undesiredTimeslotTags = new LinkedHashSet<>();
        private SequencedSet<String> requiredRoomTags = new LinkedHashSet<>();
        private SequencedSet<String> preferredRoomTags = new LinkedHashSet<>();
        private SequencedSet<String> prohibitedRoomTags = new LinkedHashSet<>();
        private SequencedSet<String> undesiredRoomTags = new LinkedHashSet<>();
        private SequencedSet<String> mutuallyExclusiveTalksTags = new LinkedHashSet<>();
        private SequencedSet<Talk> prerequisiteTalks = new LinkedHashSet<>();
        private int favoriteCount;
        private int crowdControlRisk;
        private Timeslot timeslot;
        private Room room;

        private Builder(String code) {
            this.code = code;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder talkType(TalkType talkType) {
            this.talkType = talkType;
            return this;
        }

        public Builder speakers(List<Speaker> speakers) {
            this.speakers = speakers;
            return this;
        }

        public Builder timeslot(Timeslot timeslot) {
            this.timeslot = timeslot;
            return this;
        }

        public Builder room(Room room) {
            this.room = room;
            return this;
        }

        public Builder themeTrackTags(SequencedSet<String> themeTrackTags) {
            this.themeTrackTags = themeTrackTags;
            return this;
        }

        public Builder sectorTags(SequencedSet<String> sectorTags) {
            this.sectorTags = sectorTags;
            return this;
        }

        public Builder audienceTypes(SequencedSet<String> audienceTypes) {
            this.audienceTypes = audienceTypes;
            return this;
        }

        public Builder audienceLevel(int audienceLevel) {
            this.audienceLevel = audienceLevel;
            return this;
        }

        public Builder contentTags(SequencedSet<String> contentTags) {
            this.contentTags = contentTags;
            return this;
        }

        public Builder language(String language) {
            this.language = language;
            return this;
        }

        public Builder requiredTimeslotTags(SequencedSet<String> requiredTimeslotTags) {
            this.requiredTimeslotTags = requiredTimeslotTags;
            return this;
        }

        public Builder preferredTimeslotTags(SequencedSet<String> preferredTimeslotTags) {
            this.preferredTimeslotTags = preferredTimeslotTags;
            return this;
        }

        public Builder prohibitedTimeslotTags(SequencedSet<String> prohibitedTimeslotTags) {
            this.prohibitedTimeslotTags = prohibitedTimeslotTags;
            return this;
        }

        public Builder undesiredTimeslotTags(SequencedSet<String> undesiredTimeslotTags) {
            this.undesiredTimeslotTags = undesiredTimeslotTags;
            return this;
        }

        public Builder requiredRoomTags(SequencedSet<String> requiredRoomTags) {
            this.requiredRoomTags = requiredRoomTags;
            return this;
        }

        public Builder preferredRoomTags(SequencedSet<String> preferredRoomTags) {
            this.preferredRoomTags = preferredRoomTags;
            return this;
        }

        public Builder prohibitedRoomTags(SequencedSet<String> prohibitedRoomTags) {
            this.prohibitedRoomTags = prohibitedRoomTags;
            return this;
        }

        public Builder undesiredRoomTags(SequencedSet<String> undesiredRoomTags) {
            this.undesiredRoomTags = undesiredRoomTags;
            return this;
        }

        public Builder mutuallyExclusiveTalksTags(SequencedSet<String> mutuallyExclusiveTalksTags) {
            this.mutuallyExclusiveTalksTags = mutuallyExclusiveTalksTags;
            return this;
        }

        public Builder prerequisiteTalks(SequencedSet<Talk> prerequisiteTalks) {
            this.prerequisiteTalks = prerequisiteTalks;
            return this;
        }

        public Builder favoriteCount(int favoriteCount) {
            this.favoriteCount = favoriteCount;
            return this;
        }

        public Builder crowdControlRisk(int crowdControlRisk) {
            this.crowdControlRisk = crowdControlRisk;
            return this;
        }

        public Talk build() {
            Talk talk = new Talk();
            talk.code = code;
            talk.title = title;
            talk.talkType = talkType;
            talk.speakers = speakers;
            talk.themeTrackTags = themeTrackTags;
            talk.sectorTags = sectorTags;
            talk.audienceTypes = audienceTypes;
            talk.audienceLevel = audienceLevel;
            talk.contentTags = contentTags;
            talk.language = language;
            talk.requiredTimeslotTags = requiredTimeslotTags;
            talk.preferredTimeslotTags = preferredTimeslotTags;
            talk.prohibitedTimeslotTags = prohibitedTimeslotTags;
            talk.undesiredTimeslotTags = undesiredTimeslotTags;
            talk.requiredRoomTags = requiredRoomTags;
            talk.preferredRoomTags = preferredRoomTags;
            talk.prohibitedRoomTags = prohibitedRoomTags;
            talk.undesiredRoomTags = undesiredRoomTags;
            talk.mutuallyExclusiveTalksTags = mutuallyExclusiveTalksTags;
            talk.prerequisiteTalks = prerequisiteTalks;
            talk.favoriteCount = favoriteCount;
            talk.crowdControlRisk = crowdControlRisk;
            talk.timeslot = timeslot;
            talk.room = room;
            return talk;
        }
    }

    @ValueRangeProvider
    public Set<Timeslot> getTimeslotRange() {
        return talkType.compatibleTimeslots();
    }

    @ValueRangeProvider
    public Set<Room> getRoomRange() {
        return talkType.compatibleRooms();
    }

    public int overlappingThemeTrackCount(Talk other) {
        return overlappingCount(themeTrackTags, other.themeTrackTags);
    }

    private static <T> int overlappingCount(Set<T> left, Set<T> right) {
        if (left.isEmpty() || right.isEmpty()) {
            return 0;
        }
        int overlappingCount = 0;
        for (T t : left) {
            if (right.contains(t)) {
                overlappingCount++;
            }
        }
        return overlappingCount;
    }

    public int overlappingSectorCount(Talk other) {
        return overlappingCount(sectorTags, other.sectorTags);
    }

    public int overlappingAudienceTypeCount(Talk other) {
        return overlappingCount(audienceTypes, other.audienceTypes);

    }

    public int overlappingContentCount(Talk other) {
        return overlappingCount(contentTags, other.contentTags);

    }

    public int missingRequiredTimeslotTagCount() {
        if (timeslot == null) {
            return 0;
        }
        return missingCount(requiredTimeslotTags, timeslot.getTags());

    }

    private static <T> int missingCount(Set<T> required, Set<T> available) {
        if (required.isEmpty()) {
            return 0; // If no items are required, none can be missing.
        }
        if (available.isEmpty()) {
            return required.size(); // All the items are missing.
        }
        int missingCount = 0;
        for (T t : required) {
            if (!available.contains(t)) {
                missingCount++;
            }
        }
        return missingCount;
    }

    public int missingPreferredTimeslotTagCount() {
        if (timeslot == null) {
            return 0;
        }
        return missingCount(preferredTimeslotTags, timeslot.getTags());
    }

    public int prevailingProhibitedTimeslotTagCount() {
        if (timeslot == null) {
            return 0;
        }
        return overlappingCount(prohibitedTimeslotTags, timeslot.getTags());
    }

    public int prevailingUndesiredTimeslotTagCount() {
        if (timeslot == null) {
            return 0;
        }
        return overlappingCount(undesiredTimeslotTags, timeslot.getTags());
    }

    public int missingRequiredRoomTagCount() {
        if (room == null) {
            return 0;
        }
        return missingCount(requiredRoomTags, room.tags());

    }

    public int missingPreferredRoomTagCount() {
        if (room == null) {
            return 0;
        }
        return missingCount(preferredRoomTags, room.tags());
    }

    public int prevailingProhibitedRoomTagCount() {
        if (room == null) {
            return 0;
        }
        return overlappingCount(prohibitedRoomTags, room.tags());

    }

    public int prevailingUndesiredRoomTagCount() {
        if (room == null) {
            return 0;
        }
        return overlappingCount(undesiredRoomTags, room.tags());
    }

    public int missingSpeakerRequiredTimeslotTagCount() {
        if (timeslot == null) {
            return 0;
        }
        int count = 0;
        for (Speaker speaker : speakers) {
            count += missingCount(speaker.requiredTimeslotTags(), timeslot.getTags());
        }
        return count;
    }

    public int missingSpeakerPreferredTimeslotTagCount() {
        if (timeslot == null) {
            return 0;
        }
        int count = 0;
        for (Speaker speaker : speakers) {
            count += missingCount(speaker.preferredTimeslotTags(), timeslot.getTags());
        }
        return count;
    }

    public int prevailingSpeakerProhibitedTimeslotTagCount() {
        if (timeslot == null) {
            return 0;
        }
        int count = 0;
        for (Speaker speaker : speakers) {
            count += overlappingCount(speaker.prohibitedTimeslotTags(), timeslot.getTags());
        }
        return count;
    }

    public int prevailingSpeakerUndesiredTimeslotTagCount() {
        if (timeslot == null) {
            return 0;
        }
        int count = 0;
        for (Speaker speaker : speakers) {
            count += overlappingCount(speaker.undesiredTimeslotTags(), timeslot.getTags());
        }
        return count;
    }

    public int missingSpeakerRequiredRoomTagCount() {
        if (room == null) {
            return 0;
        }
        int count = 0;
        for (Speaker speaker : speakers) {
            count += missingCount(speaker.requiredRoomTags(), room.tags());
        }
        return count;
    }

    public int missingSpeakerPreferredRoomTagCount() {
        if (room == null) {
            return 0;
        }
        int count = 0;
        for (Speaker speaker : speakers) {
            count += missingCount(speaker.preferredRoomTags(), room.tags());
        }
        return count;
    }

    public int prevailingSpeakerProhibitedRoomTagCount() {
        if (room == null) {
            return 0;
        }
        int count = 0;
        for (Speaker speaker : speakers) {
            count += overlappingCount(speaker.prohibitedRoomTags(), room.tags());
        }
        return count;
    }

    public int prevailingSpeakerUndesiredRoomTagCount() {
        if (room == null) {
            return 0;
        }
        int count = 0;
        for (Speaker speaker : speakers) {
            count += overlappingCount(speaker.undesiredRoomTags(), room.tags());
        }
        return count;
    }

    public boolean hasUnavailableRoom() {
        if (timeslot == null || room == null) {
            return false;
        }
        return room.unavailableTimeslots().contains(timeslot);
    }

    public int overlappingMutuallyExclusiveTalksTagCount(Talk other) {
        return overlappingCount(mutuallyExclusiveTalksTags, other.mutuallyExclusiveTalksTags);
    }

    @JsonIgnore
    public Integer getDurationInMinutes() {
        return timeslot == null ? null : timeslot.getDurationInMinutes();
    }

    public int overlappingDurationInMinutes(Talk other) {
        if (timeslot == null) {
            return 0;
        }
        if (other.getTimeslot() == null) {
            return 0;
        }
        return timeslot.getOverlapInMinutes(other.getTimeslot());
    }

    public int combinedDurationInMinutes(Talk other) {
        if (timeslot == null) {
            return 0;
        }
        if (other.getTimeslot() == null) {
            return 0;
        }
        return timeslot.getDurationInMinutes() + other.getTimeslot().getDurationInMinutes();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public TalkType getTalkType() {
        return talkType;
    }

    public void setTalkType(TalkType talkType) {
        this.talkType = talkType;
    }

    public List<Speaker> getSpeakers() {
        return speakers;
    }

    public void setSpeakers(List<Speaker> speakers) {
        this.speakers = speakers;
    }

    public SequencedSet<String> getThemeTrackTags() {
        return themeTrackTags;
    }

    public void setThemeTrackTags(SequencedSet<String> themeTrackTags) {
        this.themeTrackTags = themeTrackTags;
    }

    public SequencedSet<String> getSectorTags() {
        return sectorTags;
    }

    public void setSectorTags(SequencedSet<String> sectorTags) {
        this.sectorTags = sectorTags;
    }

    public SequencedSet<String> getAudienceTypes() {
        return audienceTypes;
    }

    public void setAudienceTypes(SequencedSet<String> audienceTypes) {
        this.audienceTypes = audienceTypes;
    }

    public int getAudienceLevel() {
        return audienceLevel;
    }

    public void setAudienceLevel(int audienceLevel) {
        this.audienceLevel = audienceLevel;
    }

    public SequencedSet<String> getContentTags() {
        return contentTags;
    }

    public void setContentTags(SequencedSet<String> contentTags) {
        this.contentTags = contentTags;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public SequencedSet<String> getRequiredTimeslotTags() {
        return requiredTimeslotTags;
    }

    public void setRequiredTimeslotTags(SequencedSet<String> requiredTimeslotTags) {
        this.requiredTimeslotTags = requiredTimeslotTags;
    }

    public SequencedSet<String> getPreferredTimeslotTags() {
        return preferredTimeslotTags;
    }

    public void setPreferredTimeslotTags(SequencedSet<String> preferredTimeslotTags) {
        this.preferredTimeslotTags = preferredTimeslotTags;
    }

    public SequencedSet<String> getProhibitedTimeslotTags() {
        return prohibitedTimeslotTags;
    }

    public void setProhibitedTimeslotTags(SequencedSet<String> prohibitedTimeslotTags) {
        this.prohibitedTimeslotTags = prohibitedTimeslotTags;
    }

    public SequencedSet<String> getUndesiredTimeslotTags() {
        return undesiredTimeslotTags;
    }

    public void setUndesiredTimeslotTags(SequencedSet<String> undesiredTimeslotTags) {
        this.undesiredTimeslotTags = undesiredTimeslotTags;
    }

    public boolean isScheduled() {
        return timeslot != null && room != null;
    }

    public SequencedSet<String> getRequiredRoomTags() {
        return requiredRoomTags;
    }

    public void setRequiredRoomTags(SequencedSet<String> requiredRoomTags) {
        this.requiredRoomTags = requiredRoomTags;
    }

    public SequencedSet<String> getPreferredRoomTags() {
        return preferredRoomTags;
    }

    public void setPreferredRoomTags(SequencedSet<String> preferredRoomTags) {
        this.preferredRoomTags = preferredRoomTags;
    }

    public SequencedSet<String> getProhibitedRoomTags() {
        return prohibitedRoomTags;
    }

    public void setProhibitedRoomTags(SequencedSet<String> prohibitedRoomTags) {
        this.prohibitedRoomTags = prohibitedRoomTags;
    }

    public SequencedSet<String> getUndesiredRoomTags() {
        return undesiredRoomTags;
    }

    public void setUndesiredRoomTags(SequencedSet<String> undesiredRoomTags) {
        this.undesiredRoomTags = undesiredRoomTags;
    }

    public SequencedSet<String> getMutuallyExclusiveTalksTags() {
        return mutuallyExclusiveTalksTags;
    }

    public void setMutuallyExclusiveTalksTags(SequencedSet<String> mutuallyExclusiveTalksTags) {
        this.mutuallyExclusiveTalksTags = mutuallyExclusiveTalksTags;
    }

    public SequencedSet<Talk> getPrerequisiteTalks() {
        return prerequisiteTalks;
    }

    public void setPrerequisiteTalks(SequencedSet<Talk> prerequisiteTalks) {
        this.prerequisiteTalks = prerequisiteTalks;
    }

    public int getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(int favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public int getCrowdControlRisk() {
        return crowdControlRisk;
    }

    public void setCrowdControlRisk(int crowdControlRisk) {
        this.crowdControlRisk = crowdControlRisk;
    }

    public Timeslot getTimeslot() {
        return timeslot;
    }

    public void setTimeslot(Timeslot timeslot) {
        this.timeslot = timeslot;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Talk talk)) {
            return false;
        }
        return Objects.equals(getCode(), talk.getCode());
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

    @Override
    public String toString() {
        return code;
    }
}
