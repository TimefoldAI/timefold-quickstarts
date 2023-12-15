let autoRefreshIntervalId = null;
const zoomMin = 2 * 1000 * 60 * 60 * 24 // 2 day in milliseconds
const zoomMax = 4 * 7 * 1000 * 60 * 60 * 24 // 4 weeks in milliseconds

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
    replaceTimefoldAutoHeaderFooter();
    $.ajaxSetup({
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
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

    $("#refreshButton").click(function () {
        refreshSchedule();
    });
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

    refreshSchedule();
});


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
    $.getJSON("/schedule", function (schedule) {
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
                id : 'availability-' + index, group: availability.employee.name,
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
            byEmployeeGroupDataSet.add({id : employee.name, content: employeeGroupElement.html()});
        });

        schedule.shifts.forEach((shift, index) => {
            if (groups.indexOf(shift.location) === -1) {
                groups.push(shift.location);
                byLocationGroupDataSet.add({
                    id : shift.location,
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
                    id : 'shift-' + index, group: shift.location,
                    content: byLocationShiftElement.html(),
                    start: shift.start, end: shift.end,
                    style: "background-color: #EF292999"
                });
            } else {
                const skillColor = (shift.employee.skills.indexOf(shift.requiredSkill) === -1? '#ef2929' : '#8ae234');
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

                const shiftColor =  getShiftColor(shift, availabilityMap);
                byEmployeeItemDataSet.add({
                    id : 'shift-' + index, group: shift.employee.name,
                    content: byEmployeeShiftElement.html(),
                    start: shift.start, end: shift.end,
                    style: "background-color: " + shiftColor
                });
                byLocationItemDataSet.add({
                    id : 'shift-' + index, group: shift.location,
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
    });
}

function solve() {
    $.post("/schedule/solve", function () {
        refreshSolvingButtons(true);
    }).fail(function (xhr, ajaxOptions, thrownError) {
        showError("Start solving failed.", xhr);
    });
}

function publish() {
    $.post("/schedule/publish", function () {
        refreshSolvingButtons(true);
    }).fail(function (xhr, ajaxOptions, thrownError) {
        showError("Publish failed.", xhr);
    });
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
    $.post("/schedule/stopSolving", function () {
        refreshSolvingButtons(false);
        refreshSchedule();
    }).fail(function (xhr, ajaxOptions, thrownError) {
        showError("Stop solving failed.", xhr);
    });
}
