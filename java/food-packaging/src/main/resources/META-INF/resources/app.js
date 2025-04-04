var autoRefreshIntervalId = null;
let loadedSchedule = null;

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

  $("#refreshButton").click(function () {
    refreshSchedule();
  });
  $("#solveButton").click(function () {
    solve();
  });
  $("#stopSolvingButton").click(function () {
    stopSolving();
  });
  $("#analyzeButton").click(function () {
    analyze();
  });
  // HACK to allow vis-timeline to work within Bootstrap tabs
  $("#byLineTab").on('shown.bs.tab', function (event) {
    byLineTimeline.redraw();
  })
  $("#byJobTab").on('shown.bs.tab', function (event) {
    byJobTimeline.redraw();
  })

  setupAjax();
  refreshSchedule();
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

function refreshSchedule() {
  $.getJSON("/schedule", function (schedule) {
    refreshSolvingButtons(schedule.solverStatus != null && schedule.solverStatus !== "NOT_SOLVING");
    $("#score").text("Score: " + (schedule.score == null ? "?" : schedule.score));
    loadedSchedule = schedule;
    const unassignedJobs = $("#unassignedJobs");
    unassignedJobs.children().remove();
    var unassignedJobsCount = 0;
    byLineGroupDataSet.clear();
    byJobGroupDataSet.clear();
    byLineItemDataSet.clear();
    byJobItemDataSet.clear();

    $.each(schedule.lines, (index, line) => {
      const lineGroupElement = $(`<div/>`)
        .append($(`<h5 class="card-title mb-1"/>`).text(line.name))
        .append($(`<p class="card-text ms-2 mb-0"/>`).text(line.operator));
      byLineGroupDataSet.add({id : line.id, content: lineGroupElement.html()});
    });

    $.each(schedule.jobs, (index, job) => {
      byJobGroupDataSet.add({id : job.id, content: job.name});
      byJobItemDataSet.add({
        id: job.id + "_readyToIdealEnd", group: job.id,
        start: job.minStartTime,
        end: job.idealEndTime,
        type: "background",
        style: "background-color: #8AE23433"
      });
      byJobItemDataSet.add({
        id: job.id + "_idealEndToDue", group: job.id,
        start: job.idealEndTime,
        end: job.maxEndTime,
        type: "background",
        style: "background-color: #FCAF3E33"
      });

      if (job.line == null || job.startCleaningDateTime == null || job.startProductionDateTime == null || job.endDateTime == null) {
        unassignedJobsCount++;
        const durationMinutes = JSJoda.Duration.ofSeconds(job.duration).toMinutes();
        const unassignedJobElement = $(`<div class="card-body p-2"/>`)
          .append($(`<h5 class="card-title mb-1"/>`).text(job.name))
          .append($(`<p class="card-text ms-2 mb-0"/>`).text(`${Math.floor(durationMinutes / 60)} hours ${durationMinutes % 60} mins`))
          .append($(`<p class="card-text ms-2 mb-0"/>`).text(`Min: ${JSJoda.LocalDateTime.parse(job.minStartTime).format(dateTimeFormat)}`))
          .append($(`<p class="card-text ms-2 mb-0"/>`).text(`Ideal: ${JSJoda.LocalDateTime.parse(job.idealEndTime).format(dateTimeFormat)}`))
          .append($(`<p class="card-text ms-2 mb-0"/>`).text(`Max: ${JSJoda.LocalDateTime.parse(job.maxEndTime).format(dateTimeFormat)}`));
        const byJobJobElement = $(`<div/>`)
          .append($(`<h5 class="card-title mb-1"/>`).text(`Unassigned`));
        unassignedJobs.append($(`<div class="col"/>`).append($(`<div class="card"/>`).append(unassignedJobElement)));
        byJobItemDataSet.add({
          id : job.id, group: job.id,
          content: byJobJobElement.html(),
          start: job.minStartTime, end: JSJoda.LocalDateTime.parse(job.minStartTime).plus(JSJoda.Duration.ofSeconds(job.duration)).toString(),
          style: "background-color: #EF292999"
        });
      } else {
        const beforeReady = JSJoda.LocalDateTime.parse(job.startProductionDateTime).isBefore(JSJoda.LocalDateTime.parse(job.minStartTime));
        const afterDue = JSJoda.LocalDateTime.parse(job.endDateTime).isAfter(JSJoda.LocalDateTime.parse(job.maxEndTime));
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

function analyze() {
  new bootstrap.Modal("#scoreAnalysisModal").show()
  const scoreAnalysisModalContent = $("#scoreAnalysisModalContent");
  scoreAnalysisModalContent.children().remove();
  if (loadedSchedule.score == null) {
    scoreAnalysisModalContent.text("No score to analyze yet, please first press the 'solve' button.");
  } else {
    $('#scoreAnalysisScoreLabel').text(`(${loadedSchedule.score})`);
    $.put("/schedule/analyze", function (scoreAnalysis) {
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
  $.post("/schedule/stopSolving", function () {
    refreshSolvingButtons(false);
    refreshSchedule();
  }).fail(function (xhr, ajaxOptions, thrownError) {
    showError("Stop solving failed.", xhr);
  });
}
