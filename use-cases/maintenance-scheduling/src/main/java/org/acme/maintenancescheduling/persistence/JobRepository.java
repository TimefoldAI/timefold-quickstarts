package org.acme.maintenancescheduling.persistence;

import jakarta.enterprise.context.ApplicationScoped;

import org.acme.maintenancescheduling.domain.Job;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class JobRepository implements PanacheRepository<Job> {

}
