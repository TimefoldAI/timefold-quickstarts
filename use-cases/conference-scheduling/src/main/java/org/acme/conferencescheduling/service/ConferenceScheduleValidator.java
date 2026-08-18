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
import org.acme.conferencescheduling.dto.validation.RoomIdDetail;
import org.acme.conferencescheduling.dto.validation.SpeakerIdDetail;
import org.acme.conferencescheduling.dto.validation.TalkIdDetail;
import org.acme.conferencescheduling.dto.validation.TimeslotIdDetail;
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
            String talkCode = talk.code();
            // A talk without a code cannot be pointed at, so its other issues are reported without a talk detail.
            TalkIdDetail talkIdDetail = talkCode == null || talkCode.isBlank() ? null : new TalkIdDetail(talkCode);
            if (talkIdDetail == null) {
                validationBuilder.addIssue(new TalkIdMissingIssue());
            } else if (!talkCodes.add(talkCode)) {
                validationBuilder.addIssue(new DuplicateTalkIdIssue(talkIdDetail));
            }
            if (talk.timeslotId() != null && !timeslotIds.contains(talk.timeslotId())) {
                validationBuilder.addIssue(talkIdDetail == null
                        ? new NonExistingTimeslotReferenceIssue()
                        : new NonExistingTimeslotReferenceIssue(talkIdDetail));
            }
            if (talk.roomId() != null && !roomIds.contains(talk.roomId())) {
                validationBuilder.addIssue(talkIdDetail == null
                        ? new NonExistingRoomReferenceIssue()
                        : new NonExistingRoomReferenceIssue(talkIdDetail));
            }
            // A null or blank talk type name is just as invalid as an unknown one: the model requires a talk type.
            if (talk.talkTypeName() == null || talk.talkTypeName().isBlank()
                    || !talkTypeNames.contains(talk.talkTypeName())) {
                validationBuilder.addIssue(talkIdDetail == null
                        ? new NonExistingTalkTypeReferenceIssue()
                        : new NonExistingTalkTypeReferenceIssue(talkIdDetail));
            }
            for (String speakerId : talk.speakerIds()) {
                if (!speakerIds.contains(speakerId)) {
                    validationBuilder.addIssue(talkIdDetail == null
                            ? new NonExistingSpeakerReferenceIssue()
                            : new NonExistingSpeakerReferenceIssue(talkIdDetail));
                }
            }
        }
    }
}
