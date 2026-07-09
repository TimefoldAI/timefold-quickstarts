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

let teamMap = null;

// Color Picker: Based on https://venngage.com/blog/color-blind-friendly-palette/
const BG_COLORS = ["#009E73","#0072B2","#D55E00","#000000","#CC79A7","#E69F00","#F0E442","#F6768E","#C10020","#A6BDD7","#803E75","#007D34","#56B4E9","#999999","#8DD3C7","#FFD92F","#B3DE69","#FB8072","#80B1D3","#B15928","#CAB2D6","#1B9E77","#E7298A","#6A3D9A"];
const FG_COLORS = ["#FFFFFF","#FFFFFF","#FFFFFF","#FFFFFF","#FFFFFF","#000000","#000000","#FFFFFF","#FFFFFF","#000000","#FFFFFF","#FFFFFF","#FFFFFF","#000000","#000000","#000000","#000000","#FFFFFF","#000000","#FFFFFF","#000000","#FFFFFF","#FFFFFF","#FFFFFF"];
let COLOR_MAP = new Map();
let nextColorIndex = 0;

function pickColor(object) {
  let color = COLOR_MAP.get(object);
  if (color !== undefined) {
    return color;
  }
  let index = nextColorIndex++ % BG_COLORS.length;
  color = {bg: BG_COLORS[index], fg: FG_COLORS[index]};
  COLOR_MAP.set(object, color);
  return color;
}

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
    updateScheduleMap(loadedSchedule);
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
    $.get(api('/v1/demo-data/BASIC'), function (data) {
      loadedSchedule = data.modelInput;
      updateScheduleMap(loadedSchedule);
      renderSchedule(loadedSchedule, 'NOT_SOLVING');
    }).fail(function (xhr) {
      // disable this page as there is no data
      let $demo = $("#demo");
      $demo.empty();
      $demo.html("<h1><p align=\"center\">No test data available</p></h1>");
    });
  } else {
    $.get(api(`/v1/schedules/${jobId}`), function (data) {
      const solverStatus = data.metadata.solverStatus;
      const solution = data.modelOutput || loadedSchedule;
      loadedSchedule = solution;
      updateScheduleMap(solution);
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

function updateScheduleMap(schedule) {
  teamMap = new Map();
  for (const team of schedule.teams) {
    teamMap.set(team.id, team);
  }
}

function renderSchedule(schedule, solverStatus) {
  refreshSolvingButtons(solverStatus);
  $("#score").text("Score: " + (schedule.score == null ? "?" : schedule.score));
  $("#info").text(`This dataset has ${schedule.teams.length} teams, ${schedule.days.length} days and ${schedule.teamAssignments.length} assignment slots.`);

  const scheduleTable = $("#scheduleTable");
  scheduleTable.children().remove();
  const unassignedAssignments = $("#unassignedAssignments");
  unassignedAssignments.children().remove();

  // Determine the maximum number of assignments per day to size the columns.
  const assignmentsByDay = new Map();
  let maxPerDay = 0;
  $.each(schedule.teamAssignments, (index, assignment) => {
    if (!assignmentsByDay.has(assignment.dateIndex)) {
      assignmentsByDay.set(assignment.dateIndex, []);
    }
    assignmentsByDay.get(assignment.dateIndex).push(assignment);
    maxPerDay = Math.max(maxPerDay, assignmentsByDay.get(assignment.dateIndex).length);
  });

  const thead = $("<thead>").appendTo(scheduleTable);
  const headerRow = $("<tr>").appendTo(thead);
  headerRow.append($("<th>Day</th>"));
  for (let i = 0; i < maxPerDay; i++) {
    headerRow.append($("<th/>").text(`Slot ${i + 1}`));
  }

  const tbody = $("<tbody>").appendTo(scheduleTable);

  $.each(schedule.days, (index, day) => {
    const row = $("<tr>").appendTo(tbody);
    row.append($(`<th class="align-middle"/>`).text(`Day ${day.dateIndex + 1}`));
    const dayAssignments = (assignmentsByDay.get(day.dateIndex) || [])
        .sort((a, b) => a.indexInDay - b.indexInDay);
    for (let i = 0; i < maxPerDay; i++) {
      const cell = $("<td/>").appendTo(row);
      const assignment = dayAssignments[i];
      if (assignment != null) {
        if (assignment.teamId == null) {
          unassignedAssignments.append($(`<div class="col"/>`).append(assignmentCard(assignment)));
        } else {
          cell.append(assignmentCard(assignment));
        }
      }
    }
  });

  if (unassignedAssignments.children().length === 0) {
    const banner = $(`<div class="col-12"/>`)
        .append($(`<div class="alert alert-success d-flex align-items-center justify-content-center" role="alert"/>`)
            .append($(`<i class="fas fa-check-circle me-2"/>`))
            .append($(`<span/>`).text("All team assignments have been assigned!")));
    unassignedAssignments.append(banner);
  }
}

function assignmentCard(assignment) {
  const team = assignment.teamId == null ? null : teamMap.get(assignment.teamId);
  const teamName = team == null ? "Unassigned" : team.name;
  const color = pickColor(assignment.teamId == null ? "unassigned" : assignment.teamId);
  return $(`<div class="card" style="background-color: ${color.bg};color: ${color.fg}"/>`)
      .append($(`<div class="card-body p-2"/>`)
          .append($(`<h5 class="card-title mb-1"/>`).text(teamName))
          .append($(`<small class="ms-2 mt-1 card-text align-bottom float-end"/>`).text(assignment.id)));
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

  $.each([...score.matchAll(/(-?[0-9.]+)(hard|medium|soft)/g)], (i, parts) => {
    components[parts[2]] = parseFloat(parts[1]);
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
