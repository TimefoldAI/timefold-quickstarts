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
let initialized = false;
let planId = null;
// The demo dataset ({config, modelInput}) used both as the POST body and as the source of problem facts.
let demoDataset = null;
// Bounding box and time window cached from the demo modelInput; they do not change while solving.
let southWestCorner = null;
let northEastCorner = null;
let startDateTime = null;
let endDateTime = null;

const SOLVING_STATUSES = new Set(["SOLVING_SCHEDULED", "SOLVING_ACTIVE", "SOLVING_STARTED", "SCHEDULED", "STARTED",
    "SOLVING"]);

const solveButton = $('#solveButton');
const stopSolvingButton = $('#stopSolvingButton');
const vehiclesTable = $('#vehicles');

/*************************************** Map constants and variable definitions  **************************************/

const homeLocationMarkerByIdMap = new Map();
const visitMarkerByIdMap = new Map();

const map = L.map('map', {doubleClickZoom: false}).setView([51.505, -0.09], 13);
const visitGroup = L.layerGroup().addTo(map);
const homeLocationGroup = L.layerGroup().addTo(map);
const routeGroup = L.layerGroup().addTo(map);

/************************************ Time line constants and variable definitions ************************************/

const byVehicleTimelineElement = document.getElementById("byVehicleTimeline");
const byVehicleTimelineOptions = {
    timeAxis: {scale: "hour"},
    orientation: {axis: "top"},
    xss: {disabled: true}, // Items are XSS safe through JQuery
    stack: false,
    stackSubgroups: false,
    zoomMin: 1000 * 60 * 60, // A single hour in milliseconds
    zoomMax: 1000 * 60 * 60 * 24 // A single day in milliseconds
};
const byVehicleGroupData = new vis.DataSet();
const byVehicleItemData = new vis.DataSet();
const byVehicleTimeline = new vis.Timeline(byVehicleTimelineElement, byVehicleItemData, byVehicleGroupData, byVehicleTimelineOptions);

const byVisitTimelineElement = document.getElementById("byVisitTimeline");
const byVisitTimelineOptions = {
    timeAxis: {scale: "hour"},
    orientation: {axis: "top"},
    verticalScroll: true,
    xss: {disabled: true}, // Items are XSS safe through JQuery
    stack: false,
    stackSubgroups: false,
    zoomMin: 1000 * 60 * 60, // A single hour in milliseconds
    zoomMax: 1000 * 60 * 60 * 24 // A single day in milliseconds
};
const byVisitGroupData = new vis.DataSet();
const byVisitItemData = new vis.DataSet();
const byVisitTimeline = new vis.Timeline(byVisitTimelineElement, byVisitItemData, byVisitGroupData, byVisitTimelineOptions);

const BG_COLORS = ["#009E73","#0072B2","#D55E00","#000000","#CC79A7","#E69F00","#F0E442","#F6768E","#C10020","#A6BDD7","#803E75","#007D34","#56B4E9","#999999","#8DD3C7","#FFD92F","#B3DE69","#FB8072","#80B1D3","#B15928","#CAB2D6","#1B9E77","#E7298A","#6A3D9A"];
const FG_COLORS = ["#FFFFFF","#FFFFFF","#FFFFFF","#FFFFFF","#FFFFFF","#000000","#000000","#FFFFFF","#FFFFFF","#000000","#FFFFFF","#FFFFFF","#FFFFFF","#000000","#000000","#000000","#000000","#FFFFFF","#000000","#FFFFFF","#000000","#FFFFFF","#FFFFFF","#FFFFFF"];
let COLOR_MAP = new Map()
let nextColorIndex = 0

function pickColor(object) {
    let color = COLOR_MAP.get(object);
    if (color !== undefined) {
        return color;
    }
    let index = nextColorIndex % BG_COLORS.length;
    nextColorIndex++;
    color = {bg : BG_COLORS[index], fg: FG_COLORS[index]};
    COLOR_MAP.set(object,color);
    return color;
}

/************************************ Initialize ************************************/

