package org.acme.employeescheduling.persistence;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import org.acme.employeescheduling.domain.Availability;

@ApplicationScoped
public class AvailabilityRepository implements PanacheRepositoryBase<Availability, String> {

}
