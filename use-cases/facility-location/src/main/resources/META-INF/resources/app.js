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

const colors = [
  'aqua',
  'aquamarine',
  'blue',
  'blueviolet',
  'chocolate',
  'cornflowerblue',
  'crimson',
  'forestgreen',
  'gold',
  'lawngreen',
  'limegreen',
  'maroon',
  'mediumvioletred',
  'orange',
  'slateblue',
  'tomato',
];
let autoRefreshCount = 0;
let autoRefreshIntervalId = null;
let jobId = null;
let loadedSchedule = null;

let initialized = false;
const facilityByIdMap = new Map();

const solveButton = $('#solveButton');
const analyzeButton = $('#analyzeButton');
const facilitiesTable = $('#facilities');

const toLatLng = (location) => [location.latitude, location.longitude];

const colorById = (i) => colors[i % colors.length];
const colorByFacility = (facility) => facility === null ? null : colorById(facility.id);

const defaultIcon = new L.Icon.Default();
const greyIcon = new L.Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-grey.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.6.0/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41],
});

const createCostFormat = (notation) => new Intl.NumberFormat('en-US', {
  style: 'currency',
  currency: 'USD',
  maximumFractionDigits: 1,
  minimumFractionDigits: 1,
  notation,
});
const shortCostFormat = createCostFormat('compact');
const longCostFormat = createCostFormat('standard');

// ── Platform: load an existing run (read-only) ──
// ModelRest exposes the run's input at /{id}/model-request (ModelRequest → {modelInput})
// and its output+status at /{id} (ModelResponse → {metadata:{solverStatus}, modelOutput}).
const loadPlatformRun = () => {
  if (!PLATFORM.runId) {
    showError('No runId provided by platform.', {status: 0, statusText: 'missing runId'});
    return;
  }
  jobId = PLATFORM.runId;
  $.get(api(`/v1/facilitylocations/${jobId}/model-request`), (req) => {
    const input = req.modelInput || req;
    loadedSchedule = input;
    showProblem({solution: input, scoreExplanation: null});
    getStatus(); // fetch output + render
    // Poll while the run is still solving; refreshSolvingButtons clears it on completion.
    if (autoRefreshIntervalId == null) {
      autoRefreshIntervalId = setInterval(autoRefresh, 1000);
    }
  }).fail((xhr) => {
    showError('Failed to load run input from platform.', xhr);
  });
};

const getStatus = () => {
  if (jobId == null) {
    $.get(api('/v1/demo-data/BASIC'), (data) => {
      loadedSchedule = data.modelInput;
      showProblem({
        solution: data.modelInput,
        scoreExplanation: null,
      });
    }).fail((xhr) => {
      showError('Get demo data failed.', xhr);
    });
  } else {
    $.get(api(`/v1/facilitylocations/${jobId}`), (data) => {
      loadedSchedule = data.modelOutput;
      const solverStatus = data.metadata.solverStatus;
      showProblem({
        solution: data.modelOutput || loadedSchedule,
        scoreExplanation: null,
      });
      refreshSolvingButtons(solverStatus);
    }).fail((xhr) => {
      showError('Get status failed.', xhr);
    });
  }
};

const solve = () => {
  $.get(api('/v1/demo-data/BASIC'), (modelRequest) => {
    $.post(api('/v1/facilitylocations'), JSON.stringify(modelRequest), (metadata) => {
      jobId = metadata.id;
      refreshSolvingButtons(metadata.solverStatus);
      if (autoRefreshIntervalId == null) {
        autoRefreshIntervalId = setInterval(autoRefresh, 1000);
      }
    }).fail((xhr) => {
      showError('Start solving failed.', xhr);
    });
  });
};

const stopSolving = () => {
  $.delete(api(`/v1/facilitylocations/${jobId}`), (data) => {
    refreshSolvingButtons('SOLVING_COMPLETED');
    getStatus();
  }).fail((xhr) => {
    showError('Stop solving failed.', xhr);
  });
};

const refreshSolvingButtons = (solverStatus) => {
  if (solverStatus === 'SOLVING_ACTIVE' || solverStatus === 'SOLVING_SCHEDULED' || solverStatus === 'SOLVING_STARTED') {
    solveButton.html('<i class="fas fa-stop"></i> Stop solving');
    solveButton.removeClass('btn-success').addClass('btn-danger');
    solveButton.off('click').click(stopSolving);
  } else {
    solveButton.html('<i class="fas fa-play"></i> Solve');
    solveButton.removeClass('btn-danger').addClass('btn-success');
    solveButton.off('click').click(solve);
    if (autoRefreshIntervalId != null) {
      clearInterval(autoRefreshIntervalId);
      autoRefreshIntervalId = null;
    }
  }
};

