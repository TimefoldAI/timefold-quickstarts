const LEFT = 'LEFT';
const RIGHT = 'RIGHT';

const WAREHOUSE_COLUMNS = ['A', 'B', 'C', 'D', 'E'];
const WAREHOUSE_ROWS = ['1', '2', '3'];

const WAREHOUSE_PADDING_TOP = 100;
const WAREHOUSE_PADDING_LEFT = 100;

const SHELVING_PADDING = 80;
const SHELVING_WIDTH = 150;
const SHELVING_HEIGHT = 300;
const SHELVING_ROWS = 10;

const SHELVING_LINE_WIDTH = 5;
const SHELVING_STROKE_STYLE = 'green';
const SHELVING_LINE_JOIN = 'bevel';

const TROLLEY_PATH_LINE_WIDTH = 5;
const TROLLEY_PATH_LINE_JOIN = 'round';

const SHELVINGS_MAP = new Map();

function shelvingId(i, j) {
    return '(' + i + ',' + j + ')';
}

class Point {
    x = 0;
    y = 0;

    constructor(x, y) {
        this.x = x;
        this.y = y;
    }
}

class ShelvingShape {
    id;
    x = 0;
    y = 0;
    width = 0;
    height = 0;

    constructor(id, x, y, width, height) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
}

class WarehouseLocation {
    shelvingId;
    side;
    row;

    constructor(shelvingId, side, row) {
        this.shelvingId = shelvingId;
        this.side = side;
        this.row = row;
    }
}

/**
 * Returns the WarehouseCanvas.
 */
function getWarehouseCanvasContext() {
    return document.getElementById('warehouseCanvas').getContext('2d');
}

/**
 * Clears the WarehouseCanvas.
 */
function clearWarehouseCanvas() {
    const canvas = document.getElementById('warehouseCanvas');
    const ctx = canvas.getContext('2d');
    ctx.setTransform(1, 0, 0, 1, 0, 0);
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    const parentWidth = canvas.parentElement.clientWidth;
    const parentHeight = canvas.parentElement.clientHeight;
    const contentWidth = 2 * WAREHOUSE_PADDING_LEFT + (SHELVING_WIDTH + SHELVING_PADDING) * WAREHOUSE_COLUMNS.length;
    const contentHeight = 2 * WAREHOUSE_PADDING_TOP + (SHELVING_HEIGHT + SHELVING_PADDING) * WAREHOUSE_ROWS.length;
    const minToFillParent = Math.min(parentWidth / contentWidth, parentHeight / contentHeight);
    const xOffset = (parentWidth - (contentWidth * minToFillParent)) / 2;
    const yOffset = (parentHeight - (contentHeight * minToFillParent)) / 2;
    canvas.width = parentWidth;
    canvas.height = parentHeight;
    ctx.translate(xOffset, yOffset);
    ctx.scale(minToFillParent, minToFillParent);
}

/**
 * Draws the WarehouseStructure on the WarehouseCanvas.
 */
function drawWarehouse() {
    const ctx = getWarehouseCanvasContext();
    let x = WAREHOUSE_PADDING_LEFT;
    for (let column = 0; column < WAREHOUSE_COLUMNS.length; column++) {
        let y = WAREHOUSE_PADDING_TOP;
        for (let row = 0; row < WAREHOUSE_ROWS.length; row++) {
            const shelving = new ShelvingShape(shelvingId(WAREHOUSE_COLUMNS[column], WAREHOUSE_ROWS[row]), x, y, SHELVING_WIDTH, SHELVING_HEIGHT);
            drawShelving(ctx, shelving);
            SHELVINGS_MAP.set(shelving.id, shelving);
            y = y + SHELVING_HEIGHT + SHELVING_PADDING;
        }
        x = x + SHELVING_WIDTH + SHELVING_PADDING;
    }
}

/**
 * Draws a ShelvingShape on the Warehouse canvas.
 */
