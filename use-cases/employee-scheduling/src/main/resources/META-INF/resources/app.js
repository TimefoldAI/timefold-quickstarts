let autoRefreshIntervalId = null;
const zoomMin = 2 * 1000 * 60 * 60 * 24 // 2 day in milliseconds
const zoomMax = 4 * 7 * 1000 * 60 * 60 * 24 // 4 weeks in milliseconds

let demoDataId = null;
let scheduleId = null;
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

const today = new Date();
let windowStart = JSJoda.LocalDate.now().toString();
let windowEnd = JSJoda.LocalDate.parse(windowStart).plusDays(7).toString();

byEmployeeTimeline.addCustomTime(today, 'published');
byEmployeeTimeline.setCustomTimeMarker('Published Shifts', 'published', false);
byEmployeeTimeline.setCustomTimeTitle('Published Shifts', 'published');

byEmployeeTimeline.addCustomTime(today, 'draft');
byEmployeeTimeline.setCustomTimeMarker('Draft Shifts', 'draft', false);
byEmployeeTimeline.setCustomTimeTitle('Draft Shifts', 'draft');

byLocationTimeline.addCustomTime(today, 'published');
byLocationTimeline.setCustomTimeMarker('Published Shifts', 'published', false);
byLocationTimeline.setCustomTimeTitle('Published Shifts', 'published');

byLocationTimeline.addCustomTime(today, 'draft');
byLocationTimeline.setCustomTimeMarker('Draft Shifts', 'draft', false);
byLocationTimeline.setCustomTimeTitle('Draft Shifts', 'draft');

$(document).ready(function () {
    replaceQuickstartTimefoldAutoHeaderFooter();

    $("#solveButton").click(function () {
        solve();
    });
    $("#stopSolvingButton").click(function () {
        stopSolving();
    });
    $("#publish").click(function () {
        publish();
    });
    // HACK to allow vis-timeline to work within Bootstrap tabs
    $("#byEmployeeTab").on('shown.bs.tab', function (event) {
        byEmployeeTimeline.redraw();
    })
    $("#byLocationTab").on('shown.bs.tab', function (event) {
        byLocationTimeline.redraw();
    })

    setupAjax();
    fetchDemoData();
});

