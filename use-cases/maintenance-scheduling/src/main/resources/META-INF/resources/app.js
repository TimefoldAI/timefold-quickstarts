var autoRefreshIntervalId = null;

const byCrewPanel = document.getElementById("byCrewPanel");
const byCrewTimelineOptions = {
    timeAxis: {scale: "day"},
    orientation: {axis: "top"},
    stack: false,
    xss: {disabled: true}, // Items are XSS safe through JQuery
    zoomMin: 3 * 1000 * 60 * 60 * 24 // Three day in milliseconds
};
var byCrewGroupDataSet = new vis.DataSet();
var byCrewItemDataSet = new vis.DataSet();
var byCrewTimeline = new vis.Timeline(byCrewPanel, byCrewItemDataSet, byCrewGroupDataSet, byCrewTimelineOptions);

const byJobPanel = document.getElementById("byJobPanel");
const byJobTimelineOptions = {
    timeAxis: {scale: "day"},
    orientation: {axis: "top"},
    xss: {disabled: true}, // Items are XSS safe through JQuery
    zoomMin: 3 * 1000 * 60 * 60 * 24 // Three day in milliseconds
};
var byJobGroupDataSet = new vis.DataSet();
var byJobItemDataSet = new vis.DataSet();
var byJobTimeline = new vis.Timeline(byJobPanel, byJobItemDataSet, byJobGroupDataSet, byJobTimelineOptions);

$(document).ready(function () {
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
    // HACK to allow vis-timeline to work within Bootstrap tabs
    $("#byCrewPanelTab").on('shown.bs.tab', function (event) {
        byCrewTimeline.redraw();
    })
    $("#byJobPanelTab").on('shown.bs.tab', function (event) {
        byJobTimeline.redraw();
    })

    refreshSchedule();
});

function refreshSchedule() {
    $.getJSON("/schedule", function (schedule) {
        refreshSolvingButtons(schedule.solverStatus != null && schedule.solverStatus !== "NOT_SOLVING");
        $("#score").text("Score: " + (schedule.score == null ? "?" : schedule.score));

        const unassignedJobs = $("#unassignedJobs");
        unassignedJobs.children().remove();
        var unassignedJobsCount = 0;
        byCrewGroupDataSet.clear();
        byJobGroupDataSet.clear();
        byCrewItemDataSet.clear();
        byJobItemDataSet.clear();

        $.each(schedule.crewList, (index, crew) => {
            byCrewGroupDataSet.add({id : crew.id, content: crew.name});
        });

        $.each(schedule.jobList, (index, job) => {
            const jobGroupElement = $(`<div/>`)
              .append($(`<h5 class="card-title mb-1"/>`).text(job.name))
              .append($(`<p class="card-text ms-2 mb-0"/>`).text(`${job.durationInDays} workdays`));
            byJobGroupDataSet.add({
                id : job.id,
                content: jobGroupElement.html()
            });
            byJobItemDataSet.add({
                  id: job.id + "_readyToIdealEnd", group: job.id,
                  start: job.readyDate, end: job.idealEndDate,
                  type: "background",
                  style: "background-color: #8AE23433"
            });
            byJobItemDataSet.add({
                  id: job.id + "_idealEndToDue", group: job.id,
                  start: job.idealEndDate, end: job.dueDate,
                  type: "background",
                  style: "background-color: #FCAF3E33"
            });

            if (job.crew == null || job.startDate == null) {
                unassignedJobsCount++;
                const unassignedJobElement = $(`<div class="card-body p-2"/>`)
                    .append($(`<h5 class="card-title mb-1"/>`).text(job.name))
                    .append($(`<p class="card-text ms-2 mb-0"/>`).text(`${job.durationInDays} workdays`))
                    .append($(`<p class="card-text ms-2 mb-0"/>`).text(`Ready: ${job.readyDate}`))
                    .append($(`<p class="card-text ms-2 mb-0"/>`).text(`Due: ${job.dueDate}`));
                const byJobJobElement = $(`<div/>`)
                  .append($(`<h5 class="card-title mb-1"/>`).text(`Unassigned`));
                $.each(job.tagSet, (index, tag) => {
                    const color = pickColor(tag);
                    unassignedJobElement.append($(`<span class="badge me-1" style="background-color: ${color}"/>`).text(tag));
                    byJobJobElement.append($(`<span class="badge me-1" style="background-color: ${color}"/>`).text(tag));
                });
                unassignedJobs.append($(`<div class="col"/>`).append($(`<div class="card"/>`).append(unassignedJobElement)));
                byJobItemDataSet.add({
                    id : job.id, group: job.id,
                    content: byJobJobElement.html(),
                    start: job.readyDate, end: JSJoda.LocalDate.parse(job.readyDate).plusDays(job.durationInDays).toString(),
                    style: "background-color: #EF292999"
                });
            } else {
                const beforeReady = JSJoda.LocalDate.parse(job.startDate).isBefore(JSJoda.LocalDate.parse(job.readyDate));
                const afterDue = JSJoda.LocalDate.parse(job.endDate).isAfter(JSJoda.LocalDate.parse(job.dueDate));
                const byCrewJobElement = $(`<div/>`)
                    .append($(`<h5 class="card-title mb-1"/>`).text(job.name))
                    .append($(`<p class="card-text ms-2 mb-0"/>`).text(`${job.durationInDays} workdays`));
                const byJobJobElement = $(`<div/>`)
                    .append($(`<h5 class="card-title mb-1"/>`).text(job.crew.name));
                if (beforeReady) {
                    byCrewJobElement.append($(`<p class="badge badge-danger mb-0"/>`).text(`Before ready (too early)`));
                    byJobJobElement.append($(`<p class="badge badge-danger mb-0"/>`).text(`Before ready (too early)`));
                }
                if (afterDue) {
                    byCrewJobElement.append($(`<p class="badge badge-danger mb-0"/>`).text(`After due (too late)`));
                    byJobJobElement.append($(`<p class="badge badge-danger mb-0"/>`).text(`After due (too late)`));
                }
                $.each(job.tagSet, (index, tag) => {
                    const color = pickColor(tag);
                    byCrewJobElement.append($(`<span class="badge me-1" style="background-color: ${color}"/>`).text(tag));
                    byJobJobElement.append($(`<span class="badge me-1" style="background-color: ${color}"/>`).text(tag));
                });
                byCrewItemDataSet.add({
                    id : job.id, group: job.crew.id,
                    content: byCrewJobElement.html(),
                    start: job.startDate, end: job.endDate
                });
                byJobItemDataSet.add({
                    id : job.id, group: job.id,
                    content: byJobJobElement.html(),
                    start: job.startDate, end: job.endDate
                });
            }
        });
        if (unassignedJobsCount === 0) {
            unassignedJobs.append($(`<p/>`).text(`There are no unassigned jobs.`));
        }
        byCrewTimeline.setWindow(schedule.workCalendar.fromDate, schedule.workCalendar.toDate);
        byJobTimeline.setWindow(schedule.workCalendar.fromDate, schedule.workCalendar.toDate);
    });
}

