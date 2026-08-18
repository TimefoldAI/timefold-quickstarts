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

    public Talk(String code, String title, TalkType talkType, List<Speaker> speakers,
            SequencedSet<String> themeTrackTags, SequencedSet<String> sectorTags, SequencedSet<String> audienceTypes,
            int audienceLevel, SequencedSet<String> contentTags, String language,
            SequencedSet<String> requiredTimeslotTags, SequencedSet<String> preferredTimeslotTags,
            SequencedSet<String> prohibitedTimeslotTags, SequencedSet<String> undesiredTimeslotTags,
            SequencedSet<String> requiredRoomTags, SequencedSet<String> preferredRoomTags,
            SequencedSet<String> prohibitedRoomTags, SequencedSet<String> undesiredRoomTags,
            SequencedSet<String> mutuallyExclusiveTalksTags, SequencedSet<Talk> prerequisiteTalks, int favoriteCount,
            int crowdControlRisk) {
        this.code = code;
        this.title = title;
        this.talkType = talkType;
        this.speakers = speakers;
        this.themeTrackTags = themeTrackTags;
        this.sectorTags = sectorTags;
        this.audienceTypes = audienceTypes;
        this.audienceLevel = audienceLevel;
        this.contentTags = contentTags;
        this.language = language;
        this.requiredTimeslotTags = requiredTimeslotTags;
        this.preferredTimeslotTags = preferredTimeslotTags;
        this.prohibitedTimeslotTags = prohibitedTimeslotTags;
        this.undesiredTimeslotTags = undesiredTimeslotTags;
        this.requiredRoomTags = requiredRoomTags;
        this.preferredRoomTags = preferredRoomTags;
        this.prohibitedRoomTags = prohibitedRoomTags;
        this.undesiredRoomTags = undesiredRoomTags;
        this.mutuallyExclusiveTalksTags = mutuallyExclusiveTalksTags;
        this.prerequisiteTalks = prerequisiteTalks;
        this.favoriteCount = favoriteCount;
        this.crowdControlRisk = crowdControlRisk;
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

    public String getTitle() {
        return title;
    }

    public TalkType getTalkType() {
        return talkType;
    }

    public List<Speaker> getSpeakers() {
        return speakers;
    }

    public SequencedSet<String> getThemeTrackTags() {
        return themeTrackTags;
    }

    public SequencedSet<String> getSectorTags() {
        return sectorTags;
    }

    public SequencedSet<String> getAudienceTypes() {
        return audienceTypes;
    }

    public int getAudienceLevel() {
        return audienceLevel;
    }

    public SequencedSet<String> getContentTags() {
        return contentTags;
    }

    public String getLanguage() {
        return language;
    }

    public SequencedSet<String> getRequiredTimeslotTags() {
        return requiredTimeslotTags;
    }

    public SequencedSet<String> getPreferredTimeslotTags() {
        return preferredTimeslotTags;
    }

    public SequencedSet<String> getProhibitedTimeslotTags() {
        return prohibitedTimeslotTags;
    }

    public SequencedSet<String> getUndesiredTimeslotTags() {
        return undesiredTimeslotTags;
    }

    public boolean isScheduled() {
        return timeslot != null && room != null;
    }

    public SequencedSet<String> getRequiredRoomTags() {
        return requiredRoomTags;
    }

    public SequencedSet<String> getPreferredRoomTags() {
        return preferredRoomTags;
    }

    public SequencedSet<String> getProhibitedRoomTags() {
        return prohibitedRoomTags;
    }

    public SequencedSet<String> getUndesiredRoomTags() {
        return undesiredRoomTags;
    }

    public SequencedSet<String> getMutuallyExclusiveTalksTags() {
        return mutuallyExclusiveTalksTags;
    }

    public SequencedSet<Talk> getPrerequisiteTalks() {
        return prerequisiteTalks;
    }

    public int getFavoriteCount() {
        return favoriteCount;
    }

    public int getCrowdControlRisk() {
        return crowdControlRisk;
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
