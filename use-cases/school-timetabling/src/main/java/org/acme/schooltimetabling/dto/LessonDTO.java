package org.acme.schooltimetabling.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A lesson taught by a teacher to a student group about a subject, assigned to a timeslot and room.")
public record LessonDTO(
        @Schema(description = "Unique identifier of the lesson.") String id,
        @Schema(description = "Subject taught during the lesson.") String subject,
        @Schema(description = "Teacher who teaches the lesson.") String teacher,
        @Schema(description = "Student group that attends the lesson.") String studentGroup,
        @Schema(description = "ID of the timeslot assigned to the lesson. Null when unscheduled.") String timeslotId,
        @Schema(description = "ID of the room assigned to the lesson. Null when unscheduled.") String roomId) {

    public LessonDTO {
        timeslotId = normalizeId(timeslotId);
        roomId = normalizeId(roomId);
    }

    private static String normalizeId(String id) {
        return id != null && id.isBlank() ? null : id;
    }

    public LessonDTO withId(String id) {
        return new LessonDTO(id, subject, teacher, studentGroup, timeslotId, roomId);
    }

    public LessonDTO withSubject(String subject) {
        return new LessonDTO(id, subject, teacher, studentGroup, timeslotId, roomId);
    }

    public LessonDTO withTeacher(String teacher) {
        return new LessonDTO(id, subject, teacher, studentGroup, timeslotId, roomId);
    }

    public LessonDTO withStudentGroup(String studentGroup) {
        return new LessonDTO(id, subject, teacher, studentGroup, timeslotId, roomId);
    }

    public LessonDTO withTimeslotId(String timeslotId) {
        return new LessonDTO(id, subject, teacher, studentGroup, timeslotId, roomId);
    }

    public LessonDTO withRoomId(String roomId) {
        return new LessonDTO(id, subject, teacher, studentGroup, timeslotId, roomId);
    }
}
