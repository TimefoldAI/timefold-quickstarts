package org.acme.conferencescheduling.service;

import static org.acme.conferencescheduling.support.SolverTestDataFactory.LAB;
import static org.acme.conferencescheduling.support.SolverTestDataFactory.SPEAKERS;
import static org.acme.conferencescheduling.support.SolverTestDataFactory.TALK_TYPES;
import static org.acme.conferencescheduling.support.SolverTestDataFactory.TIMESLOTS;
import static org.acme.conferencescheduling.support.SolverTestDataFactory.createProblem;
import static org.acme.conferencescheduling.support.SolverTestDataFactory.input;
import static org.acme.conferencescheduling.support.SolverTestDataFactory.inputWithRooms;
import static org.acme.conferencescheduling.support.SolverTestDataFactory.inputWithSpeakers;
import static org.acme.conferencescheduling.support.SolverTestDataFactory.inputWithTalks;
import static org.acme.conferencescheduling.support.SolverTestDataFactory.inputWithTimeslots;
import static org.acme.conferencescheduling.support.SolverTestDataFactory.room;
import static org.acme.conferencescheduling.support.SolverTestDataFactory.speaker;
import static org.acme.conferencescheduling.support.SolverTestDataFactory.talk;
import static org.acme.conferencescheduling.support.SolverTestDataFactory.timeslot;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.Issue;
import ai.timefold.solver.service.definition.api.validation.IssueCode;
import ai.timefold.solver.service.definition.api.validation.IssueSeverity;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;
import ai.timefold.solver.service.definition.api.validation.ValidationStatus;
import ai.timefold.solver.service.definition.api.validation.dto.ValidationResult;

import org.acme.conferencescheduling.dto.ConferenceScheduleInput;
import org.acme.conferencescheduling.dto.TalkDTO;
import org.acme.conferencescheduling.service.validation.DuplicateRoomIdIssue;
import org.acme.conferencescheduling.service.validation.DuplicateSpeakerIdIssue;
import org.acme.conferencescheduling.service.validation.DuplicateTalkIdIssue;
import org.acme.conferencescheduling.service.validation.DuplicateTimeslotIdIssue;
import org.acme.conferencescheduling.service.validation.NonExistingRoomReferenceIssue;
import org.acme.conferencescheduling.service.validation.NonExistingSpeakerReferenceIssue;
import org.acme.conferencescheduling.service.validation.NonExistingTalkTypeReferenceIssue;
import org.acme.conferencescheduling.service.validation.NonExistingTimeslotReferenceIssue;
import org.junit.jupiter.api.Test;

class ConferenceScheduleValidatorTest {

    private final ConferenceScheduleValidator validator = new ConferenceScheduleValidator();

