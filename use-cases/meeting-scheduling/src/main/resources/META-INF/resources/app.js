let autoRefreshIntervalId = null;

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

const byTimelineOptions = {
    timeAxis: {scale: "hour", step: 1},
    orientation: {axis: "top"},
    stack: false,
    xss: {disabled: true}, // Items are XSS safe through JQuery
    zoomMin: 1000 * 60 * 60, // one hour in milliseconds
    zoomMax: 4 * 1000 * 60 * 60 * 24,
    showCurrentTime: false,
};

const byRoomPanel = document.getElementById("roomVisualization");
let byRoomGroupData = new vis.DataSet();
let byRoomItemData = new vis.DataSet();
let byRoomTimeline = new vis.Timeline(byRoomPanel, byRoomItemData, byRoomGroupData, byTimelineOptions);

const byPersonPanel = document.getElementById("personVisualization");
let byPersonGroupData = new vis.DataSet();
let byPersonItemData = new vis.DataSet();
let byPersonTimeline = new vis.Timeline(byPersonPanel, byPersonItemData, byPersonGroupData, byTimelineOptions);

let scheduleId = null;
let loadedSchedule = null;
let viewType = "R";

// Lookup maps: DTOs flatten references to ids, so rebuild id → object maps on each load.
let roomMap = new Map();
let personMap = new Map();
let timeGrainMap = new Map();
let meetingMap = new Map();

// Color Picker: Based on https://venngage.com/blog/color-blind-friendly-palette/
const BG_COLORS = ["#009E73","#0072B2","#D55E00","#000000","#CC79A7","#E69F00","#F0E442","#F6768E","#C10020","#A6BDD7","#803E75","#007D34","#56B4E9","#999999","#8DD3C7","#FFD92F","#B3DE69","#FB8072","#80B1D3","#B15928","#CAB2D6","#1B9E77","#E7298A","#6A3D9A"];
const FG_COLORS = ["#FFFFFF","#FFFFFF","#FFFFFF","#FFFFFF","#FFFFFF","#000000","#000000","#FFFFFF","#FFFFFF","#000000","#FFFFFF","#FFFFFF","#FFFFFF","#000000","#000000","#000000","#000000","#FFFFFF","#000000","#FFFFFF","#000000","#FFFFFF","#FFFFFF","#FFFFFF"];
let COLOR_MAP = new Map()
    .set("R1", {bg: "#009E73", fg: "#FFFFFF"})
    .set("R2", {bg: "#0072B2", fg: "#FFFFFF"})
    .set("R3", {bg: "#E69F00", fg: "#FFFFFF"});
let nextColorIndex = 0;

function pickColor(object) {
    let color = COLOR_MAP.get(object);
    if (color !== undefined) {
        return color;
    }
    let index = nextColorIndex++;
    color = {bg: BG_COLORS[index], fg: FG_COLORS[index]};
    COLOR_MAP.set(object, color);
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
    $("#byRoomTab").click(function () {
        viewType = "R";
        byRoomTimeline.redraw();
        renderSchedule(loadedSchedule);
    });
    $("#byPersonTab").click(function () {
        viewType = "P";
        byPersonTimeline.redraw();
        renderSchedule(loadedSchedule);
    });
    setupAjax();
    if (PLATFORM.onPlatform) {
        // Embedded: hide demo chrome and load the existing run read-only.
        document.body.classList.add('on-platform');
        loadPlatformRun();
    } else {
        // Standalone dev: load demo data, allow solving.
        getStatus();
    }
});

