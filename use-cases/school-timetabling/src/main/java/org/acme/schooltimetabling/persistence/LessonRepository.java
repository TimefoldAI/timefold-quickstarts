package org.acme.schooltimetabling.persistence;

import jakarta.enterprise.context.ApplicationScoped;

import org.acme.schooltimetabling.domain.Lesson;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class LessonRepository implements PanacheRepository<Lesson> {

}
