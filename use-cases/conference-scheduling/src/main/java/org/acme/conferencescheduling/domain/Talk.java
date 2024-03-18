package org.acme.conferencescheduling.domain;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.entity.PlanningPin;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

import com.fasterxml.jackson.annotation.JsonIgnore;

@PlanningEntity
public class Talk {

    @PlanningId
    private String code;
    private String title;
    private TalkType talkType;
    private List<Speaker> speakers;
    private Set<String> themeTrackTags;
    private Set<String> sectorTags;
    private Set<String> audienceTypes;
    private int audienceLevel;
    private Set<String> contentTags;
    private String language;
    private Set<String> requiredTimeslotTags;
    private Set<String> preferredTimeslotTags;
    private Set<String> prohibitedTimeslotTags;
    private Set<String> undesiredTimeslotTags;
    private Set<String> requiredRoomTags;
    private Set<String> preferredRoomTags;
    private Set<String> prohibitedRoomTags;
    private Set<String> undesiredRoomTags;
    private Set<String> mutuallyExclusiveTalksTags;
    private Set<Talk> prerequisiteTalks;
    private int favoriteCount;
    private int crowdControlRisk;
    private Timeslot publishedTimeslot;
    private Room publishedRoom;

    @PlanningPin
    private boolean pinnedByUser = false;

    @PlanningVariable
    private Timeslot timeslot;

    @PlanningVariable
    private Room room;

    public Talk() {
    }

    public Talk(String code, Timeslot timeslot, Room room) {
        this(code, timeslot, room, emptyList());
    }

    public Talk(String code, Timeslot timeslot, Room room, List<Speaker> speakers) {
        this(code, null, null, speakers, emptySet(), emptySet(), emptySet(), 0, emptySet(), null, 0, 0);
        this.timeslot = timeslot;
        this.room = room;
    }

    public Talk(String code, String title, TalkType talkType, List<Speaker> speakers, Set<String> themeTrackTags,
            Set<String> sectorTags, Set<String> audienceTypes, int audienceLevel, Set<String> contentTags,
            String language, int favoriteCount, int crowdControlRisk) {
        this(code, title, talkType, speakers, themeTrackTags, sectorTags, audienceTypes, audienceLevel, contentTags,
                language, emptySet(), emptySet(), emptySet(), emptySet(), emptySet(), emptySet(), emptySet(),
                emptySet(), emptySet(), emptySet(), favoriteCount, crowdControlRisk, null,
                null);
    }

    public Talk(String code, String title, TalkType talkType, List<Speaker> speakers, Set<String> themeTrackTags,
            Set<String> sectorTags, Set<String> audienceTypes, int audienceLevel, Set<String> contentTags,
            String language, Set<String> requiredTimeslotTags, Set<String> preferredTimeslotTags,
            Set<String> prohibitedTimeslotTags, Set<String> undesiredTimeslotTags, Set<String> requiredRoomTags,
            Set<String> preferredRoomTags, Set<String> prohibitedRoomTags, Set<String> undesiredRoomTags,
            Set<String> mutuallyExclusiveTalksTags, Set<Talk> prerequisiteTalks, int favoriteCount, int crowdControlRisk,
            Timeslot publishedTimeslot, Room publishedRoom) {
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
        this.publishedTimeslot = publishedTimeslot;
        this.publishedRoom = publishedRoom;
    }

    @ValueRangeProvider
    public Set<Timeslot> getTimeslotRange() {
        return talkType.getCompatibleTimeslots();
    }

    @ValueRangeProvider
    public Set<Room> getRoomRange() {
        return talkType.getCompatibleRooms();
    }

    public boolean hasSpeaker(Speaker speaker) {
        return speakers.contains(speaker);
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
        return missingCount(requiredRoomTags, room.getTags());

    }

    public int missingPreferredRoomTagCount() {
        if (room == null) {
            return 0;
        }
        return missingCount(preferredRoomTags, room.getTags());
    }

