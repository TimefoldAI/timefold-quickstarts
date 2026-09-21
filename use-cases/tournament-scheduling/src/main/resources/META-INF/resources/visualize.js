// index.template.html has a single #visualization slot; this fills it with the
// demo's own markup via setVisualizationSlot(), then owns rendering the
// schedule into that markup for the rest of the page's lifetime.

const TIMELINE_OPTIONS = {
    timeAxis: {scale: "day"},
    orientation: {axis: "top"},
    stack: false,
    xss: {disabled: true}, // Items are XSS safe through JQuery
    zoomMin: 1000 * 60 * 60 * 24, // One day in milliseconds
    zoomMax: 3 * 7 * 1000 * 60 * 60 * 24, // Three weeks in milliseconds
    showCurrentTime: false,
};

function isAssigned(match) {
    return match.teamId != null;
}

const app = {
    start() {
        setVisualizationSlot(`
    <div id="byTeamPanel"></div>

    <h2 class="my-4">Unassigned matches</h2>
    <div id="unassignedMatches" class="row row-cols-4 g-3 mb-4"></div>
`);

        this.byTeamGroupData = new vis.DataSet();
        this.byTeamItemData = new vis.DataSet();
        this.byTeamTimeline = new vis.Timeline(document.getElementById("byTeamPanel"), this.byTeamItemData,
            this.byTeamGroupData, TIMELINE_OPTIONS);

        this.quickstartPage = new QuickstartPage({
            modelPath: '/v1/schedules',
            renderSchedule: (schedule) => this.renderSchedule(schedule),
            renderInfo: (schedule) => this.renderInfo(schedule),
            mergeModelOutput: (schedule, modelOutput) => this.mergeModelOutput(schedule, modelOutput),
        });
    },

    // modelOutput only carries the assignments (matches: [{id, teamId}]), not the full problem, so schedule
    // (the QuickstartPage's loadedSchedule) keeps the full modelInput (teams, unavailabilities, match dates)
    // and this only overlays the assigned team.
    mergeModelOutput(schedule, modelOutput) {
        if (schedule == null) {
            return;
        }
        if (modelOutput != null && modelOutput.matches != null) {
            const teamIdByMatchId = new Map(modelOutput.matches.map((match) => [match.id, match.teamId]));
            schedule.matches = schedule.matches.map((match) => teamIdByMatchId.has(match.id)
                ? {...match, teamId: teamIdByMatchId.get(match.id)}
                : match);
        }
    },

    renderInfo(schedule) {
        if (schedule == null) {
            return "";
        }
        const dayCount = new Set(schedule.matches.map((match) => match.date)).size;
        return `${schedule.teams.length} teams · ${schedule.matches.length} matches over ${dayCount} days`;
    },

    // Matches are listed date-first in the input, so the position of a match among the others sharing its
    // date is a stable, one-based "Match N" label - the same numbering the old hand-written UI showed.
    matchNumbersByDate(schedule) {
        const numberByMatchId = new Map();
        const seenPerDate = new Map();
        schedule.matches.forEach((match) => {
            const count = (seenPerDate.get(match.date) ?? 0) + 1;
            seenPerDate.set(match.date, count);
            numberByMatchId.set(match.id, count);
        });
        return numberByMatchId;
    },

    renderSchedule(schedule) {
        resetColorMap();
        const unassignedMatches = $("#unassignedMatches");
        unassignedMatches.children().remove();
        this.byTeamGroupData.clear();
        this.byTeamItemData.clear();
        const matchNumbersByDate = this.matchNumbersByDate(schedule);

        schedule.teams.forEach((team) => this.byTeamGroupData.add({
            id: team.id,
            content: $(`<div class="d-flex flex-column"/>`).append($(`<h5 class="card-title mb-1"/>`).text(team.name)).html(),
        }));

        schedule.unavailabilities.forEach((unavailability) => this.byTeamItemData.add({
            id: `u-${unavailability.teamId}-${unavailability.date}`,
            group: unavailability.teamId,
            content: "",
            start: unavailability.date,
            end: JSJoda.LocalDate.parse(unavailability.date).plusDays(1).toString(),
            style: "background-color: gray; min-height: 50px",
        }));

        schedule.matches.forEach((match) => {
            const matchNumber = matchNumbersByDate.get(match.id);
            if (!isAssigned(match)) {
                const card = $(`<div class="card-body p-2"/>`)
                    .append($(`<h5 class="card-title mb-1"/>`).text(`Match ${matchNumber}`))
                    .append($(`<p class="card-text ms-2 mb-0"/>`).text(match.date));
                unassignedMatches.append($(`<div class="col"/>`).append($(`<div class="card"/>`).append(card)));
                return;
            }
            const color = pickColor(match.teamId);
            const content = $(`<div class="d-flex justify-content-center"/>`)
                .append($(`<h5 class="card-title mb-1"/>`).text(`Match ${matchNumber}`));
            this.byTeamItemData.add({
                id: match.id,
                group: match.teamId,
                content: content.html(),
                start: match.date,
                end: JSJoda.LocalDate.parse(match.date).plusDays(1).toString(),
                style: `background-color: ${color.bg}; color: ${color.fg}; min-height: 50px`,
            });
        });

        if (unassignedMatches.children().length === 0) {
            unassignedMatches.append($(`<div class="col-12"/>`)
                .append($(`<div class="alert alert-success d-flex align-items-center justify-content-center" role="alert"/>`)
                    .append($(`<i class="fas fa-check-circle me-2"/>`))
                    .append($(`<span/>`).text("All matches have been assigned!"))));
        }
    },
};

app.start();