function drawShelving(ctx, shelving) {
    ctx.strokeStyle = SHELVING_STROKE_STYLE;
    ctx.lineJoin = SHELVING_LINE_JOIN;
    ctx.lineWidth = SHELVING_LINE_WIDTH;
    ctx.strokeRect(shelving.x, shelving.y, shelving.width, shelving.height);

    ctx.font = '30px serif';
    ctx.textAlign = 'center';
    ctx.fillStyle = SHELVING_STROKE_STYLE

    const shelvingTextParts = shelving.id.split(',');
    ctx.fillText(shelvingTextParts[0].substring(1) + shelvingTextParts[1].substring(0, shelvingTextParts[1].length - 1),
               shelving.x + shelving.width / 2, shelving.y + shelving.height / 2);
}

/**
 * Draws the path travelled by a Trolley.
 */
function drawTrolleyPath(strokeStyle, warehouseLocations, trolleyIndex, trolleyCount) {
    const ctx = getWarehouseCanvasContext();
    ctx.lineJoin = TROLLEY_PATH_LINE_JOIN;
    ctx.lineWidth = TROLLEY_PATH_LINE_WIDTH;
    ctx.strokeStyle = strokeStyle;
    drawWarehousePath(warehouseLocations, trolleyIndex, trolleyCount);
}

function drawTrolleyText(strokeStyle, warehouseLocations, trolleyIndex, trolleyCount) {
    const ctx = getWarehouseCanvasContext();
    ctx.lineJoin = TROLLEY_PATH_LINE_JOIN;
    ctx.lineWidth = TROLLEY_PATH_LINE_WIDTH;
    ctx.strokeStyle = strokeStyle;
    drawTextForTrolley(strokeStyle, warehouseLocations, trolleyIndex, trolleyCount);
}

/**
 * Draws a path composed of WarehouseLocations.
 */
function drawWarehousePath(warehouseLocations, trolleyIndex, trolleyCount) {
    const ctx = getWarehouseCanvasContext();
    const startLocation = warehouseLocations[0];
    const startShelving = SHELVINGS_MAP.get(startLocation.shelvingId);
    const startPoint = location2Point(startShelving, startLocation.side, startLocation.row, trolleyIndex, trolleyCount);
    let lastPoint = startPoint;
    let lastShelving = startShelving;
    let lastSide = startLocation.side;
    let lastRow = startLocation.row;

    ctx.beginPath();
    ctx.moveTo(startPoint.x, startPoint.y);
    ctx.arc(startPoint.x, startPoint.y, 5, 0, 2 * Math.PI);

    for (let i = 1; i < warehouseLocations.length; i++) {
        const location = warehouseLocations[i];
        const shelving = SHELVINGS_MAP.get(location.shelvingId);
        const side = location.side;
        const row = location.row;
        const point = location2Point(shelving, location.side, location.row, trolleyIndex, trolleyCount);
        drawWarehousePathBetweenShelves(ctx, trolleyIndex, trolleyCount, lastShelving, lastSide, lastRow, lastPoint,
                shelving, side, row, point);
        ctx.arc(point.x, point.y, 5, 0, 2 * Math.PI);
        lastPoint = point;
        lastShelving = shelving;
        lastSide = side;
        lastRow = row;
    }
    ctx.stroke();
    ctx.closePath();
}

function drawTextForTrolley(strokeStyle, warehouseLocations, trolleyIndex, trolleyCount) {
    const ctx = getWarehouseCanvasContext();

    ctx.fillStyle = strokeStyle;
    ctx.strokeStyle = "#000000";
    let overlappingOrderTexts = [];
    const SHELVING_ROW_HEIGHT = SHELVING_HEIGHT / SHELVING_ROWS;
    const TEXT_SEPERATOR_HEIGHT = SHELVING_ROW_HEIGHT * 4;
    const SEPERATION_PER_TROLLEY = TEXT_SEPERATOR_HEIGHT / trolleyCount;
    for (let i = 0; i < warehouseLocations.length; i++) {
        const location = warehouseLocations[i];
        const shelving = SHELVINGS_MAP.get(location.shelvingId);
        const point = location2Point(shelving, location.side, location.row, trolleyIndex, trolleyCount);
        const pointFlooredRow = Math.floor(point.y / TEXT_SEPERATOR_HEIGHT) * TEXT_SEPERATOR_HEIGHT;

        point.y = pointFlooredRow + SEPERATION_PER_TROLLEY * trolleyIndex;
        addToOverlap(overlappingOrderTexts, i.toString(10), point);
    }

    for (let i = 0; i < overlappingOrderTexts.length; i++) {
        const overlappingOrders = overlappingOrderTexts[i];
        const text = '(' + overlappingOrders.orders.join(', ') + ')';

        ctx.strokeText(text, overlappingOrders.x, overlappingOrders.y);
        ctx.fillText(text, overlappingOrders.x, overlappingOrders.y);
    }
}

