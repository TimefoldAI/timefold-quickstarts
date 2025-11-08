package org.acme.facilitylocation.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.facilitylocation.domain.FacilityLocationProblem;

import java.util.Optional;

@ApplicationScoped
public class FacilityLocationProblemRepository {

    private FacilityLocationProblem facilityLocationProblem;

    public Optional<FacilityLocationProblem> solution() {
        return Optional.ofNullable(facilityLocationProblem);
    }

    public void update(FacilityLocationProblem facilityLocationProblem) {
        this.facilityLocationProblem = facilityLocationProblem;
    }
}
