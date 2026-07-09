package org.acme.schooltimetabling.dto;

import ai.timefold.solver.service.definition.api.validation.IssueMetadata;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Details about a lesson ID validation issue.")
public record LessonIdDetail(
        @Schema(description = "The ID of the lesson.") String lessonId) implements IssueMetadata {

    public LessonIdDetail {
        lessonId = lessonId == null ? "" : lessonId;
    }

    public LessonIdDetail withLessonId(String lessonId) {
        return new LessonIdDetail(lessonId);
    }

    @Override
    public String getType() {
        return "LessonId";
    }
}
