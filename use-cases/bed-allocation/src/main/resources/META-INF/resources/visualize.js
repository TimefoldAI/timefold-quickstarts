// index.template.html has a single #visualization slot; this fills it with the
// demo's own markup via setVisualizationSlot(), then owns rendering the
// schedule into that markup for the rest of the page's lifetime.
const app = {
    start() {
        setVisualizationSlot(`
    <div class="mb-2 d-flex justify-content-end">
        <ul class="nav nav-pills" role="tablist">
            <li class="nav-item" role="presentation">
                <button class="nav-link active" id="byRoomTab" data-bs-toggle="tab"
                        data-bs-target="#byRoomPanel" type="button" role="tab" aria-controls="byRoomPanel"
                        aria-selected="true">By room
                </button>
            </li>
        </ul>
    </div>
    <div class="tab-content">
        <div class="tab-pane fade show active" id="byRoomPanel" role="tabpanel" aria-labelledby="byRoomTab">
        </div>
    </div>

    <h2 class="my-4">Unassigned stays</h2>
    <div id="unassignedPatients" class="row row-cols-3 g-3 mb-4"></div>
`);

        this.byRoomGroupData = new vis.DataSet();
        this.byRoomItemData = new vis.DataSet();
        this.byRoomTimeline = new vis.Timeline(document.getElementById("byRoomPanel"), this.byRoomItemData, this.byRoomGroupData, {
            timeAxis: {scale: "day"},
            orientation: {axis: "top"},
            stack: false,
            xss: {disabled: true}, // Items are XSS safe through JQuery
            zoomMin: 3 * 1000 * 60 * 60 * 24 // Three day in milliseconds
        });

        document.getElementById("byRoomTab").addEventListener('click', () => {
            this.byRoomTimeline.redraw();
            this.quickstartPage.changeRenderer((schedule) => this.renderScheduleByRoom(schedule));
        });

        this.quickstartPage = new QuickstartPage({
            modelPath: '/v1/schedules',
            renderSchedule: (schedule) => this.renderScheduleByRoom(schedule),
            renderInfo: (schedule) => this.renderInfo(schedule),
            mergeModelOutput: (schedule, modelOutput) => this.mergeModelOutput(schedule, modelOutput),
        });
    },

    // modelOutput only carries the possible assignments (stays: [{id, bedId}]), not the
    // full problem, so schedule (the QuickstartPage's loadedSchedule) keeps the full
    // modelInput (departments + stay details) and this only overlays the bedId per stay.
    mergeModelOutput(schedule, modelOutput) {
        if (schedule == null) {
            return;
        }
        if (modelOutput != null && modelOutput.stays != null) {
            const bedIdByStayId = new Map(modelOutput.stays.map(stay => [stay.id, stay.bedId]));
            schedule.stays = schedule.stays.map(stay => bedIdByStayId.has(stay.id)
                ? {...stay, bedId: bedIdByStayId.get(stay.id)}
                : stay);
        }
    },

    renderInfo(schedule) {
        if (schedule == null) {
            return "";
        }
        const beds = schedule.departments.flatMap(d => d.rooms).flatMap(r => r.beds);
        return `${schedule.stays.length} stays · ${beds.length} beds · ${schedule.departments.length} departments`;
    },

    renderScheduleByRoom(schedule) {
        const unassignedPatients = $("#unassignedPatients");
        unassignedPatients.children().remove();
        this.byRoomGroupData.clear();
        this.byRoomItemData.clear();

        $.each(schedule.departments.flatMap(d => d.rooms), (_, room) => {
            let content = `<div class="d-flex flex-column"><div><h5 class="card-title mb-1">${room.name}</h5></div>`;
            if (room.equipments.length > 0) {
                let equipments = room.equipments.sort().slice(0, Math.min(2, room.equipments.length));
                content += `<div class="d-flex">`;
                equipments.forEach(e => content += `<div><span class="badge text-bg-success m-1">${e}</span></div>`);
                content += "</div>";
                if (room.equipments.length > 2) {
                    let equipments = room.equipments.sort().slice(2, Math.min(4, room.equipments.length));
                    content += `<div class="d-flex">`;
                    equipments.forEach(e => content += `<div><span class="badge text-bg-success m-1">${e}</span></div>`);
                    content += "</div>";
                }
            }
            content += "</div>";

            const roomData = {
                id: room.id,
                content: content,
                treeLevel: 1,
                nestedLevels: [...room.beds.map(b => b.id)]
            };
            this.byRoomGroupData.add(roomData);
            room.beds.forEach((bed, index) => this.byRoomGroupData.add({
                id: bed.id,
                content: `Bed ${index + 1}`,
                treeLevel: 2
            }));
        });

        $.each(schedule.stays, (_, stay) => {
            const bgcolor = stay.patientGender === 'MALE' ? '#729FCF' : '#FCE94F';
            const color = stay.patientGender === 'MALE' ? 'white' : 'black';

            if (stay.bedId == null) {
                const unassignedPatientElement = $(`<div class="card-body p-2"/>`)
                    .append($(`<h5 class="card-title mb-1"/>`).text(`${stay.patientName} (${stay.patientGender.substring(0, 1)})`))
                    .append($(`<p class="card-text ms-2 mb-0"/>`).text(`${JSJoda.LocalDate.parse(stay.arrivalDate)
                        .until(JSJoda.LocalDate.parse(stay.departureDate), JSJoda.ChronoUnit.DAYS)} day(s)`))
                    .append($(`<p class="card-text ms-2 mb-0"/>`).text(`Arrival: ${stay.arrivalDate}`))
                    .append($(`<p class="card-text ms-2 mb-0"/>`).text(`Departure: ${stay.departureDate}`));

                unassignedPatientElement
                    .append($(`<p class="card-text mb-0"/>`).append($(`<span class="badge rounded-pill text-bg-primary m-1"/>`)
                        .text(stay.specialty)));

                const equipmentDiv = $("<div />").prop("class", "col");
                unassignedPatientElement.append(equipmentDiv);
                stay.patientRequiredEquipments.sort().forEach(e => {
                    equipmentDiv.append($(`<span class="badge text-bg-success m-1"/>`).text(e))
                });
                const preferredEquipmentDiv = $("<div />").prop("class", "col");
                unassignedPatientElement.append(preferredEquipmentDiv);
                if (stay.patientPreferredEquipments && stay.patientPreferredEquipments.length > 0) {
                    stay.patientPreferredEquipments
                        .filter(e => stay.patientRequiredEquipments.indexOf(e) == -1)
                        .sort()
                        .forEach(e => preferredEquipmentDiv.append($(`<span class="badge text-bg-secondary m-1"/>`).text(e)));
                }
                unassignedPatientElement.append($("<div />").prop("class", "d-flex justify-content-end").append($(`<small class="ms-2 mt-1 card-text text-muted"/>`)
                    .text(stay.patientPreferredMaximumRoomCapacity)));

                unassignedPatients.append($(`<div class="col"/>`).append($(`<div class="card" style="background-color: ${bgcolor};color:${color}"/>`).append(unassignedPatientElement)));
                this.byRoomItemData.add({
                    id: stay.id,
                    group: stay.id,
                    start: stay.arrivalDate,
                    end: stay.departureDate,
                    style: "background-color: #EF292999"
                });
            } else {
                const byPatientElement = $(`<div />`)
                    .append($(`<h5 class="card-title mb-1"/>`).text(`${stay.patientName} (${stay.patientGender.substring(0, 1)})`));

                byPatientElement
                    .append($(`<p class="card-text mb-0"/>`).append($(`<span class="badge rounded-pill text-bg-primary m-1"/>`)
                        .text(stay.specialty)));

                const equipmentDiv = $("<div />").prop("class", "col");
                byPatientElement.append(equipmentDiv);
                stay.patientRequiredEquipments.sort().forEach(e => {
                    equipmentDiv.append($(`<span class="badge text-bg-success m-1"/>`).text(e))
                });
                const preferredEquipmentDiv = $("<div />").prop("class", "col");
                byPatientElement.append(preferredEquipmentDiv);
                if (stay.patientPreferredEquipments && stay.patientPreferredEquipments.length > 0) {
                    stay.patientPreferredEquipments
                        .filter(e => stay.patientRequiredEquipments.indexOf(e) == -1)
                        .sort()
                        .forEach(e => preferredEquipmentDiv.append($(`<span class="badge text-bg-secondary m-1"/>`).text(e)));
                }
                byPatientElement.append($("<div />").prop("class", "d-flex justify-content-end").append($(`<small class="ms-2 mt-1 card-text text-muted"/>`)
                    .text(stay.patientPreferredMaximumRoomCapacity)));

                this.byRoomItemData.add({
                    id: stay.id,
                    group: stay.bedId,
                    content: byPatientElement.html(),
                    start: stay.arrivalDate,
                    end: stay.departureDate,
                    style: `background-color: ${bgcolor}; color: ${color}`
                });
            }
        });
        // Show banner if no unassigned items
        if (unassignedPatients.children().length === 0) {
            const banner = $(`<div class="col-12"/>`)
                .append($(`<div class="alert alert-success d-flex align-items-center justify-content-center" role="alert"/>`)
                    .append($(`<i class="fas fa-check-circle me-2"/>`))
                    .append($(`<span/>`).text("All stays have been assigned!")));
            unassignedPatients.append(banner);
        }

        const arrivalDates = schedule.stays.map(s => s.arrivalDate);
        const departureDates = schedule.stays.map(s => s.departureDate);
        const allDates = [...new Set([...arrivalDates, ...departureDates])]
            .sort((a, b) => JSJoda.LocalDate.parse(a).compareTo(JSJoda.LocalDate.parse(b)));
        this.byRoomTimeline.setWindow(allDates[0], allDates[allDates.length - 1]);
    },
};

app.start();
