// ── Platform context ──
// When embedded in the Timefold Platform, the iframe URL carries these query params.
// Standalone (local dev), none are present and the app behaves as before.
const PLATFORM = (function () {
    const q = new URL(window.location.href).searchParams;
    return {
        onPlatform: q.has('onPlatform'),
        runId: q.get('runId'),
        apiUrl: q.has('apiUrl') ? decodeURIComponent(q.get('apiUrl')).replace(/\/+$/, '') : null,
    };
})();

// Build an API URL: prefix the platform base when embedded, else root-relative (local dev).
function api(path) {
    return PLATFORM.apiUrl ? PLATFORM.apiUrl + path : path;
}

const MODEL_PATH = '/v1/schedules';
const DEMO_DATA_PATH = '/v1/demo-data/BASIC';

var autoRefreshIntervalId = null;

const byRoomPanel = document.getElementById("byRoomPanel");
const byRoomTimelineOptions = {
    timeAxis: {scale: "day"},
    orientation: {axis: "top"},
    stack: false,
    xss: {disabled: true}, // Items are XSS safe through JQuery
    zoomMin: 3 * 1000 * 60 * 60 * 24 // Three day in milliseconds
};
var byRoomGroupData = new vis.DataSet();
var byRoomItemData = new vis.DataSet();
var byRoomTimeline = new vis.Timeline(byRoomPanel, byRoomItemData, byRoomGroupData, byRoomTimelineOptions);

let jobId = null;
let loadedSchedule = null;
let viewType = "R";
// Set when stop is pressed before the POST that creates the run has returned a jobId.
let stopRequested = false;

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
    const beds = schedule.departments.flatMap(d => d.rooms).flatMap(r => r.beds);
    $("#score").text("Score: " + (schedule.score == null || schedule.score === "" ? "?" : schedule.score));
    $("#info").text(`This dataset has ${schedule.stays.length} stays and ${beds.length} beds across ${schedule.departments.length} departments.`);

    if (viewType === "R") {
        renderScheduleByRoom(schedule);
    }
}

