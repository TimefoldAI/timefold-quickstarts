// index.template.html has a single #visualization slot; this fills it with the
// demo's own markup via setVisualizationSlot(), then owns rendering the
// schedule into that markup for the rest of the page's lifetime.

const SATURDAY = 6;
const SUNDAY = 7;

// Mirrors Job.calculateEndDate(): a job only progresses on workdays, so its end date
// (exclusive) is reached by adding `workdays` non-weekend days to its start date.
function addWorkdays(isoDate, workdays) {
    let result = JSJoda.LocalDate.parse(isoDate);
    let addedDays = 0;
    while (addedDays < workdays) {
        const dayOfWeek = result.dayOfWeek().value();
        if (dayOfWeek !== SATURDAY && dayOfWeek !== SUNDAY) {
            addedDays++;
        }
        result = result.plusDays(1);
    }
    return result.toString();
}

// Mirrors MaintenanceSchedule.createStartDateList(): only non-weekend days inside the
// work calendar are workdays the solver can schedule a job on.
function countWorkdays(workCalendar) {
    const toDate = JSJoda.LocalDate.parse(workCalendar.toDate);
    let date = JSJoda.LocalDate.parse(workCalendar.fromDate);
    let workdays = 0;
    while (date.isBefore(toDate)) {
        const dayOfWeek = date.dayOfWeek().value();
        if (dayOfWeek !== SATURDAY && dayOfWeek !== SUNDAY) {
            workdays++;
        }
        date = date.plusDays(1);
    }
    return workdays;
}

// The solver reports every job's end date, but a job that is not scheduled yet has none,
// so fall back to the earliest end date its own window allows.
function jobEndDate(job) {
    if (job.endDate != null) {
        return job.endDate;
    }
    return addWorkdays(job.startDate ?? job.minStartDate, job.durationInDays);
}

function isAssigned(job) {
    return job.crewId != null && job.startDate != null;
}

function isBefore(isoDate, otherIsoDate) {
    return JSJoda.LocalDate.parse(isoDate).isBefore(JSJoda.LocalDate.parse(otherIsoDate));
}

function tagBadges(element, job) {
    job.tags.forEach((tag) => {
        const color = pickColor(tag);
        element.append($(`<span class="badge me-1"/>`)
            .css({backgroundColor: color.bg, color: color.fg})
            .text(tag));
    });
}

function renderUnassignedBanner(unassignedJobs) {
    if (unassignedJobs.children().length === 0) {
        const banner = $(`<div class="col-12"/>`)
            .append($(`<div class="alert alert-success d-flex align-items-center justify-content-center" role="alert"/>`)
                .append($(`<i class="fas fa-check-circle me-2"/>`))
                .append($(`<span/>`).text("All jobs have been assigned!")));
        unassignedJobs.append(banner);
    }
}

