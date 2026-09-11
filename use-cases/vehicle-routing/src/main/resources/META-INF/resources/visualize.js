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
    const seconds = drivingTimeInSeconds ?? 0;
    return `${Math.floor(seconds / 3600)}h ${Math.round((seconds % 3600) / 60)}m`;
}

function colorOfVehicle(vehicleId) {
    return pickColor('vehicle' + vehicleId);
}

function isAssigned(visit) {
    return visit.vehicleId != null;
}

// Mirrors Visit.isServiceFinishedAfterMaxEndTime(): servicing is measured from the moment
// the vehicle arrives, so a visit is late exactly when arrival plus its service duration
// runs past the end of its window - waiting for the window to open does not make it late.
function isLate(visit) {
    return visit.arrivalTime != null
        && JSJoda.OffsetDateTime.parse(visit.arrivalTime).plusMinutes(visit.serviceDurationMinutes)
            .isAfter(JSJoda.OffsetDateTime.parse(visit.maxEndTime));
}

function timelineItemContent(text) {
    return $(`<div/>`).append($(`<h5 class="card-title mb-1"/>`).text(text)).html();
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
    },

    startMap() {
        this.map = L.map('map', {doubleClickZoom: false}).setView([51.505, -0.09], 13);
        L.tileLayer(OPEN_STREET_MAP_TILES, {
            maxZoom: 19,
            attribution: '&copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors',
        }).addTo(this.map);

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
            marker.setPopupContent(`<h5>Vehicle ${vehicle.id}</h5>
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
        const arrival = visit.arrivalTime == null
            ? '<h6>Not assigned to a vehicle.</h6>'
            : `<h6>Vehicle ${visit.vehicleId} arrives at ${showTimeOnly(visit.arrivalTime)}.</h6>`;
        return `<h5>${visit.name}</h5>
            <h6>Demand: ${visit.demand}</h6>
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
        const totalDrivingTimeSeconds = schedule.vehicles
            .reduce((sum, vehicle) => sum + (vehicle.totalDrivingTimeSeconds ?? 0), 0);
        $('#drivingTime').text(formatDrivingTime(totalDrivingTimeSeconds));
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
        <td>Vehicle ${vehicle.id}</td>
        <td>
          <div class="progress" title="Cargo: ${totalDemand} / Capacity: ${vehicle.capacity}">
            <div class="progress-bar" role="progressbar" style="width: ${percentage}%">${totalDemand}/${vehicle.capacity}</div>
          </div>
        </td>
        <td>${formatDrivingTime(vehicle.totalDrivingTimeSeconds)}</td>
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
                content: `<h5 class="card-title mb-1">vehicle-${vehicle.id}</h5>
                          <div class="progress" title="Cargo: ${totalDemand} / Capacity: ${vehicle.capacity}">
                            <div class="progress-bar" role="progressbar" style="width: ${percentage}%">
                              ${totalDemand}/${vehicle.capacity}
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
                // Unassigned: shown at the start of the window, as long as servicing would take.
                const minStartTime = JSJoda.OffsetDateTime.parse(visit.minStartTime);
                this.byVisitItemData.add({
                    id: visit.id + '_unassigned',
                    group: visit.id,
                    content: timelineItemContent('Unassigned'),
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
