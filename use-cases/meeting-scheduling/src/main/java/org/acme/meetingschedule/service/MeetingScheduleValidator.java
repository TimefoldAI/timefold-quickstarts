package org.acme.meetingschedule.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.ModelValidator;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;

import org.acme.meetingschedule.dto.input.MeetingInputDTO;
import org.acme.meetingschedule.dto.input.MeetingScheduleConfigOverrides;
import org.acme.meetingschedule.dto.input.MeetingScheduleInput;
import org.acme.meetingschedule.dto.input.OfficeHoursDTO;
import org.acme.meetingschedule.dto.input.PersonInputDTO;
import org.acme.meetingschedule.dto.input.RoomInputDTO;
import org.acme.meetingschedule.dto.input.TimeConfigurationDTO;
import org.acme.meetingschedule.service.validation.MeetingScheduleIssue;
import org.acme.meetingschedule.service.validation.MeetingScheduleIssue.DuplicateMeetingIdIssue;
import org.acme.meetingschedule.service.validation.MeetingScheduleIssue.DuplicatePersonIdIssue;
import org.acme.meetingschedule.service.validation.MeetingScheduleIssue.DuplicateRoomIdIssue;
import org.acme.meetingschedule.service.validation.MeetingScheduleIssue.InvalidOfficeHoursIssue;
import org.acme.meetingschedule.service.validation.MeetingScheduleIssue.MeetingDurationNotAMultipleOfGranularityIssue;
import org.acme.meetingschedule.service.validation.MeetingScheduleIssue.MeetingLongerThanOfficeDayIssue;
import org.acme.meetingschedule.service.validation.MeetingScheduleIssue.MeetingStartOutsideOfficeHoursIssue;
import org.acme.meetingschedule.service.validation.MeetingScheduleIssue.NonExistingPersonReferenceIssue;
import org.acme.meetingschedule.service.validation.MeetingScheduleIssue.NonExistingRoomReferenceIssue;

@ApplicationScoped
public class MeetingScheduleValidator implements ModelValidator<MeetingScheduleInput, MeetingScheduleConfigOverrides> {

    @Override
    public void validate(ValidationBuilder validationBuilder, MeetingScheduleInput modelInput,
            ModelConfig<MeetingScheduleConfigOverrides> modelConfig) {
        // OpenAPI spec (Bean Validation) compliance is enforced by the Service module at the REST layer,
        // before this validator ever runs; only domain-specific checks belong here.
        Set<String> personIds = collectIds(validationBuilder, orEmpty(modelInput.people()), PersonInputDTO::id,
                DuplicatePersonIdIssue::new);
        Set<String> roomIds = collectIds(validationBuilder, orEmpty(modelInput.rooms()), RoomInputDTO::id,
                DuplicateRoomIdIssue::new);
        TimeConfigurationDTO timeConfiguration = modelInput.timeConfiguration();
        validateOfficeHours(validationBuilder, timeConfiguration);
        validateMeetings(validationBuilder, orEmpty(modelInput.meetings()), personIds, roomIds, timeConfiguration);
    }

    /**
     * Collects the ids of {@code elements}, reporting one duplicate issue per repeated id.
     *
     * @return the distinct, non-blank ids, so the reference checks below can resolve against them
     */
    private static <T> Set<String> collectIds(ValidationBuilder validationBuilder, List<T> elements,
            Function<T, String> idExtractor, Function<String, MeetingScheduleIssue> duplicateIssueFactory) {
        Set<String> ids = new HashSet<>();
        for (T element : elements) {
            String id = idExtractor.apply(element);
            if (hasId(id) && !ids.add(id)) {
                validationBuilder.addIssue(duplicateIssueFactory.apply(id));
            }
        }
        return ids;
    }