function renderScheduleByRoom(schedule) {
    const unassignedPatients = $("#unassignedPatients");
    unassignedPatients.children().remove();
    byRoomGroupData.clear();
    byRoomItemData.clear();

    $.each(schedule.departments.flatMap(d => d.rooms), (_, room) => {
        let content = `<div class="d-flex flex-column"><div><h5 class="card-title mb-1">${room.name}</h5></div>`;
        if (room.equipments.length > 0) {
            let equipments = room.equipments.sort().slice(0, Math.min(2, room.equipments.length));
            content += `<div class="d-flex">`;
            equipments.forEach(e => content += `<div><span class="badge text-bg-success m-1">${e}</span></div>`);
            content += "</div>";
            if (room.equipments.length > 2) {
                let equipments = room.equipments.sort().slice(2, Math.min(4, room.equipments.length));
                content += `<div class="d-flex">`;
                equipments.forEach(e => content += `<div><span class="badge text-bg-success m-1">${e}</span></div>`);
                content += "</div>";
            }
        }

        const roomData = {
            id: room.id,
            content: content,
            treeLevel: 1,
            nestedLevels: [...room.beds.map(b => b.id)]
        };
        byRoomGroupData.add(roomData);
        room.beds.forEach((bed, index) => byRoomGroupData.add({
            id: bed.id,
            content: `Bed ${index + 1}`,
            treeLevel: 2
        }));
    });

    $.each(schedule.stays, (_, stay) => {
        const bgcolor = stay.patientGender === 'MALE' ? '#729FCF' : '#FCE94F';
        const color = stay.patientGender === 'MALE' ? 'white' : 'black';

        if (stay.bedId == null) {
            const unassignedPatientElement = $(`<div class="card-body p-2"/>`)
                .append($(`<h5 class="card-title mb-1"/>`).text(`${stay.patientName} (${stay.patientGender.substring(0, 1)})`))
                .append($(`<p class="card-text ms-2 mb-0"/>`).text(`${JSJoda.LocalDate.parse(stay.arrivalDate)
                    .until(JSJoda.LocalDate.parse(stay.departureDate), JSJoda.ChronoUnit.DAYS)} day(s)`))
                .append($(`<p class="card-text ms-2 mb-0"/>`).text(`Arrival: ${stay.arrivalDate}`))
                .append($(`<p class="card-text ms-2 mb-0"/>`).text(`Departure: ${stay.departureDate}`));

            unassignedPatientElement
                .append($(`<p class="card-text mb-0"/>`).append($(`<span class="badge rounded-pill text-bg-primary m-1"/>`)
                    .text(stay.specialty)));

            const equipmentDiv = $("<div />").prop("class", "col");
            unassignedPatientElement.append(equipmentDiv);
            stay.patientRequiredEquipments.sort().forEach(e => {
                equipmentDiv.append($(`<span class="badge text-bg-success m-1"/>`).text(e))
            });
            const preferredEquipmentDiv = $("<div />").prop("class", "col");
            unassignedPatientElement.append(preferredEquipmentDiv);
            if (stay.patientPreferredEquipments && stay.patientPreferredEquipments.length > 0) {
                stay.patientPreferredEquipments
                    .filter(e => stay.patientRequiredEquipments.indexOf(e) == -1)
                    .sort()
                    .forEach(e => preferredEquipmentDiv.append($(`<span class="badge text-bg-secondary m-1"/>`).text(e)));
            }
            unassignedPatientElement.append($("<div />").prop("class", "d-flex justify-content-end").append($(`<small class="ms-2 mt-1 card-text text-muted"/>`)
                .text(stay.patientPreferredMaximumRoomCapacity)));

            unassignedPatients.append($(`<div class="col"/>`).append($(`<div class="card" style="background-color: ${bgcolor};color:${color}"/>`).append(unassignedPatientElement)));
            byRoomItemData.add({
                id: stay.id,
                group: stay.id,
                start: stay.arrivalDate,
                end: stay.departureDate,
                style: "background-color: #EF292999"
            });
        } else {
            const byPatientElement = $(`<div />`)
                .append($(`<h5 class="card-title mb-1"/>`).text(`${stay.patientName} (${stay.patientGender.substring(0, 1)})`));

            byPatientElement
                .append($(`<p class="card-text mb-0"/>`).append($(`<span class="badge rounded-pill text-bg-primary m-1"/>`)
                    .text(stay.specialty)));

            const equipmentDiv = $("<div />").prop("class", "col");
            byPatientElement.append(equipmentDiv);
            stay.patientRequiredEquipments.sort().forEach(e => {
                equipmentDiv.append($(`<span class="badge text-bg-success m-1"/>`).text(e))
            });
            const preferredEquipmentDiv = $("<div />").prop("class", "col");
            byPatientElement.append(preferredEquipmentDiv);
            if (stay.patientPreferredEquipments && stay.patientPreferredEquipments.length > 0) {
                stay.patientPreferredEquipments
                    .filter(e => stay.patientRequiredEquipments.indexOf(e) == -1)
                    .sort()
                    .forEach(e => preferredEquipmentDiv.append($(`<span class="badge text-bg-secondary m-1"/>`).text(e)));
            }
            byPatientElement.append($("<div />").prop("class", "d-flex justify-content-end").append($(`<small class="ms-2 mt-1 card-text text-muted"/>`)
                .text(stay.patientPreferredMaximumRoomCapacity)));

            byRoomItemData.add({
                id: stay.id,
                group: stay.bedId,
                content: byPatientElement.html(),
                start: stay.arrivalDate,
                end: stay.departureDate,
                style: `background-color: ${bgcolor}; color: ${color}`
            });
        }
    });
    // Show banner if no unassigned items
    if (unassignedPatients.children().length === 0) {
        const banner = $(`<div class="col-12"/>`)
            .append($(`<div class="alert alert-success d-flex align-items-center justify-content-center" role="alert"/>`)
                .append($(`<i class="fas fa-check-circle me-2"/>`))
                .append($(`<span/>`).text("All stays have been assigned!")));
        unassignedPatients.append(banner);
    }

    const arrivalDates = schedule.stays.map(s => s.arrivalDate);
    const departureDates = schedule.stays.map(s => s.departureDate);
    const allDates = [...new Set([...arrivalDates, ...departureDates])]
        .sort((a, b) => JSJoda.LocalDate.parse(a).compareTo(JSJoda.LocalDate.parse(b)));
    byRoomTimeline.setWindow(allDates[0], allDates[allDates.length - 1]);
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

                row.append($(`<td/>`).append($(`<a/>`)
                    .attr('href', "#row" + index + "Collapse")
                    .append($(`<span/>`).addClass('fas').addClass('fa-chevron-down'))
                    .click(e => {
                        e.preventDefault();
                        const collapseEl = matchesRow.get(0);
                        bootstrap.Collapse.getOrCreateInstance(collapseEl).toggle();
                        let icon = $(e.currentTarget).find('span.fas');
                        if (icon.hasClass('fa-chevron-down')) {
                            icon.removeClass('fa-chevron-down').addClass('fa-chevron-up');
                        } else {
                            icon.removeClass('fa-chevron-up').addClass('fa-chevron-down');
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
