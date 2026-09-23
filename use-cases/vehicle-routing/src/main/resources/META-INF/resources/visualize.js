// index.template.html has a single #visualization slot; this fills it with the
// demo's own markup via setVisualizationSlot(), then owns rendering the route
// plan into that markup for the rest of the page's lifetime.

const OPEN_STREET_MAP_TILES = 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';
const UNASSIGNED_COLOR = '#999999';

const TRAVEL_COLOR = '#f7dd8f90';
const WAIT_COLOR = '#97C2FC90';
const SERVICE_COLOR = '#83C15955';
const LATE_COLOR = '#EF292999';
const TIME_WINDOW_COLOR = '#8AE23433';

// vis-timeline and Leaflet both take Date objects unambiguously, where an
// offset-carrying ISO-8601 string would depend on their own parsing.
function toDate(offsetDateTime) {
    return new Date(offsetDateTime);
}

function toLatLng(location) {
    return [location.latitude, location.longitude];
}

function showTimeOnly(offsetDateTime) {
    return JSJoda.OffsetDateTime.parse(offsetDateTime).toLocalTime().toString();
}

function formatDrivingTime(drivingTimeInSeconds) {
    if (drivingTimeInSeconds == null) {
        return '?';
    }
    return `${Math.floor(drivingTimeInSeconds / 3600)}h ${Math.round((drivingTimeInSeconds % 3600) / 60)}m`;
}

// A vehicle that drives no visits drives no time. One that does, but whose route this page changed
// since the solver last timed it, has a driving time that is genuinely unknown here rather than
// zero: it comes from the map service's travel time matrix.
function drivingTimeSecondsOf(vehicle) {
    return (vehicle.visitIds ?? []).length === 0 ? 0 : vehicle.totalDrivingTimeSeconds ?? null;
}

function colorOfVehicle(vehicleId) {
    return pickColor('vehicle' + vehicleId);
}

function isAssigned(visit) {
    return visit.vehicleId != null;
}

// Mirrors Visit.isServiceFinishedAfterMaxEndTime(): servicing starts when the window opens
// at the earliest, so a visit is late exactly when the service finishes - that is, when the
// vehicle departs - past the end of its window.
function isLate(visit) {
    return visit.departureTime != null
        && JSJoda.OffsetDateTime.parse(visit.departureTime)
            .isAfter(JSJoda.OffsetDateTime.parse(visit.maxEndTime));
}

