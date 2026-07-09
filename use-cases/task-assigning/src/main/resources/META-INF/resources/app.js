// ── Platform context ──
// When embedded in the Timefold Platform, the iframe URL carries these query params.
// Standalone (local dev), none are present and the app behaves as before.
const PLATFORM = (function () {
    const q = new URL(window.location.href).searchParams;
    return {
        onPlatform: q.has('onPlatform'),
        runId: q.get('runId'),
        // apiUrl: base URL of the model API on the platform (URL-encoded). Trailing slash stripped.
        apiUrl: q.has('apiUrl') ? decodeURIComponent(q.get('apiUrl')).replace(/\/+$/, '') : null,
        apiKey: q.has('apiKey') ? q.get('apiKey') : null,
    };
})();

// Build an API URL: prefix the platform base when embedded, else root-relative (local dev).
function api(path) {
    return PLATFORM.apiUrl ? PLATFORM.apiUrl + path : path;
}

let autoRefreshIntervalId = null;

const byEmployeePanel = document.getElementById("byEmployeePanel");
const byEmployeeTimelineOptions = {
    timeAxis: {scale: "hour", step: 1},
    orientation: {axis: "top"},
    stack: false,
    showCurrentTime: false,
    xss: {disabled: true}, // Items are XSS safe through JQuery
    zoomMin: 1000 * 60 * 60 // One hour in milliseconds
};
const byEmployeeGroupData = new vis.DataSet();
const byEmployeeItemData = new vis.DataSet();
const byEmployeeTimeline = new vis.Timeline(byEmployeePanel, byEmployeeItemData, byEmployeeGroupData, byEmployeeTimelineOptions);

const SOLVING_STATUSES = new Set(["SOLVING_SCHEDULED", "SOLVING_ACTIVE", "SOLVING_STARTED", "SCHEDULED", "STARTED",
    "SOLVING"]);

let planId = null;
// The demo dataset ({config, modelInput}) used both as the POST body and as the source of problem facts.
let demoDataset = null;
// Problem facts (taskTypes, customers) cached from the demo dataset; they do not change while solving.
let taskTypeMap = new Map();
let customerMap = new Map();

// Color Picker: Based on https://venngage.com/blog/color-blind-friendly-palette/
const BG_COLORS = ["#009E73","#0072B2","#D55E00","#000000","#CC79A7","#E69F00","#F0E442","#F6768E","#C10020","#A6BDD7","#803E75","#007D34","#56B4E9","#999999","#8DD3C7","#FFD92F","#B3DE69","#FB8072","#80B1D3","#B15928","#CAB2D6","#1B9E77","#E7298A","#6A3D9A"];
const FG_COLORS = ["#FFFFFF","#FFFFFF","#FFFFFF","#FFFFFF","#FFFFFF","#000000","#000000","#FFFFFF","#FFFFFF","#000000","#FFFFFF","#FFFFFF","#FFFFFF","#000000","#000000","#000000","#000000","#FFFFFF","#000000","#FFFFFF","#000000","#FFFFFF","#FFFFFF","#FFFFFF"];
let COLOR_MAP = new Map()
let nextColorIndex = 0

function pickColor(object) {
    let color = COLOR_MAP.get(object);
    if (color !== undefined) {
        return color;
    }
    let index = nextColorIndex % BG_COLORS.length;
    nextColorIndex++;
    color = {bg : BG_COLORS[index], fg: FG_COLORS[index]};
    COLOR_MAP.set(object,color);
    return color;
}

$(document).ready(function () {
    $("#solveButton").click(function () {
        solve();
    });
    $("#stopSolvingButton").click(function () {
        stopSolving();
    });
    // Score analysis is not available through the Models Service SDK local endpoints.
    $("#analyzeButton").hide();

    setupAjax();
    if (PLATFORM.onPlatform) {
        // Embedded: hide demo chrome (navbar + dataset info + solve/score controls)
        // and load the existing run read-only.
        document.body.classList.add("on-platform");
        loadPlatformRun();
    } else {
        // Standalone dev: load demo data, allow solving.
        loadDemoData();
    }
});

// ── Platform: load an existing run (read-only) ──
// ModelRest exposes the run's input at /{id}/model-request (ModelRequest -> {modelInput})
// and its output+status at /{id} (ModelResponse -> {metadata:{solverStatus,score}, modelOutput}).
function loadPlatformRun() {
    if (!PLATFORM.runId) {
        showError("No runId provided by platform.", {status: 0, statusText: "missing runId"});
        return;
    }
    planId = PLATFORM.runId;
    $.getJSON(api("/v1/task-assigning-plans/" + planId + "/model-request"), function (req) {
        const modelInput = req.modelInput || req;
        cacheProblemFacts(modelInput);
        renderSchedule(modelInput.employees, modelInput.tasks, "NOT_SOLVING", null);
        refreshSchedule(); // fetch output + render; auto-polls while solving
    }).fail(function (xhr) {
        showError("Failed to load run input from platform.", xhr);
    });
}

