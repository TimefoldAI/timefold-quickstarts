var autoRefreshIntervalId = null;

let demoDataId = null;
let scheduleId = null;
let loadedSchedule = null;

const byCrewPanel = document.getElementById("byCrewPanel");
const byCrewTimelineOptions = {
    timeAxis: {scale: "day"},
    orientation: {axis: "top"},
    stack: false,
    xss: {disabled: true}, // Items are XSS safe through JQuery
    zoomMin: 3 * 1000 * 60 * 60 * 24 // Three day in milliseconds
};
var byCrewGroupData = new vis.DataSet();
var byCrewItemData = new vis.DataSet();
var byCrewTimeline = new vis.Timeline(byCrewPanel, byCrewItemData, byCrewGroupData, byCrewTimelineOptions);

const byJobPanel = document.getElementById("byJobPanel");
const byJobTimelineOptions = {
    timeAxis: {scale: "day"},
    orientation: {axis: "top"},
    xss: {disabled: true}, // Items are XSS safe through JQuery
    zoomMin: 3 * 1000 * 60 * 60 * 24 // Three day in milliseconds
};
var byJobGroupData = new vis.DataSet();
var byJobItemData = new vis.DataSet();
var byJobTimeline = new vis.Timeline(byJobPanel, byJobItemData, byJobGroupData, byJobTimelineOptions);


$(document).ready(function () {
    replaceQuickstartTimefoldAutoHeaderFooter();

    $("#solveButton").click(function () {
        solve();
    });
    $("#stopSolvingButton").click(function () {
        stopSolving();
    });
    $("#analyzeButton").click(function () {
        analyze();
    });
    // HACK to allow vis-timeline to work within Bootstrap tabs
    $("#byCrewTab").on('shown.bs.tab', function (event) {
        byCrewTimeline.redraw();
    })
    $("#byJobTab").on('shown.bs.tab', function (event) {
        byJobTimeline.redraw();
    })

    setupAjax();
    fetchDemoData();
});

function setupAjax() {
    $.ajaxSetup({
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json,text/plain', // plain text is required by solve() returning UUID of the solver job
        }
    });

    // Extend jQuery to support $.put() and $.delete()
    jQuery.each(["put", "delete"], function (i, method) {
        jQuery[method] = function (url, data, callback, type) {
            if (jQuery.isFunction(data)) {
                type = type || callback;
                callback = data;
                data = undefined;
            }
            return jQuery.ajax({
                url: url,
                type: method,
                dataType: type,
                data: data,
                success: callback
            });
        };
    });
}

function fetchDemoData() {
    $.get("/demo-data", function (data) {
        data.forEach(item => {
            $("#testDataButton").append($('<a id="' + item + 'TestData" class="dropdown-item" href="#">' + item + '</a>'));

            $("#" + item + "TestData").click(function () {
                switchDataDropDownItemActive(item);
                scheduleId = null;
                demoDataId = item;

                refreshSchedule();
            });
        });

        // load first data set
        demoDataId = data[0];
        switchDataDropDownItemActive(demoDataId);
        refreshSchedule();
    }).fail(function (xhr, ajaxOptions, thrownError) {
        // disable this page as there is no data
        let $demo = $("#demo");
        $demo.empty();
        $demo.html("<h1><p align=\"center\">No test data available</p></h1>")
    });
}

function switchDataDropDownItemActive(newItem) {
    activeCssClass = "active";
    $("#testDataButton > a." + activeCssClass).removeClass(activeCssClass);
    $("#" + newItem + "TestData").addClass(activeCssClass);
}

function refreshSchedule() {
    let path = "/schedules/" + scheduleId;
    if (scheduleId === null) {
        if (demoDataId === null) {
            alert("Please select a test data set.");
            return;
        }

        path = "/demo-data/" + demoDataId;
    }

    $.getJSON(path, function (schedule) {
        loadedSchedule = schedule;
        renderSchedule(schedule);
    })
        .fail(function (xhr, ajaxOptions, thrownError) {
            showError("Getting the schedule has failed.", xhr);
            refreshSolvingButtons(false);
        });
}

