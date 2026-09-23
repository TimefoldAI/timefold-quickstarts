// ── Backend request log ──
// A self-contained <request-log-bar> custom element: a bar pinned to the bottom of the
// window that expands into a log of every REST call this page made to the backend,
// with the request/response bodies - the same "what is the browser actually sending?"
// view the htmx examples put under their demos.
//
// It listens to jQuery's global ajax events rather than wrapping SolverClient, so it
// picks up every call the page makes (quickstart-page.js's polling, a quickstart's own
// visualize.js, ...) without any of them knowing about it. jQuery is the only HTTP
// client in use here; a call made with fetch()/XMLHttpRequest directly would not show up.
//
// Not attached at all on the Timefold Platform: the embedded iframe is a read-only
// visualization, not a demo of the API. Detected from the same ?onPlatform query
// parameter platform-integration.js keys off - body.on-platform is only added later by
// that script, so it is not available yet when this element is upgraded.

// Entries past this are dropped oldest-first: the status polling in quickstart-page.js
// adds one every 2s while solving, so this log is unbounded in practice.
const REQUEST_LOG_MAX_ENTRIES = 200;

// Bodies are shown verbatim up to this; a whole solved schedule is far too big to paste
// into a panel, and the point here is the shape of the call, not the full payload.
const REQUEST_LOG_MAX_BODY_CHARS = 20000;

customElements.define('request-log-bar', class extends HTMLElement {
    connectedCallback() {
        if (new URL(window.location.href).searchParams.has('onPlatform')) return;
        this.entries = new Map();
        this.nextId = 1;
        this.render();
        this.listenToAjax();
    }

    render() {
        this.innerHTML = `
    <div id="requestLog" class="request-log collapsed">
        <div class="request-log-header">
            <button type="button" class="request-log-toggle" aria-expanded="false" aria-controls="requestLogBody">
                <span class="fas fa-chevron-up request-log-chevron"></span>
                <span class="fw-semibold">Backend requests</span>
                <span class="badge bg-secondary" id="requestLogCount">0</span>
                <span class="request-log-latest text-truncate" id="requestLogLatest">No requests yet</span>
            </button>
            <button type="button" class="btn btn-sm btn-outline-light border-0" id="requestLogClear" title="Clear the log">
                <span class="fas fa-trash-can"></span>
            </button>
        </div>
        <div class="request-log-body" id="requestLogBody">
            <div class="request-log-empty" id="requestLogEmpty">
                Every call this page makes to the backend shows up here, newest first.
            </div>
            <div class="request-log-entries" id="requestLogEntries"></div>
        </div>
    </div>`;

        this.panelEl = this.querySelector('#requestLog');
        this.toggleEl = this.querySelector('.request-log-toggle');
        this.countEl = this.querySelector('#requestLogCount');
        this.latestEl = this.querySelector('#requestLogLatest');
        this.entriesEl = this.querySelector('#requestLogEntries');
        this.emptyEl = this.querySelector('#requestLogEmpty');

        this.toggleEl.addEventListener('click', () => this.toggle());
        this.querySelector('#requestLogClear').addEventListener('click', (e) => {
            // Without this the click bubbles to the header and toggles the panel too.
            e.stopPropagation();
            this.clear();
        });

        document.body.classList.add('has-request-log');
    }

    toggle() {
        const collapsed = this.panelEl.classList.toggle('collapsed');
        this.toggleEl.setAttribute('aria-expanded', String(!collapsed));
    }

    clear() {
        this.entries.clear();
        this.entriesEl.replaceChildren();
        this.countEl.textContent = '0';
        this.latestEl.textContent = 'No requests yet';
        this.emptyEl.style.display = '';
    }

    // ── jQuery global ajax hooks ──
    // ajaxSend/ajaxComplete fire for every $.get/$.post/... on the page and hand over the
    // same jqXHR object, which is what ties the two halves of one entry together.
    listenToAjax() {
        $(document).on('ajaxSend', (event, jqXHR, settings) => {
            jqXHR.requestLogId = this.nextId++;
            this.addEntry(jqXHR.requestLogId, settings);
        });
        $(document).on('ajaxComplete', (event, jqXHR, settings) => {
            this.completeEntry(jqXHR.requestLogId, jqXHR);
        });
    }

    addEntry(id, settings) {
        const entry = {
            method: (settings.type || 'GET').toUpperCase(),
            url: settings.url,
            requestBody: typeof settings.data === 'string' ? settings.data : null,
            startedAt: performance.now(),
            time: new Date(),
        };

        const el = this.renderEntry(entry);
        this.entries.set(id, {entry, el});
        this.entriesEl.prepend(el);
        this.emptyEl.style.display = 'none';
        this.countEl.textContent = String(this.entries.size);
        this.latestEl.textContent = `${entry.method} ${shortenRequestLogUrl(entry.url)}`;

        while (this.entries.size > REQUEST_LOG_MAX_ENTRIES) {
            const oldestId = this.entries.keys().next().value;
            this.entries.get(oldestId).el.remove();
            this.entries.delete(oldestId);
        }
    }

    completeEntry(id, jqXHR) {
        const logged = this.entries.get(id);
        // Dropped by the max-entries cap while it was still in flight.
        if (!logged) return;

        const {entry, el} = logged;
        entry.status = jqXHR.status;
        entry.statusText = jqXHR.statusText;
        entry.durationMs = Math.round(performance.now() - entry.startedAt);
        entry.responseBody = jqXHR.responseText ?? null;

        const replacement = this.renderEntry(entry, el.classList.contains('expanded'));
        el.replaceWith(replacement);
        logged.el = replacement;
        this.latestEl.textContent =
            `${entry.method} ${shortenRequestLogUrl(entry.url)} → ${entry.status || 'failed'}`;
    }

    renderEntry(entry, expanded = false) {
        const el = document.createElement('div');
        el.className = 'request-log-entry' + (expanded ? ' expanded' : '');

        const pending = entry.status === undefined;
        // status 0 is jQuery's "the request never got a response" (offline, aborted, CORS).
        const statusClass = pending ? 'bg-secondary'
            : (entry.status >= 200 && entry.status < 400 ? 'bg-success' : 'bg-danger');
        const statusText = pending ? '···' : (entry.status || 'failed');

        el.innerHTML = `
        <button type="button" class="request-log-summary">
            <span class="fas fa-chevron-right request-log-chevron"></span>
            <span class="badge bg-dark request-log-method">${entry.method}</span>
            <span class="request-log-url text-truncate">${escapeRequestLogHtml(entry.url)}</span>
            <span class="badge ${statusClass}">${statusText}</span>
            <span class="request-log-meta">${pending ? '' : entry.durationMs + ' ms'}</span>
            <span class="request-log-meta">${entry.time.toLocaleTimeString()}</span>
        </button>
        <div class="request-log-detail">
            ${renderRequestLogBody('Request body', entry.requestBody)}
            ${renderRequestLogBody('Response body', entry.responseBody)}
        </div>`;

        el.querySelector('.request-log-summary')
            .addEventListener('click', () => el.classList.toggle('expanded'));
        return el;
    }
});

