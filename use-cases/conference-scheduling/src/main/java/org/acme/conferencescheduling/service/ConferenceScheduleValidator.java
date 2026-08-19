package org.acme.conferencescheduling.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.ModelValidator;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;

import org.acme.conferencescheduling.dto.ConferenceScheduleConfigOverrides;
import org.acme.conferencescheduling.dto.ConferenceScheduleInput;
import org.acme.conferencescheduling.dto.RoomDTO;
import org.acme.conferencescheduling.dto.SpeakerDTO;
import org.acme.conferencescheduling.dto.TalkDTO;
import org.acme.conferencescheduling.dto.TalkTypeDTO;
import org.acme.conferencescheduling.dto.TimeslotDTO;
import org.acme.conferencescheduling.service.validation.DuplicateRoomIdIssue;
import org.acme.conferencescheduling.service.validation.DuplicateSpeakerIdIssue;
import org.acme.conferencescheduling.service.validation.DuplicateTalkIdIssue;
import org.acme.conferencescheduling.service.validation.DuplicateTimeslotIdIssue;
import org.acme.conferencescheduling.service.validation.NonExistingRoomReferenceIssue;
import org.acme.conferencescheduling.service.validation.NonExistingSpeakerReferenceIssue;
import org.acme.conferencescheduling.service.validation.NonExistingTalkTypeReferenceIssue;
import org.acme.conferencescheduling.service.validation.NonExistingTimeslotReferenceIssue;
import org.acme.conferencescheduling.service.validation.RoomIdMissingIssue;
import org.acme.conferencescheduling.service.validation.SpeakerIdMissingIssue;
import org.acme.conferencescheduling.service.validation.TalkIdMissingIssue;
import org.acme.conferencescheduling.service.validation.TimeslotIdMissingIssue;

@ApplicationScoped
public class ConferenceScheduleValidator
        implements ModelValidator<ConferenceScheduleInput, ConferenceScheduleConfigOverrides> {

    @Override
    public void validate(ValidationBuilder validationBuilder, ConferenceScheduleInput modelInput,
            ModelConfig<ConferenceScheduleConfigOverrides> modelConfig) {
        Set<String> talkTypeNames = validateTalkTypes(modelInput.talkTypes());
        Set<String> timeslotIds = validateTimeslots(validationBuilder, modelInput.timeslots());
        Set<String> roomIds = validateRooms(validationBuilder, modelInput.rooms());
        Set<String> speakerIds = validateSpeakers(validationBuilder, modelInput.speakers());
        validateTalks(validationBuilder, modelInput.talks(), timeslotIds, roomIds, speakerIds, talkTypeNames);
    }

    private static Set<String> validateTalkTypes(List<TalkTypeDTO> talkTypes) {
        Set<String> names = new HashSet<>();
        for (TalkTypeDTO talkType : talkTypes) {
            names.add(talkType.name());
        }
        return names;
    }

    private static Set<String> validateTimeslots(ValidationBuilder validationBuilder, List<TimeslotDTO> timeslots) {
        Set<String> timeslotIds = new HashSet<>();
        for (TimeslotDTO timeslot : timeslots) {
            if (timeslot.id() == null || timeslot.id().isBlank()) {
                validationBuilder.addIssue(new TimeslotIdMissingIssue());
            } else if (!timeslotIds.add(timeslot.id())) {
                validationBuilder.addIssue(new DuplicateTimeslotIdIssue(timeslot.id()));
            }
        }
        return timeslotIds;
    }

    private static Set<String> validateRooms(ValidationBuilder validationBuilder, List<RoomDTO> rooms) {
        Set<String> roomIds = new HashSet<>();
        for (RoomDTO room : rooms) {
            if (room.id() == null || room.id().isBlank()) {
                validationBuilder.addIssue(new RoomIdMissingIssue());
            } else if (!roomIds.add(room.id())) {
                validationBuilder.addIssue(new DuplicateRoomIdIssue(room.id()));
            }
        }
        return roomIds;
    }

    private static Set<String> validateSpeakers(ValidationBuilder validationBuilder, List<SpeakerDTO> speakers) {
        Set<String> speakerIds = new HashSet<>();
        for (SpeakerDTO speaker : speakers) {
            if (speaker.id() == null || speaker.id().isBlank()) {
                validationBuilder.addIssue(new SpeakerIdMissingIssue());
            } else if (!speakerIds.add(speaker.id())) {
                validationBuilder.addIssue(new DuplicateSpeakerIdIssue(speaker.id()));
            }
        }
        return speakerIds;
    }

    private static void validateTalks(ValidationBuilder validationBuilder, List<TalkDTO> talks, Set<String> timeslotIds,
            Set<String> roomIds, Set<String> speakerIds, Set<String> talkTypeNames) {
        Set<String> talkCodes = new HashSet<>();
        for (TalkDTO talk : talks) {
            String talkCode = talk.code();
            // A talk without a code cannot be pointed at, so its other issues are reported without a talk ID.
            String talkId = talkCode == null || talkCode.isBlank() ? null : talkCode;
            if (talkId == null) {
                validationBuilder.addIssue(new TalkIdMissingIssue());
            } else if (!talkCodes.add(talkId)) {
                validationBuilder.addIssue(new DuplicateTalkIdIssue(talkId));
            }
            if (talk.timeslotId() != null && !timeslotIds.contains(talk.timeslotId())) {
                validationBuilder.addIssue(new NonExistingTimeslotReferenceIssue(talkId));
            }
            if (talk.roomId() != null && !roomIds.contains(talk.roomId())) {
                validationBuilder.addIssue(new NonExistingRoomReferenceIssue(talkId));
            }
            // A null or blank talk type name is just as invalid as an unknown one: the model requires a talk type.
            if (talk.talkTypeName() == null || talk.talkTypeName().isBlank()
                    || !talkTypeNames.contains(talk.talkTypeName())) {
                validationBuilder.addIssue(new NonExistingTalkTypeReferenceIssue(talkId));
            }
            for (String speakerId : talk.speakerIds()) {
                if (!speakerIds.contains(speakerId)) {
                    validationBuilder.addIssue(new NonExistingSpeakerReferenceIssue(talkId));
                }
            }
        }
    }
}
