package org.acme.employeescheduling.persistence;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import org.acme.employeescheduling.domain.Shift;

@ApplicationScoped
public class ShiftRepository implements PanacheRepositoryBase<Shift, String> {

}
