package org.acme.schooltimetabling.dto.input;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = """
        A lesson of one subject, taught by one teacher to one student group. The solver assigns it \
        a timeslot and a room.""")
public record LessonInputDTO(
        @Schema(description = "Unique identifier of the lesson.", required = true, minLength = 1) String id,
        @Schema(description = "Subject taught in the lesson, e.g. Math.", required = true, minLength = 1) String subject,
        @Schema(description = """
                Name of the teacher giving the lesson. Lessons with the same teacher never overlap.""",
                required = true, minLength = 1) String teacher,
        @Schema(description = """
                Name of the student group attending the lesson. Lessons with the same student group \
                never overlap.""", required = true, minLength = 1) String studentGroup,
        @Schema(description = "ID of the timeslot the lesson is scheduled in, or null if not decided yet.",
                minLength = 1) String timeslotId,
        @Schema(description = "ID of the room the lesson is scheduled in, or null if not decided yet.",
                minLength = 1) String roomId) {

    public LessonInputDTO withAssignment(String timeslotId, String roomId) {
        return new LessonInputDTO(id, subject, teacher, studentGroup, timeslotId, roomId);
    }
}
