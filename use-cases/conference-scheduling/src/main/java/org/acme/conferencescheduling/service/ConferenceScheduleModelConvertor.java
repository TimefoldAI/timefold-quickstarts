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
import org.acme.conferencescheduling.dto.ConferenceScheduleConfigOverrides;
import org.acme.conferencescheduling.dto.ConferenceScheduleInput;
import org.acme.conferencescheduling.dto.ConferenceScheduleOutput;
import org.acme.conferencescheduling.dto.RoomDTO;
import org.acme.conferencescheduling.dto.SpeakerDTO;
import org.acme.conferencescheduling.dto.TalkDTO;
import org.acme.conferencescheduling.dto.TalkTypeDTO;
import org.acme.conferencescheduling.dto.TimeslotDTO;

@ApplicationScoped
public class ConferenceScheduleModelConvertor
        implements
        ModelConvertor<HardMediumSoftScore, ConferenceScheduleInput, ConferenceScheduleConfigOverrides, ConferenceSchedule, ConferenceScheduleOutput> {

    @Override
    public ConferenceScheduleInput applyOutputToInput(ConferenceScheduleInput modelInput,
            ConferenceScheduleOutput modelOutput) {
        Map<String, TalkDTO> outputTalks = modelOutput.talks().stream()
                .collect(Collectors.toMap(TalkDTO::code, talk -> talk));
        List<TalkDTO> updatedTalks = modelInput.talks().stream()
                .map(talk -> {
                    TalkDTO solved = outputTalks.get(talk.code());
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
                    Timeslot timeslot = new Timeslot(dto.id(), LocalDateTime.parse(dto.startDateTime()),
                            LocalDateTime.parse(dto.endDateTime()), relevantTalkTypes,
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
        return Speaker.builder(dto.id())
                .name(dto.name())
                .unavailableTimeslots(timeslotsByIds(dto.unavailableTimeslotIds(), timeslotMap))
                .requiredTimeslotTags(new LinkedHashSet<>(dto.requiredTimeslotTags()))
                .preferredTimeslotTags(new LinkedHashSet<>(dto.preferredTimeslotTags()))
                .prohibitedTimeslotTags(new LinkedHashSet<>(dto.prohibitedTimeslotTags()))
                .undesiredTimeslotTags(new LinkedHashSet<>(dto.undesiredTimeslotTags()))
                .requiredRoomTags(new LinkedHashSet<>(dto.requiredRoomTags()))
                .preferredRoomTags(new LinkedHashSet<>(dto.preferredRoomTags()))
                .prohibitedRoomTags(new LinkedHashSet<>(dto.prohibitedRoomTags()))
                .undesiredRoomTags(new LinkedHashSet<>(dto.undesiredRoomTags()))
                .build();
    }

    private static Talk toTalk(TalkDTO dto, Map<String, TalkType> talkTypeMap, Map<String, Speaker> speakerMap,
            Map<String, Timeslot> timeslotMap, Map<String, Room> roomMap) {
        List<Speaker> speakers = dto.speakerIds().stream()
                .map(speakerId -> require(speakerMap, speakerId, "speaker"))
                .collect(Collectors.toList());
        Talk talk = Talk.builder(dto.code())
                .title(dto.title())
                .talkType(require(talkTypeMap, dto.talkTypeName(), "talk type"))
                .speakers(speakers)
                .themeTrackTags(new LinkedHashSet<>(dto.themeTrackTags()))
                .sectorTags(new LinkedHashSet<>(dto.sectorTags()))
                .audienceTypes(new LinkedHashSet<>(dto.audienceTypes()))
                .audienceLevel(dto.audienceLevel())
                .contentTags(new LinkedHashSet<>(dto.contentTags()))
                .language(dto.language())
                .requiredTimeslotTags(new LinkedHashSet<>(dto.requiredTimeslotTags()))
                .preferredTimeslotTags(new LinkedHashSet<>(dto.preferredTimeslotTags()))
                .prohibitedTimeslotTags(new LinkedHashSet<>(dto.prohibitedTimeslotTags()))
                .undesiredTimeslotTags(new LinkedHashSet<>(dto.undesiredTimeslotTags()))
                .requiredRoomTags(new LinkedHashSet<>(dto.requiredRoomTags()))
                .preferredRoomTags(new LinkedHashSet<>(dto.preferredRoomTags()))
                .prohibitedRoomTags(new LinkedHashSet<>(dto.prohibitedRoomTags()))
                .undesiredRoomTags(new LinkedHashSet<>(dto.undesiredRoomTags()))
                .mutuallyExclusiveTalksTags(new LinkedHashSet<>(dto.mutuallyExclusiveTalksTags()))
                .favoriteCount(dto.favoriteCount())
                .crowdControlRisk(dto.crowdControlRisk())
                .build();
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
            // (initially empty) set instead of being passed to the builder.
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
        for (TalkDTO solved : lastModelOutput.get().talks()) {
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
        List<TalkTypeDTO> talkTypes = solverModel.getTalkTypes().stream()
                .map(talkType -> new TalkTypeDTO(talkType.name())).collect(Collectors.toList());
        List<TimeslotDTO> timeslots = solverModel.getTimeslots().stream().map(this::toDTO).collect(Collectors.toList());
        List<RoomDTO> rooms = solverModel.getRooms().stream().map(this::toDTO).collect(Collectors.toList());
        List<SpeakerDTO> speakers = solverModel.getSpeakers().stream().map(this::toDTO).collect(Collectors.toList());
        List<TalkDTO> talks = solverModel.getTalks().stream().map(this::toDTO).collect(Collectors.toList());
        String score = solverModel.getScore() == null ? "" : solverModel.getScore().toString();
        return new ConferenceScheduleOutput(solverModel.getName(), talkTypes, timeslots, rooms, speakers, talks, score);
    }

    private TimeslotDTO toDTO(Timeslot timeslot) {
        List<String> talkTypeNames = timeslot.getTalkTypes().stream().map(TalkType::name).collect(Collectors.toList());
        return new TimeslotDTO(timeslot.getId(), timeslot.getStartDateTime().toString(),
                timeslot.getEndDateTime().toString(), talkTypeNames, List.copyOf(timeslot.getTags()));
    }

    private RoomDTO toDTO(Room room) {
        List<String> talkTypeNames = room.talkTypes().stream().map(TalkType::name).collect(Collectors.toList());
        List<String> unavailableTimeslotIds =
                room.unavailableTimeslots().stream().map(Timeslot::getId).collect(Collectors.toList());
        return new RoomDTO(room.id(), room.name(), room.capacity(), talkTypeNames, unavailableTimeslotIds,
                List.copyOf(room.tags()));
    }

    private SpeakerDTO toDTO(Speaker speaker) {
        List<String> unavailableTimeslotIds =
                speaker.unavailableTimeslots().stream().map(Timeslot::getId).collect(Collectors.toList());
        return new SpeakerDTO(speaker.id(), speaker.name(), unavailableTimeslotIds,
                List.copyOf(speaker.requiredTimeslotTags()), List.copyOf(speaker.preferredTimeslotTags()),
                List.copyOf(speaker.prohibitedTimeslotTags()), List.copyOf(speaker.undesiredTimeslotTags()),
                List.copyOf(speaker.requiredRoomTags()), List.copyOf(speaker.preferredRoomTags()),
                List.copyOf(speaker.prohibitedRoomTags()), List.copyOf(speaker.undesiredRoomTags()));
    }

    private TalkDTO toDTO(Talk talk) {
        List<String> speakerIds = talk.getSpeakers().stream().map(Speaker::id).collect(Collectors.toList());
        List<String> prerequisiteCodes =
                talk.getPrerequisiteTalks().stream().map(Talk::getCode).collect(Collectors.toList());
        String timeslotId = talk.getTimeslot() == null ? null : talk.getTimeslot().getId();
        String roomId = talk.getRoom() == null ? null : talk.getRoom().id();
        return new TalkDTO(talk.getCode(), talk.getTitle(), talk.getTalkType().name(), speakerIds,
                List.copyOf(talk.getThemeTrackTags()), List.copyOf(talk.getSectorTags()),
                List.copyOf(talk.getAudienceTypes()), talk.getAudienceLevel(), List.copyOf(talk.getContentTags()),
                talk.getLanguage(), List.copyOf(talk.getRequiredTimeslotTags()),
                List.copyOf(talk.getPreferredTimeslotTags()), List.copyOf(talk.getProhibitedTimeslotTags()),
                List.copyOf(talk.getUndesiredTimeslotTags()), List.copyOf(talk.getRequiredRoomTags()),
                List.copyOf(talk.getPreferredRoomTags()), List.copyOf(talk.getProhibitedRoomTags()),
                List.copyOf(talk.getUndesiredRoomTags()), List.copyOf(talk.getMutuallyExclusiveTalksTags()),
                prerequisiteCodes, talk.getFavoriteCount(), talk.getCrowdControlRisk(), timeslotId, roomId);
    }
}
