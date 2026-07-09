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

const byTeamPanel = document.getElementById("byTeamPanel");
let byTeamGroupData = new vis.DataSet();
let byTeamItemData = new vis.DataSet();
let byTeamTimeline = new vis.Timeline(byTeamPanel, byTeamItemData, byTeamGroupData, byTimelineOptions);

let scheduleId = null;
let loadedSchedule = null;
let viewType = "T";

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
    $("#byTeamTab").click(function () {
        viewType = "T";
        getStatus();
    });

    setupAjax();
    if (PLATFORM.onPlatform) {
        // Embedded: hide demo chrome (navbar + solve/score controls) and load the
        // existing run read-only.
        document.body.classList.add('on-platform');
        loadPlatformRun();
    } else {
        // Standalone dev: load demo data, allow solving.
        getStatus();
    }
});

// ── Platform: load an existing run (read-only) ──
// ModelRest exposes the run's input at /{id}/model-request (ModelRequest → {modelInput})
// and its output+status at /{id} (ModelResponse → {metadata:{solverStatus}, modelOutput}).
function loadPlatformRun() {
    if (!PLATFORM.runId) {
        showError("No runId provided by platform.", {status: 0, statusText: "missing runId"});
        return;
    }
    scheduleId = PLATFORM.runId;
    $.getJSON(api(`/v1/schedules/${scheduleId}/model-request`), function (req) {
        loadedSchedule = req.modelInput || req;
        renderSchedule(loadedSchedule, 'NOT_SOLVING');
        getStatus(); // fetch output + render; auto-polls while solving
    }).fail(function (xhr) {
        showError("Failed to load run input from platform.", xhr);
    });
}

