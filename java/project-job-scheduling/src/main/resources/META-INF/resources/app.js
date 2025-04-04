let autoRefreshIntervalId = null;
const formatter = JSJoda.DateTimeFormatter.ofPattern("MM/dd/YYYY HH:mm").withLocale(JSJodaLocale.Locale.ENGLISH);

const zoomMin = 1000 * 60 * 60 * 24 // 1 day in milliseconds
const zoomMax = 1000 * 60 * 60 * 24 * 7 * 4 // 2 weeks in milliseconds

const byTimelineOptions = {
    timeAxis: {scale: "day"},
    orientation: {axis: "top"},
    stack: false,
    xss: {disabled: true}, // Items are XSS safe through JQuery
    zoomMin: zoomMin,
    showCurrentTime: false,
};

const byJobPanel = document.getElementById("byJobPanel");
let byJobGroupData = new vis.DataSet();
let byJobItemData = new vis.DataSet();
let byJobTimeline = new vis.Timeline(byJobPanel, byJobItemData, byJobGroupData, byTimelineOptions);

const byResourceTimelineOptions = {
    timeAxis: {scale: "day"},
    orientation: {axis: "top"},
    stack: true,
    xss: {disabled: true}, // Items are XSS safe through JQuery
    zoomMin: zoomMin,
    showCurrentTime: false,
};

const byResourcePanel = document.getElementById("byResourcePanel");
let byResourceGroupData = new vis.DataSet();
let byResourceItemData = new vis.DataSet();
let byResourceTimeline = new vis.Timeline(byResourcePanel, byResourceItemData, byResourceGroupData, byResourceTimelineOptions);

let scheduleId = null;
let loadedSchedule = null;
let viewType = "J";

