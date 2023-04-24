package org.acme.callcenter.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.enterprise.context.ApplicationScoped;

import org.acme.callcenter.domain.Agent;
import org.acme.callcenter.domain.Call;
import org.acme.callcenter.domain.CallCenter;
import org.acme.callcenter.domain.Skill;

@ApplicationScoped
public class DataGenerator {

    private static final AtomicLong NEXT_ID = new AtomicLong(0L);

    private final Random RANDOM = new Random(37);

    private static final Agent[] AGENTS = new Agent[] {
            new Agent(nextId(), "Ann", buildSkillSet(Skill.ENGLISH, Skill.LIFE_INSURANCE, Skill.PROPERTY_INSURANCE)),
            new Agent(nextId(), "Beth", buildSkillSet(Skill.ENGLISH, Skill.SPANISH, Skill.CAR_INSURANCE)),
            new Agent(nextId(), "Carl", buildSkillSet(Skill.ENGLISH, Skill.PROPERTY_INSURANCE)),
            new Agent(nextId(), "Dennis", buildSkillSet(Skill.SPANISH, Skill.LIFE_INSURANCE)),
            new Agent(nextId(), "Elsa", buildSkillSet(Skill.SPANISH, Skill.CAR_INSURANCE, Skill.PROPERTY_INSURANCE)),
            new Agent(nextId(), "Francis", buildSkillSet(Skill.SPANISH, Skill.PROPERTY_INSURANCE)),
            new Agent(nextId(), "Gus", buildSkillSet(Skill.GERMAN, Skill.ENGLISH, Skill.LIFE_INSURANCE)),
            new Agent(nextId(), "Hugo", buildSkillSet(Skill.GERMAN, Skill.CAR_INSURANCE, Skill.PROPERTY_INSURANCE))
    };

    private static final Skill[] LANGUAGE_SKILLS = new Skill[] { Skill.ENGLISH, Skill.SPANISH, Skill.GERMAN };
    private static final Skill[] PRODUCT_SKILLS =
            new Skill[] { Skill.CAR_INSURANCE, Skill.LIFE_INSURANCE, Skill.PROPERTY_INSURANCE };

    private static Set<Skill> buildSkillSet(Skill... skills) {
        return EnumSet.copyOf(Arrays.asList(skills));
    }

    public CallCenter generateCallCenter() {
        return new CallCenter(EnumSet.allOf(Skill.class), Arrays.asList(AGENTS), new ArrayList<>());
    }

    public Call generateCall(int durationSeconds) {
        return new Call(nextId(), generatePhoneNumber(), buildSkillSet(pickRandomLanguageSkill(), pickRandomProductSkill()),
                durationSeconds);
    }

    private synchronized Skill pickRandomProductSkill() {
        return PRODUCT_SKILLS[RANDOM.nextInt(PRODUCT_SKILLS.length)];
    }

    private synchronized Skill pickRandomLanguageSkill() {
        return LANGUAGE_SKILLS[RANDOM.nextInt(LANGUAGE_SKILLS.length)];
    }

    private synchronized String generatePhoneNumber() {
        int firstGroup = RANDOM.nextInt(1_000);
        int secondGroup = RANDOM.nextInt(1_000);
        int thirdGroup = RANDOM.nextInt(10_000);
        return firstGroup + "-" + secondGroup + "-" + thirdGroup;
    }

    private static long nextId() {
        return NEXT_ID.getAndIncrement();
    }
}
