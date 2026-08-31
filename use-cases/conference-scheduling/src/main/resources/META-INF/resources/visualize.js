// index.template.html has a single #visualization slot; this fills it with the
// demo's own markup via setVisualizationSlot(), then owns rendering the
// schedule into that markup for the rest of the page's lifetime.

const timeFormatter = JSJoda.DateTimeFormatter.ofPattern('HH:mm');

// Talk types get a fixed color regardless of encounter order; everything else
// (theme tracks, sectors, audience tags, ...) is picked on first use.
const TALK_TYPE_SEED_COLORS = new Map()
    .set("Blue", {bg: "#0072B2", fg: "#FFFFFF"})
    .set("Green", {bg: "#009E73", fg: "#FFFFFF"})
    .set("Orange", {bg: "#D55E00", fg: "#FFFFFF"});

function compareTimeslots(t1, t2) {
    const OffsetDateTime = JSJoda.OffsetDateTime;
    let diff = OffsetDateTime.parse(t1.startDateTime).compareTo(OffsetDateTime.parse(t2.startDateTime));
    if (diff === 0) {
        diff = OffsetDateTime.parse(t1.endDateTime).compareTo(OffsetDateTime.parse(t2.endDateTime));
    }
    return diff;
}

function isAssigned(talk) {
    return talk.timeslotId != null && talk.roomId != null;
}

function renderUnassignedBanner(unassignedTalks) {
    if (unassignedTalks.children().length === 0) {
        const banner = $(`<div class="col-12"/>`)
            .append($(`<div class="alert alert-success d-flex align-items-center justify-content-center" role="alert"/>`)
                .append($(`<i class="fas fa-check-circle me-2"/>`))
                .append($(`<span/>`).text("All talks have been assigned!")));
        unassignedTalks.append(banner);
    }
}