$(document).ready(function () {
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors',
    }).addTo(map);

    solveButton.click(solve);
    stopSolvingButton.click(stopSolving);
    refreshSolvingButtons(false);

    // HACK to allow vis-timeline to work within Bootstrap tabs
    $("#byVehicleTab").on('shown.bs.tab', function (event) {
        byVehicleTimeline.redraw();
    })
    $("#byVisitTab").on('shown.bs.tab', function (event) {
        byVisitTimeline.redraw();
    })
    setupAjax();
    // Embedded on the platform: hide the demo chrome and load the run read-only.
    if (PLATFORM.onPlatform) {
        document.body.classList.add('on-platform');
        loadPlatformRun();
    } else {
        loadDemoData();
    }
});

// ── Platform: load an existing run (read-only) ──
// ModelRest exposes the run's input at /{id}/model-request (ModelRequest -> {config, modelInput})
// and its output+status at /{id} (ModelResponse -> {metadata:{solverStatus,score}, modelOutput}).
function loadPlatformRun() {
    if (!PLATFORM.runId) {
        showError("No runId provided by platform.", {status: 0, statusText: "missing runId"});
        return;
    }
    planId = PLATFORM.runId;
    $.getJSON(api("/v1/route-plans/" + planId + "/model-request"), function (req) {
        const input = req.modelInput || req;
        cacheBounds(input);
        homeLocationGroup.clearLayers();
        homeLocationMarkerByIdMap.clear();
        visitGroup.clearLayers();
        visitMarkerByIdMap.clear();
        initialized = false;
        render(input.vehicles, input.visits, null);
        refreshRoutePlan();
        if (autoRefreshIntervalId == null) {
            autoRefreshIntervalId = setInterval(refreshRoutePlan, 2000);
        }
    }).fail(function (xhr) {
        showError("Failed to load run input from platform.", xhr);
    });
}

function colorByVehicleId(vehicleId) {
    return vehicleId == null ? null : pickColor('vehicle' + vehicleId);
}

function formatScore(score) {
    if (!score) return '?';
    return score.replace('hard', 'H').replace('medium', 'M').replace('soft', 'S');
}

function formatDrivingTime(drivingTimeInSeconds) {
    return `${Math.floor(drivingTimeInSeconds / 3600)}h ${Math.round((drivingTimeInSeconds % 3600) / 60)}m`;
}

function homeLocationPopupContent(vehicle) {
    return `<h5>Vehicle ${vehicle.id}</h5>Home Location`;
}

function visitPopupContent(visit) {
    const arrival = visit.arrivalTime ? `<h6>Arrival at ${showTimeOnly(visit.arrivalTime)}.</h6>` : '';
    return `<h5>${visit.name}</h5>
    <h6>Demand: ${visit.demand}</h6>
    <h6>Available from ${showTimeOnly(visit.minStartTime)} to ${showTimeOnly(visit.maxEndTime)}.</h6>
    ${arrival}`;
}

// Times are serialized as UTC OffsetDateTime strings (e.g. 2026-07-01T13:00:00Z); this
// js-joda build has no OffsetDateTime, so drop the offset and parse as a LocalDateTime.
function parseLocalDateTime(offsetDateTimeString) {
    return JSJoda.LocalDateTime.parse(offsetDateTimeString.replace(/(Z|[+-]\d{2}:\d{2})$/, ''));
}

function showTimeOnly(offsetDateTimeString) {
    return parseLocalDateTime(offsetDateTimeString).toLocalTime();
}

// LocationDTO is a {latitude, longitude} object; Leaflet expects a [lat, lng] tuple.
function latLng(location) {
    return location ? [location.latitude, location.longitude] : location;
}

function getHomeLocationMarker(vehicle) {
    let marker = homeLocationMarkerByIdMap.get(vehicle.id);
    if (marker) {
        return marker;
    }
    const color = colorByVehicleId(vehicle.id);
    const homeIcon = L.divIcon({
        html: `<i class="fas fa-home" style="color: ${color.bg}; font-size: 20px; text-shadow: -1px -1px 0 #fff, 1px -1px 0 #fff, -1px 1px 0 #fff, 1px 1px 0 #fff;"></i>`,
        className: 'home-location-icon',
        iconSize: [20, 20],
        iconAnchor: [10, 10]
    });
    marker = L.marker(latLng(vehicle.homeLocation), { icon: homeIcon });
    marker.addTo(homeLocationGroup).bindPopup();
    homeLocationMarkerByIdMap.set(vehicle.id, marker);
    return marker;
}

