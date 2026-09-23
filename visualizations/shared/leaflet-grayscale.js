// ── Grayscale map control ──
// A native Leaflet control (a leaflet-bar button, like the zoom control) that desaturates
// the tile layer, so the colored routes and markers drawn on top of the map stay readable.

L.Control.Grayscale = L.Control.extend({
    options: {
        position: 'topleft',
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