function escapeHtml(value) {
    return String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function timelineItemContent(text) {
    return $(`<div/>`).append($(`<h5 class="card-title mb-1"/>`).text(text)).html();
}


// ── Recommended assignments ──
// Double-clicking the map drops a visit that is not in the plan yet and asks the solver where it
// would best fit, through this model's own /route-plans/recommendation endpoints (see
// VehicleRoutePlanRecommendationResource). It is a standalone-only feature: on the Timefold
// Platform a run is driven from the platform's own screens, so the whole flow stays off there.

// The id a new visit gets: one past the highest of the plan's own ids when those are numbers, as
// the demo datasets' are, and a timestamp otherwise - either way an id no other visit carries.
function nextVisitId(schedule) {
    const numericIds = schedule.visits
        .map((visit) => Number.parseInt(visit.id, 10))
        .filter((id) => Number.isInteger(id));
    return numericIds.length === schedule.visits.length && numericIds.length > 0
        ? String(Math.max(...numericIds) + 1)
        : `visit-${Date.now()}`;
}

// <input type="datetime-local"> speaks local date-times without an offset, while the model speaks
// ISO-8601 with one. The plan's own window supplies that offset, so a visit added here lands in the
// same offset as every visit already in the plan.
function toLocalDateTimeInput(offsetDateTime) {
    return JSJoda.OffsetDateTime.parse(offsetDateTime).toLocalDateTime()
        .truncatedTo(JSJoda.ChronoUnit.MINUTES).toString();
}

function toOffsetDateTime(localDateTime, offsetSource) {
    const offset = JSJoda.OffsetDateTime.parse(offsetSource).offset();
    return JSJoda.LocalDateTime.parse(localDateTime).atOffset(offset).toString();
}

// The page's loadedSchedule is the model input with the solved routes and times overlaid on it;
// these strip it back to exactly the input the endpoints take, so no solver output is sent back as
// if it were input.
function toVisitInput(visit) {
    return {
        id: visit.id,
        name: visit.name,
        location: visit.location,
        demand: visit.demand,
        minStartTime: visit.minStartTime,
        maxEndTime: visit.maxEndTime,
        serviceDurationMinutes: visit.serviceDurationMinutes,
    };
}

function toModelInput(schedule, extraVisit) {
    return {
        startDateTime: schedule.startDateTime,
        endDateTime: schedule.endDateTime,
        vehicles: schedule.vehicles.map((vehicle) => ({
            id: vehicle.id,
            capacity: vehicle.capacity,
            homeLocation: vehicle.homeLocation,
            departureTime: vehicle.departureTime,
            visitIds: vehicle.visitIds ?? [],
        })),
        visits: schedule.visits.map(toVisitInput).concat(extraVisit == null ? [] : [extraVisit]),
    };
}

// The vehicle with the visit spliced into its route at the recommended position. Its load stays
// exact - it is a plain sum of the route's demands - but its driving times do not: those come from
// the map service's travel time matrix, so they are dropped instead of kept stale.
function withVisitInserted(vehicle, visit, index, visits) {
    const route = (vehicle.visitIds ?? []).slice();
    route.splice(index, 0, visit.id);
    const demandByVisitId = new Map(visits.map((candidate) => [candidate.id, candidate.demand]));
    return {
        ...vehicle,
        visitIds: route,
        totalDemand: route.reduce((sum, visitId) => sum + (demandByVisitId.get(visitId) ?? 0), 0),
        totalDrivingTimeSeconds: null,
        arrivalTime: null,
    };
}

// A visit that is still on its route, but is reached at a time this page cannot work out.
function withoutTimings(visit) {
    return {
        ...visit,
        arrivalTime: null,
        startServiceTime: null,
        departureTime: null,
        drivingTimeSecondsFromPreviousStandstill: null,
    };
}

function newVisitModal() {
    return bootstrap.Modal.getOrCreateInstance(document.getElementById('newVisitModal'));
}

// The same constraint breakdown the score analysis modal shows, for the score difference one
// recommendation would make.
function scoreAnalysisDiffTable(scoreAnalysisDiff) {
    const constraints = scoreAnalysisDiff?.constraints ?? [];
    if (constraints.length === 0) {
        return '<p class="mb-0 text-muted small">This recommendation changes no constraint.</p>';
    }
    const rows = constraints.map((constraint) => {
        const matches = (constraint.matches ?? [])
            .map((match) => `<li class="list-group-item list-group-item-light py-1">
                ${escapeHtml(match.justification?.description ?? match.score)}</li>`)
            .join('');
        return `<tr>
                <td class="text-start">${escapeHtml(constraint.name)}</td>
                <td>${escapeHtml(constraint.matchCount)}</td>
                <td>${escapeHtml(constraint.weight)}</td>
                <td>${escapeHtml(constraint.score)}</td>
            </tr>
            ${matches === '' ? '' : `<tr><td colspan="4" class="text-start">
                <ul class="list-group list-group-flush">${matches}</ul></td></tr>`}`;
    }).join('');
    return `<table class="table table-sm mb-0 text-center">
            <thead><tr>
                <th class="text-start">Constraint</th><th># Matches</th><th>Weight</th><th>Score</th>
            </tr></thead>
            <tbody>${rows}</tbody>
        </table>`;
}

const app = {
    start() {
        setVisualizationSlot(`
    <ul class="nav nav-pills viewTabs" role="tablist">
        <li class="nav-item" role="presentation">
            <button class="nav-link active" id="mapTab" data-bs-toggle="tab"
                    data-bs-target="#mapPanel" type="button" role="tab" aria-controls="mapPanel"
                    aria-selected="true">Map
            </button>
        </li>
        <li class="nav-item" role="presentation">
            <button class="nav-link" id="byVehicleTab" data-bs-toggle="tab"
                    data-bs-target="#byVehiclePanel" type="button" role="tab" aria-controls="byVehiclePanel"
                    aria-selected="false">By vehicle
            </button>
        </li>
        <li class="nav-item" role="presentation">
            <button class="nav-link" id="byVisitTab" data-bs-toggle="tab"
                    data-bs-target="#byVisitPanel" type="button" role="tab" aria-controls="byVisitPanel"
                    aria-selected="false">By visit
            </button>
        </li>
    </ul>
    <div class="tab-content">
        <div class="tab-pane fade show active" id="mapPanel" role="tabpanel" aria-labelledby="mapTab">
            <div id="mapContainer" class="position-relative">
                <div id="map"></div>
                <div id="solutionSummaryPanel" class="card shadow-sm">
                    <h5>Solution summary</h5>
                    <table class="table table-sm">
                        <tbody>
                        <tr>
                            <td>Total driving time:</td>
                            <td><span id="drivingTime">unknown</span></td>
                        </tr>
                        <tr>
                            <td>Unassigned visits:</td>
                            <td><span id="unassignedVisitCount">unknown</span></td>
                        </tr>
                        </tbody>
                    </table>
                    <h5>Vehicles</h5>
                    <table class="table-sm w-100">
                        <thead>
                        <tr>
                            <th class="col-1"></th>
                            <th class="col-3">Name</th>
                            <th class="col-4">
                                Load
                                <i class="fas fa-info-circle" title="Vehicle load is displayed as: total cargo / vehicle capacity."></i>
                            </th>
                            <th class="col-4">Driving time</th>
                        </tr>
                        </thead>
                        <tbody id="vehicles"></tbody>
                    </table>
                </div>
            </div>
        </div>
        <div class="tab-pane fade" id="byVehiclePanel" role="tabpanel" aria-labelledby="byVehicleTab">
            <div class="d-flex align-items-center flex-wrap gap-2 px-3 py-2 border-bottom bg-light">
                <span class="text-muted small">Each row shows a vehicle's day: travel blocks, optional wait, and service stops.</span>
                <div class="ms-auto d-flex gap-3 flex-wrap">
                    <span class="d-flex align-items-center gap-1 small"><span class="legend-swatch" style="background:#f7dd8f;"></span>Travel</span>
                    <span class="d-flex align-items-center gap-1 small"><span class="legend-swatch" style="background:#97C2FC;"></span>Wait</span>
                    <span class="d-flex align-items-center gap-1 small"><span class="legend-swatch" style="background:#83C159;"></span>Service</span>
                    <span class="d-flex align-items-center gap-1 small"><span class="legend-swatch" style="background:#EF2929;"></span>Late</span>
                </div>
            </div>
            <div id="byVehicleTimeline"></div>
        </div>
        <div class="tab-pane fade" id="byVisitPanel" role="tabpanel" aria-labelledby="byVisitTab">
            <div class="d-flex align-items-center flex-wrap gap-2 px-3 py-2 border-bottom bg-light">
                <span class="text-muted small">Each row shows a visit's assigned vehicle and where it falls relative to its time window.</span>
                <div class="ms-auto d-flex gap-3 flex-wrap">
                    <span class="d-flex align-items-center gap-1 small"><span class="legend-swatch" style="background:#8AE234;opacity:0.4;"></span>Time window</span>
                    <span class="d-flex align-items-center gap-1 small"><span class="legend-swatch" style="background:#83C159;"></span>Assigned</span>
                    <span class="d-flex align-items-center gap-1 small"><span class="legend-swatch" style="background:#EF2929;"></span>Late / Unassigned</span>
                </div>
            </div>
            <div id="byVisitTimeline"></div>
        </div>
    </div>
    <div class="modal fade" id="newVisitModal" tabindex="-1" aria-labelledby="newVisitModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-lg modal-dialog-scrollable">
            <div class="modal-content">
                <div class="modal-header">
                    <h1 class="modal-title fs-5" id="newVisitModalLabel">New visit</h1>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body" id="newVisitModalContent"></div>
                <div class="modal-footer" id="newVisitModalFooter"></div>
            </div>
        </div>
    </div>
`);

        this.startMap();
        this.startTimelines();

        // The map only has a size once its tab is on screen, and a timeline in a hidden
        // tab pane has no width to lay itself out against, so both need a nudge once shown.
        document.getElementById("mapTab").addEventListener('click', () => {
            this.quickstartPage.changeRenderer((schedule) => this.renderMap(schedule));
            this.map.invalidateSize();
        });
        document.getElementById("byVehicleTab").addEventListener('click', () => {
            this.byVehicleTimeline.redraw();
            this.quickstartPage.changeRenderer((schedule) => this.renderByVehicle(schedule));
        });
        document.getElementById("byVisitTab").addEventListener('click', () => {
            this.byVisitTimeline.redraw();
            this.quickstartPage.changeRenderer((schedule) => this.renderByVisit(schedule));
        });

        this.quickstartPage = new QuickstartPage({
            modelPath: '/v1/route-plans',
            renderSchedule: (schedule) => this.renderMap(schedule),
            renderInfo: (schedule) => this.renderInfo(schedule),
            mergeModelOutput: (schedule, modelOutput) => this.mergeModelOutput(schedule, modelOutput),
        });

        this.startRecommendations();
    },

    startMap() {
        this.map = L.map('map', {doubleClickZoom: false}).setView([51.505, -0.09], 13);
        L.tileLayer(OPEN_STREET_MAP_TILES, {
            maxZoom: 19,
            attribution: '&copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors',
        }).addTo(this.map);
        // Grayscale tiles (on by default) keep the colored routes below readable;
        // L.control.grayscale comes from shared/leaflet-grayscale.js.
        L.control.grayscale().addTo(this.map);

        this.homeLocationGroup = L.layerGroup().addTo(this.map);
        this.visitGroup = L.layerGroup().addTo(this.map);
        this.routeGroup = L.layerGroup().addTo(this.map);
        // Markers are kept across renders, so a popup the user opened survives the
        // two-second refresh; they are dropped again when the dataset no longer has them.
        this.homeLocationMarkerById = new Map();
        this.visitMarkerById = new Map();
        this.fittedBounds = null;
    },

    startTimelines() {
        const timelineOptions = {
            timeAxis: {scale: "hour"},
            orientation: {axis: "top"},
            xss: {disabled: true}, // Items are XSS safe through JQuery
            stack: false,
            stackSubgroups: false,
            zoomMin: 1000 * 60 * 60, // A single hour in milliseconds
            zoomMax: 1000 * 60 * 60 * 24 // A single day in milliseconds
        };

        this.byVehicleGroupData = new vis.DataSet();
        this.byVehicleItemData = new vis.DataSet();
        this.byVehicleTimeline = new vis.Timeline(document.getElementById("byVehicleTimeline"),
            this.byVehicleItemData, this.byVehicleGroupData, timelineOptions);

        this.byVisitGroupData = new vis.DataSet();
        this.byVisitItemData = new vis.DataSet();
        this.byVisitTimeline = new vis.Timeline(document.getElementById("byVisitTimeline"),
            this.byVisitItemData, this.byVisitGroupData, {...timelineOptions, verticalScroll: true});

        this.timelineWindow = null;
    },

    // modelOutput only carries the solved routes and times, not the full problem, so schedule
    // (the QuickstartPage's loadedSchedule) keeps the full modelInput (map corners, planning
    // window, vehicle and visit details) and this only overlays the solution onto it. The
    // assignment is the route list, exactly as in VehicleRoutePlanModelConvertor's
    // applyOutputToInput: a visit in no vehicle's visitIds is unassigned.
    mergeModelOutput(schedule, modelOutput) {
        if (schedule == null || modelOutput == null) {
            return;
        }
        if (modelOutput.vehicles != null) {
            const routeByVehicleId = new Map(modelOutput.vehicles.map(vehicle => [vehicle.id, vehicle]));
            schedule.vehicles = schedule.vehicles.map(vehicle => {
                const route = routeByVehicleId.get(vehicle.id);
                return route == null ? vehicle : {
                    ...vehicle,
                    visitIds: route.visitIds ?? [],
                    totalDemand: route.totalDemand,
                    totalDrivingTimeSeconds: route.totalDrivingTimeSeconds,
                    arrivalTime: route.arrivalTime,
                };
            });
        }
        if (modelOutput.visits != null) {
            const solvedByVisitId = new Map(modelOutput.visits.map(visit => [visit.id, visit]));
            schedule.visits = schedule.visits.map(visit => {
                const solved = solvedByVisitId.get(visit.id);
                return solved == null ? visit : {
                    ...visit,
                    vehicleId: solved.vehicleId,
                    arrivalTime: solved.arrivalTime,
                    startServiceTime: solved.startServiceTime,
                    departureTime: solved.departureTime,
                    drivingTimeSecondsFromPreviousStandstill: solved.drivingTimeSecondsFromPreviousStandstill,
                };
            });
        }
    },

    renderInfo(schedule) {
        if (schedule == null) {
            return "";
        }
        // The same counts VehicleRoutePlanInputMetrics reports for the dataset.
        const totalDemand = schedule.visits.reduce((sum, visit) => sum + visit.demand, 0);
        const totalCapacity = schedule.vehicles.reduce((sum, vehicle) => sum + vehicle.capacity, 0);
        return `${schedule.visits.length} visits · ${schedule.vehicles.length} vehicles `
            + `· demand ${totalDemand} of ${totalCapacity} capacity`;
    },

    // Every renderer assigns the vehicle colors in dataset order first, so a vehicle keeps
    // the same color on the map and on both timelines, whichever one is drawn.
    prepareRender(schedule) {
        resetColorMap();
        schedule.vehicles.forEach((vehicle) => colorOfVehicle(vehicle.id));
    },

    // ── Map ──

    renderMap(schedule) {
        this.prepareRender(schedule);
        this.fitBounds(schedule);
        this.renderHomeLocations(schedule);
        this.renderVisitMarkers(schedule);
        this.renderRoutes(schedule);
        this.renderSummary(schedule);
    },

    // Only on the first render of a dataset: re-framing on every two-second refresh would
    // undo whatever the user panned or zoomed to. The bounding box is not part of the model
    // input (it was only ever a demo-data-generation detail, not something the UI should
    // depend on), so it is derived here from every vehicle's home location and every visit.
    fitBounds(schedule) {
        const points = [
            ...schedule.vehicles.map(vehicle => toLatLng(vehicle.homeLocation)),
            ...schedule.visits.map(visit => toLatLng(visit.location)),
        ];
        if (points.length === 0) {
            return;
        }
        const boundsKey = JSON.stringify(points);
        if (this.fittedBounds === boundsKey) {
            return;
        }
        this.fittedBounds = boundsKey;
        // The map is constructed while the page body is still hidden (see index.html), so
        // Leaflet caches a 0x0 size at that point; invalidateSize() forces it to re-measure
        // the now-visible container before boxing the bounds, or tiles outside that stale
        // size never get requested.
        this.map.invalidateSize();
        this.map.fitBounds(L.latLngBounds(points), {padding: [20, 20]});
    },

    renderHomeLocations(schedule) {
        const vehicleIds = new Set(schedule.vehicles.map(vehicle => vehicle.id));
        this.dropStaleMarkers(this.homeLocationMarkerById, this.homeLocationGroup, vehicleIds);

        schedule.vehicles.forEach((vehicle) => {
            const color = colorOfVehicle(vehicle.id);
            let marker = this.homeLocationMarkerById.get(vehicle.id);
            if (!marker) {
                const homeIcon = L.divIcon({
                    html: `<i class="fas fa-home" style="color: ${color.bg}; font-size: 20px; text-shadow: -1px -1px 0 #fff, 1px -1px 0 #fff, -1px 1px 0 #fff, 1px 1px 0 #fff;"></i>`,
                    className: 'home-location-icon',
                    iconSize: [20, 20],
                    iconAnchor: [10, 10]
                });
                marker = L.marker(toLatLng(vehicle.homeLocation), {icon: homeIcon});
                marker.addTo(this.homeLocationGroup).bindPopup();
                this.homeLocationMarkerById.set(vehicle.id, marker);
            }
            // Demo datasets reuse the same simple vehicle/visit ids across different cities, so a
            // marker kept across a dataset switch still needs to be moved to its new coordinates.
            marker.setLatLng(toLatLng(vehicle.homeLocation));
            marker.setPopupContent(`<h5>Vehicle ${escapeHtml(vehicle.id)}</h5>
                <h6>Home location, departing at ${showTimeOnly(vehicle.departureTime)}.</h6>`);
        });
    },

    renderVisitMarkers(schedule) {
        const visitIds = new Set(schedule.visits.map(visit => visit.id));
        this.dropStaleMarkers(this.visitMarkerById, this.visitGroup, visitIds);

        schedule.visits.forEach((visit) => {
            let marker = this.visitMarkerById.get(visit.id);
            if (!marker) {
                marker = L.circleMarker(toLatLng(visit.location));
                marker.addTo(this.visitGroup).bindPopup();
                this.visitMarkerById.set(visit.id, marker);
            }
            // Demo datasets reuse the same simple vehicle/visit ids across different cities, so a
            // marker kept across a dataset switch still needs to be moved to its new coordinates.
            marker.setLatLng(toLatLng(visit.location));
            marker.setPopupContent(this.visitPopupContent(visit));
            if (isAssigned(visit)) {
                marker.setStyle({color: colorOfVehicle(visit.vehicleId).bg, fillOpacity: 0.8});
            } else {
                marker.setStyle({color: UNASSIGNED_COLOR, fillOpacity: 0.5});
            }
        });
    },

    visitPopupContent(visit) {
        let arrival;
        if (!isAssigned(visit)) {
            arrival = '<h6>Not assigned to a vehicle.</h6>';
        } else if (visit.arrivalTime == null) {
            arrival = `<h6>On vehicle ${escapeHtml(visit.vehicleId)}'s route, at a time that is not computed yet.</h6>`;
        } else {
            arrival = `<h6>Vehicle ${escapeHtml(visit.vehicleId)} arrives at ${showTimeOnly(visit.arrivalTime)}.</h6>`;
        }
        return `<h5>${escapeHtml(visit.name)}</h5>
            <h6>Demand: ${escapeHtml(visit.demand)}</h6>
            <h6>Available from ${showTimeOnly(visit.minStartTime)} to ${showTimeOnly(visit.maxEndTime)}.</h6>
            ${arrival}`;
    },

    renderRoutes(schedule) {
        this.routeGroup.clearLayers();
        const visitById = new Map(schedule.visits.map(visit => [visit.id, visit]));
        schedule.vehicles.forEach((vehicle) => {
            const homeLocation = toLatLng(vehicle.homeLocation);
            const stops = (vehicle.visitIds ?? [])
                .map(visitId => visitById.get(visitId))
                .filter(visit => visit != null)
                .map(visit => toLatLng(visit.location));
            if (stops.length === 0) {
                return;
            }
            L.polyline([homeLocation, ...stops, homeLocation], {color: colorOfVehicle(vehicle.id).bg})
                .addTo(this.routeGroup);
        });
    },

    renderSummary(schedule) {
        // One unknown route makes the fleet's total unknown too, rather than quietly too low.
        const drivingTimes = schedule.vehicles.map(drivingTimeSecondsOf);
        $('#drivingTime').text(formatDrivingTime(drivingTimes.includes(null)
            ? null
            : drivingTimes.reduce((sum, seconds) => sum + seconds, 0)));
        $('#unassignedVisitCount').text(schedule.visits.filter(visit => !isAssigned(visit)).length);

        const vehiclesTable = $('#vehicles');
        vehiclesTable.children().remove();
        schedule.vehicles.forEach((vehicle) => {
            const totalDemand = vehicle.totalDemand ?? 0;
            const percentage = vehicle.capacity === 0 ? 0 : totalDemand / vehicle.capacity * 100;
            const color = colorOfVehicle(vehicle.id);
            vehiclesTable.append(`
      <tr>
        <td>
          <i class="fas fa-home"
            style="color: ${color.bg}; font-size: 1.2rem; display: inline-block; width: 1rem; text-align: center">
          </i>
        </td>
        <td>Vehicle ${escapeHtml(vehicle.id)}</td>
        <td>
          <div class="progress" title="Cargo: ${escapeHtml(totalDemand)} / Capacity: ${escapeHtml(vehicle.capacity)}">
            <div class="progress-bar" role="progressbar" style="width: ${percentage}%">${escapeHtml(totalDemand)}/${escapeHtml(vehicle.capacity)}</div>
          </div>
        </td>
        <td>${formatDrivingTime(drivingTimeSecondsOf(vehicle))}</td>
      </tr>`);
        });
    },

    // ── Timelines ──

    renderByVehicle(schedule) {
        this.prepareRender(schedule);
        this.byVehicleGroupData.clear();
        this.byVehicleItemData.clear();

        schedule.vehicles.forEach((vehicle) => {
            const totalDemand = vehicle.totalDemand ?? 0;
            const percentage = vehicle.capacity === 0 ? 0 : totalDemand / vehicle.capacity * 100;
            this.byVehicleGroupData.add({
                id: vehicle.id,
                content: `<h5 class="card-title mb-1">vehicle-${escapeHtml(vehicle.id)}</h5>
                          <div class="progress" title="Cargo: ${escapeHtml(totalDemand)} / Capacity: ${escapeHtml(vehicle.capacity)}">
                            <div class="progress-bar" role="progressbar" style="width: ${percentage}%">
                              ${escapeHtml(totalDemand)}/${escapeHtml(vehicle.capacity)}
                            </div>
                          </div>`
            });
        });

        schedule.visits.filter(isAssigned).forEach((visit) => {
            // Driving to the visit: back from its arrival time by the driving time the solver
            // reported for that leg, which starts at the previous stop's departure.
            if (visit.drivingTimeSecondsFromPreviousStandstill != null && visit.arrivalTime != null) {
                const arrivalTime = JSJoda.OffsetDateTime.parse(visit.arrivalTime);
                this.byVehicleItemData.add({
                    id: visit.id + '_travel',
                    group: visit.vehicleId,
                    subgroup: visit.vehicleId,
                    content: timelineItemContent('Travel'),
                    start: toDate(arrivalTime.minusSeconds(visit.drivingTimeSecondsFromPreviousStandstill).toString()),
                    end: toDate(visit.arrivalTime),
                    style: "background-color: " + TRAVEL_COLOR
                });
            }
            // Waiting, when the vehicle got there before the visit was ready for it.
            if (visit.arrivalTime != null && visit.startServiceTime != null
                && visit.arrivalTime !== visit.startServiceTime) {
                this.byVehicleItemData.add({
                    id: visit.id + '_wait',
                    group: visit.vehicleId,
                    subgroup: visit.vehicleId,
                    content: timelineItemContent('Wait'),
                    start: toDate(visit.arrivalTime),
                    end: toDate(visit.startServiceTime),
                    style: "background-color: " + WAIT_COLOR
                });
            }
            if (visit.startServiceTime != null && visit.departureTime != null) {
                this.byVehicleItemData.add({
                    id: visit.id + '_service',
                    group: visit.vehicleId,
                    subgroup: visit.vehicleId,
                    content: timelineItemContent(visit.name),
                    start: toDate(visit.startServiceTime),
                    end: toDate(visit.departureTime),
                    style: "background-color: " + (isLate(visit) ? LATE_COLOR : SERVICE_COLOR)
                });
            }
        });

        // The drive home, which no visit carries: it runs from the last stop's departure to
        // the vehicle's own arrival time.
        const visitById = new Map(schedule.visits.map(visit => [visit.id, visit]));
        schedule.vehicles.forEach((vehicle) => {
            const visitIds = vehicle.visitIds ?? [];
            if (visitIds.length === 0 || vehicle.arrivalTime == null) {
                return;
            }
            const lastVisit = visitById.get(visitIds[visitIds.length - 1]);
            if (lastVisit == null || lastVisit.departureTime == null) {
                return;
            }
            this.byVehicleItemData.add({
                id: vehicle.id + '_travelBackToHomeLocation',
                group: vehicle.id,
                subgroup: vehicle.id,
                content: timelineItemContent('Travel'),
                start: toDate(lastVisit.departureTime),
                end: toDate(vehicle.arrivalTime),
                style: "background-color: " + TRAVEL_COLOR
            });
        });

        this.setWindow(schedule);
    },

    renderByVisit(schedule) {
        this.prepareRender(schedule);
        this.byVisitGroupData.clear();
        this.byVisitItemData.clear();

        schedule.visits.forEach((visit) => {
            this.byVisitGroupData.add({id: visit.id, content: timelineItemContent(visit.name)});
            // The green band is the window the visit accepts a vehicle in.
            this.byVisitItemData.add({
                id: visit.id + '_readyToDue',
                group: visit.id,
                start: toDate(visit.minStartTime),
                end: toDate(visit.maxEndTime),
                type: "background",
                style: "background-color: " + TIME_WINDOW_COLOR
            });

            if (isAssigned(visit) && visit.startServiceTime != null && visit.departureTime != null) {
                this.byVisitItemData.add({
                    id: visit.id,
                    group: visit.id,
                    content: timelineItemContent('vehicle-' + visit.vehicleId),
                    start: toDate(visit.startServiceTime),
                    end: toDate(visit.departureTime),
                    style: "background-color: " + (isLate(visit) ? LATE_COLOR : SERVICE_COLOR)
                });
            } else {
                // Either on no route at all, or on one this page changed since the solver last timed
                // it. Both are shown at the start of the window, as long as servicing would take.
                const minStartTime = JSJoda.OffsetDateTime.parse(visit.minStartTime);
                this.byVisitItemData.add({
                    id: visit.id + '_unassigned',
                    group: visit.id,
                    content: timelineItemContent(isAssigned(visit) ? 'Not timed yet' : 'Unassigned'),
                    start: toDate(visit.minStartTime),
                    end: toDate(minStartTime.plusMinutes(visit.serviceDurationMinutes).toString()),
                    style: "background-color: " + LATE_COLOR
                });
            }
        });

        this.setWindow(schedule);
    },

    // Both timelines share the dataset's planning window, and only on its first render, so a
    // refresh does not undo the user's zoom.
    setWindow(schedule) {
        const windowKey = schedule.startDateTime + '/' + schedule.endDateTime;
        if (this.timelineWindow === windowKey) {
            return;
        }
        this.timelineWindow = windowKey;
        this.byVehicleTimeline.setWindow(toDate(schedule.startDateTime), toDate(schedule.endDateTime));
        this.byVisitTimeline.setWindow(toDate(schedule.startDateTime), toDate(schedule.endDateTime));
    },

    // ── Recommended assignments ──

    startRecommendations() {
        this.newVisit = null;
        this.newVisitMarker = null;
        // The dataset the plan on screen was edited from, so modelRequestToSolve() knows whether the
        // plan is still the published one. Stays null when there is no way to edit it at all.
        this.editedPlanDemoDataId = null;

        // Embedded in the platform the plan is the platform's to change, not this page's.
        if (SETUP.onPlatform) {
            return;
        }

        // The map is created with doubleClickZoom off, so this gesture is free for adding a visit
        // and a single click still pans and selects markers as usual.
        this.map.on('dblclick', (e) => this.openNewVisitModal(e.latlng));
        // The marker stands for a visit that is not in the plan yet, so it goes with the modal.
        document.getElementById('newVisitModal')
            .addEventListener('hidden.bs.modal', () => this.clearNewVisit());

        $('#mapContainer').append(
            `<div id="mapHint" class="text-muted small">Double-click the map to add a visit.</div>`);

        this.submitPlanOnScreenWhenSolving();
    },

    /**
     * Makes Solve submit the plan on screen once a recommendation has been applied to it.
     */
    submitPlanOnScreenWhenSolving() {
        // The client only exists once the page has resolved SETUP.ready; this handler is registered
        // after the page's own, so it runs after that.
        SETUP.ready.then(() => {
            const client = this.quickstartPage.client;
            const submitRun = client.createRun.bind(client);
            client.createRun = (modelRequest, onSuccess, onFailure) =>
                submitRun(this.planOnScreenOr(modelRequest), onSuccess, onFailure);
        });
    },

    /**
     * @return the model request with the plan on screen in place of the published one, once a
     *         recommendation has been applied to it; the request unchanged otherwise. Either way the
     *         dataset's own run configuration - its name and tags - is the one that is submitted.
     */
    planOnScreenOr(modelRequest) {
        const page = this.quickstartPage;
        // Picking another dataset from the Data dropdown abandons the plan the edit was made to, so
        // the edit only counts while its own dataset is still the selected one.
        if (this.editedPlanDemoDataId == null || this.editedPlanDemoDataId !== page.demoDataId) {
            return modelRequest;
        }
        return {...modelRequest, modelInput: toModelInput(page.loadedSchedule, null)};
    },

    clearNewVisit() {
        if (this.newVisitMarker != null) {
            this.map.removeLayer(this.newVisitMarker);
            this.newVisitMarker = null;
        }
        this.newVisit = null;
    },

    openNewVisitModal(latLng) {
        const schedule = this.quickstartPage.loadedSchedule;
        if (schedule == null) {
            return;
        }
        if (this.quickstartPage.autoRefreshIntervalId != null) {
            // A run in flight overwrites the plan every two seconds, so a visit added to it now
            // would be gone by the next refresh.
            this.quickstartPage.showError("A visit cannot be added while solving is in progress.",
                {status: 0, statusText: "please wait for the run to finish, or stop it"});
            return;
        }

        this.clearNewVisit();
        this.newVisit = {id: nextVisitId(schedule), latitude: latLng.lat, longitude: latLng.lng};
        this.newVisitMarker = L.circleMarker([latLng.lat, latLng.lng], {color: '#198754', fillOpacity: 0.8})
            .addTo(this.map);

        $('#newVisitModalContent').html(this.newVisitFormHtml(schedule, this.newVisit));
        $('#newVisitModalFooter').html(
            `<button id="recommendationButton" type="button" class="btn btn-success">
                 <span class="fas fa-arrow-right"></span> Get recommendations
             </button>`);
        $('#recommendationButton').on('click', () => this.requestRecommendations());
        newVisitModal().show();
    },

    newVisitFormHtml(schedule, newVisit) {
        // The plan's own window bounds the defaults, so a visit added here is servable in principle
        // and every recommendation is about where it fits, not about whether it can fit at all.
        const windowStart = toLocalDateTimeInput(schedule.startDateTime);
        const windowEnd = toLocalDateTimeInput(schedule.endDateTime);
        return `
        <div class="row g-3">
            <div class="col-6">
                <label class="form-label" for="inputName">Name</label>
                <input type="text" class="form-control" id="inputName" value="Visit ${escapeHtml(newVisit.id)}" required>
                <div class="invalid-feedback">Field is required</div>
            </div>
            <div class="col-3">
                <label class="form-label" for="inputLatitude">Latitude</label>
                <input type="text" class="form-control" id="inputLatitude" value="${escapeHtml(newVisit.latitude)}" disabled>
            </div>
            <div class="col-3">
                <label class="form-label" for="inputLongitude">Longitude</label>
                <input type="text" class="form-control" id="inputLongitude" value="${escapeHtml(newVisit.longitude)}" disabled>
            </div>
            <div class="col-4">
                <label class="form-label" for="inputDemand">Demand</label>
                <input type="number" min="0" class="form-control" id="inputDemand" value="1" required>
                <div class="invalid-feedback">Field is required</div>
            </div>
            <div class="col-4">
                <label class="form-label" for="inputMinStartTime">Min start time</label>
                <input type="datetime-local" class="form-control" id="inputMinStartTime"
                       value="${escapeHtml(windowStart)}" required>
                <div class="invalid-feedback">Field is required</div>
            </div>
            <div class="col-4">
                <label class="form-label" for="inputMaxEndTime">Max end time</label>
                <input type="datetime-local" class="form-control" id="inputMaxEndTime"
                       value="${escapeHtml(windowEnd)}" required>
                <div class="invalid-feedback">Field is required</div>
            </div>
            <div class="col-4">
                <label class="form-label" for="inputServiceDuration">Service duration (minutes)</label>
                <input type="number" min="0" class="form-control" id="inputServiceDuration" value="30" required>
                <div class="invalid-feedback">Field is required</div>
            </div>
        </div>`;
    },

    /**
     * @return the new visit in the shape the model input takes, or null when the form is incomplete
     */
    readNewVisitForm(schedule) {
        const fields = {
            inputName: 'name',
            inputDemand: 'demand',
            inputMinStartTime: 'minStartTime',
            inputMaxEndTime: 'maxEndTime',
            inputServiceDuration: 'serviceDurationMinutes',
        };
        const values = {};
        let valid = true;
        Object.entries(fields).forEach(([inputId, key]) => {
            const value = $('#' + inputId).val();
            $('#' + inputId).toggleClass('is-invalid', value === '');
            valid = valid && value !== '';
            values[key] = value;
        });
        if (!valid) {
            return null;
        }
        return {
            id: this.newVisit.id,
            name: values.name,
            location: {latitude: this.newVisit.latitude, longitude: this.newVisit.longitude},
            demand: Number.parseInt(values.demand, 10),
            minStartTime: toOffsetDateTime(values.minStartTime, schedule.startDateTime),
            maxEndTime: toOffsetDateTime(values.maxEndTime, schedule.startDateTime),
            serviceDurationMinutes: Number.parseInt(values.serviceDurationMinutes, 10),
        };
    },

    requestRecommendations() {
        const schedule = this.quickstartPage.loadedSchedule;
        const visit = this.readNewVisitForm(schedule);
        if (visit == null) {
            return;
        }
        const modelInput = toModelInput(schedule, visit);
        $('#newVisitModalContent').html(`<p class="mb-0">Looking for the best places for this visit...</p>`);
        $('#newVisitModalFooter').empty();

        $.post(`${this.quickstartPage.client.modelPath}/recommendation`,
            JSON.stringify({modelInput, visitId: visit.id}),
            (recommendations) => this.renderRecommendations(visit, recommendations))
            .fail((xhr) => {
                newVisitModal().hide();
                this.quickstartPage.showError("Getting recommendations has failed.", xhr);
            });
    },

    renderRecommendations(visit, recommendations) {
        if (recommendations.length === 0) {
            $('#newVisitModalContent').html(`<p class="mb-0">No place was found for this visit.</p>`);
            $('#newVisitModalFooter').empty();
            return;
        }
        const content = $('#newVisitModalContent').empty();
        recommendations.forEach((recommendation, index) => {
            const proposition = recommendation.proposition;
            // A null proposition is the solver saying the visit is better left off every route.
            const label = proposition == null
                ? `Leave <b>${escapeHtml(visit.name)}</b> unassigned`
                : `Add <b>${escapeHtml(visit.name)}</b> to vehicle <b>${escapeHtml(proposition.vehicleId)}</b>`
                    + ` at position <b>${escapeHtml(proposition.index + 1)}</b>`;
            content.append(`
            <div class="form-check mb-2">
                <input class="form-check-input" type="radio" name="recommendationOptions"
                       id="recommendationOption${index}" value="${index}" ${index === 0 ? 'checked' : ''}>
                <label class="form-check-label" for="recommendationOption${index}">
                    ${label} (${escapeHtml(recommendation.scoreAnalysisDiff?.score ?? '?')})
                    ${index === 0 ? ' - <b>best</b>' : ''}
                </label>
                <a class="ms-2" data-bs-toggle="collapse" href="#recommendationDetail${index}" role="button"
                   aria-expanded="false" aria-controls="recommendationDetail${index}">
                    <span class="fas fa-chevron-down"></span>
                </a>
                <div class="collapse" id="recommendationDetail${index}">
                    <div class="card card-body p-2">${scoreAnalysisDiffTable(recommendation.scoreAnalysisDiff)}</div>
                </div>
            </div>`);
        });
        $('#newVisitModalFooter').html(
            `<button id="applyRecommendationButton" type="button" class="btn btn-success">
                 <span class="fas fa-check"></span> Accept
             </button>`);
        $('#applyRecommendationButton').on('click', () => this.applyRecommendation(visit, recommendations));
    },

    // Applying a recommendation needs no server: the route list *is* the assignment, so putting the
    // visit at the recommended position is an edit to that list, made right here. What the edit
    // makes unknown is cleared rather than left stale - the driving times come from the map
    // service's travel time matrix and the score from the solver, and neither lives in this page.
    // The plan is then a plan like any other, ready to be submitted for a solve.
    applyRecommendation(visit, recommendations) {
        const selected = recommendations[Number.parseInt($('input[name="recommendationOptions"]:checked').val(), 10)];
        // A null proposition is the "leave it unassigned" option: the visit joins the plan, but no route.
        const proposition = selected?.proposition;
        const schedule = this.quickstartPage.loadedSchedule;

        schedule.visits = schedule.visits.concat([visit]);
        if (proposition != null) {
            visit.vehicleId = proposition.vehicleId;
            schedule.vehicles = schedule.vehicles
                .map((vehicle) => vehicle.id !== proposition.vehicleId
                    ? vehicle
                    : withVisitInserted(vehicle, visit, proposition.index, schedule.visits));
            // Only the stops from the insertion point on are reached at a different time now; the
            // ones before it are reached exactly as before, so their times still hold.
            const route = schedule.vehicles.find((vehicle) => vehicle.id === proposition.vehicleId).visitIds;
            const shiftedVisitIds = new Set(route.slice(proposition.index));
            schedule.visits = schedule.visits
                .map((candidate) => shiftedVisitIds.has(candidate.id) ? withoutTimings(candidate) : candidate);
        }
        // Every constraint weighs in on the score, so the solved one no longer describes this plan.
        schedule.score = null;
        // From here on, Solve submits this plan rather than the dataset it started from.
        this.editedPlanDemoDataId = this.quickstartPage.demoDataId;

        this.renderCurrentSchedule();
        newVisitModal().hide();
    },

    renderCurrentSchedule() {
        const page = this.quickstartPage;
        page.renderScore();
        page.renderSchedule(page.loadedSchedule);
        $("#info").text(page.renderInfo(page.loadedSchedule));
    },

    dropStaleMarkers(markerById, layerGroup, currentIds) {
        markerById.forEach((marker, id) => {
            if (!currentIds.has(id)) {
                layerGroup.removeLayer(marker);
                markerById.delete(id);
            }
        });
    },
};

app.start();
