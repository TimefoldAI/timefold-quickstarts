package org.acme.conferencescheduling.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.ModelValidator;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;

import org.acme.conferencescheduling.dto.input.ConferenceScheduleConfigOverrides;
import org.acme.conferencescheduling.dto.input.ConferenceScheduleInput;
import org.acme.conferencescheduling.dto.input.RoomDTO;
import org.acme.conferencescheduling.dto.input.SpeakerDTO;
import org.acme.conferencescheduling.dto.input.TalkDTO;
import org.acme.conferencescheduling.dto.input.TalkTypeDTO;
import org.acme.conferencescheduling.dto.input.TimeslotDTO;
import org.acme.conferencescheduling.service.validation.ConferenceScheduleIssue.DuplicateRoomIdIssue;
import org.acme.conferencescheduling.service.validation.ConferenceScheduleIssue.DuplicateSpeakerIdIssue;
import org.acme.conferencescheduling.service.validation.ConferenceScheduleIssue.DuplicateTalkIdIssue;
import org.acme.conferencescheduling.service.validation.ConferenceScheduleIssue.DuplicateTimeslotIdIssue;
import org.acme.conferencescheduling.service.validation.ConferenceScheduleIssue.NonExistingRoomReferenceIssue;
import org.acme.conferencescheduling.service.validation.ConferenceScheduleIssue.NonExistingSpeakerReferenceIssue;
import org.acme.conferencescheduling.service.validation.ConferenceScheduleIssue.NonExistingTalkTypeReferenceIssue;
import org.acme.conferencescheduling.service.validation.ConferenceScheduleIssue.NonExistingTimeslotReferenceIssue;

@ApplicationScoped
public class ConferenceScheduleValidator
        implements ModelValidator<ConferenceScheduleInput, ConferenceScheduleConfigOverrides> {

    @Override
    public void validate(ValidationBuilder validationBuilder, ConferenceScheduleInput modelInput,
            ModelConfig<ConferenceScheduleConfigOverrides> modelConfig) {
        // OpenAPI spec (Bean Validation) compliance is enforced by the Service module at the REST layer,
        // before this validator ever runs; only domain-specific checks (duplicate and dangling references)
        // belong here.
        Set<String> talkTypeNames = collectTalkTypeNames(orEmpty(modelInput.talkTypes()));
        Set<String> timeslotIds = validateTimeslots(validationBuilder, orEmpty(modelInput.timeslots()));
        Set<String> roomIds = validateRooms(validationBuilder, orEmpty(modelInput.rooms()));
        Set<String> speakerIds = validateSpeakers(validationBuilder, orEmpty(modelInput.speakers()));
        validateTalks(validationBuilder, orEmpty(modelInput.talks()), timeslotIds, roomIds, speakerIds, talkTypeNames);
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }

    private static Set<String> collectTalkTypeNames(List<TalkTypeDTO> talkTypes) {
        Set<String> names = new HashSet<>();
        for (TalkTypeDTO talkType : talkTypes) {
            names.add(talkType.name());
        }
        return names;
    }

    private static Set<String> validateTimeslots(ValidationBuilder validationBuilder, List<TimeslotDTO> timeslots) {
        Set<String> timeslotIds = new HashSet<>();
        for (TimeslotDTO timeslot : timeslots) {
            if (hasId(timeslot.id()) && !timeslotIds.add(timeslot.id())) {
                validationBuilder.addIssue(new DuplicateTimeslotIdIssue(timeslot.id()));
            }
        }
        return timeslotIds;
    }

    private static Set<String> validateRooms(ValidationBuilder validationBuilder, List<RoomDTO> rooms) {
        Set<String> roomIds = new HashSet<>();
        for (RoomDTO room : rooms) {
            if (hasId(room.id()) && !roomIds.add(room.id())) {
                validationBuilder.addIssue(new DuplicateRoomIdIssue(room.id()));
            }
        }
        return roomIds;
    }

    private static Set<String> validateSpeakers(ValidationBuilder validationBuilder, List<SpeakerDTO> speakers) {
        Set<String> speakerIds = new HashSet<>();
        for (SpeakerDTO speaker : speakers) {
            if (hasId(speaker.id()) && !speakerIds.add(speaker.id())) {
                validationBuilder.addIssue(new DuplicateSpeakerIdIssue(speaker.id()));
            }
        }
        return speakerIds;
    }

    private static void validateTalks(ValidationBuilder validationBuilder, List<TalkDTO> talks, Set<String> timeslotIds,
            Set<String> roomIds, Set<String> speakerIds, Set<String> talkTypeNames) {
        Set<String> talkCodes = new HashSet<>();
        for (TalkDTO talk : talks) {
            // A talk without a code cannot be pointed at, so its other issues are reported without a talk ID.
            String talkId = hasId(talk.code()) ? talk.code() : null;
            if (talkId != null && !talkCodes.add(talkId)) {
                validationBuilder.addIssue(new DuplicateTalkIdIssue(talkId));
            }
            if (talk.timeslotId() != null && !timeslotIds.contains(talk.timeslotId())) {
                validationBuilder.addIssue(new NonExistingTimeslotReferenceIssue(talkId));
            }
            if (talk.roomId() != null && !roomIds.contains(talk.roomId())) {
                validationBuilder.addIssue(new NonExistingRoomReferenceIssue(talkId));
            }
            // A null or blank talk type name is just as invalid as an unknown one: the model requires a talk type.
            if (!hasId(talk.talkTypeName()) || !talkTypeNames.contains(talk.talkTypeName())) {
                validationBuilder.addIssue(new NonExistingTalkTypeReferenceIssue(talkId));
            }
            for (String speakerId : talk.speakerIds()) {
                if (!speakerIds.contains(speakerId)) {
                    validationBuilder.addIssue(new NonExistingSpeakerReferenceIssue(talkId));
                }
            }
        }
    }

    private static boolean hasId(String id) {
        return id != null && !id.isBlank();
    }
}
