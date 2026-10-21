// index.template.html has a single #visualization slot; this fills it with the
// demo's own markup via setVisualizationSlot(), then owns rendering the
// schedule into that markup for the rest of the page's lifetime.

const UNAVAILABLE_COLOR = '#ef2929'; // Tango Scarlet Red
const UNDESIRED_COLOR = '#f57900'; // Tango Orange
const DESIRED_COLOR = '#73d216'; // Tango Chameleon
const NEUTRAL_COLOR = '#729fcf'; // Tango Sky Blue
const UNASSIGNED_COLOR = '#EF292999';
const MISSING_SKILL_COLOR = '#ef2929';
const MATCHING_SKILL_COLOR = '#8ae234';

// The employee's dates are calendar days in the offset of the shifts, so the timeline shows every shift at its
// wall-clock time in that offset (rather than converted to the browser's time zone) to keep both aligned.
function wallClock(offsetDateTime) {
    return JSJoda.OffsetDateTime.parse(offsetDateTime).toLocalDateTime();
}

// The dates a shift touches: the day it starts on, plus the day it ends on unless it ends at midnight.
function shiftDates(shift) {
    const start = wallClock(shift.start);
    const end = wallClock(shift.end);
    const dates = [start.toLocalDate().toString()];
    if (end.isAfter(start.toLocalDate().plusDays(1).atStartOfDay())) {
        dates.push(end.toLocalDate().toString());
    }
    return dates;
}

function shiftColor(shift, employee) {
    const dates = shiftDates(shift);
    const touches = (employeeDates) => dates.some(date => employeeDates.includes(date));
    if (touches(employee.unavailableDates)) {
        return UNAVAILABLE_COLOR;
    } else if (touches(employee.undesiredDates)) {
        return UNDESIRED_COLOR;
    } else if (touches(employee.desiredDates)) {
        return DESIRED_COLOR;
    }
    return NEUTRAL_COLOR;
}

function skillBadge(skill, color) {
    return $(`<span class="badge me-1 mt-1"/>`).css("background-color", color).text(skill);
}

function cardHtml(title, ...badges) {
    return $('<div/>').append($('<div class="card-body p-2"/>')
        .append($(`<h5 class="card-title mb-2"/>`).text(title))
        .append($('<div/>').append(badges))).html();
}