function getVisitMarker(visit) {
    let marker = visitMarkerByIdMap.get(visit.id);
    if (marker) {
        return marker;
    }
    marker = L.circleMarker(latLng(visit.location));
    marker.addTo(visitGroup).bindPopup();
    visitMarkerByIdMap.set(visit.id, marker);
    return marker;
}

function totalDrivingTime(vehicles) {
    return vehicles.reduce((sum, vehicle) => sum + (vehicle.totalDrivingTimeSeconds || 0), 0);
}

function renderRoutes(vehicles, visits, score) {
    if (!initialized && southWestCorner && northEastCorner) {
        map.fitBounds([latLng(southWestCorner), latLng(northEastCorner)]);
    }
    // Vehicles
    vehiclesTable.children().remove();
    vehicles.forEach(function (vehicle) {
        getHomeLocationMarker(vehicle).setPopupContent(homeLocationPopupContent(vehicle));
        const {id, capacity, totalDemand, totalDrivingTimeSeconds} = vehicle;
        const percentage = totalDemand / capacity * 100;
        const color = colorByVehicleId(id);
        vehiclesTable.append(`
      <tr>
        <td><i class="fas fa-home" id="home-${id}"
            style="color: ${color.bg}; font-size: 1.2rem; display: inline-block; width: 1rem; text-align: center"></i></td>
        <td>Vehicle ${id}</td>
        <td>
          <div class="progress" data-bs-toggle="tooltip-load" data-bs-placement="left" data-html="true"
            title="Cargo: ${totalDemand} / Capacity: ${capacity}">
            <div class="progress-bar" role="progressbar" style="width: ${percentage}%">${totalDemand}/${capacity}</div>
          </div>
        </td>
        <td>${formatDrivingTime(totalDrivingTimeSeconds || 0)}</td>
      </tr>`);
    });
    // Visits
    visits.forEach(function (visit) {
        const marker = getVisitMarker(visit);
        marker.setPopupContent(visitPopupContent(visit));
        if (visit.vehicleId != null) {
            marker.setStyle({color: colorByVehicleId(visit.vehicleId).bg, fillOpacity: 0.8});
        } else {
            marker.setStyle({color: '#999999', fillOpacity: 0.5});
        }
    });
    // Route
    routeGroup.clearLayers();
    const visitByIdMap = new Map(visits.map(visit => [visit.id, visit]));
    for (let vehicle of vehicles) {
        const homeLocation = latLng(vehicle.homeLocation);
        const locations = (vehicle.visitIds || [])
            .map(visitId => visitByIdMap.get(visitId))
            .filter(visit => visit != null)
            .map(visit => latLng(visit.location));
        L.polyline([homeLocation, ...locations, homeLocation], {color: colorByVehicleId(vehicle.id).bg}).addTo(routeGroup);
    }

    // Summary
    $('#score').text(formatScore(score));
    $("#info").text(`This dataset has ${visits.length} visits who need to be assigned to ${vehicles.length} vehicles.`);
    $('#drivingTime').text(formatDrivingTime(totalDrivingTime(vehicles)));
}

