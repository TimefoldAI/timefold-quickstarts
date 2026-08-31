// ── Visualization slot ──
// index.html has a single #visualization slot; a quickstart's visualize.js
// fills it with its own demo-specific markup (tabs, tables, ...) via this, function.
function setVisualizationSlot(html) {
    document.getElementById('visualization').innerHTML = html;
}

// ── Quickstart page controller ──

class QuickstartPage {
    constructor({modelPath, renderSchedule, renderInfo, mergeModelOutput}) {
        if (!renderSchedule) {
            throw new Error('QuickstartPage requires a renderSchedule(schedule) callback.');
        }
        if (!renderInfo) {
            throw new Error('QuickstartPage requires a renderInfo(schedule) => string callback.');
        }
        if (!mergeModelOutput) {
            throw new Error('QuickstartPage requires a mergeModelOutput(schedule, modelOutput) callback.');
        }
        this.client = new SolverClient({
            modelPath,
            apiUrl: SETUP.apiUrl,
            apiKey: SETUP.apiKey,
        });
        this.renderSchedule = renderSchedule;
        this.renderInfo = renderInfo;
        this.mergeModelOutput = mergeModelOutput;

        this.autoRefreshIntervalId = null;
        this.jobId = null;
        this.loadedSchedule = null;
        // Set when stop is pressed before the POST that creates the run has returned a jobId.
        this.stopRequested = false;
        // The demo dataset id currently selected in the Data dropdown; set once the list
        this.demoDataId = null;

        this.bindControls();
        SETUP.loadInitialData(this);
    }

    // The header controls (index.template.html) are parsed into the page before this
    // script tag runs, so no need to wait for a document-ready event here.
    bindControls() {
        $('#solveButton').on('click', () => this.solve());
        $('#stopSolvingButton').on('click', () => this.stopSolving());
        $('#analyzeButton').on('click', () => this.analyze());

        const solutionSummaryPanel = document.getElementById('solutionSummaryPanel');
        if (solutionSummaryPanel) {
            $('#toggleSummaryButton').show().on('click', function () {
                $(this).toggleClass('collapsed');
                $(solutionSummaryPanel).toggleClass('hide');
            });
        }
    }

    renderScore(metadata) {
        if (metadata != null) {
            this.loadedSchedule.score = metadata.score;
        }
        const score = this.loadedSchedule?.score;
        $("#score").text("Score: " + (score == null || score === "" ? "?" : score));
    }

    // When not running on the platform, this function can be deleted.
    loadPlatformRun() {
        if (!SETUP.runId) {
            this.showError("No runId provided by platform.", {status: 0, statusText: "missing runId"});
            return;
        }
        this.jobId = SETUP.runId;
        this.client.fetchModelRequest(this.jobId, (req) => {
            this.loadedSchedule = req.modelInput || req;
            this.renderScore();
            this.renderSchedule(this.loadedSchedule);
            $("#info").text(this.renderInfo(this.loadedSchedule));
            this.getStatus();
            if (this.autoRefreshIntervalId == null) {
                this.autoRefreshIntervalId = setInterval(() => this.getStatus(), 2000);
            }
        }, (xhr) => {
            this.showError("Failed to load run input from platform.", xhr);
        });
    }

    loadDemoDataList() {
        this.client.fetchDemoDataList((demoDataList) => {
            const dropdown = $("#testDataButton");
            dropdown.empty();
            demoDataList.forEach((demoData) => {
                $(`<a class="dropdown-item" href="#"></a>`)
                    .attr("id", demoData.id + "TestData")
                    .text(demoData.shortDescription || demoData.id)
                    .click(() => this.selectDemoData(demoData.id))
                    .appendTo(dropdown);
            });
            // A single dataset leaves nothing to choose between, so the picker is just noise.
            $("#dataDropdown, #dataDropdownDivider").toggle(demoDataList.length > 1);
            if (demoDataList.length > 0) {
                this.selectDemoData(demoDataList[0].id);
            }
        }, (xhr) => {
            this.showError("Getting the list of demo datasets has failed.", xhr);
            this.refreshSolvingButtons("SOLVING_COMPLETED");
        });
    }

    selectDemoData(id) {
        this.demoDataId = id;
        $("#testDataButton > a.active").removeClass("active");
        $("#" + id + "TestData").addClass("active");
        // Switching datasets abandons whatever run was loaded/in flight for the
        // previous one; getStatus() then re-fetches the newly selected demo data.
        this.jobId = null;
        this.refreshSolvingButtons("SOLVING_COMPLETED");
        this.getStatus();
    }

