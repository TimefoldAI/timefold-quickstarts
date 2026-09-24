package org.acme.meetingschedule.dto.input;

import ai.timefold.solver.service.definition.api.ModelConfigOverrides;
import ai.timefold.solver.service.definition.api.domain.ConstraintReference;

import org.acme.meetingschedule.domain.MeetingScheduleConstraintProperties;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(description = "Soft constraint weights. Set a weight to 0 to disable the corresponding constraint. "
        + "A weight left unset (null) is not overridden here, so the value from the configuration profile "
        + "(or the constraint's default) applies. This makes it possible to override some weights via the "
        + "input while leaving others to the configuration profile.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MeetingScheduleConfigOverrides(
        @ConstraintReference(MeetingScheduleConstraintProperties.DO_ALL_MEETINGS_AS_SOON_AS_POSSIBLE) @Schema(
                description = "Soft weight of the doAllMeetingsAsSoonAsPossible constraint.",
                minimum = "0") Long doAllMeetingsAsSoonAsPossibleWeight,
        @ConstraintReference(MeetingScheduleConstraintProperties.ONE_BREAK_BETWEEN_CONSECUTIVE_MEETINGS) @Schema(
                description = "Soft weight of the oneBreakBetweenConsecutiveMeetings constraint.",
                minimum = "0") Long oneBreakBetweenConsecutiveMeetingsWeight,
        @ConstraintReference(MeetingScheduleConstraintProperties.OVERLAPPING_MEETINGS) @Schema(
                description = "Soft weight of the overlappingMeetings constraint.",
                minimum = "0") Long overlappingMeetingsWeight,
        @ConstraintReference(MeetingScheduleConstraintProperties.ASSIGN_LARGER_ROOMS_FIRST) @Schema(
                description = "Soft weight of the assignLargerRoomsFirst constraint.",
                minimum = "0") Long assignLargerRoomsFirstWeight,
        @ConstraintReference(MeetingScheduleConstraintProperties.ROOM_STABILITY) @Schema(
                description = "Soft weight of the roomStability constraint.", minimum = "0") Long roomStabilityWeight)
        implements
            ModelConfigOverrides {

    /**
     * Creates an empty overrides instance: no weight is overridden, so the configuration profile
     * (or each constraint's default) applies. Required by the Service Module to generate the default config profile.
     */
    public MeetingScheduleConfigOverrides() {
        this(null, null, null, null, null);
    }
}