function setupAjax() {
    $.ajaxSetup({
        headers: {
            'Content-Type': 'application/json', 'Accept': 'application/json,text/plain',
            // On the platform, authenticate every request with the supplied API key.
            ...(PLATFORM.apiKey ? {'X-API-KEY': PLATFORM.apiKey} : {})
        }
    });

    // Extend jQuery to support $.put() and $.delete()
    jQuery.each(["put", "delete"], function (i, method) {
        jQuery[method] = function (url, data, callback, type) {
            if (jQuery.isFunction(data)) {
                type = type || callback;
                callback = data;
                data = undefined;
            }
            return jQuery.ajax({
                url: url, type: method, dataType: type, data: data, success: callback
            });
        };
    });
}

function cacheProblemFacts(modelInput) {
    taskTypeMap = new Map();
    (modelInput.taskTypes || []).forEach(t => taskTypeMap.set(t.code, t));
    customerMap = new Map();
    (modelInput.customers || []).forEach(c => customerMap.set(c.id, c));
}

function loadDemoData() {
    $.getJSON(api("/v1/demo-data/BASIC"), function (dataset) {
        demoDataset = dataset;
        cacheProblemFacts(dataset.modelInput);
        renderSchedule(dataset.modelInput.employees, dataset.modelInput.tasks, "NOT_SOLVING", null);
        refreshSolvingButtons(false);
    }).fail(function (xhr) {
        showError("Getting the demo data has failed.", xhr);
    });
}

function refreshSchedule() {
    if (planId === null) {
        loadDemoData();
        return;
    }
    $.getJSON(api("/v1/task-assigning-plans/" + planId), function (plan) {
        const metadata = plan.metadata || {};
        const output = plan.modelOutput || {};
        renderSchedule(output.employees || [], output.tasks || [], metadata.solverStatus, metadata.score);
        refreshSolvingButtons(SOLVING_STATUSES.has(metadata.solverStatus));
    }).fail(function (xhr) {
        showError("Getting the schedule has failed.", xhr);
        refreshSolvingButtons(false);
    });
}

function affinityMultiplier(affinity) {
    if (affinity === "LOW") return 3;
    if (affinity === "MEDIUM") return 2;
    if (affinity === "HIGH") return 1;
    return 4;
}

function affinityIcon(affinity) {
    if (affinity === "LOW") return "<span class='fas fa-solid fa-arrow-down' style='color: blue' title='Low Affinity'/>";
    if (affinity === "MEDIUM") return "<span class='fas fa-solid fa-arrow-up' style='color: blue' title='Medium Affinity'/>";
    if (affinity === "HIGH") return "<span class='fas fa-solid fa-arrow-circle-up' style='color: blue' title='High Affinity'/>";
    return "<span class='fas fa-solid fa-exclamation-circle' style='color: red' title='No Affinity'/>";
}