const app = {
    start() {
        setVisualizationSlot(`
    <div class="mb-2 d-flex justify-content-end">
        <ul class="nav nav-pills" role="tablist">
            <li class="nav-item" role="presentation">
                <button class="nav-link active" id="byCrewTab" data-bs-toggle="tab"
                        data-bs-target="#byCrewPanel" type="button" role="tab" aria-controls="byCrewPanel"
                        aria-selected="true">By crew
                </button>
            </li>
            <li class="nav-item" role="presentation">
                <button class="nav-link" id="byJobTab" data-bs-toggle="tab"
                        data-bs-target="#byJobPanel" type="button" role="tab" aria-controls="byJobPanel"
                        aria-selected="false">By job
                </button>
            </li>
        </ul>
    </div>
    <div class="tab-content">
        <div class="tab-pane fade show active" id="byCrewPanel" role="tabpanel" aria-labelledby="byCrewTab">
        </div>
        <div class="tab-pane fade" id="byJobPanel" role="tabpanel" aria-labelledby="byJobTab">
        </div>
    </div>

    <h2 class="my-4">Unassigned jobs</h2>
    <div id="unassignedJobs" class="row row-cols-3 g-3 mb-4"></div>
`);

        const timelineOptions = {
            timeAxis: {scale: "day"},
            orientation: {axis: "top"},
            xss: {disabled: true}, // Items are XSS safe through JQuery
            zoomMin: 3 * 1000 * 60 * 60 * 24 // Three days in milliseconds
        };

        this.byCrewGroupData = new vis.DataSet();
        this.byCrewItemData = new vis.DataSet();
        this.byCrewTimeline = new vis.Timeline(document.getElementById("byCrewPanel"), this.byCrewItemData,
            this.byCrewGroupData, {...timelineOptions, stack: false});

        this.byJobGroupData = new vis.DataSet();
        this.byJobItemData = new vis.DataSet();
        this.byJobTimeline = new vis.Timeline(document.getElementById("byJobPanel"), this.byJobItemData,
            this.byJobGroupData, timelineOptions);

        // HACK to allow vis-timeline to work within Bootstrap tabs: a timeline in a hidden
        // tab pane has no width to lay itself out against, so it needs a redraw once shown.
        document.getElementById("byCrewTab").addEventListener('click', () => {
            this.byCrewTimeline.redraw();
            this.quickstartPage.changeRenderer((schedule) => this.renderScheduleByCrew(schedule));
        });
        document.getElementById("byJobTab").addEventListener('click', () => {
            this.byJobTimeline.redraw();
            this.quickstartPage.changeRenderer((schedule) => this.renderScheduleByJob(schedule));
        });

        this.quickstartPage = new QuickstartPage({
            modelPath: '/v1/schedules',
            renderSchedule: (schedule) => this.renderScheduleByCrew(schedule),
            renderInfo: (schedule) => this.renderInfo(schedule),
            mergeModelOutput: (schedule, modelOutput) => this.mergeModelOutput(schedule, modelOutput),
        });
    },

    // modelOutput only carries the assignments (jobs: [{id, crewId, startDate, endDate}]), not the
    // full problem, so schedule (the QuickstartPage's loadedSchedule) keeps the full modelInput
    // (work calendar, crews and job details) and this only overlays the assignment per job.
    mergeModelOutput(schedule, modelOutput) {
        if (schedule == null) {
            return;
        }
        if (modelOutput != null && modelOutput.jobs != null) {
            const assignmentByJobId = new Map(modelOutput.jobs.map(job => [job.id, job]));
            schedule.jobs = schedule.jobs.map(job => {
                const assignment = assignmentByJobId.get(job.id);
                return assignment == null ? job : {
                    ...job,
                    crewId: assignment.crewId,
                    startDate: assignment.startDate,
                    endDate: assignment.endDate,
                };
            });
        }
    },

    renderInfo(schedule) {
        if (schedule == null) {
            return "";
        }
        // The same three counts MaintenanceScheduleInputMetrics reports for the dataset.
        return `${schedule.jobs.length} jobs · ${schedule.crews.length} crews `
            + `· ${countWorkdays(schedule.workCalendar)} workdays`;
    },

    renderScheduleByCrew(schedule) {
        this.prepareRender(schedule);
        const crewNameById = new Map(schedule.crews.map(crew => [crew.id, crew.name]));

        this.byCrewGroupData.clear();
        this.byCrewItemData.clear();
        schedule.crews.forEach((crew) => this.byCrewGroupData.add({id: crew.id, content: crew.name}));

        schedule.jobs.filter(isAssigned).forEach((job) => {
            const jobElement = $(`<div/>`)
                .append($(`<h5 class="card-title mb-1"/>`).text(job.name))
                .append($(`<p class="card-text ms-2 mb-0"/>`).text(`${job.durationInDays} workdays`));
            this.appendWindowWarnings(jobElement, job);
            tagBadges(jobElement, job);
            this.byCrewItemData.add({
                id: job.id,
                group: job.crewId,
                content: jobElement.html(),
                title: crewNameById.get(job.crewId),
                start: job.startDate,
                end: jobEndDate(job)
            });
        });

        this.setWindow(this.byCrewTimeline, schedule);
    },

    renderScheduleByJob(schedule) {
        this.prepareRender(schedule);
        const crewNameById = new Map(schedule.crews.map(crew => [crew.id, crew.name]));

        this.byJobGroupData.clear();
        this.byJobItemData.clear();

        schedule.jobs.forEach((job) => {
            const jobGroupElement = $(`<div/>`)
                .append($(`<h5 class="card-title mb-1"/>`).text(job.name))
                .append($(`<p class="card-text ms-2 mb-0"/>`).text(`${job.durationInDays} workdays`));
            this.byJobGroupData.add({id: job.id, content: jobGroupElement.html()});
            // The green band runs up to the ideal end date, the orange one from there to the due date.
            this.byJobItemData.add({
                id: job.id + "_readyToIdealEnd", group: job.id,
                start: job.minStartDate, end: job.idealEndDate,
                type: "background",
                style: "background-color: #8AE23433"
            });
            this.byJobItemData.add({
                id: job.id + "_idealEndToDue", group: job.id,
                start: job.idealEndDate, end: job.maxEndDate,
                type: "background",
                style: "background-color: #FCAF3E33"
            });

            const assigned = isAssigned(job);
            const jobElement = $(`<div/>`)
                .append($(`<h5 class="card-title mb-1"/>`).text(assigned ? crewNameById.get(job.crewId) : "Unassigned"));
            this.appendWindowWarnings(jobElement, job);
            tagBadges(jobElement, job);
            this.byJobItemData.add({
                id: job.id,
                group: job.id,
                content: jobElement.html(),
                start: assigned ? job.startDate : job.minStartDate,
                end: jobEndDate(job),
                style: assigned ? undefined : "background-color: #EF292999"
            });
        });

        this.setWindow(this.byJobTimeline, schedule);
    },

    // Both renderers share the tag colors and the "Unassigned jobs" card list, which does
    // not depend on which timeline is on screen.
    prepareRender(schedule) {
        resetColorMap();
        const unassignedJobs = $("#unassignedJobs");
        unassignedJobs.children().remove();
        schedule.jobs.filter(job => !isAssigned(job)).forEach((job) => {
            const jobElement = $(`<div class="card-body p-2"/>`)
                .append($(`<h5 class="card-title mb-1"/>`).text(job.name))
                .append($(`<p class="card-text ms-2 mb-0"/>`).text(`${job.durationInDays} workdays`))
                .append($(`<p class="card-text ms-2 mb-0"/>`).text(`Ready: ${job.minStartDate}`))
                .append($(`<p class="card-text ms-2 mb-0"/>`).text(`Due: ${job.maxEndDate}`));
            tagBadges(jobElement, job);
            unassignedJobs.append($(`<div class="col"/>`).append($(`<div class="card"/>`).append(jobElement)));
        });
        renderUnassignedBanner(unassignedJobs);
    },

    appendWindowWarnings(element, job) {
        if (!isAssigned(job)) {
            return;
        }
        if (isBefore(job.startDate, job.minStartDate)) {
            element.append($(`<p class="badge text-bg-danger mb-0"/>`).text("Before ready (too early)"));
        }
        if (isBefore(job.maxEndDate, jobEndDate(job))) {
            element.append($(`<p class="badge text-bg-danger mb-0"/>`).text("After due (too late)"));
        }
    },

    setWindow(timeline, schedule) {
        timeline.setWindow(schedule.workCalendar.fromDate, schedule.workCalendar.toDate);
    },
};

app.start();
