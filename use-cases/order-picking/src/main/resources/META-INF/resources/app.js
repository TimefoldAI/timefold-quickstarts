// ── Platform context ──
// When embedded in the Timefold Platform, the iframe URL carries these query params.
// Standalone (local dev), none are present and the app behaves as before.
const PLATFORM = (function () {
    const q = new URL(window.location.href).searchParams;
    return {
        onPlatform: q.has('onPlatform'),
        runId: q.get('runId'),
        apiUrl: q.has('apiUrl') ? decodeURIComponent(q.get('apiUrl')).replace(/\/+$/, '') : null,
        apiKey: q.has('apiKey') ? q.get('apiKey') : null,
    };
})();

// Build an API URL: prefix the platform base when embedded, else root-relative (local dev).
function api(path) {
    return PLATFORM.apiUrl ? PLATFORM.apiUrl + path : path;
}

const PLANS_PATH = '/v1/order-picking-plans';

let autoRefreshIntervalId = null;
let jobId = null;
let loadedSolution = null;

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
    document.body.classList.add('on-platform');
    loadPlatformRun();
  } else {
    getStatus();
  }
});

function loadPlatformRun() {
  if (!PLATFORM.runId) {
    showError("No runId provided by platform.", {status: 0, statusText: "missing runId"});
    return;
  }
  jobId = PLATFORM.runId;
  $.get(api(`${PLANS_PATH}/${jobId}/model-request`), function (req) {
    loadedSolution = req.modelInput || req;
    renderSolution(loadedSolution, 'NOT_SOLVING');
    getStatus();
  }).fail(function (xhr) {
    showError("Failed to load run input from platform.", xhr);
  });
}

function setupAjax() {
  $.ajaxSetup({
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json,text/plain',
      ...(PLATFORM.apiKey ? {'X-API-KEY': PLATFORM.apiKey} : {})
    }
  });

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
      loadedSolution = data.modelInput;
      renderSolution(loadedSolution, 'NOT_SOLVING');
    }).fail(function (xhr) {
      let $demo = $("#demo");
      $demo.empty();
      $demo.html("<h1><p align=\"center\">No test data available</p></h1>");
    });
  } else {
    $.get(api(`${PLANS_PATH}/${jobId}`), function (data) {
      const solverStatus = data.metadata.solverStatus;
      const solution = data.modelOutput || loadedSolution;
      loadedSolution = solution;
      renderSolution(solution, solverStatus);
    }).fail(function (xhr) {
      showError("Getting the picking plan has failed.", xhr);
      refreshSolvingButtons('NOT_SOLVING');
    });
  }
}

function isSolving(solverStatus) {
  return solverStatus === 'SOLVING_ACTIVE' || solverStatus === 'SOLVING_SCHEDULED'
      || solverStatus === 'SOLVING_STARTED';
}

function locationLabel(location) {
  if (location == null) {
    return "";
  }
  return `${location.shelvingId} ${location.side}/${location.row}`;
}

function renderSolution(solution, solverStatus) {
  refreshSolvingButtons(solverStatus);
  $("#score").text("Score: " + (solution.score == null ? "?" : solution.score));
  $("#info").text(`This dataset has ${solution.pickTasks.length} pick tasks and ${solution.trolleys.length} trolleys.`);

  const pickTaskMap = new Map();
  for (const pickTask of solution.pickTasks) {
    pickTaskMap.set(pickTask.id, pickTask);
  }

  const trolleysContainer = $("#trolleys");
  trolleysContainer.children().remove();
  const unassignedContainer = $("#unassignedPickTasks");
  unassignedContainer.children().remove();

  const assignedIds = new Set();

  $.each(solution.trolleys, (index, trolley) => {
    const pickTaskIds = trolley.pickTaskIds || [];
    const color = pickColor(trolley.id);
    const card = $(`<div class="card h-100"/>`);
    card.append($(`<div class="card-header fw-bold" style="background-color: ${color.bg};color: ${color.fg}"/>`)
        .text(`Trolley ${trolley.id} (${pickTaskIds.length} picks, ${trolley.bucketCount} buckets)`));
    const list = $(`<ul class="list-group list-group-flush"/>`);
    if (pickTaskIds.length === 0) {
      list.append($(`<li class="list-group-item text-muted fst-italic"/>`).text("No picks assigned"));
    }
    $.each(pickTaskIds, (pos, pickTaskId) => {
      assignedIds.add(pickTaskId);
      const pickTask = pickTaskMap.get(pickTaskId);
      const item = $(`<li class="list-group-item d-flex justify-content-between align-items-start"/>`);
      const left = $(`<div/>`)
          .append($(`<div class="fw-semibold"/>`).text(`${pos + 1}. ${pickTask ? pickTask.productName : pickTaskId}`));
      if (pickTask) {
        left.append($(`<small class="text-muted"/>`).text(`Order ${pickTask.orderId} · ${locationLabel(pickTask.location)}`));
      }
      item.append(left);
      list.append(item);
    });
    card.append(list);
    trolleysContainer.append($(`<div class="col"/>`).append(card));
  });

  $.each(solution.pickTasks, (index, pickTask) => {
    if (assignedIds.has(pickTask.id)) {
      return;
    }
    const color = pickColor(pickTask.orderId);
    const card = $(`<div class="card" style="background-color: ${color.bg};color: ${color.fg}"/>`)
        .append($(`<div class="card-body p-2"/>`)
            .append($(`<h6 class="card-title mb-1"/>`).text(pickTask.productName))
            .append($(`<p class="card-text ms-1 mb-1"/>`).append($(`<small/>`).text(`Order ${pickTask.orderId}`)))
            .append($(`<p class="card-text ms-1 mb-0"/>`).append($(`<small/>`).text(locationLabel(pickTask.location)))));
    unassignedContainer.append($(`<div class="col"/>`).append(card));
  });

  if (unassignedContainer.children().length === 0) {
    const banner = $(`<div class="col-12"/>`)
        .append($(`<div class="alert alert-success d-flex align-items-center justify-content-center" role="alert"/>`)
            .append($(`<i class="fas fa-check-circle me-2"/>`))
            .append($(`<span/>`).text("All pick tasks have been assigned to a trolley!")));
    unassignedContainer.append(banner);
  }
}

function solve() {
  $.get(api('/v1/demo-data/BASIC'), function (modelRequest) {
    $.post(api(PLANS_PATH), JSON.stringify(modelRequest), function (metadata) {
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
  if (loadedSolution == null || loadedSolution.score == null) {
    scoreAnalysisModalContent.text("No score to analyze yet, please first press the 'solve' button.");
  } else if (jobId == null) {
    scoreAnalysisModalContent.text("No solving job yet, please first press the 'solve' button.");
  } else {
    $('#scoreAnalysisScoreLabel').text(`(${loadedSolution.score})`);
    $.get(api(`${PLANS_PATH}/${jobId}/score-analysis`), function (scoreAnalysis) {
      let constraints = scoreAnalysis.constraints;
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
          + " <b>Timefold Solver Enterprise Edition</b>, which is not on the classpath."));
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
  $.delete(api(`${PLANS_PATH}/${jobId}`), function () {
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