function renderTimelines(vehicles, visits) {
    byVehicleGroupData.clear();
    byVisitGroupData.clear();
    byVehicleItemData.clear();
    byVisitItemData.clear();

    $.each(vehicles, function (index, vehicle) {
        const {totalDemand, capacity} = vehicle
        const percentage = totalDemand / capacity * 100;
        const vehicleWithLoad = `<h5 class="card-title mb-1">vehicle-${vehicle.id}</h5>
                                 <div class="progress" data-bs-toggle="tooltip-load" data-bs-placement="left"
                                      data-html="true" title="Cargo: ${totalDemand} / Capacity: ${capacity}">
                                   <div class="progress-bar" role="progressbar" style="width: ${percentage}%">
                                      ${totalDemand}/${capacity}
                                   </div>
                                 </div>`
        byVehicleGroupData.add({id: vehicle.id, content: vehicleWithLoad});
    });

    $.each(visits, function (index, visit) {
        const minStartTime = parseLocalDateTime(visit.minStartTime);
        const maxEndTime = parseLocalDateTime(visit.maxEndTime);
        const serviceDuration = JSJoda.Duration.ofSeconds(visit.serviceDurationSeconds);

        const visitGroupElement = $(`<div/>`)
            .append($(`<h5 class="card-title mb-1"/>`).text(`${visit.name}`));
        byVisitGroupData.add({
            id: visit.id,
            content: visitGroupElement.html()
        });

        // Time window per visit.
        byVisitItemData.add({
            id: visit.id + "_readyToDue",
            group: visit.id,
            start: visit.minStartTime,
            end: visit.maxEndTime,
            type: "background",
            style: "background-color: #8AE23433"
        });

        if (visit.vehicleId == null) {
            const byJobJobElement = $(`<div/>`)
                .append($(`<h5 class="card-title mb-1"/>`).text(`Unassigned`));
            byVisitItemData.add({
                id: visit.id + '_unassigned',
                group: visit.id,
                content: byJobJobElement.html(),
                start: minStartTime.toString(),
                end: minStartTime.plus(serviceDuration).toString(),
                style: "background-color: #EF292999"
            });
        } else {
            const arrivalTime = parseLocalDateTime(visit.arrivalTime);
            const beforeReady = arrivalTime.isBefore(minStartTime);
            const arrivalPlusService = arrivalTime.plus(serviceDuration);
            const afterDue = arrivalPlusService.isAfter(maxEndTime);

            const byVehicleElement = $(`<div/>`)
                .append('<div/>')
                .append($(`<h5 class="card-title mb-1"/>`).text(visit.name));
            const byVisitElement = $(`<div/>`)
                .append($(`<h5 class="card-title mb-1"/>`).text('vehicle-' + visit.vehicleId));
            const byVehicleTravelElement = $(`<div/>`)
                .append($(`<h5 class="card-title mb-1"/>`).text('Travel'));

            const previousDeparture = arrivalTime.minusSeconds(visit.drivingTimeSecondsFromPreviousStandstill);
            byVehicleItemData.add({
                id: visit.id + '_travel',
                group: visit.vehicleId,
                subgroup: visit.vehicleId,
                content: byVehicleTravelElement.html(),
                start: previousDeparture.toString(),
                end: visit.arrivalTime,
                style: "background-color: #f7dd8f90"
            });
            if (beforeReady) {
                const byVehicleWaitElement = $(`<div/>`)
                    .append($(`<h5 class="card-title mb-1"/>`).text('Wait'));
                byVehicleItemData.add({
                    id: visit.id + '_wait',
                    group: visit.vehicleId,
                    subgroup: visit.vehicleId,
                    content: byVehicleWaitElement.html(),
                    start: visit.arrivalTime,
                    end: visit.minStartTime
                });
            }
            let serviceElementBackground = afterDue ? '#EF292999' : '#83C15955'
            byVehicleItemData.add({
                id: visit.id + '_service',
                group: visit.vehicleId,
                subgroup: visit.vehicleId,
                content: byVehicleElement.html(),
                start: visit.startServiceTime,
                end: visit.departureTime,
                style: "background-color: " + serviceElementBackground
            });
            byVisitItemData.add({
                id: visit.id,
                group: visit.id,
                content: byVisitElement.html(),
                start: visit.startServiceTime,
                end: visit.departureTime,
                style: "background-color: " + serviceElementBackground
            });
        }
    });

    $.each(vehicles, function (index, vehicle) {
        if ((vehicle.visitIds || []).length > 0 && vehicle.arrivalTime) {
            let lastVisitId = vehicle.visitIds[vehicle.visitIds.length - 1];
            let lastVisit = visits.filter((visit) => visit.id === lastVisitId).pop();
            if (lastVisit && lastVisit.departureTime) {
                byVehicleItemData.add({
                    id: vehicle.id + '_travelBackToHomeLocation',
                    group: vehicle.id,
                    subgroup: vehicle.id,
                    content: $(`<div/>`).append($(`<h5 class="card-title mb-1"/>`).text('Travel')).html(),
                    start: lastVisit.departureTime,
                    end: vehicle.arrivalTime,
                    style: "background-color: #f7dd8f90"
                });
            }
        }
    });

    if (!initialized && startDateTime && endDateTime) {
        byVehicleTimeline.setWindow(startDateTime, endDateTime);
        byVisitTimeline.setWindow(startDateTime, endDateTime);
    }
    requestAnimationFrame(() => {
        if ($('#byVehiclePanel').hasClass('active')) byVehicleTimeline.redraw();
        if ($('#byVisitPanel').hasClass('active')) byVisitTimeline.redraw();
    });
}