    getStatus() {
        if (this.jobId == null) {
            this.client.fetchDemoData(this.demoDataId, (data) => {
                this.loadedSchedule = data.modelInput;
                this.renderScore();
                this.renderSchedule(this.loadedSchedule);
                $("#info").text(this.renderInfo(this.loadedSchedule));
            }, (xhr) => {
                this.showError("Getting the demo data has failed.", xhr);
                this.refreshSolvingButtons("SOLVING_COMPLETED");
            });
        } else {
            this.client.fetchStatus(this.jobId, (data) => {
                this.mergeModelOutput(this.loadedSchedule, data.modelOutput);
                this.renderScore(data.metadata);
                this.renderSchedule(this.loadedSchedule);
                $("#info").text(this.renderInfo(this.loadedSchedule));
                this.refreshSolvingButtons(data.metadata.solverStatus);
            }, (xhr) => {
                this.showError("Getting the schedule has failed.", xhr);
                this.refreshSolvingButtons("SOLVING_COMPLETED");
            });
        }
    }

    solve() {
        // Swap in the stop button right away, rather than waiting for the demo-data GET
        // and the POST to return.
        this.stopRequested = false;
        this.showStopSolvingButton(true);
        this.client.fetchDemoData(this.demoDataId, (modelRequest) => {
            this.client.createRun(modelRequest, (metadata) => {
                this.jobId = metadata.id;
                if (this.stopRequested) {
                    // Stop was pressed while the run was still starting up: honour it now
                    // that there is a jobId to stop.
                    this.stopRequested = false;
                    this.stopSolving();
                    return;
                }
                this.refreshSolvingButtons(metadata.solverStatus || "SOLVING_ACTIVE");
                if (this.autoRefreshIntervalId == null) {
                    this.autoRefreshIntervalId = setInterval(() => this.getStatus(), 2000);
                }
            }, (xhr) => {
                this.showError("Start solving failed.", xhr);
                this.refreshSolvingButtons("SOLVING_COMPLETED");
            });
        }, (xhr) => {
            this.showError("Start solving failed.", xhr);
            this.refreshSolvingButtons("SOLVING_COMPLETED");
        });
    }

    getScoreAnalysis(onSuccess, onFailure) {
        return this.client.fetchScoreAnalysis(this.jobId, onSuccess, onFailure);
    }

    stopSolving() {
        if (this.jobId == null) {
            // The run is still being created, so there is nothing to DELETE yet. Keep the
            // stop button showing and let solve() stop the run as soon as it has an id.
            this.stopRequested = true;
            return;
        }
        this.client.deleteRun(this.jobId, () => {
            this.refreshSolvingButtons("SOLVING_COMPLETED");
            this.getStatus();
        }, (xhr) => {
            this.showError("Stop solving failed.", xhr);
        });
    }

    showStopSolvingButton(solving) {
        if (solving) {
            $("#solveButton").hide();
            $("#stopSolvingButton").show();
        } else {
            $("#solveButton").show();
            $("#stopSolvingButton").hide();
        }
    }

    refreshSolvingButtons(solverStatus) {
        const solving = isSolving(solverStatus);
        this.showStopSolvingButton(solving);
        if (!solving && this.autoRefreshIntervalId != null) {
            clearInterval(this.autoRefreshIntervalId);
            this.autoRefreshIntervalId = null;
        }
    }

