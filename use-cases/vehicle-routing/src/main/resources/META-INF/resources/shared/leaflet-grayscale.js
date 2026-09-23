// ── Grayscale map control ──
// A native Leaflet control (a leaflet-bar button, like the zoom control) that desaturates
// the tile layer, so the colored routes and markers drawn on top of the map stay readable
// (see #605). Part of the "leaflet" feature in visualizations/sync.sh, so it is loaded
// after Leaflet itself and only by quickstarts that use a map.
//
// The button is a label around a real checkbox rather than a plain <a>: the graying out
// itself is done in CSS off #grayscaleMapToggle:checked (shared/leaflet-grayscale.css),
// which leaves this with no state of its own to keep in sync, and it keeps the control
// keyboard-operable the way a checkbox already is.
//
// Add it to a map with L.control.grayscale().addTo(map) - see vehicle-routing's
// visualize.js for the call.
L.Control.Grayscale = L.Control.extend({
    options: {
        position: 'topleft',
        // Maps are grayscale by default: that is the state in which the routes drawn on
        // top of them read best, which is the reason this control exists.
        grayscale: true,
    },

    onAdd() {
        const container = L.DomUtil.create('div', 'leaflet-bar leaflet-control leaflet-control-grayscale');
        container.innerHTML = `
            <label title="Grayscale map">
                <input type="checkbox" id="grayscaleMapToggle"${this.options.grayscale ? ' checked' : ''}>
                <span class="fas fa-circle-half-stroke" aria-hidden="true"></span>
                <span class="leaflet-control-grayscale-text">Grayscale map</span>
            </label>`;
        // Without this, clicking the button also pans/zooms the map underneath it.
        L.DomEvent.disableClickPropagation(container);
        return container;
    },
});

L.control.grayscale = (options) => new L.Control.Grayscale(options);
