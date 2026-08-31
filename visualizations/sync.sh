#!/usr/bin/env bash
# Syncs the shared visualization UI in visualizations/shared/ into each
# quickstart listed below:
#   - visualizations/shared/*.js, *.css are copied verbatim into that
#     quickstart's META-INF/resources/shared/ folder.
#   - visualizations/shared/index.template.html is rendered into that
#     quickstart's META-INF/resources/index.html, filling in the
#     {{TITLE}}/{{NAME}}/{{UTM_CONTENT}}/{{HEAD_EXTRA}}/{{SCRIPTS_EXTRA}}
#     placeholders from the metadata configured below.
#
# There is no per-quickstart HTML content here: the template's only "slot" is
# the <timefold-quickstart-visualization> element, which each quickstart's own
# visualize.js defines as a custom element and fills with whatever demo-specific
# markup it needs (see e.g. use-cases/bed-allocation/.../visualize.js).
#
# These are plain static resources with no build-time include mechanism, so
# the shared files are physically duplicated into every consuming quickstart.
# Run this after editing anything under visualizations/shared/, or a
# quickstart's metadata/features below, and commit the result together with
# the source edit.
#
# Deliberately written without associative arrays or namerefs (both bash 4+):
# macOS ships bash 3.2 as /bin/bash, and `env bash` resolves to it unless the
# caller has a newer bash earlier on PATH. Plain functions + case statements
# below do the same job and run on both.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SHARED_DIR="$SCRIPT_DIR/shared"
TEMPLATE_FILE="$SHARED_DIR/index.template.html"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Quickstarts that consume the shared visualization components.
QUICKSTART_DIRS=(
    "bed-allocation"
    "conference-scheduling"
)

# Metadata needed to render each quickstart's index.html from the shared
# template. Title follows the "<name> - Timefold Solver on Quarkus" convention
# used by every quickstart today; give one an override in render_index_html()
# if it ever needs to deviate from it.
quickstart_name() {
    case "$1" in
        bed-allocation) echo "Bed Allocation Scheduling" ;;
        conference-scheduling) echo "Conference Scheduling" ;;
        *) echo "quickstart_name: unknown quickstart '$1'" >&2; exit 1 ;;
    esac
}

quickstart_utm_content() {
    case "$1" in
        bed-allocation) echo "bed-allocation-java" ;;
        conference-scheduling) echo "conference-scheduling-java" ;;
        *) echo "quickstart_utm_content: unknown quickstart '$1'" >&2; exit 1 ;;
    esac
}

# Space-separated feature names each quickstart enables from the catalog below.
quickstart_features() {
    case "$1" in
        bed-allocation) echo "vis-timeline custom-css" ;;
        conference-scheduling) echo "color-picker" ;;
    esac
}

# Catalog of optional vendor "features" a quickstart's visualize.js can build
# on - external resources that aren't part of the shared bundle, so they don't
# belong in visualizations/shared/. Add a new one to feature_head/feature_scripts
# below, then enable it in quickstart_features() by name; a quickstart never
# writes out its own <script>/<style> tags.
#
# "custom-css" just links a style.css that the quickstart keeps next to its
# own index.html (not under shared/, so sync.sh never touches its content).
feature_head() {
    case "$1" in
        custom-css) echo '    <link rel="stylesheet" href="style.css">' ;;
    esac
}

feature_scripts() {
    case "$1" in
        vis-timeline) echo '<script src="https://cdn.jsdelivr.net/npm/vis-timeline@7.7.2/standalone/umd/vis-timeline-graph2d.min.js"
        integrity="sha256-Jy2+UO7rZ2Dgik50z3XrrNpnc5+2PAx9MhL2CicodME=" crossorigin="anonymous"></script>' ;;
        color-picker) echo '<script src="shared/color-picker.js"></script>' ;;
    esac
}

# Joins feature_head/feature_scripts (passed by name) output for each of a
# quickstart's enabled features, in listed order.
join_features() {
    local catalog_fn="$1" features="$2" feature
    for feature in $features; do
        "$catalog_fn" "$feature"
    done
}

render_index_html() {
    local resources_dir="$1" quickstart="$2"
    local name utm_content title features head_extra scripts_extra rendered
    name="$(quickstart_name "$quickstart")"
    utm_content="$(quickstart_utm_content "$quickstart")"
    title="$name - Timefold Solver on Quarkus"
    features="$(quickstart_features "$quickstart")"
    head_extra="$(join_features feature_head "$features")"
    scripts_extra="$(join_features feature_scripts "$features")"

    rendered="$(cat "$TEMPLATE_FILE")"
    # Strip the leading "this is a template, don't hand-edit the output" comment -
    # it documents this file for whoever edits it, but has no reason to ship in
    # every quickstart's actual generated index.html. Relies on that comment being
    # the file's first thing, closed by the first "-->" in the whole file.
    rendered="${rendered#*-->}"
    rendered="${rendered#$'\n'}"
    rendered="${rendered//\{\{TITLE\}\}/$title}"
    rendered="${rendered//\{\{NAME\}\}/$name}"
    rendered="${rendered//\{\{UTM_CONTENT\}\}/$utm_content}"
    rendered="${rendered//\{\{HEAD_EXTRA\}\}/$head_extra}"
    rendered="${rendered//\{\{SCRIPTS_EXTRA\}\}/$scripts_extra}"

    printf '%s\n' "$rendered" > "$resources_dir/index.html"
    echo "rendered $resources_dir/index.html"
}

for quickstart in "${QUICKSTART_DIRS[@]}"; do
    resources_dir="$REPO_ROOT/use-cases/$quickstart/src/main/resources/META-INF/resources"
    shared_target="$resources_dir/shared"

    rm -rf "$shared_target"
    mkdir -p "$shared_target"
    cp -v "$SHARED_DIR"/*.js "$SHARED_DIR"/*.css "$shared_target/"

    render_index_html "$resources_dir" "$quickstart"
done
