// index.template.html has a single #visualization slot; this fills it with the
// demo's own markup via setVisualizationSlot(), then owns rendering the
// plan into that markup for the rest of the page's lifetime.

// The warehouse layout mirrors the static structure of Warehouse.java: 5 columns (A to E) and
// 3 rows (1 to 3) of shelvings, each with 10 rows per side. Sizes are in canvas units, which
// are scaled to the width of the map.
const WAREHOUSE_COLUMNS = ['A', 'B', 'C', 'D', 'E'];
const WAREHOUSE_ROWS = ['1', '2', '3'];
const WAREHOUSE_PADDING = 100;
const SHELVING_PADDING = 80;
const SHELVING_WIDTH = 150;
const SHELVING_HEIGHT = 300;
const SHELVING_ROWS = 10;
const SHELVING_LINE_WIDTH = 5;
const TROLLEY_PATH_LINE_WIDTH = 5;
const CONTENT_WIDTH = 2 * WAREHOUSE_PADDING + (SHELVING_WIDTH + SHELVING_PADDING) * WAREHOUSE_COLUMNS.length;
const CONTENT_HEIGHT = 2 * WAREHOUSE_PADDING + (SHELVING_HEIGHT + SHELVING_PADDING) * WAREHOUSE_ROWS.length;

const SHELVINGS = new Map();
WAREHOUSE_COLUMNS.forEach((column, columnIndex) => {
    WAREHOUSE_ROWS.forEach((row, rowIndex) => {
        const id = column + row;
        SHELVINGS.set(id, {
            id,
            columnIndex,
            x: WAREHOUSE_PADDING + columnIndex * (SHELVING_WIDTH + SHELVING_PADDING),
            y: WAREHOUSE_PADDING + rowIndex * (SHELVING_HEIGHT + SHELVING_PADDING),
        });
    });
});

