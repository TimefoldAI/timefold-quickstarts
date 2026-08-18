// ── Platform context ──
// When embedded in the Timefold Platform, the iframe URL carries these query params.
// Standalone (local dev), none are present and the app behaves as before.
const PLATFORM = (function () {
    const q = new URL(window.location.href).searchParams;
    return {
        onPlatform: q.has('onPlatform'),
        runId: q.get('runId'),
        apiUrl: q.has('apiUrl') ? decodeURIComponent(q.get('apiUrl')).replace(/\/+$/, '') : null,
        apiKey: q.has('apiKey') ? q.get('apiKey') : null,
    };
})();

// Build an API URL: prefix the platform base when embedded, else root-relative (local dev).
function api(path) {
    return PLATFORM.apiUrl ? PLATFORM.apiUrl + path : path;
}

const MODEL_PATH = '/v1/schedules';
const DEMO_DATA_PATH = '/v1/demo-data/BASIC';

var autoRefreshIntervalId = null;
const timeFormatter = JSJoda.DateTimeFormatter.ofPattern('HH:mm');

let jobId = null;
let loadedSchedule = null;
let viewType = "R";
// Set when stop is pressed before the POST that creates the run has returned a jobId.
let stopRequested = false;

// Lookup maps rebuilt on every render (the DTO flattens references to IDs).
let speakerNameById = new Map();
let roomById = new Map();

// Color Picker: Based on https://venngage.com/blog/color-blind-friendly-palette/
const BG_COLORS = ["#009E73","#0072B2","#D55E00","#000000","#CC79A7","#E69F00","#F0E442","#F6768E","#C10020","#A6BDD7","#803E75","#007D34","#56B4E9","#999999","#8DD3C7","#FFD92F","#B3DE69","#FB8072","#80B1D3","#B15928","#CAB2D6","#1B9E77","#E7298A","#6A3D9A"];
const FG_COLORS = ["#FFFFFF","#FFFFFF","#FFFFFF","#FFFFFF","#FFFFFF","#000000","#000000","#FFFFFF","#FFFFFF","#000000","#FFFFFF","#FFFFFF","#FFFFFF","#000000","#000000","#000000","#000000","#FFFFFF","#000000","#FFFFFF","#000000","#FFFFFF","#FFFFFF","#FFFFFF"];
let COLOR_MAP = null;
let nextColorIndex = 0

function resetColorMap() {
    COLOR_MAP = new Map()
        .set("Blue", {bg:"#0072B2", fg: "#FFFFFF"})
        .set("Green", {bg:"#009E73", fg: "#FFFFFF"})
        .set("Orange", {bg:"#D55E00", fg: "#FFFFFF"});
    nextColorIndex = 0
}

resetColorMap();

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
    $("#byRoomTab").click(function () {
        viewType = "R";
        renderSchedule(loadedSchedule);
    });
    $("#bySpeakerTab").click(function () {
        viewType = "S";
        renderSchedule(loadedSchedule);
    });
    $("#byThemeTrackTab").click(function () {
        viewType = "TH";
        renderSchedule(loadedSchedule);
    });
    $("#bySectorsTab").click(function () {
        viewType = "SC";
        renderSchedule(loadedSchedule);
    });
    $("#byAudienceTypeTab").click(function () {
        viewType = "AT";
        renderSchedule(loadedSchedule);
    });
    $("#byAudienceLevelTab").click(function () {
        viewType = "AL";
        renderSchedule(loadedSchedule);
    });

    setupAjax();
    if (PLATFORM.onPlatform) {
        document.body.classList.add('on-platform');
        loadPlatformRun();
    } else {
        getStatus();
    }
});

