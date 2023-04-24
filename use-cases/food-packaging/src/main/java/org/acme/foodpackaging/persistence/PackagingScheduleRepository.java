package org.acme.foodpackaging.persistence;

import java.util.concurrent.atomic.AtomicReference;

import jakarta.enterprise.context.ApplicationScoped;

import org.acme.foodpackaging.domain.PackagingSchedule;

@ApplicationScoped
public class PackagingScheduleRepository {

    private final AtomicReference<PackagingSchedule> solutionReference = new AtomicReference<>();

    public PackagingSchedule read() {
        return solutionReference.get();
    }

    public void write(PackagingSchedule schedule) {
        solutionReference.set(schedule);
    }

}
