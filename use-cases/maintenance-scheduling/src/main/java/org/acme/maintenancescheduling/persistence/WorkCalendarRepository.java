package org.acme.maintenancescheduling.persistence;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import org.acme.maintenancescheduling.domain.WorkCalendar;

@ApplicationScoped
public class WorkCalendarRepository implements PanacheRepository<WorkCalendar> {

}
