package org.acme.conferencescheduling.service;

import static org.acme.conferencescheduling.support.TestHelper.LAB;
import static org.acme.conferencescheduling.support.TestHelper.SPEAKERS;
import static org.acme.conferencescheduling.support.TestHelper.TALK_TYPES;
import static org.acme.conferencescheduling.support.TestHelper.TIMESLOTS;
import static org.acme.conferencescheduling.support.TestHelper.assignedTalk;
import static org.acme.conferencescheduling.support.TestHelper.assignedTalkOfType;
import static org.acme.conferencescheduling.support.TestHelper.createProblem;
import static org.acme.conferencescheduling.support.TestHelper.input;
import static org.acme.conferencescheduling.support.TestHelper.inputWithRooms;
import static org.acme.conferencescheduling.support.TestHelper.inputWithSpeakers;
import static org.acme.conferencescheduling.support.TestHelper.inputWithTalks;
import static org.acme.conferencescheduling.support.TestHelper.inputWithTimeslots;
import static org.acme.conferencescheduling.support.TestHelper.room;
import static org.acme.conferencescheduling.support.TestHelper.speaker;
import static org.acme.conferencescheduling.support.TestHelper.talk;
import static org.acme.conferencescheduling.support.TestHelper.talkOfType;
import static org.acme.conferencescheduling.support.TestHelper.timeslot;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;

import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.validation.Issue;
import ai.timefold.solver.service.definition.api.validation.ValidationBuilder;
import ai.timefold.solver.service.definition.api.validation.ValidationStatus;
import ai.timefold.solver.service.definition.api.validation.dto.ValidationResult;

import org.acme.conferencescheduling.dto.input.ConferenceScheduleInput;
import org.acme.conferencescheduling.service.validation.ConferenceScheduleIssue.DuplicateRoomIdIssue;
import org.acme.conferencescheduling.service.validation.ConferenceScheduleIssue.DuplicateSpeakerIdIssue;
import org.acme.conferencescheduling.service.validation.ConferenceScheduleIssue.DuplicateTalkIdIssue;
import org.acme.conferencescheduling.service.validation.ConferenceScheduleIssue.DuplicateTimeslotIdIssue;
import org.acme.conferencescheduling.service.validation.ConferenceScheduleIssue.NonExistingRoomReferenceIssue;
import org.acme.conferencescheduling.service.validation.ConferenceScheduleIssue.NonExistingSpeakerReferenceIssue;
import org.acme.conferencescheduling.service.validation.ConferenceScheduleIssue.NonExistingTalkTypeReferenceIssue;
import org.acme.conferencescheduling.service.validation.ConferenceScheduleIssue.NonExistingTimeslotReferenceIssue;
import org.junit.jupiter.api.Test;

// OpenAPI spec compliance (Bean Validation) is enforced by the Service module at the REST layer, so it's
// covered by org.acme.conferencescheduling.rest.ConferenceScheduleOpenApiValidationTest instead.
// This class only covers the domain-specific checks ConferenceScheduleValidator implements itself.
class ConferenceScheduleValidatorTest {

    private final ConferenceScheduleValidator validator = new ConferenceScheduleValidator();

    @Test
    void validInputHasNoIssues() {
        ValidationResult<Issue> result = validate(inputWithTalks(talk("T1", "s1"), talk("T2", "s1", "s2")));

        assertThat(result.issues()).isEmpty();
        assertThat(result.status()).isEqualTo(ValidationStatus.OK);
        assertThat(result.isValid()).isTrue();
    }

    // ------------------------------------------------------------------------
    // Timeslots
    // ------------------------------------------------------------------------

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
    void duplicateRoomIdIsReportedWithTheOffendingId() {
        ValidationResult<Issue> result = validate(inputWithRooms(room("r1"), room("r1")));

        DuplicateRoomIdIssue issue = singleIssue(result, DuplicateRoomIdIssue.class);
        assertThat(issue.getRoomId()).isEqualTo("r1");
    }

    // ------------------------------------------------------------------------
    // Speakers
    // ------------------------------------------------------------------------

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
        ValidationResult<Issue> result = validate(inputWithTalks(assignedTalk("T1", "does-not-exist", null, "s1")));