function renderRequestLogBody(title, body) {
    if (body == null || body === '') {
        return `<div class="request-log-section"><h6>${title}</h6><p class="text-muted mb-0">(empty)</p></div>`;
    }
    const truncated = body.length > REQUEST_LOG_MAX_BODY_CHARS;
    const shown = truncated ? body.slice(0, REQUEST_LOG_MAX_BODY_CHARS) : body;
    const note = truncated
        ? `<p class="text-muted small mb-0">Truncated: showing the first ${REQUEST_LOG_MAX_BODY_CHARS} of ${body.length} characters.</p>`
        : '';
    return `<div class="request-log-section">
                <h6>${title}</h6>
                <pre><code>${escapeRequestLogHtml(prettyPrintRequestLogJson(shown))}</code></pre>
                ${note}
            </div>`;
}

function prettyPrintRequestLogJson(body) {
    try {
        return JSON.stringify(JSON.parse(body), null, 2);
    } catch (e) {
        // Not JSON (or truncated mid-document): show it as it came over the wire.
        return body;
    }
}

// Query strings and the platform's absolute apiUrl make the one-line summary unreadable;
// the full url is still shown on the entry itself.
function shortenRequestLogUrl(url) {
    const path = String(url).split('?')[0];
    return path.length > 60 ? '…' + path.slice(-60) : path;
}

function escapeRequestLogHtml(value) {
    return String(value).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}
