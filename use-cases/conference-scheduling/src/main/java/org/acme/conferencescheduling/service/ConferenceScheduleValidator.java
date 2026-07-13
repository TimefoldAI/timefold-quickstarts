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
import org.acme.conferencescheduling.dto.RoomIdDetail;
import org.acme.conferencescheduling.dto.SpeakerDTO;
import org.acme.conferencescheduling.dto.SpeakerIdDetail;
import org.acme.conferencescheduling.dto.TalkDTO;
import org.acme.conferencescheduling.dto.TalkIdDetail;
import org.acme.conferencescheduling.dto.TalkTypeDTO;
import org.acme.conferencescheduling.dto.TimeslotDTO;
import org.acme.conferencescheduling.dto.TimeslotIdDetail;
import org.acme.conferencescheduling.service.ConferenceScheduleIssues.DuplicateRoomIdIssue;
import org.acme.conferencescheduling.service.ConferenceScheduleIssues.DuplicateSpeakerIdIssue;
import org.acme.conferencescheduling.service.ConferenceScheduleIssues.DuplicateTalkIdIssue;
import org.acme.conferencescheduling.service.ConferenceScheduleIssues.DuplicateTimeslotIdIssue;
import org.acme.conferencescheduling.service.ConferenceScheduleIssues.NonExistingRoomReferenceIssue;
import org.acme.conferencescheduling.service.ConferenceScheduleIssues.NonExistingSpeakerReferenceIssue;
import org.acme.conferencescheduling.service.ConferenceScheduleIssues.NonExistingTalkTypeReferenceIssue;
import org.acme.conferencescheduling.service.ConferenceScheduleIssues.NonExistingTimeslotReferenceIssue;
import org.acme.conferencescheduling.service.ConferenceScheduleIssues.RoomIdMissingIssue;
import org.acme.conferencescheduling.service.ConferenceScheduleIssues.SpeakerIdMissingIssue;
import org.acme.conferencescheduling.service.ConferenceScheduleIssues.TalkIdMissingIssue;
import org.acme.conferencescheduling.service.ConferenceScheduleIssues.TimeslotIdMissingIssue;

@ApplicationScoped
@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
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
                validationBuilder.addIssue(new DuplicateTimeslotIdIssue(new TimeslotIdDetail(timeslot.id())));
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
                validationBuilder.addIssue(new DuplicateRoomIdIssue(new RoomIdDetail(room.id())));
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
                validationBuilder.addIssue(new DuplicateSpeakerIdIssue(new SpeakerIdDetail(speaker.id())));
            }
        }
        return speakerIds;
    }

    private static void validateTalks(ValidationBuilder validationBuilder, List<TalkDTO> talks, Set<String> timeslotIds,
            Set<String> roomIds, Set<String> speakerIds, Set<String> talkTypeNames) {
        Set<String> talkCodes = new HashSet<>();
        for (TalkDTO talk : talks) {
            if (talk.code() == null || talk.code().isBlank()) {
                validationBuilder.addIssue(new TalkIdMissingIssue());
            } else if (!talkCodes.add(talk.code())) {
                validationBuilder.addIssue(new DuplicateTalkIdIssue(new TalkIdDetail(talk.code())));
            }
            if (talk.timeslotId() != null && !timeslotIds.contains(talk.timeslotId())) {
                validationBuilder.addIssue(new NonExistingTimeslotReferenceIssue(new TalkIdDetail(talk.code())));
            }
            if (talk.roomId() != null && !roomIds.contains(talk.roomId())) {
                validationBuilder.addIssue(new NonExistingRoomReferenceIssue(new TalkIdDetail(talk.code())));
            }
            // A blank talk type name is just as invalid as an unknown one: the solver model requires a talk type.
            if (talk.talkTypeName().isBlank() || !talkTypeNames.contains(talk.talkTypeName())) {
                validationBuilder.addIssue(new NonExistingTalkTypeReferenceIssue(new TalkIdDetail(talk.code())));
            }
            for (String speakerId : talk.speakerIds()) {
                if (!speakerIds.contains(speakerId)) {
                    validationBuilder.addIssue(new NonExistingSpeakerReferenceIssue(new TalkIdDetail(talk.code())));
                }
            }
        }
    }
}
