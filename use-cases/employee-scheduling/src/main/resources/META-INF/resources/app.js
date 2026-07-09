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
const zoomMin = 2 * 1000 * 60 * 60 * 24 // 2 day in milliseconds
const zoomMax = 4 * 7 * 1000 * 60 * 60 * 24 // 4 weeks in milliseconds

const UNAVAILABLE_COLOR = '#ef2929' // Tango Scarlet Red
const UNDESIRED_COLOR = '#f57900' // Tango Orange
const DESIRED_COLOR = '#73d216' // Tango Chameleon

let jobId = null;
let loadedSchedule = null;

const byEmployeePanel = document.getElementById("byEmployeePanel");
const byEmployeeTimelineOptions = {
    timeAxis: {scale: "hour", step: 6},
    orientation: {axis: "top"},
    stack: false,
    xss: {disabled: true}, // Items are XSS safe through JQuery
    zoomMin: zoomMin,
    zoomMax: zoomMax,
};
let byEmployeeGroupDataSet = new vis.DataSet();
let byEmployeeItemDataSet = new vis.DataSet();
let byEmployeeTimeline = new vis.Timeline(byEmployeePanel, byEmployeeItemDataSet, byEmployeeGroupDataSet, byEmployeeTimelineOptions);

const byLocationPanel = document.getElementById("byLocationPanel");
const byLocationTimelineOptions = {
    timeAxis: {scale: "hour", step: 6},
    orientation: {axis: "top"},
    xss: {disabled: true}, // Items are XSS safe through JQuery
    zoomMin: zoomMin,
    zoomMax: zoomMax,
};
let byLocationGroupDataSet = new vis.DataSet();
let byLocationItemDataSet = new vis.DataSet();
let byLocationTimeline = new vis.Timeline(byLocationPanel, byLocationItemDataSet, byLocationGroupDataSet, byLocationTimelineOptions);

let windowStart = JSJoda.LocalDate.now().toString();
let windowEnd = JSJoda.LocalDate.parse(windowStart).plusDays(7).toString();

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
    // HACK to allow vis-timeline to work within Bootstrap tabs
    $("#byEmployeeTab").on('shown.bs.tab', function (event) {
        byEmployeeTimeline.redraw();
    })
    $("#byLocationTab").on('shown.bs.tab', function (event) {
        byLocationTimeline.redraw();
    })

    addImportDropdownItem();
    addExportDropdownItem();

    setupAjax();
    if (PLATFORM.onPlatform) {
        // Embedded in the platform: load the existing run read-only and hide app chrome.
        document.body.classList.add('on-platform');
        loadPlatformRun();
    } else {
        getStatus();
    }
});

// When embedded, load the run's input (and then its output) from the platform API.
function loadPlatformRun() {
    jobId = PLATFORM.runId;
    $.get(api(`/v1/schedules/${jobId}/model-request`), function (req) {
        loadedSchedule = req.modelInput || req;
        $('#exportData').attr('href', 'data:text/plain;charset=utf-8,' + JSON.stringify(loadedSchedule));
        renderSchedule(loadedSchedule, 'NOT_SOLVING');
        getStatus();
    }).fail(function (xhr, ajaxOptions, thrownError) {
        showError("Getting the model input has failed.", xhr);
        refreshSolvingButtons('NOT_SOLVING');
    });
}

function addImportDropdownItem() {
    $("#testDataButton")
        .append($('<hr class="dropdown-divider">'))
        .append($('<a id="importTestData" class="dropdown-item" href="#">Import</a>'));
    $("#uploadModalImportButton").click(importLocalFile);
    $("#importTestData").click(function () {
        jobId = null;
        $('#uploadModal').modal('show');
    });
}

function addExportDropdownItem() {
    $("#testDataButton")
        .append($('<a id="exportData" class="dropdown-item" href="#" download="result.json">Export</a>'));
}

function setupAjax() {
    $.ajaxSetup({
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json,text/plain', // plain text is required by solve() returning UUID of the solver job
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
                url: url,
                type: method,
                dataType: type,
                data: data,
                success: callback
            });
        };
    });
}

