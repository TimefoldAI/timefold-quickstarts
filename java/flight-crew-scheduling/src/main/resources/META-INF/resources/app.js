let autoRefreshIntervalId = null;
const formatter = JSJoda.DateTimeFormatter.ofPattern("MM/dd/YYYY HH:mm").withLocale(JSJodaLocale.Locale.ENGLISH);

const zoomMin = 1000 * 60 * 60 * 8 // 2 hours in milliseconds
const zoomMax = 2 * 7 * 1000 * 60 * 60 * 24 // 2 weeks in milliseconds

const byTimelineOptions = {
    timeAxis: {scale: "hour", step: 8},
    orientation: {axis: "top"},
    stack: false,
    xss: {disabled: true}, // Items are XSS safe through JQuery
    zoomMin: zoomMin,
    zoomMax: zoomMax,
};

const byCrewPanel = document.getElementById("byCrewPanel");
let byCrewGroupData = new vis.DataSet();
let byCrewItemData = new vis.DataSet();
let byCrewTimeline = new vis.Timeline(byCrewPanel, byCrewItemData, byCrewGroupData, byTimelineOptions);

const byFlightPanel = document.getElementById("byFlightPanel");
let byFlightGroupData = new vis.DataSet();
let byFlightItemData = new vis.DataSet();
let byFlightTimeline = new vis.Timeline(byFlightPanel, byFlightItemData, byFlightGroupData, byTimelineOptions);

let scheduleId = null;
let loadedSchedule = null;
let viewType = "R";

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
    $("#byCrewTab").click(function () {
        viewType = "R";
        refreshSchedule();
    });
    $("#byFlightTab").click(function () {
        viewType = "F";
        refreshSchedule();
    });
    // HACK to allow vis-timeline to work within Bootstrap tabs
    $("#byCrewTab").on('shown.bs.tab', function (event) {
        byCrewTimeline.redraw();
    })
    $("#byFlightTab").on('shown.bs.tab', function (event) {
        byFlightTimeline.redraw();
    })

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
    $("#info").text(`This dataset has ${schedule.employees.length} employees which need to be assigned ${schedule.flightAssignments.length} tasks on ${schedule.flights.length} flights.`);

    if (viewType === "R") {
        renderScheduleByCrew(schedule);
    }
    if (viewType === "F") {
        renderScheduleByFlight(schedule);
    }
}

function getCrewIcon(employee) {
    return employee.skills.indexOf("Pilot") >= 0 ? '<span class="fas fa-solid fa-plane-departure" title="Pilot"></span>' :
        '<span class="fas fa-solid fa-glass-martini" title="Flight Attendant"></span>';

}

function renderScheduleByCrew(schedule) {
    const unassignedCrew = $("#unassignedCrew");
    unassignedCrew.children().remove();
    let unassignedCrewCount = 0;
    byCrewGroupData.clear();
    byCrewItemData.clear();

    $.each(schedule.employees.sort((e1, e2) => e1.name.localeCompare(e2.name)), (_, employee) => {
        const crewIcon = getCrewIcon(employee);
        let content = `<div class="d-flex flex-column"><div><h5 class="card-title mb-1">${employee.name} (${employee.homeAirport}) ${crewIcon}</h5></div>`;

        byCrewGroupData.add({
            id: employee.id,
            content: content,
        });

        // Unavailable days
        if (employee.unavailableDays) {
            let count = 0;
            employee.unavailableDays.forEach(date => {
                const unavailableDatetime = JSJoda.LocalDate.parse(date);
                byCrewItemData.add({
                    id: `${employee.id}-${count++}`,
                    group: employee.id,
                    content: $(`<div />`).html(),
                    start: unavailableDatetime.atStartOfDay().toString(),
                    end: unavailableDatetime.atStartOfDay().withHour(23).withMinute(59).toString(),
                    style: "background-color: gray; min-height: 50px"
                });
            });
        }
    });

    const flightMap = new Map();
    schedule.flights.forEach(f => flightMap.set(f.flightNumber, f));
    $.each(schedule.flightAssignments, (_, assignment) => {
        const flight = flightMap.get(assignment.flight);
        if (assignment.employee == null) {
            unassignedCrewCount++;
            const departureDateTime = JSJoda.LocalDateTime.parse(flight.departureUTCDateTime);
            const arrivalDateTime = JSJoda.LocalDateTime.parse(flight.arrivalUTCDateTime);
            const unassignedElement = $(`<div class="card-body"/>`)
                .append($(`<h5 class="card-title mb-1"/>`).text(`${flight.departureAirport} → ${flight.arrivalAirport}`))
                .append($(`<p class="card-text ms-2 mb-0"/>`).text(`${departureDateTime.until(arrivalDateTime, JSJoda.ChronoUnit.HOURS)} hour(s)`))
                .append($(`<p class="card-text ms-2 mb-0"/>`).text(`Departure: ${formatter.format(departureDateTime)}`))
                .append($(`<p class="card-text ms-2 mb-0"/>`).text(`Arrival: ${formatter.format(arrivalDateTime)}`));

            unassignedCrew.append($(`<div class="pl-1"/>`).append($(`<div class="card"/>`).append(unassignedElement)));
            byCrewItemData.add({
                id: assignment.id,
                group: assignment.employee,
                start: formatter.format(departureDateTime),
                end: formatter.format(arrivalDateTime),
                style: "background-color: #EF292999"
            });
        } else {
            const byCrewElement = $("<div />").append($("<div class='d-flex justify-content-center' />").append($(`<h5 class="card-title mb-1"/>`).text(`${flight.departureAirport} → ${flight.arrivalAirport}`)));
            byCrewItemData.add({
                id: assignment.id,
                group: assignment.employee,
                content: byCrewElement.html(),
                start: flight.departureUTCDateTime,
                end: flight.arrivalUTCDateTime,
                style: "min-height: 50px"
            });
        }
    });
    if (unassignedCrew.children().length === 0) {
        const banner = $(`<div class="col-12"/>`)
            .append($(`<div class="alert alert-success d-flex align-items-center justify-content-center" role="alert"/>`)
                .append($(`<i class="fas fa-check-circle me-2"/>`))
                .append($(`<span/>`).text("All crew members have been assigned!")));
        unassignedCrew.append(banner);
    }
    byCrewTimeline.setWindow(JSJoda.LocalDateTime.now().minusMinutes(1).toString(),
        JSJoda.LocalDateTime.now().plusDays(4).withHour(23).withMinute(59).toString());
    byCrewTimeline.redraw();
}

