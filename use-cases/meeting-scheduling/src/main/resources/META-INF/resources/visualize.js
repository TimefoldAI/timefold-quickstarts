// index.template.html has a single #visualization slot; this fills it with the
// demo's own markup via setVisualizationSlot(), then owns rendering the
// schedule into that markup for the rest of the page's lifetime.

const MINUTES_PER_DAY = 24 * 60;

// Rooms get a fixed color regardless of encounter order, so a meeting keeps its color
// when the solver moves it and when the view switches between rooms and people.
const ROOM_SEED_COLORS = new Map()
    .set("R1", {bg: "#009E73", fg: "#FFFFFF"})
    .set("R2", {bg: "#0072B2", fg: "#FFFFFF"})
    .set("R3", {bg: "#E69F00", fg: "#FFFFFF"});

const TIMELINE_OPTIONS = {
    timeAxis: {scale: "hour", step: 1},
    orientation: {axis: "top"},
    stack: false,
    xss: {disabled: true}, // Items are XSS safe through JQuery
    showCurrentTime: false,
    zoomMin: 1000 * 60 * 60, // One hour in milliseconds
    zoomMax: 5 * 1000 * 60 * 60 * 24, // Five days in milliseconds
    // Office hours are sent in UTC, so the axis is drawn in UTC as well. Left to the viewer's
    // own zone, an 08:00-18:00 office day would read as 10:00-20:00 in UTC+2.
    moment: (date) => vis.moment(date).utc(),
};

function escapeHtml(value) {
    return $('<div/>').text(value ?? '').html();
}

function isAssigned(meeting) {
    return meeting.roomId != null && meeting.startDateTime != null;
}

// The input only submits the office hours of each day, so evenings and nights are not
// schedulable at all. Folding them away keeps a multi-day schedule readable instead of
// showing mostly empty night. Formatted the way vis-timeline wants its hiddenDates: as
// instants with an explicit UTC offset, since vis-timeline parses them through the moment
// of TIMELINE_OPTIONS, and moment reads a zone-less "2026-09-07 18:00:00" in the viewer's
// own zone, which would fold away the last hours of every office day in UTC+2.
function outsideOfficeHoursGap(days) {
    const utcDateTime = (dateTime) => JSJoda.OffsetDateTime.parse(dateTime)
        .withOffsetSameInstant(JSJoda.ZoneOffset.UTC);
    const starts = days.map((day) => utcDateTime(day.startDateTime));
    const minuteOfDay = (dateTime) => dateTime.hour() * 60 + dateTime.minute();
    const firstMinute = Math.min(...starts.map(minuteOfDay));
    const lastMinute = Math.min(MINUTES_PER_DAY, Math.max(...days.map((day, index) => {
        const endMinute = minuteOfDay(utcDateTime(day.endDateTime));
        // Office hours that run up to (or past) midnight land on the next calendar day.
        return endMinute <= minuteOfDay(starts[index]) ? endMinute + MINUTES_PER_DAY : endMinute;
    })));
    if (firstMinute === 0 && lastMinute >= MINUTES_PER_DAY) {
        return []; // The office is open around the clock, so there is nothing to fold away.
    }
    // One daily gap, anchored on the midnight of the earliest office day: from the evening of
    // one day to the morning of the next. plusMinutes() rolls a gap that starts at midnight over
    // to the next day by itself.
    const midnight = starts.reduce((earliest, start) => start.isBefore(earliest) ? start : earliest)
        .truncatedTo(JSJoda.ChronoUnit.DAYS);
    return [{
        start: midnight.plusMinutes(lastMinute).toString(),
        end: midnight.plusDays(1).plusMinutes(firstMinute).toString(),
        repeat: 'daily',
    }];
}