    public int prevailingProhibitedRoomTagCount() {
        if (room == null) {
            return 0;
        }
        return overlappingCount(prohibitedRoomTags, room.getTags());

    }

    public int prevailingUndesiredRoomTagCount() {
        if (room == null) {
            return 0;
        }
        return overlappingCount(undesiredRoomTags, room.getTags());
    }

    public int missingSpeakerRequiredTimeslotTagCount() {
        if (timeslot == null) {
            return 0;
        }
        int count = 0;
        for (Speaker speaker : speakers) {
            count += missingCount(speaker.getRequiredTimeslotTags(), timeslot.getTags());
        }
        return count;
    }

    public int missingSpeakerPreferredTimeslotTagCount() {
        if (timeslot == null) {
            return 0;
        }
        int count = 0;
        for (Speaker speaker : speakers) {
            count += missingCount(speaker.getPreferredTimeslotTags(), timeslot.getTags());
        }
        return count;
    }

    public int prevailingSpeakerProhibitedTimeslotTagCount() {
        if (timeslot == null) {
            return 0;
        }
        int count = 0;
        for (Speaker speaker : speakers) {
            count += overlappingCount(speaker.getProhibitedTimeslotTags(), timeslot.getTags());
        }
        return count;
    }

    public int prevailingSpeakerUndesiredTimeslotTagCount() {
        if (timeslot == null) {
            return 0;
        }
        int count = 0;
        for (Speaker speaker : speakers) {
            count += overlappingCount(speaker.getUndesiredTimeslotTags(), timeslot.getTags());
        }
        return count;
    }

    public int missingSpeakerRequiredRoomTagCount() {
        if (room == null) {
            return 0;
        }
        int count = 0;
        for (Speaker speaker : speakers) {
            count += missingCount(speaker.getRequiredRoomTags(), room.getTags());
        }
        return count;
    }

    public int missingSpeakerPreferredRoomTagCount() {
        if (room == null) {
            return 0;
        }
        int count = 0;
        for (Speaker speaker : speakers) {
            count += missingCount(speaker.getPreferredRoomTags(), room.getTags());
        }
        return count;
    }

    public int prevailingSpeakerProhibitedRoomTagCount() {
        if (room == null) {
            return 0;
        }
        int count = 0;
        for (Speaker speaker : speakers) {
            count += overlappingCount(speaker.getProhibitedRoomTags(), room.getTags());
        }
        return count;
    }

    public int prevailingSpeakerUndesiredRoomTagCount() {
        if (room == null) {
            return 0;
        }
        int count = 0;
        for (Speaker speaker : speakers) {
            count += overlappingCount(speaker.getUndesiredRoomTags(), room.getTags());
        }
        return count;
    }

    public boolean hasUnavailableRoom() {
        if (timeslot == null || room == null) {
            return false;
        }
        return room.getUnavailableTimeslots().contains(timeslot);
    }

    public int overlappingMutuallyExclusiveTalksTagCount(Talk other) {
        return overlappingCount(mutuallyExclusiveTalksTags, other.mutuallyExclusiveTalksTags);
    }

    public boolean hasMutualSpeaker(Talk other) {
        for (Speaker speaker : speakers) {
            if (other.hasSpeaker(speaker)) {
                return true;
            }
        }
        return false;
    }

    @JsonIgnore
    public Integer getDurationInMinutes() {
        return timeslot == null ? null : timeslot.getDurationInMinutes();
    }