const autoRefresh = () => {
  getStatus();
};

const facilityPopupContent = (facility, cost, color) => `<h5>${facility.name}</h5>
<ul class="list-unstyled">
<li>Usage: ${facility.usedCapacity}/${facility.capacity}</li>
<li>Setup cost: ${cost}</li>
<li><span style="background-color: ${color}; display: inline-block; width: 12px; height: 12px; text-align: center">
</span> ${color}</li>
</ul>`;

const getFacilityMarker = (facility) => {
  let marker = facilityByIdMap.get(facility.id);
  if (marker) {
    return marker;
  }
  marker = L.marker(toLatLng(facility.location));
  marker.addTo(facilityGroup).bindPopup();
  facilityByIdMap.set(facility.id, marker);
  return marker;
};

const showProblem = ({solution, scoreExplanation}) => {
  if (!initialized && solution.bounds) {
    initialized = true;
    map.fitBounds(solution.bounds.map(toLatLng));
  }
  // Facilities
  facilitiesTable.children().remove();
  solution.facilities.forEach((facility) => {
    const {id, setupCost, capacity, usedCapacity, used} = facility;
    const percentage = usedCapacity / capacity * 100;
    const color = colorByFacility(facility);
    const colorIfUsed = facility.used ? color : 'white';
    const icon = facility.used ? defaultIcon : greyIcon;
    const marker = getFacilityMarker(facility);
    marker.setIcon(icon);
    marker.setPopupContent(facilityPopupContent(facility, longCostFormat.format(facility.setupCost), color));
    facilitiesTable.append(`<tr class="${used ? 'table-active' : 'text-muted'}">
<td><i class="fas fa-crosshairs" id="crosshairs-${id}"
style="background-color: ${colorIfUsed}; display: inline-block; width: 1rem; height: 1rem; text-align: center">
</i></td><td>${facility.name}</td>
<td><div class="progress">
<div class="progress-bar" role="progressbar" style="width: ${percentage}%">${usedCapacity}/${capacity}</div>
</div></td>
<td class="text-end">${shortCostFormat.format(setupCost)}</td>
</tr>`);
    $(`#crosshairs-${id}`)
      .mouseenter(() => marker.openPopup())
      .mouseleave(() => marker.closePopup());
  });
  // Consumers
  consumerGroup.clearLayers();
  solution.consumers.forEach((consumer) => {
    const facility = consumer.facilityId ? solution.facilities.find(f => f.id === consumer.facilityId) : null;
    const color = colorByFacility(facility);
    const consumerLatLng = toLatLng(consumer.location);
    L.circleMarker(consumerLatLng, consumer.assigned ? {color} : {}).addTo(consumerGroup);
    if (consumer.assigned && facility) {
      L.polyline([consumerLatLng, toLatLng(facility.location)], {color}).addTo(consumerGroup);
    }
  });
  // Summary
  $('#score').text(solution.score || 'unknown');
  $('#cost').text(solution.totalCost ? longCostFormat.format(solution.totalCost) : 'unknown');
  $('#cost-percentage').text(solution.totalCost ? Math.round(solution.totalCost * 1000 / solution.potentialCost) / 10 : 'unknown');
  $('#distance').text(solution.totalDistance || 'unknown');
  $('#scoreInfo').text(scoreExplanation);
};

function analyze() {
  new bootstrap.Modal("#scoreAnalysisModal").show()
  const scoreAnalysisModalContent = $("#scoreAnalysisModalContent");
  scoreAnalysisModalContent.children().remove();
  if (loadedSchedule.score == null) {
    scoreAnalysisModalContent.text("No score to analyze yet, please first press the 'solve' button.");
  } else {
    $('#scoreAnalysisScoreLabel').text(`(${loadedSchedule.score})`);
    $.get(api(`/v1/facilitylocations/${jobId}/score-analysis`), function (scoreAnalysis) {
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

setupAjax();

// Embedded on the platform: hide the demo chrome and load the run read-only.
if (PLATFORM.onPlatform) {
  document.body.classList.add('on-platform');
}

const map = L.map('map', {doubleClickZoom: false}).setView([51.505, -0.09], 13);
map.whenReady(PLATFORM.onPlatform ? loadPlatformRun : getStatus);

L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
  maxZoom: 19,
  attribution: '&copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors',
}).addTo(map);

const consumerGroup = L.layerGroup();
const facilityGroup = L.layerGroup();
consumerGroup.addTo(map);
facilityGroup.addTo(map);

solveButton.off('click').click(solve);
analyzeButton.click(analyze);

refreshSolvingButtons();

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
