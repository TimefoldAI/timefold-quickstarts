// index.template.html has a single #visualization slot; this fills it with the
// demo's own markup via setVisualizationSlot(), then owns rendering the
// schedule into that markup for the rest of the page's lifetime.
const app = {
    start() {
        setVisualizationSlot(`
    <div class="mb-2 d-flex justify-content-end">
        <ul class="nav nav-pills" role="tablist">
            <li class="nav-item" role="presentation">
                <button class="nav-link active" id="byCrewTab" data-bs-toggle="tab"
                        data-bs-target="#byCrewPanel" type="button" role="tab" aria-controls="byCrewPanel"
                        aria-selected="true">By crew
                </button>
            </li>
            <li class="nav-item" role="presentation">
                <button class="nav-link" id="byFlightTab" data-bs-toggle="tab"
                        data-bs-target="#byFlightPanel" type="button" role="tab" aria-controls="byFlightPanel"
                        aria-selected="false">By flight
                </button>
            </li>
        </ul>
    </div>
    <div class="tab-content">
        <div class="tab-pane fade show active" id="byCrewPanel" role="tabpanel" aria-labelledby="byCrewTab">
        </div>
        <div class="tab-pane fade" id="byFlightPanel" role="tabpanel" aria-labelledby="byFlightTab">
        </div>
    </div>

    <h2 class="my-4">Unassigned seats</h2>
    <div id="unassignedSeats" class="row row-cols-3 g-3 mb-4"></div>
`);

        const timelineOptions = {
            timeAxis: {scale: "hour", step: 8},
            orientation: {axis: "top"},
            stack: false,
            xss: {disabled: true}, // Items are XSS safe through JQuery
            zoomMin: 8 * 1000 * 60 * 60, // Eight hours in milliseconds
            zoomMax: 2 * 7 * 1000 * 60 * 60 * 24, // Two weeks in milliseconds
        };

        this.byCrewGroupData = new vis.DataSet();
        this.byCrewItemData = new vis.DataSet();
        this.byCrewTimeline = new vis.Timeline(document.getElementById("byCrewPanel"), this.byCrewItemData,
            this.byCrewGroupData, timelineOptions);

        this.byFlightGroupData = new vis.DataSet();
        this.byFlightItemData = new vis.DataSet();
        this.byFlightTimeline = new vis.Timeline(document.getElementById("byFlightPanel"), this.byFlightItemData,
            this.byFlightGroupData, timelineOptions);

        document.getElementById("byCrewTab").addEventListener('click', () => {
            this.byCrewTimeline.redraw();
            this.quickstartPage.changeRenderer((schedule) => this.renderScheduleByCrew(schedule));
        });
        document.getElementById("byFlightTab").addEventListener('click', () => {
            this.byFlightTimeline.redraw();
            this.quickstartPage.changeRenderer((schedule) => this.renderScheduleByFlight(schedule));
        });

        this.quickstartPage = new QuickstartPage({
            modelPath: '/v1/schedules',
            renderSchedule: (schedule) => this.renderScheduleByCrew(schedule),
            renderInfo: (schedule) => this.renderInfo(schedule),
            mergeModelOutput: (schedule, modelOutput) => this.mergeModelOutput(schedule, modelOutput),
        });
    },

    // modelOutput only carries the possible assignments (flightAssignments: [{id, employeeId}]), not the
    // full problem, so schedule (the QuickstartPage's loadedSchedule) keeps the full modelInput
    // (airports + flights + crew) and this only overlays the employeeId per seat.
    mergeModelOutput(schedule, modelOutput) {
        if (schedule == null) {
            return;
        }
        if (modelOutput != null && modelOutput.flightAssignments != null) {
            const employeeIdBySeatId = new Map(modelOutput.flightAssignments
                .map(flightAssignment => [flightAssignment.id, flightAssignment.employeeId]));
            schedule.flightAssignments = schedule.flightAssignments.map(flightAssignment =>
                employeeIdBySeatId.has(flightAssignment.id)
                    ? {...flightAssignment, employeeId: employeeIdBySeatId.get(flightAssignment.id)}
                    : flightAssignment);
        }
    },

    renderInfo(schedule) {
        if (schedule == null) {
            return "";
        }
        return `${schedule.employees.length} crew members · ${schedule.flightAssignments.length} seats`
            + ` · ${schedule.flights.length} flights · ${schedule.airports.length} airports`;
    },

    crewIcon(employee) {
        return employee.skills.indexOf("Pilot") >= 0
            ? '<span class="fas fa-solid fa-plane-departure" title="Pilot"></span>'
            : '<span class="fas fa-solid fa-glass-martini" title="Flight attendant"></span>';
    },

    flightMap(schedule) {
        return new Map(schedule.flights.map(flight => [flight.flightNumber, flight]));
    },

    // The unassigned-seat cards and the "all assigned" banner are identical in both views.
    renderUnassignedSeats(schedule, flights) {
        const unassignedSeats = $("#unassignedSeats");
        unassignedSeats.children().remove();
        $.each(schedule.flightAssignments.filter(seat => seat.employeeId == null), (_, seat) => {
            const flight = flights.get(seat.flightNumber);
            const card = $(`<div class="card-body p-2"/>`)
                .append($(`<h5 class="card-title mb-1"/>`)
                    .text(`${flight.departureAirportCode} → ${flight.arrivalAirportCode}`))
                .append($(`<p class="card-text ms-2 mb-0"/>`).text(`Flight: ${flight.flightNumber}`))
                .append($(`<p class="card-text ms-2 mb-0"/>`).text(`Departure: ${flight.departureUTCDateTime}`))
                .append($(`<p class="card-text ms-2 mb-0"/>`).text(`Arrival: ${flight.arrivalUTCDateTime}`))
                .append($(`<p class="card-text mb-0"/>`)
                    .append($(`<span class="badge rounded-pill text-bg-primary m-1"/>`).text(seat.requiredSkill)));
            unassignedSeats.append($(`<div class="col"/>`)
                .append($(`<div class="card" style="background-color: #EF292999"/>`).append(card)));
        });
        if (unassignedSeats.children().length === 0) {
            const banner = $(`<div class="col-12"/>`)
                .append($(`<div class="alert alert-success d-flex align-items-center justify-content-center" role="alert"/>`)
                    .append($(`<i class="fas fa-check-circle me-2"/>`))
                    .append($(`<span/>`).text("All seats have been assigned!")));
            unassignedSeats.append(banner);
        }
    },

    renderScheduleByCrew(schedule) {
        const flights = this.flightMap(schedule);
        this.byCrewGroupData.clear();
        this.byCrewItemData.clear();

        $.each([...schedule.employees].sort((left, right) => left.name.localeCompare(right.name)), (_, employee) => {
            const name = $('<div/>').text(`${employee.name} (${employee.homeAirportCode})`).html();
            this.byCrewGroupData.add({
                id: employee.id,
                content: `<div class="d-flex flex-column"><div><h5 class="card-title mb-1">${name} ${this.crewIcon(employee)}</h5></div></div>`,
            });
            employee.unavailableDays.forEach((date) => {
                const day = JSJoda.LocalDate.parse(date);
                this.byCrewItemData.add({
                    id: `${employee.id}-unavailable-${date}`,
                    group: employee.id,
                    start: day.atStartOfDay().toString(),
                    end: day.plusDays(1).atStartOfDay().toString(),
                    style: "background-color: gray; min-height: 50px"
                });
            });
        });

        $.each(schedule.flightAssignments.filter(seat => seat.employeeId != null), (_, seat) => {
            const flight = flights.get(seat.flightNumber);
            const content = $("<div class='flight-card' />")
                .append($("<div class='flight-ribbon' />")
                    .text(flight.flightNumber))
                .append($("<div class='d-flex justify-content-center' />")
                    .append($(`<h5 class="card-title mb-1"/>`)
                        .text(`${flight.departureAirportCode} → ${flight.arrivalAirportCode}`)));
            this.byCrewItemData.add({
                id: seat.id,
                group: seat.employeeId,
                content: content.html(),
                start: flight.departureUTCDateTime,
                end: flight.arrivalUTCDateTime,
                style: "min-height: 50px"
            });
        });

        this.renderUnassignedSeats(schedule, flights);
        this.setWindow(this.byCrewTimeline, schedule);
        this.byCrewTimeline.redraw();
    },

    renderScheduleByFlight(schedule) {
        const flights = this.flightMap(schedule);
        const employees = new Map(schedule.employees.map(employee => [employee.id, employee]));
        this.byFlightGroupData.clear();
        this.byFlightItemData.clear();

        $.each(schedule.flights, (_, flight) => {
            const label = $('<div/>')
                .text(`${flight.flightNumber}: ${flight.departureAirportCode} → ${flight.arrivalAirportCode}`).html();
            this.byFlightGroupData.add({
                id: flight.flightNumber,
                content: `<div class="d-flex flex-column"><div><h5 class="card-title mb-1">${label}</h5></div></div>`,
            });
        });

        $.each(schedule.flightAssignments.filter(seat => seat.employeeId != null), (_, seat) => {
            const flight = flights.get(seat.flightNumber);
            const employee = employees.get(seat.employeeId);
            const content = $("<div />")
                .append($("<div class='d-flex justify-content-center' />")
                    .append($(`<h5 class="card-title mb-1"/>`).text(employee.name)));
            this.byFlightItemData.add({
                id: seat.id,
                group: seat.flightNumber,
                content: `${content.html()} ${this.crewIcon(employee)}`,
                start: flight.departureUTCDateTime,
                end: flight.arrivalUTCDateTime,
                style: "min-height: 50px",
                // Seats of the same flight share its start/end, so let vis-timeline stack them.
                subgroup: seat.id
            });
        });

        this.renderUnassignedSeats(schedule, flights);
        this.setWindow(this.byFlightTimeline, schedule);
        this.byFlightTimeline.redraw();
    },

    setWindow(timeline, schedule) {
        if (schedule.flights.length === 0) {
            return;
        }
        const departures = schedule.flights.map(flight => flight.departureUTCDateTime).sort();
        const arrivals = schedule.flights.map(flight => flight.arrivalUTCDateTime).sort();
        timeline.setWindow(departures[0], arrivals[arrivals.length - 1]);
    },
};

app.start();
