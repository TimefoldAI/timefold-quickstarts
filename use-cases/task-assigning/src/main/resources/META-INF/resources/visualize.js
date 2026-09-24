// index.template.html has a single #visualization slot; this fills it with the
// demo's own markup via setVisualizationSlot(), then owns rendering the
// schedule into that markup for the rest of the page's lifetime.

// Every task's start/end is expressed in minutes relative to the start of the schedule, not as a
// real date, so the timeline is anchored on this fixed, arbitrary instant instead of "now" - that
// keeps two renders of the same schedule identical even across a midnight boundary.
const SCHEDULE_START = JSJoda.LocalDateTime.of(2024, 1, 1, 8, 0, 0);

const AFFINITY_DURATION_MULTIPLIER = {NONE: 4, LOW: 3, MEDIUM: 2, HIGH: 1};

const TIMELINE_OPTIONS = {
    timeAxis: {scale: "hour", step: 1},
    orientation: {axis: "top"},
    stack: false,
    showCurrentTime: false,
    xss: {disabled: true}, // Items are XSS safe through JQuery
    zoomMin: 1000 * 60 * 60, // One hour in milliseconds
};

const PRIORITY_ICON = {
    MINOR: '<span class="fas fa-solid fa-chevron-down" style="color: green" title="Minor Priority"/>',
    MAJOR: '<span class="fas fa-solid fa-chevron-up" style="color: red" title="Major Priority"/>',
    CRITICAL: '<span class="fas fa-solid fa-chevron-circle-up" style="color: red" title="Critical Priority"/>',
};

const AFFINITY_ICON = {
    NONE: '<span class="fas fa-solid fa-exclamation-circle" style="color: red" title="No Affinity"/>',
    LOW: '<span class="fas fa-solid fa-arrow-down" style="color: blue" title="Low Affinity"/>',
    MEDIUM: '<span class="fas fa-solid fa-arrow-up" style="color: blue" title="Medium Affinity"/>',
    HIGH: '<span class="fas fa-solid fa-arrow-circle-up" style="color: blue" title="High Affinity"/>',
};

function escapeHtml(value) {
    return $('<div/>').text(value ?? '').html();
}

