// index.template.html has a single #visualization slot; this fills it with the
// demo's own markup via setVisualizationSlot(), then owns rendering the
// timetable into that markup for the rest of the page's lifetime.

const timeFormatter = JSJoda.DateTimeFormatter.ofPattern('HH:mm');
const DAYS_OF_WEEK = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"];

function compareTimeslots(t1, t2) {
    const dayDiff = DAYS_OF_WEEK.indexOf(t1.dayOfWeek) - DAYS_OF_WEEK.indexOf(t2.dayOfWeek);
    if (dayDiff !== 0) {
        return dayDiff;
    }
    return JSJoda.LocalTime.parse(t1.startTime).compareTo(JSJoda.LocalTime.parse(t2.startTime));
}

function formatTimeslot(timeslot) {
    const day = timeslot.dayOfWeek.charAt(0) + timeslot.dayOfWeek.slice(1).toLowerCase();
    const start = JSJoda.LocalTime.parse(timeslot.startTime).format(timeFormatter);
    const end = JSJoda.LocalTime.parse(timeslot.endTime).format(timeFormatter);
    return `${day} ${start} - ${end}`;
}

function isAssigned(lesson) {
    return lesson.timeslotId != null && lesson.roomId != null;
}

function distinctSorted(values) {
    return [...new Set(values)].sort();
}

const app = {
    start() {
        setVisualizationSlot(`
    <div class="mb-2 d-flex justify-content-end">
        <ul class="nav nav-pills" role="tablist">
            <li class="nav-item" role="presentation">
                <button class="nav-link active" id="byRoomTab" data-bs-toggle="tab" type="button" role="tab"
                        aria-selected="true">By room
                </button>
            </li>
            <li class="nav-item" role="presentation">
                <button class="nav-link" id="byTeacherTab" data-bs-toggle="tab" type="button" role="tab"
                        aria-selected="false">By teacher
                </button>
            </li>
            <li class="nav-item" role="presentation">
                <button class="nav-link" id="byStudentGroupTab" data-bs-toggle="tab" type="button" role="tab"
                        aria-selected="false">By student group
                </button>
            </li>
        </ul>
    </div>
    <table class="table table-borderless table-striped" id="timetable">
        <!-- Filled in by visualize.js -->
    </table>

    <h2 class="my-4">Unassigned lessons</h2>
    <div id="unassignedLessons" class="row row-cols-4 g-3 mb-4"></div>
`);

        // Lookup map rebuilt on every render (the DTO flattens references to IDs).
        this.roomById = new Map();

        // Every view is the same grid of timeslots against one property of a lesson; only the
        // column header and the property differ.
        const views = {
            byRoomTab: {
                columns: schedule => schedule.rooms.map(room => ({key: room.id, label: room.name})),
                columnKey: lesson => lesson.roomId,
            },
            byTeacherTab: {
                columns: schedule => distinctSorted(schedule.lessons.map(lesson => lesson.teacher))
                    .map(teacher => ({key: teacher, label: teacher})),
                columnKey: lesson => lesson.teacher,
            },
            byStudentGroupTab: {
                columns: schedule => distinctSorted(schedule.lessons.map(lesson => lesson.studentGroup))
                    .map(studentGroup => ({key: studentGroup, label: studentGroup})),
                columnKey: lesson => lesson.studentGroup,
            },
        };
        for (const [tabId, view] of Object.entries(views)) {
            document.getElementById(tabId).addEventListener('click', () => {
                this.quickstartPage.changeRenderer((schedule) => this.renderTimetable(schedule, view));
            });
        }

        this.quickstartPage = new QuickstartPage({
            modelPath: '/v1/timetables',
            renderSchedule: (schedule) => this.renderTimetable(schedule, views.byRoomTab),
            renderInfo: (schedule) => this.renderInfo(schedule),
            mergeModelOutput: (schedule, modelOutput) => this.mergeModelOutput(schedule, modelOutput),
        });
    },

    // modelOutput only carries the lesson assignments (lessons: [{id, timeslotId, roomId}]), not the
    // full problem, so schedule (the QuickstartPage's loadedSchedule) keeps the full modelInput
    // (timeslots, rooms, lesson details) and this only overlays the timeslotId/roomId per lesson.
    mergeModelOutput(schedule, modelOutput) {
        if (schedule == null) {
            return;
        }
        if (modelOutput != null && modelOutput.lessons != null) {
            const assignmentById = new Map(modelOutput.lessons.map(lesson => [lesson.id, lesson]));
            schedule.lessons = schedule.lessons.map(lesson => assignmentById.has(lesson.id)
                ? {...lesson, timeslotId: assignmentById.get(lesson.id).timeslotId, roomId: assignmentById.get(lesson.id).roomId}
                : lesson);
        }
    },

    renderInfo(schedule) {
        if (schedule == null) {
            return "";
        }
        return `${schedule.lessons.length} lessons · ${schedule.rooms.length} rooms · ${schedule.timeslots.length} timeslots`;
    },

    renderTimetable(schedule, view) {
        this.roomById = new Map((schedule.rooms || []).map(room => [room.id, room]));
        resetColorMap();

        const timetable = $("#timetable");
        timetable.children().remove();
        const unassignedLessons = $("#unassignedLessons");
        unassignedLessons.children().remove();

        const columns = view.columns(schedule);
        const headerRow = $("<tr>").appendTo($("<thead>").appendTo(timetable));
        headerRow.append($("<th/>").text("Timeslot"));
        for (const column of columns) {
            headerRow.append($("<th/>").text(column.label));
        }

        // Cells are looked up by (timeslot, column) rather than by a generated element id, since
        // teacher and student group names are free text.
        const cells = new Map();
        const tbody = $("<tbody>").appendTo(timetable);
        for (const timeslot of [...schedule.timeslots].sort(compareTimeslots)) {
            const row = $("<tr>").appendTo(tbody);
            row.append($(`<th class="align-middle text-nowrap"/>`).text(formatTimeslot(timeslot)));
            for (const column of columns) {
                cells.set(`${timeslot.id}\u0000${column.key}`, $("<td/>").appendTo(row));
            }
        }

        for (const lesson of schedule.lessons) {
            const lessonElement = this.renderLesson(lesson);
            if (isAssigned(lesson)) {
                cells.get(`${lesson.timeslotId}\u0000${view.columnKey(lesson)}`)?.append(lessonElement);
            } else {
                unassignedLessons.append($(`<div class="col"/>`).append(lessonElement));
            }
        }

        if (unassignedLessons.children().length === 0) {
            unassignedLessons.append($(`<div class="col-12"/>`)
                .append($(`<div class="alert alert-success d-flex align-items-center justify-content-center" role="alert"/>`)
                    .append($(`<i class="fas fa-check-circle me-2"/>`))
                    .append($(`<span/>`).text("All lessons have been assigned!"))));
        }
    },

    renderLesson(lesson) {
        const color = pickColor(lesson.subject);
        const room = this.roomById.get(lesson.roomId);
        return $(`<div class="card mb-1" style="background-color: ${color.bg};color: ${color.fg}"/>`)
            .append($(`<div class="card-body p-2"/>`)
                .append($(`<h5 class="card-title mb-1"/>`).text(lesson.subject))
                .append($(`<p class="card-text ms-2 mb-1"/>`).append($(`<em/>`).text(`by ${lesson.teacher}`)))
                .append($(`<p class="card-text ms-2 mb-0"/>`).text(lesson.studentGroup))
                .append($(`<small class="ms-2 card-text"/>`).text(room ? room.name : `#${lesson.id}`)));
    },
};

app.start();
