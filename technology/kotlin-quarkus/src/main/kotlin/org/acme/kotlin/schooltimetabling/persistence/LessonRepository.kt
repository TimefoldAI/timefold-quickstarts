package org.acme.kotlin.schooltimetabling.persistence

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import org.acme.kotlin.schooltimetabling.domain.Lesson
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class LessonRepository : PanacheRepository<Lesson>
