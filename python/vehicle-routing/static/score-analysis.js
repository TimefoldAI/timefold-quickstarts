function analyzeScore(solution, endpointPath) {
    new bootstrap.Modal("#scoreAnalysisModal").show()
    const scoreAnalysisModalContent = $("#scoreAnalysisModalContent");
    scoreAnalysisModalContent.children().remove();
    scoreAnalysisModalContent.text("");

    if (solution.score == null) {
        scoreAnalysisModalContent.text("Score not ready for analysis, try to run the solver first or wait until it advances.");
    } else {
        visualizeScoreAnalysis(scoreAnalysisModalContent, solution, endpointPath)
    }
}

function visualizeScoreAnalysis(scoreAnalysisModalContent, solution, endpointPath) {
    $('#scoreAnalysisScoreLabel').text(`(${solution.score})`);
    $.put(endpointPath, JSON.stringify(solution), function (scoreAnalysis) {
        let constraints = scoreAnalysis.constraints;
        constraints.sort(compareConstraintsBySeverity);
        constraints.map(addDerivedScoreAttributes);
        scoreAnalysis.constraints = constraints;

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
        $.each(scoreAnalysis.constraints, function (index, constraintAnalysis) {
            visualizeConstraintAnalysis(analysisTBody, index, constraintAnalysis)
        });
        analysisTable.append(analysisTBody);
        scoreAnalysisModalContent.append(analysisTable);
    }).fail(function (xhr, ajaxOptions, thrownError) {
            showError("Score analysis failed.", xhr);
        },
        "text");
}

function compareConstraintsBySeverity(a, b) {
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
}

function addDerivedScoreAttributes(constraint) {
    let components = getScoreComponents(constraint.weight);
    constraint.type = components.hard != 0 ? 'hard' : (components.medium != 0 ? 'medium' : 'soft');
    constraint.weight = components[constraint.type];
    let scores = getScoreComponents(constraint.score);
    constraint.implicitScore = scores.hard != 0 ? scores.hard : (scores.medium != 0 ? scores.medium : scores.soft);
}

function getScoreComponents(score) {
    let components = {hard: 0, medium: 0, soft: 0};

    $.each([...score.matchAll(/(-?[0-9]+)(hard|medium|soft)/g)], function (i, parts) {
        components[parts[2]] = parseInt(parts[1], 10);
    });

    return components;
}

function visualizeConstraintAnalysis(analysisTBody, constraintIndex, constraintAnalysis, recommendation = false, recommendationIndex = null) {
    let icon = constraintAnalysis.type == "hard" && constraintAnalysis.implicitScore < 0 ? '<span class="fas fa-exclamation-triangle" style="color: red"></span>' : '';
    if (!icon) icon = constraintAnalysis.weight < 0 && constraintAnalysis.matches.length == 0 ? '<span class="fas fa-check-circle" style="color: green"></span>' : '';

    let row = $(`<tr/>`);
    row.append($(`<td/>`).html(icon))
        .append($(`<td/>`).text(constraintAnalysis.name).css({textAlign: 'left'}))
        .append($(`<td/>`).text(constraintAnalysis.type))
        .append($(`<td/>`).html(`<b>${constraintAnalysis.matches.length}</b>`))
        .append($(`<td/>`).text(constraintAnalysis.weight))
        .append($(`<td/>`).text(recommendation ? constraintAnalysis.score : constraintAnalysis.implicitScore));

    analysisTBody.append(row);
    row.append($(`<td/>`));
}