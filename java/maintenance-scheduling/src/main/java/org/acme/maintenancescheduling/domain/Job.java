package org.acme.maintenancescheduling.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;
import java.util.function.Predicate;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import ai.timefold.solver.core.api.domain.variable.ShadowSources;
import ai.timefold.solver.core.api.domain.variable.ShadowVariable;

@PlanningEntity
public class Job {

    @PlanningId
    private String id;

    private String name;
    private int durationInDays;
    private LocalDate minStartDate; // Inclusive
    private LocalDate maxEndDate; // Exclusive
    private LocalDate idealEndDate; // Exclusive

    private Set<String> tags;

    @PlanningVariable
    private Crew crew;
    // Follows the TimeGrain Design Pattern
    @PlanningVariable
    private LocalDate startDate; // Inclusive
    @ShadowVariable(supplierName = "endDateSupplier")
    private LocalDate endDate; // Exclusive

    public Job() {
    }

    public Job(String id, String name, int durationInDays, LocalDate minStartDate, LocalDate maxEndDate, LocalDate idealEndDate, Set<String> tags) {
        this.id = id;
        this.name = name;
        this.durationInDays = durationInDays;
        this.minStartDate = minStartDate;
        this.maxEndDate = maxEndDate;
        this.idealEndDate = idealEndDate;
        this.tags = tags;
    }

    public Job(String id, String name, int durationInDays, LocalDate minStartDate, LocalDate maxEndDate, LocalDate idealEndDate, Set<String> tags,
               Crew crew, LocalDate startDate) {
        this.id = id;
        this.name = name;
        this.durationInDays = durationInDays;
        this.minStartDate = minStartDate;
        this.maxEndDate = maxEndDate;
        this.idealEndDate = idealEndDate;
        this.tags = tags;
        this.crew = crew;
        this.startDate = startDate;
        this.endDate = calculateEndDate(startDate, durationInDays);
    }

    @Override
    public String toString() {
        return name + "(" + id + ")";
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getDurationInDays() {
        return durationInDays;
    }

    public LocalDate getMinStartDate() {
        return minStartDate;
    }

    public LocalDate getMaxEndDate() {
        return maxEndDate;
    }

    public LocalDate getIdealEndDate() {
        return idealEndDate;
    }

    public Set<String> getTags() {
        return tags;
    }

    public Crew getCrew() {
        return crew;
    }

    public void setCrew(Crew crew) {
        this.crew = crew;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************
    @SuppressWarnings("unused")
    @ShadowSources("startDate")
    public LocalDate endDateSupplier() {
        return calculateEndDate(startDate, durationInDays);
    }

    public static LocalDate calculateEndDate(LocalDate startDate, int durationInDays) {
        if (startDate == null) {
            return null;
        } else {
            // Skip weekends. Does not work for holidays.
            // To skip holidays too, cache all working days in WorkCalendar.
            // Keep in sync with MaintenanceSchedule.createStartDateList().
            Predicate<LocalDate> exclude = test ->
                    test.getDayOfWeek() == DayOfWeek.SATURDAY
                    || test.getDayOfWeek() == DayOfWeek.SUNDAY;
            return plusBusinessDays(startDate, durationInDays, exclude);
        }
    }

    private static LocalDate plusBusinessDays(LocalDate startDate, int businessDays, Predicate<LocalDate> exclude) {
        LocalDate result = startDate;
        int addedDays = 0;
        while (addedDays < businessDays) {
            if (!exclude.test(result)) {
                addedDays++;
            }
            result = result.plusDays(1);
        }
        return result;
    }
}
