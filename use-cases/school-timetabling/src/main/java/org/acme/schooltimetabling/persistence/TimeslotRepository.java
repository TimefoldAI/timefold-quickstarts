package org.acme.schooltimetabling.persistence;

import jakarta.enterprise.context.ApplicationScoped;

import org.acme.schooltimetabling.domain.Timeslot;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class TimeslotRepository implements PanacheRepository<Timeslot> {

}
