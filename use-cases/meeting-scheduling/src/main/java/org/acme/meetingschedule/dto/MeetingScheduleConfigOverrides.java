package org.acme.meetingschedule.dto;

import ai.timefold.solver.service.definition.api.ModelConfigOverrides;
import ai.timefold.solver.service.definition.api.domain.ConstraintReference;

import org.acme.meetingschedule.solver.MeetingScheduleConstraintProperties;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(description = "Soft constraint weights. Set a weight to 0 to disable the corresponding constraint. "
        + "A weight left unset (null) is not overridden here, so the value from the configuration profile "
        + "(or the constraint's default) applies. This makes it possible to override some weights via the "
        + "input while leaving others to the configuration profile.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MeetingScheduleConfigOverrides(
        @ConstraintReference(MeetingScheduleConstraintProperties.DO_ALL_MEETINGS_AS_SOON_AS_POSSIBLE) @Schema(
                description = "Soft weight of the do meetings as soon as possible constraint.") Long doMeetingsAsSoonAsPossibleWeight,
        @ConstraintReference(MeetingScheduleConstraintProperties.ONE_TIME_GRAIN_BREAK_BETWEEN_TWO_CONSECUTIVE_MEETINGS) @Schema(
                description = "Soft weight of the one break between consecutive meetings constraint.") Long oneBreakBetweenConsecutiveMeetingsWeight,
        @ConstraintReference(MeetingScheduleConstraintProperties.OVERLAPPING_MEETINGS) @Schema(
                description = "Soft weight of the overlapping meetings constraint.") Long overlappingMeetingsWeight,
        @ConstraintReference(MeetingScheduleConstraintProperties.ASSIGN_LARGER_ROOMS_FIRST) @Schema(
                description = "Soft weight of the assign larger rooms first constraint.") Long assignLargerRoomsFirstWeight,
        @ConstraintReference(MeetingScheduleConstraintProperties.ROOM_STABILITY) @Schema(
                description = "Soft weight of the room stability constraint.") Long roomStabilityWeight)
        implements
            ModelConfigOverrides {

    public MeetingScheduleConfigOverrides {
        doMeetingsAsSoonAsPossibleWeight = doMeetingsAsSoonAsPossibleWeight != null && doMeetingsAsSoonAsPossibleWeight < 0L
                ? 0L
                : doMeetingsAsSoonAsPossibleWeight;
        oneBreakBetweenConsecutiveMeetingsWeight =
                oneBreakBetweenConsecutiveMeetingsWeight != null && oneBreakBetweenConsecutiveMeetingsWeight < 0L ? 0L
                        : oneBreakBetweenConsecutiveMeetingsWeight;
        overlappingMeetingsWeight =
                overlappingMeetingsWeight != null && overlappingMeetingsWeight < 0L ? 0L : overlappingMeetingsWeight;
        assignLargerRoomsFirstWeight =
                assignLargerRoomsFirstWeight != null && assignLargerRoomsFirstWeight < 0L ? 0L : assignLargerRoomsFirstWeight;
        roomStabilityWeight = roomStabilityWeight != null && roomStabilityWeight < 0L ? 0L : roomStabilityWeight;
    }

    public MeetingScheduleConfigOverrides() {
        this(1L, 1L, 1L, 1L, 1L);
    }

    public MeetingScheduleConfigOverrides withDoMeetingsAsSoonAsPossibleWeight(Long doMeetingsAsSoonAsPossibleWeight) {
        return new MeetingScheduleConfigOverrides(doMeetingsAsSoonAsPossibleWeight, oneBreakBetweenConsecutiveMeetingsWeight,
                overlappingMeetingsWeight, assignLargerRoomsFirstWeight, roomStabilityWeight);
    }

    public MeetingScheduleConfigOverrides withOneBreakBetweenConsecutiveMeetingsWeight(
            Long oneBreakBetweenConsecutiveMeetingsWeight) {
        return new MeetingScheduleConfigOverrides(doMeetingsAsSoonAsPossibleWeight, oneBreakBetweenConsecutiveMeetingsWeight,
                overlappingMeetingsWeight, assignLargerRoomsFirstWeight, roomStabilityWeight);
    }

    public MeetingScheduleConfigOverrides withOverlappingMeetingsWeight(Long overlappingMeetingsWeight) {
        return new MeetingScheduleConfigOverrides(doMeetingsAsSoonAsPossibleWeight, oneBreakBetweenConsecutiveMeetingsWeight,
                overlappingMeetingsWeight, assignLargerRoomsFirstWeight, roomStabilityWeight);
    }

    public MeetingScheduleConfigOverrides withAssignLargerRoomsFirstWeight(Long assignLargerRoomsFirstWeight) {
        return new MeetingScheduleConfigOverrides(doMeetingsAsSoonAsPossibleWeight, oneBreakBetweenConsecutiveMeetingsWeight,
                overlappingMeetingsWeight, assignLargerRoomsFirstWeight, roomStabilityWeight);
    }

    public MeetingScheduleConfigOverrides withRoomStabilityWeight(Long roomStabilityWeight) {
        return new MeetingScheduleConfigOverrides(doMeetingsAsSoonAsPossibleWeight, oneBreakBetweenConsecutiveMeetingsWeight,
                overlappingMeetingsWeight, assignLargerRoomsFirstWeight, roomStabilityWeight);
    }
}