function renderScheduleByFlight(schedule) {
    const unassignedCrew = $("#unassignedCrew");
    unassignedCrew.children().remove();
    byFlightGroupData.clear();
    byFlightItemData.clear();

    $.each(schedule.flights.sort((e1, e2) => JSJoda.LocalDateTime.parse(e1.departureUTCDateTime)
        .compareTo(JSJoda.LocalDateTime.parse(e2.departureUTCDateTime))), (_, flight) => {
        let content = `<div class="d-flex flex-column"><div><h5 class="card-title mb-1">${flight.departureAirport} → ${flight.arrivalAirport}</h5></div>`;

        byFlightGroupData.add({
            id: flight.flightNumber,
            content: content,
        });
    });

    const employeeMap = new Map();
    schedule.employees.forEach(e => employeeMap.set(e.id, e));

    $.each(schedule.flights, (_, flight) => {
        const content = $(`<div class="card-body"/>`).append($(`<h4 class="card-title mb-1"/>`).text(flight.flightNumber));
        const unassignedElement = $(`<div class="card-body"/>`).append($(`<h4 class="card-title mb-1"/>`).text(`${flight.departureAirport} → ${flight.arrivalAirport}`));
        const assignments = schedule.flightAssignments.filter(f => f.flight === flight.flightNumber);
        let countUnassigned = 0;
        const missingSkills = [];
        const pilots = [];
        const attendants = [];
        assignments.forEach(assigment => {
            if (assigment.employee == null) {
                countUnassigned++;
                missingSkills.push(assigment.requiredSkill);
            } else {
                const employee = employeeMap.get(assigment.employee);
                if (assigment.requiredSkill === 'Pilot') {
                    pilots.push(employee.name);
                } else {
                    attendants.push(employee.name);
                }
            }
        });

        if (pilots.length > 0 && attendants.length > 0) {
            content.append($(`<p class="card-text" style="font-weight: bold"/>`).text(`Pilot(s)`));
            pilots.sort().forEach(pilot => content.append($(`<p class="card-text mx-2"/>`).text(pilot)));
            content.append($(`<p class="card-text" style="font-weight: bold"/>`).text(`Attendant(s)`));
            attendants.sort().forEach(attendant => content.append($(`<p class="card-text mx-2"/>`).text(attendant)));
            byFlightItemData.add({
                id: flight.flightNumber,
                group: flight.flightNumber,
                content: $('<div class="d-flex flex-column" />').append(content).html(),
                start: flight.departureUTCDateTime,
                end: flight.arrivalUTCDateTime,
            });
        }
        if (unassignedCrew.children().length === 0) {
            const banner = $(`<div class="col-12"/>`)
                .append($(`<div class="alert alert-success d-flex align-items-center justify-content-center" role="alert"/>`)
                    .append($(`<i class="fas fa-check-circle me-2"/>`))
                    .append($(`<span/>`).text("All crew members have been assigned!")));
            unassignedCrew.append(banner);
        }
    });

    byFlightTimeline.setWindow(JSJoda.LocalDateTime.now().minusMinutes(1).toString(),
        JSJoda.LocalDateTime.now().plusDays(4).withHour(23).withMinute(59).toString());
    byFlightTimeline.redraw();
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
