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
        const homeTeam = teamMap.get(match.homeTeam);
        const awayTeam = teamMap.get(match.awayTeam);
        if (match.round == null) {
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
                start: currentDate.plusDays(match.round).toString(),
                end: currentDate.plusDays(match.round + 1).toString(),
                style: `background-color: ${match.classicMatch ? '#009E73' : '#0072B2'}; color: white`
            });
            byTeamItemData.add({
                id: `${match.id}-2`,
                group: awayTeam.id,
                content: byAwayTeamElement.html(),
                start: currentDate.plusDays(match.round).toString(),
                end: currentDate.plusDays(match.round + 1).toString(),
                style: `background-color: ${match.classicMatch ? '#009E73' : '#0072B2'}; color: white`
            });
        }
    });

    byTeamTimeline.setWindow(JSJoda.LocalDate.now().toString(), JSJoda.LocalDate.now().plusDays(7).toString());
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
