// index.template.html has a single #visualization slot; this fills it with the
// demo's own markup via setVisualizationSlot(), then owns rendering the
// schedule into that markup for the rest of the page's lifetime.
const app = {
    start() {
        setVisualizationSlot(`
    <div class="mb-2 d-flex justify-content-end">
        <ul class="nav nav-pills" role="tablist">
            <li class="nav-item" role="presentation">
                <button class="nav-link active" id="byLineTab" data-bs-toggle="tab"
                        data-bs-target="#byLinePanel" type="button" role="tab" aria-controls="byLinePanel"
                        aria-selected="true">By line
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
        <div class="tab-pane fade show active" id="byLinePanel" role="tabpanel" aria-labelledby="byLineTab">
        </div>
        <div class="tab-pane fade" id="byJobPanel" role="tabpanel" aria-labelledby="byJobTab">
        </div>
    </div>

    <h2 class="my-4">Unassigned jobs</h2>
    <div id="unassignedJobs" class="row row-cols-3 g-3 mb-4"></div>

    <h2 class="my-4">Unassigned operators</h2>
    <div id="unassignedOperators" class="row row-cols-3 g-3 mb-4"></div>
`);

        const timelineOptions = {
            timeAxis: {scale: "hour"},
            orientation: {axis: "top"},
            stack: false,
            xss: {disabled: true}, // Items are XSS safe through JQuery
            zoomMin: 1000 * 60 * 60 * 12 // Half a day in milliseconds
        };

        this.byLineGroupData = new vis.DataSet();
        this.byLineItemData = new vis.DataSet();
        this.byLineTimeline = new vis.Timeline(document.getElementById("byLinePanel"), this.byLineItemData,
            this.byLineGroupData, timelineOptions);

        this.byJobGroupData = new vis.DataSet();
        this.byJobItemData = new vis.DataSet();
        this.byJobTimeline = new vis.Timeline(document.getElementById("byJobPanel"), this.byJobItemData,
            this.byJobGroupData, timelineOptions);

        // HACK to allow vis-timeline to work within Bootstrap tabs: a timeline in a hidden tab pane has
        // no width to lay itself out in, so it has to be redrawn once its pane becomes visible.
        document.getElementById("byLineTab").addEventListener('click', () => this.byLineTimeline.redraw());
        document.getElementById("byJobTab").addEventListener('click', () => this.byJobTimeline.redraw());

        this.quickstartPage = new QuickstartPage({
            modelPath: '/v1/schedules',
            renderSchedule: (schedule) => this.renderSchedule(schedule),
            renderInfo: (schedule) => this.renderInfo(schedule),
            mergeModelOutput: (schedule, modelOutput) => this.mergeModelOutput(schedule, modelOutput),
        });
    },

    // modelOutput only carries the assignments (which operator and jobs a line got, and the times that
    // follow from a job's place in its line's sequence), not the full problem, so schedule (the
    // QuickstartPage's loadedSchedule) keeps the full modelInput and this only overlays those.
    mergeModelOutput(schedule, modelOutput) {
        if (schedule == null || modelOutput == null) {
            return;
        }
        if (modelOutput.lines != null) {
            const solvedLines = new Map(modelOutput.lines.map(line => [line.id, line]));
            schedule.lines = schedule.lines.map(line => solvedLines.has(line.id)
                ? {...line, ...solvedLines.get(line.id)}
                : line);
        }
        if (modelOutput.jobs != null) {
            const solvedJobs = new Map(modelOutput.jobs.map(job => [job.id, job]));
            schedule.jobs = schedule.jobs.map(job => solvedJobs.has(job.id)
                ? {...job, ...solvedJobs.get(job.id)}
                : job);
        }
    },

    renderInfo(schedule) {
        if (schedule == null) {
            return "";
        }
        return `${schedule.jobs.length} jobs · ${schedule.products.length} products`
            + ` · ${schedule.lines.length} lines · ${schedule.operators.length} operators`;
    },

    renderSchedule(schedule) {
        const unassignedJobs = $("#unassignedJobs");
        const unassignedOperators = $("#unassignedOperators");
        unassignedJobs.children().remove();
        unassignedOperators.children().remove();
        this.byLineGroupData.clear();
        this.byLineItemData.clear();
        this.byJobGroupData.clear();
        this.byJobItemData.clear();

        const operatorsById = new Map(schedule.operators.map(operator => [operator.id, operator]));
        const linesById = new Map(schedule.lines.map(line => [line.id, line]));

        $.each(schedule.lines, (_, line) => {
            const operator = operatorsById.get(line.operatorId);
            const lineGroupElement = $(`<div/>`)
                .append($(`<h5 class="card-title mb-1"/>`).text(line.name))
                .append($(`<p class="card-text ms-2 mb-0"/>`).text(operator == null ? "No operator" : operator.name));
            this.byLineGroupData.add({id: line.id, content: lineGroupElement.html()});
        });

        $.each(schedule.jobs, (_, job) => {
            this.byJobGroupData.add({id: job.id, content: $(`<div/>`).text(job.name).html()});
            // The bands behind a job show the time it should ideally be finished in (green) and the time it
            // may still be finished in (orange).
            this.byJobItemData.add({
                id: job.id + "_minStartToIdealEnd", group: job.id,
                start: job.minStartTime,
                end: job.idealEndTime,
                type: "background",
                style: "background-color: #8AE23433"
            });
            this.byJobItemData.add({
                id: job.id + "_idealEndToMaxEnd", group: job.id,
                start: job.idealEndTime,
                end: job.maxEndTime,
                type: "background",
                style: "background-color: #FCAF3E33"
            });

            if (job.lineId == null || job.startProductionDateTime == null || job.endDateTime == null) {
                this.renderUnassignedJob(unassignedJobs, job);
                return;
            }
            this.renderScheduledJob(job, linesById.get(job.lineId));
        });

        const assignedOperatorIds = new Set(schedule.lines.map(line => line.operatorId).filter(id => id != null));
        $.each(schedule.operators, (_, operator) => {
            if (assignedOperatorIds.has(operator.id)) {
                return;
            }
            unassignedOperators.append($(`<div class="col"/>`).append($(`<div class="card"/>`)
                .append($(`<div class="card-body p-2"/>`)
                    .append($(`<h5 class="card-title mb-1"/>`).text(operator.name)))));
        });

        this.renderEmptyBanner(unassignedJobs, "All jobs have been assigned to a line!");
        this.renderEmptyBanner(unassignedOperators, "All operators are running a line.");

        const firstDay = schedule.workCalendar.fromDate;
        const secondDay = JSJoda.LocalDate.parse(firstDay).plusDays(1).toString();
        this.byLineTimeline.setWindow(firstDay, secondDay);
        this.byJobTimeline.setWindow(firstDay, secondDay);
    },

    renderUnassignedJob(unassignedJobs, job) {
        const durationMinutes = job.durationMinutes;
        const unassignedJobElement = $(`<div class="card-body p-2"/>`)
            .append($(`<h5 class="card-title mb-1"/>`).text(job.name))
            .append($(`<p class="card-text ms-2 mb-0"/>`)
                .text(`${Math.floor(durationMinutes / 60)} hours ${durationMinutes % 60} mins`))
            .append($(`<p class="card-text ms-2 mb-0"/>`).text(`Min start: ${formatDateTime(job.minStartTime)}`))
            .append($(`<p class="card-text ms-2 mb-0"/>`).text(`Ideal end: ${formatDateTime(job.idealEndTime)}`))
            .append($(`<p class="card-text ms-2 mb-0"/>`).text(`Max end: ${formatDateTime(job.maxEndTime)}`));
        unassignedJobs.append($(`<div class="col"/>`).append($(`<div class="card"/>`).append(unassignedJobElement)));

        this.byJobItemData.add({
            id: job.id, group: job.id,
            content: $(`<div/>`).append($(`<h5 class="card-title mb-1"/>`).text("Unassigned")).html(),
            start: job.minStartTime,
            end: JSJoda.OffsetDateTime.parse(job.minStartTime)
                .plus(JSJoda.Duration.ofMinutes(job.durationMinutes)).toString(),
            style: "background-color: #EF292999"
        });
    },

    renderScheduledJob(job, line) {
        const tooEarly = JSJoda.OffsetDateTime.parse(job.startProductionDateTime)
            .isBefore(JSJoda.OffsetDateTime.parse(job.minStartTime));
        const tooLate = JSJoda.OffsetDateTime.parse(job.endDateTime)
            .isAfter(JSJoda.OffsetDateTime.parse(job.maxEndTime));
        const byLineJobElement = $(`<div/>`).append($(`<p class="card-text"/>`).text(job.name));
        const byJobJobElement = $(`<div/>`).append($(`<p class="card-text"/>`).text(line == null ? "" : line.name));
        if (tooEarly) {
            byLineJobElement.append($(`<p class="badge text-bg-danger mb-0"/>`).text("Before min start (too early)"));
            byJobJobElement.append($(`<p class="badge text-bg-danger mb-0"/>`).text("Before min start (too early)"));
        }
        if (tooLate) {
            byLineJobElement.append($(`<p class="badge text-bg-danger mb-0"/>`).text("After max end (too late)"));
            byJobJobElement.append($(`<p class="badge text-bg-danger mb-0"/>`).text("After max end (too late)"));
        }

        // The first job of a line needs no changeover cleaning, so its cleaning block is empty.
        if (job.startCleaningDateTime != null && job.startCleaningDateTime !== job.startProductionDateTime) {
            const cleaningItem = {
                content: "Cleaning",
                start: job.startCleaningDateTime, end: job.startProductionDateTime,
                style: "background-color: #FCAF3E99"
            };
            this.byLineItemData.add({...cleaningItem, id: job.id + "_cleaning", group: job.lineId});
            this.byJobItemData.add({...cleaningItem, id: job.id + "_cleaning", group: job.id});
        }
        this.byLineItemData.add({
            id: job.id, group: job.lineId,
            content: byLineJobElement.html(),
            start: job.startProductionDateTime, end: job.endDateTime
        });
        this.byJobItemData.add({
            id: job.id, group: job.id,
            content: byJobJobElement.html(),
            start: job.startProductionDateTime, end: job.endDateTime
        });
    },

    renderEmptyBanner(container, message) {
        if (container.children().length > 0) {
            return;
        }
        container.append($(`<div class="col-12"/>`)
            .append($(`<div class="alert alert-success d-flex align-items-center justify-content-center" role="alert"/>`)
                .append($(`<i class="fas fa-check-circle me-2"/>`))
                .append($(`<span/>`).text(message))));
    },
};

const DATE_TIME_FORMAT = JSJoda.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

function formatDateTime(dateTime) {
    return JSJoda.OffsetDateTime.parse(dateTime).format(DATE_TIME_FORMAT);
}

app.start();
