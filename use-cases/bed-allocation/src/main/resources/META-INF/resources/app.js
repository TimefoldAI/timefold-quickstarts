var autoRefreshIntervalId = null;
const dateFormatter = JSJoda.DateTimeFormatter.ofPattern('MM-dd');

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
    const scheduleByRoom = $("#scheduleByRoom");
    scheduleByRoom.children().remove();

    const unassignedPatients = $("#unassignedPatients");
    unassignedPatients.children().remove();

    const theadByRoom = $("<thead>").appendTo(scheduleByRoom);
    const headerRowByRoom = $("<tr>").appendTo(theadByRoom);
    headerRowByRoom.append($("<th>Room</th>"));

    const LocalDate = JSJoda.LocalDate;

    const arrivalDates = schedule.bedDesignations.map(d => d.stay.arrivalDate);
    const departureDates = schedule.bedDesignations.map(d => d.stay.departureDate);
    const allDates = [...new Set([...arrivalDates, ...departureDates])];

    $.each(allDates.sort((a, b) => LocalDate.parse(a).compareTo(LocalDate.parse(b))), (index, date) => {
        headerRowByRoom
            .append($("<th/>").prop("style", "text-align: center")
                .append($("<span/>").text(`${LocalDate.parse(date).format(dateFormatter)}`))
                .append($(`<button type="button" class="ms-2 mb-1 btn btn-light btn-sm p-1"/>`))
            );
    });

    const tbodyByRow = $("<tbody>").appendTo(scheduleByRoom);
    const maxBeds = Math.max(...schedule.departments.flatMap(d => d.rooms).map(r => r.beds.length));
    const addMinHeight = schedule.bedDesignations.find(d => d.bed != null);

    $.each(schedule.departments.flatMap(d => d.rooms), (index, room) => {
        const rowByRoom = $("<tr>").appendTo(tbodyByRow);
        rowByRoom
            .append($(`<th class="align-middle"/>`)
                .append($("<span/>").text(room.name)));
        $.each(allDates.sort((a, b) => LocalDate.parse(a).compareTo(LocalDate.parse(b))), (index, date) => {
            const columnByRow = $("<td/>");
            const containerByBed = $("<div/>").prop("class", "d-flex flex-column");
            columnByRow.append(containerByBed);
            for (let i = 0; i < maxBeds; i++) {
                const rowByBed = $("<div/>")
                    .prop("id", `room${room.id}date${LocalDate.parse(date).format(dateFormatter)}bed${i}`);
                if (addMinHeight) {
                    rowByBed.prop("style", "min-height: 100");
                }
                containerByBed.append(rowByBed);
            }
            rowByRoom.append(columnByRow);
        });
    });

    const bedMap = new Map();
    schedule.departments.flatMap(d => d.rooms).flatMap(r => r.beds).forEach(b => bedMap.set(b.id, b));

    $.each(schedule.bedDesignations, (index, designation) => {
        let currentDate = designation?.stay?.arrivalDate;
        do {
            const color = pickColor(designation.stay.patient.id);
            const bed = designation.bed != null ? bedMap.get(designation.bed) : {};
            const talkElement = $(`<div class="card mb-2" style="background-color: ${color}"/>`)
                .append($(`<div class="card-body p-2"/>`)
                    .append($(`<h5 class="card-title mb-1 text-truncate"/>`).text(`${designation.stay.patient.name}`))
                    .append($(`<p class="card-text ms-2 mb-1"/>`)
                        .append($(`<em/>`).text(`${designation.stay.specialism}`)))
                    .append($(`<p class="card-text ms-2 mb-1"/>`)
                        .append($(`<em/>`).text(`Bed ${bed?.indexInRoom ?? 'not scheduled'}`))));
            if (designation.bed != null) {
                $(`#room${bed.room}date${LocalDate.parse(currentDate).format(dateFormatter)}bed${bed.indexInRoom}`).append(talkElement.clone());
                currentDate = LocalDate.parse(currentDate).plusDays(1).toString();
            } else {
                unassignedPatients.append($(`<div class="col"/>`).append(talkElement));
                currentDate = null;
            }
        } while (currentDate != null && !LocalDate.parse(currentDate).isAfter(LocalDate.parse(designation.stay.departureDate)));
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
    if (loadedSchedule.score == null || loadedSchedule.score.indexOf('init') != -1) {
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