const app = {
    start() {
        setVisualizationSlot(`
    <div class="row g-3">
        <div class="col-lg-8">
            <div class="card shadow-sm">
                <div class="card-body">
                    <h5 class="card-title">Warehouse map</h5>
                    <div id="warehouseCanvasContainer"><canvas id="warehouseCanvas"></canvas></div>
                    <small class="text-muted">A shelving is 2 meters wide and 10 meters high. The numbers on a path are the order in which the trolley picks.</small>
                </div>
            </div>
        </div>
        <div class="col-lg-4">
            <div class="card shadow-sm">
                <div class="card-body">
                    <h5 class="card-title">Trolleys</h5>
                    <table class="table table-sm align-middle mb-0">
                        <thead><tr><th scope="col">Show</th><th scope="col">Trolley</th><th scope="col" class="text-end">Items</th><th scope="col" class="text-end">Distance</th></tr></thead>
                        <tbody id="trolleyLegend"></tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
    <h5 class="mt-4">Picking plan</h5>
    <div id="pickingPlan"></div>
    <div id="unassignedPickTasksContainer"></div>
`);

        // Trolleys whose path is hidden on the map; kept across renders, as a solving plan re-renders every 2 seconds.
        this.hiddenTrolleyIds = new Set();
        this.plan = null;
        window.addEventListener('resize', () => this.drawMap());

        this.quickstartPage = new QuickstartPage({
            modelPath: '/v1/picking-plans',
            renderSchedule: (plan) => this.renderPlan(plan),
            renderInfo: (plan) => this.renderInfo(plan),
            mergeModelOutput: (plan, modelOutput) => this.mergeModelOutput(plan, modelOutput),
        });
    },

    // modelOutput only carries what the solver decided (the path of each trolley and its length), not the full
    // problem, so plan (the QuickstartPage's loadedSchedule) keeps the full modelInput (products, pick tasks,
    // buckets) and this only overlays the solved bits.
    mergeModelOutput(plan, modelOutput) {
        if (plan == null || modelOutput == null || modelOutput.trolleys == null) {
            return;
        }
        const outputByTrolleyId = new Map(modelOutput.trolleys.map((trolley) => [trolley.id, trolley]));
        plan.trolleys = plan.trolleys.map((trolley) => {
            const solved = outputByTrolleyId.get(trolley.id);
            return solved == null ? trolley
                : {...trolley, pickTaskIds: solved.pickTaskIds ?? [], travelDistance: solved.travelDistance};
        });
    },

    renderInfo(plan) {
        if (plan == null) {
            return '';
        }
        const orderCount = new Set(plan.pickTasks.map((pickTask) => pickTask.orderId)).size;
        return `${plan.trolleys.length} trolleys · ${orderCount} orders · ${plan.pickTasks.length} pick tasks`;
    },

    renderPlan(plan) {
        this.plan = plan;
        const productById = new Map(plan.products.map((product) => [product.id, product]));
        const pickTaskById = new Map(plan.pickTasks.map((pickTask) => [pickTask.id, pickTask]));
        // Trolley colors first, in trolley order, so a trolley keeps its color while orders move between trolleys.
        resetColorMap();
        plan.trolleys.forEach((trolley) => this.trolleyColor(trolley.id));

        this.renderTrolleyLegend(plan);
        this.drawMap();
        this.renderPickingPlan(plan, productById, pickTaskById);
        this.renderUnassignedPickTasks(plan, productById);
    },

    trolleyColor(trolleyId) {
        return pickColor(`trolley-${trolleyId}`);
    },

    orderColor(orderId) {
        return pickColor(`order-${orderId}`);
    },

    pickTaskIdsOf(trolley) {
        return trolley.pickTaskIds ?? [];
    },

    renderTrolleyLegend(plan) {
        const legend = $('#trolleyLegend');
        legend.children().remove();
        plan.trolleys.forEach((trolley) => {
            const color = this.trolleyColor(trolley.id);
            const checkbox = $('<input class="form-check-input" type="checkbox"/>')
                .prop('checked', !this.hiddenTrolleyIds.has(trolley.id))
                .on('change', (event) => {
                    if (event.target.checked) {
                        this.hiddenTrolleyIds.delete(trolley.id);
                    } else {
                        this.hiddenTrolleyIds.add(trolley.id);
                    }
                    this.drawMap();
                });
            legend.append($('<tr/>')
                .append($('<td/>').append(checkbox))
                .append($('<td/>')
                    .append($('<span class="trolley-swatch me-2"/>').css('background-color', color.bg))
                    .append($('<a/>').attr('href', `#trolley-${trolley.id}`).text(`Trolley ${trolley.id}`)))
                .append($('<td class="text-end"/>').text(this.pickTaskIdsOf(trolley).length))
                .append($('<td class="text-end"/>').text(this.formatDistance(trolley.travelDistance))));
        });
    },

    formatDistance(distance) {
        return distance == null ? '-' : `${distance} m`;
    },

    formatLocation(location) {
        return `${location.shelvingId} ${location.side.toLowerCase()} row ${location.row}`;
    },

    // ************************************************************************
    // Picking plan
    // ************************************************************************

    renderPickingPlan(plan, productById, pickTaskById) {
        const container = $('#pickingPlan');
        container.children().remove();
        const trolleysByOrderId = new Map();
        plan.trolleys.forEach((trolley) => this.pickTaskIdsOf(trolley).forEach((pickTaskId) => {
            const orderId = pickTaskById.get(pickTaskId)?.orderId;
            if (!trolleysByOrderId.has(orderId)) {
                trolleysByOrderId.set(orderId, new Set());
            }
            trolleysByOrderId.get(orderId).add(trolley.id);
        }));

        const usedTrolleys = plan.trolleys.filter((trolley) => this.pickTaskIdsOf(trolley).length > 0);
        if (usedTrolleys.length === 0) {
            container.append($('<p class="text-muted"/>').text('No trolley has a path yet. Click Solve to plan the picking.'));
            return;
        }
        usedTrolleys.forEach((trolley) => {
            const pickTasks = this.pickTaskIdsOf(trolley).map((pickTaskId) => pickTaskById.get(pickTaskId))
                .filter((pickTask) => pickTask != null);
            container.append(this.renderTrolleyCard(trolley, pickTasks, productById, trolleysByOrderId));
        });
    },

    renderTrolleyCard(trolley, pickTasks, productById, trolleysByOrderId) {
        const color = this.trolleyColor(trolley.id);
        const card = $('<div class="card shadow-sm mb-3"/>').attr('id', `trolley-${trolley.id}`);
        card.append($('<div class="card-header d-flex align-items-center"/>')
            .append($('<span class="trolley-swatch me-2"/>').css('background-color', color.bg))
            .append($('<strong/>').text(`Trolley ${trolley.id}`))
            .append($('<span class="ms-auto text-muted"/>')
                .text(`${pickTasks.length} items · ${this.formatDistance(trolley.travelDistance)}`)));

        const body = $('<div class="card-body row g-3"/>').appendTo(card);
        body.append($('<div class="col-md-4"/>').append(this.renderBuckets(trolley, pickTasks, productById, trolleysByOrderId)));
        body.append($('<div class="col-md-8 picking-table"/>').append(this.renderPickTaskTable(pickTasks, productById)));
        return card;
    },

    // A bucket is never shared between two orders, so every order on the trolley takes up whole buckets.
    renderBuckets(trolley, pickTasks, productById, trolleysByOrderId) {
        const volumeByOrderId = new Map();
        pickTasks.forEach((pickTask) => {
            const volume = productById.get(pickTask.productId)?.volume ?? 0;
            volumeByOrderId.set(pickTask.orderId, (volumeByOrderId.get(pickTask.orderId) ?? 0) + volume);
        });
        const container = $('<div/>');
        const buckets = $('<div class="d-flex flex-wrap gap-1 mb-2"/>').appendTo(container);
        const ordersTable = $('<table class="table table-sm mb-1"/>')
            .append($('<thead><tr><th scope="col">Order</th><th scope="col" class="text-end">Volume</th><th scope="col" class="text-end">Buckets</th></tr></thead>'));
        const ordersTableBody = $('<tbody/>').appendTo(ordersTable);
        let usedBuckets = 0;
        [...volumeByOrderId.entries()].sort((a, b) => b[1] - a[1]).forEach(([orderId, orderVolume]) => {
            const orderColor = this.orderColor(orderId);
            const requiredBuckets = Math.ceil(orderVolume / trolley.bucketCapacity);
            for (let bucket = 1; bucket <= requiredBuckets; bucket++) {
                const bucketVolume = bucket < requiredBuckets ? trolley.bucketCapacity
                    : orderVolume - (requiredBuckets - 1) * trolley.bucketCapacity;
                const fillPercentage = Math.ceil((100 * bucketVolume) / trolley.bucketCapacity);
                buckets.append($('<div class="bucket"/>')
                    .attr('title', `${fillPercentage}% of the bucket holds order ${orderId}`)
                    .append($('<div class="bucket-fill"/>').css({width: `${fillPercentage}%`, 'background-color': orderColor.bg})));
            }
            usedBuckets += requiredBuckets;
            const splitTrolleyIds = [...(trolleysByOrderId.get(orderId) ?? [])].filter((trolleyId) => trolleyId !== trolley.id);
            const orderCell = $('<td/>').append($('<span class="badge"/>')
                .css({'background-color': orderColor.bg, color: orderColor.fg}).text(orderId));
            if (splitTrolleyIds.length > 0) {
                orderCell.append($('<small class="text-muted ms-1"/>')
                    .text(`also on ${splitTrolleyIds.map((trolleyId) => `trolley ${trolleyId}`).join(', ')}`));
            }
            ordersTableBody.append($('<tr/>')
                .append(orderCell)
                .append($('<td class="text-end"/>').text(orderVolume))
                .append($('<td class="text-end"/>').text(requiredBuckets)));
        });
        for (let bucket = usedBuckets; bucket < trolley.bucketCount; bucket++) {
            buckets.append($('<div class="bucket" title="Free bucket"/>'));
        }
        if (usedBuckets > trolley.bucketCount) {
            container.append($('<div class="alert alert-danger py-1 px-2"/>')
                .text(`Needs ${usedBuckets} buckets, but only has ${trolley.bucketCount}.`));
        }
        container.append(ordersTable);
        container.append($('<small class="text-muted"/>')
            .text(`${trolley.bucketCount} buckets of ${trolley.bucketCapacity} cm³`));
        return container;
    },

    renderPickTaskTable(pickTasks, productById) {
        const table = $('<table class="table table-sm table-striped mb-0"/>')
            .append($('<thead><tr><th scope="col">#</th><th scope="col">Location</th><th scope="col">Order</th><th scope="col">Pick task</th><th scope="col">Product</th><th scope="col" class="text-end">Volume</th></tr></thead>'));
        const tableBody = $('<tbody/>').appendTo(table);
        pickTasks.forEach((pickTask, index) => {
            const product = productById.get(pickTask.productId);
            const orderColor = this.orderColor(pickTask.orderId);
            tableBody.append($('<tr/>')
                .append($('<td/>').text(index + 1))
                .append($('<td/>').text(product == null ? '-' : this.formatLocation(product.location)))
                .append($('<td/>').append($('<span class="badge"/>')
                    .css({'background-color': orderColor.bg, color: orderColor.fg}).text(pickTask.orderId)))
                .append($('<td/>').text(pickTask.id))
                .append($('<td/>').text(product?.name ?? pickTask.productId))
                .append($('<td class="text-end"/>').text(product?.volume ?? '-')));
        });
        return table;
    },

    renderUnassignedPickTasks(plan, productById) {
        const container = $('#unassignedPickTasksContainer');
        container.children().remove();
        const assignedPickTaskIds = new Set(plan.trolleys.flatMap((trolley) => this.pickTaskIdsOf(trolley)));
        const unassignedPickTasks = plan.pickTasks.filter((pickTask) => !assignedPickTaskIds.has(pickTask.id));
        if (unassignedPickTasks.length === 0) {
            return;
        }
        container.append($('<h5 class="mt-4"/>').text(`Unassigned pick tasks (${unassignedPickTasks.length})`));
        const table = $('<table class="table table-sm table-striped"/>')
            .append($('<thead><tr><th scope="col">Pick task</th><th scope="col">Order</th><th scope="col">Product</th><th scope="col">Location</th><th scope="col" class="text-end">Volume</th></tr></thead>'))
            .appendTo(container);
        const tableBody = $('<tbody/>').appendTo(table);
        unassignedPickTasks.forEach((pickTask) => {
            const product = productById.get(pickTask.productId);
            tableBody.append($('<tr/>')
                .append($('<td/>').text(pickTask.id))
                .append($('<td/>').text(pickTask.orderId))
                .append($('<td/>').text(product?.name ?? pickTask.productId))
                .append($('<td/>').text(product == null ? '-' : this.formatLocation(product.location)))
                .append($('<td class="text-end"/>').text(product?.volume ?? '-')));
        });
    },

    // ************************************************************************
    // Warehouse map
    // ************************************************************************

    drawMap() {
        const canvas = document.getElementById('warehouseCanvas');
        if (canvas == null) {
            return;
        }
        const width = canvas.parentElement.clientWidth;
        if (width === 0) {
            return;
        }
        const scale = width / CONTENT_WIDTH;
        const height = CONTENT_HEIGHT * scale;
        const pixelRatio = window.devicePixelRatio || 1;
        canvas.width = width * pixelRatio;
        canvas.height = height * pixelRatio;
        canvas.style.width = `${width}px`;
        canvas.style.height = `${height}px`;
        const ctx = canvas.getContext('2d');
        ctx.setTransform(scale * pixelRatio, 0, 0, scale * pixelRatio, 0, 0);
        ctx.clearRect(0, 0, CONTENT_WIDTH, CONTENT_HEIGHT);

        SHELVINGS.forEach((shelving) => this.drawShelving(ctx, shelving));
        if (this.plan == null) {
            return;
        }
        const productById = new Map(this.plan.products.map((product) => [product.id, product]));
        const pickTaskById = new Map(this.plan.pickTasks.map((pickTask) => [pickTask.id, pickTask]));
        const trolleyCount = this.plan.trolleys.length;
        // Every trolley walks its own lane, offset by its index, so paths through the same aisle stay apart.
        const paths = this.plan.trolleys.map((trolley, trolleyIndex) => {
            const locations = this.pickTaskIdsOf(trolley)
                .map((pickTaskId) => productById.get(pickTaskById.get(pickTaskId)?.productId)?.location)
                .filter((location) => location != null && SHELVINGS.has(location.shelvingId));
            return {trolley, trolleyIndex, locations};
        }).filter((path) => path.locations.length > 0 && !this.hiddenTrolleyIds.has(path.trolley.id)
            && SHELVINGS.has(path.trolley.location.shelvingId));

        paths.forEach((path) => this.drawTrolleyPath(ctx, path, trolleyCount));
        paths.forEach((path) => this.drawPickOrder(ctx, path, trolleyCount));
    },

    drawShelving(ctx, shelving) {
        ctx.strokeStyle = '#198754';
        ctx.lineJoin = 'bevel';
        ctx.lineWidth = SHELVING_LINE_WIDTH;
        ctx.strokeRect(shelving.x, shelving.y, SHELVING_WIDTH, SHELVING_HEIGHT);
        ctx.font = '30px sans-serif';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillStyle = '#198754';
        ctx.fillText(shelving.id, shelving.x + SHELVING_WIDTH / 2, shelving.y + SHELVING_HEIGHT / 2);
    },

    drawTrolleyPath(ctx, {trolley, trolleyIndex, locations}, trolleyCount) {
        const path = [trolley.location, ...locations, trolley.location];
        ctx.lineJoin = 'round';
        ctx.lineWidth = TROLLEY_PATH_LINE_WIDTH;
        ctx.strokeStyle = this.trolleyColor(trolley.id).bg;
        ctx.beginPath();
        let previous = path[0];
        let previousPoint = locationToPoint(previous, trolleyIndex, trolleyCount);
        ctx.moveTo(previousPoint.x, previousPoint.y);
        ctx.arc(previousPoint.x, previousPoint.y, 5, 0, 2 * Math.PI);
        for (let i = 1; i < path.length; i++) {
            const location = path[i];
            const point = locationToPoint(location, trolleyIndex, trolleyCount);
            drawPathBetweenLocations(ctx, trolleyIndex, trolleyCount, previous, previousPoint, location, point);
            ctx.arc(point.x, point.y, 5, 0, 2 * Math.PI);
            previous = location;
            previousPoint = point;
        }
        ctx.stroke();
    },

    // Labels every stop with its position in the trolley's path. Stops close to each other share one label, so the
    // numbers stay readable.
    drawPickOrder(ctx, {trolley, trolleyIndex, locations}, trolleyCount) {
        const rowHeight = SHELVING_HEIGHT / SHELVING_ROWS;
        const labelBandHeight = rowHeight * 4;
        const labels = [];
        locations.forEach((location, index) => {
            const point = locationToPoint(location, trolleyIndex, trolleyCount);
            point.y = Math.floor(point.y / labelBandHeight) * labelBandHeight + (labelBandHeight / trolleyCount) * trolleyIndex;
            const nearbyLabel = labels.find((label) => Math.abs(point.x - label.x) + Math.abs(point.y - label.y) < 2 * rowHeight);
            if (nearbyLabel != null) {
                nearbyLabel.numbers.push(index + 1);
            } else {
                labels.push({x: point.x, y: point.y, numbers: [index + 1]});
            }
        });
        ctx.font = 'bold 22px sans-serif';
        ctx.textAlign = 'center';
        ctx.lineWidth = 4;
        ctx.strokeStyle = '#ffffff';
        ctx.fillStyle = this.trolleyColor(trolley.id).bg;
        labels.forEach((label) => {
            const text = label.numbers.join(', ');
            ctx.strokeText(text, label.x, label.y);
            ctx.fillText(text, label.x, label.y);
        });
    },
};

