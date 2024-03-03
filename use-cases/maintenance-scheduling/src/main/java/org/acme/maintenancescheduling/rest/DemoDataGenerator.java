package org.acme.maintenancescheduling.rest;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

import org.acme.maintenancescheduling.domain.Crew;
import org.acme.maintenancescheduling.domain.Job;
import org.acme.maintenancescheduling.domain.MaintenanceSchedule;
import org.acme.maintenancescheduling.domain.WorkCalendar;
import org.acme.maintenancescheduling.solver.EndDateUpdatingVariableListener;

@ApplicationScoped
public class DemoDataGenerator {

    public enum DemoData {
        SMALL,
        LARGE
    }

    public MaintenanceSchedule generateDemoData(DemoData demoData) {
        MaintenanceSchedule maintenanceSchedule = new MaintenanceSchedule();

        List<Crew> crews = new ArrayList<>();
        crews.add(new Crew("1", "Alpha crew"));
        crews.add(new Crew("2", "Beta crew"));
        crews.add(new Crew("3", "Gamma crew"));
        if (demoData == DemoData.LARGE) {
            crews.add(new Crew("4", "Delta crew"));
            crews.add(new Crew("5", "Epsilon crew"));
        }
        maintenanceSchedule.setCrews(crews);

        LocalDate fromDate = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        int weekListSize = (demoData == DemoData.LARGE) ? 16 : 8;
        LocalDate toDate = fromDate.plusWeeks(weekListSize);
        maintenanceSchedule.setWorkCalendar(new WorkCalendar("1", fromDate, toDate));

        int workdayTotal = weekListSize * 5;

        final String[] jobAreaNames = {
                "Downtown", "Uptown", "Park", "Airport", "Bay", "Hill", "Forest", "Station", "Hospital",
                "Harbor", "Market", "Fort", "Beach", "Garden", "River", "Springs", "Tower", "Mountain" };
        final String[] jobTargetNames = { "Street", "Bridge", "Tunnel", "Highway", "Boulevard", "Avenue",
                "Square", "Plaza" };

        List<Job> jobs = new ArrayList<>();
        int jobListSize = weekListSize * crews.size() * 3 / 5;
        int jobAreaTargetLimit = Math.min(jobTargetNames.length, crews.size() * 2);
        Random random = new Random(17);
        for (int i = 0; i < jobListSize; i++) {
            String jobArea = jobAreaNames[i / jobAreaTargetLimit];
            String jobTarget = jobTargetNames[i % jobAreaTargetLimit];
            // 1 day to 2 workweeks (1 workweek on average)
            int durationInDays = 1 + random.nextInt(10);
            int readyDueBetweenWorkdays = durationInDays + 5 // at least 5 days of flexibility
                    + random.nextInt(workdayTotal - (durationInDays + 5));
            int readyWorkdayOffset = random.nextInt(workdayTotal - readyDueBetweenWorkdays + 1);
            int readyIdealEndBetweenWorkdays = readyDueBetweenWorkdays - 1 - random.nextInt(4);
            LocalDate readyDate = EndDateUpdatingVariableListener.calculateEndDate(fromDate, readyWorkdayOffset);
            LocalDate dueDate = EndDateUpdatingVariableListener.calculateEndDate(readyDate, readyDueBetweenWorkdays);
            LocalDate idealEndDate = EndDateUpdatingVariableListener.calculateEndDate(readyDate, readyIdealEndBetweenWorkdays);
            Set<String> tags = random.nextDouble() < 0.1 ? Set.of(jobArea, "Subway") : Set.of(jobArea);
            jobs.add(new Job(Integer.toString(i), jobArea + " " + jobTarget, durationInDays, readyDate, dueDate, idealEndDate,
                    tags));
        }
        maintenanceSchedule.setJobs(jobs);
        return maintenanceSchedule;
    }

}