const app = {
    start() {
        setVisualizationSlot(`
    <div class="mb-2 d-flex justify-content-end">
        <div>
            <ul class="nav nav-pills" role="tablist">
                <li class="nav-item" role="presentation">
                    <button class="nav-link active" id="byRoomTab" data-bs-toggle="tab"
                            data-bs-target="#byRoomPanel" type="button" role="tab" aria-controls="byRoomPanel"
                            aria-selected="true">By room
                    </button>
                </li>
                <li class="nav-item" role="presentation">
                    <button class="nav-link" id="bySpeakerTab" data-bs-toggle="tab" data-bs-target="#byTeacherPanel"
                            type="button" role="tab" aria-controls="byTeacherPanel" aria-selected="false">By Speaker
                    </button>
                </li>
                <li class="nav-item" role="presentation">
                    <button class="nav-link" id="byThemeTrackTab" data-bs-toggle="tab"
                            data-bs-target="#byThemeTrackPanel" type="button" role="tab"
                            aria-controls="byThemeTrackPanel" aria-selected="false">By Theme
                        Tracks
                    </button>
                </li>
                <li class="nav-item" role="presentation">
                    <button class="nav-link" id="bySectorsTab" data-bs-toggle="tab"
                            data-bs-target="#bySectorsPanel" type="button" role="tab"
                            aria-controls="bySectorsPanel" aria-selected="false">By Sectors
                    </button>
                </li>
                <li class="nav-item" role="presentation">
                    <button class="nav-link" id="byAudienceTypeTab" data-bs-toggle="tab"
                            data-bs-target="#byAudienceTypePanel" type="button" role="tab"
                            aria-controls="byAudienceTypePanel" aria-selected="false">By Audience Type
                    </button>
                </li>
                <li class="nav-item" role="presentation">
                    <button class="nav-link" id="byAudienceLevelTab" data-bs-toggle="tab"
                            data-bs-target="#byAudienceLevelPanel" type="button" role="tab"
                            aria-controls="byAudienceLevelPanel" aria-selected="false">By Audience Level
                    </button>
                </li>
            </ul>
        </div>
    </div>
    <div class="tab-content">
        <div class="tab-pane fade show active" id="byRoomPanel" role="tabpanel" aria-labelledby="byRoomTab">
            <table class="table table-borderless table-striped" id="scheduleByRoom">
                <!-- Filled in by visualize.js -->
            </table>
        </div>
        <div class="tab-pane fade" id="byTeacherPanel" role="tabpanel" aria-labelledby="bySpeakerTab">
            <table class="table table-borderless table-striped" id="scheduleBySpeaker">
                <!-- Filled in by visualize.js -->
            </table>
        </div>
        <div class="tab-pane fade" id="byThemeTrackPanel" role="tabpanel" aria-labelledby="byThemeTrackTab">
            <table class="table table-borderless table-striped" id="scheduleByThemeTrack">
                <!-- Filled in by visualize.js -->
            </table>
        </div>
        <div class="tab-pane fade" id="bySectorsPanel" role="tabpanel" aria-labelledby="bySectorsTab">
            <table class="table table-borderless table-striped" id="scheduleBySectors">
                <!-- Filled in by visualize.js -->
            </table>
        </div>
        <div class="tab-pane fade" id="byAudienceTypePanel" role="tabpanel" aria-labelledby="byAudienceTypeTab">
            <table class="table table-borderless table-striped" id="scheduleByAudienceType">
                <!-- Filled in by visualize.js -->
            </table>
        </div>
        <div class="tab-pane fade" id="byAudienceLevelPanel" role="tabpanel" aria-labelledby="byAudienceLevelTab">
            <table class="table table-borderless table-striped" id="scheduleByAudienceLevel">
                <!-- Filled in by visualize.js -->
            </table>
        </div>
    </div>

    <h2 class="my-4">Unassigned talks</h2>
    <div id="unassignedTalks" class="row row-cols-3 g-3 mb-4"></div>
`);

        this.viewType = "R";
        // Lookup maps rebuilt on every render (the DTO flattens references to IDs).
        this.speakerNameById = new Map();
        this.roomById = new Map();

        document.getElementById("byRoomTab").addEventListener('click', () => {
            this.viewType = "R";
            this.renderSchedule(this.quickstartPage.loadedSchedule);
        });
        document.getElementById("bySpeakerTab").addEventListener('click', () => {
            this.viewType = "S";
            this.renderSchedule(this.quickstartPage.loadedSchedule);
        });
        document.getElementById("byThemeTrackTab").addEventListener('click', () => {
            this.viewType = "TH";
            this.renderSchedule(this.quickstartPage.loadedSchedule);
        });
        document.getElementById("bySectorsTab").addEventListener('click', () => {
            this.viewType = "SC";
            this.renderSchedule(this.quickstartPage.loadedSchedule);
        });
        document.getElementById("byAudienceTypeTab").addEventListener('click', () => {
            this.viewType = "AT";
            this.renderSchedule(this.quickstartPage.loadedSchedule);
        });
        document.getElementById("byAudienceLevelTab").addEventListener('click', () => {
            this.viewType = "AL";
            this.renderSchedule(this.quickstartPage.loadedSchedule);
        });

        this.quickstartPage = new QuickstartPage({
            modelPath: '/v1/schedules',
            renderSchedule: (schedule) => this.renderSchedule(schedule),
            renderInfo: (schedule) => this.renderInfo(schedule),
            mergeModelOutput: (schedule, modelOutput) => this.mergeModelOutput(schedule, modelOutput),
        });
    },

    // modelOutput only carries the talk assignments (talks: [{code, timeslotId, roomId}]), not the
    // full problem, so schedule (the QuickstartPage's loadedSchedule) keeps the full modelInput
    // (talk types, timeslots, rooms, speakers, talk details) and this only overlays the
    // timeslotId/roomId per talk code.
    mergeModelOutput(schedule, modelOutput) {
        if (schedule == null) {
            return;
        }
        if (modelOutput != null && modelOutput.talks != null) {
            const assignmentByCode = new Map(modelOutput.talks.map(talk => [talk.code, talk]));
            schedule.talks = schedule.talks.map(talk => assignmentByCode.has(talk.code)
                ? {...talk, timeslotId: assignmentByCode.get(talk.code).timeslotId, roomId: assignmentByCode.get(talk.code).roomId}
                : talk);
        }
    },

    renderSchedule(schedule) {
        if (schedule == null) {
            return;
        }
        this.speakerNameById = new Map((schedule.speakers || []).map(s => [s.id, s.name]));
        this.roomById = new Map((schedule.rooms || []).map(r => [r.id, r]));

        resetColorMap(TALK_TYPE_SEED_COLORS);

        if (this.viewType === "R") {
            this.renderScheduleByRoom(schedule);
        } else if (this.viewType === "S") {
            this.renderScheduleBySpeaker(schedule);
        } else if (this.viewType === "TH") {
            this.renderScheduleByThemeTrack(schedule);
        } else if (this.viewType === "SC") {
            this.renderScheduleBySectors(schedule);
        } else if (this.viewType === "AT") {
            this.renderScheduleByAudienceType(schedule);
        } else if (this.viewType === "AL") {
            this.renderScheduleByAudienceLevel(schedule);
        }
    },

    renderInfo(schedule) {
        if (schedule == null) {
            return "";
        }
        return `${schedule.talks.length} talks · ${schedule.speakers.length} speakers · ${schedule.timeslots.length} timeslots · ${schedule.rooms.length} rooms`;
    },

    talkSpeakerNames(talk) {
        return (talk.speakerIds || []).map(id => this.speakerNameById.get(id) ?? id).join(", ");
    },

    renderScheduleByRoom(schedule) {
        const scheduleByRoom = $("#scheduleByRoom");
        scheduleByRoom.children().remove();

        const unassignedTalks = $("#unassignedTalks");
        unassignedTalks.children().remove();

        const colgroup = $("<colgroup>").appendTo(scheduleByRoom)
        colgroup.append('<col style="width: 250px">');
        $.each(schedule.rooms, item => {
            colgroup.append('<col style="width: 250px">');
        })
        const theadByRoom = $("<thead>").appendTo(scheduleByRoom);
        const headerRowByRoom = $("<tr>").appendTo(theadByRoom);
        headerRowByRoom.append($("<th>Timeslot</th>"));

        $.each(schedule.rooms.sort((a, b) => a.id > b.id ? 1 : (a.id < b.id ? -1 : 0)), (index, room) => {
            headerRowByRoom
                .append($("<th/>")
                    .append($("<span/>").text(room.name))
                    .append($(`<button type="button" class="ms-2 mb-1 btn btn-light btn-sm p-1"/>`)));
        });

        const tbodyByRoom = $("<tbody>").appendTo(scheduleByRoom);

        const OffsetDateTime = JSJoda.OffsetDateTime;

        $.each(schedule.timeslots.sort((a, b) => compareTimeslots(a, b)), (index, timeslot) => {
            const rowByRoom = $("<tr>").appendTo(tbodyByRoom);
            rowByRoom
                .append($(`<th class="align-middle"/>`)
                    .append($("<span/>").text(`
                    ${OffsetDateTime.parse(timeslot.startDateTime).dayOfWeek().name().charAt(0) + OffsetDateTime.parse(timeslot.startDateTime).dayOfWeek().name().slice(1).toLowerCase()}
                    ${OffsetDateTime.parse(timeslot.startDateTime).format(timeFormatter)}
                    -
                    ${OffsetDateTime.parse(timeslot.endDateTime).format(timeFormatter)}
                `)));
            $.each(schedule.rooms, (index, room) => {
                rowByRoom.append($("<td/>").prop("id", `timeslot${timeslot.id}room${room.id}`));
            });
        });

        $.each(schedule.talks.sort((a, b) => a.code > b.code ? 1 : (a.code < b.code ? -1 : 0)), (index, talk) => {
            const color = pickColor(talk.talkTypeName);
            const talkElement = $(`<div class="card" style="background-color: ${color.bg};color:${color.fg}"/>`)
                .append($(`<div class="card-body p-2"/>`)
                    .append($(`<h5 class="card-title mb-1 text-truncate"/>`).text(`${talk.code}: ${talk.title}`))
                    .append($(`<p class="card-text ms-2 mb-1"/>`)
                        .append($(`<em/>`).text(`by ${this.talkSpeakerNames(talk)}`))));
            if (isAssigned(talk)) {
                $(`#timeslot${talk.timeslotId}room${talk.roomId}`).append(talkElement.clone());
            } else {
                unassignedTalks.append($(`<div class="col"/>`).append(talkElement));
            }
        });

        renderUnassignedBanner(unassignedTalks);
    },

    renderScheduleBySpeaker(schedule) {
        const scheduleBySpeaker = $("#scheduleBySpeaker");
        scheduleBySpeaker.children().remove();

        const unassignedTalks = $("#unassignedTalks");
        unassignedTalks.children().remove();

        const colgroup = $("<colgroup>").appendTo(scheduleBySpeaker)
        colgroup.append('<col style="width: 250px">');
        $.each(schedule.timeslots, item => {
            colgroup.append('<col style="width: 250px">');
        })
        const theadBySpeaker = $("<thead>").appendTo(scheduleBySpeaker);
        const headerRowBySpeaker = $("<tr>").appendTo(theadBySpeaker);
        headerRowBySpeaker.append($("<th>Speaker</th>"));

        const OffsetDateTime = JSJoda.OffsetDateTime;

        $.each(schedule.timeslots.sort((a, b) => compareTimeslots(a, b)), (index, timeslot) => {
            headerRowBySpeaker
                .append($("<th/>")
                    .append($("<span/>").text(`
                    ${OffsetDateTime.parse(timeslot.startDateTime).dayOfWeek().name().charAt(0) + OffsetDateTime.parse(timeslot.startDateTime).dayOfWeek().name().slice(1).toLowerCase()}
                    ${OffsetDateTime.parse(timeslot.startDateTime).format(timeFormatter)} - ${OffsetDateTime.parse(timeslot.endDateTime).format(timeFormatter)}`))
                );
        });

        const tbodyBySpeaker = $("<tbody>").appendTo(scheduleBySpeaker);

        $.each(schedule.speakers.sort((a, b) => a.name > b.name ? 1 : (a.name < b.name ? -1 : 0)), (index, speaker) => {
            const rowBySpeaker = $("<tr>").appendTo(tbodyBySpeaker);
            rowBySpeaker
                .append($(`<th class="align-middle"/>`)
                    .append($("<span/>").text(speaker.name)));
            $.each(schedule.timeslots.sort((a, b) => compareTimeslots(a, b)), (index, timeslot) => {
                rowBySpeaker.append($("<td style=\"white-space: normal; word-wrap: break-word; overflow-wrap: break-word;\"/>").prop("id", `speaker${speaker.id}timeslot${timeslot.id}`));
            });
        });

        $.each(schedule.talks.sort((a, b) => a.code > b.code ? 1 : (a.code < b.code ? -1 : 0)), (index, talk) => {
            $.each(talk.speakerIds || [], (_, speakerId) => {
                const talkElement = $(`<div class="card"/>`)
                    .append($(`<div class="card-body p-2"/>`)
                        .append($(`<h5 class="card-title mb-1"/>`).text(`${talk.title}`))
                        .append($(`<p class="card-text ms-2 mb-1"/>`)
                            .append($(`<em/>`).text(`code ${talk.code}`))));
                if (isAssigned(talk)) {
                    $(`#speaker${speakerId}timeslot${talk.timeslotId}`).append(talkElement.clone());
                } else {
                    unassignedTalks.append($(`<div class="col"/>`).append(talkElement));
                }
            });
        });

        renderUnassignedBanner(unassignedTalks);
    },

    renderScheduleByThemeTrack(schedule) {
        const allTalkThemes = schedule.talks.flatMap(t => t.themeTrackTags).sort();
        const themes = [...new Set(allTalkThemes)];
        this.renderScheduleByValues(schedule, "#scheduleByThemeTrack", "Theme Track Tag", "theme", "themeTrackTags", themes);
    },

    renderScheduleBySectors(schedule) {
        const allTalkSectors = schedule.talks.flatMap(t => t.sectorTags).sort();
        const sectors = [...new Set(allTalkSectors)];
        this.renderScheduleByValues(schedule, "#scheduleBySectors", "Sector Tag", "sector", "sectorTags", sectors);
    },

    renderScheduleByAudienceType(schedule) {
        const allAudienceTypes = schedule.talks.flatMap(t => t.audienceTypes).sort();
        const audienceTypes = [...new Set(allAudienceTypes)];
        this.renderScheduleByValues(schedule, "#scheduleByAudienceType", "Audience Type Tag", "audience_type", "audienceTypes", audienceTypes);
    },

    renderScheduleByAudienceLevel(schedule) {
        const allAudienceLevels = schedule.talks.map(t => t.audienceLevel).sort();
        const audienceLevels = [...new Set(allAudienceLevels)];
        this.renderScheduleByValues(schedule, "#scheduleByAudienceLevel", "Audience Level", "audience_level", "audienceLevel", audienceLevels, true);
    },

    renderScheduleByValues(schedule, tableKey, rowTitle, rowKey, key, values, singleValue = false) {
        const scheduleByValue = $(tableKey);
        scheduleByValue.children().remove();

        const unassignedTalks = $("#unassignedTalks");
        unassignedTalks.children().remove();

        const colgroup = $("<colgroup>").appendTo(scheduleByValue)
        colgroup.append('<col style="width: 250px">');
        $.each(schedule.timeslots, item => {
            colgroup.append('<col style="width: 250px">');
        })

        const theadByValue = $("<thead>").appendTo(scheduleByValue);
        const headerRowByValue = $("<tr>").appendTo(theadByValue);
        headerRowByValue.append($(`<th>${rowTitle}</th>`));

        const OffsetDateTime = JSJoda.OffsetDateTime;

        $.each(schedule.timeslots.sort((a, b) => compareTimeslots(a, b)), (index, timeslot) => {
            headerRowByValue
                .append($("<th/>")
                    .append($("<span/>").text(`
                    ${OffsetDateTime.parse(timeslot.startDateTime).dayOfWeek().name().charAt(0) + OffsetDateTime.parse(timeslot.startDateTime).dayOfWeek().name().slice(1).toLowerCase()}
                    ${OffsetDateTime.parse(timeslot.startDateTime).format(timeFormatter)}
                    -
                    ${OffsetDateTime.parse(timeslot.endDateTime).format(timeFormatter)}
                `))
                    .append($(`<button type="button" class="ms-2 mb-1 btn btn-light btn-sm p-1"/>`))
                );
        });

        const tbodyByValue = $("<tbody>").appendTo(scheduleByValue);

        $.each(values, (index, value) => {
            const rowByValue = $("<tr>").appendTo(tbodyByValue);
            rowByValue
                .append($(`<th class="align-middle"/>`)
                    .append($("<span/>").text(value)));
            $.each(schedule.timeslots.sort((a, b) => compareTimeslots(a, b)), (index, timeslot) => {
                rowByValue.append($("<td/>").prop("id", `${rowKey}${value}timeslot${timeslot.id}`));
            });
        });

        $.each(schedule.talks.sort((a, b) => a.code > b.code ? 1 : (a.code < b.code ? -1 : 0)), (index, talk) => {
            const roomName = this.roomById.get(talk.roomId)?.name ?? 'not scheduled';
            if (singleValue) {
                const value = talk[key];
                const color = pickColor(value);
                const talkElement = $(`<div class="card" style="background-color: ${color.bg};color:${color.fg}"/>`)
                    .append($(`<div class="card-body p-2"/>`)
                        .append($(`<h5 class="card-title mb-1 text-truncate"/>`).text(`${talk.code}: ${talk.title}`))
                        .append($(`<p class="card-text ms-2 mb-1"/>`)
                            .append($(`<em/>`).text(`by ${this.talkSpeakerNames(talk)} ${roomName}`))));
                if (isAssigned(talk)) {
                    $(`#${rowKey}${value}timeslot${talk.timeslotId}`).append(talkElement.clone());
                } else {
                    unassignedTalks.append($(`<div class="col"/>`).append(talkElement));
                }
            } else {
                $.each(talk[key], (_, value) => {
                    const color = pickColor(value);
                    const talkElement = $(`<div class="card" style="background-color: ${color.bg};color:${color.fg}"/>`)
                        .append($(`<div class="card-body p-2"/>`)
                            .append($(`<h5 class="card-title mb-1 text-truncate"/>`).text(`${talk.code}: ${talk.title}`))
                            .append($(`<p class="card-text ms-2 mb-1"/>`)
                                .append($(`<em/>`).text(`by ${this.talkSpeakerNames(talk)} at ${roomName}`))));
                    if (isAssigned(talk)) {
                        $(`#${rowKey}${value}timeslot${talk.timeslotId}`).append(talkElement.clone());
                    } else {
                        unassignedTalks.append($(`<div class="col"/>`).append(talkElement));
                    }
                });
            }
        });

        renderUnassignedBanner(unassignedTalks);
    },
};

app.start();