    public boolean overlapsTime(Talk other) {
        return timeslot != null && other.getTimeslot() != null && timeslot.overlapsTime(other.getTimeslot());
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

    public Set<String> getThemeTrackTags() {
        return themeTrackTags;
    }

    public void setThemeTrackTags(Set<String> themeTrackTags) {
        this.themeTrackTags = themeTrackTags;
    }

    public Set<String> getSectorTags() {
        return sectorTags;
    }

    public void setSectorTags(Set<String> sectorTags) {
        this.sectorTags = sectorTags;
    }

    public Set<String> getAudienceTypes() {
        return audienceTypes;
    }

    public void setAudienceTypes(Set<String> audienceTypes) {
        this.audienceTypes = audienceTypes;
    }

    public int getAudienceLevel() {
        return audienceLevel;
    }

    public void setAudienceLevel(int audienceLevel) {
        this.audienceLevel = audienceLevel;
    }

    public Set<String> getContentTags() {
        return contentTags;
    }

    public void setContentTags(Set<String> contentTags) {
        this.contentTags = contentTags;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Set<String> getRequiredTimeslotTags() {
        return requiredTimeslotTags;
    }

    public void setRequiredTimeslotTags(Set<String> requiredTimeslotTags) {
        this.requiredTimeslotTags = requiredTimeslotTags;
    }

    public Set<String> getPreferredTimeslotTags() {
        return preferredTimeslotTags;
    }

    public void setPreferredTimeslotTags(Set<String> preferredTimeslotTags) {
        this.preferredTimeslotTags = preferredTimeslotTags;
    }

    public Set<String> getProhibitedTimeslotTags() {
        return prohibitedTimeslotTags;
    }

    public void setProhibitedTimeslotTags(Set<String> prohibitedTimeslotTags) {
        this.prohibitedTimeslotTags = prohibitedTimeslotTags;
    }

    public Set<String> getUndesiredTimeslotTags() {
        return undesiredTimeslotTags;
    }

    public void setUndesiredTimeslotTags(Set<String> undesiredTimeslotTags) {
        this.undesiredTimeslotTags = undesiredTimeslotTags;
    }

    public Set<String> getRequiredRoomTags() {
        return requiredRoomTags;
    }

    public void setRequiredRoomTags(Set<String> requiredRoomTags) {
        this.requiredRoomTags = requiredRoomTags;
    }

    public Set<String> getPreferredRoomTags() {
        return preferredRoomTags;
    }

    public void setPreferredRoomTags(Set<String> preferredRoomTags) {
        this.preferredRoomTags = preferredRoomTags;
    }

    public Set<String> getProhibitedRoomTags() {
        return prohibitedRoomTags;
    }

    public void setProhibitedRoomTags(Set<String> prohibitedRoomTags) {
        this.prohibitedRoomTags = prohibitedRoomTags;
    }

    public Set<String> getUndesiredRoomTags() {
        return undesiredRoomTags;
    }

    public void setUndesiredRoomTags(Set<String> undesiredRoomTags) {
        this.undesiredRoomTags = undesiredRoomTags;
    }

    public Set<String> getMutuallyExclusiveTalksTags() {
        return mutuallyExclusiveTalksTags;
    }

    public void setMutuallyExclusiveTalksTags(Set<String> mutuallyExclusiveTalksTags) {
        this.mutuallyExclusiveTalksTags = mutuallyExclusiveTalksTags;
    }

    public Set<Talk> getPrerequisiteTalks() {
        return prerequisiteTalks;
    }

    public void setPrerequisiteTalks(Set<Talk> prerequisiteTalks) {
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

    public Timeslot getPublishedTimeslot() {
        return publishedTimeslot;
    }

    public void setPublishedTimeslot(Timeslot publishedTimeslot) {
        this.publishedTimeslot = publishedTimeslot;
    }

    public Room getPublishedRoom() {
        return publishedRoom;
    }

    public void setPublishedRoom(Room publishedRoom) {
        this.publishedRoom = publishedRoom;
    }

    public boolean isPinnedByUser() {
        return pinnedByUser;
    }

    public void setPinnedByUser(boolean pinnedByUser) {
        this.pinnedByUser = pinnedByUser;
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
        if (this == o)
            return true;
        if (!(o instanceof Talk talk))
            return false;
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