function getStatus() {
    if (jobId == null) {
        $.get(api('/v1/demo-data/BASIC'), function (data) {
            loadedSchedule = data.modelInput;
            $('#exportData').attr('href', 'data:text/plain;charset=utf-8,' + JSON.stringify(loadedSchedule));
            renderSchedule(loadedSchedule, 'NOT_SOLVING');
        }).fail(function (xhr, ajaxOptions, thrownError) {
            showError("Getting the schedule has failed.", xhr);
            refreshSolvingButtons('NOT_SOLVING');
        });
    } else {
        $.get(api(`/v1/schedules/${jobId}`), function (data) {
            const solverStatus = data.metadata.solverStatus;
            const solution = data.modelOutput || loadedSchedule;
            loadedSchedule = solution;
            $('#exportData').attr('href', 'data:text/plain;charset=utf-8,' + JSON.stringify(loadedSchedule));
            renderSchedule(solution, solverStatus);
        }).fail(function (xhr, ajaxOptions, thrownError) {
            showError("Getting the schedule has failed.", xhr);
            refreshSolvingButtons('NOT_SOLVING');
        });
    }
}

function isSolving(solverStatus) {
    return solverStatus === 'SOLVING_ACTIVE' || solverStatus === 'SOLVING_SCHEDULED'
        || solverStatus === 'SOLVING_STARTED';
}

function getShiftColor(shift, employee) {
    const shiftStart = JSJoda.LocalDateTime.parse(shift.start);
    const shiftStartDateString = shiftStart.toLocalDate().toString();
    const shiftEnd = JSJoda.LocalDateTime.parse(shift.end);
    const shiftEndDateString = shiftEnd.toLocalDate().toString();
    if (employee.unavailableDates.includes(shiftStartDateString) ||
        // The contains() check is ignored for a shift end at midnight (00:00:00).
        (shiftEnd.isAfter(shiftStart.toLocalDate().plusDays(1).atStartOfDay()) &&
            employee.unavailableDates.includes(shiftEndDateString))) {
        return UNAVAILABLE_COLOR
    } else if (employee.undesiredDates.includes(shiftStartDateString) ||
        // The contains() check is ignored for a shift end at midnight (00:00:00).
        (shiftEnd.isAfter(shiftStart.toLocalDate().plusDays(1).atStartOfDay()) &&
            employee.undesiredDates.includes(shiftEndDateString))) {
        return UNDESIRED_COLOR
    } else if (employee.desiredDates.includes(shiftStartDateString) ||
        // The contains() check is ignored for a shift end at midnight (00:00:00).
        (shiftEnd.isAfter(shiftStart.toLocalDate().plusDays(1).atStartOfDay()) &&
            employee.desiredDates.includes(shiftEndDateString))) {
        return DESIRED_COLOR
    } else {
        return " #729fcf"; // Tango Sky Blue
    }
}