/**
 * Transforms a warehouse location into a point in the aisle next to it, offset by the trolley's lane.
 */
function locationToPoint(location, trolleyIndex, trolleyCount) {
    const shelving = SHELVINGS.get(location.shelvingId);
    return {
        x: aisleX(shelving, location.side, trolleyIndex, trolleyCount),
        y: shelving.y + location.row * (SHELVING_HEIGHT / SHELVING_ROWS),
    };
}

function aisleX(shelving, side, trolleyIndex, trolleyCount) {
    const laneOffset = ((0.5 * SHELVING_PADDING) / trolleyCount) * trolleyIndex;
    return side === 'LEFT'
        ? shelving.x - SHELVING_LINE_WIDTH - laneOffset
        : shelving.x + SHELVING_WIDTH + SHELVING_LINE_WIDTH + laneOffset;
}

/**
 * Gets the point in the cross aisle above or below a shelving side, offset by the trolley's lane.
 */
function crossAislePoint(shelving, side, isAbove, trolleyIndex, trolleyCount) {
    const laneOffset = ((0.25 * SHELVING_PADDING) / trolleyCount) * trolleyIndex;
    return {
        x: aisleX(shelving, side, trolleyIndex, trolleyCount),
        y: isAbove ? shelving.y - 0.25 * SHELVING_PADDING - laneOffset
            : shelving.y + SHELVING_HEIGHT + 0.25 * SHELVING_PADDING + laneOffset,
    };
}