function render(vehicles, visits, score) {
    renderRoutes(vehicles, visits, score);
    renderTimelines(vehicles, visits);
    initialized = true;
}

function setupAjax() {
    $.ajaxSetup({
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json,text/plain',
            // On the platform, authenticate every request with the supplied API key.
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
            return jQuery.ajax({url: url, type: method, dataType: type, data: data, success: callback});
        };
    });
}

function cacheBounds(modelInput) {
    southWestCorner = modelInput.southWestCorner;
    northEastCorner = modelInput.northEastCorner;
    startDateTime = modelInput.startDateTime;
    endDateTime = modelInput.endDateTime;
}

function loadDemoData() {
    $.getJSON(api("/v1/demo-data/BASIC"), function (dataset) {
        demoDataset = dataset;
        cacheBounds(dataset.modelInput);
        homeLocationGroup.clearLayers();
        homeLocationMarkerByIdMap.clear();
        visitGroup.clearLayers();
        visitMarkerByIdMap.clear();
        initialized = false;
        render(dataset.modelInput.vehicles, dataset.modelInput.visits, null);
        refreshSolvingButtons(false);
    }).fail(function (xhr) {
        showError("Getting the demo data has failed.", xhr);
    });
}

function solve() {
    if (demoDataset === null) {
        showError("No demo data loaded yet.", {status: 0, statusText: "no data"});
        return;
    }
    $.post(api("/v1/route-plans"), JSON.stringify(demoDataset), function (data) {
        planId = data.id;
        refreshSolvingButtons(true);
    }).fail(function (xhr) {
        showError("Start solving failed.", xhr);
        refreshSolvingButtons(false);
    });
}

function refreshRoutePlan() {
    if (planId === null) {
        loadDemoData();
        return;
    }
    $.getJSON(api("/v1/route-plans/" + planId), function (plan) {
        const metadata = plan.metadata || {};
        const output = plan.modelOutput || {};
        render(output.vehicles || [], output.visits || [], metadata.score);
        refreshSolvingButtons(SOLVING_STATUSES.has(metadata.solverStatus));
    }).fail(function (xhr) {
        showError("Getting the route plan has failed.", xhr);
        refreshSolvingButtons(false);
    });
}

function refreshSolvingButtons(solving) {
    if (solving) {
        $("#solveButton").hide();
        $("#stopSolvingButton").show();
        if (autoRefreshIntervalId == null) {
            autoRefreshIntervalId = setInterval(refreshRoutePlan, 2000);
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
    if (planId === null) {
        return;
    }
    $.delete(api("/v1/route-plans/" + planId), function () {
        refreshSolvingButtons(false);
        refreshRoutePlan();
    }).fail(function (xhr) {
        showError("Stop solving failed.", xhr);
    });
}

function copyTextToClipboard(id) {
    let text = $("#" + id).text().trim();
    let dummy = document.createElement("textarea");
    document.body.appendChild(dummy);
    dummy.value = text;
    dummy.select();
    document.execCommand("copy");
    document.body.removeChild(dummy);
}

function showError(title, xhr) {
    let serverErrorMessage = !xhr.responseJSON ? `${xhr.status}: ${xhr.statusText}` : xhr.responseJSON.message;
    console.error(title + "\n" + serverErrorMessage);
    const notification = $(`<div class="toast" role="alert" aria-live="assertive" aria-atomic="true" style="min-width: 50rem"/>`)
        .append($(`<div class="toast-header bg-danger">
                 <strong class="me-auto text-dark">Error</strong>
                 <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
               </div>`))
        .append($(`<div class="toast-body"/>`)
            .append($(`<p/>`).text(title))
            .append($(`<pre/>`).append($(`<code/>`).text(serverErrorMessage)))
        );
    $("#notificationPanel").append(notification);
    notification.toast({delay: 30000});
    notification.toast('show');
}