function renderSchedule(employees, tasks, solverStatus, score) {
    $("#score").text("Score: " + (score == null ? "?" : score));
    $("#info").text(`This dataset has ${tasks.length} tasks and ${employees.length} employees.`);

    const unassigned = $("#unassigned");
    unassigned.children().remove();
    byEmployeeGroupData.clear();
    byEmployeeItemData.clear();

    // Build employee groups (rows in the timeline).
    employees.slice().sort((e1, e2) => e1.fullName.localeCompare(e2.fullName)).forEach(employee => {
        let content = `<div class="d-flex flex-column"><div><h5 class="card-title mb-1">${employee.fullName}</h5></div>`;
        const skills = (employee.skills || []).slice().sort();
        if (skills.length > 0) {
            content += `<div class="d-flex flex-wrap">`;
            skills.forEach(s => {
                const c = pickColor(s);
                content += `<div><span class="badge m-1" style="background-color: ${c.bg};color:${c.fg}">${s}</span></div>`;
            });
            content += "</div>";
        }
        content += "</div>";
        byEmployeeGroupData.add({id: employee.id, content: content});
    });

    // Map each task id to the employee it is assigned to, plus each employee's customer affinities.
    const taskToEmployee = new Map();
    const affinityByEmployee = new Map();
    employees.forEach(employee => {
        const affinities = new Map();
        (employee.affinities || []).forEach(a => affinities.set(a.customerId, a.affinity));
        affinityByEmployee.set(employee.id, affinities);
        (employee.taskIds || []).forEach(taskId => taskToEmployee.set(taskId, employee.id));
    });

    let unassignedCount = 0;
    tasks.forEach(task => {
        const taskType = taskTypeMap.get(task.taskTypeCode)
            || {title: task.taskTypeCode, requiredSkills: [], baseDuration: 60};
        const customer = customerMap.get(task.customerId) || {id: task.customerId, name: task.customerId};
        const employeeId = taskToEmployee.get(task.id);

        const skillsDiv = $("<div />").prop("class", "col");
        (taskType.requiredSkills || []).slice().sort().forEach(s => {
            const c = pickColor(s);
            skillsDiv.append($(`<span class="badge m-1" style="background-color: ${c.bg};color:${c.fg}"/>`).text(s));
        });

        const customerDiv = $("<div />").prop("class", "col");
        const customerColor = pickColor("customer-" + customer.id);
        customerDiv.append($(`<span class="badge m-1" style="background-color: ${customerColor.bg};color:${customerColor.fg}" />`).text(customer.name));

        let priorityElement = $("<small class='ms-2 mt-1 card-text text-muted align-bottom float-end' />");
        if (task.priority === "MINOR") {
            priorityElement.append($(`<span class='fas fa-solid fa-chevron-down' style="color: green" title='Minor Priority'/>`));
        } else if (task.priority === "MAJOR") {
            priorityElement.append($(`<span class='fas fa-solid fa-chevron-up' style="color: red" title='Major Priority'/>`));
        } else {
            priorityElement.append($(`<span class='fas fa-solid fa-chevron-circle-up' style="color: red" title='Critical Priority'/>`));
        }

        if (employeeId == null || task.startTime == null) {
            unassignedCount++;
            const unassignedElement = $(`<div class="card-body p-2"/>`)
                .append($(`<h5 class="card-title mb-1"/>`).text(`${taskType.title}-${task.indexInTaskType}`));
            unassignedElement.append(skillsDiv);
            unassignedElement.append(customerDiv);
            unassignedElement.append(priorityElement);
            unassigned.append($(`<div class="pl-1" />`).append($(`<div class="card" />`).append(unassignedElement)));
        } else {
            const affinity = (affinityByEmployee.get(employeeId) || new Map()).get(task.customerId);
            const employeeElement = $(`<div class="card-body p-2"/>`)
                .append($(`<h5 class="card-title mb-1"/>`).text(`${taskType.title}-${task.indexInTaskType} `).append($(affinityIcon(affinity))));
            employeeElement.append(skillsDiv);
            employeeElement.append(customerDiv);
            employeeElement.append(priorityElement);
            const startTime = JSJoda.LocalDateTime.now().withHour(8).withMinute(0).withSecond(0).withNano(0)
                .plusMinutes(task.startTime);
            const duration = affinityMultiplier(affinity) * taskType.baseDuration;
            byEmployeeItemData.add({
                id: task.id,
                group: employeeId,
                content: employeeElement.html(),
                start: startTime.toString(),
                end: startTime.plusMinutes(duration).toString(),
            });
        }
    });

    if (unassignedCount === 0 && tasks.length > 0) {
        const banner = $(`<div class="col-12"/>`)
            .append($(`<div class="alert alert-success d-flex align-items-center justify-content-center" role="alert"/>`)
                .append($(`<i class="fas fa-check-circle me-2"/>`))
                .append($(`<span/>`).text("All tasks have been assigned!")));
        unassigned.append(banner);
    }

    byEmployeeTimeline.setWindow(JSJoda.LocalDateTime.now().withHour(8).withMinute(0).toString(),
        JSJoda.LocalDateTime.now().withHour(12).withMinute(0).toString());
}

function solve() {
    if (demoDataset === null) {
        showError("No demo data loaded yet.", {status: 0, statusText: "no data"});
        return;
    }
    $.post(api("/v1/task-assigning-plans"), JSON.stringify(demoDataset), function (data) {
        planId = data.id;
        refreshSolvingButtons(true);
    }).fail(function (xhr) {
        showError("Start solving failed.", xhr);
        refreshSolvingButtons(false);
    });
}

function refreshSolvingButtons(solving) {
    if (solving) {
        $("#solveButton").hide();
        $("#stopSolvingButton").show();
        if (autoRefreshIntervalId == null) {
            autoRefreshIntervalId = setInterval(refreshSchedule, 2000);
        }
    } else {
        $("#solveButton").show();
        $("#stopSolvingButton").hide();
        if (autoRefreshIntervalId != null) {
            clearInterval(autoRefreshIntervalId);
            autoRefreshIntervalId = null;
        }
    }
}

function stopSolving() {
    if (planId === null) {
        return;
    }
    $.delete(api("/v1/task-assigning-plans/" + planId), function () {
        refreshSolvingButtons(false);
        refreshSchedule();
    }).fail(function (xhr) {
        showError("Stop solving failed.", xhr);
    });
}

function copyTextToClipboard(id) {
    let text = $("#" + id).text().trim();
    let dummy = document.createElement("textarea");
    document.body.appendChild(dummy);
    dummy.value = text;
    dummy.select();
    document.execCommand("copy");
    document.body.removeChild(dummy);
}

function showError(title, xhr) {
    let serverErrorMessage = !xhr.responseJSON ? `${xhr.status}: ${xhr.statusText}` : xhr.responseJSON.message;
    console.error(title + "\n" + serverErrorMessage);
    const notification = $(`<div class="toast" role="alert" aria-live="assertive" aria-atomic="true" style="min-width: 50rem"/>`)
        .append($(`<div class="toast-header bg-danger">
                 <strong class="me-auto text-dark">Error</strong>
                 <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
               </div>`))
        .append($(`<div class="toast-body"/>`)
            .append($(`<p/>`).text(title))
            .append($(`<pre/>`).append($(`<code/>`).text(serverErrorMessage)))
        );
    $("#notificationPanel").append(notification);
    notification.toast({delay: 30000});
    notification.toast('show');
}
