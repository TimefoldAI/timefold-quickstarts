var autoRefreshIntervalId = null;
const timeFormatter = JSJoda.DateTimeFormatter.ofPattern('HH:mm');

let scheduleId = null;
let loadedSchedule = null;
let viewType = "R";

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
    $("#byRoomTab").click(function () {
        viewType = "R";
        refreshSchedule();
    });
    $("#bySpeakerTab").click(function () {
        viewType = "S";
        refreshSchedule();
    });
    $("#byThemeTrackTab").click(function () {
        viewType = "TH";
        refreshSchedule();
    });
    $("#bySectorsTab").click(function () {
        viewType = "SC";
        refreshSchedule();
    });
    $("#byAudienceTypeTab").click(function () {
        viewType = "AT";
        refreshSchedule();
    });
    $("#byAudienceLevelTab").click(function () {
        viewType = "AL";
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
        renderSchedule(schedule);
    })
        .fail(function (xhr, ajaxOptions, thrownError) {
            showError("Getting the timetable has failed.", xhr);
            refreshSolvingButtons(false);
        });
}

function renderSchedule(schedule) {
    refreshSolvingButtons(schedule.solverStatus != null && schedule.solverStatus !== "NOT_SOLVING");
    $("#score").text("Score: " + (schedule.score == null ? "?" : schedule.score));

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

function renderScheduleByRoom(schedule) {
    const scheduleByRoom = $("#scheduleByRoom");
    scheduleByRoom.children().remove();

    const unassignedTalks = $("#unassignedTalks");
    unassignedTalks.children().remove();

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
        const color = pickColor(talk.talkType);
        const talkElement = $(`<div class="card" style="background-color: ${color}"/>`)
            .append($(`<div class="card-body p-2"/>`)
                .append($(`<h5 class="card-title mb-1 text-truncate"/>`).text(`${talk.code}: ${talk.title}`))
                .append($(`<p class="card-text ms-2 mb-1"/>`)
                    .append($(`<em/>`).text(`by ${talk.speakers.map(s => s.name).join(", ")}`))));
        if (talk.timeslot != null && talk.room != null) {
            $(`#timeslot${talk.timeslot.id}room${talk.room.id}`).append(talkElement.clone());
        } else {
            unassignedTalks.append($(`<div class="col"/>`).append(talkElement));
        }
    });
}

function compareTimeslots(t1, t2) {
    const LocalDateTime = JSJoda.LocalDateTime;
    const diff = LocalDateTime.parse(t1.startDateTime).compareTo(LocalDateTime.parse(t2.startDateTime));
    if (diff === 0) {
        return LocalDateTime.parse(t1.endDateTime).compareTo(LocalDateTime.parse(t2.endDateTime));
    }
    return diff;
}