function setupAjax() {
    $.ajaxSetup({
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json,text/plain',
            ...(PLATFORM.apiKey ? {'X-API-KEY': PLATFORM.apiKey} : {})
        }
    });

    // Extend jQuery to support $.put() and $.delete()
    jQuery.each(["put", "delete"], function (i, method) {
        jQuery[method] = function (url, data, callback, type) {
            if (typeof data === "function") {
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

// ── ModelRest plumbing ──
// demo-data → ModelRequest {modelInput,...}; POST model → metadata {id, solverStatus};
// GET model/{id} → ModelResponse {metadata:{solverStatus}, modelOutput}.

function loadPlatformRun() {
    if (!PLATFORM.runId) {
        showError("No runId provided by platform.", {status: 0, statusText: "missing runId"});
        return;
    }
    jobId = PLATFORM.runId;
    $.get(api(`${MODEL_PATH}/${jobId}/model-request`), function (req) {
        loadedSchedule = req.modelInput || req;
        renderSchedule(loadedSchedule);
        getStatus();
        if (autoRefreshIntervalId == null) {
            autoRefreshIntervalId = setInterval(getStatus, 2000);
        }
    }).fail(function (xhr) {
        showError("Failed to load run input from platform.", xhr);
    });
}

function getStatus() {
    if (jobId == null) {
        $.get(api(DEMO_DATA_PATH), function (data) {
            loadedSchedule = data.modelInput;
            renderSchedule(loadedSchedule);
        }).fail(function (xhr) {
            showError("Getting the demo data has failed.", xhr);
            refreshSolvingButtons("SOLVING_COMPLETED");
        });
    } else {
        $.get(api(`${MODEL_PATH}/${jobId}`), function (data) {
            loadedSchedule = data.modelOutput || loadedSchedule;
            renderSchedule(loadedSchedule);
            refreshSolvingButtons(data.metadata.solverStatus);
        }).fail(function (xhr) {
            showError("Getting the schedule has failed.", xhr);
            refreshSolvingButtons("SOLVING_COMPLETED");
        });
    }
}

function solve() {
    // Swap in the stop button right away, rather than waiting for the demo-data GET
    // and the POST to return.
    stopRequested = false;
    showStopSolvingButton(true);
    $.get(api(DEMO_DATA_PATH), function (modelRequest) {
        $.post(api(MODEL_PATH), JSON.stringify(modelRequest), function (metadata) {
            jobId = metadata.id;
            if (stopRequested) {
                // Stop was pressed while the run was still starting up: honour it now
                // that there is a jobId to stop.
                stopRequested = false;
                stopSolving();
                return;
            }
            refreshSolvingButtons(metadata.solverStatus || "SOLVING_ACTIVE");
            if (autoRefreshIntervalId == null) {
                autoRefreshIntervalId = setInterval(getStatus, 2000);
            }
        }).fail(function (xhr) {
            showError("Start solving failed.", xhr);
            refreshSolvingButtons("SOLVING_COMPLETED");
        });
    }).fail(function (xhr) {
        showError("Start solving failed.", xhr);
        refreshSolvingButtons("SOLVING_COMPLETED");
    });
}

function stopSolving() {
    if (jobId == null) {
        // The run is still being created, so there is nothing to DELETE yet. Keep the
        // stop button showing and let solve() stop the run as soon as it has an id.
        stopRequested = true;
        return;
    }
    $.delete(api(`${MODEL_PATH}/${jobId}`), function () {
        refreshSolvingButtons("SOLVING_COMPLETED");
        getStatus();
    }).fail(function (xhr) {
        showError("Stop solving failed.", xhr);
    });
}

// SolvingStatus values that mean the run is over and nothing is in flight.
const TERMINAL_SOLVER_STATUSES = [
    "DATASET_INVALID", "SOLVING_COMPLETED", "SOLVING_INCOMPLETE", "SOLVING_FAILED"];

function isSolving(solverStatus) {
    // Anything non-terminal means work is queued or running, including the DATASET_*
    // states a run passes through before solving actually starts. Treating those as
    // "not solving" made the button flip back to Solve right after the POST returned.
    return solverStatus != null && !TERMINAL_SOLVER_STATUSES.includes(solverStatus);
}

function showStopSolvingButton(solving) {
    if (solving) {
        $("#solveButton").hide();
        $("#stopSolvingButton").show();
    } else {
        $("#solveButton").show();
        $("#stopSolvingButton").hide();
    }
}

function refreshSolvingButtons(solverStatus) {
    const solving = isSolving(solverStatus);
    showStopSolvingButton(solving);
    if (!solving && autoRefreshIntervalId != null) {
        clearInterval(autoRefreshIntervalId);
        autoRefreshIntervalId = null;
    }
}

function renderSchedule(schedule) {
    if (schedule == null) {
        return;
    }
    speakerNameById = new Map((schedule.speakers || []).map(s => [s.id, s.name]));
    roomById = new Map((schedule.rooms || []).map(r => [r.id, r]));

    $("#score").text("Score: " + (schedule.score == null || schedule.score === "" ? "?" : schedule.score));
    $("#info").text(`This dataset has ${schedule.talks.length} talks by ${schedule.speakers.length} speakers which need to be scheduled in ${schedule.timeslots.length} timeslots and ${schedule.rooms.length} rooms.`);

    resetColorMap();

    if (viewType === "R") {
        renderScheduleByRoom(schedule);
    } else if (viewType === "S") {
        renderScheduleBySpeaker(schedule);
    } else if (viewType === "TH") {
        renderScheduleByThemeTrack(schedule);
    } else if (viewType === "SC") {
        renderScheduleBySectors(schedule);
    } else if (viewType === "AT") {
        renderScheduleByAudienceType(schedule);
    } else if (viewType === "AL") {
        renderScheduleByAudienceLevel(schedule);
    }
}

function talkSpeakerNames(talk) {
    return (talk.speakerIds || []).map(id => speakerNameById.get(id) ?? id).join(", ");
}

function isAssigned(talk) {
    return talk.timeslotId != null && talk.roomId != null;
}

function renderScheduleByRoom(schedule) {
    const scheduleByRoom = $("#scheduleByRoom");
    scheduleByRoom.children().remove();

    const unassignedTalks = $("#unassignedTalks");
    unassignedTalks.children().remove();

    const colgroup = $("<colgroup>").appendTo(scheduleByRoom)
    colgroup.append('<col style="width: 250px">');
    $.each(schedule.rooms, item => {
        colgroup.append('<col style="width: 250px">');
    })
    const theadByRoom = $("<thead>").appendTo(scheduleByRoom);
    const headerRowByRoom = $("<tr>").appendTo(theadByRoom);
    headerRowByRoom.append($("<th>Timeslot</th>"));

    $.each(schedule.rooms.sort((a, b) => a.id > b.id ? 1 : (a.id < b.id ? -1 : 0)), (index, room) => {
        headerRowByRoom
            .append($("<th/>")
                .append($("<span/>").text(room.name))
                .append($(`<button type="button" class="ms-2 mb-1 btn btn-light btn-sm p-1"/>`)));
    });

    const tbodyByRoom = $("<tbody>").appendTo(scheduleByRoom);

    const LocalDateTime = JSJoda.LocalDateTime;

    $.each(schedule.timeslots.sort((a, b) => compareTimeslots(a, b)), (index, timeslot) => {
        const rowByRoom = $("<tr>").appendTo(tbodyByRoom);
        rowByRoom
            .append($(`<th class="align-middle"/>`)
                .append($("<span/>").text(`
                    ${LocalDateTime.parse(timeslot.startDateTime).dayOfWeek().name().charAt(0) + LocalDateTime.parse(timeslot.startDateTime).dayOfWeek().name().slice(1).toLowerCase()}
                    ${LocalDateTime.parse(timeslot.startDateTime).format(timeFormatter)}
                    -
                    ${LocalDateTime.parse(timeslot.endDateTime).format(timeFormatter)}
                `)));
        $.each(schedule.rooms, (index, room) => {
            rowByRoom.append($("<td/>").prop("id", `timeslot${timeslot.id}room${room.id}`));
        });
    });

    $.each(schedule.talks.sort((a, b) => a.code > b.code ? 1 : (a.code < b.code ? -1 : 0)), (index, talk) => {
        const color = pickColor(talk.talkTypeName);
        const talkElement = $(`<div class="card" style="background-color: ${color.bg};color:${color.fg}"/>`)
            .append($(`<div class="card-body p-2"/>`)
                .append($(`<h5 class="card-title mb-1 text-truncate"/>`).text(`${talk.code}: ${talk.title}`))
                .append($(`<p class="card-text ms-2 mb-1"/>`)
                    .append($(`<em/>`).text(`by ${talkSpeakerNames(talk)}`))));
        if (isAssigned(talk)) {
            $(`#timeslot${talk.timeslotId}room${talk.roomId}`).append(talkElement.clone());
        } else {
            unassignedTalks.append($(`<div class="col"/>`).append(talkElement));
        }
    });

    renderUnassignedBanner(unassignedTalks);
}

function compareTimeslots(t1, t2) {
    const LocalDateTime = JSJoda.LocalDateTime;
    let diff = LocalDateTime.parse(t1.startDateTime).compareTo(LocalDateTime.parse(t2.startDateTime));
    if (diff === 0) {
        diff = LocalDateTime.parse(t1.endDateTime).compareTo(LocalDateTime.parse(t2.endDateTime));
    }
    return diff;
}

function renderScheduleBySpeaker(schedule) {
    const scheduleBySpeaker = $("#scheduleBySpeaker");
    scheduleBySpeaker.children().remove();

    const unassignedTalks = $("#unassignedTalks");
    unassignedTalks.children().remove();

    const colgroup = $("<colgroup>").appendTo(scheduleBySpeaker)
    colgroup.append('<col style="width: 250px">');
    $.each(schedule.timeslots, item => {
        colgroup.append('<col style="width: 250px">');
    })
    const theadBySpeaker = $("<thead>").appendTo(scheduleBySpeaker);
    const headerRowBySpeaker = $("<tr>").appendTo(theadBySpeaker);
    headerRowBySpeaker.append($("<th>Speaker</th>"));

    const LocalDateTime = JSJoda.LocalDateTime;

    $.each(schedule.timeslots.sort((a, b) => compareTimeslots(a, b)), (index, timeslot) => {
        headerRowBySpeaker
            .append($("<th/>")
                .append($("<span/>").text(`
                    ${LocalDateTime.parse(timeslot.startDateTime).dayOfWeek().name().charAt(0) + LocalDateTime.parse(timeslot.startDateTime).dayOfWeek().name().slice(1).toLowerCase()}
                    ${LocalDateTime.parse(timeslot.startDateTime).format(timeFormatter)} - ${LocalDateTime.parse(timeslot.endDateTime).format(timeFormatter)}`))
            );
    });

    const tbodyBySpeaker = $("<tbody>").appendTo(scheduleBySpeaker);

    $.each(schedule.speakers.sort((a, b) => a.name > b.name ? 1 : (a.name < b.name ? -1 : 0)), (index, speaker) => {
        const rowBySpeaker = $("<tr>").appendTo(tbodyBySpeaker);
        rowBySpeaker
            .append($(`<th class="align-middle"/>`)
                .append($("<span/>").text(speaker.name)));
        $.each(schedule.timeslots.sort((a, b) => compareTimeslots(a, b)), (index, timeslot) => {
            rowBySpeaker.append($("<td style=\"white-space: normal; word-wrap: break-word; overflow-wrap: break-word;\"/>").prop("id", `speaker${speaker.id}timeslot${timeslot.id}`));
        });
    });

    $.each(schedule.talks.sort((a, b) => a.code > b.code ? 1 : (a.code < b.code ? -1 : 0)), (index, talk) => {
        $.each(talk.speakerIds || [], (_, speakerId) => {
            const talkElement = $(`<div class="card"/>`)
                .append($(`<div class="card-body p-2"/>`)
                    .append($(`<h5 class="card-title mb-1"/>`).text(`${talk.title}`))
                    .append($(`<p class="card-text ms-2 mb-1"/>`)
                        .append($(`<em/>`).text(`code ${talk.code}`))));
            if (isAssigned(talk)) {
                $(`#speaker${speakerId}timeslot${talk.timeslotId}`).append(talkElement.clone());
            } else {
                unassignedTalks.append($(`<div class="col"/>`).append(talkElement));
            }
        });
    });

    renderUnassignedBanner(unassignedTalks);
}

function renderScheduleByThemeTrack(schedule) {
    const allTalkThemes = schedule.talks.flatMap(t => t.themeTrackTags).sort();
    const themes = [...new Set(allTalkThemes)];
    renderScheduleByValues(schedule, "#scheduleByThemeTrack", "Theme Track Tag", "theme", "themeTrackTags", themes);
}

function renderScheduleBySectors(schedule) {
    const allTalkSectors = schedule.talks.flatMap(t => t.sectorTags).sort();
    const sectors = [...new Set(allTalkSectors)];
    renderScheduleByValues(schedule, "#scheduleBySectors", "Sector Tag", "sector", "sectorTags", sectors);
}

function renderScheduleByAudienceType(schedule) {
    const allAudienceTypes = schedule.talks.flatMap(t => t.audienceTypes).sort();
    const audienceTypes = [...new Set(allAudienceTypes)];
    renderScheduleByValues(schedule, "#scheduleByAudienceType", "Audience Type Tag", "audience_type", "audienceTypes", audienceTypes);
}

function renderScheduleByAudienceLevel(schedule) {
    const allAudienceLevels = schedule.talks.map(t => t.audienceLevel).sort();
    const audienceLevels = [...new Set(allAudienceLevels)];
    renderScheduleByValues(schedule, "#scheduleByAudienceLevel", "Audience Level", "audience_level", "audienceLevel", audienceLevels, true);
}

function renderScheduleByValues(schedule, tableKey, rowTitle, rowKey, key, values, singleValue = false) {
    const scheduleByValue = $(tableKey);
    scheduleByValue.children().remove();

    const unassignedTalks = $("#unassignedTalks");
    unassignedTalks.children().remove();

    const colgroup = $("<colgroup>").appendTo(scheduleByValue)
    colgroup.append('<col style="width: 250px">');
    $.each(schedule.timeslots, item => {
        colgroup.append('<col style="width: 250px">');
    })

    const theadByValue = $("<thead>").appendTo(scheduleByValue);
    const headerRowByValue = $("<tr>").appendTo(theadByValue);
    headerRowByValue.append($(`<th>${rowTitle}</th>`));

    const LocalDateTime = JSJoda.LocalDateTime;

    $.each(schedule.timeslots.sort((a, b) => compareTimeslots(a, b)), (index, timeslot) => {
        headerRowByValue
            .append($("<th/>")
                .append($("<span/>").text(`
                    ${LocalDateTime.parse(timeslot.startDateTime).dayOfWeek().name().charAt(0) + LocalDateTime.parse(timeslot.startDateTime).dayOfWeek().name().slice(1).toLowerCase()}
                    ${LocalDateTime.parse(timeslot.startDateTime).format(timeFormatter)}
                    -
                    ${LocalDateTime.parse(timeslot.endDateTime).format(timeFormatter)}
                `))
                .append($(`<button type="button" class="ms-2 mb-1 btn btn-light btn-sm p-1"/>`))
            );
    });

    const tbodyByValue = $("<tbody>").appendTo(scheduleByValue);

    $.each(values, (index, value) => {
        const rowByValue = $("<tr>").appendTo(tbodyByValue);
        rowByValue
            .append($(`<th class="align-middle"/>`)
                .append($("<span/>").text(value)));
        $.each(schedule.timeslots.sort((a, b) => compareTimeslots(a, b)), (index, timeslot) => {
            rowByValue.append($("<td/>").prop("id", `${rowKey}${value}timeslot${timeslot.id}`));
        });
    });

    $.each(schedule.talks.sort((a, b) => a.code > b.code ? 1 : (a.code < b.code ? -1 : 0)), (index, talk) => {
        const roomName = roomById.get(talk.roomId)?.name ?? 'not scheduled';
        if (singleValue) {
            const value = talk[key];
            const color = pickColor(value);
            const talkElement = $(`<div class="card" style="background-color: ${color.bg};color:${color.fg}"/>`)
                .append($(`<div class="card-body p-2"/>`)
                    .append($(`<h5 class="card-title mb-1 text-truncate"/>`).text(`${talk.code}: ${talk.title}`))
                    .append($(`<p class="card-text ms-2 mb-1"/>`)
                        .append($(`<em/>`).text(`by ${talkSpeakerNames(talk)} ${roomName}`))));
            if (isAssigned(talk)) {
                $(`#${rowKey}${value}timeslot${talk.timeslotId}`).append(talkElement.clone());
            } else {
                unassignedTalks.append($(`<div class="col"/>`).append(talkElement));
            }
        } else {
            $.each(talk[key], (_, value) => {
                const color = pickColor(value);
                const talkElement = $(`<div class="card" style="background-color: ${color.bg};color:${color.fg}"/>`)
                    .append($(`<div class="card-body p-2"/>`)
                        .append($(`<h5 class="card-title mb-1 text-truncate"/>`).text(`${talk.code}: ${talk.title}`))
                        .append($(`<p class="card-text ms-2 mb-1"/>`)
                            .append($(`<em/>`).text(`by ${talkSpeakerNames(talk)} at ${roomName}`))));
                if (isAssigned(talk)) {
                    $(`#${rowKey}${value}timeslot${talk.timeslotId}`).append(talkElement.clone());
                } else {
                    unassignedTalks.append($(`<div class="col"/>`).append(talkElement));
                }
            });
        }
    });

    renderUnassignedBanner(unassignedTalks);
}

function renderUnassignedBanner(unassignedTalks) {
    if (unassignedTalks.children().length === 0) {
        const banner = $(`<div class="col-12"/>`)
            .append($(`<div class="alert alert-success d-flex align-items-center justify-content-center" role="alert"/>`)
                .append($(`<i class="fas fa-check-circle me-2"/>`))
                .append($(`<span/>`).text("All talks have been assigned!")));
        unassignedTalks.append(banner);
    }
}

function analyze() {
    new bootstrap.Modal("#scoreAnalysisModal").show()
    const scoreAnalysisModalContent = $("#scoreAnalysisModalContent");
    scoreAnalysisModalContent.children().remove();
    if (jobId == null || loadedSchedule == null || loadedSchedule.score == null || loadedSchedule.score === "") {
        scoreAnalysisModalContent.text("No score to analyze yet, please first press the 'solve' button.");
        return;
    }
    $('#scoreAnalysisScoreLabel').text(`(${loadedSchedule.score})`);
    $.get(api(`${MODEL_PATH}/${jobId}/score-analysis?includeJustifications=true`), function (scoreAnalysis) {
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
            const matches = constraintAnalysis.matches ?? [];
            const matchCount = constraintAnalysis.matchCount ?? matches.length;
            let icon = constraintAnalysis.type == "hard" && constraintAnalysis.implicitScore < 0 ? '<span class="fas fa-exclamation-triangle" style="color: red"></span>' : '';
            if (!icon) icon = matchCount == 0 ? '<span class="fas fa-check-circle" style="color: green"></span>' : '';

            let row = $(`<tr/>`);
            row.append($(`<td/>`).html(icon))
                .append($(`<td/>`).text(constraintAnalysis.name).css({textAlign: 'left'}))
                .append($(`<td/>`).text(constraintAnalysis.type))
                .append($(`<td/>`).html(`<b>${matchCount}</b>`))
                .append($(`<td/>`).text(constraintAnalysis.weight))
                .append($(`<td/>`).text(constraintAnalysis.implicitScore));

            analysisTBody.append(row);

            if (matches.length > 0) {
                let matchesRow = $(`<tr/>`).addClass("collapse").attr("id", "row" + index + "Collapse");
                let matchesListGroup = $(`<ul/>`).addClass('list-group').addClass('list-group-flush').css({textAlign: 'left'});

                $.each(matches, (_, match) => {
                    matchesListGroup.append($(`<li/>`).addClass('list-group-item').addClass('list-group-item-light')
                        .text(match.justification?.description ?? match.score));
                });

                matchesRow.append($(`<td/>`));
                matchesRow.append($(`<td/>`).attr('colspan', '6').append(matchesListGroup));
                analysisTBody.append(matchesRow);

                row.append($(`<td/>`).append($(`<a/>`).attr("data-toggle", "collapse").attr('href', "#row" + index + "Collapse").append($(`<span/>`).addClass('fas').addClass('fa-chevron-down')).click(e => {
                    matchesRow.collapse('toggle');
                    let target = $(e.target);
                    if (target.hasClass('fa-chevron-down')) {
                        target.removeClass('fa-chevron-down').addClass('fa-chevron-up');
                    } else {
                        target.removeClass('fa-chevron-up').addClass('fa-chevron-down');
                    }
                })));
            } else {
                row.append($(`<td/>`));
            }

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
    });
}

function getScoreComponents(score) {
    let components = {hard: 0, medium: 0, soft: 0};

    $.each([...score.matchAll(/(-?[0-9]+)(hard|medium|soft)/g)], (i, parts) => {
        components[parts[2]] = parseInt(parts[1], 10);
    });

    return components;
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
