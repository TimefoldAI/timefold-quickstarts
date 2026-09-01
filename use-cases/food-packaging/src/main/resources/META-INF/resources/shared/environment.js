// ── Environment / SETUP ──
// Resolves how this page reached the browser - standalone (local dev) or
// embedded in the Timefold Platform (an iframe URL carrying query params) -
// into a single SETUP object, and applies whatever page-level effect that
// choice implies (the on-platform body class below). This is the ONE place
// that distinction is decided: quickstart-page.js's QuickstartPage just reads
// SETUP.apiUrl/apiKey and calls SETUP.loadInitialData(this) - it never itself
// branches on "am I on the platform?".
//
// SETUP.loadInitialData is a (page) => ... function, not a method name string,
// so QuickstartPage doesn't need to know QuickstartPage#loadPlatformRun /
// #loadDemoDataList exist as distinct concepts - it just calls what it's given.
const SETUP = (function () {
    const q = new URL(window.location.href).searchParams;
    const onPlatform = q.has('onPlatform');

    if (onPlatform) {
        document.body.classList.add('on-platform');
        return {
            onPlatform: true,
            runId: q.get('runId'),
            apiUrl: q.has('apiUrl') ? decodeURIComponent(q.get('apiUrl')).replace(/\/+$/, '') : null,
            // Not actually parsed from the URL yet - the platform has no way to hand
            // one over today. Kept as an explicit field (rather than left undefined)
            // so SolverClient's apiKey contract stays obvious from here.
            apiKey: null,
            loadInitialData: (page) => page.loadPlatformRun(),
        };
    }

    return {
        onPlatform: false,
        runId: null,
        apiUrl: null,
        apiKey: null,
        loadInitialData: (page) => page.loadDemoDataList(),
    };
})();