const app = {
    start() {
        setVisualizationSlot(`
    <div id="employeePanel"></div>
    <h2 class="my-4">Unassigned tasks</h2>
    <div id="unassignedTasks" class="row row-cols-3 g-3 mb-4"></div>
`);

        this.employeeGroupData = new vis.DataSet();
        this.employeeItemData = new vis.DataSet();
        this.employeeTimeline = new vis.Timeline(document.getElementById("employeePanel"), this.employeeItemData,
            this.employeeGroupData, TIMELINE_OPTIONS);

        this.quickstartPage = new QuickstartPage({
            modelPath: '/v1/schedules',
            renderSchedule: (schedule) => this.renderSchedule(schedule),
            renderInfo: (schedule) => this.renderInfo(schedule),
            mergeModelOutput: (schedule, modelOutput) => this.mergeModelOutput(schedule, modelOutput),
        });
    },

    // modelOutput only carries each employee's assigned task IDs and their solved start times, not the
    // full problem, so schedule (the QuickstartPage's loadedSchedule) keeps the full modelInput (customers,
    // task types, tasks, employees) and this only overlays the per-employee assignment.
    mergeModelOutput(schedule, modelOutput) {
        if (schedule == null) {
            return;
        }
        const assignedTasksByEmployeeId = new Map((modelOutput?.employees ?? [])
            .map((employee) => [employee.id, employee.assignedTasks]));
        schedule.employees = schedule.employees.map((employee) => ({
            ...employee,
            assignedTasks: assignedTasksByEmployeeId.get(employee.id) ?? [],
        }));
    },

    renderInfo(schedule) {
        if (schedule == null) {
            return "";
        }
        return `${schedule.tasks.length} tasks · ${schedule.employees.length} employees`;
    },

    renderSchedule(schedule) {
        resetColorMap();
        const taskTypeById = new Map(schedule.taskTypes.map((taskType) => [taskType.code, taskType]));
        const taskById = new Map(schedule.tasks.map((task) => [task.id, task]));
        const customerById = new Map(schedule.customers.map((customer) => [customer.id, customer]));
        const assignedTaskIds = new Set(schedule.employees.flatMap((employee) =>
            (employee.assignedTasks ?? []).map((assignedTask) => assignedTask.taskId)));

        this.employeeGroupData.clear();
        this.employeeItemData.clear();

        schedule.employees.slice().sort((e1, e2) => e1.fullName.localeCompare(e2.fullName)).forEach((employee) => {
            const skills = employee.skills.slice().sort();
            let content = `<div class="d-flex flex-column"><h5 class="card-title mb-1">${escapeHtml(employee.fullName)}</h5>`;
            for (let i = 0; i < skills.length; i += 2) {
                content += `<div class="d-flex">${skills.slice(i, i + 2)
                    .map((skill) => `<span class="badge text-bg-primary m-1">${escapeHtml(skill)}</span>`).join('')}</div>`;
            }
            content += "</div>";
            this.employeeGroupData.add({id: employee.id, content: content});

            (employee.assignedTasks ?? []).forEach((assignedTask) => {
                const task = taskById.get(assignedTask.taskId);
                const taskType = taskTypeById.get(task.taskTypeCode);
                const customer = customerById.get(task.customerId);
                const affinity = employee.customerAffinities[task.customerId] ?? "NONE";
                const durationInMinutes = taskType.baseDurationInMinutes * AFFINITY_DURATION_MULTIPLIER[affinity];
                const startDateTime = SCHEDULE_START.plusMinutes(assignedTask.startTimeInMinutes);

                const content = $(`<div class="card-body p-2"/>`)
                    .append($(`<h5 class="card-title mb-1"/>`).text(`${taskType.title}-${task.indexInTaskType} `)
                        .append($(AFFINITY_ICON[affinity])));
                content.append($(`<div class="col"/>`).append(
                    (taskType.requiredSkills.slice().sort())
                        .map((skill) => $(`<span class="badge text-bg-primary m-1"/>`).text(skill)),
                ));
                content.append($(`<div class="col"/>`).append(
                    $(`<span class="badge m-1"/>`)
                        .css({backgroundColor: pickColor(customer.id).bg, color: pickColor(customer.id).fg})
                        .text(customer.name),
                ));
                content.append($(`<small class="ms-2 mt-1 card-text text-muted align-bottom float-end"/>`)
                    .append($(PRIORITY_ICON[task.priority])));

                this.employeeItemData.add({
                    id: task.id,
                    group: employee.id,
                    content: content.html(),
                    start: startDateTime.toString(),
                    end: startDateTime.plusMinutes(durationInMinutes).toString(),
                });
            });
        });

        this.renderUnassignedTasks(schedule, taskTypeById, customerById, assignedTaskIds);

        this.employeeTimeline.setWindow(SCHEDULE_START.toString(), SCHEDULE_START.plusHours(4).toString());
    },

    renderUnassignedTasks(schedule, taskTypeById, customerById, assignedTaskIds) {
        const unassignedTasks = $("#unassignedTasks");
        unassignedTasks.children().remove();

        schedule.tasks.filter((task) => !assignedTaskIds.has(task.id)).forEach((task) => {
            const taskType = taskTypeById.get(task.taskTypeCode);
            const customer = customerById.get(task.customerId);

            const card = $(`<div class="card-body p-2"/>`)
                .append($(`<h5 class="card-title mb-1"/>`).text(`${taskType.title}-${task.indexInTaskType}`));
            card.append($(`<div class="col"/>`).append(
                (taskType.requiredSkills.slice().sort())
                    .map((skill) => $(`<span class="badge text-bg-primary m-1"/>`).text(skill)),
            ));
            card.append($(`<div class="col"/>`).append(
                $(`<span class="badge m-1"/>`)
                    .css({backgroundColor: pickColor(customer.id).bg, color: pickColor(customer.id).fg})
                    .text(customer.name),
            ));
            card.append($(`<small class="ms-2 mt-1 card-text text-muted align-bottom float-end"/>`)
                .append($(PRIORITY_ICON[task.priority])));

            unassignedTasks.append($(`<div class="col"/>`).append($(`<div class="card"/>`).append(card)));
        });
        if (unassignedTasks.children().length === 0) {
            unassignedTasks.append($(`<div class="col-12"/>`)
                .append($(`<div class="alert alert-success d-flex align-items-center justify-content-center" role="alert"/>`)
                    .append($(`<i class="fas fa-check-circle me-2"/>`))
                    .append($(`<span/>`).text("All tasks have been assigned!"))));
        }
    },
};

app.start();
