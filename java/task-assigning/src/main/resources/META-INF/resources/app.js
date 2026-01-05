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

let planId = null;
let loadedPlan = null;
let viewType = "E";

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
    let index = nextColorIndex++;
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
    $("#analyzeButton").click(function () {
        analyze();
    });
    $("#byEmployeeTab").click(function () {
        viewType = "E";
        byEmployeeTimeline.redraw();
        refreshSchedule();
    });

    setupAjax();
    refreshSchedule();
});

function setupAjax() {
    $.ajaxSetup({
        headers: {
            'Content-Type': 'application/json', 'Accept': 'application/json,text/plain', // plain text is required by solve() returning UUID of the solver job
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

function refreshSchedule() {
    let path = "/schedules/" + planId;
    if (planId === null) {
        path = "/demo-data";
    }

    $.getJSON(path, function (plan) {
        loadedPlan = plan;
        $('#exportData').attr('href', 'data:text/plain;charset=utf-8,' + JSON.stringify(loadedPlan));
        renderSchedule(plan);
    })
        .fail(function (xhr, ajaxOptions, thrownError) {
            showError("Getting the schedule has failed.", xhr);
            refreshSolvingButtons(false);
        });
}

function renderSchedule(plan) {
    refreshSolvingButtons(plan.solverStatus != null && plan.solverStatus !== "NOT_SOLVING");
    $("#score").text("Score: " + (plan.score == null ? "?" : plan.score));
    $("#info").text(`This dataset has ${plan.tasks.length} tasks and ${plan.employees.length} employees.`);

    if (viewType === "E") {
        renderScheduleByEmployee(plan);
    }
}

function renderScheduleByEmployee(plan) {
    const unassigned = $("#unassigned");
    unassigned.children().remove();
    let unassignedCount = 0;
    byEmployeeGroupData.clear();
    byEmployeeItemData.clear();

    $.each(plan.employees.sort((e1, e2) => e1.fullName.localeCompare(e2.fullName)), (_, employee) => {
        let content = `<div class="d-flex flex-column"><div><h5 class="card-title mb-1">${employee.fullName}</h5></div>`;
        if (employee.skills.length > 0) {
            let skills = employee.skills.sort().slice(0, Math.min(2, employee.skills.length));
            content += `<div class="d-flex">`;
            skills.forEach(s => content += `<div><span class="badge text-bg-primary m-1" style="background-color: ${pickColor(s.bg)};color:${pickColor(s.fg)}">${s}</span></div>`);
            content += "</div>";
            if (employee.skills.length > 2) {
                let skills = employee.skills.sort().slice(2, Math.min(4, employee.skills.length));
                content += `<div class="d-flex">`;
                skills.forEach(s => content += `<div><span class="badge text-bg-primary m-1" style="background-color: ${pickColor(s.bg)};color:${pickColor(s.fg)}">${s}</span></div>`);
                content += "</div>";
            }
        }
        byEmployeeGroupData.add({id: employee.id, content: content});
    });

    const taskTypeMap = new Map();
    plan.taskTypes.forEach(t => taskTypeMap.set(t.code, t));

    const taskMap = new Map();
    plan.tasks.forEach(t => taskMap.set(t.id, t));

    const employeeMap = new Map();
    plan.employees.forEach(e => employeeMap.set(e.id, e));

    const customerMap = new Map();
    plan.customers.forEach(c => customerMap.set(c.id, c));

    let tasks = [];
    plan.employees.forEach(e => {
        e.tasks.forEach(t => tasks.push({
            ...taskMap.get(t), employee: e.id
        }));
    });
    if (tasks.length === 0) {
        tasks = plan.tasks;
    }

    $.each(tasks, (_, task) => {
        const taskType = taskTypeMap.get(task.taskType);
        const customer = customerMap.get(task.customer);

        const skillsDiv = $("<div />").prop("class", "col");
        taskType.requiredSkills.sort().forEach(s => {
            skillsDiv.append($(`<span class="badge text-bg-primary m-1"/>`).text(s))
        });


        const customerDiv = $("<div />").prop("class", "col");
        const customerColor = pickColor(customer.id);
        customerDiv.append($(`<span class="badge m-1" style="background-color: ${customerColor.bg};color:${customerColor.fg}" />`).text(customer.name));

        let priorityElement = $("<small class='ms-2 mt-1 card-text text-muted align-bottom float-end' />");
        if (task.priority === "MINOR") {
            priorityElement.append($(`<span class='fas fa-solid fa-chevron-down' style="color: green" title='Minor Priority'/>`));
        } else if (task.priority === "MAJOR") {
            priorityElement.append($(`<span class='fas fa-solid fa-chevron-up' style="color: red" title='Major Priority'/>`));
        } else {
            priorityElement.append($(`<span class='fas fa-solid fa-chevron-circle-up' style="color: red" title='Critical Priority'/>`));
        }
        if (task.employee == null) {
            unassignedCount++;
            const unassignedElement = $(`<div class="card-body p-2"/>`)
                .append($(`<h5 class="card-title mb-1"/>`).text(`${taskType.title}-${task.indexInTaskType}`));

            unassignedElement.append(skillsDiv);
            unassignedElement.append(customerDiv);
            unassignedElement.append(priorityElement);
            unassigned.append($(`<div class="pl-1" />`).append($(`<div class="card" />`).append(unassignedElement)));
        } else {
            const employee = employeeMap.get(task.employee);
            const affinity = employee.customerToAffinity[task.customer];
            let affinityMultiplier = 4;
            let affinityIcon = "<span class='fas fa-solid fa-exclamation-circle' style='color: red' title='No Affinity'/>";
            if (affinity === 'LOW') {
                affinityIcon = "<span class='fas fa-solid fa-arrow-down' style='color: blue' title='Low Affinity'/>";
                affinityMultiplier = 3;
            } else if (affinity === 'MEDIUM') {
                affinityIcon = "<span class='fas fa-solid fa-arrow-up' style='color: blue' title='Medium Affinity'/>";
                affinityMultiplier = 2;
            } else if (affinity === 'HIGH') {
                affinityIcon = "<span class='fas fa-solid fa-arrow-circle-up' style='color: blue' title='High Affinity'/>";
                affinityMultiplier = 1;
            }

            const employeeElement = $(`<div class="card-body p-2"/>`)
                .append($(`<h5 class="card-title mb-1"/>`).text(`${taskType.title}-${task.indexInTaskType} `).append($(affinityIcon)));

            employeeElement.append(skillsDiv);
            employeeElement.append(customerDiv);
            employeeElement.append(priorityElement);
            const startTime = JSJoda.LocalDateTime.now().withHour(8).withMinute(0).withSecond(0)
                .plusMinutes(task.startTime);
            const duration = affinityMultiplier * taskType.baseDuration;
            byEmployeeItemData.add({
                id: task.id,
                group: task.employee,
                content: employeeElement.html(),
                start: startTime.toString(),
                end: startTime.plusMinutes(duration).toString(),
            });
        }
    });
    if (unassignedCount === 0) {
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
    $.post("/schedules", JSON.stringify(loadedPlan), function (data) {
        planId = data;
        refreshSolvingButtons(true);
    }).fail(function (xhr, ajaxOptions, thrownError) {
        showError("Start solving failed.", xhr);
        refreshSolvingButtons(false);
    }, "text");
}

function analyze() {
    new bootstrap.Modal("#scoreAnalysisModal").show()
    const scoreAnalysisModalContent = $("#scoreAnalysisModalContent");
    scoreAnalysisModalContent.children().remove();
    if (loadedPlan.score == null) {
        scoreAnalysisModalContent.text("No score to analyze yet, please first press the 'solve' button.");
    } else {
        $('#scoreAnalysisScoreLabel').text(`(${loadedPlan.score})`);
        $.put("/schedules/analyze", JSON.stringify(loadedPlan), function (scoreAnalysis) {
            let constraints = scoreAnalysis.constraints;
            constraints.sort((a, b) => {
                let aComponents = getScoreComponents(a.score), bComponents = getScoreComponents(b.score);
                if (aComponents.hard < 0 && bComponents.hard > 0) return -1;
                if (aComponents.hard > 0 && bComponents.soft < 0) return 1;
                if (Math.abs(aComponents.hard) > Math.abs(bComponents.hard)) {
                    return -1;
                } else {
                    if (aComponents.medium < 0 && bComponents.medium > 0) return -1;
                    if (aComponents.medium > 0 && bComponents.medium < 0) return 1;
                    if (Math.abs(aComponents.medium) > Math.abs(bComponents.medium)) {
                        return -1;
                    } else {
                        if (aComponents.soft < 0 && bComponents.soft > 0) return -1;
                        if (aComponents.soft > 0 && bComponents.soft < 0) return 1;

                        return Math.abs(bComponents.soft) - Math.abs(aComponents.soft);
                    }
                }
            });
            constraints.map((e) => {
                let components = getScoreComponents(e.weight);
                e.type = components.hard != 0 ? 'hard' : (components.medium != 0 ? 'medium' : 'soft');
                e.weight = components[e.type];
                let scores = getScoreComponents(e.score);
                e.implicitScore = scores.hard != 0 ? scores.hard : (scores.medium != 0 ? scores.medium : scores.soft);
            });
            scoreAnalysis.constraints = constraints;

            scoreAnalysisModalContent.children().remove();
            scoreAnalysisModalContent.text("");

            const analysisTable = $(`<table class="table"/>`).css({textAlign: 'center'});
            const analysisTHead = $(`<thead/>`).append($(`<tr/>`)
                .append($(`<th></th>`))
                .append($(`<th>Constraint</th>`).css({textAlign: 'left'}))
                .append($(`<th>Type</th>`))
                .append($(`<th># Matches</th>`))
                .append($(`<th>Weight</th>`))
                .append($(`<th>Score</th>`))
                .append($(`<th></th>`)));
            analysisTable.append(analysisTHead);
            const analysisTBody = $(`<tbody/>`)
            $.each(scoreAnalysis.constraints, (index, constraintAnalysis) => {
                let icon = constraintAnalysis.type == "hard" && constraintAnalysis.implicitScore < 0 ? '<span class="fas fa-exclamation-triangle" style="color: red"></span>' : '';
                if (!icon) icon = constraintAnalysis.matches.length == 0 ? '<span class="fas fa-check-circle" style="color: green"></span>' : '';

                let row = $(`<tr/>`);
                row.append($(`<td/>`).html(icon))
                    .append($(`<td/>`).text(constraintAnalysis.name).css({textAlign: 'left'}))
                    .append($(`<td/>`).text(constraintAnalysis.type))
                    .append($(`<td/>`).html(`<b>${constraintAnalysis.matches.length}</b>`))
                    .append($(`<td/>`).text(constraintAnalysis.weight))
                    .append($(`<td/>`).text(constraintAnalysis.implicitScore));
                analysisTBody.append(row);
                row.append($(`<td/>`));
            });
            analysisTable.append(analysisTBody);
            scoreAnalysisModalContent.append(analysisTable);
        }).fail(function (xhr, ajaxOptions, thrownError) {
            showError("Analyze failed.", xhr);
        }, "text");
    }
}

function getScoreComponents(score) {
    let components = {hard: 0, medium: 0, soft: 0};

    $.each([...score.matchAll(/(-?[0-9]+)(hard|medium|soft)/g)], (i, parts) => {
        components[parts[2]] = parseInt(parts[1], 10);
    });

    return components;
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
    $.delete("/schedules/" + planId, function () {
        refreshSolvingButtons(false);
        refreshSchedule();
    }).fail(function (xhr, ajaxOptions, thrownError) {
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
    let serverErrorCode = !xhr.responseJSON ? `unknown` : xhr.responseJSON.code;
    let serverErrorId = !xhr.responseJSON ? `----` : xhr.responseJSON.id;
    let serverErrorDetails = !xhr.responseJSON ? `no details provided` : xhr.responseJSON.details;

    if (xhr.responseJSON && !serverErrorMessage) {
        serverErrorMessage = JSON.stringify(xhr.responseJSON);
        serverErrorCode = xhr.statusText + '(' + xhr.status + ')';
        serverErrorId = `----`;
    }

    console.error(title + "\n" + serverErrorMessage + " : " + serverErrorDetails);
    const notification = $(`<div class="toast" role="alert" aria-live="assertive" aria-atomic="true" style="min-width: 50rem"/>`)
        .append($(`<div class="toast-header bg-danger">
                 <strong class="me-auto text-dark">Error</strong>
                 <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
               </div>`))
        .append($(`<div class="toast-body"/>`)
            .append($(`<p/>`).text(title))
            .append($(`<pre/>`)
                .append($(`<code/>`).text(serverErrorMessage + "\n\nCode: " + serverErrorCode + "\nError id: " + serverErrorId))
            )
        );
    $("#notificationPanel").append(notification);
    notification.toast({delay: 30000});
    notification.toast('show');
}