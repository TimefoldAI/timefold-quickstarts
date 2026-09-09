// index.template.html has a single #visualization slot; this fills it with the
// demo's own markup via setVisualizationSlot(), then owns rendering the
// schedule into that markup for the rest of the page's lifetime.

// A round is a matchday, and matchdays are played back to back, so round N is
// rendered as the Nth day from today. Nothing in the model carries a real date.
const SEASON_START = JSJoda.LocalDate.now();
// Rounds shown at once when the timeline first opens; the rest is a scroll away.
const VISIBLE_ROUNDS = 7;

const CLASSIC_MATCH_COLOR = "#009E73";
const REGULAR_MATCH_COLOR = "#0072B2";

function roundDate(roundIndex) {
    return SEASON_START.plusDays(roundIndex).toString();
}

function isAssigned(match) {
    return match.roundIndex != null;
}

function matchColor(match) {
    return match.classicMatch ? CLASSIC_MATCH_COLOR : REGULAR_MATCH_COLOR;
}

// The venue icon says whether this row's team is the one hosting the match; the
// name next to it is always the opponent.
function matchContent(opponentName, atHome) {
    const icon = atHome
        ? `<span class="fas fa-solid fa-home text-white" title="Home match"></span>`
        : `<span class="fas fa-plane-departure text-white" title="Away match"></span>`;
    return $(`<div/>`)
        .append($(`<div class="d-flex justify-content-center align-items-center"/>`)
            .append($(`<h5 class="card-title mb-1"/>`).text(opponentName))
            .append($(`<small class="ms-2 card-text"/>`).append(icon)))
        .html();
}

function renderUnassignedBanner(unassignedMatches) {
    if (unassignedMatches.children().length === 0) {
        const banner = $(`<div class="col-12"/>`)
            .append($(`<div class="alert alert-success d-flex align-items-center justify-content-center" role="alert"/>`)
                .append($(`<i class="fas fa-check-circle me-2"/>`))
                .append($(`<span/>`).text("All matches have been assigned!")));
        unassignedMatches.append(banner);
    }
}

const app = {
    start() {
        setVisualizationSlot(`
    <div id="byTeamPanel"></div>

    <h2 class="my-4">Unassigned matches</h2>
    <div id="unassignedMatches" class="row row-cols-3 g-3 mb-4"></div>
`);

        this.byTeamGroupData = new vis.DataSet();
        this.byTeamItemData = new vis.DataSet();
        this.byTeamTimeline = new vis.Timeline(document.getElementById("byTeamPanel"), this.byTeamItemData,
            this.byTeamGroupData, {
                timeAxis: {scale: "day"},
                orientation: {axis: "top"},
                stack: false,
                xss: {disabled: true}, // Items are XSS safe through JQuery
                zoomMin: 1000 * 60 * 60 * 24, // One day in milliseconds
                showCurrentTime: false,
            });

        this.quickstartPage = new QuickstartPage({
            modelPath: '/v1/schedules',
            renderSchedule: (schedule) => this.renderScheduleByTeam(schedule),
            renderInfo: (schedule) => this.renderInfo(schedule),
            mergeModelOutput: (schedule, modelOutput) => this.mergeModelOutput(schedule, modelOutput),
        });
    },

    // modelOutput only carries the assignments (matches: [{id, roundIndex}]), not the full
    // problem, so schedule (the QuickstartPage's loadedSchedule) keeps the full modelInput
    // (rounds, teams and match details) and this only overlays the round per match.
    mergeModelOutput(schedule, modelOutput) {
        if (schedule == null) {
            return;
        }
        if (modelOutput != null && modelOutput.matches != null) {
            const assignmentByMatchId = new Map(modelOutput.matches.map(match => [match.id, match]));
            schedule.matches = schedule.matches.map(match => {
                const assignment = assignmentByMatchId.get(match.id);
                return assignment == null ? match : {...match, roundIndex: assignment.roundIndex};
            });
        }
    },

    renderInfo(schedule) {
        if (schedule == null) {
            return "";
        }
        // The same three counts LeagueScheduleInputMetrics reports for the dataset.
        return `${schedule.matches.length} matches · ${schedule.rounds.length} rounds `
            + `· ${schedule.teams.length} teams`;
    },

    renderScheduleByTeam(schedule) {
        const teamNameById = new Map(schedule.teams.map(team => [team.id, team.name]));

        this.byTeamGroupData.clear();
        this.byTeamItemData.clear();

        [...schedule.teams]
            .sort((team, otherTeam) => team.name.localeCompare(otherTeam.name))
            .forEach((team) => this.byTeamGroupData.add({
                id: team.id,
                content: $(`<div class="d-flex flex-column"/>`)
                    .append($(`<h5 class="card-title mb-1"/>`).text(team.name)).html(),
            }));

        // Weekend and holiday rounds are when a classic match belongs, so shade them.
        schedule.rounds.filter(round => round.weekendOrHoliday).forEach((round) => {
            this.byTeamItemData.add({
                id: `round-${round.index}`,
                start: roundDate(round.index),
                end: roundDate(round.index + 1),
                type: "background",
                style: "background-color: #8AE23433",
            });
        });

        const unassignedMatches = $("#unassignedMatches");
        unassignedMatches.children().remove();

        schedule.matches.forEach((match) => {
            const homeTeamName = teamNameById.get(match.homeTeamId);
            const awayTeamName = teamNameById.get(match.awayTeamId);
            if (!isAssigned(match)) {
                const matchElement = $(`<div class="card-body p-2"/>`)
                    .css({backgroundColor: matchColor(match), color: "white"})
                    .append($(`<h5 class="card-title mb-1"/>`).text(`${homeTeamName} x ${awayTeamName}`));
                unassignedMatches.append($(`<div class="col"/>`)
                    .append($(`<div class="card"/>`).append(matchElement)));
                return;
            }
            // One match occupies both teams' rows: the home team's and the away team's.
            const start = roundDate(match.roundIndex);
            const end = roundDate(match.roundIndex + 1);
            const style = `background-color: ${matchColor(match)}; color: white`;
            this.byTeamItemData.add({
                id: `${match.id}-home`,
                group: match.homeTeamId,
                content: matchContent(awayTeamName, true),
                title: `${homeTeamName} x ${awayTeamName}`,
                start: start,
                end: end,
                style: style,
            });
            this.byTeamItemData.add({
                id: `${match.id}-away`,
                group: match.awayTeamId,
                content: matchContent(homeTeamName, false),
                title: `${homeTeamName} x ${awayTeamName}`,
                start: start,
                end: end,
                style: style,
            });
        });

        renderUnassignedBanner(unassignedMatches);
        this.byTeamTimeline.setWindow(roundDate(0), roundDate(VISIBLE_ROUNDS));
    },
};

app.start();
