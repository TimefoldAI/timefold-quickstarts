// ── Platform context ──
// When embedded in the Timefold Platform, the iframe URL carries these query params.
// Standalone (local dev), none are present and the app behaves as before.
const PLATFORM = (function () {
    const q = new URL(window.location.href).searchParams;
    return {
        onPlatform: q.has('onPlatform'),
        runId: q.get('runId'),
        // apiUrl: base URL of the model API on the platform (URL-encoded). Trailing slash stripped.
        apiUrl: q.has('apiUrl') ? decodeURIComponent(q.get('apiUrl')).replace(/\/+$/, '') : null,
        apiKey: q.has('apiKey') ? q.get('apiKey') : null,
    };
})();

// Build an API URL: prefix the platform base when embedded, else root-relative (local dev).
function api(path) {
    return PLATFORM.apiUrl ? PLATFORM.apiUrl + path : path;
}

let autoRefreshIntervalId = null;
let jobId = null;
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
const byLineGroupDataSet = new vis.DataSet();
const byLineItemDataSet = new vis.DataSet();
const byLineTimeline = new vis.Timeline(byLinePanel, byLineItemDataSet, byLineGroupDataSet, byLineTimelineOptions);

const byJobPanel = document.getElementById("byJobPanel");
const byJobTimelineOptions = {
  timeAxis: {scale: "hour"},
  orientation: {axis: "top"},
  stack: false,
  xss: {disabled: true}, // Items are XSS safe through JQuery
  zoomMin: 1000 * 60 * 60 * 12 // Half day in milliseconds
};
const byJobGroupDataSet = new vis.DataSet();
const byJobItemDataSet = new vis.DataSet();
const byJobTimeline = new vis.Timeline(byJobPanel, byJobItemDataSet, byJobGroupDataSet, byJobTimelineOptions);

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
  // HACK to allow vis-timeline to work within Bootstrap tabs
  $("#byLineTab").on('shown.bs.tab', function (event) {
    byLineTimeline.redraw();
  });
  $("#byJobTab").on('shown.bs.tab', function (event) {
    byJobTimeline.redraw();
  });

  setupAjax();
  if (PLATFORM.onPlatform) {
    // Embedded: hide demo chrome (navbar + solve/score controls) and load the
    // existing run read-only.
    document.body.classList.add('on-platform');
    loadPlatformRun();
  } else {
    // Standalone dev: load demo data, allow solving.
    getStatus();
  }
});

// ── Platform: load an existing run (read-only) ──
// ModelRest exposes the run's input at /{id}/model-request (ModelRequest → {modelInput})
// and its output+status at /{id} (ModelResponse → {metadata:{solverStatus}, modelOutput}).
function loadPlatformRun() {
  if (!PLATFORM.runId) {
    showError("No runId provided by platform.", {status: 0, statusText: "missing runId"});
    return;
  }
  jobId = PLATFORM.runId;
  $.get(api(`/v1/schedules/${jobId}/model-request`), function (req) {
    loadedSchedule = req.modelInput || req;
    renderSchedule(loadedSchedule, 'NOT_SOLVING');
    getStatus(); // fetch output + render; auto-polls while solving
  }).fail(function (xhr) {
    showError("Failed to load run input from platform.", xhr);
  });
}

