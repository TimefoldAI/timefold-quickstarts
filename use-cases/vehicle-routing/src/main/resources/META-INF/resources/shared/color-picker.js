// ── Color picker ──
// Color-blind-friendly categorical palette:
// https://venngage.com/blog/color-blind-friendly-palette/
//
// Assigns a stable {bg, fg} color pair to arbitrary keys (e.g. a talk type, a
// tag), first-come-first-served from the palette below, then remembers the
// assignment so the same key always maps to the same color for the rest of the
// render. Call resetColorMap() before each new render of a schedule - optionally
// with a Map/entries of fixed key -> {bg, fg} assignments to seed it with, for
// keys that should always get the same color regardless of encounter order.

const COLOR_PICKER_BG_COLORS = ["#009E73", "#0072B2", "#D55E00", "#000000", "#CC79A7", "#E69F00", "#F0E442", "#F6768E", "#C10020", "#A6BDD7", "#803E75", "#007D34", "#56B4E9", "#999999", "#8DD3C7", "#FFD92F", "#B3DE69", "#FB8072", "#80B1D3", "#B15928", "#CAB2D6", "#1B9E77", "#E7298A", "#6A3D9A"];
const COLOR_PICKER_FG_COLORS = ["#FFFFFF", "#FFFFFF", "#FFFFFF", "#FFFFFF", "#FFFFFF", "#000000", "#000000", "#FFFFFF", "#FFFFFF", "#000000", "#FFFFFF", "#FFFFFF", "#FFFFFF", "#000000", "#000000", "#000000", "#000000", "#FFFFFF", "#000000", "#FFFFFF", "#000000", "#FFFFFF", "#FFFFFF", "#FFFFFF"];

let COLOR_MAP = null;
let nextColorIndex = 0;

function resetColorMap(seedEntries) {
    COLOR_MAP = seedEntries ? new Map(seedEntries) : new Map();
    nextColorIndex = 0;
}

resetColorMap();

function pickColor(key) {
    let color = COLOR_MAP.get(key);
    if (color !== undefined) {
        return color;
    }
    const index = nextColorIndex++;
    color = {bg: COLOR_PICKER_BG_COLORS[index], fg: COLOR_PICKER_FG_COLORS[index]};
    COLOR_MAP.set(key, color);
    return color;
}
