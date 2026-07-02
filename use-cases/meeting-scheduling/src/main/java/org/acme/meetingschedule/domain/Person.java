package org.acme.meetingschedule.domain;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = Person.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Person {

    private String id;
    private String fullName;

    public Person() {
    }

    public Person(String id) {
        this.id = id;
    }

    public Person(String id, String fullName) {
        this(id);
        this.fullName = fullName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Person person))
            return false;
        return Objects.equals(getId(), person.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
