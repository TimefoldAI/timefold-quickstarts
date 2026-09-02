package org.acme.meetingschedule.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

import org.acme.meetingschedule.dto.input.MeetingInputDTO;
import org.acme.meetingschedule.dto.input.MeetingScheduleInput;
import org.acme.meetingschedule.dto.input.OfficeHoursDTO;
import org.acme.meetingschedule.dto.input.PersonInputDTO;
import org.junit.jupiter.api.Test;

class DemoDataBuilderTest {

    @Test
    void shouldBuildData() {
        MeetingScheduleInput problem = DemoDataBuilder.basic();

        assertThat(problem.people()).hasSize(20);
        assertThat(problem.people()).extracting(PersonInputDTO::id).doesNotHaveDuplicates();
        assertThat(problem.rooms()).hasSize(3);
        assertThat(problem.rooms()).extracting(room -> room.capacity()).containsExactly(30, 20, 16);
        assertThat(problem.meetings()).hasSize(24);
    }

    @Test
    void officeHoursAreOneWorkWeekOfTenHourDays() {
        var timeConfiguration = DemoDataBuilder.basic().timeConfiguration();

        assertThat(timeConfiguration.granularityInMinutes()).isEqualTo(15);
        assertThat(timeConfiguration.days()).hasSize(5);
        assertThat(timeConfiguration.days())
                .isSortedAccordingTo(Comparator.comparing(OfficeHoursDTO::startDateTime));
        assertThat(timeConfiguration.days()).extracting(day -> day.startDateTime().toLocalDate())
                .doesNotHaveDuplicates();
        for (OfficeHoursDTO day : timeConfiguration.days()) {
            assertThat(day.startDateTime().toLocalTime()).isEqualTo(LocalTime.of(8, 0));
            assertThat(day.endDateTime().toLocalTime()).isEqualTo(LocalTime.of(18, 0));
            assertThat(day.endDateTime().toLocalDate()).isEqualTo(day.startDateTime().toLocalDate());
        }
        // Five days of ten hours, divided into quarters of an hour.
        assertThat(timeConfiguration.slotStartDateTimes()).hasSize(200);
    }

    @Test
    void everyMeetingFitsInTheLargestRoomAndInOneOfficeDay() {
        MeetingScheduleInput problem = DemoDataBuilder.basic();
        var timeConfiguration = problem.timeConfiguration();
        int largestRoomCapacity = problem.rooms().stream().mapToInt(room -> room.capacity()).max().orElseThrow();
        long shortestDayInMinutes = timeConfiguration.days().stream()
                .mapToLong(day -> Duration.between(day.startDateTime(), day.endDateTime()).toMinutes())
                .min()
                .orElseThrow();
        List<OffsetDateTime> slotStartDateTimes = timeConfiguration.slotStartDateTimes();

        for (MeetingInputDTO meeting : problem.meetings()) {
            assertThat(meeting.roomId()).isNull();
            assertThat(meeting.startDateTime()).isNull();
            assertThat(meeting.durationInMinutes() % timeConfiguration.granularityInMinutes()).isZero();
            assertThat(meeting.durationInMinutes()).isBetween(1, (int) shortestDayInMinutes);
            // Nobody both has to and would like to attend the same meeting.
            assertThat(meeting.requiredAttendeeIds()).doesNotHaveDuplicates()
                    .doesNotContainAnyElementsOf(meeting.preferredAttendeeIds());
            assertThat(meeting.preferredAttendeeIds()).doesNotHaveDuplicates();
            int attendeeCount = meeting.requiredAttendeeIds().size() + meeting.preferredAttendeeIds().size();
            assertThat(attendeeCount).isBetween(1, largestRoomCapacity);
        }
        assertThat(slotStartDateTimes).isNotEmpty();
    }
}