function renderSchedule(schedule) {
    refreshSolvingButtons(schedule.solverStatus != null && schedule.solverStatus !== "NOT_SOLVING");
    $("#score").text("Score: " + (schedule.score == null ? "?" : schedule.score));

    const unassignedJobs = $("#unassignedJobs");
    unassignedJobs.children().remove();
    var unassignedJobsCount = 0;
    byCrewGroupData.clear();
    byJobGroupData.clear();
    byCrewItemData.clear();
    byJobItemData.clear();

    $.each(schedule.crews, (index, crew) => {
        byCrewGroupData.add({id: crew.id, content: crew.name});
    });

    $.each(schedule.jobs, (index, job) => {
        const jobGroupElement = $(`<div/>`)
            .append($(`<h5 class="card-title mb-1"/>`).text(job.name))
            .append($(`<p class="card-text ms-2 mb-0"/>`).text(`${job.durationInDays} workdays`));
        byJobGroupData.add({
            id: job.id,
            content: jobGroupElement.html()
        });
        byJobItemData.add({
            id: job.id + "_readyToIdealEnd", group: job.id,
            start: job.minStartDate, end: job.idealEndDate,
            type: "background",
            style: "background-color: #8AE23433"
        });
        byJobItemData.add({
            id: job.id + "_idealEndToDue", group: job.id,
            start: job.idealEndDate, end: job.maxEndDate,
            type: "background",
            style: "background-color: #FCAF3E33"
        });

        if (job.crew == null || job.startDate == null) {
            unassignedJobsCount++;
            const unassignedJobElement = $(`<div class="card-body p-2"/>`)
                .append($(`<h5 class="card-title mb-1"/>`).text(job.name))
                .append($(`<p class="card-text ms-2 mb-0"/>`).text(`${job.durationInDays} workdays`))
                .append($(`<p class="card-text ms-2 mb-0"/>`).text(`Start: ${job.minStartDate}`))
                .append($(`<p class="card-text ms-2 mb-0"/>`).text(`End: ${job.maxEndDate}`));
            const byJobJobElement = $(`<div/>`)
                .append($(`<h5 class="card-title mb-1"/>`).text(`Unassigned`));
            $.each(job.tags, (index, tag) => {
                const color = pickColor(tag);
                unassignedJobElement.append($(`<span class="badge me-1" style="background-color: ${color}"/>`).text(tag));
                byJobJobElement.append($(`<span class="badge me-1" style="background-color: ${color}"/>`).text(tag));
            });
            unassignedJobs.append($(`<div class="col"/>`).append($(`<div class="card"/>`).append(unassignedJobElement)));
            byJobItemData.add({
                id: job.id,
                group: job.id,
                content: byJobJobElement.html(),
                start: job.minStartDate,
                end: JSJoda.LocalDate.parse(job.minStartDate).plusDays(job.durationInDays).toString(),
                style: "background-color: #EF292999"
            });
        } else {
            const beforeReady = JSJoda.LocalDate.parse(job.startDate).isBefore(JSJoda.LocalDate.parse(job.minStartDate));
            const afterDue = JSJoda.LocalDate.parse(job.endDate).isAfter(JSJoda.LocalDate.parse(job.maxEndDate));
            const byCrewJobElement = $(`<div/>`)
                .append($(`<h5 class="card-title mb-1"/>`).text(job.name))
                .append($(`<p class="card-text ms-2 mb-0"/>`).text(`${job.durationInDays} workdays`));
            const byJobJobElement = $(`<div/>`)
                .append($(`<h5 class="card-title mb-1"/>`).text(job.crew.name));
            if (beforeReady) {
                byCrewJobElement.append($(`<p class="badge badge-danger mb-0"/>`).text(`Before ready (too early)`));
                byJobJobElement.append($(`<p class="badge badge-danger mb-0"/>`).text(`Before ready (too early)`));
            }
            if (afterDue) {
                byCrewJobElement.append($(`<p class="badge badge-danger mb-0"/>`).text(`After due (too late)`));
                byJobJobElement.append($(`<p class="badge badge-danger mb-0"/>`).text(`After due (too late)`));
            }
            $.each(job.tags, (index, tag) => {
                const color = pickColor(tag);
                byCrewJobElement.append($(`<span class="badge me-1" style="background-color: ${color}"/>`).text(tag));
                byJobJobElement.append($(`<span class="badge me-1" style="background-color: ${color}"/>`).text(tag));
            });
            byCrewItemData.add({
                id: job.id, group: job.crew.id,
                content: byCrewJobElement.html(),
                start: job.startDate, end: job.endDate
            });
            byJobItemData.add({
                id: job.id, group: job.id,
                content: byJobJobElement.html(),
                start: job.startDate, end: job.endDate
            });
        }
    });
    if (unassignedJobsCount === 0) {
        unassignedJobs.append($(`<p/>`).text(`There are no unassigned jobs.`));
    }
    byCrewTimeline.setWindow(schedule.workCalendar.fromDate, schedule.workCalendar.toDate);
    byJobTimeline.setWindow(schedule.workCalendar.fromDate, schedule.workCalendar.toDate);
}

function solve() {
    $.post("/schedules", JSON.stringify(loadedSchedule), function (data) {
        scheduleId = data;
        refreshSolvingButtons(true);
    }).fail(function (xhr, ajaxOptions, thrownError) {
            showError("Start solving failed.", xhr);
            refreshSolvingButtons(false);
        },
        "text");
}