function solve() {
    $.post("/schedule/solve", function () {
        refreshSolvingButtons(true);
    }).fail(function (xhr, ajaxOptions, thrownError) {
        showError("Start solving failed.", xhr);
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

function showError(title, xhr) {
    const serverErrorMessage = !xhr.responseJSON ? `${xhr.status}: ${xhr.statusText}` : xhr.responseJSON.message;
    console.error(title + "\n" + serverErrorMessage);
    const notification = $(`<div class="toast" role="alert" aria-live="assertive" aria-atomic="true" style="min-width: 30rem"/>`)
      .append($(`<div class="toast-header bg-danger">
                 <strong class="me-auto text-dark">Error</strong>
                 <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
               </div>`))
      .append($(`<div class="toast-body"/>`)
        .append($(`<p/>`).text(title))
        .append($(`<pre/>`)
          .append($(`<code/>`).text(serverErrorMessage))
        )
      );
    $("#notificationPanel").append(notification);
    notification.toast({delay: 30000});
    notification.toast('show');
}

// ****************************************************************************
// TangoColorFactory
// ****************************************************************************

const SEQUENCE_1 = [0x8AE234, 0xFCE94F, 0x729FCF, 0xE9B96E, 0xAD7FA8];
const SEQUENCE_2 = [0x73D216, 0xEDD400, 0x3465A4, 0xC17D11, 0x75507B];

var colorMap = new Map;
var nextColorCount = 0;

function pickColor(object) {
    let color = colorMap[object];
    if (color !== undefined) {
        return color;
    }
    color = nextColor();
    colorMap[object] = color;
    return color;
}

function nextColor() {
    let color;
    let colorIndex = nextColorCount % SEQUENCE_1.length;
    let shadeIndex = Math.floor(nextColorCount / SEQUENCE_1.length);
    if (shadeIndex === 0) {
        color = SEQUENCE_1[colorIndex];
    } else if (shadeIndex === 1) {
        color = SEQUENCE_2[colorIndex];
    } else {
        shadeIndex -= 3;
        let floorColor = SEQUENCE_2[colorIndex];
        let ceilColor = SEQUENCE_1[colorIndex];
        let base = Math.floor((shadeIndex / 2) + 1);
        let divisor = 2;
        while (base >= divisor) {
            divisor *= 2;
        }
        base = (base * 2) - divisor + 1;
        let shadePercentage = base / divisor;
        color = buildPercentageColor(floorColor, ceilColor, shadePercentage);
    }
    nextColorCount++;
    return "#" + color.toString(16);
}

function buildPercentageColor(floorColor, ceilColor, shadePercentage) {
    let red = (floorColor & 0xFF0000) + Math.floor(shadePercentage * ((ceilColor & 0xFF0000) - (floorColor & 0xFF0000))) & 0xFF0000;
    let green = (floorColor & 0x00FF00) + Math.floor(shadePercentage * ((ceilColor & 0x00FF00) - (floorColor & 0x00FF00))) & 0x00FF00;
    let blue = (floorColor & 0x0000FF) + Math.floor(shadePercentage * ((ceilColor & 0x0000FF) - (floorColor & 0x0000FF))) & 0x0000FF;
    return red | green | blue;
}