    private static void validateOfficeHours(ValidationBuilder validationBuilder,
            TimeConfigurationDTO timeConfiguration) {
        if (timeConfiguration == null || timeConfiguration.granularityInMinutes() == null
                || timeConfiguration.granularityInMinutes() <= 0) {
            return; // The OpenAPI schema already requires a positive granularity.
        }
        int granularityInMinutes = timeConfiguration.granularityInMinutes();
        for (OfficeHoursDTO day : orEmpty(timeConfiguration.days())) {
            if (day.startDateTime() == null || day.endDateTime() == null
                    || day.startDateTime().plusMinutes(granularityInMinutes).isAfter(day.endDateTime())) {
                validationBuilder.addIssue(new InvalidOfficeHoursIssue(
                        day.startDateTime() == null ? null : day.startDateTime().toString()));
            }
        }
    }

    private static void validateMeetings(ValidationBuilder validationBuilder, List<MeetingInputDTO> meetings,
            Set<String> personIds, Set<String> roomIds, TimeConfigurationDTO timeConfiguration) {
        List<OffsetDateTime> slotStartDateTimes =
                timeConfiguration == null ? List.of() : timeConfiguration.slotStartDateTimes();
        Set<Instant> slotStartInstants = slotStartDateTimes.stream()
                .map(OffsetDateTime::toInstant)
                .collect(Collectors.toSet());
        // A meeting has to start and end on the same day, so the longest day is what bounds its duration.
        Map<LocalDate, Long> slotsPerDate = slotStartDateTimes.stream()
                .collect(Collectors.groupingBy(OffsetDateTime::toLocalDate, Collectors.counting()));
        long longestDayInSlots = slotsPerDate.values().stream().mapToLong(Long::longValue).max().orElse(0);
        Integer granularityInMinutes = timeConfiguration == null ? null : timeConfiguration.granularityInMinutes();

        Set<String> meetingIds = new HashSet<>();
        for (MeetingInputDTO meeting : meetings) {
            if (hasId(meeting.id()) && !meetingIds.add(meeting.id())) {
                validationBuilder.addIssue(new DuplicateMeetingIdIssue(meeting.id()));
                continue;
            }
            // Only one issue per meeting, rather than one per unknown attendee, keeps the issue list
            // bounded for a dataset that refers to a whole batch of people that were never submitted.
            if (hasUnknownAttendee(meeting, personIds)) {
                validationBuilder.addIssue(new NonExistingPersonReferenceIssue(meeting.id()));
            }
            if (meeting.roomId() != null && !roomIds.contains(meeting.roomId())) {
                validationBuilder.addIssue(new NonExistingRoomReferenceIssue(meeting.id()));
            }
            if (meeting.startDateTime() != null && !slotStartInstants.contains(meeting.startDateTime().toInstant())) {
                validationBuilder.addIssue(new MeetingStartOutsideOfficeHoursIssue(meeting.id()));
            }
            validateDuration(validationBuilder, meeting, granularityInMinutes, longestDayInSlots);
        }
    }

    private static void validateDuration(ValidationBuilder validationBuilder, MeetingInputDTO meeting,
            Integer granularityInMinutes, long longestDayInSlots) {
        if (meeting.durationInMinutes() == null || granularityInMinutes == null || granularityInMinutes <= 0) {
            return; // The OpenAPI schema already requires both to be present and positive.
        }
        if (meeting.durationInMinutes() % granularityInMinutes != 0) {
            validationBuilder.addIssue(new MeetingDurationNotAMultipleOfGranularityIssue(meeting.id()));
        } else if (meeting.durationInMinutes() / granularityInMinutes > longestDayInSlots) {
            validationBuilder.addIssue(new MeetingLongerThanOfficeDayIssue(meeting.id()));
        }
    }

    private static boolean hasUnknownAttendee(MeetingInputDTO meeting, Set<String> personIds) {
        return !personIds.containsAll(meeting.requiredAttendeeIds())
                || !personIds.containsAll(meeting.preferredAttendeeIds());
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }

    private static boolean hasId(String id) {
        return id != null && !id.isBlank();
    }
}
