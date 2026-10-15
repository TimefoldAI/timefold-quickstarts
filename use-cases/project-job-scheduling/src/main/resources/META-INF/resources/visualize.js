// index.template.html has a single #visualization slot; this fills it with the
// demo's own markup via setVisualizationSlot(), then owns rendering the
// schedule into that markup for the rest of the page's lifetime.
//
// All dates in the model are day numbers counted from the start of the schedule,
// not calendar dates, so they are drawn relative to today (see dayToDate()).
const app = {
    start() {
        setVisualizationSlot(`
    <div class="mb-2 d-flex justify-content-end">
        <ul class="nav nav-pills" role="tablist">
            <li class="nav-item" role="presentation">
                <button class="nav-link active" id="byJobTab" data-bs-toggle="tab"
                        data-bs-target="#byJobPanel" type="button" role="tab" aria-controls="byJobPanel"
                        aria-selected="true">By job
                </button>
            </li>
            <li class="nav-item" role="presentation">
                <button class="nav-link" id="byResourceTab" data-bs-toggle="tab"
                        data-bs-target="#byResourcePanel" type="button" role="tab" aria-controls="byResourcePanel"
                        aria-selected="false">By resource
                </button>
            </li>
        </ul>
    </div>
    <div class="tab-content">
        <div class="tab-pane fade show active" id="byJobPanel" role="tabpanel" aria-labelledby="byJobTab">
        </div>
        <div class="tab-pane fade" id="byResourcePanel" role="tabpanel" aria-labelledby="byResourceTab">
        </div>
    </div>

    <h2 class="my-4">Unscheduled jobs</h2>
    <div id="unscheduledJobs" class="row row-cols-3 g-3 mb-4"></div>
`);

        const timelineOptions = {
            timeAxis: {scale: "day"},
            orientation: {axis: "top"},
            xss: {disabled: true}, // Items are XSS safe through JQuery
            zoomMin: 1000 * 60 * 60 * 24, // One day in milliseconds
            zoomMax: 1000 * 60 * 60 * 24 * 7 * 8, // Eight weeks in milliseconds
            showCurrentTime: false,
        };

        this.byJobGroupData = new vis.DataSet();
        this.byJobItemData = new vis.DataSet();
        this.byJobTimeline = new vis.Timeline(document.getElementById("byJobPanel"), this.byJobItemData,
            this.byJobGroupData, {...timelineOptions, stack: false});

        this.byResourceGroupData = new vis.DataSet();
        this.byResourceItemData = new vis.DataSet();
        this.byResourceTimeline = new vis.Timeline(document.getElementById("byResourcePanel"),
            this.byResourceItemData, this.byResourceGroupData, {...timelineOptions, stack: true});

        document.getElementById("byJobTab").addEventListener('click', () => {
            this.byJobTimeline.redraw();
            this.quickstartPage.changeRenderer((schedule) => this.renderScheduleByJob(schedule));
        });
        document.getElementById("byResourceTab").addEventListener('click', () => {
            this.byResourceTimeline.redraw();
            this.quickstartPage.changeRenderer((schedule) => this.renderScheduleByResource(schedule));
        });

        this.quickstartPage = new QuickstartPage({
            modelPath: '/v1/schedules',
            renderSchedule: (schedule) => this.renderScheduleByJob(schedule),
            renderInfo: (schedule) => this.renderInfo(schedule),
            mergeModelOutput: (schedule, modelOutput) => this.mergeModelOutput(schedule, modelOutput),
        });
    },

    // modelOutput only carries what the solver decided per allocation (allocations: [{id,
    // executionModeId, delay, startDate, endDate}]), not the full problem, so schedule (the
    // QuickstartPage's loadedSchedule) keeps the full modelInput (projects + resources + jobs) and
    // this only overlays the schedule of each job onto it.
    mergeModelOutput(schedule, modelOutput) {
        if (schedule == null) {
            return;
        }
        if (modelOutput != null && modelOutput.allocations != null) {
            const solvedById = new Map(modelOutput.allocations.map(allocation => [allocation.id, allocation]));
            schedule.allocations = schedule.allocations.map(allocation =>
                solvedById.has(allocation.id) ? {...allocation, ...solvedById.get(allocation.id)} : allocation);
        }
    },

    renderInfo(schedule) {
        if (schedule == null) {
            return "";
        }
        return `${schedule.jobs.length} jobs in ${schedule.projects.length} projects`
            + ` · ${schedule.resources.length} resources`;
    },

    // ── Lookups and formatting ──

    jobMap(schedule) {
        return new Map(schedule.jobs.map(job => [job.id, job]));
    },

    resourceMap(schedule) {
        return new Map(schedule.resources.map(resource => [resource.id, resource]));
    },

    executionModeMap(schedule) {
        return new Map(schedule.jobs.flatMap(job => job.executionModes)
            .map(executionMode => [executionMode.id, executionMode]));
    },

    // A job is only drawn on the timeline once the solver has given it both an execution mode and a
    // delay, since only then does it have dates.
    isScheduled(allocation) {
        return allocation.executionModeId != null && allocation.delay != null
            && allocation.startDate != null && allocation.endDate != null;
    },

    dayToDate(day) {
        return JSJoda.LocalDate.now().plusDays(day).toString();
    },

    resourceLabel(resource) {
        return `${resource.projectId == null ? "Shared" : "Project " + resource.projectId} resource`
            + ` ${resource.id} (${resource.capacity}${resource.renewable ? "/day" : " total"})`;
    },

    jobBadges(job) {
        const jobColor = pickColor(job.jobType);
        const projectColor = pickColor(`project-${job.projectId}`);
        return $(`<div class="d-flex justify-content-start gap-1 mt-2"/>`)
            .append($(`<span class="badge"/>`)
                .attr("style", `background-color: ${jobColor.bg};color:${jobColor.fg}`).text(job.jobType))
            .append($(`<span class="badge"/>`)
                .attr("style", `background-color: ${projectColor.bg};color:${projectColor.fg}`)
                .text(`Project ${job.projectId}`));
    },

    // The unscheduled-job cards and the "all scheduled" banner are identical in both views.
    renderUnscheduledJobs(schedule, jobs) {
        const unscheduledJobs = $("#unscheduledJobs");
        unscheduledJobs.children().remove();
        $.each(schedule.allocations.filter(allocation => !this.isScheduled(allocation)), (_, allocation) => {
            const job = jobs.get(allocation.jobId);
            const card = $(`<div class="card-body p-2"/>`)
                .append($(`<h5 class="card-title mb-1"/>`).text(`Job ${job.id}`))
                .append(this.jobBadges(job));
            unscheduledJobs.append($(`<div class="col"/>`)
                .append($(`<div class="card" style="background-color: #EF292999"/>`).append(card)));
        });
        if (unscheduledJobs.children().length === 0) {
            const banner = $(`<div class="col-12"/>`)
                .append($(`<div class="alert alert-success d-flex align-items-center justify-content-center" role="alert"/>`)
                    .append($(`<i class="fas fa-check-circle me-2"/>`))
                    .append($(`<span/>`).text("All jobs have been scheduled!")));
            unscheduledJobs.append(banner);
        }
    },

    // ── Renderers ──

    renderScheduleByJob(schedule) {
        this.resetColors();
        const jobs = this.jobMap(schedule);
        const executionModes = this.executionModeMap(schedule);
        this.byJobGroupData.clear();
        this.byJobItemData.clear();

        $.each(schedule.jobs, (_, job) => {
            const label = $('<div/>').text(`Job ${job.id} (project ${job.projectId})`).html();
            this.byJobGroupData.add({
                id: job.id,
                content: `<div class="d-flex flex-column"><div><h5 class="card-title mb-1">${label}</h5></div></div>`,
            });
        });

        $.each(schedule.allocations.filter(allocation => this.isScheduled(allocation)), (_, allocation) => {
            const job = jobs.get(allocation.jobId);
            const executionMode = executionModes.get(allocation.executionModeId);
            const content = $(`<div class="card-body p-2"/>`);
            if (job.successorJobIds.length > 0) {
                content.append($(`<p class="card-text mb-0"/>`)
                    .text(`Successor(s): ${job.successorJobIds.map(id => `Job ${id}`).join(", ")}`));
            }
            content.append(this.jobBadges(job));
            const requirements = $(`<div class="d-flex justify-content-start gap-1 mt-2"/>`);
            $.each(executionMode.resourceRequirements, (_, requirement) => {
                requirements.append($(`<span class="badge text-bg-secondary"/>`)
                    .text(`Resource ${requirement.resourceId}: ${requirement.requirement}`));
            });
            content.append(requirements);
            this.byJobItemData.add({
                id: allocation.id,
                group: allocation.jobId,
                content: content.html(),
                start: this.dayToDate(allocation.startDate),
                // A source or a sink job takes no time at all, so give it a day of width to stay visible.
                end: this.dayToDate(Math.max(allocation.endDate, allocation.startDate + 1)),
                style: `min-height: 50px; border-color: ${pickColor(job.jobType).bg}`,
            });
        });

        this.renderUnscheduledJobs(schedule, jobs);
        this.setWindow(this.byJobTimeline, schedule);
        this.byJobTimeline.redraw();
    },

    renderScheduleByResource(schedule) {
        this.resetColors();
        const jobs = this.jobMap(schedule);
        const executionModes = this.executionModeMap(schedule);
        this.byResourceGroupData.clear();
        this.byResourceItemData.clear();

        $.each(schedule.resources, (_, resource) => {
            const label = $('<div/>').text(this.resourceLabel(resource)).html();
            this.byResourceGroupData.add({
                id: resource.id,
                content: `<div class="d-flex flex-column"><div><h5 class="card-title mb-1">${label}</h5></div></div>`,
            });
        });

        $.each(schedule.allocations.filter(allocation => this.isScheduled(allocation)), (_, allocation) => {
            const job = jobs.get(allocation.jobId);
            const executionMode = executionModes.get(allocation.executionModeId);
            $.each(executionMode.resourceRequirements, (_, requirement) => {
                const content = $(`<div class="card-body p-2"/>`)
                    .append($(`<h5 class="card-title mb-1"/>`)
                        .text(`Job ${job.id}: ${requirement.requirement} unit(s)`))
                    .append(this.jobBadges(job));
                this.byResourceItemData.add({
id: JSON.stringify([allocation.id, requirement.resourceId]),
                    group: requirement.resourceId,
                    content: content.html(),
                    start: this.dayToDate(allocation.startDate),
                    end: this.dayToDate(Math.max(allocation.endDate, allocation.startDate + 1)),
                    style: "min-height: 50px",
                });
            });
        });

        this.renderUnscheduledJobs(schedule, jobs);
        this.setWindow(this.byResourceTimeline, schedule);
        this.byResourceTimeline.redraw();
    },

    // Seeded so that the job types always get the same three colors, whichever job is drawn first.
    resetColors() {
        resetColorMap();
        ["SOURCE", "STANDARD", "SINK"].forEach(jobType => pickColor(jobType));
    },

    setWindow(timeline, schedule) {
        const endDates = schedule.allocations
            .filter(allocation => this.isScheduled(allocation))
            .map(allocation => allocation.endDate);
        const lastDay = endDates.length === 0 ? 7 : Math.max(...endDates);
        timeline.setWindow(this.dayToDate(-1), this.dayToDate(lastDay + 1));
    },
};

app.start();