function addToOverlap(overlappingOrderTexts, orderText, orderPoint) {
    const SHELVING_ROW_HEIGHT = SHELVING_HEIGHT / SHELVING_ROWS;
    for (let i = 0; i < overlappingOrderTexts.length; i++) {
        const overlappingOrderText = overlappingOrderTexts[i];
        const distance = Math.abs(orderPoint.x - overlappingOrderText.x) + Math.abs(orderPoint.y - overlappingOrderText.y);
        if (distance < 2 * SHELVING_ROW_HEIGHT) {
            overlappingOrderText.orders.push(orderText);
            return;
        }
    }
    const newOverlappingOrderText = {
        orders: [orderText],
        x: orderPoint.x,
        y: orderPoint.y,
    };
    overlappingOrderTexts.push(newOverlappingOrderText);
}

/**
 * Draw a path around shelves connecting two WarehouseLocations
 */
function drawWarehousePathBetweenShelves(ctx, trolleyIndex, trolleyCount,
        startShelving, startSide, startRow, startPoint,
        endShelving, endSide, endRow, endPoint) {
    ctx.moveTo(startPoint.x, startPoint.y);
    if (startShelving === endShelving) {
        if (startSide === endSide) {
            // Two points on the same shelf and same side
            ctx.lineTo(endPoint.x, endPoint.y);
        } else {
            // Two points on the same shelf but different sides
            const isAbove = startRow + endRow < 2*SHELVING_ROWS - startRow - endRow;
            const aisleChangeStartPoint = location2AisleLane(startShelving, startSide, isAbove, trolleyIndex, trolleyCount);
            const aisleChangeEndPoint = location2AisleLane(endShelving, endSide, isAbove, trolleyIndex, trolleyCount);
            ctx.lineTo(aisleChangeStartPoint.x, aisleChangeStartPoint.y);
            ctx.lineTo(aisleChangeEndPoint.x, aisleChangeEndPoint.y);
            ctx.lineTo(endPoint.x, endPoint.y);
        }
    } else if (startShelving.x === endShelving.x) {
        if (startSide === endSide) {
            // Same Aisle, different rows
            ctx.lineTo(endPoint.x, endPoint.y);
        } else {
            // Different Aisle
            if (startShelving.y < endShelving.y) {
                // Going up
                const aisleChangeStartPoint = location2AisleLane(endShelving, startSide, false, trolleyIndex, trolleyCount);
                const aisleChangeEndPoint = location2AisleLane(endShelving, endSide, false, trolleyIndex, trolleyCount);
                ctx.lineTo(aisleChangeStartPoint.x, aisleChangeStartPoint.y);
                ctx.lineTo(aisleChangeEndPoint.x, aisleChangeEndPoint.y);
                ctx.lineTo(endPoint.x, endPoint.y);
            } else {
                // Going down
                const aisleChangeStartPoint = location2AisleLane(endShelving, startSide, true, trolleyIndex, trolleyCount);
                const aisleChangeEndPoint = location2AisleLane(endShelving, endSide, true, trolleyIndex, trolleyCount);
                ctx.lineTo(aisleChangeStartPoint.x, aisleChangeStartPoint.y);
                ctx.lineTo(aisleChangeEndPoint.x, aisleChangeEndPoint.y);
                ctx.lineTo(endPoint.x, endPoint.y);
            }
        }
    } else if (startShelving.y === endShelving.y) {
        const startColumn = shelvingToColumn(startShelving);
        const endColumn = shelvingToColumn(endShelving);
        if (startSide === LEFT) {
            if (endSide === RIGHT && endColumn === startColumn - 1) {
                // Same Aisle, different shelving
                ctx.lineTo(endPoint.x, startPoint.y);
                ctx.lineTo(endPoint.x, endPoint.y);
            } else {
                drawWarehousePathBetweenColumns(ctx, trolleyIndex, trolleyCount,
                        startShelving, startSide, startRow, startPoint,
                        endShelving, endSide, endRow, endPoint);
            }
        } else {
            if (endSide === LEFT && endColumn === startColumn + 1) {
                // Same Aisle, different shelving
                ctx.lineTo(endPoint.x, startPoint.y);
                ctx.lineTo(endPoint.x, endPoint.y);
            } else {
                drawWarehousePathBetweenColumns(ctx, trolleyIndex, trolleyCount,
                        startShelving, startSide, startRow, startPoint,
                        endShelving, endSide, endRow, endPoint);
            }
        }
    } else {
        drawWarehousePathBetweenColumns(ctx, trolleyIndex, trolleyCount,
                startShelving, startSide, startRow, startPoint,
                endShelving, endSide, endRow, endPoint);
    }
}