$(document).ready(function () {
    replaceQuickstartTimefoldAutoHeaderFooter();

    $("#solveButton").click(function () {
        solve();
    });
    $("#stopSolvingButton").click(function () {
        stopSolving();
    });
    $("#analyzeButton").click(function () {
        analyze();
    });
    $("#byJobTab").click(function () {
        viewType = "J";
        refreshSchedule();
    });
    $("#byResourceTab").click(function () {
        viewType = "R";
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
    let path = "/schedules/" + scheduleId;
    if (scheduleId === null) {
        path = "/demo-data";
    }

    $.getJSON(path, function (schedule) {
        loadedSchedule = schedule;
        $('#exportData').attr('href', 'data:text/plain;charset=utf-8,' + JSON.stringify(loadedSchedule));
        renderSchedule(schedule);
    })
        .fail(function (xhr, ajaxOptions, thrownError) {
            showError("Getting the schedule has failed.", xhr);
            refreshSolvingButtons(false);
        });
}

function renderSchedule(schedule) {
    refreshSolvingButtons(schedule.solverStatus != null && schedule.solverStatus !== "NOT_SOLVING");
    $("#score").text("Score: " + (schedule.score == null ? "?" : schedule.score));

    if (viewType === "J") {
        renderScheduleByJob(schedule);
    } else if (viewType === "R") {
        renderScheduleByResource(schedule);
    }
}

function renderScheduleByJob(schedule) {
    const unassigned = $("#unassigned");
    unassigned.children().remove();
    byJobGroupData.clear();
    byJobItemData.clear();

    const jobMap = new Map();
    $.each(schedule.jobs.sort((j1, j2) => (+j1.id) - (+j2.id)), (_, job) => {
        jobMap.set(job.id, job);
        let content = `<div class="d-flex flex-column"><div><h5 class="card-title mb-1">Job ${job.id}</h5></div>`;
        byJobGroupData.add({
            id: job.id,
            content: content,
        });
    });

    const resourceMap = new Map();
    schedule.resources.forEach(r => resourceMap.set(r.id, r));
    const currentDate = JSJoda.LocalDate.now();
    let countAssigned = 0;
    $.each(schedule.allocations, (_, allocation) => {
        const job = jobMap.get(allocation.job);
        const isSource = job.jobType === 'SOURCE';
        const isSink = job.jobType === 'SINK';

        if (allocation.executionMode == null || allocation.delay == null) {
            const unassignedElement = $(`<div class="card-body"/>`)
                .append($(`<h5 class="card-title mb-1"/>`).text(`Job ${job.id}`))
                .append($("<div class='d-flex justify-content-start' />").append($(`<span class="badge" style="background-color: ${pickColor(job.jobType)}"/>`).text(job.jobType)))
                .append($("<div class='d-flex justify-content-start mt-2' />").append($(`<span class="badge" style="background-color: ${pickColor(job.project)}"/>`).text(`Project ${job.project}`)));

            unassigned.append($(`<div class="pl-1"/>`).append($(`<div class="card"/>`).append(unassignedElement)));
        } else {
            countAssigned++;

            if (isSource || isSink) {
                const unassignedElement = $(`<div class="card-body" />`)
                    .append($(`<h5 class="card-title mb-1"/>`).text(`Job ${job.id}`))
                    .append($("<div class='d-flex justify-content-start' />").append($(`<span class="badge" style="background-color: ${pickColor(job.jobType)}"/>`).text(job.jobType)))
                    .append($("<div class='d-flex justify-content-start mt-2' />").append($(`<span class="badge" style="background-color: ${pickColor(job.project)}"/>`).text(`Project ${job.project}`)));

                unassigned.append($(`<div class="pl-1"/>`).append($(`<div class="card"/>`).append(unassignedElement)));
            }

            let jobElement = $(`<div class="card-body"/>`)
                .append($("<div class='d-flex justify-content-start' />").append($(`<span class="badge" style="background-color: ${pickColor(job.jobType)}"/>`).text(job.jobType)))
                .append($("<div class='d-flex justify-content-start mt-2' />").append($(`<span class="badge" style="background-color: ${pickColor(job.project)}"/>`).text(`Project ${job.project}`)));
            if (!isSource && !isSink) {
                const executionMode = job.executionModes.filter(e => e.id === allocation.executionMode)[0];
                const successorJobs = job.successorJobs.sort().map(j => `Job ${j}`).join(", ");
                jobElement = $(`<div class="card-body"/>`)
                    .append($(`<p class="card-text mt-2 mb-2"/>`).text(`Successor(s): ${successorJobs}`))
                    .append($("<div class='d-flex justify-content-start' />").append($(`<span class="badge" style="background-color: ${pickColor(job.jobType)}"/>`).text(job.jobType)))
                    .append($("<div class='d-flex justify-content-start mt-2' />").append($(`<span class="badge" style="background-color: ${pickColor(job.project)}"/>`).text(`Project ${job.project}`)));
                const resourcesElement = $("<div class='d-flex justify-content-start mt-2' />");
                executionMode.resourceRequirements.sort((r1, r2) => r1.resource.localeCompare(r2.resource)).forEach(r => {
                    const resourceType = resourceMap.get(r.resource)['@type'];
                    resourcesElement.append($(`<span class="badge me-1 text-bg-secondary" />`).text(`${resourceType} ${r.resource}`))
                });
                jobElement.append(resourcesElement);
            }

            const startDate = currentDate.plusDays(allocation.startDate);
            const endDate = currentDate.plusDays(allocation.endDate);
            byJobItemData.add({
                id: `${allocation.id}-1`,
                group: allocation.job,
                content: jobElement.html(),
                start: startDate.toString(),
                end: endDate.toString(),
                style: `border-color: ${isSource || isSink ? pickColor(job.jobType) : '#97b0f8'}`
            });
        }
    });

    if (countAssigned > 4) {
        unassigned.children().remove();
    }

    byJobTimeline.setWindow(currentDate.minusDays(1).toString(), currentDate.plusDays(7).toString());
}

function renderScheduleByResource(schedule) {
    const unassigned = $("#unassigned");
    unassigned.children().remove();
    byResourceGroupData.clear();
    byResourceItemData.clear();

    const jobMap = new Map();
    schedule.jobs.forEach(j => jobMap.set(j.id, j));
    const resourceMap = new Map();
    $.each(schedule.resources.sort((r1, r2) => (+r1.id) - (+r2.id)), (_, resource) => {
        resourceMap.set(resource.id, resource);
        let content = `<div class="d-flex flex-column"><div><h5 class="card-title mb-1">${resource['@type']} ${resource.id}</h5></div>`;
        byResourceGroupData.add({
            id: resource.id,
            content: content,
        });
    });

    const currentDate = JSJoda.LocalDate.now();
    $.each(schedule.allocations, (_, allocation) => {
        const job = jobMap.get(allocation.job);
        const isSource = job.jobType === 'SOURCE';
        const isSink = job.jobType === 'SINK';

        if (isSink || isSource) {
            return;
        }

        if (allocation.executionMode == null || allocation.delay == null) {
            const unassignedElement = $(`<div class="card-body"/>`)
                .append($(`<h5 class="card-title mb-1"/>`).text(`Job ${job.id}`))
                .append($("<div class='d-flex justify-content-start' />").append($(`<span class="badge" style="background-color: ${pickColor(job.jobType)}"/>`).text(job.jobType)))
                .append($("<div class='d-flex justify-content-start mt-2' />").append($(`<span class="badge" style="background-color: ${pickColor(job.project)}"/>`).text(`Project ${job.project}`)));

            unassigned.append($(`<div class="pl-1"/>`).append($(`<div class="card"/>`).append(unassignedElement)));
        } else {
            const executionMode = job.executionModes.filter(e => e.id === allocation.executionMode)[0];
            const successorJobs = job.successorJobs.sort().map(j => `Job ${j}`).join(", ");
            const startDate = currentDate.plusDays(allocation.startDate);
            const endDate = currentDate.plusDays(allocation.endDate);
            executionMode.resourceRequirements.sort((r1, r2) => r1.resource.localeCompare(r2.resource)).forEach(r => {
                const jobElement = $(`<div class="card-body"/>`)
                    .append($(`<h5 class="card-title mb-1"/>`).text(`Job ${job.id}`))
                    .append($(`<p class="card-text mt-2 mb-2"/>`).text(`Successor(s): ${successorJobs}`))
                    .append($("<div class='d-flex justify-content-start' />").append($(`<span class="badge" style="background-color: ${pickColor(job.jobType)}"/>`).text(job.jobType)))
                    .append($("<div class='d-flex justify-content-start mt-2' />").append($(`<span class="badge" style="background-color: ${pickColor(job.project)}"/>`).text(`Project ${job.project}`)));
                byResourceItemData.add({
                    id: `${allocation.id}-${r.resource}`,
                    group: r.resource,
                    content: jobElement.html(),
                    start: startDate.toString(),
                    end: endDate.toString(),
                });

            });
        }
    });
    byResourceTimeline.setWindow(currentDate.minusDays(1).toString(), currentDate.plusDays(7).toString());
}

function solve() {
    $.post("/schedules", JSON.stringify(loadedSchedule), function (data) {
        scheduleId = data;
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
    if (loadedSchedule.score == null) {
        scoreAnalysisModalContent.text("No score to analyze yet, please first press the 'solve' button.");
    } else {
        $('#scoreAnalysisScoreLabel').text(`(${loadedSchedule.score})`);
        $.put("/schedules/analyze", JSON.stringify(loadedSchedule), function (scoreAnalysis) {
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
    $.delete("/schedules/" + scheduleId, function () {
        refreshSolvingButtons(false);
        refreshSchedule();
    }).fail(function (xhr, ajaxOptions, thrownError) {
        showError("Stop solving failed.", xhr);
    });
}

function copyTextToClipboard(id) {
    const text = $("#" + id).text().trim();

    const dummy = document.createElement("textarea");
    document.body.appendChild(dummy);
    dummy.value = text;
    dummy.select();
    document.execCommand("copy");
    document.body.removeChild(dummy);
}

// TODO: move to the webjar
function replaceQuickstartTimefoldAutoHeaderFooter() {
    const timefoldHeader = $("header#timefold-auto-header");
    if (timefoldHeader != null) {
        timefoldHeader.addClass("bg-black")
        timefoldHeader.append($(`<div class="container-fluid">
        <nav class="navbar sticky-top navbar-expand-lg navbar-dark shadow mb-3">
          <a class="navbar-brand" href="https://timefold.ai">
            <img src="/webjars/timefold/img/timefold-logo-horizontal-negative.svg" alt="Timefold logo" width="200">
          </a>
          <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
          </button>
          <div class="collapse navbar-collapse" id="navbarNav">
            <ul class="nav nav-pills">
              <li class="nav-item active" id="navUIItem">
                <button class="nav-link active" id="navUI" data-bs-toggle="pill" data-bs-target="#demo" type="button">Demo UI</button>
              </li>
              <li class="nav-item" id="navRestItem">
                <button class="nav-link" id="navRest" data-bs-toggle="pill" data-bs-target="#rest" type="button">Guide</button>
              </li>
              <li class="nav-item" id="navOpenApiItem">
                <button class="nav-link" id="navOpenApi" data-bs-toggle="pill" data-bs-target="#openapi" type="button">REST API</button>
              </li>
            </ul>
          </div>
        </nav>
      </div>`));
    }

    const timefoldFooter = $("footer#timefold-auto-footer");
    if (timefoldFooter != null) {
        timefoldFooter.append($(`<footer class="bg-black text-white-50">
               <div class="container">
                 <div class="hstack gap-3 p-4">
                   <div class="ms-auto"><a class="text-white" href="https://timefold.ai">Timefold</a></div>
                   <div class="vr"></div>
                   <div><a class="text-white" href="https://timefold.ai/docs">Documentation</a></div>
                   <div class="vr"></div>
                   <div><a class="text-white" href="https://github.com/TimefoldAI/timefold-quickstarts">Code</a></div>
                   <div class="vr"></div>
                   <div class="me-auto"><a class="text-white" href="https://timefold.ai/product/support/">Support</a></div>
                 </div>
               </div>
             </footer>`));
    }
}