    // ── Score analysis modal ──
    analyze() {
        new bootstrap.Modal("#scoreAnalysisModal").show()
        const scoreAnalysisModalContent = $("#scoreAnalysisModalContent");
        scoreAnalysisModalContent.children().remove();
        if (this.jobId == null || this.loadedSchedule == null || this.loadedSchedule.score == null || this.loadedSchedule.score === "") {
            scoreAnalysisModalContent.text("No score to analyze yet, please first press the 'solve' button.");
            return;
        }
        $('#scoreAnalysisScoreLabel').text(`(${this.loadedSchedule.score})`);
        this.getScoreAnalysis((scoreAnalysis) => {
            let constraints = scoreAnalysis.constraints;
            constraints.sort((a, b) => {
                let aComponents = this.getScoreComponents(a.score), bComponents = this.getScoreComponents(b.score);
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
            });
            constraints.map((e) => {
                let components = this.getScoreComponents(e.weight);
                e.type = components.hard != 0 ? 'hard' : (components.medium != 0 ? 'medium' : 'soft');
                e.weight = components[e.type];
                let scores = this.getScoreComponents(e.score);
                e.implicitScore = scores.hard != 0 ? scores.hard : (scores.medium != 0 ? scores.medium : scores.soft);
            });
            scoreAnalysis.constraints = constraints;

            scoreAnalysisModalContent.children().remove();
            scoreAnalysisModalContent.text("");

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
            $.each(scoreAnalysis.constraints, (index, constraintAnalysis) => {
                const matches = constraintAnalysis.matches ?? [];
                const matchCount = constraintAnalysis.matchCount ?? matches.length;
                let icon = constraintAnalysis.type == "hard" && constraintAnalysis.implicitScore < 0 ? '<span class="fas fa-exclamation-triangle" style="color: red"></span>' : '';
                if (!icon) icon = matchCount == 0 ? '<span class="fas fa-check-circle" style="color: green"></span>' : '';

                let row = $(`<tr/>`);
                row.append($(`<td/>`).html(icon))
                    .append($(`<td/>`).text(constraintAnalysis.name).css({textAlign: 'left'}))
                    .append($(`<td/>`).text(constraintAnalysis.type))
                    .append($(`<td/>`).html(`<b>${matchCount}</b>`))
                    .append($(`<td/>`).text(constraintAnalysis.weight))
                    .append($(`<td/>`).text(constraintAnalysis.implicitScore));

                analysisTBody.append(row);

                if (matches.length > 0) {
                    let matchesRow = $(`<tr/>`).addClass("collapse").attr("id", "row" + index + "Collapse");
                    let matchesListGroup = $(`<ul/>`).addClass('list-group').addClass('list-group-flush').css({textAlign: 'left'});

                    $.each(matches, (_, match) => {
                        matchesListGroup.append($(`<li/>`).addClass('list-group-item').addClass('list-group-item-light')
                            .text(match.justification?.description ?? match.score));
                    });

                    matchesRow.append($(`<td/>`));
                    matchesRow.append($(`<td/>`).attr('colspan', '6').append(matchesListGroup));
                    analysisTBody.append(matchesRow);

                    row.append($(`<td/>`).append($(`<a/>`)
                        .attr('href', "#row" + index + "Collapse")
                        .append($(`<span/>`).addClass('fas').addClass('fa-chevron-down'))
                        .click(e => {
                            e.preventDefault();
                            const collapseEl = matchesRow.get(0);
                            bootstrap.Collapse.getOrCreateInstance(collapseEl).toggle();
                            let icon = $(e.currentTarget).find('span.fas');
                            if (icon.hasClass('fa-chevron-down')) {
                                icon.removeClass('fa-chevron-down').addClass('fa-chevron-up');
                            } else {
                                icon.removeClass('fa-chevron-up').addClass('fa-chevron-down');
                            }
                        })));
                } else {
                    row.append($(`<td/>`));
                }

            });
            analysisTable.append(analysisTBody);
            scoreAnalysisModalContent.append(analysisTable);
        }, (xhr, ajaxOptions, thrownError) => {
            scoreAnalysisModalContent.children().remove();
            scoreAnalysisModalContent.append($("<p/>").html(
                "The server returned an error."
                + " This may be due to a misconfiguration, or because Score Analysis requires"
                + " <b>Timefold Solver Enterprise Edition</b>, which is not on the classpath."
                + " If the latter, reach out to Timefold, obtain your license,"
                + " and then run the quickstart with an Enterprise profile to see Score analysis in action."));
        });
    }

    getScoreComponents(score) {
        let components = {hard: 0, medium: 0, soft: 0};

        $.each([...score.matchAll(/(-?[0-9]+)(hard|medium|soft)/g)], (i, parts) => {
            components[parts[2]] = parseInt(parts[1], 10);
        });

        return components;
    }

    // ── Error toast ──
    // Needs a #notificationPanel in the host page's markup (see index.html).
    showError(title, xhr) {
        let serverErrorMessage = !xhr.responseJSON ? `${xhr.status}: ${xhr.statusText}` : xhr.responseJSON.message;
        let serverErrorCode = !xhr.responseJSON ? `unknown` : xhr.responseJSON.code;
        let serverErrorId = !xhr.responseJSON ? `----` : xhr.responseJSON.id;
        let serverErrorDetails = !xhr.responseJSON ? `no details provided` : xhr.responseJSON.details;

        if (xhr.responseJSON && !serverErrorMessage) {
            serverErrorMessage = JSON.stringify(xhr.responseJSON);
            serverErrorCode = xhr.statusText + '(' + xhr.status + ')';
            serverErrorId = `----`;
        }

        console.error(title + "\n" + serverErrorMessage + " : " + serverErrorDetails);
        const notification = $(`<div class="toast" role="alert" aria-live="assertive" aria-atomic="true" style="min-width: 50rem"/>`)
            .append($(`<div class="toast-header bg-danger">
                     <strong class="me-auto text-dark">Error</strong>
                     <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
                   </div>`))
            .append($(`<div class="toast-body"/>`)
                .append($(`<p/>`).text(title))
                .append($(`<pre/>`)
                    .append($(`<code/>`).text(serverErrorMessage + "\n\nCode: " + serverErrorCode + "\nError id: " + serverErrorId))
                )
            );
        $("#notificationPanel").append(notification);
        notification.toast({delay: 30000});
        notification.toast('show');
    }
}