/**
 * Draws a path between shelvings in two different columns
 */
function drawWarehousePathBetweenColumns(ctx, trolleyIndex, trolleyCount,
        startShelving, startSide, startRow, startPoint,
        endShelving, endSide, endRow, endPoint) {
    if (startShelving.y === endShelving.y) {
        const isAbove = startRow + endRow < 2*SHELVING_ROWS - startRow - endRow;
        const aisleChangeStartPoint = location2AisleLane(startShelving, startSide, isAbove, trolleyIndex, trolleyCount);
        const aisleChangeEndPoint = location2AisleLane(endShelving, endSide, isAbove, trolleyIndex, trolleyCount);
        ctx.lineTo(aisleChangeStartPoint.x, aisleChangeStartPoint.y);
        ctx.lineTo(aisleChangeEndPoint.x, aisleChangeEndPoint.y);
        ctx.lineTo(endPoint.x, endPoint.y);
    } else {
        const isAbove = endShelving.y < startShelving.y;
        const aisleChangeStartPoint = location2AisleLane(startShelving, startSide, isAbove, trolleyIndex, trolleyCount);
        const aisleChangeEndPoint = location2AisleLane(endShelving, endSide, isAbove, trolleyIndex, trolleyCount);
        ctx.lineTo(aisleChangeStartPoint.x, aisleChangeStartPoint.y);
        ctx.lineTo(aisleChangeEndPoint.x, aisleChangeStartPoint.y);
        ctx.lineTo(aisleChangeEndPoint.x, aisleChangeEndPoint.y);
        ctx.lineTo(endPoint.x, endPoint.y);
    }
}

/**
 * Transforms a WarehouseLocation into an absolute Point in the canvas.
 */
function location2Point(shelving, side, row, trolleyIndex, trolleyCount) {
    let x;
    let y;
    if (side === LEFT) {
        x = shelving.x - SHELVING_LINE_WIDTH - ((0.5 * SHELVING_PADDING) / trolleyCount) * trolleyIndex;
    } else {
        x = shelving.x + shelving.width + SHELVING_LINE_WIDTH + ((0.5 * SHELVING_PADDING) / trolleyCount) * trolleyIndex;
    }
    const rowWidth = shelving.height / SHELVING_ROWS;
    y = shelving.y + row * rowWidth;

    return new Point(x, y);
}

/**
 * Get location for the aisle lane above or below a shelving side
 */
function location2AisleLane(shelving, side, isAbove, trolleyIndex, trolleyCount) {
    let x;
    let y;
    if (side === LEFT) {
        x = shelving.x - SHELVING_LINE_WIDTH - ((0.5 * SHELVING_PADDING) / trolleyCount) * trolleyIndex;
    } else {
        x = shelving.x + SHELVING_LINE_WIDTH + shelving.width + ((0.5 * SHELVING_PADDING) / trolleyCount) * trolleyIndex;
    }

    if (isAbove) {
        y = shelving.y - 0.25 * SHELVING_PADDING - (((0.25 * SHELVING_PADDING) / trolleyCount) * trolleyIndex);
    } else {
        y = shelving.y + shelving.height + 0.25 * SHELVING_PADDING + (((0.25 * SHELVING_PADDING) / trolleyCount) * trolleyIndex);
    }

    return new Point(x, y);
}

/**
 * Get the column number of a shelving
 */
function shelvingToColumn(shelving) {
    const COLUMN_WIDTH = SHELVING_WIDTH + SHELVING_PADDING;
    return (shelving.x - WAREHOUSE_PADDING_LEFT) / COLUMN_WIDTH;
}