const app = {
    start() {
        setVisualizationSlot(`
    <div class="mb-2 d-flex justify-content-end">
        <ul class="nav nav-pills" role="tablist">
            <li class="nav-item" role="presentation">
                <button class="nav-link active" id="byPersonTab" data-bs-toggle="tab"
                        data-bs-target="#byPersonPanel" type="button" role="tab" aria-controls="byPersonPanel"
                        aria-selected="true">By person
                </button>
            </li>
            <li class="nav-item" role="presentation">
                <button class="nav-link" id="byRoomTab" data-bs-toggle="tab"
                        data-bs-target="#byRoomPanel" type="button" role="tab" aria-controls="byRoomPanel"
                        aria-selected="false">By room
                </button>
            </li>

        </ul>
    </div>
    <div class="tab-content">
        <div class="tab-pane fade show active" id="byPersonPanel" role="tabpanel" aria-labelledby="byPersonTab"></div>
        <div class="tab-pane fade" id="byRoomPanel" role="tabpanel" aria-labelledby="byRoomTab"></div>
    </div>

    <h2 class="my-4">Unassigned meetings</h2>
    <div id="unassignedMeetings" class="row row-cols-3 g-3 mb-4"></div>
`);

        this.focusedTimelineDays = new Map();

        this.byRoomGroupData = new vis.DataSet();
        this.byRoomItemData = new vis.DataSet();
        this.byRoomTimeline = new vis.Timeline(document.getElementById("byRoomPanel"), this.byRoomItemData,
            this.byRoomGroupData, TIMELINE_OPTIONS);

        this.byPersonGroupData = new vis.DataSet();
        this.byPersonItemData = new vis.DataSet();
        this.byPersonTimeline = new vis.Timeline(document.getElementById("byPersonPanel"), this.byPersonItemData,
            this.byPersonGroupData, TIMELINE_OPTIONS);

        document.getElementById("byRoomTab").addEventListener('click', () => {
            this.byRoomTimeline.redraw();
            this.quickstartPage.changeRenderer((schedule) => this.renderScheduleByRoom(schedule));
        });
        document.getElementById("byPersonTab").addEventListener('click', () => {
            this.byPersonTimeline.redraw();
            this.quickstartPage.changeRenderer((schedule) => this.renderScheduleByPerson(schedule));
        });

        this.quickstartPage = new QuickstartPage({
            modelPath: '/v1/schedules',
            renderSchedule: (schedule) => this.renderScheduleByPerson(schedule),
            renderInfo: (schedule) => this.renderInfo(schedule),
            mergeModelOutput: (schedule, modelOutput) => this.mergeModelOutput(schedule, modelOutput),
        });
    },

    // modelOutput only carries the assignments (meetings: [{id, roomId, startDateTime, endDateTime}]), not the
    // full problem, so schedule (the QuickstartPage's loadedSchedule) keeps the full modelInput
    // (people, rooms, office hours, meeting details) and this only overlays the room and start.
    mergeModelOutput(schedule, modelOutput) {
        if (schedule == null) {
            return;
        }
        if (modelOutput != null && modelOutput.meetings != null) {
            const assignmentByMeetingId = new Map(modelOutput.meetings.map((meeting) => [meeting.id, meeting]));
            schedule.meetings = schedule.meetings.map((meeting) => {
                const assignment = assignmentByMeetingId.get(meeting.id);
                return assignment == null ? meeting : {
                    ...meeting,
                    roomId: assignment.roomId,
                    startDateTime: assignment.startDateTime,
                    endDateTime: assignment.endDateTime,
                };
            });
        }
    },

    renderInfo(schedule) {
        if (schedule == null) {
            return "";
        }
        const days = schedule.timeConfiguration.days.length;
        const granularity = schedule.timeConfiguration.granularityInMinutes;
        return `${schedule.meetings.length} meetings · ${schedule.people.length} people`
            + ` · ${schedule.rooms.length} rooms · ${days} office days of ${granularity} minute slots`;
    },

    // Both views draw the same meetings, so they share the lookups and the per-meeting time span.
    prepare(schedule) {
        resetColorMap(ROOM_SEED_COLORS);
        const roomById = new Map(schedule.rooms.map((room) => [room.id, room]));
        // The output states when a meeting ends; an input dataset that already carries assignments
        // only states its start, so that end is derived from the duration instead.
        const spanOf = (meeting) => {
            const start = JSJoda.OffsetDateTime.parse(meeting.startDateTime);
            const end = meeting.endDateTime == null
                ? start.plusMinutes(meeting.durationInMinutes)
                : JSJoda.OffsetDateTime.parse(meeting.endDateTime);
            return {start: start.toString(), end: end.toString()};
        };
        return {roomById, spanOf};
    },

    renderUnassignedMeetings(schedule) {
        const unassignedMeetings = $("#unassignedMeetings");
        unassignedMeetings.children().remove();
        schedule.meetings.filter((meeting) => !isAssigned(meeting)).forEach((meeting) => {
            const attendeeCount = meeting.requiredAttendeeIds.length + meeting.preferredAttendeeIds.length;
            const card = $(`<div class="card-body p-2"/>`)
                .append($(`<h5 class="card-title mb-1"/>`).text(meeting.topic))
                .append($(`<p class="card-text ms-2 mb-0"/>`).text(`${meeting.durationInMinutes / 60} hour(s)`))
                .append($(`<p class="card-text ms-2 mb-0"/>`).text(`${attendeeCount} attendee(s)`));
            unassignedMeetings.append($(`<div class="col"/>`).append($(`<div class="card"/>`).append(card)));
        });
        if (unassignedMeetings.children().length === 0) {
            unassignedMeetings.append($(`<div class="col-12"/>`)
                .append($(`<div class="alert alert-success d-flex align-items-center justify-content-center" role="alert"/>`)
                    .append($(`<i class="fas fa-check-circle me-2"/>`))
                    .append($(`<span/>`).text("All meetings have been assigned!"))));
        }
    },

    // Fold away everything outside office hours and open on the office hours of the first day.
    // vis-timeline needs its hiddenDates up front, but the office hours are only known once a
    // schedule has been loaded, so both timelines get theirs on the first render. Office hours
    // only change when another dataset is loaded, so keying on them keeps the 2-second poll
    // during solving from yanking the window back from wherever the user has panned or zoomed to.
    focus(timeline, schedule) {
        const days = schedule.timeConfiguration.days;
        const daysKey = JSON.stringify(days);
        if (this.focusedTimelineDays.get(timeline) === daysKey) {
            return;
        }
        this.focusedTimelineDays.set(timeline, daysKey);
        timeline.setOptions({hiddenDates: outsideOfficeHoursGap(days)});
        const firstDay = days.reduce((earliest, day) => JSJoda.OffsetDateTime.parse(day.startDateTime)
            .isBefore(JSJoda.OffsetDateTime.parse(earliest.startDateTime)) ? day : earliest);
        timeline.setWindow(firstDay.startDateTime, firstDay.endDateTime);
    },

    renderScheduleByRoom(schedule) {
        const {spanOf} = this.prepare(schedule);
        this.byRoomGroupData.clear();
        this.byRoomItemData.clear();

        schedule.rooms.forEach((room) => this.byRoomGroupData.add({
            id: room.id,
            content: `<div class="d-flex flex-column"><h5 class="card-title mb-1">${escapeHtml(room.name)}</h5>`
                + `<small class="text-muted">${room.capacity} seats</small></div>`,
        }));

        schedule.meetings.filter(isAssigned).forEach((meeting) => {
            const color = pickColor(meeting.roomId);
            const attendeeCount = meeting.requiredAttendeeIds.length + meeting.preferredAttendeeIds.length;
            const content = $("<div/>")
                .append($(`<div class="d-flex justify-content-center"/>`)
                    .append($(`<h5 class="card-title mb-1"/>`).text(meeting.topic)))
                .append($(`<div class="d-flex justify-content-center"/>`)
                    .append($(`<small/>`).text(`${attendeeCount} attendee(s)`)));
            const {start, end} = spanOf(meeting);
            this.byRoomItemData.add({
                id: meeting.id,
                group: meeting.roomId,
                content: content.html(),
                start: start,
                end: end,
                style: `min-height: 50px; background-color: ${color.bg}; color: ${color.fg}`,
            });
        });

        this.renderUnassignedMeetings(schedule);
        this.focus(this.byRoomTimeline, schedule);
    },

    renderScheduleByPerson(schedule) {
        const {roomById, spanOf} = this.prepare(schedule);
        this.byPersonGroupData.clear();
        this.byPersonItemData.clear();

        schedule.people.forEach((person) => this.byPersonGroupData.add({
            id: person.id,
            content: `<div class="d-flex flex-column"><h5 class="card-title mb-1">${escapeHtml(person.fullName)}</h5></div>`,
        }));

        schedule.meetings.filter(isAssigned).forEach((meeting) => {
            const color = pickColor(meeting.roomId);
            const room = roomById.get(meeting.roomId);
            const {start, end} = spanOf(meeting);
            const addAttendance = (personId, required) => {
                const content = $("<div/>")
                    .append($(`<div class="d-flex justify-content-center"/>`)
                        .append($(`<h5 class="card-title mb-1"/>`).text(meeting.topic)))
                    .append($(`<div class="d-flex justify-content-center"/>`)
                        .append($(`<span class="badge m-1 ${required ? 'bg-primary' : 'bg-secondary'}"/>`)
                            .text(required ? "Required" : "Preferred"))
                        .append($(`<span class="badge bg-light text-dark m-1"/>`)
                            .text(room == null ? meeting.roomId : room.name)));
                this.byPersonItemData.add({
                    id: `${meeting.id}-${personId}`,
                    group: personId,
                    content: content.html(),
                    start: start,
                    end: end,
                    style: `min-height: 50px; background-color: ${color.bg}; color: ${color.fg}`,
                });
            };
            meeting.requiredAttendeeIds.forEach((personId) => addAttendance(personId, true));
            meeting.preferredAttendeeIds
                .filter((personId) => !meeting.requiredAttendeeIds.includes(personId))
                .forEach((personId) => addAttendance(personId, false));
        });

        this.renderUnassignedMeetings(schedule);
        this.focus(this.byPersonTimeline, schedule);
    },
};

app.start();
