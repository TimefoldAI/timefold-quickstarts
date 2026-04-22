package org.acme.taskassigning.domain;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = TaskType.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "code")
public class TaskType {

    private String code;
    private String title;
    private long baseDuration; // In minutes
    private List<String> requiredSkills;

    public TaskType() {
    }

    public TaskType(String code, String title, int baseDuration) {
        this.code = code;
        this.title = title;
        this.baseDuration = baseDuration;
        requiredSkills = new ArrayList<>();
    }

    public TaskType(String code, String title, int baseDuration, List<String> requiredSkills) {
        this.code = code;
        this.title = title;
        this.baseDuration = baseDuration;
        this.requiredSkills = requiredSkills;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getBaseDuration() {
        return baseDuration;
    }

    public void setBaseDuration(long baseDuration) {
        this.baseDuration = baseDuration;
    }

    public List<String> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(List<String> requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @Override
    public String toString() {
        return code;
    }

}