/**
 * Draws the walk between two locations around the shelvings, the way Warehouse.calculateDistance() measures it.
 */
function drawPathBetweenLocations(ctx, trolleyIndex, trolleyCount, start, startPoint, end, endPoint) {
    const startShelving = SHELVINGS.get(start.shelvingId);
    const endShelving = SHELVINGS.get(end.shelvingId);
    ctx.moveTo(startPoint.x, startPoint.y);
    if (startShelving === endShelving && start.side === end.side) {
        // Same aisle of the same shelving
        ctx.lineTo(endPoint.x, endPoint.y);
    } else if (startShelving === endShelving) {
        // Around the shelving, over the shortest end
        const isAbove = start.row + end.row < 2 * SHELVING_ROWS - start.row - end.row;
        lineThrough(ctx, [
            crossAislePoint(startShelving, start.side, isAbove, trolleyIndex, trolleyCount),
            crossAislePoint(endShelving, end.side, isAbove, trolleyIndex, trolleyCount),
            endPoint]);
    } else if (startShelving.x === endShelving.x && start.side === end.side) {
        // Same aisle, different warehouse rows
        ctx.lineTo(endPoint.x, endPoint.y);
    } else if (startShelving.x === endShelving.x) {
        // Same column, but the other aisle: cross over in the cross aisle between both shelvings
        const isAbove = startShelving.y < endShelving.y;
        lineThrough(ctx, [
            crossAislePoint(endShelving, start.side, isAbove, trolleyIndex, trolleyCount),
            crossAislePoint(endShelving, end.side, isAbove, trolleyIndex, trolleyCount),
            endPoint]);
    } else if (startShelving.y === endShelving.y && isSharedAisle(startShelving, start.side, endShelving, end.side)) {
        // Neighbouring shelvings that face each other across one aisle
        lineThrough(ctx, [{x: endPoint.x, y: startPoint.y}, endPoint]);
    } else if (startShelving.y === endShelving.y) {
        // Same warehouse row, through the cross aisle over the shortest end
        const isAbove = start.row + end.row < 2 * SHELVING_ROWS - start.row - end.row;
        lineThrough(ctx, [
            crossAislePoint(startShelving, start.side, isAbove, trolleyIndex, trolleyCount),
            crossAislePoint(endShelving, end.side, isAbove, trolleyIndex, trolleyCount),
            endPoint]);
    } else {
        // Different warehouse rows and columns
        const isAbove = endShelving.y < startShelving.y;
        const startCrossing = crossAislePoint(startShelving, start.side, isAbove, trolleyIndex, trolleyCount);
        const endCrossing = crossAislePoint(endShelving, end.side, isAbove, trolleyIndex, trolleyCount);
        lineThrough(ctx, [startCrossing, {x: endCrossing.x, y: startCrossing.y}, endCrossing, endPoint]);
    }
}

function isSharedAisle(startShelving, startSide, endShelving, endSide) {
    return (startSide === 'LEFT' && endSide === 'RIGHT' && endShelving.columnIndex === startShelving.columnIndex - 1)
        || (startSide === 'RIGHT' && endSide === 'LEFT' && endShelving.columnIndex === startShelving.columnIndex + 1);
}

function lineThrough(ctx, points) {
    points.forEach((point) => ctx.lineTo(point.x, point.y));
}

app.start();
