var autoRefreshIntervalId = null;
const dateFormatter = JSJoda.DateTimeFormatter.ofPattern('MM-dd');
const roomDateFormatter = JSJoda.DateTimeFormatter.ofPattern('d MMM').withLocale(JSJodaLocale.Locale.ENGLISH);

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
        byRoomTimeline.redraw();
        refreshSchedule();
    });

    addImportDropdownItem();
    addExportDropdownItem();

    setupAjax();
    refreshSchedule();
});

function addImportDropdownItem() {
    $("#testDataButton")
        .append($('<hr class="dropdown-divider">'))
        .append($('<a id="importTestData" class="dropdown-item" href="#">Import</a>'));
    $("#uploadModalImportButton").click(importLocalFile);
    $("#importTestData").click(function () {
        scheduleId = null;
        demoDataId = null;
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

    if (viewType === "R") {
        renderScheduleByRoom(schedule);
    }
}

function renderScheduleByRoom(schedule) {
    const unassignedPatients = $("#unassignedPatients");
    unassignedPatients.children().remove();
    let unassignedJobsCount = 0;
    byRoomGroupData.clear();
    byRoomItemData.clear();

    $.each(schedule.departments.flatMap(d => d.rooms), (_, room) => {
        let content = `<div class="d-flex flex-column"><div><h5 class="card-title mb-1">${room.name}</h5></div>`;
        if (room.equipments.length > 0) {
            let equipments = room.equipments.sort().slice(0, Math.min(2, room.equipments.length));
            content += `<div class="d-flex">`;
            equipments.forEach(e => content += `<div><span class="badge text-bg-success m-1" style="background-color: ${pickColor(e)}">${e}</span></div>`);
            content += "</div>";
            if (room.equipments.length > 2) {
                let equipments = room.equipments.sort().slice(2, Math.min(4, room.equipments.length));
                content += `<div class="d-flex">`;
                equipments.forEach(e => content += `<div><span class="badge text-bg-success m-1" style="background-color: ${pickColor(e)}">${e}</span></div>`);
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
        room.beds.forEach(bed => byRoomGroupData.add({
            id: bed.id,
            content: `Bed ${bed.indexInRoom + 1}`,
            treeLevel: 2
        }));
    });

    const bedMap = new Map();
    schedule.departments.flatMap(d => d.rooms).flatMap(r => r.beds).forEach(b => bedMap.set(b.id, b));

    $.each(schedule.stays, (_, stay) => {
        if (stay.bed == null) {
            unassignedJobsCount++;
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

            const color = stay.patientGender == 'MALE' ? '#729FCF' : '#FCE94F';
            unassignedPatients.append($(`<div class="col"/>`).append($(`<div class="card" style="background-color: ${color}"/>`).append(unassignedPatientElement)));
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
            const color = stay.patientGender == 'MALE' ? '#729FCF' : '#FCE94F';

            byRoomItemData.add({
                id: stay.id,
                group: stay.bed,
                content: byPatientElement.html(),
                start: stay.arrivalDate,
                end: stay.departureDate,
                style: `background-color: ${color}`
            });
        }
    });
    if (unassignedJobsCount === 0) {
        unassignedPatients.append($(`<p/>`).text(`There are no unassigned stays.`));
    }

    const arrivalDates = schedule.stays.map(s => s.arrivalDate);
    const departureDates = schedule.stays.map(s => s.departureDate);
    const allDates = [...new Set([...arrivalDates, ...departureDates])]
        .sort((a, b) => JSJoda.LocalDate.parse(a).compareTo(JSJoda.LocalDate.parse(b)));
    byRoomTimeline.setWindow(allDates[0], allDates[allDates.length - 1]);
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

function publish() {
    $("#publishButton").hide();
    $("#publishLoadingButton").show();
    $.put(`/schedules/${scheduleId}/publish`, function (schedule) {
        loadedSchedule = schedule;
        renderSchedule(schedule);
    })
        .fail(function (xhr, ajaxOptions, thrownError) {
            showError("Publish failed.", xhr);
            refreshSolvingButtons(false);
        });
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

function importLocalFile() {
    var file = document.querySelector('input[type=file]').files[0];
    var reader = new FileReader();

    reader.addEventListener("load", function () {
        // convert image file to base64 string
        var data = atob(reader.result.toString().replace(/^data:(.*,)?/, ''));
        $("#importedFile").val('');

        try {
            loadedSchedule = JSON.parse(data);
            renderSchedule(loadedSchedule);
            $('#exportData').attr('href', 'data:text/plain;charset=utf-8,' + JSON.stringify(loadedSchedule));
        } catch (error) {
            console.error(error);
            showSimpleError("Failed loading a bed plan.\nCheck if the content of the file represents a valid bed plan.");
        }
        $('#uploadModal').modal('hide');
    }, false);

    reader.readAsDataURL(file);
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