    @Test
    void validInputHasNoIssues() {
        ValidationResult<Issue> result = validate(inputWithTalks(talk("T1", "s1"), talk("T2", "s1", "s2")));

        assertThat(result.issues()).isEmpty();
        assertThat(result.status()).isEqualTo(ValidationStatus.OK);
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void emptyInputHasNoIssues() {
        ValidationResult<Issue> result = validate(input(List.of(), List.of(), List.of(), List.of(), List.of()));

        assertThat(result.issues()).isEmpty();
        assertThat(result.isValid()).isTrue();
    }

    // ------------------------------------------------------------------------
    // Timeslots
    // ------------------------------------------------------------------------

    @Test
    void nullTimeslotIdIsReportedAsMissing() {
        ValidationResult<Issue> result = validate(inputWithTimeslots(timeslot(null)));

        Issue issue = singleIssue(result, "TIMESLOT_ID_MISSING");
        assertThat(issue.getSeverity()).isEqualTo(IssueSeverity.ERROR);
        assertThat(result.status()).isEqualTo(ValidationStatus.ERRORS);
        assertThat(result.isValid()).isFalse();
    }

    @Test
    void blankTimeslotIdIsReportedAsMissing() {
        ValidationResult<Issue> result = validate(inputWithTimeslots(timeslot("  ")));

        singleIssue(result, "TIMESLOT_ID_MISSING");
    }

    @Test
    void duplicateTimeslotIdIsReportedWithTheOffendingId() {
        ValidationResult<Issue> result = validate(inputWithTimeslots(timeslot("ts1"), timeslot("ts1")));

        DuplicateTimeslotIdIssue issue = singleIssue(result, DuplicateTimeslotIdIssue.class);
        assertThat(issue.getTimeslotId()).isEqualTo("ts1");
    }

    // ------------------------------------------------------------------------
    // Rooms
    // ------------------------------------------------------------------------

    @Test
    void nullRoomIdIsReportedAsMissing() {
        ValidationResult<Issue> result = validate(inputWithRooms(room(null)));

        singleIssue(result, "ROOM_ID_MISSING");
    }

    @Test
    void blankRoomIdIsReportedAsMissing() {
        ValidationResult<Issue> result = validate(inputWithRooms(room("  ")));

        singleIssue(result, "ROOM_ID_MISSING");
    }

    @Test
    void duplicateRoomIdIsReportedWithTheOffendingId() {
        ValidationResult<Issue> result = validate(inputWithRooms(room("r1"), room("r1")));

        DuplicateRoomIdIssue issue = singleIssue(result, DuplicateRoomIdIssue.class);
        assertThat(issue.getRoomId()).isEqualTo("r1");
    }

    // ------------------------------------------------------------------------
    // Speakers
    // ------------------------------------------------------------------------

    @Test
    void blankSpeakerIdIsReportedAsMissing() {
        ValidationResult<Issue> result = validate(inputWithSpeakers(speaker("  ")));

        singleIssue(result, "SPEAKER_ID_MISSING");
    }

    @Test
    void duplicateSpeakerIdIsReportedWithTheOffendingId() {
        ValidationResult<Issue> result = validate(inputWithSpeakers(speaker("s1"), speaker("s1")));

        DuplicateSpeakerIdIssue issue = singleIssue(result, DuplicateSpeakerIdIssue.class);
        assertThat(issue.getSpeakerId()).isEqualTo("s1");
    }

    // ------------------------------------------------------------------------
    // Talks: identity
    // ------------------------------------------------------------------------

    @Test
    void nullTalkCodeIsReportedAsMissing() {
        ValidationResult<Issue> result = validate(inputWithTalks(talk(null, "s1")));

        singleIssue(result, "TALK_ID_MISSING");
    }

    @Test
    void blankTalkCodeIsReportedAsMissing() {
        ValidationResult<Issue> result = validate(inputWithTalks(talk("  ", "s1")));

        singleIssue(result, "TALK_ID_MISSING");
    }

    @Test
    void duplicateTalkCodeIsReportedWithTheOffendingCode() {
        ValidationResult<Issue> result = validate(inputWithTalks(talk("T1", "s1"), talk("T1", "s2")));

        DuplicateTalkIdIssue issue = singleIssue(result, DuplicateTalkIdIssue.class);
        assertThat(issue.getTalkId()).isEqualTo("T1");
    }

    // ------------------------------------------------------------------------
    // Talks: references
    // ------------------------------------------------------------------------

    @Test
    void talkReferencingNonExistingTimeslotIsReported() {
        ValidationResult<Issue> result = validate(inputWithTalks(talk("T1", "s1").withTimeslotId("does-not-exist")));

        NonExistingTimeslotReferenceIssue issue = singleIssue(result, NonExistingTimeslotReferenceIssue.class);
        assertThat(issue.getTalkId()).isEqualTo("T1");
    }

    @Test
    void talkReferencingNonExistingRoomIsReported() {
        ValidationResult<Issue> result = validate(inputWithTalks(talk("T1", "s1").withRoomId("does-not-exist")));

        NonExistingRoomReferenceIssue issue = singleIssue(result, NonExistingRoomReferenceIssue.class);
        assertThat(issue.getTalkId()).isEqualTo("T1");
    }

    @Test
    void talkReferencingExistingTimeslotAndRoomIsAccepted() {
        ValidationResult<Issue> result = validate(inputWithTalks(talk("T1", "s1").withTimeslotId("ts1").withRoomId("r1")));

        assertThat(result.issues()).isEmpty();
    }

    @Test
    void unassignedTalkIsAccepted() {
        // TalkDTO normalizes a blank timeslot/room ID to null, which means "unassigned", not "unknown reference".
        ValidationResult<Issue> result = validate(inputWithTalks(talk("T1", "s1").withTimeslotId("").withRoomId("")));

        assertThat(result.issues()).isEmpty();
    }

    @Test
    void talkReferencingNonExistingSpeakerIsReported() {
        ValidationResult<Issue> result = validate(inputWithTalks(talk("T1", "unknown-speaker")));

        NonExistingSpeakerReferenceIssue issue = singleIssue(result, NonExistingSpeakerReferenceIssue.class);
        assertThat(issue.getTalkId()).isEqualTo("T1");
    }

    @Test
    void everyNonExistingSpeakerOfATalkIsReportedSeparately() {
        ValidationResult<Issue> result = validate(inputWithTalks(talk("T1", "s1", "unknown-1", "unknown-2")));

        assertThat(codesOf(result)).containsExactly("NON_EXISTING_SPEAKER_REFERENCE", "NON_EXISTING_SPEAKER_REFERENCE");
    }

    @Test
    void talkWithoutSpeakersIsAccepted() {
        ValidationResult<Issue> result = validate(inputWithTalks(talk("T1")));

        assertThat(result.issues()).isEmpty();
    }

    // ------------------------------------------------------------------------
    // Talks: talk type
    // ------------------------------------------------------------------------

    @Test
    void talkReferencingNonExistingTalkTypeIsReported() {
        ValidationResult<Issue> result = validate(inputWithTalks(
                TalkDTO.builder("T1", "Title", "Keynote").speakerIds(List.of("s1")).build()));

        NonExistingTalkTypeReferenceIssue issue = singleIssue(result, NonExistingTalkTypeReferenceIssue.class);
        assertThat(issue.getTalkId()).isEqualTo("T1");
    }

    @Test
    void talkWithNullTalkTypeIsReported() {
        ValidationResult<Issue> result = validate(inputWithTalks(
                TalkDTO.builder("T1", "Title", null).speakerIds(List.of("s1")).build()));

        NonExistingTalkTypeReferenceIssue issue = singleIssue(result, NonExistingTalkTypeReferenceIssue.class);
        assertThat(issue.getTalkId()).isEqualTo("T1");
    }

    @Test
    void talkWithBlankTalkTypeIsReported() {
        ValidationResult<Issue> result = validate(inputWithTalks(
                TalkDTO.builder("T1", "Title", "  ").speakerIds(List.of("s1")).build()));

        singleIssue(result, "NON_EXISTING_TALK_TYPE_REFERENCE");
    }

    @Test
    void talkOfAnyDeclaredTalkTypeIsAccepted() {
        ValidationResult<Issue> result = validate(inputWithTalks(
                TalkDTO.builder("T1", "Title", LAB).speakerIds(List.of("s1")).build()));

        assertThat(result.issues()).isEmpty();
    }

    // ------------------------------------------------------------------------
    // Accumulation across entity types
    // ------------------------------------------------------------------------

    @Test
    void issuesOfDifferentKindsAreAllReported() {
        ConferenceScheduleInput input = input(TALK_TYPES,
                List.of(timeslot("ts1"), timeslot("ts1")),
                List.of(room(null)),
                List.of(speaker("s1"), speaker("s1")),
                List.of(TalkDTO.builder("T1", "Title", "Keynote")
                        .speakerIds(List.of("unknown-speaker"))
                        .timeslotId("unknown-timeslot")
                        .roomId("unknown-room")
                        .build()));

        ValidationResult<Issue> result = validate(input);

        assertThat(codesOf(result)).containsExactlyInAnyOrder(
                "DUPLICATE_TIMESLOT_ID",
                "ROOM_ID_MISSING",
                "DUPLICATE_SPEAKER_ID",
                "NON_EXISTING_TIMESLOT_REFERENCE",
                "NON_EXISTING_ROOM_REFERENCE",
                "NON_EXISTING_TALK_TYPE_REFERENCE",
                "NON_EXISTING_SPEAKER_REFERENCE");
        assertThat(result.status()).isEqualTo(ValidationStatus.ERRORS);
        assertThat(result.isValid()).isFalse();
    }

    @Test
    void talkWithoutCodeStillReportsItsOtherIssuesButWithoutATalkDetail() {
        ConferenceScheduleInput input = inputWithTalks(TalkDTO.builder(null, "Title", "Keynote")
                .speakerIds(List.of("unknown-speaker"))
                .timeslotId("unknown-timeslot")
                .roomId("unknown-room")
                .build());

        ValidationResult<Issue> result = validate(input);

        assertThat(codesOf(result)).containsExactlyInAnyOrder(
                "TALK_ID_MISSING",
                "NON_EXISTING_TIMESLOT_REFERENCE",
                "NON_EXISTING_ROOM_REFERENCE",
                "NON_EXISTING_TALK_TYPE_REFERENCE",
                "NON_EXISTING_SPEAKER_REFERENCE");
        assertThat(talkIdsOf(result)).isEmpty();
    }

    @Test
    void talkWithBlankCodeStillReportsItsOtherIssuesButWithoutATalkDetail() {
        ValidationResult<Issue> result = validate(inputWithTalks(talk("  ", "unknown-speaker")));

        assertThat(codesOf(result)).containsExactlyInAnyOrder("TALK_ID_MISSING", "NON_EXISTING_SPEAKER_REFERENCE");
        assertThat(talkIdsOf(result)).isEmpty();
    }

    @Test
    void talkIssuesAreReportedAgainstTheRoomsAndTimeslotsThatSurvivedValidation() {
        // A room with a duplicate ID is still a known room, so referencing it is not a dangling reference.
        ConferenceScheduleInput input = input(TALK_TYPES, TIMESLOTS, List.of(room("r1"), room("r1")), SPEAKERS,
                List.of(talk("T1", "s1").withRoomId("r1")));

        ValidationResult<Issue> result = validate(input);

        assertThat(codesOf(result)).containsExactly("DUPLICATE_ROOM_ID");
    }

    @Test
    void theSharedSolverTestProblemIsValid() {
        // The solver tests would otherwise silently solve a dataset the service would have rejected.
        ValidationResult<Issue> result = validate(createProblem());

        assertThat(result.issues()).isEmpty();
    }

    // ------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------

    private ValidationResult<Issue> validate(ConferenceScheduleInput input) {
        ValidationBuilder validationBuilder = new ValidationBuilder();
        validator.validate(validationBuilder, input, ModelConfig.empty());
        return validationBuilder.build();
    }

    private static Issue singleIssue(ValidationResult<Issue> result, String expectedCode) {
        Collection<Issue> issues = result.issues();
        assertThat(issues).hasSize(1);
        Issue issue = issues.iterator().next();
        assertThat(issue.getCode()).isEqualTo(IssueCode.of(expectedCode));
        return issue;
    }

    private static <T extends Issue> T singleIssue(ValidationResult<Issue> result, Class<T> expectedType) {
        Collection<Issue> issues = result.issues();
        assertThat(issues).hasSize(1);
        Issue issue = issues.iterator().next();
        assertThat(issue).isInstanceOf(expectedType);
        return expectedType.cast(issue);
    }

    private static List<String> codesOf(ValidationResult<Issue> result) {
        return result.issues().stream().map(issue -> issue.getCode().value()).toList();
    }

    private static List<String> talkIdsOf(ValidationResult<Issue> result) {
        return result.issues().stream()
                .map(ConferenceScheduleValidatorTest::talkIdOf)
                .filter(Objects::nonNull)
                .toList();
    }

    private static String talkIdOf(Issue issue) {
        return switch (issue) {
            case DuplicateTalkIdIssue talkIssue -> talkIssue.getTalkId();
            case NonExistingTimeslotReferenceIssue talkIssue -> talkIssue.getTalkId();
            case NonExistingRoomReferenceIssue talkIssue -> talkIssue.getTalkId();
            case NonExistingSpeakerReferenceIssue talkIssue -> talkIssue.getTalkId();
            case NonExistingTalkTypeReferenceIssue talkIssue -> talkIssue.getTalkId();
            default -> null;
        };
    }
}
