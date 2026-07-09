package org.acme.maintenancescheduling.solver;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import org.acme.maintenancescheduling.domain.Job;
import org.acme.maintenancescheduling.dto.CrewDTO;
import org.acme.maintenancescheduling.dto.JobDTO;
import org.acme.maintenancescheduling.dto.MaintenanceScheduleInput;
import org.acme.maintenancescheduling.dto.WorkCalendarDTO;

final class SolverTestDataFactory {

    private SolverTestDataFactory() {
    }

    static MaintenanceScheduleInput createProblem() {
        LocalDate fromDate = LocalDate.now(ZoneId.systemDefault()).with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        int weekListSize = 8;
        LocalDate toDate = fromDate.plusWeeks(weekListSize);
        WorkCalendarDTO workCalendar = new WorkCalendarDTO("1", fromDate.toString(), toDate.toString());

        List<CrewDTO> crews = new ArrayList<>();
        crews.add(new CrewDTO("1", "Alpha crew"));
        crews.add(new CrewDTO("2", "Beta crew"));
        crews.add(new CrewDTO("3", "Gamma crew"));

        String[] areas = { "Downtown", "Uptown", "Park", "Airport", "Bay", "Hill" };
        List<JobDTO> jobs = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            String area = areas[i % areas.length];
            int durationInDays = 1 + (i % 3);
            LocalDate minStartDate = Job.calculateEndDate(fromDate, i % 5);
            LocalDate maxEndDate = Job.calculateEndDate(minStartDate, durationInDays + 10);
            LocalDate idealEndDate = Job.calculateEndDate(minStartDate, durationInDays + 3);
            jobs.add(new JobDTO(Integer.toString(i), area + " Street", durationInDays,
                    minStartDate.toString(), maxEndDate.toString(), idealEndDate.toString(),
                    List.of(area), "", "", ""));
        }

        return new MaintenanceScheduleInput(workCalendar, crews, jobs);
    }
}
