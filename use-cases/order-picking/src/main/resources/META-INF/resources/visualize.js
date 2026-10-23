// index.template.html has a single #visualization slot; this fills it with the
// demo's own markup via setVisualizationSlot(), then owns rendering the
// plan into that markup for the rest of the page's lifetime.

// The warehouse layout mirrors the static structure of Warehouse.java: 5 columns (A to E) and
// 3 rows (1 to 3) of racks, each with 10 bays per side. Sizes are in canvas units, which
// are scaled to the width of the map.
const WAREHOUSE_COLUMNS = ['A', 'B', 'C', 'D', 'E'];
const WAREHOUSE_ROWS = ['1', '2', '3'];
const WAREHOUSE_PADDING = 100;
const RACK_PADDING = 80;
const RACK_WIDTH = 150;
const RACK_HEIGHT = 300;
const RACK_BAYS = 10;
const RACK_LINE_WIDTH = 5;
const PICKING_CART_PATH_LINE_WIDTH = 5;
const CONTENT_WIDTH = 2 * WAREHOUSE_PADDING + (RACK_WIDTH + RACK_PADDING) * WAREHOUSE_COLUMNS.length;
const CONTENT_HEIGHT = 2 * WAREHOUSE_PADDING + (RACK_HEIGHT + RACK_PADDING) * WAREHOUSE_ROWS.length;

const RACKS = new Map();
WAREHOUSE_COLUMNS.forEach((column, columnIndex) => {
    WAREHOUSE_ROWS.forEach((row, rowIndex) => {
        const id = column + row;
        RACKS.set(id, {
            id,
            columnIndex,
            x: WAREHOUSE_PADDING + columnIndex * (RACK_WIDTH + RACK_PADDING),
            y: WAREHOUSE_PADDING + rowIndex * (RACK_HEIGHT + RACK_PADDING),
        });
    });
});

