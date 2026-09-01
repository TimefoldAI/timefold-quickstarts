// ── Solver REST client ──
// A thin, DOM-free wrapper around the Service-module's REST API for one
// quickstart. Every method takes the data it needs plus (onSuccess, onFailure)
// callbacks and does nothing else - no DOM reads/writes, no showError(), no
// solving-lifecycle state (jobId, loadedSchedule, ...), and - deliberately -
// no dependency on the global SETUP object (environment.js): it only knows the
// plain apiUrl/apiKey it was constructed with. See quickstart-page.js's
// QuickstartPage for the class that resolves those from SETUP and wires these
// calls into the page.
//
//   const client = new SolverClient({
//       modelPath: '/v1/schedules',   // this quickstart's REST resource base path
//       apiUrl,                       // optional: prefixes every request (the embedding platform's API base)
//       apiKey,                       // optional: sent as the X-API-KEY header on every request
//   });
//
// The constructor also runs the jQuery ajax setup this needs (default headers,
// $.put()/$.delete() shims) - there's nothing left for a caller to opt out of by
// not calling it, so it isn't a separate step.
//
// DEMO_DATA_PATH_SUFFIX is not part of that constructor contract: every
// Service-module quickstart exposes its demo datasets at .../v1/demo-data
// (derived from `model.api.version=v1` in application.properties, which every
// quickstart sets the same way today), so it's fixed here rather than passed in.
// If a quickstart ever needs a different model.api.version, this constant -
// and the {{SCRIPTS_EXTRA}}-loaded feature scripts that assume it too - would
// need to become configurable again.
const DEMO_DATA_PATH_SUFFIX = '/v1/demo-data';

class SolverClient {
    constructor({modelPath, apiUrl, apiKey}) {
        if (!modelPath) {
            throw new Error('SolverClient requires a modelPath, e.g. "/v1/schedules".');
        }
        this.apiKey = apiKey || null;
        const prefix = apiUrl || '';
        this.modelPath = prefix + modelPath;
        this.demoDataPath = prefix + DEMO_DATA_PATH_SUFFIX;

        this.setupAjax();
    }

    // One-time jQuery setup
    setupAjax() {
        $.ajaxSetup({
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json',
                ...(this.apiKey ? {'X-API-KEY': this.apiKey} : {})
            }
        });

        jQuery.each(["put", "delete"], function (i, method) {
            jQuery[method] = function (url, data, callback, type) {
                if (typeof data === "function") {
                    type = type || callback;
                    callback = data;
                    data = undefined;
                }
                return jQuery.ajax({
                    url: url, type: method, dataType: type, data: data, success: callback
                });
            };
        });
    }

    // ── ModelRest plumbing ──
    fetchDemoDataList(onSuccess, onFailure) {
        return $.get(this.demoDataPath, onSuccess).fail(onFailure);
    }

    fetchDemoData(demoDataId, onSuccess, onFailure) {
        return $.get(`${this.demoDataPath}/${demoDataId}`, onSuccess).fail(onFailure);
    }

    fetchModelRequest(jobId, onSuccess, onFailure) {
        return $.get(`${this.modelPath}/${jobId}/model-request`, onSuccess).fail(onFailure);
    }

    fetchStatus(jobId, onSuccess, onFailure) {
        return $.get(`${this.modelPath}/${jobId}`, onSuccess).fail(onFailure);
    }

    createRun(modelRequest, onSuccess, onFailure) {
        return $.post(this.modelPath, JSON.stringify(modelRequest), onSuccess).fail(onFailure);
    }

    deleteRun(jobId, onSuccess, onFailure) {
        return $.delete(`${this.modelPath}/${jobId}`, onSuccess).fail(onFailure);
    }

    fetchScoreAnalysis(jobId, onSuccess, onFailure) {
        return $.get(`${this.modelPath}/${jobId}/score-analysis?includeJustifications=true`, onSuccess)
            .fail(onFailure);
    }
}

// SolvingStatus values that mean the run is over and nothing is in flight.
const TERMINAL_SOLVER_STATUSES = [
    "DATASET_INVALID", "SOLVING_COMPLETED", "SOLVING_INCOMPLETE", "SOLVING_FAILED"];

function isSolving(solverStatus) {
    return solverStatus != null && !TERMINAL_SOLVER_STATUSES.includes(solverStatus);
}
