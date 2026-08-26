package org.acme.conferencescheduling.service;

import static java.util.stream.Collectors.toCollection;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SequencedSet;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.core.api.domain.solution.ConstraintWeightOverrides;
import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.service.definition.api.ModelConvertor;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;

import org.acme.conferencescheduling.domain.ConferenceConstraintProperties;
import org.acme.conferencescheduling.domain.ConferenceSchedule;
import org.acme.conferencescheduling.domain.Room;
import org.acme.conferencescheduling.domain.Speaker;
import org.acme.conferencescheduling.domain.Talk;
import org.acme.conferencescheduling.domain.TalkType;
import org.acme.conferencescheduling.domain.Timeslot;
import org.acme.conferencescheduling.dto.input.ConferenceScheduleConfigOverrides;
import org.acme.conferencescheduling.dto.input.ConferenceScheduleInput;
import org.acme.conferencescheduling.dto.input.SpeakerDTO;
import org.acme.conferencescheduling.dto.input.TalkDTO;
import org.acme.conferencescheduling.dto.input.TalkTypeDTO;
import org.acme.conferencescheduling.dto.output.ConferenceScheduleOutput;
import org.acme.conferencescheduling.dto.output.TalkAssignmentDTO;

@ApplicationScoped
public class ConferenceScheduleModelConvertor
        implements
        ModelConvertor<HardMediumSoftScore, ConferenceScheduleInput, ConferenceScheduleConfigOverrides, ConferenceSchedule, ConferenceScheduleOutput> {

    @Override
    public ConferenceScheduleInput applyOutputToInput(ConferenceScheduleInput modelInput,
            ConferenceScheduleOutput modelOutput) {
        Map<String, TalkAssignmentDTO> outputTalks = modelOutput.talks().stream()
                .collect(Collectors.toMap(TalkAssignmentDTO::code, talk -> talk));
        List<TalkDTO> updatedTalks = modelInput.talks().stream()
                .map(talk -> {
                    TalkAssignmentDTO solved = outputTalks.get(talk.code());
                    if (solved == null) {
                        return talk;
                    }
                    return talk.withTimeslotId(solved.timeslotId()).withRoomId(solved.roomId());
                })
                .collect(Collectors.toList());
        return modelInput.withTalks(updatedTalks);
    }

    @Override
    public ConferenceSchedule toSolverModel(ConferenceScheduleInput modelInput,
            ModelConfig<ConferenceScheduleConfigOverrides> modelConfig,
            Optional<ConferenceScheduleOutput> lastModelOutput) {
        Map<String, TalkType> talkTypeMap = modelInput.talkTypes().stream()
                .collect(Collectors.toMap(TalkTypeDTO::name, dto -> {
                    return new TalkType(dto.name(), new LinkedHashSet<>(), new LinkedHashSet<>());
                }, (a, b) -> a, java.util.LinkedHashMap::new));

        Map<String, Timeslot> timeslotMap = new java.util.LinkedHashMap<>();
        Set<Timeslot> timeslots = modelInput.timeslots().stream()
                .map(dto -> {
                    var relevantTalkTypes = talkTypes(dto.talkTypeNames(), talkTypeMap);
                    Timeslot timeslot = new Timeslot(dto.id(), dto.startDateTime(), dto.endDateTime(), relevantTalkTypes,
                            new LinkedHashSet<>(dto.tags()));
                    timeslotMap.put(timeslot.getId(), timeslot);
                    relevantTalkTypes.forEach(r -> r.compatibleTimeslots().add(timeslot));
                    return timeslot;
                })
                .collect(toCollection(LinkedHashSet::new));

        Map<String, Room> roomMap = new java.util.LinkedHashMap<>();
        Set<Room> rooms = modelInput.rooms().stream()
                .map(dto -> {
                    var relevantTalkTypes = talkTypes(dto.talkTypeNames(), talkTypeMap);
                    Room room = new Room(dto.id(), dto.name(), dto.capacity(),
                            relevantTalkTypes,
                            timeslotsByIds(dto.unavailableTimeslotIds(), timeslotMap),
                            new LinkedHashSet<>(dto.tags()));
                    roomMap.put(room.id(), room);
                    relevantTalkTypes.forEach(r -> r.compatibleRooms().add(room));
                    return room;
                })
                .collect(toCollection(LinkedHashSet::new));

        Map<String, Speaker> speakerMap = new java.util.LinkedHashMap<>();
        Set<Speaker> speakers = modelInput.speakers().stream()
                .map(dto -> {
                    Speaker speaker = toSpeaker(dto, timeslotMap);
                    speakerMap.put(speaker.id(), speaker);
                    return speaker;
                })
                .collect(toCollection(LinkedHashSet::new));

        Map<String, Talk> talkMap = new java.util.LinkedHashMap<>();
        Set<Talk> talks = modelInput.talks().stream()
                .map(dto -> {
                    Talk talk = toTalk(dto, talkTypeMap, speakerMap, timeslotMap, roomMap);
                    talkMap.put(talk.getCode(), talk);
                    return talk;
                })
                .collect(toCollection(LinkedHashSet::new));
        applyPrerequisites(modelInput.talks(), talkMap);

        ConferenceSchedule schedule = new ConferenceSchedule(modelInput.name(),
                new LinkedHashSet<>(talkTypeMap.values()), timeslots, rooms, speakers, talks);
        schedule.setConstraintProperties(new ConferenceConstraintProperties());
        applyConstraintWeightOverrides(schedule, modelConfig);
        applyLastOutput(talkMap, timeslotMap, roomMap, lastModelOutput);
        return schedule;
    }

    private static Set<TalkType> talkTypes(List<String> names, Map<String, TalkType> talkTypeMap) {
        return names.stream().map(name -> require(talkTypeMap, name, "talk type"))
                .collect(toCollection(LinkedHashSet::new));
    }

    private static SequencedSet<Timeslot> timeslotsByIds(List<String> ids, Map<String, Timeslot> timeslotMap) {
        return ids.stream().map(id -> require(timeslotMap, id, "timeslot"))
                .collect(toCollection(LinkedHashSet::new));
    }

    /**
     * Fails fast with an actionable message instead of letting an unknown reference
     * turn into a null in the solver model and a delayed NullPointerException.
     */
    private static <T> T require(Map<String, T> map, String key, String kind) {
        T value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Unknown %s '%s'.".formatted(kind, key));
        }
        return value;
    }

    private static Speaker toSpeaker(SpeakerDTO dto, Map<String, Timeslot> timeslotMap) {
        return new Speaker(dto.id(),
                dto.name(),
                timeslotsByIds(dto.unavailableTimeslotIds(), timeslotMap),
                new LinkedHashSet<>(dto.requiredTimeslotTags()),
                new LinkedHashSet<>(dto.preferredTimeslotTags()),
                new LinkedHashSet<>(dto.prohibitedTimeslotTags()),
                new LinkedHashSet<>(dto.undesiredTimeslotTags()),
                new LinkedHashSet<>(dto.requiredRoomTags()),
                new LinkedHashSet<>(dto.preferredRoomTags()),
                new LinkedHashSet<>(dto.prohibitedRoomTags()),
                new LinkedHashSet<>(dto.undesiredRoomTags()));
    }

    private static Talk toTalk(TalkDTO dto, Map<String, TalkType> talkTypeMap, Map<String, Speaker> speakerMap,
            Map<String, Timeslot> timeslotMap, Map<String, Room> roomMap) {
        List<Speaker> speakers = dto.speakerIds().stream()
                .map(speakerId -> require(speakerMap, speakerId, "speaker"))
                .collect(Collectors.toList());
        // Prerequisites are resolved in applyPrerequisites(), once every talk exists, so an empty set is passed here.
        Talk talk = new Talk(dto.code(),
                dto.title(),
                require(talkTypeMap, dto.talkTypeName(), "talk type"),
                speakers,
                new LinkedHashSet<>(dto.themeTrackTags()),
                new LinkedHashSet<>(dto.sectorTags()),
                new LinkedHashSet<>(dto.audienceTypes()),
                dto.audienceLevel(),
                new LinkedHashSet<>(dto.contentTags()),
                dto.language(),
                new LinkedHashSet<>(dto.requiredTimeslotTags()),
                new LinkedHashSet<>(dto.preferredTimeslotTags()),
                new LinkedHashSet<>(dto.prohibitedTimeslotTags()),
                new LinkedHashSet<>(dto.undesiredTimeslotTags()),
                new LinkedHashSet<>(dto.requiredRoomTags()),
                new LinkedHashSet<>(dto.preferredRoomTags()),
                new LinkedHashSet<>(dto.prohibitedRoomTags()),
                new LinkedHashSet<>(dto.undesiredRoomTags()),
                new LinkedHashSet<>(dto.mutuallyExclusiveTalksTags()),
                new LinkedHashSet<>(),
                dto.favoriteCount(),
                dto.crowdControlRisk());
        if (dto.timeslotId() != null) {
            talk.setTimeslot(require(timeslotMap, dto.timeslotId(), "timeslot"));
        }
        if (dto.roomId() != null) {
            talk.setRoom(require(roomMap, dto.roomId(), "room"));
        }
        return talk;
    }

    private static void applyPrerequisites(List<TalkDTO> talkDtos, Map<String, Talk> talkMap) {
        for (TalkDTO dto : talkDtos) {
            if (dto.prerequisiteTalkCodes().isEmpty()) {
                continue;
            }
            SequencedSet<Talk> prerequisites = dto.prerequisiteTalkCodes().stream()
                    .map(code -> require(talkMap, code, "prerequisite talk"))
                    .collect(toCollection(LinkedHashSet::new));
            // Prerequisites can only be resolved once every talk exists, so they are added to the talk's own
            // (initially empty) set instead of being passed to the constructor.
            talkMap.get(dto.code()).getPrerequisiteTalks().addAll(prerequisites);
        }
    }

    private static void applyConstraintWeightOverrides(ConferenceSchedule schedule,
            ModelConfig<ConferenceScheduleConfigOverrides> modelConfig) {
        if (modelConfig == null || modelConfig.overrides() == null) {
            return;
        }
        ConferenceScheduleConfigOverrides overrides = modelConfig.overrides();
        // Only apply weights that are actually set (non-null) in the merged overrides. A null weight means the
        // input did not override it, so the configuration profile value (or the constraint's default) is kept.
        Map<String, HardMediumSoftScore> weights = new HashMap<>();
        putIfPresent(weights, ConferenceConstraintProperties.THEME_TRACK_CONFLICT, overrides.themeTrackConflictWeight());
        putIfPresent(weights, ConferenceConstraintProperties.THEME_TRACK_ROOM_STABILITY,
                overrides.themeTrackRoomStabilityWeight());
        putIfPresent(weights, ConferenceConstraintProperties.SECTOR_CONFLICT, overrides.sectorConflictWeight());
        putIfPresent(weights, ConferenceConstraintProperties.AUDIENCE_TYPE_DIVERSITY,
                overrides.audienceTypeDiversityWeight());
        putIfPresent(weights, ConferenceConstraintProperties.AUDIENCE_TYPE_THEME_TRACK_CONFLICT,
                overrides.audienceTypeThemeTrackConflictWeight());
        putIfPresent(weights, ConferenceConstraintProperties.AUDIENCE_LEVEL_DIVERSITY,
                overrides.audienceLevelDiversityWeight());
        putIfPresent(weights, ConferenceConstraintProperties.CONTENT_AUDIENCE_LEVEL_FLOW_VIOLATION,
                overrides.contentAudienceLevelFlowViolationWeight());
        putIfPresent(weights, ConferenceConstraintProperties.CONTENT_CONFLICT, overrides.contentConflictWeight());
        putIfPresent(weights, ConferenceConstraintProperties.LANGUAGE_DIVERSITY, overrides.languageDiversityWeight());
        putIfPresent(weights, ConferenceConstraintProperties.SAME_DAY_TALKS, overrides.sameDayTalksWeight());
        putIfPresent(weights, ConferenceConstraintProperties.POPULAR_TALKS, overrides.popularTalksWeight());
        putIfPresent(weights, ConferenceConstraintProperties.SPEAKER_PREFERRED_TIMESLOT_TAGS,
                overrides.speakerPreferredTimeslotTagsWeight());
        putIfPresent(weights, ConferenceConstraintProperties.SPEAKER_UNDESIRED_TIMESLOT_TAGS,
                overrides.speakerUndesiredTimeslotTagsWeight());
        putIfPresent(weights, ConferenceConstraintProperties.TALK_PREFERRED_TIMESLOT_TAGS,
                overrides.talkPreferredTimeslotTagsWeight());
        putIfPresent(weights, ConferenceConstraintProperties.TALK_UNDESIRED_TIMESLOT_TAGS,
                overrides.talkUndesiredTimeslotTagsWeight());
        putIfPresent(weights, ConferenceConstraintProperties.SPEAKER_PREFERRED_ROOM_TAGS,
                overrides.speakerPreferredRoomTagsWeight());
        putIfPresent(weights, ConferenceConstraintProperties.SPEAKER_UNDESIRED_ROOM_TAGS,
                overrides.speakerUndesiredRoomTagsWeight());
        putIfPresent(weights, ConferenceConstraintProperties.TALK_PREFERRED_ROOM_TAGS,
                overrides.talkPreferredRoomTagsWeight());
        putIfPresent(weights, ConferenceConstraintProperties.TALK_UNDESIRED_ROOM_TAGS,
                overrides.talkUndesiredRoomTagsWeight());
        putIfPresent(weights, ConferenceConstraintProperties.SPEAKER_MAKESPAN, overrides.speakerMakespanWeight());
        if (!weights.isEmpty()) {
            schedule.setConstraintWeightOverrides(ConstraintWeightOverrides.of(weights));
        }
    }

    private static void putIfPresent(Map<String, HardMediumSoftScore> weights, String constraintName, Long weight) {
        if (weight != null) {
            weights.put(constraintName, HardMediumSoftScore.ofSoft(weight));
        }
    }

    private static void applyLastOutput(Map<String, Talk> talkMap, Map<String, Timeslot> timeslotMap,
            Map<String, Room> roomMap, Optional<ConferenceScheduleOutput> lastModelOutput) {
        if (lastModelOutput.isEmpty()) {
            return;
        }
        for (TalkAssignmentDTO solved : lastModelOutput.get().talks()) {
            Talk talk = talkMap.get(solved.code());
            if (talk == null) {
                continue;
            }
            if (solved.timeslotId() != null) {
                talk.setTimeslot(timeslotMap.get(solved.timeslotId()));
            }
            if (solved.roomId() != null) {
                talk.setRoom(roomMap.get(solved.roomId()));
            }
        }
    }

    @Override
    public ConferenceScheduleOutput toModelOutput(ConferenceSchedule solverModel) {
        List<TalkAssignmentDTO> talks = solverModel.getTalks().stream().map(this::toAssignmentDTO)
                .collect(Collectors.toList());
        return new ConferenceScheduleOutput(talks);
    }

    private TalkAssignmentDTO toAssignmentDTO(Talk talk) {
        String timeslotId = talk.getTimeslot() == null ? null : talk.getTimeslot().getId();
        String roomId = talk.getRoom() == null ? null : talk.getRoom().id();
        return new TalkAssignmentDTO(talk.getCode(), timeslotId, roomId);
    }
}