function analyze() {
    new bootstrap.Modal("#scoreAnalysisModal").show()
    const scoreAnalysisModalContent = $("#scoreAnalysisModalContent");
    scoreAnalysisModalContent.children().remove();
    if (loadedSchedule.score == null) {
        scoreAnalysisModalContent.text("No score to analyze yet, please first press the 'solve' button.");
    } else {
        $('#scoreAnalysisScoreLabel').text(`(${loadedSchedule.score})`);
        $.put("/schedules/analyze", JSON.stringify(loadedSchedule), function (scoreAnalysis) {
            let constraints = scoreAnalysis.constraints;
            constraints.sort((a, b) => {
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
            });
            constraints.map((e) => {
                let components = getScoreComponents(e.weight);
                e.type = components.hard != 0 ? 'hard' : (components.medium != 0 ? 'medium' : 'soft');
                e.weight = components[e.type];
                let scores = getScoreComponents(e.score);
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
                let icon = constraintAnalysis.type == "hard" && constraintAnalysis.implicitScore < 0 ? '<span class="fas fa-exclamation-triangle" style="color: red"></span>' : '';
                if (!icon) icon = constraintAnalysis.matches.length == 0 ? '<span class="fas fa-check-circle" style="color: green"></span>' : '';

                let row = $(`<tr/>`);
                row.append($(`<td/>`).html(icon))
                    .append($(`<td/>`).text(constraintAnalysis.name).css({textAlign: 'left'}))
                    .append($(`<td/>`).text(constraintAnalysis.type))
                    .append($(`<td/>`).html(`<b>${constraintAnalysis.matches.length}</b>`))
                    .append($(`<td/>`).text(constraintAnalysis.weight))
                    .append($(`<td/>`).text(constraintAnalysis.implicitScore));
                analysisTBody.append(row);
                row.append($(`<td/>`));
            });
            analysisTable.append(analysisTBody);
            scoreAnalysisModalContent.append(analysisTable);
        }).fail(function (xhr, ajaxOptions, thrownError) {
            showError("Analyze failed.", xhr);
        }, "text");
    }
}

function getScoreComponents(score) {
    let components = {hard: 0, medium: 0, soft: 0};
    $.each([...score.matchAll(/(-?[0-9]+)(hard|medium|soft)/g)], (i, parts) => {
        components[parts[2]] = parseInt(parts[1], 10);
    });

    return components;
}

function refreshSolvingButtons(solving) {
    if (solving) {
        $("#solveButton").hide();
        $("#stopSolvingButton").show();
        if (autoRefreshIntervalId == null) {
            autoRefreshIntervalId = setInterval(refreshSchedule, 2000);
        }
    } else {
        $("#solveButton").show();
        $("#stopSolvingButton").hide();
        if (autoRefreshIntervalId != null) {
            clearInterval(autoRefreshIntervalId);
            autoRefreshIntervalId = null;
        }
    }
}

function stopSolving() {
    $.delete("/schedules/" + scheduleId, function () {
        refreshSolvingButtons(false);
        refreshSchedule();
    }).fail(function (xhr, ajaxOptions, thrownError) {
        showError("Stop solving failed.", xhr);
    });
}

function replaceQuickstartTimefoldAutoHeaderFooter() {
    const timefoldHeader = $("header#timefold-auto-header");
    if (timefoldHeader != null) {
        timefoldHeader.addClass("bg-black")
        timefoldHeader.append(
            $(`<div class="container-fluid">
        <nav class="navbar sticky-top navbar-expand-lg navbar-dark shadow mb-3">
          <a class="navbar-brand" href="https://timefold.ai">
            <img src="/webjars/timefold/img/timefold-logo-horizontal-negative.svg" alt="Timefold logo" width="200">
          </a>
          <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
          </button>
          <div class="collapse navbar-collapse" id="navbarNav">
            <ul class="nav nav-pills">
              <li class="nav-item active" id="navUIItem">
                <button class="nav-link active" id="navUI" data-bs-toggle="pill" data-bs-target="#demo" type="button">Demo UI</button>
              </li>
              <li class="nav-item" id="navRestItem">
                <button class="nav-link" id="navRest" data-bs-toggle="pill" data-bs-target="#rest" type="button">Guide</button>
              </li>
              <li class="nav-item" id="navOpenApiItem">
                <button class="nav-link" id="navOpenApi" data-bs-toggle="pill" data-bs-target="#openapi" type="button">REST API</button>
              </li>
            </ul>
          </div>
          <div class="ms-auto">
              <div class="dropdown">
                  <button class="btn btn-secondary dropdown-toggle" type="button" id="dropdownMenuButton" data-bs-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                      Data
                  </button>
                  <div id="testDataButton" class="dropdown-menu" aria-labelledby="dropdownMenuButton"></div>
              </div>
          </div>
        </nav>
      </div>`));
    }

    const timefoldFooter = $("footer#timefold-auto-footer");
    if (timefoldFooter != null) {
        timefoldFooter.append(
            $(`<footer class="bg-black text-white-50">
               <div class="container">
                 <div class="hstack gap-3 p-4">
                   <div class="ms-auto"><a class="text-white" href="https://timefold.ai">Timefold</a></div>
                   <div class="vr"></div>
                   <div><a class="text-white" href="https://timefold.ai/docs">Documentation</a></div>
                   <div class="vr"></div>
                   <div><a class="text-white" href="https://github.com/TimefoldAI/timefold-quickstarts">Code</a></div>
                   <div class="vr"></div>
                   <div class="me-auto"><a class="text-white" href="https://timefold.ai/product/support/">Support</a></div>
                 </div>
               </div>
             </footer>`));
    }
}
