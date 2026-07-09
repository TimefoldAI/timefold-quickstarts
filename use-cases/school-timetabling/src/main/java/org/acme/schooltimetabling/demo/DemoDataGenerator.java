package org.acme.schooltimetabling.demo;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.service.definition.api.data.AbstractBasicDemoDataGenerator;
import ai.timefold.solver.service.definition.api.domain.Configuration;
import ai.timefold.solver.service.definition.api.domain.ModelConfig;
import ai.timefold.solver.service.definition.api.domain.ModelRequest;
import ai.timefold.solver.service.definition.api.domain.RunConfiguration;

import org.acme.schooltimetabling.dto.TimetableConfigOverrides;
import org.acme.schooltimetabling.dto.TimetableInput;

@ApplicationScoped
public class DemoDataGenerator
        extends
        AbstractBasicDemoDataGenerator<TimetableInput, TimetableConfigOverrides> {

    private static final String MATH = "Math";
    private static final String PHYSICS = "Physics";
    private static final String CHEMISTRY = "Chemistry";
    private static final String ENGLISH = "English";
    private static final String SPANISH = "Spanish";
    private static final String HISTORY = "History";
    private static final String TURING = "A. Turing";
    private static final String CURIE = "M. Curie";
    private static final String JONES = "I. Jones";
    private static final String CRUZ = "P. Cruz";
    private static final String DARWIN = "C. Darwin";
    private static final String GRADE_9 = "9th grade";
    private static final String GRADE_10 = "10th grade";

    @Override
    protected ModelRequest<TimetableInput, TimetableConfigOverrides> generateBasicDemoDataRequest() {
        TimetableInput problem = DemoDataBuilder.builder()
                .setDayCount(2)
                .setRoomCount(3)
                .addLesson(MATH, TURING, GRADE_9)
                .addLesson(MATH, TURING, GRADE_9)
                .addLesson(PHYSICS, CURIE, GRADE_9)
                .addLesson(CHEMISTRY, CURIE, GRADE_9)
                .addLesson("Biology", DARWIN, GRADE_9)
                .addLesson(HISTORY, JONES, GRADE_9)
                .addLesson(ENGLISH, JONES, GRADE_9)
                .addLesson(ENGLISH, JONES, GRADE_9)
                .addLesson(SPANISH, CRUZ, GRADE_9)
                .addLesson(SPANISH, CRUZ, GRADE_9)
                .addLesson(MATH, TURING, GRADE_10)
                .addLesson(MATH, TURING, GRADE_10)
                .addLesson(MATH, TURING, GRADE_10)
                .addLesson(PHYSICS, CURIE, GRADE_10)
                .addLesson(CHEMISTRY, CURIE, GRADE_10)
                .addLesson("French", CURIE, GRADE_10)
                .addLesson("Geography", DARWIN, GRADE_10)
                .addLesson(HISTORY, JONES, GRADE_10)
                .addLesson(ENGLISH, CRUZ, GRADE_10)
                .addLesson(SPANISH, CRUZ, GRADE_10)
                .build();
        // Ship no constraint weight overrides in the demo input, so that any overrides coming from the
        // configuration profile are applied instead of being masked. Callers that want to override
        // specific weights via the input can build a TimetableConfigOverrides and set only those.
        Configuration<TimetableConfigOverrides> configuration = new Configuration<>(
                new RunConfiguration("BASIC"), ModelConfig.empty());
        return new ModelRequest<>(configuration, problem);
    }
}