function renderSchedule(schedule, solverStatus) {
    refreshSolvingButtons(solverStatus);
    $("#score").text("Score: " + (schedule.score == null ? "?" : schedule.score));

    const groups = [];

    const employeeMap = new Map();
    schedule.employees.forEach(employee => employeeMap.set(employee.id, employee));

    // Show only first 7 days of draft
    const scheduleStart = schedule.shifts.map(shift => JSJoda.LocalDateTime.parse(shift.start).toLocalDate()).sort()[0].toString();
    const scheduleEnd = JSJoda.LocalDate.parse(scheduleStart).plusDays(7).toString();

    windowStart = scheduleStart;
    windowEnd = scheduleEnd;

    byEmployeeGroupDataSet.clear();
    byLocationGroupDataSet.clear();

    byEmployeeItemDataSet.clear();
    byLocationItemDataSet.clear();


    schedule.employees.forEach((employee, index) => {
        const employeeGroupElement = $('<div class="card-body p-2"/>')
            .append($(`<h5 class="card-title mb-2"/>)`)
                .append(employee.id))
            .append($('<div/>')
                .append($(employee.skills.map(skill => `<span class="badge me-1 mt-1" style="background-color:#d3d7cf">${skill}</span>`).join(''))));
        byEmployeeGroupDataSet.add({id: employee.id, content: employeeGroupElement.html()});

        employee.unavailableDates.forEach((rawDate, dateIndex) => {
            const date = JSJoda.LocalDate.parse(rawDate)
            const start = date.atStartOfDay().toString();
            const end = date.plusDays(1).atStartOfDay().toString();
            const byEmployeeShiftElement = $(`<div/>`)
                .append($(`<h5 class="card-title mb-1"/>`).text("Unavailable"));
            byEmployeeItemDataSet.add({
                id: "employee-" + index + "-unavailability-" + dateIndex, group: employee.id,
                content: byEmployeeShiftElement.html(),
                start: start, end: end,
                type: "background",
                style: "opacity: 0.5; background-color: " + UNAVAILABLE_COLOR,
            });
        });
        employee.undesiredDates.forEach((rawDate, dateIndex) => {
            const date = JSJoda.LocalDate.parse(rawDate)
            const start = date.atStartOfDay().toString();
            const end = date.plusDays(1).atStartOfDay().toString();
            const byEmployeeShiftElement = $(`<div/>`)
                .append($(`<h5 class="card-title mb-1"/>`).text("Undesired"));
            byEmployeeItemDataSet.add({
                id: "employee-" + index + "-undesired-" + dateIndex, group: employee.id,
                content: byEmployeeShiftElement.html(),
                start: start, end: end,
                type: "background",
                style: "opacity: 0.5; background-color: " + UNDESIRED_COLOR,
            });
        });
        employee.desiredDates.forEach((rawDate, dateIndex) => {
            const date = JSJoda.LocalDate.parse(rawDate)
            const start = date.atStartOfDay().toString();
            const end = date.plusDays(1).atStartOfDay().toString();
            const byEmployeeShiftElement = $(`<div/>`)
                .append($(`<h5 class="card-title mb-1"/>`).text("Desired"));
            byEmployeeItemDataSet.add({
                id: "employee-" + index + "-desired-" + dateIndex, group: employee.id,
                content: byEmployeeShiftElement.html(),
                start: start, end: end,
                type: "background",
                style: "opacity: 0.5; background-color: " + DESIRED_COLOR,
            });
        });
    });

    schedule.shifts.forEach((shift, index) => {
        if (groups.indexOf(shift.location) === -1) {
            groups.push(shift.location);
            byLocationGroupDataSet.add({
                id: shift.location,
                content: shift.location,
            });
        }

        const employee = shift.employeeId == null ? null : employeeMap.get(shift.employeeId);

        if (employee == null) {
            const byLocationShiftElement = $('<div class="card-body p-2"/>')
                .append($(`<h5 class="card-title mb-2"/>)`)
                    .append("Unassigned"))
                .append($('<div/>')
                    .append($(`<span class="badge me-1 mt-1" style="background-color:#d3d7cf">${shift.requiredSkill}</span>`)));

            byLocationItemDataSet.add({
                id: 'shift-' + index, group: shift.location,
                content: byLocationShiftElement.html(),
                start: shift.start, end: shift.end,
                style: "background-color: #EF292999"
            });
        } else {
            const skillColor = (employee.skills.indexOf(shift.requiredSkill) === -1 ? '#ef2929' : '#8ae234');
            const byEmployeeShiftElement = $('<div class="card-body p-2"/>')
                .append($(`<h5 class="card-title mb-2"/>)`)
                    .append(shift.location))
                .append($('<div/>')
                    .append($(`<span class="badge me-1 mt-1" style="background-color:${skillColor}">${shift.requiredSkill}</span>`)));
            const byLocationShiftElement = $('<div class="card-body p-2"/>')
                .append($(`<h5 class="card-title mb-2"/>)`)
                    .append(employee.id))
                .append($('<div/>')
                    .append($(`<span class="badge me-1 mt-1" style="background-color:${skillColor}">${shift.requiredSkill}</span>`)));

            const shiftColor = getShiftColor(shift, employee);
            byEmployeeItemDataSet.add({
                id: 'shift-' + index, group: employee.id,
                content: byEmployeeShiftElement.html(),
                start: shift.start, end: shift.end,
                style: "background-color: " + shiftColor
            });
            byLocationItemDataSet.add({
                id: 'shift-' + index, group: shift.location,
                content: byLocationShiftElement.html(),
                start: shift.start, end: shift.end,
                style: "background-color: " + shiftColor
            });
        }
    });

    $("#info").text(`This dataset has ${schedule.shifts.length} shifts and ${schedule.employees.length} employees.`);

    byEmployeeTimeline.setWindow(scheduleStart, scheduleEnd);
    byLocationTimeline.setWindow(scheduleStart, scheduleEnd);
}