function setupAjax() {
    $.ajaxSetup({
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json,text/plain', // plain text is required by solve() returning UUID of the solver job
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

function fetchDemoData() {
    $.get("/demo-data", function (data) {
        // load first data set
        demoDataId = data[0];
        refreshSchedule();
    }).fail(function (xhr, ajaxOptions, thrownError) {
        // disable this page as there is no data
        let $demo = $("#demo");
        $demo.empty();
        $demo.html("<h1><p align=\"center\">No test data available</p></h1>")
    });
}

function getAvailabilityColor(availabilityType) {
    switch (availabilityType) {
        case 'DESIRED':
            return ' #73d216'; // Tango Chameleon

        case 'UNDESIRED':
            return ' #f57900'; // Tango Orange

        case 'UNAVAILABLE':
            return ' #ef2929 '; // Tango Scarlet Red


        default:
            throw new Error('Unknown availability type: ' + availabilityType);
    }
}

function getShiftColor(shift, availabilityMap) {
    const shiftDate = JSJoda.LocalDateTime.parse(shift.start).toLocalDate().toString();
    const mapKey = shift.employee.name + '-' + shiftDate;
    if (availabilityMap.has(mapKey)) {
        return getAvailabilityColor(availabilityMap.get(mapKey));
    } else {
        return " #729fcf"; // Tango Sky Blue
    }
}

function refreshSchedule() {
    let path = "/schedules/" + scheduleId;
    if (scheduleId === null) {
        if (demoDataId === null) {
            alert("Please select a test data set.");
            return;
        }

        path = "/demo-data/" + demoDataId;
    }
    $.getJSON(path, function (schedule) {
        loadedSchedule = schedule;
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

    const unassignedShifts = $("#unassignedShifts");
    const groups = [];
    const availabilityMap = new Map();

    // Show only first 7 days of draft
    const scheduleStart = schedule.scheduleState.firstDraftDate;
    const scheduleEnd = JSJoda.LocalDate.parse(scheduleStart).plusDays(7).toString();

    windowStart = scheduleStart;
    windowEnd = scheduleEnd;

    unassignedShifts.children().remove();
    let unassignedShiftsCount = 0;
    byEmployeeGroupDataSet.clear();
    byLocationGroupDataSet.clear();

    byEmployeeItemDataSet.clear();
    byLocationItemDataSet.clear();

    byEmployeeTimeline.setCustomTime(schedule.scheduleState.lastHistoricDate, 'published');
    byEmployeeTimeline.setCustomTime(schedule.scheduleState.firstDraftDate, 'draft');

    byLocationTimeline.setCustomTime(schedule.scheduleState.lastHistoricDate, 'published');
    byLocationTimeline.setCustomTime(schedule.scheduleState.firstDraftDate, 'draft');

    schedule.availabilities.forEach((availability, index) => {
        const availabilityDate = JSJoda.LocalDate.parse(availability.date);
        const start = availabilityDate.atStartOfDay().toString();
        const end = availabilityDate.plusDays(1).atStartOfDay().toString();
        const byEmployeeShiftElement = $(`<div/>`)
            .append($(`<h5 class="card-title mb-1"/>`).text(availability.availabilityType));
        const mapKey = availability.employee.name + '-' + availabilityDate.toString();
        availabilityMap.set(mapKey, availability.availabilityType);
        byEmployeeItemDataSet.add({
            id: 'availability-' + index, group: availability.employee.name,
            content: byEmployeeShiftElement.html(),
            start: start, end: end,
            type: "background",
            style: "opacity: 0.5; background-color: " + getAvailabilityColor(availability.availabilityType),
        });
    });


    schedule.employees.forEach((employee, index) => {
        const employeeGroupElement = $('<div class="card-body p-2"/>')
            .append($(`<h5 class="card-title mb-2"/>)`)
                .append(employee.name))
            .append($('<div/>')
                .append($(employee.skills.map(skill => `<span class="badge me-1 mt-1" style="background-color:#d3d7cf">${skill}</span>`).join(''))));
        byEmployeeGroupDataSet.add({id: employee.name, content: employeeGroupElement.html()});
    });

    schedule.shifts.forEach((shift, index) => {
        if (groups.indexOf(shift.location) === -1) {
            groups.push(shift.location);
            byLocationGroupDataSet.add({
                id: shift.location,
                content: shift.location,
            });
        }

        if (shift.employee == null) {
            unassignedShiftsCount++;

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
            const skillColor = (shift.employee.skills.indexOf(shift.requiredSkill) === -1 ? '#ef2929' : '#8ae234');
            const byEmployeeShiftElement = $('<div class="card-body p-2"/>')
                .append($(`<h5 class="card-title mb-2"/>)`)
                    .append(shift.location))
                .append($('<div/>')
                    .append($(`<span class="badge me-1 mt-1" style="background-color:${skillColor}">${shift.requiredSkill}</span>`)));
            const byLocationShiftElement = $('<div class="card-body p-2"/>')
                .append($(`<h5 class="card-title mb-2"/>)`)
                    .append(shift.employee.name))
                .append($('<div/>')
                    .append($(`<span class="badge me-1 mt-1" style="background-color:${skillColor}">${shift.requiredSkill}</span>`)));

            const shiftColor = getShiftColor(shift, availabilityMap);
            byEmployeeItemDataSet.add({
                id: 'shift-' + index, group: shift.employee.name,
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


    if (unassignedShiftsCount === 0) {
        unassignedShifts.append($(`<p/>`).text(`There are no unassigned shifts.`));
    } else {
        unassignedShifts.append($(`<p/>`).text(`There are ${unassignedShiftsCount} unassigned shifts.`));
    }
    byEmployeeTimeline.setWindow(scheduleStart, scheduleEnd);
    byLocationTimeline.setWindow(scheduleStart, scheduleEnd);
}

function solve() {
    $.post("/schedules", JSON.stringify(loadedSchedule), function (data) {
        scheduleId = data;
        refreshSolvingButtons(true);
    }).fail(function (xhr, ajaxOptions, thrownError) {
            showError("Start solving failed.", xhr);
            refreshSolvingButtons(false);
        },
        "text");
}

function publish() {
    $.post(`/schedules/${scheduleId}/publish`, function () {
        refreshSolvingButtons(true);
        refreshSchedule();
    }).fail(function (xhr, ajaxOptions, thrownError) {
        showError("Publish failed.", xhr);
    });
}

function refreshSolvingButtons(solving) {
    if (solving) {
        $("#solveButton").hide();
        $("#publish").hide();
        $("#stopSolvingButton").show();
        if (autoRefreshIntervalId == null) {
            autoRefreshIntervalId = setInterval(refreshSchedule, 2000);
        }
    } else {
        $("#solveButton").show();
        if (scheduleId != null) {
            $("#publish").show();
        } else {
            $("#publish").hide();
        }
        $("#stopSolvingButton").hide();
        if (autoRefreshIntervalId != null) {
            clearInterval(autoRefreshIntervalId);
            autoRefreshIntervalId = null;
        }
    }
}

function stopSolving() {
    $.delete(`/schedules/${scheduleId}`, function () {
        refreshSolvingButtons(false);
        refreshSchedule();
    }).fail(function (xhr, ajaxOptions, thrownError) {
        showError("Stop solving failed.", xhr);
    });
}

function replaceQuickstartTimefoldAutoHeaderFooter() {
    const timefoldHeader = $("header#timefold-auto-header");
    if (timefoldHeader != null) {
        timefoldHeader.addClass("bg-black")
        timefoldHeader.append(
            $(`<div class="container-fluid">
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
        timefoldFooter.append(
            $(`<footer class="bg-black text-white-50">
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