const app = {
    start() {
        setVisualizationSlot(`
    <ul class="nav nav-pills viewTabs" role="tablist">
        <li class="nav-item" role="presentation">
            <button class="nav-link active" id="pickingPlanTab" data-bs-toggle="tab"
                    data-bs-target="#pickingPlanPanel" type="button" role="tab" aria-controls="pickingPlanPanel"
                    aria-selected="true">Picking plan
            </button>
        </li>
        <li class="nav-item" role="presentation">
            <button class="nav-link" id="mapTab" data-bs-toggle="tab"
                    data-bs-target="#mapPanel" type="button" role="tab" aria-controls="mapPanel"
                    aria-selected="false">Map
            </button>
        </li>
        <li class="nav-item" role="presentation">
            <button class="nav-link" id="unassignedTab" data-bs-toggle="tab"
                    data-bs-target="#unassignedPanel" type="button" role="tab" aria-controls="unassignedPanel"
                    aria-selected="false">Unassigned <span id="unassignedPickTaskCount" class="badge rounded-pill text-bg-secondary"></span>
            </button>
        </li>
    </ul>
    <div class="tab-content">
        <div class="tab-pane fade show active" id="pickingPlanPanel" role="tabpanel" aria-labelledby="pickingPlanTab">
            <div id="pickingPlan"></div>
        </div>
        <div class="tab-pane fade" id="mapPanel" role="tabpanel" aria-labelledby="mapTab">
            <div class="row g-3">
                <div class="col-lg-8">
                    <div class="card shadow-sm">
                        <div class="card-body">
                            <h5 class="card-title">Warehouse map</h5>
                            <div id="warehouseCanvasContainer"><canvas id="warehouseCanvas"></canvas></div>
                            <small class="text-muted">A rack is 2 meters wide and 10 meters long. The numbers on a path are the order in which the picking cart picks.</small>
                        </div>
                    </div>
                </div>
                <div class="col-lg-4">
                    <div class="card shadow-sm">
                        <div class="card-body">
                            <h5 class="card-title">Picking carts</h5>
                            <table class="table table-sm align-middle mb-0">
                                <thead><tr><th scope="col">Show</th><th scope="col">Picking cart</th><th scope="col" class="text-end">Items</th><th scope="col" class="text-end">Distance</th></tr></thead>
                                <tbody id="pickingCartLegend"></tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="tab-pane fade" id="unassignedPanel" role="tabpanel" aria-labelledby="unassignedTab">
            <div id="unassignedPickTasksContainer"></div>
        </div>
    </div>
`);

        // Picking carts whose path is hidden on the map; kept across renders, as a solving plan re-renders every 2 seconds.
        this.hiddenPickingCartIds = new Set();
        this.plan = null;
        window.addEventListener('resize', () => this.drawMap());
        // The canvas has no width while its tab pane is hidden, so drawMap() skips it then and it needs a redraw once shown.
        document.getElementById('mapTab').addEventListener('shown.bs.tab', () => this.drawMap());

        this.quickstartPage = new QuickstartPage({
            modelPath: '/v1/picking-plans',
            renderSchedule: (plan) => this.renderPlan(plan),
            renderInfo: (plan) => this.renderInfo(plan),
            mergeModelOutput: (plan, modelOutput) => this.mergeModelOutput(plan, modelOutput),
        });
    },

    // modelOutput only carries what the solver decided (the path of each picking cart and its length), not the full
    // problem, so plan (the QuickstartPage's loadedSchedule) keeps the full modelInput (products, pick tasks,
    // buckets) and this only overlays the solved bits.
    mergeModelOutput(plan, modelOutput) {
        if (plan == null || modelOutput == null || modelOutput.pickingCarts == null) {
            return;
        }
        const outputByPickingCartId = new Map(modelOutput.pickingCarts.map((pickingCart) => [pickingCart.id, pickingCart]));
        plan.pickingCarts = plan.pickingCarts.map((pickingCart) => {
            const solved = outputByPickingCartId.get(pickingCart.id);
            return solved == null ? pickingCart
                : {...pickingCart, pickTaskIds: solved.pickTaskIds ?? [], travelDistance: solved.travelDistance};
        });
    },

    renderInfo(plan) {
        if (plan == null) {
            return '';
        }
        const orderCount = new Set(plan.pickTasks.map((pickTask) => pickTask.orderId)).size;
        return `${plan.pickingCarts.length} picking carts · ${orderCount} orders · ${plan.pickTasks.length} pick tasks`;
    },

    renderPlan(plan) {
        this.plan = plan;
        const productById = new Map(plan.products.map((product) => [product.id, product]));
        const pickTaskById = new Map(plan.pickTasks.map((pickTask) => [pickTask.id, pickTask]));
        // Picking cart colors first, in picking cart order, so a picking cart keeps its color while orders move between picking carts.
        resetColorMap();
        plan.pickingCarts.forEach((pickingCart) => this.pickingCartColor(pickingCart.id));

        this.renderPickingCartLegend(plan);
        this.drawMap();
        this.renderPickingPlan(plan, productById, pickTaskById);
        this.renderUnassignedPickTasks(plan, productById);
    },

    pickingCartColor(pickingCartId) {
        return pickColor(`picking-cart-${pickingCartId}`);
    },

    orderColor(orderId) {
        return pickColor(`order-${orderId}`);
    },

    pickTaskIdsOf(pickingCart) {
        return pickingCart.pickTaskIds ?? [];
    },

    renderPickingCartLegend(plan) {
        const legend = $('#pickingCartLegend');
        legend.children().remove();
        plan.pickingCarts.forEach((pickingCart) => {
            const color = this.pickingCartColor(pickingCart.id);
            const checkbox = $('<input class="form-check-input" type="checkbox"/>')
                .prop('checked', !this.hiddenPickingCartIds.has(pickingCart.id))
                .on('change', (event) => {
                    if (event.target.checked) {
                        this.hiddenPickingCartIds.delete(pickingCart.id);
                    } else {
                        this.hiddenPickingCartIds.add(pickingCart.id);
                    }
                    this.drawMap();
                });
            legend.append($('<tr/>')
                .append($('<td/>').append(checkbox))
                .append($('<td/>')
                    .append($('<span class="picking-cart-swatch me-2"/>').css('background-color', color.bg))
                    .append($('<a/>').attr('href', `#picking-cart-${pickingCart.id}`).text(`Picking cart ${pickingCart.id}`)
                        .on('click', (event) => {
                            event.preventDefault();
                            this.showPickingCartCard(pickingCart.id);
                        })))
                .append($('<td class="text-end"/>').text(this.pickTaskIdsOf(pickingCart).length))
                .append($('<td class="text-end"/>').text(this.formatDistance(pickingCart.travelDistance))));
        });
    },

    // The card lives in the other tab, so a plain anchor jump would land on a hidden element.
    showPickingCartCard(pickingCartId) {
        const pickingPlanTab = document.getElementById('pickingPlanTab');
        const scrollToCard = () => document.getElementById(`picking-cart-${pickingCartId}`)?.scrollIntoView({behavior: 'smooth'});
        if (pickingPlanTab.classList.contains('active')) {
            scrollToCard();
            return;
        }
        pickingPlanTab.addEventListener('shown.bs.tab', scrollToCard, {once: true});
        bootstrap.Tab.getOrCreateInstance(pickingPlanTab).show();
    },

    formatDistance(distance) {
        return distance == null ? '-' : `${distance} m`;
    },

    formatLocation(location) {
        return `${location.rackId} ${location.side.toLowerCase()} bay ${location.bay}`;
    },

    // ************************************************************************
    // Picking plan
    // ************************************************************************

    renderPickingPlan(plan, productById, pickTaskById) {
        const container = $('#pickingPlan');
        container.children().remove();
        const pickingCartsByOrderId = new Map();
        plan.pickingCarts.forEach((pickingCart) => this.pickTaskIdsOf(pickingCart).forEach((pickTaskId) => {
            const orderId = pickTaskById.get(pickTaskId)?.orderId;
            if (!pickingCartsByOrderId.has(orderId)) {
                pickingCartsByOrderId.set(orderId, new Set());
            }
            pickingCartsByOrderId.get(orderId).add(pickingCart.id);
        }));

        // Every picking cart gets a card, even an empty one, so an unsolved plan still shows the capacity there is to fill.
        plan.pickingCarts.forEach((pickingCart) => {
            const pickTasks = this.pickTaskIdsOf(pickingCart).map((pickTaskId) => pickTaskById.get(pickTaskId))
                .filter((pickTask) => pickTask != null);
            container.append(this.renderPickingCartCard(pickingCart, pickTasks, productById, pickingCartsByOrderId));
        });
    },

    renderPickingCartCard(pickingCart, pickTasks, productById, pickingCartsByOrderId) {
        const color = this.pickingCartColor(pickingCart.id);
        const usedVolume = pickTasks.reduce((sum, pickTask) => sum + (productById.get(pickTask.productId)?.volume ?? 0), 0);
        const capacity = pickingCart.bucketCount * pickingCart.bucketCapacity;
        const card = $('<div class="card shadow-sm mb-3"/>').attr('id', `picking-cart-${pickingCart.id}`);
        card.append($('<div class="card-header d-flex align-items-center"/>')
            .append($('<span class="picking-cart-swatch me-2"/>').css('background-color', color.bg))
            .append($('<strong/>').text(`Picking cart ${pickingCart.id}`))
            .append($('<span class="ms-auto text-muted"/>')
                .text(`${pickTasks.length} items · ${usedVolume} / ${capacity} cm³ · ${this.formatDistance(pickingCart.travelDistance)}`)));

        const body = $('<div class="card-body row g-3"/>').appendTo(card);
        body.append($('<div class="col-md-4"/>').append(this.renderBuckets(pickingCart, pickTasks, productById, pickingCartsByOrderId)));
        body.append($('<div class="col-md-8 picking-table"/>').append(pickTasks.length === 0
            ? $('<p class="text-muted mb-0"/>').text('No pick tasks on this picking cart yet.')
            : this.renderPickTaskTable(pickTasks, productById)));
        return card;
    },

    // A bucket is never shared between two orders, so every order on the picking cart takes up whole buckets.
    renderBuckets(pickingCart, pickTasks, productById, pickingCartsByOrderId) {
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
            const requiredBuckets = Math.ceil(orderVolume / pickingCart.bucketCapacity);
            for (let bucket = 1; bucket <= requiredBuckets; bucket++) {
                const bucketVolume = bucket < requiredBuckets ? pickingCart.bucketCapacity
                    : orderVolume - (requiredBuckets - 1) * pickingCart.bucketCapacity;
                const fillPercentage = Math.ceil((100 * bucketVolume) / pickingCart.bucketCapacity);
                buckets.append($('<div class="bucket"/>')
                    .attr('title', `${fillPercentage}% of the bucket holds order ${orderId}`)
                    .append($('<div class="bucket-fill"/>').css({width: `${fillPercentage}%`, 'background-color': orderColor.bg})));
            }
            usedBuckets += requiredBuckets;
            const splitPickingCartIds = [...(pickingCartsByOrderId.get(orderId) ?? [])].filter((pickingCartId) => pickingCartId !== pickingCart.id);
            const orderCell = $('<td/>').append($('<span class="badge"/>')
                .css({'background-color': orderColor.bg, color: orderColor.fg}).text(orderId));
            if (splitPickingCartIds.length > 0) {
                orderCell.append($('<small class="text-muted ms-1"/>')
                    .text(`also on ${splitPickingCartIds.map((pickingCartId) => `picking cart ${pickingCartId}`).join(', ')}`));
            }
            ordersTableBody.append($('<tr/>')
                .append(orderCell)
                .append($('<td class="text-end"/>').text(orderVolume))
                .append($('<td class="text-end"/>').text(requiredBuckets)));
        });
        for (let bucket = usedBuckets; bucket < pickingCart.bucketCount; bucket++) {
            buckets.append($('<div class="bucket" title="Free bucket"/>'));
        }
        if (usedBuckets > pickingCart.bucketCount) {
            container.append($('<div class="alert alert-danger py-1 px-2"/>')
                .text(`Needs ${usedBuckets} buckets, but only has ${pickingCart.bucketCount}.`));
        }
        if (volumeByOrderId.size > 0) {
            container.append(ordersTable);
        }
        container.append($('<small class="text-muted"/>')
            .text(`${pickingCart.bucketCount} buckets of ${pickingCart.bucketCapacity} cm³`));
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
        const assignedPickTaskIds = new Set(plan.pickingCarts.flatMap((pickingCart) => this.pickTaskIdsOf(pickingCart)));
        const unassignedPickTasks = plan.pickTasks.filter((pickTask) => !assignedPickTaskIds.has(pickTask.id));
        $('#unassignedPickTaskCount').text(unassignedPickTasks.length);
        if (unassignedPickTasks.length === 0) {
            container.append($('<p class="text-muted"/>').text('Every pick task is on a picking cart.'));
            return;
        }
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

        RACKS.forEach((rack) => this.drawRack(ctx, rack));
        if (this.plan == null) {
            return;
        }
        const productById = new Map(this.plan.products.map((product) => [product.id, product]));
        const pickTaskById = new Map(this.plan.pickTasks.map((pickTask) => [pickTask.id, pickTask]));
        const pickingCartCount = this.plan.pickingCarts.length;
        // Every picking cart walks its own lane, offset by its index, so paths through the same aisle stay apart.
        const paths = this.plan.pickingCarts.map((pickingCart, pickingCartIndex) => {
            const locations = this.pickTaskIdsOf(pickingCart)
                .map((pickTaskId) => productById.get(pickTaskById.get(pickTaskId)?.productId)?.location)
                .filter((location) => location != null && RACKS.has(location.rackId));
            return {pickingCart, pickingCartIndex, locations};
        }).filter((path) => path.locations.length > 0 && !this.hiddenPickingCartIds.has(path.pickingCart.id)
            && RACKS.has(path.pickingCart.location.rackId));

        paths.forEach((path) => this.drawPickingCartPath(ctx, path, pickingCartCount));
        paths.forEach((path) => this.drawPickOrder(ctx, path, pickingCartCount));
    },

    drawRack(ctx, rack) {
        ctx.strokeStyle = '#198754';
        ctx.lineJoin = 'bevel';
        ctx.lineWidth = RACK_LINE_WIDTH;
        ctx.strokeRect(rack.x, rack.y, RACK_WIDTH, RACK_HEIGHT);
        ctx.font = '30px sans-serif';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillStyle = '#198754';
        ctx.fillText(rack.id, rack.x + RACK_WIDTH / 2, rack.y + RACK_HEIGHT / 2);
    },

    drawPickingCartPath(ctx, {pickingCart, pickingCartIndex, locations}, pickingCartCount) {
        const path = [pickingCart.location, ...locations, pickingCart.location];
        ctx.lineJoin = 'round';
        ctx.lineWidth = PICKING_CART_PATH_LINE_WIDTH;
        ctx.strokeStyle = this.pickingCartColor(pickingCart.id).bg;
        ctx.beginPath();
        let previous = path[0];
        let previousPoint = locationToPoint(previous, pickingCartIndex, pickingCartCount);
        ctx.moveTo(previousPoint.x, previousPoint.y);
        ctx.arc(previousPoint.x, previousPoint.y, 5, 0, 2 * Math.PI);
        for (let i = 1; i < path.length; i++) {
            const location = path[i];
            const point = locationToPoint(location, pickingCartIndex, pickingCartCount);
            drawPathBetweenLocations(ctx, pickingCartIndex, pickingCartCount, previous, previousPoint, location, point);
            ctx.arc(point.x, point.y, 5, 0, 2 * Math.PI);
            previous = location;
            previousPoint = point;
        }
        ctx.stroke();
    },

    // Labels every stop with its position in the picking cart's path. Stops close to each other share one label, so the
    // numbers stay readable.
    drawPickOrder(ctx, {pickingCart, pickingCartIndex, locations}, pickingCartCount) {
        const bayHeight = RACK_HEIGHT / RACK_BAYS;
        const labelBandHeight = bayHeight * 4;
        const labels = [];
        locations.forEach((location, index) => {
            const point = locationToPoint(location, pickingCartIndex, pickingCartCount);
            point.y = Math.floor(point.y / labelBandHeight) * labelBandHeight + (labelBandHeight / pickingCartCount) * pickingCartIndex;
            const nearbyLabel = labels.find((label) => Math.abs(point.x - label.x) + Math.abs(point.y - label.y) < 2 * bayHeight);
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
        ctx.fillStyle = this.pickingCartColor(pickingCart.id).bg;
        labels.forEach((label) => {
            const text = label.numbers.join(', ');
            ctx.strokeText(text, label.x, label.y);
            ctx.fillText(text, label.x, label.y);
        });
    },
};

/**
 * Transforms a warehouse location into a point in the aisle next to it, offset by the picking cart's lane.
 */
function locationToPoint(location, pickingCartIndex, pickingCartCount) {
    const rack = RACKS.get(location.rackId);
    return {
        x: aisleX(rack, location.side, pickingCartIndex, pickingCartCount),
        y: rack.y + location.bay * (RACK_HEIGHT / RACK_BAYS),
    };
}

function aisleX(rack, side, pickingCartIndex, pickingCartCount) {
    const laneOffset = ((0.5 * RACK_PADDING) / pickingCartCount) * pickingCartIndex;
    return side === 'LEFT'
        ? rack.x - RACK_LINE_WIDTH - laneOffset
        : rack.x + RACK_WIDTH + RACK_LINE_WIDTH + laneOffset;
}

/**
 * Gets the point in the cross aisle above or below a rack side, offset by the picking cart's lane.
 */
function crossAislePoint(rack, side, isAbove, pickingCartIndex, pickingCartCount) {
    const laneOffset = ((0.25 * RACK_PADDING) / pickingCartCount) * pickingCartIndex;
    return {
        x: aisleX(rack, side, pickingCartIndex, pickingCartCount),
        y: isAbove ? rack.y - 0.25 * RACK_PADDING - laneOffset
            : rack.y + RACK_HEIGHT + 0.25 * RACK_PADDING + laneOffset,
    };
}

/**
 * Draws the walk between two locations around the racks, the way Warehouse.calculateDistance() measures it.
 */
function drawPathBetweenLocations(ctx, pickingCartIndex, pickingCartCount, start, startPoint, end, endPoint) {
    const startRack = RACKS.get(start.rackId);
    const endRack = RACKS.get(end.rackId);
    ctx.moveTo(startPoint.x, startPoint.y);
    if (startRack === endRack && start.side === end.side) {
        // Same aisle of the same rack
        ctx.lineTo(endPoint.x, endPoint.y);
    } else if (startRack === endRack) {
        // Around the rack, over the shortest end
        const isAbove = start.bay + end.bay < 2 * RACK_BAYS - start.bay - end.bay;
        lineThrough(ctx, [
            crossAislePoint(startRack, start.side, isAbove, pickingCartIndex, pickingCartCount),
            crossAislePoint(endRack, end.side, isAbove, pickingCartIndex, pickingCartCount),
            endPoint]);
    } else if (startRack.x === endRack.x && start.side === end.side) {
        // Same aisle, different warehouse rows
        ctx.lineTo(endPoint.x, endPoint.y);
    } else if (startRack.x === endRack.x) {
        // Same column, but the other aisle: cross over in the cross aisle between both racks
        const isAbove = startRack.y < endRack.y;
        lineThrough(ctx, [
            crossAislePoint(endRack, start.side, isAbove, pickingCartIndex, pickingCartCount),
            crossAislePoint(endRack, end.side, isAbove, pickingCartIndex, pickingCartCount),
            endPoint]);
    } else if (startRack.y === endRack.y && isSharedAisle(startRack, start.side, endRack, end.side)) {
        // Neighbouring racks that face each other across one aisle
        lineThrough(ctx, [{x: endPoint.x, y: startPoint.y}, endPoint]);
    } else if (startRack.y === endRack.y) {
        // Same warehouse row, through the cross aisle over the shortest end
        const isAbove = start.bay + end.bay < 2 * RACK_BAYS - start.bay - end.bay;
        lineThrough(ctx, [
            crossAislePoint(startRack, start.side, isAbove, pickingCartIndex, pickingCartCount),
            crossAislePoint(endRack, end.side, isAbove, pickingCartIndex, pickingCartCount),
            endPoint]);
    } else {
        // Different warehouse rows and columns
        const isAbove = endRack.y < startRack.y;
        const startCrossing = crossAislePoint(startRack, start.side, isAbove, pickingCartIndex, pickingCartCount);
        const endCrossing = crossAislePoint(endRack, end.side, isAbove, pickingCartIndex, pickingCartCount);
        lineThrough(ctx, [startCrossing, {x: endCrossing.x, y: startCrossing.y}, endCrossing, endPoint]);
    }
}

function isSharedAisle(startRack, startSide, endRack, endSide) {
    return (startSide === 'LEFT' && endSide === 'RIGHT' && endRack.columnIndex === startRack.columnIndex - 1)
        || (startSide === 'RIGHT' && endSide === 'LEFT' && endRack.columnIndex === startRack.columnIndex + 1);
}

function lineThrough(ctx, points) {
    points.forEach((point) => ctx.lineTo(point.x, point.y));
}

app.start();