function renderScheduleBySpeaker(schedule) {
    const scheduleBySpeaker = $("#scheduleBySpeaker");
    scheduleBySpeaker.children().remove();

    const unassignedTalks = $("#unassignedTalks");
    unassignedTalks.children().remove();

    const theadBySpeaker = $("<thead>").appendTo(scheduleBySpeaker);
    const headerRowBySpeaker = $("<tr>").appendTo(theadBySpeaker);
    headerRowBySpeaker.append($("<th>Speaker</th>"));

    const LocalDateTime = JSJoda.LocalDateTime;

    $.each(schedule.timeslots.sort((a, b) => a.id > b.id ? 1 : (a.id < b.id ? -1 : 0)), (index, timeslot) => {
        headerRowBySpeaker
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

    const tbodyBySpeaker = $("<tbody>").appendTo(scheduleBySpeaker);


    $.each(schedule.speakers, (index, speaker) => {
        const rowBySpeaker = $("<tr>").appendTo(tbodyBySpeaker);
        rowBySpeaker
            .append($(`<th class="align-middle"/>`)
                .append($("<span/>").text(speaker.name)));
        $.each(schedule.timeslots.sort((a, b) => compareTimeslots(a, b)), (index, timeslot) => {
            rowBySpeaker.append($("<td/>").prop("id", `speaker${speaker.id}timeslot${timeslot.id}`));
        });
    });

    $.each(schedule.talks.sort((a, b) => a.code > b.code ? 1 : (a.code < b.code ? -1 : 0)), (index, talk) => {
        $.each(talk.speakers, (_, speaker) => {
            const color = pickColor(speaker.name);
            const talkElement = $(`<div class="card" style="background-color: ${color}"/>`)
                .append($(`<div class="card-body p-2"/>`)
                    .append($(`<h5 class="card-title mb-1 text-truncate"/>`).text(`${talk.code}: ${talk.title}`))
                    .append($(`<p class="card-text ms-2 mb-1"/>`)
                        .append($(`<em/>`).text(`by ${speaker.name}`))));
            if (talk.timeslot != null && talk.room != null) {
                $(`#speaker${speaker.id}timeslot${talk.timeslot.id}`).append(talkElement.clone());
            } else {
                unassignedTalks.append($(`<div class="col"/>`).append(talkElement));
            }
        });
    });
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

    const theadByValue = $("<thead>").appendTo(scheduleByValue);
    const headerRowByValue = $("<tr>").appendTo(theadByValue);
    headerRowByValue.append($(`<th>${rowTitle}</th>`));

    const LocalDateTime = JSJoda.LocalDateTime;

    $.each(schedule.timeslots.sort((a, b) => a.id > b.id ? 1 : (a.id < b.id ? -1 : 0)), (index, timeslot) => {
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
        if (singleValue) {
            const value = talk[key];
            const color = pickColor(value);
            const talkElement = $(`<div class="card" style="background-color: ${color}"/>`)
                .append($(`<div class="card-body p-2"/>`)
                    .append($(`<h5 class="card-title mb-1 text-truncate"/>`).text(`${talk.code}: ${talk.title}`))
                    .append($(`<p class="card-text ms-2 mb-1"/>`)
                        .append($(`<em/>`).text(`at ${talk.room?.name ?? 'not scheduled'}`))));
            if (talk.timeslot != null && talk.room != null) {
                $(`#${rowKey}${value}timeslot${talk.timeslot.id}`).append(talkElement.clone());
            } else {
                unassignedTalks.append($(`<div class="col"/>`).append(talkElement));
            }
        } else {
            $.each(talk[key], (_, value) => {
                const color = pickColor(value);
                const talkElement = $(`<div class="card" style="background-color: ${color}"/>`)
                    .append($(`<div class="card-body p-2"/>`)
                        .append($(`<h5 class="card-title mb-1 text-truncate"/>`).text(`${talk.code}: ${talk.title}`))
                        .append($(`<p class="card-text ms-2 mb-1"/>`)
                            .append($(`<em/>`).text(`at ${talk.room?.name ?? 'not scheduled'}`))));
                if (talk.timeslot != null && talk.room != null) {
                    $(`#${rowKey}${value}timeslot${talk.timeslot.id}`).append(talkElement.clone());
                } else {
                    unassignedTalks.append($(`<div class="col"/>`).append(talkElement));
                }
            });
        }
    });
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

                if (constraintAnalysis.matches.length > 0) {
                    let matchesRow = $(`<tr/>`).addClass("collapse").attr("id", "row" + index + "Collapse");
                    let matchesListGroup = $(`<ul/>`).addClass('list-group').addClass('list-group-flush').css({textAlign: 'left'});

                    $.each(constraintAnalysis.matches, (_, match) => {
                        matchesListGroup.append($(`<li/>`).addClass('list-group-item').addClass('list-group-item-light').text(match.justification.description));
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
    var text = $("#" + id).text().trim();

    var dummy = document.createElement("textarea");
    document.body.appendChild(dummy);
    dummy.value = text;
    dummy.select();
    document.execCommand("copy");
    document.body.removeChild(dummy);
}

function compareTimeslots(t1, t2) {
    const LocalDateTime = JSJoda.LocalDateTime;
    let diff = LocalDateTime.parse(t1.startDateTime).compareTo(LocalDateTime.parse(t2.startDateTime));
    if (diff === 0) {
        diff = LocalDateTime.parse(t1.endDateTime).compareTo(LocalDateTime.parse(t2.endDateTime));
    }
    return diff;
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
