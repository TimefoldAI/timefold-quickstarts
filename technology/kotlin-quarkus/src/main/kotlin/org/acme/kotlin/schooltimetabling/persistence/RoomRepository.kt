package org.acme.kotlin.schooltimetabling.persistence

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import org.acme.kotlin.schooltimetabling.domain.Room
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class RoomRepository : PanacheRepository<Room>