        NonExistingTimeslotReferenceIssue issue = singleIssue(result, NonExistingTimeslotReferenceIssue.class);
        assertThat(issue.getTalkId()).isEqualTo("T1");
    }

    @Test
    void talkReferencingNonExistingRoomIsReported() {
        ValidationResult<Issue> result = validate(inputWithTalks(assignedTalk("T1", null, "does-not-exist", "s1")));

        NonExistingRoomReferenceIssue issue = singleIssue(result, NonExistingRoomReferenceIssue.class);
        assertThat(issue.getTalkId()).isEqualTo("T1");
    }

    @Test
    void talkReferencingExistingTimeslotAndRoomIsAccepted() {
        ValidationResult<Issue> result = validate(inputWithTalks(assignedTalk("T1", "ts1", "r1", "s1")));

        assertThat(result.issues()).isEmpty();
    }

    @Test
    void unassignedTalkIsAccepted() {
        ValidationResult<Issue> result = validate(inputWithTalks(talk("T1", "s1")));

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

    // ------------------------------------------------------------------------
    // Talks: talk type
    // ------------------------------------------------------------------------

    @Test
    void talkReferencingNonExistingTalkTypeIsReported() {
        ValidationResult<Issue> result = validate(inputWithTalks(talkOfType("T1", "Keynote", "s1")));

        NonExistingTalkTypeReferenceIssue issue = singleIssue(result, NonExistingTalkTypeReferenceIssue.class);
        assertThat(issue.getTalkId()).isEqualTo("T1");
    }

    @Test
    void talkWithNullTalkTypeIsReportedAsANonExistingReference() {
        // A missing talk type is just as invalid as an unknown one, and this validator treats it that way
        // directly: it doesn't rely on Bean Validation's @NotBlank to reject it first.
        ValidationResult<Issue> result = validate(inputWithTalks(talkOfType("T1", null, "s1")));

        NonExistingTalkTypeReferenceIssue issue = singleIssue(result, NonExistingTalkTypeReferenceIssue.class);
        assertThat(issue.getTalkId()).isEqualTo("T1");
    }

    @Test
    void talkWithBlankTalkTypeIsReportedAsANonExistingReference() {
        ValidationResult<Issue> result = validate(inputWithTalks(talkOfType("T1", "  ", "s1")));

        NonExistingTalkTypeReferenceIssue issue = singleIssue(result, NonExistingTalkTypeReferenceIssue.class);
        assertThat(issue.getTalkId()).isEqualTo("T1");
    }

    @Test
    void talkOfAnyDeclaredTalkTypeIsAccepted() {
        ValidationResult<Issue> result = validate(inputWithTalks(talkOfType("T1", LAB, "s1")));

        assertThat(result.issues()).isEmpty();
    }

    // ------------------------------------------------------------------------
    // Accumulation across entity types
    // ------------------------------------------------------------------------

    @Test
    void issuesOfDifferentKindsAreAllReported() {
        ConferenceScheduleInput input = input(TALK_TYPES,
                List.of(timeslot("ts1"), timeslot("ts1")),
                List.of(room("r1"), room("r1")),
                List.of(speaker("s1"), speaker("s1")),
                List.of(assignedTalkOfType("T1", "Keynote", "unknown-timeslot", "unknown-room", "unknown-speaker")));

        ValidationResult<Issue> result = validate(input);

        assertThat(codesOf(result)).containsExactlyInAnyOrder(
                "DUPLICATE_TIMESLOT_ID",
                "DUPLICATE_ROOM_ID",
                "DUPLICATE_SPEAKER_ID",
                "NON_EXISTING_TIMESLOT_REFERENCE",
                "NON_EXISTING_ROOM_REFERENCE",
                "NON_EXISTING_TALK_TYPE_REFERENCE",
                "NON_EXISTING_SPEAKER_REFERENCE");
        assertThat(result.status()).isEqualTo(ValidationStatus.ERRORS);
        assertThat(result.isValid()).isFalse();
    }

    @Test
    void talkIssuesAreReportedAgainstTheRoomsAndTimeslotsThatSurvivedValidation() {
        // A room with a duplicate ID is still a known room, so referencing it is not a dangling reference.
        ConferenceScheduleInput input = input(TALK_TYPES, TIMESLOTS, List.of(room("r1"), room("r1")), SPEAKERS,
                List.of(assignedTalk("T1", null, "r1", "s1")));

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
}