function setupAjax() {
    $.ajaxSetup({
        headers: {
            'Content-Type': 'application/json', 'Accept': 'application/json,text/plain', // plain text is required by solve() returning UUID of the solver job
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

function isSolving(solverStatus) {
    return solverStatus === 'SOLVING_ACTIVE' || solverStatus === 'SOLVING_SCHEDULED'
        || solverStatus === 'SOLVING_STARTED';
}

function getStatus() {
    if (scheduleId == null) {
        $.getJSON(api("/v1/demo-data/BASIC"), function (data) {
            loadedSchedule = data.modelInput;
            $('#exportData').attr('href', 'data:text/plain;charset=utf-8,' + JSON.stringify(loadedSchedule));
            renderSchedule(loadedSchedule, 'NOT_SOLVING');
        }).fail(function (xhr) {
            showError("Getting the demo data has failed.", xhr);
            refreshSolvingButtons(false);
        });
    } else {
        $.getJSON(api(`/v1/schedules/${scheduleId}`), function (data) {
            const solverStatus = data.metadata.solverStatus;
            const solution = data.modelOutput || loadedSchedule;
            loadedSchedule = solution;
            $('#exportData').attr('href', 'data:text/plain;charset=utf-8,' + JSON.stringify(loadedSchedule));
            renderSchedule(solution, solverStatus);
        }).fail(function (xhr) {
            showError("Getting the schedule has failed.", xhr);
            refreshSolvingButtons(false);
        });
    }
}

function renderSchedule(schedule, solverStatus) {
    refreshSolvingButtons(isSolving(solverStatus));
    $("#score").text("Score: " + (schedule.score == null ? "?" : schedule.score));
    $("#info").text(`This dataset has ${schedule.matches.length} matches (${schedule.rounds.length} rounds) for ${schedule.teams.length} teams.`);

    if (viewType === "T") {
        renderScheduleByTeam(schedule);
    }
}

function renderScheduleByTeam(schedule) {
    const unassigned = $("#unassigned");
    unassigned.children().remove();
    byTeamGroupData.clear();
    byTeamItemData.clear();

    const teamMap = new Map();
    $.each(schedule.teams.sort((t1, t2) => t1.name.localeCompare(t2.name)), (_, team) => {
        teamMap.set(team.id, team);
        let content = `<div class="d-flex flex-column"><div><h5 class="card-title mb-1">${team.name}</h5></div>`;
        byTeamGroupData.add({
            id: team.id,
            content: content,
        });
    });

    const currentDate = JSJoda.LocalDate.now();
    $.each(schedule.matches, (_, match) => {
        const homeTeam = teamMap.get(match.homeTeamId);
        const awayTeam = teamMap.get(match.awayTeamId);
        if (match.roundIndex == null) {
            const unassignedElement = $(`<div class="card-body" style="background-color: ${match.classicMatch ? '#009E73' : '#0072B2'}; color: white"/>`)
                .append($(`<h5 class="card-title mb-1"/>`).text(`${homeTeam.name} x ${awayTeam.name}`));

            unassigned.append($(`<div class="pl-1"/>`).append($(`<div class="card"/>`).append(unassignedElement)));
        } else {
            const byHomeTeamElement = $("<div />").append($("<div class='d-flex justify-content-center align-items-center' />").append($(`<h5 class="card-title mb-1"/>`).text(awayTeam.name)).append($(`<small class="ms-2 card-text "/>`).append("<span class='fas fa-solid fa-home text-white' title='Home Match' />")));
            const byAwayTeamElement = $("<div />").append($("<div class='d-flex justify-content-center align-items-center' />").append($(`<h5 class="card-title mb-1"/>`).text(homeTeam.name)).append($(`<small class="ms-2 card-text "/>`).append("<span class='fas fa-plane-departure text-white' title='Away Match' />")));
            byTeamItemData.add({
                id: `${match.id}-1`,
                group: homeTeam.id,
                content: byHomeTeamElement.html(),
                start: currentDate.plusDays(match.roundIndex).toString(),
                end: currentDate.plusDays(match.roundIndex + 1).toString(),
                style: `background-color: ${match.classicMatch ? '#009E73' : '#0072B2'}; color: white`
            });
            byTeamItemData.add({
                id: `${match.id}-2`,
                group: awayTeam.id,
                content: byAwayTeamElement.html(),
                start: currentDate.plusDays(match.roundIndex).toString(),
                end: currentDate.plusDays(match.roundIndex + 1).toString(),
                style: `background-color: ${match.classicMatch ? '#009E73' : '#0072B2'}; color: white`
            });
        }
    });

    byTeamTimeline.setWindow(JSJoda.LocalDate.now().toString(), JSJoda.LocalDate.now().plusDays(7).toString());

    if (unassigned.children().length === 0) {
        const banner = $(`<div class="col-12"/>`)
            .append($(`<div class="alert alert-success d-flex align-items-center justify-content-center" role="alert"/>`)
                .append($(`<i class="fas fa-check-circle me-2"/>`))
                .append($(`<span/>`).text("All matches have been assigned!")));
        unassigned.append(banner);
    }
}

function solve() {
    $.getJSON(api("/v1/demo-data/BASIC"), function (modelRequest) {
        $.post(api("/v1/schedules"), JSON.stringify(modelRequest), function (metadata) {
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
    new bootstrap.Modal("#scoreAnalysisModal").show()
    const scoreAnalysisModalContent = $("#scoreAnalysisModalContent");
    scoreAnalysisModalContent.children().remove();
    if (loadedSchedule == null || loadedSchedule.score == null) {
        scoreAnalysisModalContent.text("No score to analyze yet, please first press the 'solve' button.");
    } else if (scheduleId == null) {
        scoreAnalysisModalContent.text("No solving job yet, please first press the 'solve' button.");
    } else {
        $('#scoreAnalysisScoreLabel').text(`(${loadedSchedule.score})`);
        $.get(api(`/v1/schedules/${scheduleId}/score-analysis`), function (scoreAnalysis) {
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
    $.delete(api(`/v1/schedules/${scheduleId}`), function () {
        refreshSolvingButtons(false);
        getStatus();
    }).fail(function (xhr) {
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
