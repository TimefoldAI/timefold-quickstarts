package org.acme.schooltimetabling.config;

import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;
import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.Timetable;
import org.acme.schooltimetabling.solver.TimetableConstraintProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class ConstraintConfig {

    @Bean
    public ConstraintVerifier<TimetableConstraintProvider, Timetable> buildConstraintVerifier() {
        return ConstraintVerifier.build(new TimetableConstraintProvider(), Timetable.class, Lesson.class);
    }
}