function setupAjax() {
  $.ajaxSetup({
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json,text/plain', // plain text is required by solve() returning UUID of the solver job
      // On the platform, authenticate every request with the supplied API key.
      ...(PLATFORM.apiKey ? {'X-API-KEY': PLATFORM.apiKey} : {})
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

function getStatus() {
  if (jobId == null) {
    $.get(api('/v1/demo-data/BASIC'), function (modelRequest) {
      loadedSchedule = modelRequest.modelInput;
      renderSchedule(loadedSchedule, 'NOT_SOLVING');
    }).fail(function (xhr) {
      const $demo = $("#demo");
      $demo.empty();
      $demo.html("<h1><p align=\"center\">No test data available</p></h1>");
    });
  } else {
    $.get(api(`/v1/schedules/${jobId}`), function (data) {
      const solverStatus = data.metadata.solverStatus;
      const solution = data.modelOutput || loadedSchedule;
      loadedSchedule = solution;
      renderSchedule(solution, solverStatus);
    }).fail(function (xhr) {
      showError("Getting the schedule has failed.", xhr);
      refreshSolvingButtons('NOT_SOLVING');
    });
  }
}

function isSolving(solverStatus) {
  return solverStatus === 'SOLVING_ACTIVE' || solverStatus === 'SOLVING_SCHEDULED'
      || solverStatus === 'SOLVING_STARTED';
}

function renderSchedule(schedule, solverStatus) {
  refreshSolvingButtons(solverStatus);
  $("#score").text("Score: " + (schedule.score == null ? "?" : schedule.score));
  $("#info").text(`This dataset has ${schedule.products.length} products, ${schedule.jobs.length} jobs, to be produced on ${schedule.lines.length} lines by ${schedule.operators.length} operators.`);

  const unassignedOperators = $("#unassignedOperators");
  const unassignedJobs = $("#unassignedJobs");
  unassignedOperators.children().remove();
  unassignedJobs.children().remove();
  let unassignedOperatorsCount = 0;
  let unassignedJobsCount = 0;
  byLineGroupDataSet.clear();
  byJobGroupDataSet.clear();
  byLineItemDataSet.clear();
  byJobItemDataSet.clear();

  const linesMap = new Map();
  const usedOperatorIds = new Set();
  $.each(schedule.lines, (index, line) => {
    linesMap.set(line.id, line);
    if (line.operatorId != null) {
      usedOperatorIds.add(line.operatorId);
    }
    const lineGroupElement = $(`<div/>`)
      .append($(`<h5 class="card-title mb-1"/>`).text(line.name))
      .append($(`<p class="card-text ms-2 mb-0"/>`).text(line.operatorId == null ? "No operator" : line.operatorId));
    byLineGroupDataSet.add({id: line.id, content: lineGroupElement.html()});
  });

  $.each(schedule.operators, (index, operator) => {
    if (!usedOperatorIds.has(operator.id)) {
      unassignedOperatorsCount++;
      const unassignedOperatorElement = $(`<div class="card-body p-2"/>`)
        .append($(`<h5 class="card-title mb-1"/>`).text(operator.id));
      unassignedOperators.append($(`<div class="col"/>`).append($(`<div class="card"/>`).append(unassignedOperatorElement)));
    }
  });

  $.each(schedule.jobs, (index, job) => {
    byJobGroupDataSet.add({id: job.id, content: job.name});
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

    if (job.lineId == null || job.startCleaningDateTime == null || job.startProductionDateTime == null || job.endDateTime == null) {
      unassignedJobsCount++;
      const durationMinutes = job.durationMinutes;
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
        id: job.id, group: job.id,
        content: byJobJobElement.html(),
        start: job.minStartTime,
        end: JSJoda.LocalDateTime.parse(job.minStartTime).plus(JSJoda.Duration.ofMinutes(durationMinutes)).toString(),
        style: "background-color: #EF292999"
      });
    } else {
      const beforeReady = JSJoda.LocalDateTime.parse(job.startProductionDateTime).isBefore(JSJoda.LocalDateTime.parse(job.minStartTime));
      const afterDue = JSJoda.LocalDateTime.parse(job.endDateTime).isAfter(JSJoda.LocalDateTime.parse(job.maxEndTime));
      const line = linesMap.get(job.lineId);
      const byLineJobElement = $(`<div/>`)
        .append($(`<p class="card-text"/>`).text(job.name));
      const byJobJobElement = $(`<div/>`)
        .append($(`<p class="card-text"/>`).text(line.name));
      if (beforeReady) {
        byLineJobElement.append($(`<p class="badge badge-danger mb-0"/>`).text(`Before ready (too early)`));
        byJobJobElement.append($(`<p class="badge badge-danger mb-0"/>`).text(`Before ready (too early)`));
      }
      if (afterDue) {
        byLineJobElement.append($(`<p class="badge badge-danger mb-0"/>`).text(`After due (too late)`));
        byJobJobElement.append($(`<p class="badge badge-danger mb-0"/>`).text(`After due (too late)`));
      }
      byLineItemDataSet.add({
        id: job.id + "_cleaning", group: job.lineId,
        content: "Cleaning",
        start: job.startCleaningDateTime, end: job.startProductionDateTime,
        style: "background-color: #FCAF3E99"
      });
      byLineItemDataSet.add({
        id: job.id, group: job.lineId,
        content: byLineJobElement.html(),
        start: job.startProductionDateTime, end: job.endDateTime
      });
      byJobItemDataSet.add({
        id: job.id + "_cleaning", group: job.id,
        content: "Cleaning",
        start: job.startCleaningDateTime, end: job.startProductionDateTime,
        style: "background-color: #FCAF3E99"
      });
      byJobItemDataSet.add({
        id: job.id, group: job.id,
        content: byJobJobElement.html(),
        start: job.startProductionDateTime, end: job.endDateTime
      });
    }
  });
  if (unassignedOperatorsCount === 0) {
    const banner = $(`<div class="col-12"/>`)
      .append($(`<div class="alert alert-success d-flex align-items-center justify-content-center" role="alert"/>`)
        .append($(`<i class="fas fa-check-circle me-2"/>`))
        .append($(`<span/>`).text("There are no unassigned operators.")));
    unassignedOperators.append(banner);
  }
  if (unassignedJobsCount === 0) {
    const banner = $(`<div class="col-12"/>`)
      .append($(`<div class="alert alert-success d-flex align-items-center justify-content-center" role="alert"/>`)
        .append($(`<i class="fas fa-check-circle me-2"/>`))
        .append($(`<span/>`).text("There are no unassigned jobs.")));
    unassignedJobs.append(banner);
  }
  const nextDate = JSJoda.LocalDate.parse(schedule.workCalendar.fromDate).plusDays(1);
  byLineTimeline.setWindow(schedule.workCalendar.fromDate, nextDate.toString());
  byJobTimeline.setWindow(schedule.workCalendar.fromDate, nextDate.toString());
}

function solve() {
  $.get(api('/v1/demo-data/BASIC'), function (modelRequest) {
    $.post(api('/v1/schedules'), JSON.stringify(modelRequest), function (metadata) {
      jobId = metadata.id;
      refreshSolvingButtons(metadata.solverStatus || 'SOLVING_ACTIVE');
      if (autoRefreshIntervalId == null) {
        autoRefreshIntervalId = setInterval(getStatus, 2000);
      }
    }).fail(function (xhr) {
      showError("Start solving failed.", xhr);
      refreshSolvingButtons('NOT_SOLVING');
    });
  }).fail(function (xhr) {
    showError("Get demo data failed.", xhr);
  });
}

function analyze() {
  new bootstrap.Modal("#scoreAnalysisModal").show();
  const scoreAnalysisModalContent = $("#scoreAnalysisModalContent");
  scoreAnalysisModalContent.children().remove();
  if (loadedSchedule == null || loadedSchedule.score == null) {
    scoreAnalysisModalContent.text("No score to analyze yet, please first press the 'solve' button.");
  } else if (jobId == null) {
    scoreAnalysisModalContent.text("No solving job yet, please first press the 'solve' button.");
  } else {
    $('#scoreAnalysisScoreLabel').text(`(${loadedSchedule.score})`);
    $.get(api(`/v1/schedules/${jobId}/score-analysis`), function (scoreAnalysis) {
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
      const analysisTBody = $(`<tbody/>`);
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
    }).fail(function (xhr) {
      scoreAnalysisModalContent.children().remove();
      scoreAnalysisModalContent.append($("<p/>").html(
          "The server returned an error."
          + " This may be due to a misconfiguration, or because Score Analysis requires"
          + " <b>Timefold Solver Enterprise Edition</b>, which is not on the classpath."
          + " If the latter, reach out to Timefold, obtain your license,"
          + " and then run the quickstart with an Enterprise profile to see Score analysis in action."));
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

function refreshSolvingButtons(solverStatus) {
  if (isSolving(solverStatus)) {
    $("#solveButton").hide();
    $("#stopSolvingButton").show();
    if (autoRefreshIntervalId == null) {
      autoRefreshIntervalId = setInterval(getStatus, 2000);
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
  $.delete(api(`/v1/schedules/${jobId}`), function () {
    refreshSolvingButtons('NOT_SOLVING');
    getStatus();
  }).fail(function (xhr) {
    showError("Stop solving failed.", xhr);
  });
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

function fallbackCopyTextToClipboard(text) {
  var textArea = document.createElement('textarea');
  textArea.value = text;
  textArea.style.position = 'fixed';
  textArea.style.left = '-9999px';
  textArea.style.top = '0';
  document.body.appendChild(textArea);
  textArea.focus();
  textArea.select();
  try {
    document.execCommand('copy');
  } catch (err) {
    // Copying failed; swallow the error to avoid breaking the UI.
  } finally {
    document.body.removeChild(textArea);
  }
}

function copyTextToClipboard(id) {
  var element = document.getElementById(id);
  if (!element) {
    return;
  }
  var text = (element.textContent || element.value || '').trim();
  if (!text) {
    return;
  }
  if (navigator.clipboard && window.isSecureContext) {
    navigator.clipboard.writeText(text).catch(function () {
      fallbackCopyTextToClipboard(text);
    });
  } else {
    fallbackCopyTextToClipboard(text);
  }
}
