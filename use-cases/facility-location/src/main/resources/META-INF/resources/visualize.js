// index.template.html has a single #visualization slot; this fills it with the
// demo's own markup via setVisualizationSlot(), then owns rendering the
// plan into that markup for the rest of the page's lifetime.
const COLORS = [
    'aqua', 'aquamarine', 'blue', 'blueviolet', 'chocolate', 'cornflowerblue', 'crimson', 'forestgreen',
    'gold', 'lawngreen', 'limegreen', 'maroon', 'mediumvioletred', 'orange', 'slateblue', 'tomato',
];

const createCostFormat = (notation) => new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    maximumFractionDigits: 1,
    minimumFractionDigits: 1,
    notation,
});
const SHORT_COST_FORMAT = createCostFormat('compact');
const LONG_COST_FORMAT = createCostFormat('standard');

const app = {
    start() {
        // Same layout as the vehicle routing quickstart: the map uses the full width, and the plan summary
        // floats on top of it (toggled by the header's #toggleSummaryButton, see quickstart-page.js).
        setVisualizationSlot(`
    <div id="mapContainer" class="position-relative">
        <div id="map"></div>
        <div id="solutionSummaryPanel" class="card shadow-sm">
            <h5>Plan summary</h5>
            <table class="table table-sm">
                <tbody>
                    <tr><td>Facilities used</td><td class="text-end" id="usedFacilities">-</td></tr>
                    <tr><td>Setup cost</td><td class="text-end" id="setupCost">-</td></tr>
                    <tr><td>Total distance</td><td class="text-end" id="totalDistance">-</td></tr>
                </tbody>
            </table>

            <h5>Facilities</h5>
            <table class="table table-sm align-middle mb-0">
                <tbody id="facilities"></tbody>
            </table>
        </div>
    </div>
`);

        this.map = L.map('map', {doubleClickZoom: false}).setView([51.505, -0.09], 12);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19,
            attribution: '&copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors',
        }).addTo(this.map);
        this.facilityGroup = L.layerGroup().addTo(this.map);
        this.consumerGroup = L.layerGroup().addTo(this.map);
        // Markers are reused across renders (a solving plan re-renders every 2 seconds), so the map does not
        // flicker and popups stay open while the solver keeps improving.
        this.facilityMarkerById = new Map();
        this.boundsFitted = false;

        this.quickstartPage = new QuickstartPage({
            modelPath: '/v1/plans',
            renderSchedule: (plan) => this.renderPlan(plan),
            renderInfo: (plan) => this.renderInfo(plan),
            mergeModelOutput: (plan, modelOutput) => this.mergeModelOutput(plan, modelOutput),
        });
    },

    // modelOutput only carries what the solver decided (which facility serves each consumer, and the resulting
    // load per facility), not the full problem, so plan (the QuickstartPage's loadedSchedule) keeps the full
    // modelInput (locations, demands, costs) and this only overlays the solved bits.
    mergeModelOutput(plan, modelOutput) {
        if (plan == null || modelOutput == null) {
            return;
        }
        if (modelOutput.consumers != null) {
            const outputByConsumerId = new Map(modelOutput.consumers.map((consumer) => [consumer.id, consumer]));
            plan.consumers = plan.consumers.map((consumer) => {
                const solved = outputByConsumerId.get(consumer.id);
                return solved == null ? consumer : {...consumer, facilityId: solved.facilityId};
            });
        }
        if (modelOutput.facilities != null) {
            const outputByFacilityId = new Map(modelOutput.facilities.map((facility) => [facility.id, facility]));
            plan.facilities = plan.facilities.map((facility) => {
                const solved = outputByFacilityId.get(facility.id);
                return solved == null ? facility : {...facility, used: solved.used, usedCapacity: solved.usedCapacity};
            });
        }
    },

    renderInfo(plan) {
        if (plan == null) {
            return '';
        }
        return `${plan.facilities.length} facilities · ${plan.consumers.length} consumers`;
    },

    colorOf(plan, facilityId) {
        if (facilityId == null) {
            return null;
        }
        const index = plan.facilities.findIndex((facility) => facility.id === facilityId);
        return index < 0 ? null : COLORS[index % COLORS.length];
    },

    latLngOf(location) {
        return [location.latitude, location.longitude];
    },

    renderPlan(plan) {
        const facilityById = new Map(plan.facilities.map((facility) => [facility.id, facility]));

        if (!this.boundsFitted && (plan.facilities.length > 0 || plan.consumers.length > 0)) {
            // The input carries no bounding box, so the viewport is derived from the data itself.
            const allLatLngs = [...plan.facilities, ...plan.consumers].map((point) => this.latLngOf(point.location));
            this.map.fitBounds(L.latLngBounds(allLatLngs).pad(0.05));
            this.boundsFitted = true;
        }

        this.renderFacilities(plan);
        this.renderConsumers(plan, facilityById);
        this.renderSummary(plan);
    },

    renderFacilities(plan) {
        const facilitiesTable = $('#facilities');
        facilitiesTable.children().remove();

        plan.facilities.forEach((facility) => {
            const color = this.colorOf(plan, facility.id);
            const usedCapacity = facility.usedCapacity ?? 0;
            const used = facility.used ?? usedCapacity > 0;
            const percentage = facility.capacity > 0 ? (usedCapacity / facility.capacity) * 100 : 0;

            const marker = this.facilityMarker(facility);
            marker.setLatLng(this.latLngOf(facility.location));
            marker.setOpacity(used ? 1 : 0.35);
            marker.setPopupContent(`<h5>Facility ${escapeHtml(facility.id)}</h5>
<ul class="list-unstyled mb-0">
<li>Usage: ${usedCapacity}/${facility.capacity}</li>
<li>Setup cost: ${LONG_COST_FORMAT.format(facility.setupCost)}</li>
</ul>`);

            const row = $(`<tr class="${used ? 'table-active' : 'text-muted'}"/>`)
                .append($('<td/>').append($('<span class="facility-swatch"/>')
                    .css('background-color', used ? color : 'transparent')
                    .css('border-color', color)))
                .append($('<td/>').text(facility.id))
                .append($('<td class="w-50"/>').append($('<div class="progress"/>')
                    .append($('<div class="progress-bar" role="progressbar"/>')
                        .css('width', `${percentage}%`)
                        .text(`${usedCapacity}/${facility.capacity}`))))
                .append($('<td class="text-end"/>').text(SHORT_COST_FORMAT.format(facility.setupCost)));
            row.on('mouseenter', () => marker.openPopup()).on('mouseleave', () => marker.closePopup());
            facilitiesTable.append(row);
        });
    },

    facilityMarker(facility) {
        let marker = this.facilityMarkerById.get(facility.id);
        if (marker == null) {
            marker = L.marker(this.latLngOf(facility.location)).addTo(this.facilityGroup).bindPopup();
            this.facilityMarkerById.set(facility.id, marker);
        }
        return marker;
    },

    // Consumers move between facilities on every new best solution, so their markers and the lines to their
    // facility are cheaper to redraw wholesale than to diff.
    renderConsumers(plan, facilityById) {
        this.consumerGroup.clearLayers();
        plan.consumers.forEach((consumer) => {
            const facility = consumer.facilityId == null ? null : facilityById.get(consumer.facilityId);
            const color = this.colorOf(plan, consumer.facilityId);
            const consumerLatLng = this.latLngOf(consumer.location);
            L.circleMarker(consumerLatLng, facility == null ? {radius: 4} : {radius: 4, color})
                .addTo(this.consumerGroup);
            if (facility != null) {
                L.polyline([consumerLatLng, this.latLngOf(facility.location)], {color, weight: 2})
                    .addTo(this.consumerGroup);
            }
        });
    },

    renderSummary(plan) {
        const usedFacilities = plan.facilities.filter((facility) => facility.used ?? (facility.usedCapacity ?? 0) > 0);
        const setupCost = usedFacilities.reduce((total, facility) => total + facility.setupCost, 0);
        const potentialCost = plan.facilities.reduce((total, facility) => total + facility.setupCost, 0);
        const costPercentage = potentialCost === 0 ? 0 : Math.round((setupCost * 1000) / potentialCost) / 10;
        const totalDistance = plan.consumers.reduce((total, consumer) => {
            const facility = plan.facilities.find((candidate) => candidate.id === consumer.facilityId);
            return facility == null ? total : total + distanceInMeters(consumer.location, facility.location);
        }, 0);

        $('#usedFacilities').text(`${usedFacilities.length} of ${plan.facilities.length}`);
        $('#setupCost').text(`${LONG_COST_FORMAT.format(setupCost)} (${costPercentage}%)`);
        $('#totalDistance').text(`${Math.round(totalDistance / 1000)} km`);
    },
};

// Mirrors the map service's local (haversine) fallback that backs Location.getDistanceTo(...) in the solver model,
// so the UI shows the very distance the solver minimizes.
const EARTH_RADIUS_IN_METERS = 6371000;

function distanceInMeters(from, to) {
    const toRadians = (degrees) => degrees * Math.PI / 180;
    const latitudeDiff = toRadians(to.latitude - from.latitude);
    const longitudeDiff = toRadians(to.longitude - from.longitude);
    const a = Math.sin(latitudeDiff / 2) ** 2
        + Math.cos(toRadians(from.latitude)) * Math.cos(toRadians(to.latitude)) * Math.sin(longitudeDiff / 2) ** 2;
    return Math.round(2 * EARTH_RADIUS_IN_METERS * Math.asin(Math.sqrt(a)));
}

function escapeHtml(value) {
    return $('<div/>').text(value ?? '').html();
}

app.start();
