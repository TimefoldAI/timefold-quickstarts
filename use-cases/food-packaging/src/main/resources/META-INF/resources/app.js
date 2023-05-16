var autoRefreshIntervalId = null;

const dateTimeFormat = JSJoda.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
const byLinePanel = document.getElementById("byLinePanel");
const byLineTimelineOptions = {
  timeAxis: {scale: "hour"},
  orientation: {axis: "top"},
  stack: false,
  xss: {disabled: true}, // Items are XSS safe through JQuery
  zoomMin: 1000 * 60 * 60 * 12 // Half day in milliseconds
};
var byLineGroupDataSet = new vis.DataSet();
var byLineItemDataSet = new vis.DataSet();
var byLineTimeline = new vis.Timeline(byLinePanel, byLineItemDataSet, byLineGroupDataSet, byLineTimelineOptions);

const byJobPanel = document.getElementById("byJobPanel");
const byJobTimelineOptions = {
  timeAxis: {scale: "hour"},
  orientation: {axis: "top"},
  stack: false,
  xss: {disabled: true}, // Items are XSS safe through JQuery
  zoomMin: 1000 * 60 * 60 * 12 // Half day in milliseconds
};
var byJobGroupDataSet = new vis.DataSet();
var byJobItemDataSet = new vis.DataSet();
var byJobTimeline = new vis.Timeline(byJobPanel, byJobItemDataSet, byJobGroupDataSet, byJobTimelineOptions);

$(document).ready(function () {
  replaceTimefoldAutoHeaderFooter();
  $.ajaxSetup({
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    }
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
  $("#byLineTab").on('shown.bs.tab', function (event) {
    byLineTimeline.redraw();
  })
  $("#byJobTab").on('shown.bs.tab', function (event) {
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
    byLineGroupDataSet.clear();
    byJobGroupDataSet.clear();
    byLineItemDataSet.clear();
    byJobItemDataSet.clear();

    $.each(schedule.lineList, (index, line) => {
      const lineGroupElement = $(`<div/>`)
        .append($(`<h5 class="card-title mb-1"/>`).text(line.name))
        .append($(`<p class="card-text ms-2 mb-0"/>`).text(line.operator));
      byLineGroupDataSet.add({id : line.id, content: lineGroupElement.html()});
    });

    $.each(schedule.jobList, (index, job) => {
      byJobGroupDataSet.add({id : job.id, content: job.name});
      byJobItemDataSet.add({
        id: job.id + "_readyToIdealEnd", group: job.id,
        start: job.readyDateTime,
        end: job.idealEndDateTime,
        type: "background",
        style: "background-color: #8AE23433"
      });
      byJobItemDataSet.add({
        id: job.id + "_idealEndToDue", group: job.id,
        start: job.idealEndDateTime,
        end: job.dueDateTime,
        type: "background",
        style: "background-color: #FCAF3E33"
      });

      if (job.line == null || job.startCleaningDateTime == null || job.startProductionDateTime == null || job.endDateTime == null) {
        unassignedJobsCount++;
        const durationMinutes = JSJoda.Duration.ofSeconds(job.duration).toMinutes();
        const unassignedJobElement = $(`<div class="card-body p-2"/>`)
          .append($(`<h5 class="card-title mb-1"/>`).text(job.name))
          .append($(`<p class="card-text ms-2 mb-0"/>`).text(`${Math.floor(durationMinutes / 60)} hours ${durationMinutes % 60} mins`))
          .append($(`<p class="card-text ms-2 mb-0"/>`).text(`Ready: ${JSJoda.LocalDateTime.parse(job.readyDateTime).format(dateTimeFormat)}`))
          .append($(`<p class="card-text ms-2 mb-0"/>`).text(`Ideal end: ${JSJoda.LocalDateTime.parse(job.idealEndDateTime).format(dateTimeFormat)}`))
          .append($(`<p class="card-text ms-2 mb-0"/>`).text(`Due: ${JSJoda.LocalDateTime.parse(job.dueDateTime).format(dateTimeFormat)}`));
        const byJobJobElement = $(`<div/>`)
          .append($(`<h5 class="card-title mb-1"/>`).text(`Unassigned`));
        unassignedJobs.append($(`<div class="col"/>`).append($(`<div class="card"/>`).append(unassignedJobElement)));
        byJobItemDataSet.add({
          id : job.id, group: job.id,
          content: byJobJobElement.html(),
          start: job.readyDateTime, end: JSJoda.LocalDateTime.parse(job.readyDateTime).plus(JSJoda.Duration.ofSeconds(job.duration)).toString(),
          style: "background-color: #EF292999"
        });
      } else {
        const beforeReady = JSJoda.LocalDateTime.parse(job.startProductionDateTime).isBefore(JSJoda.LocalDateTime.parse(job.readyDateTime));
        const afterDue = JSJoda.LocalDateTime.parse(job.endDateTime).isAfter(JSJoda.LocalDateTime.parse(job.dueDateTime));
        const byLineJobElement = $(`<div/>`)
          .append($(`<p class="card-text"/>`).text(job.name));
        const byJobJobElement = $(`<div/>`)
          .append($(`<p class="card-text"/>`).text(job.line.name));
        if (beforeReady) {
          byLineJobElement.append($(`<p class="badge badge-danger mb-0"/>`).text(`Before ready (too early)`));
          byJobJobElement.append($(`<p class="badge badge-danger mb-0"/>`).text(`Before ready (too early)`));
        }
        if (afterDue) {
          byLineJobElement.append($(`<p class="badge badge-danger mb-0"/>`).text(`After due (too late)`));
          byJobJobElement.append($(`<p class="badge badge-danger mb-0"/>`).text(`After due (too late)`));
        }
        byLineItemDataSet.add({
          id : job.id + "_cleaning", group: job.line.id,
          content: "Cleaning",
          start: job.startCleaningDateTime, end: job.startProductionDateTime,
          style: "background-color: #FCAF3E99"
        });
        byLineItemDataSet.add({
          id : job.id, group: job.line.id,
          content: byLineJobElement.html(),
          start: job.startProductionDateTime, end: job.endDateTime
        });
        byJobItemDataSet.add({
          id : job.id + "_cleaning", group: job.id,
          content: "Cleaning",
          start: job.startCleaningDateTime, end: job.startProductionDateTime,
          style: "background-color: #FCAF3E99"
        });
        byJobItemDataSet.add({
          id : job.id, group: job.id,
          content: byJobJobElement.html(),
          start: job.startProductionDateTime, end: job.endDateTime
        });
      }
    });
    if (unassignedJobsCount === 0) {
      unassignedJobs.append($(`<p/>`).text(`There are no unassigned jobs.`));
    }
    const nextDate = JSJoda.LocalDate.parse(schedule.workCalendar.fromDate).plusDays(1);
    byLineTimeline.setWindow(schedule.workCalendar.fromDate, nextDate.toString());
    byJobTimeline.setWindow(schedule.workCalendar.fromDate, nextDate.toString());
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