function solve() {
    $.get(api('/v1/demo-data/BASIC'), function (modelRequest) {
        modelRequest.modelInput = loadedSchedule;
        $.post(api('/v1/schedules'), JSON.stringify(modelRequest), function (metadata) {
            jobId = metadata.id;
            refreshSolvingButtons(metadata.solverStatus || 'SOLVING_ACTIVE');
            if (autoRefreshIntervalId == null) {
                autoRefreshIntervalId = setInterval(getStatus, 2000);
            }
        }).fail(function (xhr, ajaxOptions, thrownError) {
            showError("Start solving failed.", xhr);
            refreshSolvingButtons('NOT_SOLVING');
        });
    }).fail(function (xhr, ajaxOptions, thrownError) {
        showError("Get demo data failed.", xhr);
    });
}

function analyze() {
    new bootstrap.Modal("#scoreAnalysisModal").show()
    const scoreAnalysisModalContent = $("#scoreAnalysisModalContent");
    scoreAnalysisModalContent.children().remove();
    if (loadedSchedule == null || loadedSchedule.score == null) {
        scoreAnalysisModalContent.text("No score to analyze yet, please first press the 'solve' button.");
    } else if (jobId == null) {
        scoreAnalysisModalContent.text("No solving job yet, please first press the 'solve' button.");
    } else {
        $('#scoreAnalysisScoreLabel').text(`(${loadedSchedule.score})`);
        $.get(api(`/v1/schedules/${jobId}/score-analysis`), function (scoreAnalysis) {
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
            scoreAnalysisModalContent.children().remove();
            scoreAnalysisModalContent.append($("<p/>").html(
                "The server returned an error."
                + " This may be due to a misconfiguration, or because Score Analysis requires"
                + " <b>Timefold Solver Enterprise Edition</b>, which is not on the classpath."
                + " If the latter, reach out to Timefold, obtain your license,"
                + " and then run the quickstart with an Enterprise profile to see Score analysis in action."));
        }, "text");
    }
}

function getScoreComponents(score) {
    let components = {hard: 0, medium: 0, soft: 0};

    $.each([...score.matchAll(/(-?\d*(\.\d+)?)(hard|medium|soft)/g)], (i, parts) => {
        components[parts[3]] = parseFloat(parts[1], 10);
    });

    return components;
}

function refreshSolvingButtons(solverStatus) {
    if (isSolving(solverStatus)) {
        $("#solveButton").hide();
        $("#stopSolvingButton").show();
        if (autoRefreshIntervalId == null) {
            autoRefreshIntervalId = setInterval(getStatus, 2000);
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
    $.delete(api(`/v1/schedules/${jobId}`), function () {
        refreshSolvingButtons('NOT_SOLVING');
        getStatus();
    }).fail(function (xhr, ajaxOptions, thrownError) {
        showError("Stop solving failed.", xhr);
    });
}

function importLocalFile() {
    var file = document.querySelector('input[type=file]').files[0];
    var reader = new FileReader();

    reader.addEventListener("load", function () {
        // convert file to base64 string
        var data = atob(reader.result.toString().replace(/^data:(.*,)?/, ''));
        $("#importedFile").val('');

        try {
            loadedSchedule = JSON.parse(data);
            renderSchedule(loadedSchedule, 'NOT_SOLVING');
            $('#exportData').attr('href', 'data:text/plain;charset=utf-8,' + JSON.stringify(loadedSchedule));
        } catch (error) {
            console.error(error);
            showSimpleError("Failed loading a schedule.\nCheck if the content of the file represents a valid schedule.");
        }
        $('#uploadModal').modal('hide');
    }, false);

    reader.readAsDataURL(file);
}

function showSimpleError(title) {
    const notification = $(`<div class="toast" role="alert" aria-live="assertive" aria-atomic="true" style="min-width: 50rem"/>`)
        .append($(`<div class="toast-header bg-danger">
                 <strong class="me-auto text-dark">Error</strong>
                 <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
               </div>`))
        .append($(`<div class="toast-body"/>`)
            .append($(`<p/>`).text(title))
        );
    $("#notificationPanel").append(notification);
    notification.toast({delay: 30000});
    notification.toast('show');
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
