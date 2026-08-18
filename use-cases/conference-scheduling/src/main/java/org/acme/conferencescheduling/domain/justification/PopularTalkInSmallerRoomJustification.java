package org.acme.conferencescheduling.domain.justification;

import org.acme.conferencescheduling.domain.Talk;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A more popular talk is placed in a smaller room than a less popular one.")
public record PopularTalkInSmallerRoomJustification(
        @Schema(description = "The code of the more popular talk.") String talk,
        @Schema(description = "The favorite count of the more popular talk.") int favoriteCount,
        @Schema(description = "The id of the room the more popular talk is assigned to.") String room,
        @Schema(description = "The capacity of that room.") int roomCapacity,
        @Schema(description = "The code of the less popular talk.") String lessPopularTalk,
        @Schema(description = "The favorite count of the less popular talk.") int lessPopularFavoriteCount,
        @Schema(description = "The id of the room the less popular talk is assigned to.") String lessPopularRoom,
        @Schema(description = "The capacity of that room.") int lessPopularRoomCapacity)
        implements
            ConferenceSchedulingJustification {

    public static PopularTalkInSmallerRoomJustification of(Talk lessPopularTalk, Talk talk) {
        return new PopularTalkInSmallerRoomJustification(talk.getCode(), talk.getFavoriteCount(), talk.getRoom().id(),
                talk.getRoom().capacity(), lessPopularTalk.getCode(), lessPopularTalk.getFavoriteCount(),
                lessPopularTalk.getRoom().id(), lessPopularTalk.getRoom().capacity());
    }

    @Override
    public String getDescription() {
        return "Talk '%s' with %d favorites is scheduled in room '%s' with capacity %d, while the less popular talk '%s' with %d favorites is scheduled in the larger room '%s' with capacity %d."
                .formatted(talk, favoriteCount, room, roomCapacity, lessPopularTalk, lessPopularFavoriteCount,
                        lessPopularRoom, lessPopularRoomCapacity);
    }
}
