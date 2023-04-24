package org.acme.maintenancescheduling.persistence;

import jakarta.enterprise.context.ApplicationScoped;

import org.acme.maintenancescheduling.domain.Crew;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class CrewRepository implements PanacheRepository<Crew> {

}