function setupAjax() {
    $.ajaxSetup({
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json,text/plain',
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

// ── Platform: load an existing run (read-only) ──
function loadPlatformRun() {
    if (!PLATFORM.runId) {
        showError("No runId provided by platform.", {status: 0, statusText: "missing runId"});
        return;
    }
    scheduleId = PLATFORM.runId;
    $.get(api(`/v1/meeting-schedules/${scheduleId}/model-request`), function (req) {
        loadedSchedule = req.modelInput || req;
        updateScheduleMap(loadedSchedule);
        renderSchedule(loadedSchedule);
        getStatus();
    }).fail(function (xhr) {
        showError("Failed to load run input from platform.", xhr);
    });
}

function getStatus() {
    if (scheduleId == null) {
        $.get(api('/v1/demo-data/BASIC'), function (data) {
            loadedSchedule = data.modelInput;
            loadedSchedule.solverStatus = 'NOT_SOLVING';
            updateScheduleMap(loadedSchedule);
            renderSchedule(loadedSchedule);
        }).fail(function (xhr) {
            let $demo = $("#demo");
            $demo.empty();
            $demo.html("<h1><p align=\"center\">No test data available</p></h1>");
        });
    } else {
        $.get(api(`/v1/meeting-schedules/${scheduleId}`), function (data) {
            const solverStatus = data.metadata.solverStatus;
            const solution = data.modelOutput || loadedSchedule;
            solution.score = data.metadata.score;
            solution.solverStatus = solverStatus;
            loadedSchedule = solution;
            updateScheduleMap(solution);
            renderSchedule(solution);
        }).fail(function (xhr) {
            showError("Getting the schedule has failed.", xhr);
            refreshSolvingButtons(false);
        });
    }
}

function isSolving(solverStatus) {
    return solverStatus === 'SOLVING_ACTIVE' || solverStatus === 'SOLVING_SCHEDULED'
        || solverStatus === 'SOLVING_STARTED';
}

function updateScheduleMap(schedule) {
    roomMap = new Map();
    (schedule.rooms || []).forEach(r => roomMap.set(r.id, r));
    personMap = new Map();
    (schedule.people || []).forEach(p => personMap.set(p.id, p));
    timeGrainMap = new Map();
    (schedule.timeGrains || []).forEach(t => timeGrainMap.set(t.id, t));
    meetingMap = new Map();
    (schedule.meetings || []).forEach(m => meetingMap.set(m.id, m));
}

function renderSchedule(schedule) {
    if (schedule == null) {
        return;
    }
    refreshSolvingButtons(isSolving(schedule.solverStatus));
    $("#score").text("Score: " + (schedule.score == null ? "?" : schedule.score));
    $("#info").text(`This dataset has ${schedule.meetings.length} meetings which need to be assigned to ${schedule.people.length} people in ${schedule.rooms.length} rooms.`);

    if (viewType === "R") {
        renderScheduleByRoom(schedule);
    }
    if (viewType === "P") {
        renderScheduleByPerson(schedule);
    }
}

function grainStartDateTime(timeGrain) {
    const startDate = JSJoda.LocalDate.now().withDayOfYear(timeGrain.dayOfYear);
    const startTime = JSJoda.LocalTime.of(0, 0, 0, 0).plusMinutes(timeGrain.startingMinuteOfDay);
    return JSJoda.LocalDateTime.of(startDate, startTime);
}

function renderScheduleByRoom(schedule) {
    const unassigned = $("#unassigned");
    unassigned.children().remove();
    byRoomGroupData.clear();
    byRoomItemData.clear();

    $.each([...schedule.rooms].sort((e1, e2) => e1.name.localeCompare(e2.name)), (_, room) => {
        byRoomGroupData.add({
            id: room.id,
            content: `<div class="d-flex flex-column"><div><h5 class="card-title mb-1">${room.name}</h5></div>`,
        });
    });

    $.each(schedule.meetingAssignments, (_, assignment) => {
        const meet = meetingMap.get(assignment.meetingId);
        if (assignment.roomId == null || assignment.startingTimeGrainId == null) {
            const unassignedElement = $(`<div class="card-body"/>`)
                .append($(`<h5 class="card-title mb-1"/>`).text(meet.topic))
                .append($(`<p class="card-text ms-2 mb-0"/>`).text(`${(meet.durationInGrains * 15) / 60} hour(s)`));
            unassigned.append($(`<div class="pl-1"/>`).append($(`<div class="card"/>`).append(unassignedElement)));
        } else {
            const color = pickColor(assignment.roomId);
            const timeGrain = timeGrainMap.get(assignment.startingTimeGrainId);
            const startDateTime = grainStartDateTime(timeGrain);
            const endDateTime = startDateTime.toLocalTime().plusMinutes(meet.durationInGrains * 15);
            byRoomItemData.add({
                id: assignment.id,
                group: assignment.roomId,
                content: `<div><div class='d-flex justify-content-center'><h5 class="card-title mb-1">${meet.topic}</h5></div></div>`,
                start: startDateTime.toString(),
                end: endDateTime.toString(),
                style: `min-height: 50px;background-color: ${color.bg};color:${color.fg} !important"`
            });
        }
    });

    renderUnassignedBanner(unassigned);
    setTimelineWindow(byRoomTimeline, schedule);
}

function renderScheduleByPerson(schedule) {
    const unassigned = $("#unassigned");
    unassigned.children().remove();
    byPersonGroupData.clear();
    byPersonItemData.clear();

    $.each([...schedule.people].sort((e1, e2) => e1.fullName.localeCompare(e2.fullName)), (_, person) => {
        byPersonGroupData.add({
            id: person.id,
            content: `<div class="d-flex flex-column"><div><h5 class="card-title mb-1">${person.fullName}</h5></div>`,
        });
    });

    $.each(schedule.meetingAssignments, (_, assignment) => {
        const meet = meetingMap.get(assignment.meetingId);
        if (assignment.roomId == null || assignment.startingTimeGrainId == null) {
            const unassignedElement = $(`<div class="card-body"/>`)
                .append($(`<h5 class="card-title mb-1"/>`).text(meet.topic))
                .append($(`<p class="card-text ms-2 mb-0"/>`).text(`${(meet.durationInGrains * 15) / 60} hour(s)`));
            unassigned.append($(`<div class="pl-1"/>`).append($(`<div class="card"/>`).append(unassignedElement)));
        } else {
            const color = pickColor(assignment.roomId);
            const timeGrain = timeGrainMap.get(assignment.startingTimeGrainId);
            const startDateTime = grainStartDateTime(timeGrain);
            const endDateTime = startDateTime.toLocalTime().plusMinutes(meet.durationInGrains * 15);
            const required = meet.requiredAttendancePersonIds || [];
            const preferred = meet.preferredAttendancePersonIds || [];
            required.forEach(personId => {
                const element = $("<div/>").append($("<div class='d-flex justify-content-center'/>")
                    .append($(`<h5 class="card-title mb-1"/>`).text(meet.topic)));
                element.append($("<div class='d-flex justify-content-center'/>")
                    .append($(`<span class="badge bg-primary m-1"/>`).text("Required")));
                addPersonItem(assignment, personId, element, startDateTime, endDateTime, color);
            });
            preferred.forEach(personId => {
                if (required.indexOf(personId) === -1) {
                    const element = $("<div/>").append($("<div class='d-flex justify-content-center'/>")
                        .append($(`<h5 class="card-title mb-1"/>`).text(meet.topic)));
                    element.append($("<div class='d-flex justify-content-center'/>")
                        .append($(`<span class="badge bg-secondary m-1"/>`).text("Preferred")));
                    addPersonItem(assignment, personId, element, startDateTime, endDateTime, color);
                }
            });
        }
    });

    renderUnassignedBanner(unassigned);
    setTimelineWindow(byPersonTimeline, schedule);
}

function addPersonItem(assignment, personId, element, startDateTime, endDateTime, color) {
    byPersonItemData.add({
        id: `${assignment.id}-${personId}`,
        group: personId,
        content: element.html(),
        start: startDateTime.toString(),
        end: endDateTime.toString(),
        style: `min-height: 50px;background-color: ${color.bg};color:${color.fg} !important"`
    });
}

function renderUnassignedBanner(unassigned) {
    if (unassigned.children().length === 0) {
        const banner = $(`<div class="col-12"/>`)
            .append($(`<div class="alert alert-success d-flex align-items-center justify-content-center" role="alert"/>`)
                .append($(`<i class="fas fa-check-circle me-2"/>`))
                .append($(`<span/>`).text("All meetings have been assigned!")));
        unassigned.append(banner);
    }
}

function setTimelineWindow(timeline, schedule) {
    const grains = schedule.timeGrains;
    if (grains && grains.length > 0) {
        const first = grainStartDateTime(grains[0]);
        timeline.setWindow(first.toString(), first.plusHours(9).toString());
    }
}

function solve() {
    $.get(api('/v1/demo-data/BASIC'), function (modelRequest) {
        $.post(api('/v1/meeting-schedules'), JSON.stringify(modelRequest), function (metadata) {
            scheduleId = metadata.id;
            refreshSolvingButtons(true);
            if (autoRefreshIntervalId == null) {
                autoRefreshIntervalId = setInterval(getStatus, 2000);
            }
        }).fail(function (xhr) {
            showError("Start solving failed.", xhr);
            refreshSolvingButtons(false);
        });
    }).fail(function (xhr) {
        showError("Get demo data failed.", xhr);
    });
}

function analyze() {
    new bootstrap.Modal("#scoreAnalysisModal").show();
    const scoreAnalysisModalContent = $("#scoreAnalysisModalContent");
    scoreAnalysisModalContent.children().remove();
    if (loadedSchedule == null || loadedSchedule.score == null) {
        scoreAnalysisModalContent.text("No score to analyze yet, please first press the 'solve' button.");
    } else if (scheduleId == null) {
        scoreAnalysisModalContent.text("No solving job yet, please first press the 'solve' button.");
    } else {
        $('#scoreAnalysisScoreLabel').text(`(${loadedSchedule.score})`);
        $.get(api(`/v1/meeting-schedules/${scheduleId}/score-analysis`), function (scoreAnalysis) {
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
            const analysisTBody = $(`<tbody/>`);
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
        }).fail(function (xhr) {
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
    $.delete(api(`/v1/meeting-schedules/${scheduleId}`), function () {
        refreshSolvingButtons(false);
        getStatus();
    }).fail(function (xhr) {
        showError("Stop solving failed.", xhr);
    });
}

function copyTextToClipboard(id) {
    var text = $("#" + id).text().trim();
    var dummy = document.createElement("textarea");
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
