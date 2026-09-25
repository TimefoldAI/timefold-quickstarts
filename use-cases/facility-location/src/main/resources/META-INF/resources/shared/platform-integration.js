// ── Timefold Platform integration ──
// Everything this page needs to behave when embedded as an iframe in the Timefold Platform

(function () {
    const q = new URL(window.location.href).searchParams;
    if (!q.has('onPlatform')) return;

    const MESSAGE_SOURCE = "timefold-visualization";

    // How long to wait for the platform's "init" message before giving up.
    const INIT_TIMEOUT_MS = 15000;

    // Origins the platform may embed this ui from.
    const ALLOWED_PARENT_ORIGINS = [];

    function isTrustedOrigin(event) {
        if (event.origin === "null") return false;
        if (event.origin !== window.location.origin && !ALLOWED_PARENT_ORIGINS.includes(event.origin)) {
            return false;
        }
        if (event.source && event.source !== window.parent) return false;
        return true;
    }

    document.body.classList.add('on-platform');
    SETUP.onPlatform = true;
    SETUP.loadInitialData = (page) => page.loadPlatformRun();

    // The origin of the parent window, learned from the trusted "init" message. Used as the
    // targetOrigin of every message we send back, so nothing leaks to an unexpected embedder.
    let parentOrigin = null;

    // Callers (quickstart-page.js) must wait on it before touching SETUP.apiUrl/apiKey/runId.
    SETUP.ready = new Promise((resolve, reject) => {
        const timeoutId = setTimeout(() => {
            window.removeEventListener("message", handleInit);
            reject({
                status: 0,
                statusText: "platform did not send an init message within "
                        + (INIT_TIMEOUT_MS / 1000) + "s"
            });
        }, INIT_TIMEOUT_MS);

        function handleInit(event) {
            if (!isTrustedOrigin(event)) return;
            const msg = event.data;
            if (!msg || msg.source !== MESSAGE_SOURCE || msg.type !== "init") return;

            const {tenantId, runId, apiUrl, apiKey} = msg.data ?? {};
            const ok = Boolean(tenantId && runId && apiUrl && apiKey);
            if (ok) {
                SETUP.tenantId = tenantId;
                SETUP.runId = runId;
                SETUP.apiUrl = apiUrl.replace(/\/+$/, '');
                SETUP.apiKey = apiKey;
            }

            clearTimeout(timeoutId);
            window.removeEventListener("message", handleInit);
            parentOrigin = event.origin;
            window.parent.postMessage({source: MESSAGE_SOURCE, type: "init-response", success: ok}, parentOrigin);
            reportHeight();

            if (ok) {
                resolve();
            } else {
                reject({status: 0, statusText: "platform did not send tenantId/runId/apiUrl/apiKey"});
            }
        }

        window.addEventListener("message", handleInit);
    });

    // ── Iframe resize reporting ──
    let lastHeight = 0;

    function reportHeight() {
        // Before the init handshake the parent's origin is unknown; the height is reported as
        // soon as it completes.
        if (parentOrigin === null) return;
        const height = Math.round(document.body.getBoundingClientRect().height);
        if (Math.abs(height - lastHeight) <= 1) return;
        lastHeight = height;
        window.parent.postMessage({source: MESSAGE_SOURCE, type: "resize", height}, parentOrigin);
    }

    new ResizeObserver(() => requestAnimationFrame(reportHeight)).observe(document.body);
    reportHeight();
})();