const app = {
    start() {
        setVisualizationSlot(`
    <div class="mb-2 d-flex justify-content-end">
        <ul class="nav nav-pills" role="tablist">
            <li class="nav-item" role="presentation">
                <button class="nav-link active" id="byEmployeeTab" data-bs-toggle="tab"
                        data-bs-target="#byEmployeePanel" type="button" role="tab" aria-controls="byEmployeePanel"
                        aria-selected="true">By employee
                </button>
            </li>
            <li class="nav-item" role="presentation">
                <button class="nav-link" id="byLocationTab" data-bs-toggle="tab"
                        data-bs-target="#byLocationPanel" type="button" role="tab" aria-controls="byLocationPanel"
                        aria-selected="false">By location
                </button>
            </li>
        </ul>
    </div>
    <div class="tab-content">
        <div class="tab-pane fade show active" id="byEmployeePanel" role="tabpanel" aria-labelledby="byEmployeeTab">
        </div>
        <div class="tab-pane fade" id="byLocationPanel" role="tabpanel" aria-labelledby="byLocationTab">
        </div>
    </div>
`);

        const timelineOptions = {
            timeAxis: {scale: "hour", step: 6},
            orientation: {axis: "top"},
            xss: {disabled: true}, // Items are XSS safe through JQuery
            zoomMin: 2 * 1000 * 60 * 60 * 24, // Two days in milliseconds
            zoomMax: 4 * 7 * 1000 * 60 * 60 * 24, // Four weeks in milliseconds
        };

        this.byEmployeeGroupData = new vis.DataSet();
        this.byEmployeeItemData = new vis.DataSet();
        this.byEmployeeTimeline = new vis.Timeline(document.getElementById("byEmployeePanel"),
            this.byEmployeeItemData, this.byEmployeeGroupData, {...timelineOptions, stack: false});

        this.byLocationGroupData = new vis.DataSet();
        this.byLocationItemData = new vis.DataSet();
        this.byLocationTimeline = new vis.Timeline(document.getElementById("byLocationPanel"),
            this.byLocationItemData, this.byLocationGroupData, timelineOptions);

        // vis-timeline can't measure itself inside a hidden tab, so redraw it once its tab is shown.
        document.getElementById("byEmployeeTab").addEventListener('shown.bs.tab', () => this.byEmployeeTimeline.redraw());
        document.getElementById("byLocationTab").addEventListener('shown.bs.tab', () => this.byLocationTimeline.redraw());

        this.quickstartPage = new QuickstartPage({
            modelPath: '/v1/schedules',
            renderSchedule: (schedule) => this.renderSchedule(schedule),
            renderInfo: (schedule) => this.renderInfo(schedule),
            mergeModelOutput: (schedule, modelOutput) => this.mergeModelOutput(schedule, modelOutput),
        });
    },

    // modelOutput only carries the shift assignments (shifts: [{id, employeeName}]), not the full problem, so
    // schedule (the QuickstartPage's loadedSchedule) keeps the full modelInput (employees + shift details) and
    // this only overlays the employeeName per shift.
    mergeModelOutput(schedule, modelOutput) {
        if (schedule == null) {
            return;
        }
        if (modelOutput != null && modelOutput.shifts != null) {
            const employeeNameByShiftId = new Map(modelOutput.shifts.map(shift => [shift.id, shift.employeeName]));
            schedule.shifts = schedule.shifts.map(shift => employeeNameByShiftId.has(shift.id)
                ? {...shift, employeeName: employeeNameByShiftId.get(shift.id)}
                : shift);
        }
    },

    renderInfo(schedule) {
        if (schedule == null) {
            return "";
        }
        return `${schedule.shifts.length} shifts · ${schedule.employees.length} employees`;
    },

    renderSchedule(schedule) {
        const employees = new Map(schedule.employees.map(employee => [employee.name, employee]));
        this.byEmployeeGroupData.clear();
        this.byEmployeeItemData.clear();
        this.byLocationGroupData.clear();
        this.byLocationItemData.clear();

        schedule.employees.forEach(employee => this.renderEmployee(employee));

        const locations = [...new Set(schedule.shifts.map(shift => shift.location))];
        locations.forEach(location => this.byLocationGroupData.add({id: location, content: $('<div/>').text(location).html()}));

        schedule.shifts.forEach(shift => {
            const start = wallClock(shift.start).toString();
            const end = wallClock(shift.end).toString();
            const employee = shift.employeeName == null ? null : employees.get(shift.employeeName);
            if (employee == null) {
                this.byLocationItemData.add({
                    id: shift.id, group: shift.location,
                    content: cardHtml("Unassigned", skillBadge(shift.requiredSkill, "#d3d7cf")),
                    start, end,
                    style: `background-color: ${UNASSIGNED_COLOR}`,
                });
                return;
            }
            const badge = () => skillBadge(shift.requiredSkill,
                employee.skills.includes(shift.requiredSkill) ? MATCHING_SKILL_COLOR : MISSING_SKILL_COLOR);
            const style = `background-color: ${shiftColor(shift, employee)}`;
            this.byEmployeeItemData.add({
                id: shift.id, group: employee.name,
                content: cardHtml(shift.location, badge()),
                start, end, style,
            });
            this.byLocationItemData.add({
                id: shift.id, group: shift.location,
                content: cardHtml(employee.name, badge()),
                start, end, style,
            });
        });

        this.setWindow(schedule);
    },

    renderEmployee(employee) {
        this.byEmployeeGroupData.add({
            id: employee.name,
            content: cardHtml(employee.name, ...employee.skills.map(skill => skillBadge(skill, "#d3d7cf"))),
        });
        const dateBands = [
            {dates: employee.unavailableDates, label: "Unavailable", color: UNAVAILABLE_COLOR},
            {dates: employee.undesiredDates, label: "Undesired", color: UNDESIRED_COLOR},
            {dates: employee.desiredDates, label: "Desired", color: DESIRED_COLOR},
        ];
        dateBands.forEach(({dates, label, color}) => dates.forEach(rawDate => {
            const date = JSJoda.LocalDate.parse(rawDate);
            this.byEmployeeItemData.add({
                id: `${employee.name}-${label}-${rawDate}`, group: employee.name,
                content: $('<div/>').append($(`<h5 class="card-title mb-1"/>`).text(label)).html(),
                start: date.atStartOfDay().toString(),
                end: date.plusDays(1).atStartOfDay().toString(),
                type: "background",
                style: `opacity: 0.5; background-color: ${color}`,
            });
        }));
    },

    // Show the first week of the schedule; the rest is a scroll away.
    setWindow(schedule) {
        if (schedule.shifts.length === 0) {
            return;
        }
        const firstDay = schedule.shifts.map(shift => wallClock(shift.start).toLocalDate().toString()).sort()[0];
        const lastDay = JSJoda.LocalDate.parse(firstDay).plusDays(7).toString();
        this.byEmployeeTimeline.setWindow(firstDay, lastDay);
        this.byLocationTimeline.setWindow(firstDay, lastDay);
    },
};

app.start();
